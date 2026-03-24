package com.ohmyguide.app.ui.screen.navi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.repository.NaverDirectionsRepository
import com.ohmyguide.app.data.repository.TmapRepository
import com.ohmyguide.app.data.repository.WeatherRepository
import com.ohmyguide.app.domain.model.NaviRouteCache
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.domain.model.RouteCoord
import com.ohmyguide.app.domain.model.RouteSegmentGeo
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.SAMPLE_PLACES
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitGray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ── Chat Messages ──

data class PhraseItem(
    val korean: String,
    val romanization: String,
    val english: String,
)

data class TransitStopInfo(
    val stopName: String,
    val busNumber: String,
    val remainingStops: Int,
    val exitStopName: String,
)

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

data class HourForecast(
    val hour: Int,
    val temp: Double,
    val emoji: String,
    val precipProb: Int,
)

sealed class NaviChatMessage {
    data class BotText(val text: String) : NaviChatMessage()
    object BotTyping : NaviChatMessage()
    data class PlaceIntro(val detail: PlaceDetail, val distance: String, val eta: String) : NaviChatMessage()
    data class TransitInfo(val info: TransitStopInfo) : NaviChatMessage()
    data class DestinationDetail(val detail: PlaceDetail) : NaviChatMessage()
    data class NearbyPlaces(
        val places: List<Place>,
    ) : NaviChatMessage()
    data class Phrases(val items: List<PhraseItem>) : NaviChatMessage()
    object ArrivalConfirm : NaviChatMessage()
    data class NearbyRecommendations(val places: List<Place>) : NaviChatMessage()
    data class Weather(val info: WeatherInfo) : NaviChatMessage()
    data class StoryPrompt(val placeName: String) : NaviChatMessage()
}

// ── UI State ──

data class NaviUiState(
    val chatMessages: List<NaviChatMessage> = emptyList(),
    val arrived: Boolean = false,
    val progressPct: Float = 0f,
    val userLat: Double = 35.0950,
    val userLng: Double = 128.8560,
    // Course mode
    val course: Course? = null,
    val spotIndex: Int = 0,
    val totalSpots: Int = 0,
)

@HiltViewModel
class NaviViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val naviRouteCache: NaviRouteCache,
    private val directionsRepository: NaverDirectionsRepository,
    private val tmapRepository: TmapRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    val placeId: String = savedStateHandle["placeId"] ?: "dm3"
    val mode: String = savedStateHandle["mode"] ?: "walk"
    val courseId: String? = savedStateHandle.get<String>("courseId")?.ifEmpty { null }
    val spotIndex: Int = savedStateHandle.get<String>("spotIndex")?.toIntOrNull() ?: 0

    val course: Course? = courseId?.let { id -> EXPLORE_COURSES.find { it.id == id } }
    val isCourseMode: Boolean = course != null

    val detail: PlaceDetail? = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()

    private val _naviRoute = MutableStateFlow<NaviRouteData?>(
        if (mode == "transit") naviRouteCache.peek() else null
    )
    val naviRoute: StateFlow<NaviRouteData?> = _naviRoute.asStateFlow()

    private val route = FALLBACK_ROUTES[placeId to mode]
    private val totalDistance = route?.distanceMeters ?: 1500
    private val totalDuration = _naviRoute.value?.totalDurationMin ?: route?.durationMin ?: 5

    private val destinationLat = PLACE_COORDINATES[placeId]?.first ?: 37.5700
    private val destinationLng = PLACE_COORDINATES[placeId]?.second ?: 126.9990

    private val _uiState = MutableStateFlow(NaviUiState())
    val uiState: StateFlow<NaviUiState> = _uiState.asStateFlow()

    private var gpsJob: Job? = null
    private var nearbyPoiShown = false

    companion object {
        private const val DEFAULT_LAT = 35.0950
        private const val DEFAULT_LNG = 128.8560
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
        private const val ARRIVAL_THRESHOLD_METERS = 100.0

        private val USEFUL_PHRASES = listOf(
            PhraseItem("이거 주세요", "i-geo ju-se-yo", "This one, please"),
            PhraseItem("얼마예요?", "eol-ma-ye-yo?", "How much is it?"),
            PhraseItem("화장실 어디예요?", "hwa-jang-sil eo-di-ye-yo?", "Where's the restroom?"),
            PhraseItem("감사합니다", "gam-sa-ham-ni-da", "Thank you"),
            PhraseItem("맛있어요!", "ma-si-sseo-yo!", "It's delicious!"),
        )

        private val TRANSIT_STOPS = mapOf(
            "dm3" to TransitStopInfo(
                stopName = "Jongno 5-ga Stn.",
                busNumber = "Bus 201",
                remainingStops = 3,
                exitStopName = "Gwangjang Market",
            ),
            "dm4" to TransitStopInfo(
                stopName = "Anguk Stn.",
                busNumber = "Bus 172",
                remainingStops = 2,
                exitStopName = "Bukchon Hanok Village",
            ),
            "dm5" to TransitStopInfo(
                stopName = "Myeongdong Stn.",
                busNumber = "Bus 402",
                remainingStops = 4,
                exitStopName = "Namsan Tower Entrance",
            ),
            "dm6" to TransitStopInfo(
                stopName = "Jongno 3-ga Stn.",
                busNumber = "Bus 151",
                remainingStops = 1,
                exitStopName = "Ikseon-dong",
            ),
            "dm7" to TransitStopInfo(
                stopName = "Gwanghwamun Stn.",
                busNumber = "Bus 109",
                remainingStops = 2,
                exitStopName = "Cheonggyecheon Stream",
            ),
        )
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
        val placeName = detail?.place?.name ?: "your destination"

        if (isCourseMode) {
            val courseName = course?.title ?: ""
            val total = course?.spots?.size ?: 0
            addMessage(NaviChatMessage.BotText(
                "\uD83D\uDCCD $courseName — Spot ${spotIndex + 1}/$total"
            ))
        }

        // Greeting
        addMessage(NaviChatMessage.BotText(
            "I'll guide you to $placeName! Keep going straight ahead.",
        ))

        // Weather — delivered shortly after start
        viewModelScope.launch {
            delay(1500L)
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            fetchWeatherAndShow()
        }

        // Story guide prompt — separate turn
        viewModelScope.launch {
            delay(6000L)
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(NaviChatMessage.BotText(
                "By the way, I know some interesting stories about $placeName!"
            ))
            addMessage(NaviChatMessage.StoryPrompt(placeName = placeName))
        }

        // Transit info — separate turn (only for transit mode)
        if (mode == "transit") {
            viewModelScope.launch {
                delay(10000L)
                addMessage(NaviChatMessage.BotTyping)
                delay(800L)
                removeTyping()
                addMessage(NaviChatMessage.BotText("Here's your transit info:"))
                addMessage(NaviChatMessage.TransitInfo(
                    info = TRANSIT_STOPS[placeId] ?: TRANSIT_STOPS.values.first(),
                ))
            }
        }
    }

    // ── Directions API (walk / car) ──

    private fun fetchDirectionsRoute() {
        viewModelScope.launch {
            val location = LocationForegroundService.locationFlow.value
            val rawLat = location?.latitude
            val rawLng = location?.longitude
            val inKorea = rawLat != null && rawLng != null
                && rawLat in 33.0..39.0 && rawLng in 124.0..132.0
            val startLat = if (inKorea) rawLat!! else DEFAULT_LAT
            val startLng = if (inKorea) rawLng!! else DEFAULT_LNG

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
                val placeName = detail?.place?.name ?: "destination"
                LocationForegroundService.updateNaviStatus(
                    "$placeName · ${remainingMin}min"
                )

                // Nearby POI check (halfway)
                if (!nearbyPoiShown && progress > 0.5f) {
                    showNearbyPlaces()
                }

                // Arrival check
                val distToDest = haversineMeters(userLat, userLng, destinationLat, destinationLng)
                if (distToDest < ARRIVAL_THRESHOLD_METERS) {
                    onArrival()
                }
            }
        }
    }

    private fun onArrival() {
        LocationForegroundService.updateNaviStatus(null)
        _uiState.update { it.copy(arrived = true, progressPct = 1f) }
        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(1000L)
            removeTyping()
            addMessage(NaviChatMessage.BotText("You've arrived at ${detail?.place?.name ?: "your destination"}!"))
            addMessage(NaviChatMessage.ArrivalConfirm)
        }
    }

    // ── Action Button Handlers ──

    fun onPhrasesClick() {
        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(NaviChatMessage.BotText("Here are some useful Korean phrases:"))
            addMessage(NaviChatMessage.Phrases(items = USEFUL_PHRASES))
        }
    }


    // ── Nearby POI ──

    private fun showNearbyPlaces() {
        nearbyPoiShown = true
        val nearbyPlaces = SAMPLE_PLACES
            .filter { it.id != placeId }
            .shuffled()
            .take(4)

        if (nearbyPlaces.isEmpty()) return

        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(NaviChatMessage.BotText("Here are some interesting places along your route:"))
            addMessage(NaviChatMessage.NearbyPlaces(places = nearbyPlaces))
        }
    }

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

            addMessage(NaviChatMessage.BotText("Before we start, let me check the weather for you!"))
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
        0 -> "Clear sky" to if (isDay) "☀️" else "🌙"
        1 -> "Mainly clear" to if (isDay) "🌤️" else "🌙"
        2 -> "Partly cloudy" to if (isDay) "⛅" else "☁️"
        3 -> "Overcast" to "☁️"
        45, 48 -> "Foggy" to "🌫️"
        51, 53, 55 -> "Drizzle" to "🌦️"
        61, 63, 65 -> "Rain" to "🌧️"
        66, 67 -> "Freezing rain" to "🌧️"
        71, 73, 75 -> "Snow" to "🌨️"
        77 -> "Snow grains" to "🌨️"
        80, 81, 82 -> "Rain showers" to "🌧️"
        85, 86 -> "Snow showers" to "🌨️"
        95 -> "Thunderstorm" to "⛈️"
        96, 99 -> "Thunderstorm with hail" to "⛈️"
        else -> "Unknown" to "🌡️"
    }

    private fun buildWeatherTip(
        temp: Double, feelsLike: Double, code: Int, precip: Int, wind: Double, isDay: Boolean,
    ): String {
        val tips = mutableListOf<String>()

        // Temperature advice
        val feelsLikeDiff = temp - feelsLike
        when {
            temp >= 33 -> tips.add("It's very hot! Stay hydrated and find shade when possible.")
            temp >= 28 -> tips.add("It's warm outside. Light clothing recommended.")
            feelsLike < temp - 3 -> tips.add("Feels colder than it looks (${feelsLike.toInt()}°C). Layer up!")
            temp in 10.0..20.0 -> tips.add("Mild weather. A light jacket might be nice.")
            temp < 5 -> tips.add("It's cold! Bundle up warmly.")
        }

        // Wind advice
        when {
            wind >= 14 -> tips.add("🌪️ Very strong wind (${"%.1f".format(wind)}m/s). Be careful outdoors!")
            wind >= 8 -> tips.add("💨 Windy today (${"%.1f".format(wind)}m/s). Hold onto your hat!")
        }

        // Precipitation & weather code
        when {
            code in 61..67 || code in 80..82 -> tips.add("☂️ Bring an umbrella — it's raining!")
            code in 71..77 || code in 85..86 -> tips.add("🧤 Snow expected — dress warmly and watch your step.")
            code == 95 || code == 96 || code == 99 -> tips.add("⚡ Thunderstorm alert! Consider staying indoors.")
            precip >= 50 -> tips.add("☂️ ${precip}% chance of rain — umbrella recommended.")
        }

        // Day/night advice
        if (!isDay) {
            tips.add("🌙 It's getting dark. Stay on well-lit paths!")
        }

        return tips.joinToString(" ")
            .ifEmpty { if (isDay) "Great weather for exploring!" else "Clear night. Enjoy the night views!" }
    }

    // ── Arrival Confirmation ──

    fun onArrivalConfirm() {
        viewModelScope.launch {
            removeArrivalConfirm()
            addMessage(NaviChatMessage.BotTyping)
            delay(1200L)
            removeTyping()
            addMessage(NaviChatMessage.BotText("Great! Here are some spots nearby you might love:"))

            val nearbyPlaces = SAMPLE_PLACES
                .filter { it.id != placeId }
                .shuffled()
                .take(3)
            addMessage(NaviChatMessage.NearbyRecommendations(nearbyPlaces))
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
