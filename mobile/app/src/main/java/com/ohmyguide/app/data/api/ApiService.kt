package com.ohmyguide.app.data.api

import retrofit2.Retrofit

// TODO: API 인터페이스 정의
interface ApiService
import com.ohmyguide.app.data.model.AuthResponse
import com.ohmyguide.app.data.model.GoogleLoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse
}
