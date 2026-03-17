package com.ohmyguide.app.ui.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Idle : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@Composable
fun AuthScreen(navController: NavController) {
    // TODO: 스플래시 → 웰컴 → 소셜 로그인 → GPS 권한 → 관심사 선택
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    OhMyGuideTheme {
        AuthScreen(rememberNavController())
    }
}
