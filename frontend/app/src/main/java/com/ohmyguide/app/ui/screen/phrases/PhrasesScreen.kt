package com.ohmyguide.app.ui.screen.phrases

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class PhrasesUiState {
    object Loading : PhrasesUiState()
    object Idle : PhrasesUiState()
    data class Error(val message: String) : PhrasesUiState()
}

@Composable
fun PhrasesScreen(navController: NavController) {
    // TODO: 상황별 한국어 회화, 발음 재생, 즐겨찾기
}

@Preview(showBackground = true)
@Composable
private fun PhrasesScreenPreview() {
    OhMyGuideTheme {
        PhrasesScreen(rememberNavController())
    }
}
