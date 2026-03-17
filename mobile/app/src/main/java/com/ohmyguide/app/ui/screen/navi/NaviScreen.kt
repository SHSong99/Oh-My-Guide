package com.ohmyguide.app.ui.screen.navi

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class NaviUiState {
    object Loading : NaviUiState()
    object Idle : NaviUiState()
    data class Error(val message: String) : NaviUiState()
}

@Composable
fun NaviScreen(navController: NavController, placeId: String) {
    // TODO: 도보 경로 안내, POI Hero Card, Info Grid, Price Guide, Photo Spots, Audio Guide
}

@Preview(showBackground = true)
@Composable
private fun NaviScreenPreview() {
    OhMyGuideTheme {
        NaviScreen(rememberNavController(), placeId = "preview")
    }
}
