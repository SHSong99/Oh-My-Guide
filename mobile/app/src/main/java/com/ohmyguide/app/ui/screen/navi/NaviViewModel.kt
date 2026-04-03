package com.ohmyguide.app.ui.screen.navi

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.ohmyguide.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.repository.NaverDirectionsRepository
import com.ohmyguide.app.data.repository.TmapRepository
import com.ohmyguide.app.data.repository.WeatherRepository
import com.ohmyguide.app.domain.model.NaviRouteCache
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.domain.model.RouteCoord
import com.ohmyguide.app.domain.model.RouteSegmentGeo
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.domain.model.ThemeCourseCache
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.SAMPLE_PLACES
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.data.model.GuidePlaceDto
import com.ohmyguide.app.ui.theme.CatAttraction
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitGray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ── Chat Messages ──

@Immutable
data class PhraseItem(
    val korean: String,
    val romanization: String,
    val english: String,
)

@Immutable
data class TransitStopInfo(
    val stopName: String,
    val busNumber: String,
    val remainingStops: Int,
    val exitStopName: String,
)

@Immutable
data class TransitGuideInfo(
    val type: String,           // "board" or "alight"
    val transitType: String,    // "bus" or "subway"
    val lineName: String,       // "Bus 201", "Line 2"
    val stationName: String,    // station name (Korean)
    val stationNameEn: String,  // station name (English)
    val stopsCount: Int = 0,
    val exitStation: String = "",
    val exitStationEn: String = "",
)

@Immutable
data class WeatherInfo(
    val temperature: Double,
    val feelsLike: Double,
    val weatherDesc: String,
    val emoji: String,
    val precipProbability: Int,
    val windSpeed: Double,
    val isDay: Boolean,
    val tip: String,
    val hourlyForecast: List<HourForecast> = emptyList(),
)

@Immutable
data class HourForecast(
    val hour: Int,
    val temp: Double,
    val emoji: String,
    val precipProb: Int,
)

@Immutable
data class NearbySpotInfo(
    val placeId: String,
    val name: String,
    val imageUrl: String?,
    val overview: String?,
    val lat: Double,
    val lng: Double,
    val routeProgress: Float = 0f,  // 경로상 위치 (0.0~1.0)
)

sealed class NaviChatMessage {
    data class BotText(val text: String) : NaviChatMessage()
    object BotTyping : NaviChatMessage()
    data class PlaceIntro(val detail: PlaceDetail, val distance: String, val eta: String) : NaviChatMessage()
    data class TransitInfo(val info: TransitStopInfo) : NaviChatMessage()
    data class TransitGuide(val info: TransitGuideInfo) : NaviChatMessage()
    data class DestinationDetail(val detail: PlaceDetail) : NaviChatMessage()
    data class NearbyPlaces(
        val places: List<Place>,
    ) : NaviChatMessage()
    data class NearbySpotCard(val spot: NearbySpotInfo) : NaviChatMessage()
    data class Phrases(val items: List<PhraseItem>) : NaviChatMessage()
    object ArrivalConfirm : NaviChatMessage()
    data class NearbyRecommendations(val places: List<Place>) : NaviChatMessage()
    data class Weather(val info: WeatherInfo) : NaviChatMessage()
    data class StoryPrompt(val placeName: String) : NaviChatMessage()
}

// ── UI State ──

@Immutable
data class NaviUiState(
    val chatMessages: List<NaviChatMessage> = emptyList(),
    val arrived: Boolean = false,
    val progressPct: Float = 0f,
    val userLat: Double = 0.0,
    val userLng: Double = 0.0,
    val guideReady: Boolean = false,
    val pickedNearbySpots: List<NearbySpotInfo> = emptyList(),
    // Course mode
    val course: Course? = null,
    val spotIndex: Int = 0,
    val totalSpots: Int = 0,
)

@HiltViewModel
class NaviViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
    private val naviRouteCache: NaviRouteCache,
    private val directionsRepository: NaverDirectionsRepository,
    private val tmapRepository: TmapRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val s get() = LanguageManager.current.value.strings

    val placeId: String = savedStateHandle["placeId"] ?: "dm3"
    val mode: String = savedStateHandle["mode"] ?: "walk"
    val courseId: String? = savedStateHandle.get<String>("courseId")?.ifEmpty { null }
    val spotIndex: Int = savedStateHandle.get<String>("spotIndex")?.toIntOrNull() ?: 0

    val course: Course? = courseId?.let { ThemeCourseCache.get(it) }
    val isCourseMode: Boolean = course != null

    val detail: PlaceDetail? = PlaceDetailCache.get(placeId)
        ?: SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()

    private val guideData = PlaceDetailCache.getGuide(placeId)

    private val _naviRoute = MutableStateFlow<NaviRouteData?>(
        if (mode == "transit") naviRouteCache.peek() else null
    )
    val naviRoute: StateFlow<NaviRouteData?> = _naviRoute.asStateFlow()

    private val route = FALLBACK_ROUTES[placeId to mode]
    private val totalDistance = route?.distanceMeters ?: 1500
    private val totalDuration = _naviRoute.value?.totalDurationMin ?: route?.durationMin ?: 5

    val destLat = detail?.place?.lat?.takeIf { it != 0.0 }
        ?: PLACE_COORDINATES[placeId]?.first ?: 37.5700
    val destLng = detail?.place?.lng?.takeIf { it != 0.0 }
        ?: PLACE_COORDINATES[placeId]?.second ?: 126.9990
    private val destinationLat = destLat
    private val destinationLng = destLng

    private val _uiState = MutableStateFlow(NaviUiState())
    val uiState: StateFlow<NaviUiState> = _uiState.asStateFlow()

    private var gpsJob: Job? = null
    private var nearbyRecommendEnabled = false
    private var arrivalPromptShown = false
    private var nextNearbyIndex = 0
    private var pickedSpots: List<NearbySpotInfo> = emptyList()

    // Transit 세그먼트별 탑승/하차 안내 추적
    private val boardedSegments = mutableSetOf<Int>()   // 탑승 안내 완료
    private val alightedSegments = mutableSetOf<Int>()   // 하차 안내 완료

    private fun notifyUser() {
        try {
            // 비프음
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            viewModelScope.launch {
                delay(200)
                try { tone.release() } catch (_: Exception) {}
            }
            // 약한 진동
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    companion object {
        private val PLACE_COORDINATES = mapOf(
            "dm3" to (35.0807 to 128.8785),
            "dm4" to (35.1044 to 128.9459),
            "dm5" to (35.1795 to 128.9383),
            "dm6" to (35.2110 to 128.9722),
            "dm7" to (35.0720 to 128.9650),
            "p3" to (35.0850 to 128.9200),
            "p4" to (35.0530 to 128.9580),
            "p5" to (35.0470 to 128.9660),
            // Course spots
            "dh1" to (37.5265 to 127.0405),
            "dh2" to (37.5563 to 126.9236),
            "dh3" to (37.5586 to 126.9267),
        )
        private const val ARRIVAL_THRESHOLD_METERS = 30.0
        private const val ROUTE_NEARBY_RADIUS = 500.0      // 경로에서 500m 이내 장소만 선별
        private const val MAX_NEARBY_RECOMMENDATIONS = 5

        private val USEFUL_PHRASES = listOf(
            PhraseItem("이거 주세요", "i-geo ju-se-yo", "This one, please"),
            PhraseItem("얼마예요?", "eol-ma-ye-yo?", "How much is it?"),
            PhraseItem("화장실 어디예요?", "hwa-jang-sil eo-di-ye-yo?", "Where's the restroom?"),
            PhraseItem("감사합니다", "gam-sa-ham-ni-da", "Thank you"),
            PhraseItem("맛있어요!", "ma-si-sseo-yo!", "It's delicious!"),
        )

        private const val TRANSIT_BOARD_RADIUS = 100.0
    }

    init {
        if (isCourseMode) {
            _uiState.update {
                it.copy(
                    course = course,
                    spotIndex = spotIndex,
                    totalSpots = course?.spots?.size ?: 0,
                )
            }
        }
        initChat()
        if (mode == "transit") {
            startGpsTracking()
        } else {
            fetchDirectionsRoute()
        }
    }

    private fun initChat() {
        val placeName = detail?.place?.name ?: s.destination

        if (isCourseMode) {
            val courseName = course?.title ?: ""
            val total = course?.spots?.size ?: 0
            addMessage(NaviChatMessage.BotText(
                s.naviCourseSpotProgress
                    .replaceFirst("%s", courseName)
                    .replaceFirst("%d", "${spotIndex + 1}")
                    .replaceFirst("%d", "$total")
            ))
        }

        viewModelScope.launch {
            // t=0s — 전체 경로 줌아웃 상태

            // t=2s — 깨비 인사 오버레이 표시 + 줌인 준비
            delay(2000L)
            _uiState.update { it.copy(guideReady = true) }

            // t=8s — 깨비 인사(3s) + 줌인(2.5s) 완료 후 날씨로 바로 전환
            delay(6000L)
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            fetchWeatherAndShow()
            notifyUser()

            // t=13s — 이동 방법 안내
            delay(5000L)
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            when (mode) {
                "transit" -> {
                    val transitSegments = _naviRoute.value?.segments
                        ?.filter { it.type == "bus" || it.type == "subway" } ?: emptyList()
                    if (transitSegments.isNotEmpty()) {
                        val first = transitSegments.first()
                        addMessage(NaviChatMessage.TransitGuide(
                            TransitGuideInfo(
                                type = "board",
                                transitType = first.type,
                                lineName = first.lineName,
                                stationName = first.fromNameKr.ifEmpty { first.fromName },
                                stationNameEn = first.fromName,
                                stopsCount = first.stopsCount,
                                exitStation = first.toNameKr.ifEmpty { first.toName },
                                exitStationEn = first.toName,
                            )
                        ))
                        if (transitSegments.size > 1) {
                            addMessage(NaviChatMessage.BotText(
                                s.naviTransferAhead.replace("%d", "${transitSegments.size - 1}")
                            ))
                        }
                    } else {
                        addMessage(NaviChatMessage.BotText(
                            s.naviFollowTransit.replace("%d", "$totalDuration")
                        ))
                    }
                }
                "car" -> {
                    addMessage(NaviChatMessage.BotText(
                        s.naviDriveTime.replace("%d", "$totalDuration")
                    ))
                }
                else -> {
                    addMessage(NaviChatMessage.BotText(
                        s.naviWalkTime.replace("%d", "$totalDuration")
                    ))
                }
            }
            notifyUser()

            // t=18s — 목적지 가이드 유도
            val hasTts = !guideData?.destination?.overviewTts.isNullOrBlank()
                || !guideData?.destination?.overview.isNullOrBlank()
                || !detail?.desc.isNullOrBlank()
            if (hasTts) {
                delay(5000L)
                addMessage(NaviChatMessage.BotTyping)
                delay(800L)
                removeTyping()
                addMessage(NaviChatMessage.StoryPrompt(placeName = placeName))
                notifyUser()
            }

            // t=30s — 근처 장소 추천 활성화
            delay(if (hasTts) 12000L else 17000L)
            nearbyRecommendEnabled = true
        }
    }

    // ── Directions API (walk / car) ──

    private fun fetchDirectionsRoute() {
        viewModelScope.launch {
            // GPS가 잡힐 때까지 대기 (타임아웃 없음)
            val location = LocationForegroundService.locationFlow.value
                ?: LocationForegroundService.locationFlow.filterNotNull().first()
            val startLat = location.latitude
            val startLng = location.longitude

            val result = when (mode) {
                "car" -> directionsRepository.getDrivingRoute(
                    startLat, startLng, destinationLat, destinationLng,
                )
                else -> tmapRepository.getWalkingRoute(
                    startLat, startLng, destinationLat, destinationLng,
                )
            }

            if (BuildConfig.DEBUG) {
                when (result) {
                    is ApiResult.Success -> Log.d("NaviVM", "[$mode] Directions OK: ${result.data.size} coords")
                    is ApiResult.Error -> Log.e("NaviVM", "[$mode] Directions FAIL: code=${result.code} msg=${result.message}")
                    is ApiResult.Loading -> {}
                }
            }

            if (result is ApiResult.Success && result.data.size >= 2) {
                val color = if (mode == "car") TransitAmber else TransitGray
                val s = LanguageManager.current.value.strings
                _naviRoute.value = NaviRouteData(
                    mode = mode,
                    segments = listOf(
                        RouteSegmentGeo(
                            type = mode,
                            coords = result.data,
                            color = color,
                            lineName = if (mode == "car") s.taxi else s.walk,
                            fromName = s.currentLocation,
                            toName = detail?.place?.name ?: s.destination,
                        ),
                    ),
                    totalDurationMin = totalDuration,
                )
                pickNearbySpotsAlongRoute(result.data)
            }

            startGpsTracking()
        }
    }

    // ── Real-time GPS Tracking ──

    private fun allRouteCoords(): List<RouteCoord>? {
        val nr = _naviRoute.value ?: return null
        val all = nr.segments.flatMap { it.coords }
        return if (all.size >= 2) all else null
    }

    private fun startGpsTracking() {
        val transitCoords = allRouteCoords()
        val routePoints = if (transitCoords != null) {
            // transit 모드에서도 경로 기반 장소 선별
            if (pickedSpots.isEmpty()) pickNearbySpotsAlongRoute(transitCoords)
            transitCoords.map { com.ohmyguide.app.fixtures.RoutePoint(it.lat, it.lng) }
        } else {
            route?.points ?: return
        }
        if (routePoints.size < 2) return

        gpsJob = viewModelScope.launch {
            LocationForegroundService.locationFlow.collect { locationData ->
                if (_uiState.value.arrived) return@collect
                val loc = locationData ?: return@collect

                val userLat = loc.latitude
                val userLng = loc.longitude

                // Find closest point on route → calculate progress
                var closestIdx = 0
                var closestDist = Double.MAX_VALUE
                routePoints.forEachIndexed { i, pt ->
                    val d = haversineMeters(userLat, userLng, pt.lat, pt.lng)
                    if (d < closestDist) {
                        closestDist = d
                        closestIdx = i
                    }
                }
                val progress = (closestIdx + 1).toFloat() / routePoints.size

                _uiState.update {
                    it.copy(
                        userLat = userLat,
                        userLng = userLng,
                        progressPct = progress,
                    )
                }

                // Update notification
                val remainingMin = ((1f - progress) * totalDuration).toInt()
                val placeName = detail?.place?.name ?: s.destination
                LocationForegroundService.updateNaviStatus(
                    "$placeName · ${remainingMin}min"
                )

                // Transit 탑승/하차 감지
                if (mode == "transit") {
                    checkTransitBoardingAlighting(userLat, userLng)
                }

                // Nearby spot check (progress-based)
                checkNearbyByProgress(progress)

                // Arrival proximity — show confirm button (once)
                val distToDest = haversineMeters(userLat, userLng, destinationLat, destinationLng)
                if (distToDest < ARRIVAL_THRESHOLD_METERS && !arrivalPromptShown) {
                    arrivalPromptShown = true
                    viewModelScope.launch {
                        addMessage(NaviChatMessage.BotTyping)
                        delay(800L)
                        removeTyping()
                        addMessage(NaviChatMessage.BotText(
                            s.naviArrivalPrompt.replace("%s", detail?.place?.name ?: s.destination)
                        ))
                        addMessage(NaviChatMessage.ArrivalConfirm)
                        notifyUser()
                    }
                }
            }
        }
    }


    // ── Action Button Handlers ──

    fun onPhrasesClick() {
        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(NaviChatMessage.BotText(s.usefulPhrases))
            addMessage(NaviChatMessage.Phrases(items = USEFUL_PHRASES))
        }
    }


    // ── Nearby Spot: Route-based pre-pick ──

    private fun pickNearbySpotsAlongRoute(routeCoords: List<RouteCoord>) {
        val allSpots = guideData?.nearbyPlaces
            ?.filter { it.placeId.toString() != placeId && it.latitude != null && it.longitude != null }
            ?: return

        if (allSpots.isEmpty() || routeCoords.size < 2) return

        // 각 장소에 대해 경로까지의 최소 거리 + 경로상 위치(progress) 계산
        data class ScoredSpot(
            val dto: com.ohmyguide.app.data.model.GuidePlaceDto,
            val distToRoute: Double,
            val progress: Float,
        )

        val scored = allSpots.mapNotNull { dto ->
            val sLat = dto.latitude ?: return@mapNotNull null
            val sLng = dto.longitude ?: return@mapNotNull null

            // 경로의 각 점과의 거리 중 최소값 + 해당 점의 인덱스
            var minDist = Double.MAX_VALUE
            var closestIdx = 0
            routeCoords.forEachIndexed { i, coord ->
                val d = haversineMeters(sLat, sLng, coord.lat, coord.lng)
                if (d < minDist) {
                    minDist = d
                    closestIdx = i
                }
            }
            if (minDist > ROUTE_NEARBY_RADIUS) return@mapNotNull null

            val progress = (closestIdx + 1).toFloat() / routeCoords.size
            ScoredSpot(dto, minDist, progress)
        }

        // 점수: overview 있으면 +100, imageUrl 있으면 +50, 경로 가까울수록 높은 점수
        val picked = scored
            .sortedWith(compareByDescending<ScoredSpot> { spot ->
                var score = 0
                if (!spot.dto.overview.isNullOrBlank() && spot.dto.overview != "-") score += 100
                if (!spot.dto.firstImage1.isNullOrBlank()) score += 50
                score
            }.thenBy { it.distToRoute })
            .distinctBy { it.dto.placeId }
            .take(MAX_NEARBY_RECOMMENDATIONS)
            .sortedBy { it.progress }  // 경로 순서대로 정렬

        pickedSpots = picked.map { s ->
            NearbySpotInfo(
                placeId = s.dto.placeId.toString(),
                name = s.dto.title ?: "",
                imageUrl = s.dto.firstImage1,
                overview = s.dto.overview,
                lat = s.dto.latitude ?: 0.0,
                lng = s.dto.longitude ?: 0.0,
                routeProgress = s.progress,
            )
        }

        // 각 장소의 guide data를 캐시
        pickedSpots.forEach { spot ->
            PlaceDetailCache.putGuide(spot.placeId, com.ohmyguide.app.data.model.GuideNavigationResponse(
                startLocation = com.ohmyguide.app.data.model.StartLocationDto(0.0, 0.0),
                destination = com.ohmyguide.app.data.model.GuidePlaceDto(
                    placeId = spot.placeId.toLongOrNull() ?: 0L,
                    title = spot.name,
                    addr1 = null,
                    latitude = spot.lat,
                    longitude = spot.lng,
                    firstImage1 = spot.imageUrl,
                    overview = spot.overview,
                    overviewTts = null,
                ),
                nearbyPlaces = emptyList(),
            ))
        }

        // UI에 마커용 데이터 전달
        _uiState.update { it.copy(pickedNearbySpots = pickedSpots) }

        if (BuildConfig.DEBUG) {
            Log.d("NaviVM", "Picked ${pickedSpots.size} nearby spots along route")
            pickedSpots.forEach { Log.d("NaviVM", "  - ${it.name} at progress=${it.routeProgress}") }
        }
    }

    // ── Transit 탑승/하차 GPS 감지 ──

    private fun checkTransitBoardingAlighting(userLat: Double, userLng: Double) {
        val segments = _naviRoute.value?.segments ?: return
        segments.forEachIndexed { idx, seg ->
            if (seg.type != "bus" && seg.type != "subway") return@forEachIndexed
            val boardCoord = seg.coords.firstOrNull() ?: return@forEachIndexed

            val distToBoard = haversineMeters(userLat, userLng, boardCoord.lat, boardCoord.lng)

            // 탑승 정류장 100m 이내 진입 → 탑승 카드
            if (distToBoard < TRANSIT_BOARD_RADIUS && idx !in boardedSegments) {
                boardedSegments.add(idx)
                viewModelScope.launch {
                    addMessage(NaviChatMessage.TransitGuide(
                        TransitGuideInfo(
                            type = "board",
                            transitType = seg.type,
                            lineName = seg.lineName,
                            stationName = seg.fromNameKr.ifEmpty { seg.fromName },
                            stationNameEn = seg.fromName,
                            stopsCount = seg.stopsCount,
                            exitStation = seg.toNameKr.ifEmpty { seg.toName },
                            exitStationEn = seg.toName,
                        )
                    ))
                    notifyUser()
                }
            }

            // 탑승 완료 후 100m 밖으로 벗어남 → 하차 카드
            if (idx in boardedSegments && idx !in alightedSegments && distToBoard >= TRANSIT_BOARD_RADIUS) {
                alightedSegments.add(idx)
                viewModelScope.launch {
                    addMessage(NaviChatMessage.TransitGuide(
                        TransitGuideInfo(
                            type = "alight",
                            transitType = seg.type,
                            lineName = seg.lineName,
                            stationName = seg.toNameKr.ifEmpty { seg.toName },
                            stationNameEn = seg.toName,
                            stopsCount = seg.stopsCount,
                        )
                    ))
                    notifyUser()
                }
            }
        }
    }

    private fun checkNearbyByProgress(progress: Float) {
        if (!nearbyRecommendEnabled) return
        if (nextNearbyIndex >= pickedSpots.size) return

        val nextSpot = pickedSpots[nextNearbyIndex]
        if (progress >= nextSpot.routeProgress) {
            nextNearbyIndex++
            viewModelScope.launch {
                addMessage(NaviChatMessage.BotTyping)
                delay(800L)
                removeTyping()
                addMessage(NaviChatMessage.BotText(
                    s.naviNearbySpot.replace("%s", nextSpot.name)
                ))
                addMessage(NaviChatMessage.NearbySpotCard(spot = nextSpot))
                notifyUser()
            }
        }
    }

    private fun GuidePlaceDto.toPlace(): Place = Place(
        id = placeId.toString(),
        name = title ?: "",
        nameKr = "",
        rating = 0f,
        distance = "",
        tag = "",
        color = CatAttraction,
        emoji = "\uD83D\uDCCD",
        lat = latitude ?: 0.0,
        lng = longitude ?: 0.0,
        imageUrl = firstImage1,
    )

    // ── Weather ──

    private suspend fun fetchWeatherAndShow() {
        val result = weatherRepository.getHourlyForecast(destinationLat, destinationLng)
        if (result is ApiResult.Success) {
            val hourly = result.data.hourly ?: return
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val idx = currentHour.coerceIn(0, (hourly.temperature?.size ?: 1) - 1)

            val temp = hourly.temperature?.getOrNull(idx) ?: return
            val feelsLike = hourly.apparentTemperature?.getOrNull(idx) ?: temp
            val code = hourly.weatherCode?.getOrNull(idx) ?: 0
            val precip = hourly.precipitationProbability?.getOrNull(idx) ?: 0
            val wind = hourly.windSpeed?.getOrNull(idx) ?: 0.0
            val isDay = (hourly.isDay?.getOrNull(idx) ?: 1) == 1

            val (desc, emoji) = weatherCodeToDescEmoji(code, isDay)
            val tip = buildWeatherTip(temp, feelsLike, code, precip, wind, isDay)

            // Build next 4 hours forecast
            val upcoming = (1..4).mapNotNull { offset ->
                val futureIdx = idx + offset
                if (futureIdx >= (hourly.temperature?.size ?: 0)) return@mapNotNull null
                val futureCode = hourly.weatherCode?.getOrNull(futureIdx) ?: 0
                val futureIsDay = (hourly.isDay?.getOrNull(futureIdx) ?: 1) == 1
                val (_, futureEmoji) = weatherCodeToDescEmoji(futureCode, futureIsDay)
                HourForecast(
                    hour = (currentHour + offset) % 24,
                    temp = hourly.temperature?.getOrNull(futureIdx) ?: 0.0,
                    emoji = futureEmoji,
                    precipProb = hourly.precipitationProbability?.getOrNull(futureIdx) ?: 0,
                )
            }

            addMessage(NaviChatMessage.BotText(s.weatherIntro))
            addMessage(NaviChatMessage.Weather(
                WeatherInfo(
                    temperature = temp,
                    feelsLike = feelsLike,
                    weatherDesc = desc,
                    emoji = emoji,
                    precipProbability = precip,
                    windSpeed = wind,
                    isDay = isDay,
                    tip = tip,
                    hourlyForecast = upcoming,
                )
            ))
        }
    }

    private fun weatherCodeToDescEmoji(code: Int, isDay: Boolean): Pair<String, String> = when (code) {
        0 -> s.weatherClear to if (isDay) "☀️" else "🌙"
        1 -> s.weatherMainlyClear to if (isDay) "🌤️" else "🌙"
        2 -> s.weatherPartlyCloudy to if (isDay) "⛅" else "☁️"
        3 -> s.weatherOvercast to "☁️"
        45, 48 -> s.weatherFoggy to "🌫️"
        51, 53, 55 -> s.weatherDrizzle to "🌦️"
        61, 63, 65 -> s.weatherRain to "🌧️"
        66, 67 -> s.weatherFreezingRain to "🌧️"
        71, 73, 75 -> s.weatherSnow to "🌨️"
        77 -> s.weatherSnowGrains to "🌨️"
        80, 81, 82 -> s.weatherRainShowers to "🌧️"
        85, 86 -> s.weatherSnowShowers to "🌨️"
        95 -> s.weatherThunderstorm to "⛈️"
        96, 99 -> s.weatherThunderstormHail to "⛈️"
        else -> s.weatherUnknown to "🌡️"
    }

    private fun buildWeatherTip(
        temp: Double, feelsLike: Double, code: Int, precip: Int, wind: Double, isDay: Boolean,
    ): String {
        val tips = mutableListOf<String>()
        val windStr = "%.1f".format(wind)

        // Temperature advice
        when {
            temp >= 33 -> tips.add(s.weatherTipHot)
            temp >= 28 -> tips.add(s.weatherTipWarm)
            feelsLike < temp - 3 -> tips.add(s.weatherTipFeelsCold.replace("%s", "${feelsLike.toInt()}°C"))
            temp in 10.0..20.0 -> tips.add(s.weatherTipMild)
            temp < 5 -> tips.add(s.weatherTipCold)
        }

        // Wind advice
        when {
            wind >= 14 -> tips.add(s.weatherTipStrongWind.replace("%s", windStr))
            wind >= 8 -> tips.add(s.weatherTipWindy.replace("%s", windStr))
        }

        // Precipitation & weather code
        when {
            code in 61..67 || code in 80..82 -> tips.add(s.weatherTipRaining)
            code in 71..77 || code in 85..86 -> tips.add(s.weatherTipSnowing)
            code == 95 || code == 96 || code == 99 -> tips.add(s.weatherTipThunderstorm)
            precip >= 50 -> tips.add(s.weatherTipRainChance.replace("%s", "$precip"))
        }

        // Day/night advice
        if (!isDay) {
            tips.add(s.weatherTipDark)
        }

        return tips.joinToString(" ")
            .ifEmpty { if (isDay) s.weatherTipGreat else s.weatherTipClearNight }
    }

    // ── Arrival Confirmation ──

    fun onArrivalConfirm() {
        LocationForegroundService.updateNaviStatus(null)
        _uiState.update { it.copy(arrived = true, progressPct = 1f) }
        viewModelScope.launch {
            removeArrivalConfirm()
        }
    }

    // ── Helpers ──

    private fun addMessage(msg: NaviChatMessage) {
        _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    private fun removeTyping() {
        _uiState.update { state ->
            state.copy(chatMessages = state.chatMessages.filterNot { it is NaviChatMessage.BotTyping })
        }
    }

    private fun removeArrivalConfirm() {
        _uiState.update { state ->
            state.copy(chatMessages = state.chatMessages.filterNot { it is NaviChatMessage.ArrivalConfirm })
        }
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

    override fun onCleared() {
        super.onCleared()
        gpsJob?.cancel()
        LocationForegroundService.updateNaviStatus(null)
    }
}
