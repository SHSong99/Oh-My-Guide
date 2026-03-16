package com.ohmyguide.app.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Idle : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@Composable
fun HomeScreen(navController: NavController) {
    // TODO: AI 챗봇 대화, 장소/회화 추천 캐러셀, 설문형 대화, 바텀시트 3단 스냅
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    OhMyGuideTheme {
        HomeScreen(rememberNavController())
    }
}
