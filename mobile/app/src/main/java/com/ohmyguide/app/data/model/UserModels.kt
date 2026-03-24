package com.ohmyguide.app.data.model

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val imageUrl: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val onboardingCompleted: Boolean,
    val provider: String,
    val providerId: String?,
    val nationality: String?,
    val age: Int?,
    val gender: String?,
    val travelPurpose: String?,
    val lifestyle: String?,
)

data class OnboardingRequest(
    val nationality: String,
    val age: Int,
    val gender: String,
    val companion: String? = null,
    val country: String? = null,
)
