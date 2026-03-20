package com.ohmyguide.app.ui.screen.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                Log.d(TAG, "Starting Google Sign-In, context: ${context::class.java.name}")
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d(TAG, "Requesting credential...")
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                Log.d(TAG, "Credential type: ${credential.type}")

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d(TAG, "Got ID token, requesting access token...")

                val authorizationRequest = AuthorizationRequest.builder()
                    .setRequestedScopes(listOf(Scope("email"), Scope("profile")))
                    .build()
                val authorizationResult = Identity.getAuthorizationClient(context)
                    .authorize(authorizationRequest)
                    .await()
                val accessToken = authorizationResult.accessToken
                    ?: throw IllegalStateException("Access token is null")
                Log.d(TAG, "Got access token, sending to backend...")

                val loginResult = authRepository.loginWithGoogle(accessToken)
                loginResult.fold(
                    onSuccess = {
                        Log.d(TAG, "Backend login success")
                        _authState.value = AuthState.Success
                    },
                    onFailure = {
                        Log.e(TAG, "Backend login failed: ${it.message}")
                        _authState.value = AuthState.Error(it.message ?: "Login failed")
                    },
                )
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User cancelled sign-in")
                _authState.value = AuthState.Idle
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google account on device", e)
                _authState.value = AuthState.Error("기기에 Google 계정이 없습니다.\n설정에서 Google 계정을 추가해주세요.")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed: ${e::class.java.simpleName}", e)
                _authState.value = AuthState.Error(
                    "${e::class.java.simpleName}: ${e.message ?: "Google Sign-In failed"}"
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
