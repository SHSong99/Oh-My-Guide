package com.ohmyguide.app.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.PlaceCardDto
import com.ohmyguide.app.data.model.RefreshRecommendRequest
import com.ohmyguide.app.data.repository.RecommendRepository
import com.ohmyguide.app.fixtures.HOME_RECOMMENDATIONS
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.theme.CatAttraction
import com.ohmyguide.app.ui.theme.CatCulture
import com.ohmyguide.app.ui.theme.CatFood
import com.ohmyguide.app.ui.theme.CatLeports
import com.ohmyguide.app.ui.theme.CatShopping
import com.ohmyguide.app.ui.theme.CatFestival
import com.ohmyguide.app.ui.theme.CatCafe
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var flowStep = FlowStep.IDLE
    private var selectedFocus: String? = null
    private var initialLoaded = false

    fun loadInitialRecommendation(category: String) {
        if (initialLoaded) return
        initialLoaded = true

        viewModelScope.launch {
            addMessage(ChatMessage.BotTyping)

            // TODO: 테스트용 하드코딩 (부산 강서구 녹산동) — 배포 시 GPS로 복원
            val lat = 35.0946
            val lng = 128.8564

            val result = recommendRepository.getRecommendation(category, lat, lng)
            removeTyping()

            Log.d("HomeVM", "API result: success=${result.isSuccess}, error=${result.exceptionOrNull()?.message}")
            val dtos = result.getOrNull()
            Log.d("HomeVM", "DTOs: ${dtos?.size}, first=${dtos?.firstOrNull()}")
            val places = dtos?.map { it.toPlace() }
            Log.d("HomeVM", "Places: ${places?.size}, first=${places?.firstOrNull()?.name}")

            if (places.isNullOrEmpty()) {
                Log.d("HomeVM", "No places - showing error message")
                addMessage(ChatMessage.BotText("No places found nearby. Try a different category!"))
            } else {
                val section = RecommendationSection(
                    title = "Picks for You",
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
            val lat = 35.0946
            val lng = 128.8564
            val reachLat = place?.lat ?: lat
            val reachLng = place?.lng ?: lng
            recommendRepository.startGuideNavigation(attrId, lat, lng, reachLat, reachLng)
        }
    }

    // ── Show More ──

    fun onShowMore(sectionTitle: String) {
        viewModelScope.launch {
            addMessage(ChatMessage.UserText("Show more $sectionTitle"))
            addMessage(ChatMessage.BotTyping)

            val lat = 35.0946
            val lng = 128.8564
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
                addMessage(ChatMessage.BotText("Here are more $sectionTitle spots!"))
                addMessage(ChatMessage.BotRecommendation(extraSection))
            } else {
                addMessage(ChatMessage.BotText("No more places found nearby."))
            }
        }
    }

    // ── Find Other Places Flow ──

    fun onFindOtherPlaces() {
        viewModelScope.launch {
            removeFindBtn()
            addMessage(ChatMessage.UserText("Find other places"))
            addMessage(ChatMessage.BotTyping)
            delay(1200L)
            removeTyping()
            addMessage(ChatMessage.BotText("First, what's your main focus today?"))
            addMessage(
                ChatMessage.BotOptions(
                    options = listOf("Local Food & Cafe", "Photo Spots", "Shopping & Trends"),
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
            addMessage(ChatMessage.BotText("And what kind of vibe?"))
            addMessage(
                ChatMessage.BotOptions(
                    options = listOf("Active & Bustling", "Calm & Healing", "Nightlife"),
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

            // TODO: 테스트용 하드코딩 (부산 강서구 녹산동) — 배포 시 GPS로 복원
            val lat = 35.0946
            val lng = 128.8564

            val request = RefreshRecommendRequest(
                latitude = lat,
                longitude = lng,
                category = selectedFocus,
                mood = option,
            )

            val result = recommendRepository.refreshRecommendation(request)
            removeTyping()

            val newPlaces = result.getOrNull()?.map { it.toPlace() }

            if (newPlaces.isNullOrEmpty()) {
                addMessage(ChatMessage.BotText("Sorry, I couldn't find places right now. Please try again!"))
            } else {
                val newSection = RecommendationSection(
                    title = "New Picks for You",
                    icon = HOME_RECOMMENDATIONS[0].icon,
                    label = "$selectedFocus · $option",
                    places = newPlaces,
                    btnText = "",
                )
                addMessage(ChatMessage.BotText("Here are fresh picks based on your taste!"))
                addMessage(ChatMessage.BotRecommendation(newSection))
                _uiState.update { it.copy(spotCount = it.spotCount + newPlaces.size) }
            }

            addMessage(ChatMessage.FindOtherPlacesBtn)
            flowStep = FlowStep.IDLE
            selectedFocus = null
        }
    }

    companion object {
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
