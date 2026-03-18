package com.ohmyguide.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.HOME_RECOMMENDATIONS
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Idle : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        HomeHeader()

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                item {
                    LocationBar(spotCount = 6)
                }
                item {
                    GuideBubble(
                        text = "Based on your choices, I've found perfect matches for you.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(HOME_RECOMMENDATIONS) { section ->
                    RecommendationBlock(
                        section = section,
                        onPlaceClick = { placeId ->
                            navController.navigate("place/$placeId")
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BgWhite.copy(alpha = 0f),
                                BgWhite.copy(alpha = 0.9f),
                                BgWhite,
                            ),
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                FindOtherPlacesButton(onClick = { })
            }
        }

        BottomNavBar(
            activeTab = "main",
            onTabChange = { tab ->
                when (tab) {
                    "explore" -> navController.navigate(Screen.Explore.route)
                    "phrases" -> navController.navigate(Screen.Phrases.route)
                }
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    OhMyGuideTheme {
        HomeScreen(rememberNavController())
    }
}
