package com.ohmyguide.app.service

import android.content.Context
import android.media.MediaPlayer
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

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val client = OkHttpClient()

    suspend fun speak(text: String) {
        stop()
        currentText = text
        isPaused = false
        val audioBytes = fetchAudio(text) ?: return
        playAudio(audioBytes)
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
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {}
            release()
        }
        mediaPlayer = null
        isPaused = false
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        currentText = null
    }

    private suspend fun fetchAudio(text: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("input", JSONObject().put("text", text))
                put("voice", JSONObject().apply {
                    put("languageCode", LANGUAGE)
                    put("name", VOICE_NAME)
                    put("ssmlGender", "FEMALE")
                })
                put("audioConfig", JSONObject().apply {
                    put("audioEncoding", "MP3")
                    put("speakingRate", RATE)
                    put("pitch", PITCH)
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
                    }
                    setOnCompletionListener {
                        _isSpeaking.value = false
                        isPaused = false
                        tempFile.delete()
                    }
                    setOnErrorListener { _, _, _ ->
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
        private const val LANGUAGE = "en-US"
        private const val VOICE_NAME = "en-US-Neural2-F"
        private const val RATE = 1.1
        private const val PITCH = 5.0
    }
}