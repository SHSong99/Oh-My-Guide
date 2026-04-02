package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.local.TokenDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.contains("/auth/")) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenDataStore.accessToken.firstOrNull() }

        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        val response = chain.proceed(newRequest)

        if (response.code == 401 && token != null) {
            // TODO: 토큰 갱신 API 구현 후 refreshToken으로 자동 갱신 추가
            // 현재는 토큰 만료 시 clear하여 로그인 화면으로 유도
            runBlocking { tokenDataStore.clear() }
        }

        return response
    }
}
