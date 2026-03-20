package com.ohmyguide.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.fixtures.HOME_RECOMMENDATIONS
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.fixtures.SAMPLE_PLACES
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
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var flowStep = FlowStep.IDLE
    private var selectedFocus: String? = null

    init {
        initChat()
    }

    private fun initChat() {
        val initial = mutableListOf<ChatMessage>()
        initial += ChatMessage.BotText("Based on your choices, I've found perfect matches for you.")
        HOME_RECOMMENDATIONS.forEach { section ->
            initial += ChatMessage.BotRecommendation(section)
        }
        initial += ChatMessage.FindOtherPlacesBtn
        _uiState.update { it.copy(chatMessages = initial) }
    }

    // ── Place Detail ──

    fun selectPlace(placeId: String) {
        val detail = SAMPLE_PLACE_DETAILS[placeId] ?: return
        _uiState.update {
            it.copy(
                sheetMode = SheetMode.PLACE_DETAIL,
                selectedDetail = detail,
            )
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

    // ── Show More ──

    fun onShowMore(sectionTitle: String) {
        viewModelScope.launch {
            addMessage(ChatMessage.UserText("Show more $sectionTitle"))
            addMessage(ChatMessage.BotTyping)
            delay(1200L)
            removeTyping()

            val extraPlaces = SAMPLE_PLACES.shuffled().take(4)
            val extraSection = RecommendationSection(
                title = sectionTitle,
                icon = HOME_RECOMMENDATIONS.firstOrNull { it.title == sectionTitle }?.icon
                    ?: HOME_RECOMMENDATIONS[0].icon,
                label = "More",
                places = extraPlaces,
                btnText = "",
            )
            addMessage(ChatMessage.BotText("Here are more $sectionTitle spots!"))
            addMessage(ChatMessage.BotRecommendation(extraSection))
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
            delay(800L)
            removeTyping()
            addMessage(ChatMessage.BotText("Updating your preference vector..."))
            addMessage(ChatMessage.BotTyping)
            delay(1500L)
            removeTyping()

            val newPlaces = generateNewRecommendations()
            val newSection = RecommendationSection(
                title = "New Picks for You",
                icon = HOME_RECOMMENDATIONS[0].icon,
                label = "$selectedFocus · $option",
                places = newPlaces,
                btnText = "Show more new picks",
            )
            addMessage(ChatMessage.BotText("Here are fresh picks based on your taste!"))
            addMessage(ChatMessage.BotRecommendation(newSection))
            addMessage(ChatMessage.FindOtherPlacesBtn)

            _uiState.update { it.copy(spotCount = it.spotCount + newPlaces.size) }

            flowStep = FlowStep.IDLE
            selectedFocus = null
        }
    }

    private fun generateNewRecommendations(): List<Place> {
        return SAMPLE_PLACES.shuffled().take(3)
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
