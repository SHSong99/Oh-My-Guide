package com.ohmyguide.app.ui.screen.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.OmgButton
import com.ohmyguide.app.ui.common.OmgTopBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitBlueDark
import com.ohmyguide.app.ui.theme.TransitGray

private val SAMPLE_ROUTES = listOf(
    TransitRoute(
        id = "route1",
        title = "Bus 144 + 272",
        badge = "TRANSFER",
        badgeColor = TransitAmber,
        totalTime = "12 min",
        eta = "ETA 9:53 AM",
        segments = listOf(
            TransitSegment("bus", "Bus 144", InfoGreen, "Gwanghwamun", "Jongno 3-ga", 3, "7 min"),
            TransitSegment("walk", "Transfer", TransitGray, "Jongno 3-ga", "Jongno 3-ga", 0, "~2 min walk"),
            TransitSegment("bus", "Bus 272", InfoBlue, "Jongno 3-ga", "Jongno 5-ga", 2, "5 min"),
        ),
    ),
    TransitRoute(
        id = "route2",
        title = "Subway Line 1",
        badge = "SUBWAY",
        badgeColor = InfoBlue,
        totalTime = "15 min",
        eta = "ETA 9:56 AM",
        segments = listOf(
            TransitSegment("subway", "Line 1", TransitBlueDark, "Euljiro 1-ga", "Jongno 5-ga", 4, "15 min"),
        ),
    ),
    TransitRoute(
        id = "route3",
        title = "Bus 272",
        badge = "DIRECT",
        badgeColor = InfoGreen,
        totalTime = "18 min",
        eta = "ETA 9:59 AM",
        segments = listOf(
            TransitSegment("bus", "Bus 272", InfoBlue, "Gwanghwamun", "Jongno 5-ga", 5, "18 min"),
        ),
    ),
)

@Composable
fun TransitDetailScreen(navController: NavController, placeId: String) {
    var selectedRouteId by remember { mutableStateOf(SAMPLE_ROUTES[0].id) }
    var expandedRouteId by remember { mutableStateOf<String?>(SAMPLE_ROUTES[0].id) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SAMPLE_ROUTES.forEach { route ->
                RouteCard(
                    route = route,
                    selected = selectedRouteId == route.id,
                    expanded = expandedRouteId == route.id,
                    onClick = {
                        selectedRouteId = route.id
                        expandedRouteId = if (expandedRouteId == route.id) null else route.id
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GuideBubble(
                text = "Bus 144 + 272 is the fastest option. The transfer at Jongno 3-ga takes only 2 minutes!",
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        OmgButton(
            text = "Start Navigation",
            onClick = {
                navController.navigate(Screen.Navi.createRoute(placeId, "transit"))
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TransitDetailScreenPreview() {
    OhMyGuideTheme {
        TransitDetailScreen(rememberNavController(), placeId = "dm3")
    }
}
