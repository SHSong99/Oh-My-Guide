package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.model.ThemeDetailResponse
import com.ohmyguide.app.data.model.ThemeInfoDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getThemes(): Result<List<ThemeInfoDto>> {
        return try {
            val response = apiService.getThemes()
            Result.success(response.themes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getThemeDetail(themeId: Long): Result<ThemeDetailResponse> {
        return try {
            Result.success(apiService.getThemeDetail(themeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
