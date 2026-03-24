package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.model.OnboardingRequest
import com.ohmyguide.app.data.model.UserResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            Result.success(apiService.getCurrentUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeOnboarding(nationality: String, age: Int, gender: String): Result<UserResponse> {
        return try {
            val response = apiService.completeOnboarding(OnboardingRequest(nationality, age, gender))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
