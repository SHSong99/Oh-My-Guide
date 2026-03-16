package com.ohmyguide.app.ui.screen.mypage

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class MyPageUiState {
    object Loading : MyPageUiState()
    object Idle : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}

@Composable
fun MyPageScreen(navController: NavController) {
    // TODO: 프로필, 방문 기록, 북마크 목록, 설정, 로그아웃
}

@Preview(showBackground = true)
@Composable
private fun MyPageScreenPreview() {
    OhMyGuideTheme {
        MyPageScreen(rememberNavController())
    }
}
