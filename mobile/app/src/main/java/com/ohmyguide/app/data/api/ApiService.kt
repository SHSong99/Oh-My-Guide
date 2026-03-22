package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.AuthResponse
import com.ohmyguide.app.data.model.GoogleLoginRequest
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import com.ohmyguide.app.data.model.RefreshRecommendResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse

    @GET("api/userRecommend")
    suspend fun getRecommendation(
        @Query("category") category: String,
        @Query("currentLat") currentLat: Double,
        @Query("currentLng") currentLng: Double,
    ): RefreshRecommendResponse

    @POST("api/userRecommend/recommend/refresh")
    suspend fun refreshRecommendation(@Body request: RefreshRecommendRequest): RefreshRecommendResponse

    @POST("api/userRecommend/visit")
    suspend fun visitPlace(@Body request: Map<String, Long>)
}
