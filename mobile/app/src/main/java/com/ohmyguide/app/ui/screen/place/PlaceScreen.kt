package com.ohmyguide.app.ui.screen.place

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class PlaceUiState {
    object Loading : PlaceUiState()
    object Idle : PlaceUiState()
    data class Error(val message: String) : PlaceUiState()
}

@Composable
fun PlaceScreen(navController: NavController, placeId: String) {
    // TODO: 장소 상세 정보, 운영 정보, 북마크/저장, 리뷰
}

@Preview(showBackground = true)
@Composable
private fun PlaceScreenPreview() {
    OhMyGuideTheme {
        PlaceScreen(rememberNavController(), placeId = "preview")
    }
}
