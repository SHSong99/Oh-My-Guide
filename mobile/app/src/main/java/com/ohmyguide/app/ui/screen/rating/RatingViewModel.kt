package com.ohmyguide.app.ui.screen.rating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.repository.GuideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RatingUiState(
    val star: Int = 0,
    val submitting: Boolean = false,
    val submitted: Boolean = false,
)

@HiltViewModel
class RatingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val guideRepository: GuideRepository,
) : ViewModel() {

    val placeId: String = savedStateHandle["placeId"] ?: ""
    val placeName: String = java.net.URLDecoder.decode(
        savedStateHandle["placeName"] ?: "", "UTF-8",
    )

    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState: StateFlow<RatingUiState> = _uiState.asStateFlow()

    fun selectStar(star: Int) {
        _uiState.update { it.copy(star = star) }
    }

    fun submit() {
        val star = _uiState.value.star
        if (star == 0 || _uiState.value.submitting) return

        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            guideRepository.submitRating(placeId.toLongOrNull() ?: 0L, star)
            _uiState.update { it.copy(submitting = false, submitted = true) }
        }
    }
}
