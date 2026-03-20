package com.ohmyguide.app.ui.screen.navi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.SAMPLE_PLACES
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
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

sealed class NaviChatMessage {
    data class BotText(val text: String) : NaviChatMessage()
    object BotTyping : NaviChatMessage()
    data class PlaceIntro(val detail: PlaceDetail, val distance: String, val eta: String) : NaviChatMessage()
    data class ActionButtons(
        val options: List<String> = listOf("Listen", "Photo", "Prices", "Phrases", "Skip"),
        val answered: Boolean = false,
        val selected: String? = null,
    ) : NaviChatMessage()
    data class NearbyPoi(
        val name: String,
        val answered: Boolean = false,
    ) : NaviChatMessage()
    object ArrivalConfirm : NaviChatMessage()
    data class NearbyRecommendations(val places: List<Place>) : NaviChatMessage()
}

// ── UI State ──

data class NaviUiState(
    val chatMessages: List<NaviChatMessage> = emptyList(),
    val arrived: Boolean = false,
    val progressPct: Float = 0f,
    val userLat: Double = 37.5665,
    val userLng: Double = 126.9780,
)

@HiltViewModel
class NaviViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val placeId: String = savedStateHandle["placeId"] ?: "dm3"
    val mode: String = savedStateHandle["mode"] ?: "walk"

    val detail: PlaceDetail? = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()

    private val route = FALLBACK_ROUTES[placeId to mode]
    private val totalDistance = route?.distanceMeters ?: 1500
    private val totalDuration = route?.durationMin ?: 5

    private val destinationLat = PLACE_COORDINATES[placeId]?.first ?: 37.5700
    private val destinationLng = PLACE_COORDINATES[placeId]?.second ?: 126.9990

    private val _uiState = MutableStateFlow(NaviUiState())
    val uiState: StateFlow<NaviUiState> = _uiState.asStateFlow()

    private var gpsJob: Job? = null
    private var nearbyPoiShown = false

    companion object {
        private val PLACE_COORDINATES = mapOf(
            "dm3" to (37.5700 to 126.9990),
            "dm4" to (37.5826 to 126.9831),
            "dm5" to (37.5512 to 126.9882),
            "dm6" to (37.5735 to 126.9920),
            "dm7" to (37.5690 to 126.9780),
        )
        private const val ARRIVAL_THRESHOLD_METERS = 100.0
    }

    init {
        initChat()
        startGpsSimulation()
    }

    private fun initChat() {
        val msgs = mutableListOf<NaviChatMessage>()
        val dist = "${totalDistance}m"
        val eta = "$totalDuration min"

        msgs += NaviChatMessage.PlaceIntro(
            detail = detail ?: return,
            distance = dist,
            eta = eta,
        )
        msgs += NaviChatMessage.BotText(
            "I'll guide you to ${detail.place.name}! Keep going straight ahead.",
        )
        msgs += NaviChatMessage.ActionButtons()

        _uiState.update { it.copy(chatMessages = msgs) }
    }

    // ── GPS Simulation (5초 간격) ──

    private fun startGpsSimulation() {
        val routePoints = route?.points ?: return
        if (routePoints.size < 2) return

        gpsJob = viewModelScope.launch {
            val steps = routePoints.size
            for (i in routePoints.indices) {
                delay(5000L)
                if (_uiState.value.arrived) break

                val point = routePoints[i]
                val progress = (i + 1).toFloat() / steps
                _uiState.update {
                    it.copy(
                        userLat = point.lat,
                        userLng = point.lng,
                        progressPct = progress,
                    )
                }

                // Nearby POI check (halfway point)
                if (!nearbyPoiShown && progress > 0.5f) {
                    showNearbyPoi()
                }

                // Arrival check
                val dist = haversineMeters(point.lat, point.lng, destinationLat, destinationLng)
                if (dist < ARRIVAL_THRESHOLD_METERS) {
                    onArrival()
                    break
                }
            }

            // Force arrival if route completed
            if (!_uiState.value.arrived) {
                onArrival()
            }
        }
    }

    private fun onArrival() {
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

    fun onActionSelect(action: String) {
        markActionAnswered(action)

        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(1000L)
            removeTyping()

            when (action) {
                "Listen" -> addMessage(
                    NaviChatMessage.BotText(
                        "🎧 Audio Guide: ${detail?.place?.name ?: "This place"} has been a beloved landmark for decades. " +
                            "The area is known for its rich history and vibrant atmosphere. " +
                            "Take a moment to soak in the surroundings!"
                    )
                )
                "Photo" -> addMessage(
                    NaviChatMessage.BotText(
                        "📸 Best photo spots nearby:\n" +
                            "1. The main entrance — great for wide shots\n" +
                            "2. The alley to the left — perfect for street vibes\n" +
                            "3. Rooftop across the street — panoramic view"
                    )
                )
                "Prices" -> addMessage(
                    NaviChatMessage.BotText(
                        "💰 Price Guide:\n" +
                            "• Entrance: ${detail?.fee ?: "Free"}\n" +
                            "• Avg meal nearby: ₩8,000-15,000\n" +
                            "• Popular souvenir: ₩5,000-10,000"
                    )
                )
                "Phrases" -> addMessage(
                    NaviChatMessage.BotText(
                        "🗣 Useful Korean phrases here:\n" +
                            "• 이거 주세요 (i-geo ju-se-yo) — This one, please\n" +
                            "• 얼마예요? (eol-ma-ye-yo) — How much?\n" +
                            "• 화장실 어디예요? (hwa-jang-sil eo-di-ye-yo) — Where's the restroom?"
                    )
                )
                "Skip" -> addMessage(
                    NaviChatMessage.BotText("No worries! Let me know if you need anything on the way.")
                )
            }
        }
    }

    // ── Nearby POI ──

    private fun showNearbyPoi() {
        nearbyPoiShown = true
        val nearbyName = SAMPLE_PLACES
            .filter { it.id != placeId }
            .randomOrNull()?.name ?: return

        viewModelScope.launch {
            addMessage(NaviChatMessage.BotTyping)
            delay(800L)
            removeTyping()
            addMessage(NaviChatMessage.BotText("There's $nearbyName nearby! Want to check it out?"))
            addMessage(NaviChatMessage.NearbyPoi(name = nearbyName))
        }
    }

    fun onNearbyPoiResponse(accepted: Boolean, name: String) {
        markNearbyAnswered(name)
        viewModelScope.launch {
            if (accepted) {
                addMessage(NaviChatMessage.BotTyping)
                delay(800L)
                removeTyping()
                val poiDetail = SAMPLE_PLACE_DETAILS.values.firstOrNull { it.place.name == name }
                addMessage(
                    NaviChatMessage.BotText(
                        "Great choice! ${poiDetail?.desc ?: "$name is a wonderful spot to explore."}"
                    )
                )
            } else {
                addMessage(NaviChatMessage.BotText("No problem, let's keep going!"))
            }
        }
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

    private fun markActionAnswered(selected: String) {
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.map {
                    if (it is NaviChatMessage.ActionButtons && !it.answered) {
                        it.copy(answered = true, selected = selected)
                    } else it
                },
            )
        }
    }

    private fun markNearbyAnswered(name: String) {
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.map {
                    if (it is NaviChatMessage.NearbyPoi && it.name == name && !it.answered) {
                        it.copy(answered = true)
                    } else it
                },
            )
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
    }
}