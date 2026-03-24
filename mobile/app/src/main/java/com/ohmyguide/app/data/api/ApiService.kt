package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.AttractionDetailDto
import com.ohmyguide.app.data.model.AuthResponse
import com.ohmyguide.app.data.model.GoogleLoginRequest
import com.ohmyguide.app.data.model.OnboardingRequest
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import com.ohmyguide.app.data.model.RefreshRecommendResponse
import com.ohmyguide.app.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse

    // User
    @GET("user/me")
    suspend fun getCurrentUser(): UserResponse

    @PUT("user/onboarding")
    suspend fun completeOnboarding(@Body request: OnboardingRequest): UserResponse

    // Recommend
    @GET("userRecommend")
    suspend fun getRecommendation(
        @Query("category") category: String,
        @Query("currentLat") currentLat: Double,
        @Query("currentLng") currentLng: Double,
    ): RefreshRecommendResponse

    @POST("userRecommend/recommend/refresh")
    suspend fun refreshRecommendation(@Body request: RefreshRecommendRequest): RefreshRecommendResponse

    @POST("userRecommend/visit")
    suspend fun visitPlace(@Body request: Map<String, Long>)

    // Attraction
    @GET("attractions/{attrId}")
    suspend fun getAttractionDetail(@Path("attrId") attrId: Long): AttractionDetailDto
}
