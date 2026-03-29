package com.ohmyguide.app.ui.screen.explore

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ohmyguide.app.data.model.ThemeInfoDto
import com.ohmyguide.app.data.repository.ThemeRepository
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.Region
import com.ohmyguide.app.fixtures.FEATURED_THEMES
import com.ohmyguide.app.fixtures.FeaturedTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _themes = MutableStateFlow(FEATURED_THEMES)
    val themes: StateFlow<List<FeaturedTheme>> = _themes.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _regions = MutableStateFlow(listOf(Region("all", "All")))
    val regions: StateFlow<List<Region>> = _regions.asStateFlow()

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val players = mutableMapOf<Int, ExoPlayer>()

    init {
        loadThemes()
    }

    private fun loadThemes() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            themeRepository.getThemes()
                .onSuccess { dtos ->
                    val courseList = dtos.map { it.toCourse() }
                    _courses.value = courseList
                    // 리전 동적 추출
                    val regionKeys = courseList.map { it.region }.distinct()
                    _regions.value = listOf(Region("all", "All")) + regionKeys.map { key ->
                        Region(key, key.replaceFirstChar { it.uppercase() })
                    }
                    _uiState.value = ExploreUiState.Idle
                    // 쇼케이스 테마 title/subtitle 서버 데이터로 업데이트
                    updateFeaturedThemes(dtos)
                    // 각 테마 상세 조회하여 썸네일 이미지 로드
                    loadThumbnails(dtos)
                }
                .onFailure { e ->
                    _uiState.value = ExploreUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun retry() {
        loadThemes()
    }

    private fun updateFeaturedThemes(dtos: List<ThemeInfoDto>) {
        val dtoMap = dtos.associateBy { it.themeId }
        _themes.value = _themes.value.map { theme ->
            val dto = dtoMap[theme.courseId]
            if (dto != null) {
                theme.copy(
                    title = dto.name,
                    subtitle = dto.description,
                    badge = dto.category,
                )
            } else theme
        }
    }

    private fun loadThumbnails(dtos: List<ThemeInfoDto>) {
        viewModelScope.launch {
            dtos.forEach { dto ->
                launch {
                    themeRepository.getThemeDetail(dto.themeId).onSuccess { detail ->
                        val imageUrl = detail.attractions.firstOrNull()?.image
                        if (imageUrl != null) {
                            _courses.value = _courses.value.map { course ->
                                if (course.id == dto.themeId.toString()) {
                                    course.copy(imageUrl = imageUrl)
                                } else course
                            }
                        }
                    }
                }
            }
        }
    }

    fun getOrCreatePlayer(index: Int): ExoPlayer {
        return players.getOrPut(index) {
            ExoPlayer.Builder(context).build().apply {
                val uri = Uri.parse(
                    "android.resource://${context.packageName}/${_themes.value[index].videoRes}"
                )
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                // 현재 페이지만 prepare (메모리 절약)
                if (index == _currentPage.value) {
                    prepare()
                    playWhenReady = true
                }
            }
        }
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
        // 현재 페이지만 prepare + play, 나머지는 stop으로 메모리 해제
        players.forEach { (index, player) ->
            if (index == page) {
                if (player.playbackState == Player.STATE_IDLE) {
                    player.prepare()
                }
                player.playWhenReady = true
            } else {
                player.playWhenReady = false
                player.stop()
            }
        }
    }

    fun pauseAll() {
        players.values.forEach { it.playWhenReady = false }
    }

    override fun onCleared() {
        super.onCleared()
        players.values.forEach { it.release() }
        players.clear()
    }
}

private fun ThemeInfoDto.toCourse(): Course {
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
        spotCount = 0,
        rating = 0f,
        tags = emptyList(),
        spots = emptyList(),
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