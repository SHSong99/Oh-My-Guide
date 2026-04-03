package com.ohmyguide.app.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.local.TokenDataStore
import com.ohmyguide.app.data.model.AuthResponse
import com.ohmyguide.app.data.model.GoogleLoginRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val response = apiService.loginWithGoogle(GoogleLoginRequest(accessToken = idToken))
            tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Flow<Boolean> = tokenDataStore.accessToken.map { it != null }

    suspend fun logout() {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
        tokenDataStore.clear()
    }
}
