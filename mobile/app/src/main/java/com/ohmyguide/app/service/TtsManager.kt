package com.ohmyguide.app.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.ohmyguide.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class TtsManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false
    private var currentText: String? = null
    private var speakGeneration = 0

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val progressUpdater = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        _progress.value = mp.currentPosition.toFloat() / mp.duration.coerceAtLeast(1)
                    }
                } catch (_: Exception) {}
            }
            handler.postDelayed(this, PROGRESS_INTERVAL)
        }
    }

    private val client = OkHttpClient()
    private val audioCache = object : LinkedHashMap<String, ByteArray>(16, 0.75f, true) {
        private var totalBytes = 0L
        override fun put(key: String, value: ByteArray): ByteArray? {
            totalBytes += value.size
            val prev = super.put(key, value)
            if (prev != null) totalBytes -= prev.size
            trimToSize()
            return prev
        }
        override fun remove(key: String): ByteArray? {
            val removed = super.remove(key)
            if (removed != null) totalBytes -= removed.size
            return removed
        }
        private fun trimToSize() {
            val iter = entries.iterator()
            while (totalBytes > MAX_CACHE_BYTES && iter.hasNext()) {
                val entry = iter.next()
                totalBytes -= entry.value.size
                iter.remove()
            }
        }
    }

    suspend fun speak(text: String) {
        stop()
        val gen = ++speakGeneration
        currentText = text
        isPaused = false
        _isLoading.value = true
        // 캐시에 있으면 즉시 사용
        val audioBytes = audioCache.remove(text) ?: fetchAudio(text)
        if (gen != speakGeneration) return
        _isLoading.value = false
        if (audioBytes == null) return
        playAudio(audioBytes)
    }

    // 다음 텍스트를 미리 다운로드 (백그라운드)
    suspend fun prefetch(text: String) {
        if (audioCache.containsKey(text)) return
        val bytes = fetchAudio(text) ?: return
        audioCache[text] = bytes
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
                _isSpeaking.value = false
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (isPaused) {
                it.start()
                isPaused = false
                _isSpeaking.value = true
            }
        }
    }

    fun hasPaused(): Boolean = isPaused && mediaPlayer != null

    fun stop() {
        speakGeneration++
        handler.removeCallbacks(progressUpdater)
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {}
            release()
        }
        mediaPlayer = null
        isPaused = false
        _isSpeaking.value = false
        _isLoading.value = false
        _progress.value = 0f
    }

    fun shutdown() {
        stop()
        currentText = null
    }

    private fun isKoreanText(text: String): Boolean =
        text.any { it in '\uAC00'..'\uD7A3' || it in '\u3131'..'\u318E' }

    private suspend fun fetchAudio(text: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val isKorean = isKoreanText(text)
            val lang = if (isKorean) LANGUAGE_KO else LANGUAGE_EN
            val voice = if (isKorean) VOICE_NAME_KO else VOICE_NAME_EN

            val json = JSONObject().apply {
                put("input", JSONObject().put("text", text))
                put("voice", JSONObject().apply {
                    put("languageCode", lang)
                    put("name", voice)
                    put("ssmlGender", "FEMALE")
                })
                put("audioConfig", JSONObject().apply {
                    put("audioEncoding", "MP3")
                    put("speakingRate", RATE)
                    if (!isKorean) put("pitch", PITCH)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GOOGLE_CLOUD_TTS_KEY}")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                if (BuildConfig.DEBUG) Log.e(TAG, "TTS API error: ${response.code} ${response.body?.string()}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val audioContent = JSONObject(body).getString("audioContent")
            Base64.decode(audioContent, Base64.DEFAULT)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "TTS fetch failed", e)
            null
        }
    }

    private suspend fun playAudio(audioBytes: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val tempFile = File.createTempFile("tts_", ".mp3", context.cacheDir)
            tempFile.writeBytes(audioBytes)

            withContext(Dispatchers.Main) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        start()
                        _isSpeaking.value = true
                        _progress.value = 0f
                        handler.post(progressUpdater)
                    }
                    setOnCompletionListener {
                        handler.removeCallbacks(progressUpdater)
                        _progress.value = 1f
                        _isSpeaking.value = false
                        isPaused = false
                        tempFile.delete()
                    }
                    setOnErrorListener { _, _, _ ->
                        handler.removeCallbacks(progressUpdater)
                        _progress.value = 0f
                        _isSpeaking.value = false
                        isPaused = false
                        tempFile.delete()
                        true
                    }
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Audio playback failed", e)
            _isSpeaking.value = false
        }
    }

    companion object {
        private const val TAG = "TtsManager"
        private const val BASE_URL = "https://texttospeech.googleapis.com/v1/text:synthesize"
        private const val LANGUAGE_EN = "en-US"
        private const val VOICE_NAME_EN = "en-US-Neural2-F"
        private const val LANGUAGE_KO = "ko-KR"
        private const val VOICE_NAME_KO = "ko-KR-Chirp3-HD-Leda"
        private const val RATE = 1.1
        private const val PITCH = 5.0
        private const val PROGRESS_INTERVAL = 150L
        private const val MAX_CACHE_BYTES = 20L * 1024 * 1024  // 20MB
    }
}