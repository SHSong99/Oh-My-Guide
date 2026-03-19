package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.TypingIndicator
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
    viewModel: NaviViewModel = hiltViewModel(),
) {
    var showStory by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()

    val detail = viewModel.detail
    val placeName = detail?.place?.name ?: "Destination"
    val placeNameKr = detail?.place?.nameKr ?: ""
    val route = FALLBACK_ROUTES[placeId to mode]
    val distance = route?.let { "${it.distanceMeters}m" } ?: "350m"
    val eta = route?.let { "${it.durationMin} min" } ?: detail?.walkTime ?: "5 min"
    val modeLabel = MODE_LABELS[mode] ?: "Walking to"

    val scaffoldState = rememberBottomSheetScaffoldState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 220.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                // ETA header
                EtaCard(
                    placeName = placeName,
                    distance = distance,
                    eta = eta,
                    modeLabel = modeLabel,
                )
                NaviProgressBar(
                    placeName = placeName,
                    placeNameKr = placeNameKr,
                    progressPct = state.progressPct,
                )

                // Chat messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 12.dp, bottom = 80.dp,
                    ),
                ) {
                    items(state.chatMessages.size) { index ->
                        val msg = state.chatMessages[index]
                        when (msg) {
                            is NaviChatMessage.BotText -> {
                                GuideBubble(
                                    text = msg.text,
                                    showAvatar = index == 0 ||
                                        state.chatMessages.getOrNull(index - 1)
                                            .let { it !is NaviChatMessage.BotText },
                                )
                            }
                            is NaviChatMessage.BotTyping -> {
                                TypingIndicator(showAvatar = false)
                            }
                            is NaviChatMessage.PlaceIntro -> {
                                PoiHeroCard(
                                    emoji = msg.detail.place.emoji.ifEmpty { "📍" },
                                    name = msg.detail.place.name,
                                    nameKr = msg.detail.place.nameKr,
                                )
                            }
                            is NaviChatMessage.ActionButtons -> {
                                NaviActionButtons(
                                    options = msg.options,
                                    answered = msg.answered,
                                    selected = msg.selected,
                                    onSelect = { viewModel.onActionSelect(it) },
                                    onStory = { showStory = true },
                                )
                            }
                            is NaviChatMessage.NearbyPoi -> {
                                NearbyPoiButtons(
                                    name = msg.name,
                                    answered = msg.answered,
                                    onAccept = { viewModel.onNearbyPoiResponse(true, msg.name) },
                                    onSkip = { viewModel.onNearbyPoiResponse(false, msg.name) },
                                )
                            }
                            is NaviChatMessage.ArrivalConfirm -> {
                                ArrivalConfirmButton(
                                    onClick = { viewModel.onArrivalConfirm() },
                                )
                            }
                            is NaviChatMessage.NearbyRecommendations -> {
                                NearbyPlaceCards(
                                    places = msg.places,
                                    onPlaceClick = { id ->
                                        navController.navigate("place/$id")
                                    },
                                )
                            }
                        }
                    }
                }
            },
        ) {
            MapArea(
                placeId = placeId,
                placeName = placeName,
                mode = mode,
                userLat = state.userLat,
                userLng = state.userLng,
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
    userLat: Double,
    userLng: Double,
    onMinimize: () -> Unit,
) {
    val destinationPosition = PLACE_COORDINATES[placeId] ?: DEFAULT_USER_POSITION
    val route = FALLBACK_ROUTES[placeId to mode]
    val routeCoords = route?.points?.map { LatLng(it.lat, it.lng) }
    val userPosition = LatLng(userLat, userLng)

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
                state = rememberMarkerState(position = userPosition),
                captionText = "You",
            )

            Marker(
                state = rememberMarkerState(position = destinationPosition),
                captionText = placeName,
            )
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