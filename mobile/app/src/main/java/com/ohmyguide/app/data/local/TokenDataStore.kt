package com.ohmyguide.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenDataStore @Inject constructor(
    private val context: Context,
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = try {
        createEncryptedPrefs()
    } catch (e: Exception) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
        createEncryptedPrefs()
    }

    init {
        val oldFile = java.io.File(context.filesDir, "datastore/auth_prefs.preferences_pb")
        if (oldFile.exists()) oldFile.delete()
    }

    val accessToken: Flow<String?> = observeKey(KEY_ACCESS_TOKEN)
    val refreshToken: Flow<String?> = observeKey(KEY_REFRESH_TOKEN)

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    suspend fun clear() {
        prefs.edit().clear().apply()
    }

    private fun createEncryptedPrefs(): SharedPreferences =
        EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private fun observeKey(key: String): Flow<String?> = callbackFlow {
        trySend(prefs.getString(key, null))
        val listener = OnSharedPreferenceChangeListener { _, k ->
            if (k == key || k == null) trySend(prefs.getString(key, null))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
