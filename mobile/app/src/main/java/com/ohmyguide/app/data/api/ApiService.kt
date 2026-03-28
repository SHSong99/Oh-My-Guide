package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.AttractionDetailDto
import com.ohmyguide.app.data.model.PhraseBookmarkDto
import com.ohmyguide.app.data.model.PickRecommendResponse
import com.ohmyguide.app.data.model.AuthResponse
import com.ohmyguide.app.data.model.ThemeDetailResponse
import com.ohmyguide.app.data.model.ThemeListResponse
import com.ohmyguide.app.data.model.GoogleLoginRequest
import com.ohmyguide.app.data.model.GuideNavigationResponse
import com.ohmyguide.app.data.model.OnboardingRequest
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import com.ohmyguide.app.data.model.RefreshRecommendResponse
import com.ohmyguide.app.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    // Guide (GO 버튼 → 네비게이션 시작, 방문기록 + 로그 전송 포함)
    @GET("guide/{placeId}")
    suspend fun startGuideNavigation(
        @Path("placeId") placeId: Long,
        @Query("currentLat") currentLat: Double,
        @Query("currentLng") currentLng: Double,
        @Query("reachLat") reachLat: Double,
        @Query("reachLng") reachLng: Double,
    ): GuideNavigationResponse

    // Attraction
    @GET("attractions/{attrId}")
    suspend fun getAttractionDetail(@Path("attrId") attrId: Long): AttractionDetailDto

    // Theme
    @GET("themes")
    suspend fun getThemes(): ThemeListResponse

    @GET("themes/{themeId}")
    suspend fun getThemeDetail(@Path("themeId") themeId: Long): ThemeDetailResponse

    // Phrases (북마크)
    @GET("phrases/bookmarks")
    suspend fun getBookmarkedPhrases(): List<PhraseBookmarkDto>

    @POST("phrases/{phraseId}/bookmark")
    suspend fun addPhraseBookmark(@Path("phraseId") phraseId: Long)

    @DELETE("phrases/{phraseId}/bookmark")
    suspend fun removePhraseBookmark(@Path("phraseId") phraseId: Long)

    // Rating (별점)
    @POST("guide/star")
    suspend fun submitRating(
        @Query("attrId") attrId: Long,
        @Query("star") star: Int,
    )

    // Pick Recommend (빅데이터 분산 추천)
    @GET("pickRecommend")
    suspend fun getPickRecommend(
        @Query("nationality") nationality: String,
        @Query("age") age: Int,
        @Query("gender") gender: String,
        @Query("travelPurpose") travelPurpose: String,
    ): List<PickRecommendResponse>
}
