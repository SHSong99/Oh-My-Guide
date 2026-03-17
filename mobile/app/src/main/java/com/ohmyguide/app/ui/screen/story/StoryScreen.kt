package com.ohmyguide.app.ui.screen.story

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class StoryUiState {
    object Loading : StoryUiState()
    object Idle : StoryUiState()
    data class Error(val message: String) : StoryUiState()
}

@Composable
fun StoryScreen(navController: NavController, placeId: String) {
    // TODO: 몰입형 스토리 모드, TTS 타이핑 텍스트, 이미지 슬라이드, 웨이브폼 오디오 플레이어
}

@Preview(showBackground = true)
@Composable
private fun StoryScreenPreview() {
    OhMyGuideTheme {
        StoryScreen(rememberNavController(), placeId = "preview")
    }
}
