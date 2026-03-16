package com.ohmyguide.app.ui.screen.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    object Idle : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

@Composable
fun ExploreScreen(navController: NavController) {
    // TODO: 테마별 탐색 (Attraction, Culture, Festival 등), 검색, 필터/정렬
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    OhMyGuideTheme {
        ExploreScreen(rememberNavController())
    }
}
