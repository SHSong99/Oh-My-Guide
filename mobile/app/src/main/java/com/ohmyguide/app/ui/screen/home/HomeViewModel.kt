package com.ohmyguide.app.ui.screen.home

import android.util.Log
import com.ohmyguide.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.PlaceCardDto
import com.ohmyguide.app.data.api.GuideSseClient
import com.ohmyguide.app.data.api.NaverGeocodingApi
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import com.ohmyguide.app.data.repository.RecommendRepository
import com.ohmyguide.app.fixtures.HOME_RECOMMENDATIONS
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.service.LocationData
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.theme.CatAttraction
import com.ohmyguide.app.ui.theme.CatCulture
import com.ohmyguide.app.ui.theme.CatFood
import com.ohmyguide.app.ui.theme.CatLeports
import com.ohmyguide.app.ui.theme.CatShopping
import com.ohmyguide.app.ui.theme.CatFestival
import com.ohmyguide.app.ui.theme.CatCafe
import com.ohmyguide.app.ui.theme.LanguageManager
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

// ── Chat Message Types ──

sealed class ChatMessage {
    data class BotText(val text: String) : ChatMessage()
    object BotTyping : ChatMessage()
    data class UserText(val text: String) : ChatMessage()
    data class BotRecommendation(val section: RecommendationSection) : ChatMessage()
    data class BotOptions(
        val options: List<String>,
        val answered: Boolean = false,
        val selectedOption: String? = null,
    ) : ChatMessage()
    object FindOtherPlacesBtn : ChatMessage()
    data class UserInput(
        val onSubmit: (String) -> Unit,
    ) : ChatMessage()
}

// ── Sheet Mode ──

enum class SheetMode {
    RECOMMENDATIONS,
    PLACE_DETAIL,
}

// ── Chat Flow Step ──

private enum class FlowStep {
    IDLE,
    AWAITING_FOCUS,
    AWAITING_VIBE,
}

// ── UI State (분리: 채팅은 자주 변경, 시트는 사용자 인터랙션 시만 변경) ──

data class HomeChatState(
    val chatMessages: List<ChatMessage> = emptyList(),
    val spotCount: Int = 6,
    val isLoading: Boolean = true,
)

data class HomeSheetState(
    val sheetMode: SheetMode = SheetMode.RECOMMENDATIONS,
    val selectedDetail: PlaceDetail? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recommendRepository: RecommendRepository,
    private val guideSseClient: GuideSseClient,
    private val naverGeocodingApi: NaverGeocodingApi,
) : ViewModel() {

    private val s get() = LanguageManager.current.value.strings

    private val _chatState = MutableStateFlow(HomeChatState())
    val chatState: StateFlow<HomeChatState> = _chatState.asStateFlow()

    private val _sheetState = MutableStateFlow(HomeSheetState())
    val sheetState: StateFlow<HomeSheetState> = _sheetState.asStateFlow()

    private val _locationName = MutableStateFlow("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private var flowStep = FlowStep.IDLE
    private var selectedFocus: String? = null
    private var initialLoaded = false
    private var lastGeocodedLat = 0.0
    private var lastGeocodedLng = 0.0

    private suspend fun getLatestLocation(): LocationData {
        // 이미 값이 있으면 즉시 반환
        LocationForegroundService.locationFlow.value?.let { return it }
        // 없으면 최대 5초 대기 후 fallback
        return withTimeoutOrNull(5000L) {
            LocationForegroundService.locationFlow.filterNotNull().first()
        } ?: LocationData(DEFAULT_LAT, DEFAULT_LNG)
    }

    fun fetchLocationName(lat: Double, lng: Double) {
        // 같은 좌표(소수점 3자리)에 대해 중복 호출 방지
        val roundedLat = "%.3f".format(lat).toDouble()
        val roundedLng = "%.3f".format(lng).toDouble()
        if (roundedLat == lastGeocodedLat && roundedLng == lastGeocodedLng) return
        lastGeocodedLat = roundedLat
        lastGeocodedLng = roundedLng

        viewModelScope.launch {
            try {
                val coords = "$lng,$lat"
                val response = naverGeocodingApi.reverseGeocode(
                    clientId = BuildConfig.NAVER_MAP_CLIENT_ID,
                    clientSecret = BuildConfig.NAVER_MAP_CLIENT_SECRET,
                    coords = coords,
                )
                val region = response.results?.firstOrNull()?.region
                val city = region?.area1?.name ?: ""
                val district = region?.area2?.name ?: ""
                _locationName.value = when {
                    district.isNotEmpty() && city.isNotEmpty() -> "$district, $city"
                    city.isNotEmpty() -> city
                    else -> s.yourArea
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("HomeVM", "Naver geocoding failed", e)
                if (_locationName.value.isEmpty()) {
                    _locationName.value = s.yourArea
                }
            }
        }
    }

    fun loadInitialRecommendation(category: String) {
        if (initialLoaded) return
        initialLoaded = true

        viewModelScope.launch {
            addMessage(ChatMessage.BotTyping)

            val location = getLatestLocation()
            val lat = location.latitude
            val lng = location.longitude

            val result = recommendRepository.getRecommendation(category, lat, lng)
            removeTyping()

            val places = result.getOrNull()?.map { it.toPlace() }

            if (places.isNullOrEmpty()) {
                addMessage(ChatMessage.BotText(s.noPlacesFound))
            } else {
                val section = RecommendationSection(
                    title = s.picksForYou,
                    icon = HOME_RECOMMENDATIONS[0].icon,
                    label = "AI",
                    places = places,
                    btnText = "",
                )
                addMessage(ChatMessage.BotRecommendation(section))
                _chatState.update { it.copy(spotCount = places.size) }
            }
            _chatState.update { it.copy(isLoading = false) }
            addMessage(ChatMessage.FindOtherPlacesBtn)
        }
    }

    // ── Place Detail ──

    fun selectPlace(placeId: String) {
        val attrId = placeId.toLongOrNull()

        // 추천 결과에서 Place 찾기 (카드에 표시된 기본 정보)
        val place = _chatState.value.chatMessages
            .filterIsInstance<ChatMessage.BotRecommendation>()
            .flatMap { it.section.places }
            .find { it.id == placeId }

        if (attrId == null || place == null) return

        viewModelScope.launch {
            val result = recommendRepository.getAttractionDetail(attrId)
            val dto = result.getOrNull()
            val detail = PlaceDetail(
                place = place,
                desc = dto?.overview ?: "",
                hours = "",
                fee = "",
                walkTime = place.distance,
            )
            PlaceDetailCache.put(placeId, detail)
            _sheetState.update {
                it.copy(sheetMode = SheetMode.PLACE_DETAIL, selectedDetail = detail)
            }
        }
    }

    fun clearSelection() {
        _sheetState.update {
            it.copy(
                sheetMode = SheetMode.RECOMMENDATIONS,
                selectedDetail = null,
            )
        }
    }

    fun startGuide(placeId: String) {
        val attrId = placeId.toLongOrNull() ?: return
        val place = _chatState.value.chatMessages
            .filterIsInstance<ChatMessage.BotRecommendation>()
            .flatMap { it.section.places }
            .find { it.id == placeId }

        viewModelScope.launch {
            val location = LocationForegroundService.locationFlow.value
            val lat = location?.latitude ?: 35.0780
            val lng = location?.longitude ?: 128.8510
            val reachLat = place?.lat ?: lat
            val reachLng = place?.lng ?: lng

            // 이전 SSE 연결이 남아있으면 정리
            guideSseClient.close()
            // 1) SSE 연결 (fire-and-forget) → 2) REST 호출 (Kafka 트리거) → SSE 1건 수신 후 종료
            guideSseClient.connect(
                onResponse = { guide ->
                    if (BuildConfig.DEBUG) Log.d("HomeViewModel", "SSE onResponse: $guide")
                    PlaceDetailCache.putGuide(placeId, guide)
                    guideSseClient.close()
                },
                onError = {
                    if (BuildConfig.DEBUG) Log.d("HomeViewModel", "SSE error", it)
                    guideSseClient.close()
                },
            )
            // SSE 연결 요청 직후 REST 호출 — 네트워크 왕복 시간 동안 SSE가 열림
            if (BuildConfig.DEBUG) Log.d("HomeViewModel", "Calling REST guide/$attrId")
            recommendRepository.startGuideNavigation(attrId, lat, lng, reachLat, reachLng)
            if (BuildConfig.DEBUG) Log.d("HomeViewModel", "REST guide/$attrId done")
        }
    }

    // ── Show More ──

    fun onShowMore(sectionTitle: String) {
        viewModelScope.launch {
            addMessage(ChatMessage.UserText("${s.showMore} $sectionTitle"))
            addMessage(ChatMessage.BotTyping)

            val location = getLatestLocation()
            val lat = location.latitude
            val lng = location.longitude
            val result = recommendRepository.getRecommendation(sectionTitle, lat, lng)
            removeTyping()

            val extraPlaces = result.getOrNull()?.map { it.toPlace() }
            if (!extraPlaces.isNullOrEmpty()) {
                val extraSection = RecommendationSection(
                    title = sectionTitle,
                    icon = HOME_RECOMMENDATIONS[0].icon,
                    label = "More",
                    places = extraPlaces,
                    btnText = "",
                )
                addMessage(ChatMessage.BotText(s.moreSpots))
                addMessage(ChatMessage.BotRecommendation(extraSection))
            } else {
                addMessage(ChatMessage.BotText(s.noPlacesFound))
            }
        }
    }

    // ── Find Other Places Flow ──

    fun onFindOtherPlaces() {
        viewModelScope.launch {
            removeFindBtn()
            addMessage(ChatMessage.UserText(s.findOtherPlaces))
            addMessage(ChatMessage.BotTyping)
            delay(1200L)
            removeTyping()
            addMessage(ChatMessage.BotText(s.mainFocusQuestion))
            addMessage(
                ChatMessage.BotOptions(
                    options = listOf(s.optionFood, s.optionPhoto, s.optionShopping),
                )
            )
            flowStep = FlowStep.AWAITING_FOCUS
        }
    }

    fun onSelectOption(option: String) {
        if (option == "__OTHER__") {
            markOptionAnswered(option)
            addMessage(ChatMessage.UserInput(onSubmit = { customText ->
                removeUserInput()
                onSelectOption(customText)
            }))
            return
        }
        when (flowStep) {
            FlowStep.AWAITING_FOCUS -> onFocusSelected(option)
            FlowStep.AWAITING_VIBE -> onVibeSelected(option)
            FlowStep.IDLE -> {}
        }
    }

    private fun onFocusSelected(option: String) {
        selectedFocus = option
        viewModelScope.launch {
            markOptionAnswered(option)
            addMessage(ChatMessage.UserText(option))
            addMessage(ChatMessage.BotTyping)
            delay(1000L)
            removeTyping()
            addMessage(ChatMessage.BotText(s.vibeQuestion))
            addMessage(
                ChatMessage.BotOptions(
                    options = listOf(s.optionActive, s.optionCalm, s.optionNightlife),
                )
            )
            flowStep = FlowStep.AWAITING_VIBE
        }
    }

    private fun onVibeSelected(option: String) {
        viewModelScope.launch {
            markOptionAnswered(option)
            addMessage(ChatMessage.UserText(option))
            addMessage(ChatMessage.BotTyping)

            val location = getLatestLocation()
            val lat = location.latitude
            val lng = location.longitude

            val request = RefreshRecommendRequest(
                latitude = lat,
                longitude = lng,
                category = selectedFocus,
                mood = option,
            )

            val result = recommendRepository.refreshRecommendation(request)
            removeTyping()

            if (BuildConfig.DEBUG) {
                result.exceptionOrNull()?.let {
                    Log.e("HomeVM", "refreshRecommendation failed", it)
                }
            }

            val newPlaces = result.getOrNull()?.map { it.toPlace() }

            if (newPlaces.isNullOrEmpty()) {
                addMessage(ChatMessage.BotText(s.sorryNoPlaces))
            } else {
                val newSection = RecommendationSection(
                    title = s.newPicksForYou,
                    icon = HOME_RECOMMENDATIONS[0].icon,
                    label = "$selectedFocus · $option",
                    places = newPlaces,
                    btnText = "",
                )
                addMessage(ChatMessage.BotText(s.freshPicks))
                addMessage(ChatMessage.BotRecommendation(newSection))
                _chatState.update { it.copy(spotCount = it.spotCount + newPlaces.size) }
            }

            addMessage(ChatMessage.FindOtherPlacesBtn)
            flowStep = FlowStep.IDLE
            selectedFocus = null
        }
    }

    companion object {
        private const val DEFAULT_LAT = 35.0780
        private const val DEFAULT_LNG = 128.8510

        private val TAG_COLOR_MAP = mapOf(
            "Nature" to CatAttraction,
            "Culture" to CatCulture,
            "Festival" to CatFestival,
            "Activity" to CatLeports,
            "Shopping" to CatShopping,
            "Food" to CatFood,
            "Lodging" to CatCafe,
        )

        private val TAG_EMOJI_MAP = mapOf(
            "Nature" to "\uD83C\uDFDE\uFE0F",
            "Culture" to "\uD83C\uDFDB\uFE0F",
            "Festival" to "\uD83C\uDF86",
            "Activity" to "\uD83C\uDFC4",
            "Shopping" to "\uD83D\uDECD\uFE0F",
            "Food" to "\uD83C\uDF5C",
            "Lodging" to "\uD83C\uDFE8",
        )

        fun PlaceCardDto.toPlace(): Place = Place(
            id = attrId.toString(),
            name = name,
            nameKr = nameKr,
            rating = 0f,
            distance = distance,
            tag = tag,
            color = TAG_COLOR_MAP[tag] ?: CatAttraction,
            emoji = TAG_EMOJI_MAP[tag] ?: "\uD83D\uDCCD",
            lat = latitude,
            lng = longitude,
            imageUrl = imageUrl,
        )
    }

    // ── Message helpers ──

    private fun addMessage(msg: ChatMessage) {
        _chatState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    private fun removeTyping() {
        _chatState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.filterNot { it is ChatMessage.BotTyping },
            )
        }
    }

    private fun removeFindBtn() {
        _chatState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.filterNot { it is ChatMessage.FindOtherPlacesBtn },
            )
        }
    }

    private fun markOptionAnswered(selected: String) {
        _chatState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.map { msg ->
                    if (msg is ChatMessage.BotOptions && !msg.answered) {
                        msg.copy(answered = true, selectedOption = selected)
                    } else msg
                },
            )
        }
    }

    private fun removeUserInput() {
        _chatState.update { state ->
            state.copy(chatMessages = state.chatMessages.filterNot { it is ChatMessage.UserInput })
        }
    }
}
