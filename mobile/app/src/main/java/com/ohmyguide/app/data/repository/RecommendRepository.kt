package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.model.AttractionDetailDto
import com.ohmyguide.app.data.model.GuideNavigationResponse
import com.ohmyguide.app.data.model.PlaceCardDto
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getRecommendation(category: String, lat: Double, lng: Double): Result<List<PlaceCardDto>> {
        return try {
            val response = apiService.getRecommendation(category, lat, lng)
            Result.success(response.recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshRecommendation(request: RefreshRecommendRequest): Result<List<PlaceCardDto>> {
        return try {
            val response = apiService.refreshRecommendation(request)
            Result.success(response.recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startGuideNavigation(
        placeId: Long,
        currentLat: Double,
        currentLng: Double,
        reachLat: Double,
        reachLng: Double,
    ): Result<GuideNavigationResponse> {
        return try {
            val response = apiService.startGuideNavigation(placeId, currentLat, currentLng, reachLat, reachLng)
            Result.success(response)
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
