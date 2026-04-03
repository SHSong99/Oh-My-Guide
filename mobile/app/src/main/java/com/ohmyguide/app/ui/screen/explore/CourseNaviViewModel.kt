package com.ohmyguide.app.ui.screen.explore

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.repository.NaverDirectionsRepository
import com.ohmyguide.app.data.repository.TmapRepository
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.domain.model.RouteCoord
import com.ohmyguide.app.domain.model.RouteSegmentGeo
import com.ohmyguide.app.data.model.GuidePlaceDto
import com.ohmyguide.app.data.model.GuideNavigationResponse
import com.ohmyguide.app.data.model.StartLocationDto
import com.ohmyguide.app.data.model.ThemeDetailResponse
import com.ohmyguide.app.data.repository.ThemeRepository
import com.ohmyguide.app.data.repository.WeatherRepository
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.domain.model.ThemeCourseCache
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.Spot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.ohmyguide.app.service.LocationForegroundService
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import com.ohmyguide.app.ui.theme.CourseLeg1
import com.ohmyguide.app.ui.theme.CourseLeg2
import com.ohmyguide.app.ui.theme.CourseLeg3
import com.ohmyguide.app.ui.theme.CourseLeg4
import com.ohmyguide.app.ui.theme.CourseLeg5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LEG_COLORS = listOf(CourseLeg1, CourseLeg2, CourseLeg3, CourseLeg4, CourseLeg5)

sealed class CourseNaviChatMessage {
    data class BotText(val text: String) : CourseNaviChatMessage()
    object BotTyping : CourseNaviChatMessage()
    data class SpotCard(val spot: Spot, val spotIndex: Int) : CourseNaviChatMessage()
}

data class CourseNaviUiState(
    val currentSpotIndex: Int = 0,
    val isLoading: Boolean = true,
    val transportMode: String = "car",
    val chatMessages: List<CourseNaviChatMessage> = emptyList(),
    val isAdvancing: Boolean = false,
    val guideQueue: List<String> = emptyList(),
    val tourCompleted: Boolean = false,
)

@HiltViewModel
class CourseNaviViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    savedStateHandle: SavedStateHandle,
    private val directionsRepository: NaverDirectionsRepository,
    private val tmapRepository: TmapRepository,
    private val themeRepository: ThemeRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""
    private val initialMode: String = savedStateHandle["mode"] ?: "car"

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _routeData = MutableStateFlow<NaviRouteData?>(null)
    val routeData: StateFlow<NaviRouteData?> = _routeData.asStateFlow()

    private val _uiState = MutableStateFlow(CourseNaviUiState(transportMode = initialMode))
    val uiState: StateFlow<CourseNaviUiState> = _uiState.asStateFlow()

    private val _spotAdvanceEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val spotAdvanceEvent: SharedFlow<String> = _spotAdvanceEvent.asSharedFlow()

    private var gpsJob: Job? = null
    private val arrivedSpots = mutableSetOf<Int>()
    private var nextGuideIdx = 0  // 가이드 큐에 추가할 다음 스팟 인덱스

    init {
        loadCourse()
    }

    private fun loadCourse() {
        // Try cache first
        val cached = ThemeCourseCache.get(courseId)
        if (cached != null) {
            _course.value = cached
            cacheSpotGuideData(cached)
            fetchCourseRoute()
            initCourseChat(cached)   // 인사 큐 먼저
            startGpsTracking()       // GPS는 그 후
            return
        }
        val themeId = courseId.toLongOrNull() ?: return
        viewModelScope.launch {
            themeRepository.getThemeDetail(themeId)
                .onSuccess { detail ->
                    val c = detail.toCourse()
                    ThemeCourseCache.put(courseId, c)
                    _course.value = c
                    cacheSpotGuideData(c)
                    fetchCourseRoute()
                    initCourseChat(c)   // 인사 큐 먼저
                    startGpsTracking()  // GPS는 그 후
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun fetchCourseRoute() {
        val spots = _course.value?.spots ?: return
        if (spots.size < 2) return

        viewModelScope.launch {
            // 코스 경로는 항상 1번 스팟부터 시작 (GPS 위치를 시작점으로 사용하지 않음)
            val allPoints = spots.map { it.lat to it.lng }

            // 좌표 0.0 검증 — 유효하지 않은 좌표가 있으면 fallback
            val hasInvalidCoords = allPoints.any { (lat, lng) -> lat == 0.0 || lng == 0.0 }
            if (hasInvalidCoords) {
                if (BuildConfig.DEBUG) {
                    Log.w("CourseNaviVM", "Invalid spot coordinates detected, using fallback")
                }
                buildFallbackRoute(allPoints.filter { (lat, lng) -> lat != 0.0 && lng != 0.0 })
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val start = allPoints.first()
            val goal = allPoints.last()
            val waypoints = allPoints.drop(1).dropLast(1)

            val mode = _uiState.value.transportMode
            val result = when (mode) {
                "walk" -> {
                    // 도보: Tmap 보행 경로 (waypoint 미지원이므로 start→goal 직접)
                    tmapRepository.getWalkingRoute(
                        startLat = start.first,
                        startLng = start.second,
                        endLat = goal.first,
                        endLng = goal.second,
                    )
                }
                else -> {
                    // 자차: Naver Directions
                    if (waypoints.isNotEmpty()) {
                        directionsRepository.getDrivingRouteWithWaypoints(
                            startLat = start.first,
                            startLng = start.second,
                            waypoints = waypoints,
                            endLat = goal.first,
                            endLng = goal.second,
                        )
                    } else {
                        directionsRepository.getDrivingRoute(
                            startLat = start.first,
                            startLng = start.second,
                            endLat = goal.first,
                            endLng = goal.second,
                        )
                    }
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    if (BuildConfig.DEBUG) {
                        Log.d("CourseNaviVM", "Route OK: ${result.data.size} coords")
                    }
                    buildRouteSegments(result.data, allPoints)
                }
                is ApiResult.Error -> {
                    if (BuildConfig.DEBUG) {
                        Log.e("CourseNaviVM", "Route FAIL: ${result.message}")
                    }
                    // Fallback: straight lines between spots
                    buildFallbackRoute(allPoints)
                }
                else -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun buildRouteSegments(
        allCoords: List<RouteCoord>,
        waypoints: List<Pair<Double, Double>>,
    ) {
        // Single path from API — split into segments by nearest waypoint
        val segments = mutableListOf<RouteSegmentGeo>()
        val spots = _course.value?.spots ?: return

        if (waypoints.size <= 2) {
            // Simple A→B, one segment
            segments.add(
                RouteSegmentGeo(
                    type = "car",
                    coords = allCoords,
                    color = LEG_COLORS[0],
                    lineName = "${spots.first().name} → ${spots.last().name}",
                    fromName = spots.first().name,
                    toName = spots.last().name,
                )
            )
        } else {
            // Split path by finding closest point to each waypoint
            val splitIndices = mutableListOf(0)
            for (wpIdx in 1 until waypoints.size - 1) {
                val (wpLat, wpLng) = waypoints[wpIdx]
                var minDist = Double.MAX_VALUE
                var minIdx = splitIndices.last()
                for (i in splitIndices.last() until allCoords.size) {
                    val d = sqDist(allCoords[i].lat, allCoords[i].lng, wpLat, wpLng)
                    if (d < minDist) {
                        minDist = d
                        minIdx = i
                    }
                }
                splitIndices.add(minIdx)
            }
            splitIndices.add(allCoords.size - 1)

            for (i in 0 until splitIndices.size - 1) {
                val from = splitIndices[i]
                val to = splitIndices[i + 1] + 1
                val segCoords = allCoords.subList(from.coerceAtMost(allCoords.size - 1), to.coerceAtMost(allCoords.size))
                if (segCoords.size >= 2) {
                    val spotIdx = i.coerceAtMost(spots.size - 1)
                    val nextSpotIdx = (i + 1).coerceAtMost(spots.size - 1)
                    segments.add(
                        RouteSegmentGeo(
                            type = "car",
                            coords = segCoords,
                            color = LEG_COLORS[i % LEG_COLORS.size],
                            lineName = "Leg ${i + 1}",
                            fromName = spots[spotIdx].name,
                            toName = spots[nextSpotIdx].name,
                        )
                    )
                }
            }
        }

        _routeData.value = NaviRouteData(
            mode = "car",
            segments = segments,
            totalDurationMin = 0,
        )
    }

    private fun buildFallbackRoute(points: List<Pair<Double, Double>>) {
        val spots = _course.value?.spots ?: return
        val segments = mutableListOf<RouteSegmentGeo>()

        for (i in 0 until points.size - 1) {
            val spotIdx = i.coerceAtMost(spots.size - 1)
            val nextSpotIdx = (i + 1).coerceAtMost(spots.size - 1)
            segments.add(
                RouteSegmentGeo(
                    type = "car",
                    coords = listOf(
                        RouteCoord(points[i].first, points[i].second),
                        RouteCoord(points[i + 1].first, points[i + 1].second),
                    ),
                    color = LEG_COLORS[i % LEG_COLORS.size],
                    lineName = "Leg ${i + 1}",
                    fromName = spots[spotIdx].name,
                    toName = spots[nextSpotIdx].name,
                )
            )
        }

        _routeData.value = NaviRouteData(
            mode = "car",
            segments = segments,
            totalDurationMin = 0,
        )
    }

    private fun cacheSpotGuideData(course: Course) {
        course.spots.forEach { spot ->
            PlaceDetailCache.putGuide(spot.id, GuideNavigationResponse(
                startLocation = StartLocationDto(0.0, 0.0),
                destination = GuidePlaceDto(
                    placeId = spot.id.toLongOrNull() ?: 0L,
                    title = spot.name,
                    addr1 = null,
                    latitude = spot.lat,
                    longitude = spot.lng,
                    firstImage1 = spot.imageUrl,
                    overview = spot.desc,
                    overviewTts = spot.overviewTts,
                ),
                nearbyPlaces = emptyList(),
            ))
        }
    }

    // ── Chat System ──

    private fun addMessage(msg: CourseNaviChatMessage) {
        _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    // 가이드 큐에 텍스트 추가 (Screen에서 하나씩 꺼내서 TTS + 팝업)
    fun enqueueGuide(text: String) {
        // TTS가 자연스럽게 읽도록 치환
        val ttsText = text.replace("SSAFY", "싸피")
        if (com.ohmyguide.app.BuildConfig.DEBUG) {
            android.util.Log.d("CourseNaviVM", "enqueue: ${ttsText.take(30)}... queueSize=${_uiState.value.guideQueue.size + 1}")
        }
        _uiState.update { it.copy(guideQueue = it.guideQueue + ttsText) }
    }

    // 큐 맨 앞 소비 → 특수 명령이면 처리
    fun dequeueGuide() {
        val queue = _uiState.value.guideQueue
        if (queue.isEmpty()) return
        val consumed = queue.first()
        _uiState.update { it.copy(guideQueue = it.guideQueue.drop(1)) }

        // 특수 명령 처리
        if (consumed.startsWith("__ADVANCE__")) {
            val nextIdx = consumed.removePrefix("__ADVANCE__").toIntOrNull() ?: return
            val spots = _course.value?.spots ?: return
            advanceToSpot(nextIdx, spots)
        } else if (consumed == "__SOUND__") {
            com.ohmyguide.app.service.NotificationSoundPlayer.play(appContext)
        } else if (consumed == "__COMPLETE__") {
            _uiState.update { it.copy(tourCompleted = true) }
        }
    }

    private fun removeTyping() {
        _uiState.update { state ->
            state.copy(chatMessages = state.chatMessages.filterNot { it is CourseNaviChatMessage.BotTyping })
        }
    }

    private fun initCourseChat(course: Course) {
        val firstSpot = course.spots.firstOrNull() ?: return
        val lastSpot = course.spots.lastOrNull() ?: return

        // 큐에 순서대로 넣기 (Screen에서 하나씩 꺼내서 TTS)
        enqueueGuide(
            "안녕하세요! 이번 ${course.title} 가이드를 맡은 깨비에요! " +
            "총 ${course.spots.size}개의 장소를 함께 둘러볼 건데요, " +
            "${firstSpot.name}을 시작으로 ${lastSpot.name}까지 가보도록 하겠습니다!"
        )

        // 날씨 비동기 조회 → 큐에 추가
        viewModelScope.launch {
            fetchWeatherAndEnqueue(firstSpot.lat, firstSpot.lng)
        }

        enqueueGuide(
            "자, 그럼 첫 번째 장소인 ${firstSpot.name}(으)로 출발할게요! 천천히 걸어가면서 주변도 둘러보세요."
        )
    }

    private suspend fun fetchWeatherAndEnqueue(lat: Double, lng: Double) {
        when (val result = weatherRepository.getHourlyForecast(lat, lng)) {
            is ApiResult.Success -> {
                val hourly = result.data.hourly ?: return
                val now = java.time.LocalTime.now().hour
                val temp = hourly.temperature?.getOrNull(now)
                val code = hourly.weatherCode?.getOrNull(now)
                val weatherEmoji = when (code) {
                    0 -> "☀️"; 1, 2, 3 -> "⛅"; 45, 48 -> "🌫️"
                    51, 53, 55, 61, 63, 65 -> "🌧️"; 71, 73, 75 -> "❄️"
                    95, 96, 99 -> "⛈️"; else -> "🌤️"
                }
                val weatherDesc = when (code) {
                    0 -> "맑음"; 1, 2, 3 -> "구름 조금"; 45, 48 -> "안개"
                    51, 53, 55, 61, 63, 65 -> "비"; 71, 73, 75 -> "눈"
                    95, 96, 99 -> "천둥번개"; else -> "맑음"
                }
                // 인사 다음, 출발 전에 날씨를 끼워넣기 (큐의 1번 위치)
                _uiState.update { state ->
                    val queue = state.guideQueue.toMutableList()
                    val insertIdx = 1.coerceAtMost(queue.size)
                    val precip = hourly.precipitationProbability?.getOrNull(now) ?: 0
                    val wind = hourly.windSpeed?.getOrNull(now) ?: 0.0
                    val tip = buildString {
                        append("출발 전에 날씨를 알려드릴게요! 지금 기온은 ${temp?.toInt() ?: "--"}도이고, ${weatherDesc}이에요. ")
                        when {
                            (temp ?: 20.0) >= 30 -> append("꽤 더운 날이니까 물 꼭 챙기시고, 그늘에서 쉬어가며 걸어주세요. ")
                            (temp ?: 20.0) >= 20 -> append("산책하기 딱 좋은 날씨네요! ")
                            (temp ?: 20.0) >= 10 -> append("살짝 쌀쌀할 수 있으니 가벼운 겉옷 하나 챙기시면 좋겠어요. ")
                            else -> append("꽤 추운 날이에요. 따뜻하게 입고 나오셨죠? ")
                        }
                        if (precip >= 50) append("비 올 확률이 ${precip}퍼센트라서, 우산도 준비해주세요! ")
                        if (wind >= 8) append("바람이 좀 불 수 있으니 모자 조심하세요! ")
                    }
                    queue.add(insertIdx, tip)
                    state.copy(guideQueue = queue)
                }
            }
            else -> {}
        }
    }

    fun setTransportMode(mode: String) {
        _uiState.update { it.copy(transportMode = mode, isLoading = true) }
        _routeData.value = null
        fetchCourseRoute()
    }

    fun selectSpot(index: Int) {
        val course = _course.value ?: return
        val spot = course.spots.getOrNull(index) ?: return
        _uiState.update { it.copy(currentSpotIndex = index) }

        viewModelScope.launch {
            addMessage(CourseNaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(CourseNaviChatMessage.BotText(
                "${index + 1}번째 장소: ${spot.name}(으)로 이동할게요!"
            ))
            addMessage(CourseNaviChatMessage.SpotCard(spot = spot, spotIndex = index))
        }
    }

    // ── GPS Tracking ──

    // 스팟 전환 (큐에서 __ADVANCE__ 소비 시 호출됨)
    private fun advanceToSpot(nextIdx: Int, spots: List<Spot>) {
        _uiState.update { it.copy(currentSpotIndex = nextIdx) }
    }

    private fun startGpsTracking() {
        gpsJob?.cancel()
        gpsJob = viewModelScope.launch {
            LocationForegroundService.locationFlow.collect { location ->
                location ?: return@collect
                val userLat = location.latitude
                val userLng = location.longitude

                val spots = _course.value?.spots ?: return@collect
                val currentIdx = _uiState.value.currentSpotIndex
                val currentSpot = spots.getOrNull(currentIdx) ?: return@collect


                // 현재 스팟 + 아직 미도착 스팟 모두 감지 (이전 가이드 재생 중에도 미리 큐에 쌓기)
                for (checkIdx in 0..spots.lastIndex) {
                    if (checkIdx in arrivedSpots) continue
                    val checkSpot = spots[checkIdx]
                    val dist = haversineMeters(userLat, userLng, checkSpot.lat, checkSpot.lng)
                    if (com.ohmyguide.app.BuildConfig.DEBUG && checkIdx == currentIdx) {
                        android.util.Log.d("CourseNaviVM", "GPS: ($userLat, $userLng) → ${checkSpot.name}($checkIdx) dist=${dist.toInt()}m, arrived=$arrivedSpots")
                    }
                    if (dist < ARRIVAL_THRESHOLD_METERS) {
                        arrivedSpots.add(checkIdx)
                    }
                }
                // 가이드 큐는 순서대로만 추가 (1→2→3→4)
                while (nextGuideIdx <= spots.lastIndex && nextGuideIdx in arrivedSpots) {
                    enqueueSpotGuide(nextGuideIdx, spots)
                    nextGuideIdx++
                }

            }
        }
    }

    private fun enqueueSpotGuide(spotIdx: Int, spots: List<com.ohmyguide.app.fixtures.Spot>) {
        val spot = spots[spotIdx]

        enqueueGuide("__SOUND__")

        val guideText = spot.overviewTts ?: spot.desc
        if (guideText.isNotBlank()) {
            enqueueGuide(guideText)
        }

        if (spotIdx < spots.lastIndex) {
            val nextSpot = spots[spotIdx + 1]
            enqueueGuide("다음 장소는 ${nextSpot.name}이에요. 이동해볼까요?")
            // SSAFY 코스 전용: 3번→4번 이동 중 녹산공단 설명
            if (courseId == "6" && spotIdx == 2) {
                enqueueGuide("조금 더 덧붙여서 이야기 드리자면, 놀랍게도 이곳은 원래 바다였어요! 1990년에 매립을 시작해서 만든 곳이랍니다.")
                enqueueGuide("약 27,000명이 일하고 있고, 특히 조선기자재 업체가 아주 많이 모여 있어서, 이곳 없으면 배를 못 만든다는 말이 있을 정도예요! 삼성전기, 농심, 대한제강 같은 큰 기업들도 여기 있답니다.")
                enqueueGuide("바로 옆에 부산신항이 있고, 앞으로 가덕도신공항도 가까이 생길 예정이라 공항·항만·철도가 모이는 물류 중심지가 되고 있어요. 지금은 스마트그린산단으로 변신 중이랍니다!")
            }
            enqueueGuide("__ADVANCE__${spotIdx + 1}")
        } else {
            enqueueGuide("축하합니다! ${_course.value?.title ?: "코스"}를 모두 완료했어요! 즐거운 산책이었길 바랍니다.")
            enqueueGuide("깨비가 이번에 준비한 코스는 여기까지지만, 테마에 깨비가 준비한 다른 코스들도 많이 있으니 다음에도 꼭 함께해 주세요! 더 좋은 가이드로 찾아뵐게요!")
            enqueueGuide("__COMPLETE__")
        }
    }

    override fun onCleared() {
        super.onCleared()
        gpsJob?.cancel()
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun sqDist(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = lat1 - lat2
        val dLng = lng1 - lng2
        return dLat * dLat + dLng * dLng
    }

    companion object {
        private const val ARRIVAL_THRESHOLD_METERS = 30.0
        private const val ADVANCE_THRESHOLD_METERS = 150.0
    }
}

private fun ThemeDetailResponse.toCourse(): Course {
    val categoryKey = category.lowercase()
    val regionKey = when (region) {
        "부산" -> "busan"; "서울" -> "seoul"; "대전" -> "daejeon"
        "제주" -> "jeju"; "경주" -> "gyeongju"; "인천" -> "incheon"
        "전주" -> "jeonju"; else -> region.lowercase()
    }
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
        spots = attractions.sortedBy { it.attractionOrder }.map { attr ->
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