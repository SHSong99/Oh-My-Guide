package com.ohmyguide.app.data.model

data class GoogleLoginRequest(val accessToken: String)

data class AuthResponse(val accessToken: String, val refreshToken: String)
