package com.ohmyguide.app.ui.screen.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.repository.AuthRepository
import com.ohmyguide.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Welcome : SplashDestination()
    object Onboarding : SplashDestination()
    object Home : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        checkDestination()
    }

    fun checkDestination() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn().firstOrNull() ?: false
            if (!isLoggedIn) {
                _destination.value = SplashDestination.Welcome
                return@launch
            }
            val userResult = userRepository.getCurrentUser()
            _destination.value = userResult.fold(
                onSuccess = { user ->
                    if (user.onboardingCompleted) SplashDestination.Home
                    else SplashDestination.Onboarding
                },
                onFailure = {
                    Log.e("SplashViewModel", "Failed to fetch user", it)
                    SplashDestination.Welcome
                },
            )
        }
    }
}
