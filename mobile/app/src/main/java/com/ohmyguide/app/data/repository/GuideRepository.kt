package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun submitRating(attrId: Long, star: Int): Result<Unit> {
        return try {
            apiService.submitRating(attrId, star)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
