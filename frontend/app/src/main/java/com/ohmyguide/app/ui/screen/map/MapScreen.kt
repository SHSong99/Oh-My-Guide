package com.ohmyguide.app.ui.screen.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class MapUiState {
    object Loading : MapUiState()
    object Idle : MapUiState()
    data class Error(val message: String) : MapUiState()
}

@Composable
fun MapScreen(navController: NavController) {
    // TODO: 지도 렌더링, POI 마커, 경로 표시, 내 위치 추적
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    OhMyGuideTheme {
        MapScreen(rememberNavController())
    }
}
