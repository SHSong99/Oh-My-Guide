package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.model.AttractionDetailDto
import com.ohmyguide.app.data.model.OnboardingRequest
import com.ohmyguide.app.data.model.PickRecommendResponse
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

    suspend fun completeOnboarding(nationality: String, age: Int, gender: String, companion: String? = null, country: String? = null): Result<UserResponse> {
        return try {
            val response = apiService.completeOnboarding(OnboardingRequest(nationality, age, gender, companion, country))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPickRecommend(
        nationality: String,
        age: Int,
        gender: String,
        travelPurpose: String,
    ): Result<List<PickRecommendResponse>> {
        return try {
            Result.success(apiService.getPickRecommend(nationality, age, gender, travelPurpose))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttractionDetail(attrId: Long): Result<AttractionDetailDto> {
        return try {
            Result.success(apiService.getAttractionDetail(attrId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
