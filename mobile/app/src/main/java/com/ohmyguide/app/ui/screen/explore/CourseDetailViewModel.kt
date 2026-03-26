package com.ohmyguide.app.ui.screen.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.ThemeDetailResponse
import com.ohmyguide.app.data.repository.ThemeRepository
import com.ohmyguide.app.domain.model.ThemeCourseCache
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.Spot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CourseDetailUiState {
    object Loading : CourseDetailUiState()
    data class Success(val course: Course) : CourseDetailUiState()
    data class Error(val message: String) : CourseDetailUiState()
}

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val themeId: Long = savedStateHandle.get<String>("courseId")?.toLongOrNull() ?: 0

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = CourseDetailUiState.Loading
            themeRepository.getThemeDetail(themeId)
                .onSuccess { detail ->
                    val course = detail.toCourse()
                    ThemeCourseCache.put(themeId.toString(), course)
                    _uiState.value = CourseDetailUiState.Success(course)
                }
                .onFailure { e ->
                    _uiState.value = CourseDetailUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun retry() {
        loadDetail()
    }
}

private fun ThemeDetailResponse.toCourse(): Course {
    val categoryKey = category.lowercase()
    val regionKey = mapRegion(region)
    val catGroup = EXPLORE_CATEGORY_GROUPS.find { it.key == categoryKey }
    return Course(
        id = themeId.toString(),
        title = name,
        subtitle = description,
        category = categoryKey,
        region = regionKey,
        emoji = catGroup?.emoji ?: "",
        duration = "",
        spotCount = attractionCount,
        rating = 0f,
        tags = emptyList(),
        spots = attractions
            .sortedBy { it.attractionOrder }
            .map { attr ->
                Spot(
                    id = attr.attractionId.toString(),
                    name = attr.title,
                    nameKr = "",
                    desc = attr.overview ?: "",
                    walkMin = 0,
                    imageUrl = attr.image,
                    lat = attr.latitude,
                    lng = attr.longitude,
                    overviewTts = attr.overviewTts,
                )
            },
        imageUrl = attractions.firstOrNull()?.image,
    )
}

private fun mapRegion(serverRegion: String): String = when (serverRegion) {
    "부산" -> "busan"
    "서울" -> "seoul"
    "대전" -> "daejeon"
    "제주" -> "jeju"
    "경주" -> "gyeongju"
    "인천" -> "incheon"
    "전주" -> "jeonju"
    else -> serverRegion.lowercase()
}
