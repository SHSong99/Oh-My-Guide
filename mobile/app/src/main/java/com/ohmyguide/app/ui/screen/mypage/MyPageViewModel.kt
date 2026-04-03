package com.ohmyguide.app.ui.screen.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ohmyguide.app.data.model.AttractionDetailDto
import com.ohmyguide.app.data.model.UserResponse
import com.ohmyguide.app.data.repository.AuthRepository
import com.ohmyguide.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickPlace(
    val attrId: Long,
    val title: String,
    val imageUrl: String?,
    val addr: String?,
    val rank: Int,
)

data class MyPageUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = true,
    val pickPlaces: List<PickPlace> = emptyList(),
    val pickLoading: Boolean = false,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val result = userRepository.getCurrentUser()
            val user = result.getOrNull()
            _uiState.update {
                it.copy(user = user, isLoading = false)
            }
            if (user != null) {
                loadPickRecommend(user)
            }
        }
    }

    private fun loadPickRecommend(user: UserResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(pickLoading = true) }

            val result = userRepository.getPickRecommend(
                nationality = user.nationality ?: "USA",
                age = user.age ?: 25,
                gender = user.gender ?: "Male",
                travelPurpose = user.travelPurpose?.replaceFirstChar { it.uppercase() } ?: "Solo",
            )

            val picks = result.getOrNull() ?: emptyList()
            if (picks.isNotEmpty()) {
                val places = picks.take(5).map { pick ->
                    async {
                        val detail = userRepository.getAttractionDetail(pick.placeId).getOrNull()
                        if (detail != null) {
                            PickPlace(
                                attrId = pick.placeId,
                                title = detail.title ?: "",
                                imageUrl = detail.firstImage1,
                                addr = detail.addr1,
                                rank = pick.placeRank ?: 0,
                            )
                        } else null
                    }
                }.awaitAll().filterNotNull()
                _uiState.update { it.copy(pickPlaces = places, pickLoading = false) }
            } else {
                _uiState.update { it.copy(pickLoading = false) }
            }
        }
    }

    fun updateProfile(nationality: String, age: Int, gender: String) {
        viewModelScope.launch {
            val result = userRepository.completeOnboarding(nationality, age, gender)
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(user = updated) }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
