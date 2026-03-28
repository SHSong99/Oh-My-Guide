package com.ohmyguide.app.ui.screen.home

import android.util.Log
import com.ohmyguide.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.PlaceCardDto
import com.ohmyguide.app.data.api.GuideSseClient
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

// ── UI State ──

data class HomeUiState(
    val chatMessages: List<ChatMessage> = emptyList(),
    val spotCount: Int = 6,
    val sheetMode: SheetMode = SheetMode.RECOMMENDATIONS,
    val selectedDetail: PlaceDetail? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recommendRepository: RecommendRepository,
    private val guideSseClient: GuideSseClient,
) : ViewModel() {

    private val s get() = LanguageManager.current.value.strings

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var flowStep = FlowStep.IDLE
    private var selectedFocus: String? = null
    private var initialLoaded = false

    private suspend fun getLatestLocation(): LocationData {
        // 이미 값이 있으면 즉시 반환
        LocationForegroundService.locationFlow.value?.let { return it }
        // 없으면 최대 5초 대기 후 fallback
        return withTimeoutOrNull(5000L) {
            LocationForegroundService.locationFlow.filterNotNull().first()
        } ?: LocationData(DEFAULT_LAT, DEFAULT_LNG)
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
                _uiState.update { it.copy(spotCount = places.size) }
            }
            addMessage(ChatMessage.FindOtherPlacesBtn)
        }
    }

    // ── Place Detail ──

    fun selectPlace(placeId: String) {
        val attrId = placeId.toLongOrNull()

        // 추천 결과에서 Place 찾기 (카드에 표시된 기본 정보)
        val place = _uiState.value.chatMessages
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
            _uiState.update {
                it.copy(sheetMode = SheetMode.PLACE_DETAIL, selectedDetail = detail)
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                sheetMode = SheetMode.RECOMMENDATIONS,
                selectedDetail = null,
            )
        }
    }

    fun startGuide(placeId: String) {
        val attrId = placeId.toLongOrNull() ?: return
        val place = _uiState.value.chatMessages
            .filterIsInstance<ChatMessage.BotRecommendation>()
            .flatMap { it.section.places }
            .find { it.id == placeId }

        viewModelScope.launch {
            val location = LocationForegroundService.locationFlow.value
            val lat = location?.latitude ?: 35.0780
            val lng = location?.longitude ?: 128.8510
            val reachLat = place?.lat ?: lat
            val reachLng = place?.lng ?: lng

            // 1) SSE 구독 → 연결 확인 후 REST 호출 → SSE로 nearbyPlaces 수신
            guideSseClient.connect(
                onOpen = {
                    // 2) SSE 연결이 열린 후에만 안내 시작 (Kafka 발행 트리거)
                    viewModelScope.launch {
                        recommendRepository.startGuideNavigation(attrId, lat, lng, reachLat, reachLng)
                    }
                },
                onResponse = { guide ->
                    PlaceDetailCache.putGuide(placeId, guide)
                    guideSseClient.close()
                },
                onError = {
                    if (BuildConfig.DEBUG) Log.d("HomeViewModel", "SSE error", it)
                    guideSseClient.close()
                },
            )
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

    private fun removeUserInput() {
        _uiState.update { state ->
            state.copy(chatMessages = state.chatMessages.filterNot { it is ChatMessage.UserInput })
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
                _uiState.update { it.copy(spotCount = it.spotCount + newPlaces.size) }
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
        _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    private fun removeTyping() {
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.filterNot { it is ChatMessage.BotTyping },
            )
        }
    }

    private fun removeFindBtn() {
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.filterNot { it is ChatMessage.FindOtherPlacesBtn },
            )
        }
    }

    private fun markOptionAnswered(selected: String) {
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.map { msg ->
                    if (msg is ChatMessage.BotOptions && !msg.answered) {
                        msg.copy(answered = true, selectedOption = selected)
                    } else msg
                },
            )
        }
    }
}
