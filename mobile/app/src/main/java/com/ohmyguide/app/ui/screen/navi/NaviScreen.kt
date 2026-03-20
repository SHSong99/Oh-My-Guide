package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.compose.rememberMarkerState
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.screen.story.StoryOverlay
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
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

private fun getModeLabel(mode: String, strings: com.ohmyguide.app.ui.theme.AppStrings): String = when (mode) {
    "walk" -> strings.walkingTo
    "transit" -> strings.transitTo
    "taxi" -> strings.drivingTo
    else -> strings.walkingTo
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun NaviScreen(
    navController: NavController,
    placeId: String,
    mode: String = "walk",
    onMinimize: () -> Unit = {},
    viewModel: NaviViewModel = hiltViewModel(),
) {
    val strings = LocalStrings.current
    var storyPlaceId by remember { mutableStateOf<String?>(null) }
    val state by viewModel.uiState.collectAsState()

    val detail = viewModel.detail
    val placeName = detail?.place?.name ?: strings.destination
    val placeNameKr = detail?.place?.nameKr ?: ""
    val route = FALLBACK_ROUTES[placeId to mode]
    val distance = route?.let { "${it.distanceMeters}m" } ?: "350m"
    val eta = route?.let { "${it.durationMin} min" } ?: detail?.walkTime ?: "5 min"
    val modeLabel = getModeLabel(mode, strings)

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
                // Unified header: place info + progress + stop
                NaviSheetHeader(
                    placeName = placeName,
                    placeNameKr = placeNameKr,
                    distance = distance,
                    eta = eta,
                    modeLabel = modeLabel,
                    progressPct = state.progressPct,
                    onStop = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Kkaebi header
                    item {
                        KkaebiHeader()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.chatMessages.size) { index ->
                        val msg = state.chatMessages[index]
                        // Show Kkaebi label when a new "turn" starts
                        val prevMsg = state.chatMessages.getOrNull(index - 1)
                        val isNewTurn = prevMsg != null && prevMsg !is NaviChatMessage.BotText
                            && prevMsg !is NaviChatMessage.BotTyping
                            && msg is NaviChatMessage.BotText

                        if (isNewTurn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            KkaebiLabel()
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        AnimatedMessageItem {
                            when (msg) {
                                is NaviChatMessage.BotText -> {
                                    NaviBotBubble(text = msg.text)
                                }
                                is NaviChatMessage.BotTyping -> {
                                    TypingIndicator(showAvatar = false)
                                }
                                is NaviChatMessage.PlaceIntro -> { /* removed */ }
                                is NaviChatMessage.TransitInfo -> {
                                    TransitInfoCard(info = msg.info)
                                }
                                is NaviChatMessage.DestinationDetail -> {
                                    DestinationDetailCard(
                                        detail = msg.detail,
                                        onClick = { storyPlaceId = msg.detail.place.id },
                                    )
                                }
                                is NaviChatMessage.NearbyPlaces -> {
                                    NearbyPlaceCarousel(
                                        places = msg.places,
                                        onPlaceClick = { id -> storyPlaceId = id },
                                    )
                                }
                                is NaviChatMessage.Phrases -> {
                                    PhrasesDashboard(
                                        items = msg.items,
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

                    // Fixed action buttons at the bottom of chat
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        NaviQuickActions(
                            onStory = { storyPlaceId = placeId },
                            onPhrases = { viewModel.onPhrasesClick() },
                        )
                    }
                }
            },
        ) {
            MapArea(
                placeId = placeId,
                placeName = placeName,
                mode = mode,
                onMinimize = onMinimize,
            )
        }

        if (storyPlaceId != null) {
            StoryOverlay(placeId = storyPlaceId!!, onDismiss = { storyPlaceId = null })
        }
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun MapArea(
    placeId: String,
    placeName: String,
    mode: String,
    onMinimize: () -> Unit,
) {
    val destinationPosition = PLACE_COORDINATES[placeId] ?: DEFAULT_USER_POSITION
    val route = FALLBACK_ROUTES[placeId to mode]
    val routeCoords = route?.points?.map { LatLng(it.lat, it.lng) }

    // GPS 실시간 위치 가져오기
    val locationData by LocationForegroundService.locationFlow.collectAsState()
    val userPosition = locationData?.let { LatLng(it.latitude, it.longitude) }
        ?: DEFAULT_USER_POSITION

    val locationSource = rememberFusedLocationSource()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(userPosition, 15.0)
    }

    val mapProperties = remember {
        MapProperties(
            locationTrackingMode = LocationTrackingMode.Follow,
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = mapProperties,
            uiSettings = mapUiSettings,
        ) {
            val mapLocale = LanguageManager.current.value.locale
            MapEffect(mapLocale) { naverMap ->
                naverMap.setLocale(mapLocale)
            }
            if (routeCoords != null && routeCoords.size >= 2) {
                PathOverlay(
                    coords = routeCoords,
                    width = 8.dp,
                    color = Primary,
                    outlineWidth = 2.dp,
                    outlineColor = PrimaryDark,
                )
            }

            // 목적지 마커
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
            Icon(Icons.Filled.Close, contentDescription = "Minimize", modifier = Modifier.size(18.dp), tint = TextPrimary)
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