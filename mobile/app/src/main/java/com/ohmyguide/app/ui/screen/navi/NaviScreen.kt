package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.screen.story.StoryOverlay
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryDark
import com.ohmyguide.app.ui.theme.TextPrimary

private val PLACE_COORDINATES = mapOf(
    "dm3" to LatLng(37.5700, 126.9990),
    "dm4" to LatLng(37.5826, 126.9831),
    "dm5" to LatLng(37.5512, 126.9882),
    "dm6" to LatLng(37.5735, 126.9920),
    "dm7" to LatLng(37.5690, 126.9780),
)
private val DEFAULT_USER_POSITION = LatLng(37.5665, 126.9780)

private val MODE_LABELS = mapOf(
    "walk" to "Walking to",
    "transit" to "Transit to",
    "taxi" to "Driving to",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun NaviScreen(
    navController: NavController,
    placeId: String,
    mode: String = "walk",
    onMinimize: () -> Unit = {},
) {
    var showStory by remember { mutableStateOf(false) }

    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Destination"
    val placeNameKr = detail?.place?.nameKr ?: ""
    val route = FALLBACK_ROUTES[placeId to mode]
    val distance = route?.let { "${it.distanceMeters}m" } ?: "350m"
    val eta = route?.let { "${it.durationMin} min" } ?: detail?.walkTime ?: "5 min"
    val modeLabel = MODE_LABELS[mode] ?: "Walking to"

    val scaffoldState = rememberBottomSheetScaffoldState()

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 140.dp,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContainerColor = BgWhite,
            sheetDragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DragHandle),
                    )
                }
            },
            sheetContent = {
                NaviSheetContent(
                    placeName = placeName,
                    placeNameKr = placeNameKr,
                    distance = distance,
                    eta = eta,
                    modeLabel = modeLabel,
                    detail = detail,
                    onStory = { showStory = true },
                )
            },
        ) {
            MapArea(
                placeId = placeId,
                placeName = placeName,
                mode = mode,
                onBack = { navController.popBackStack() },
                onMinimize = onMinimize,
            )
        }

        if (showStory) {
            StoryOverlay(placeId = placeId, onDismiss = { showStory = false })
        }
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun MapArea(
    placeId: String,
    placeName: String,
    mode: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
) {
    val destinationPosition = PLACE_COORDINATES[placeId] ?: DEFAULT_USER_POSITION
    val route = FALLBACK_ROUTES[placeId to mode]
    val routeCoords = route?.points?.map { LatLng(it.lat, it.lng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(destinationPosition, 15.0)
    }

    val mapProperties = remember { MapProperties() }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
        ) {
            if (routeCoords != null && routeCoords.size >= 2) {
                PathOverlay(
                    coords = routeCoords,
                    width = 8.dp,
                    color = Primary,
                    outlineWidth = 2.dp,
                    outlineColor = PrimaryDark,
                )
            }

            Marker(
                state = rememberMarkerState(position = DEFAULT_USER_POSITION),
                captionText = "You",
            )

            Marker(
                state = rememberMarkerState(position = destinationPosition),
                captionText = placeName,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BgWhite.copy(alpha = 0.9f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BgWhite.copy(alpha = 0.9f))
                .clickable(onClick = onMinimize),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NaviScreenPreview() {
    OhMyGuideTheme {
        NaviScreen(rememberNavController(), placeId = "dm3")
    }
}
