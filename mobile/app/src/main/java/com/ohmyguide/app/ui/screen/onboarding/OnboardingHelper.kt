package com.ohmyguide.app.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.ohmyguide.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingHelper @Inject constructor(
    val userRepository: UserRepository,
) : ViewModel()
