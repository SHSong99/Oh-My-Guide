package com.ohmyguide.app.ui.screen.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.common.OmgButton
import com.ohmyguide.app.ui.common.OmgTopBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun TransitDetailScreen(
    navController: NavController,
    placeId: String,
    courseId: String? = null,
    viewModel: TransitDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var expandedRouteId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(TransitFilter.All) }

    // Auto-expand first route
    if (expandedRouteId == null && state.routes.isNotEmpty()) {
        expandedRouteId = state.routes[0].id
    }

    val filteredRoutes = when (selectedFilter) {
        TransitFilter.All -> state.routes
        TransitFilter.Bus -> state.routes.filter { it.pathType == 2 }
        TransitFilter.Subway -> state.routes.filter { it.pathType == 1 }
        TransitFilter.BusSubway -> state.routes.filter { it.pathType == 3 }
    }

    val placeName = SAMPLE_PLACE_DETAILS[placeId]?.place?.name ?: placeId
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        // Header
        OmgTopBar(
            title = strings.transitRoutes,
            onBack = { navController.popBackStack() },
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = LocalStrings.current.searchingRoutes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCaption,
                        )
                    }
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LocalStrings.current.noTransitRoutes,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextCaption,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = LocalStrings.current.tryWalkOrCar,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCaption,
                        )
                    }
                }
            }
            else -> {
                // Filter tabs
                TransitFilterTabs(
                    routes = state.routes,
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                )

                // Route list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    filteredRoutes.forEach { route ->
                        RouteCard(
                            route = route,
                            expanded = expandedRouteId == route.id,
                            onClick = {
                                expandedRouteId =
                                    if (expandedRouteId == route.id) null else route.id
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        OmgButton(
            text = LocalStrings.current.startNavigation,
            onClick = {
                expandedRouteId?.let { viewModel.selectRouteForNavi(it) }
                if (!courseId.isNullOrEmpty()) {
                    navController.navigate(
                        Screen.CourseNavi.createRoute(courseId, "transit")
                    )
                } else {
                    navController.navigate(Screen.Navi.createRoute(placeId, "transit"))
                }
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