package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

import com.naver.maps.map.compose.rememberMarkerState
import com.naver.maps.map.overlay.OverlayImage
import com.ohmyguide.app.R
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.common.ConfirmDialog
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.screen.story.StoryOverlay
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitGray

private val PLACE_COORDINATES = mapOf(
    "dm3" to LatLng(35.0807, 128.8785),
    "dm4" to LatLng(35.1044, 128.9459),
    "dm5" to LatLng(35.1795, 128.9383),
    "dm6" to LatLng(35.2110, 128.9722),
    "dm7" to LatLng(35.0720, 128.9650),
    "p3" to LatLng(35.0850, 128.9200),
    "p4" to LatLng(35.0530, 128.9580),
    "p5" to LatLng(35.0470, 128.9660),
    // Course spots
    "dh1" to LatLng(37.5265, 127.0405),
    "dh2" to LatLng(37.5563, 126.9236),
    "dh3" to LatLng(37.5586, 126.9267),
)
private val DEFAULT_USER_POSITION = LatLng(35.0950, 128.8560)

private fun getModeLabel(mode: String, strings: com.ohmyguide.app.ui.theme.AppStrings): String = when (mode) {
    "walk" -> strings.walkingTo
    "transit" -> strings.transitTo
    "car" -> strings.drivingTo
    else -> strings.walkingTo
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun NaviScreen(
    navController: NavController,
    placeId: String,
    mode: String = "walk",
    courseId: String? = null,
    spotIndex: Int = 0,
    onMinimize: () -> Unit = {},
    viewModel: NaviViewModel = hiltViewModel(),
) {
    val strings = LocalStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val ttsManager = remember { com.ohmyguide.app.service.TtsManager(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var storyPlaceId by remember { mutableStateOf<String?>(null) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showStorySpotlight by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    BackHandler { showStopDialog = true }

    val detail = viewModel.detail
    val destLat = viewModel.destLat
    val destLng = viewModel.destLng
    val placeName = detail?.place?.name ?: strings.destination
    val placeNameKr = detail?.place?.nameKr ?: ""
    val naviRoute by viewModel.naviRoute.collectAsState()
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

    val sheetPeek by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (state.guideReady) 220.dp else 80.dp,
        animationSpec = androidx.compose.animation.core.tween(800),
        label = "sheetPeek",
    )

    // 줌인 완료 후 바텀시트를 화면 끝까지 올림
    LaunchedEffect(state.guideReady) {
        if (state.guideReady) {
            delay(2800L) // 줌인 애니메이션(2.5s) 끝난 직후
            scaffoldState.bottomSheetState.expand()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeek,
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
                    onStop = { showStopDialog = true },
                    onStory = {
                        showStorySpotlight = false
                        storyPlaceId = placeId
                    },
                    onPhrases = { viewModel.onPhrasesClick() },
                    storyHighlight = showStorySpotlight,
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
                                is NaviChatMessage.NearbySpotCard -> {
                                    NearbySpotDashboard(
                                        spot = msg.spot,
                                        onClick = { storyPlaceId = msg.spot.placeId },
                                    )
                                }
                                is NaviChatMessage.Phrases -> {
                                    PhrasesDashboard(
                                        items = msg.items,
                                        onSpeak = { text ->
                                            scope.launch { ttsManager.speak(text) }
                                        },
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
                                is NaviChatMessage.Weather -> {
                                    WeatherCard(info = msg.info)
                                }
                                is NaviChatMessage.StoryPrompt -> {
                                    LaunchedEffect(Unit) {
                                        showStorySpotlight = true
                                    }
                                    NaviBotBubble(
                                        text = "See the 🎧 button at the top? Tap it anytime to listen while you walk!",
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            },
        ) {
            MapArea(
                placeId = placeId,
                placeName = placeName,
                mode = mode,
                naviRoute = naviRoute,
                course = state.course,
                currentSpotIndex = state.spotIndex,
                onMinimize = onMinimize,
                destLat = destLat,
                destLng = destLng,
                guideReady = state.guideReady,
            )
        }

        if (storyPlaceId != null) {
            StoryOverlay(placeId = storyPlaceId!!, onDismiss = { storyPlaceId = null })
        }

        if (showStopDialog) {
            ConfirmDialog(
                title = strings.endNaviTitle,
                message = strings.endNaviMessage,
                confirmText = strings.confirm,
                dismissText = strings.cancel,
                onConfirm = {
                    showStopDialog = false
                    navController.popBackStack()
                },
                onDismiss = { showStopDialog = false },
            )
        }
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun MapArea(
    placeId: String,
    placeName: String,
    mode: String,
    naviRoute: NaviRouteData?,
    course: com.ohmyguide.app.fixtures.Course? = null,
    currentSpotIndex: Int = 0,
    onMinimize: () -> Unit,
    destLat: Double = 0.0,
    destLng: Double = 0.0,
    guideReady: Boolean = false,
) {
    val destinationPosition = if (destLat != 0.0 && destLng != 0.0) {
        LatLng(destLat, destLng)
    } else {
        PLACE_COORDINATES[placeId] ?: DEFAULT_USER_POSITION
    }
    val route = FALLBACK_ROUTES[placeId to mode]
    val fallbackCoords = route?.points?.map { LatLng(it.lat, it.lng) }

    // GPS 실시간 위치 가져오기 (GPS/Mock only)
    val locationData by LocationForegroundService.locationFlow.collectAsState()
    val userPosition = locationData?.let { LatLng(it.latitude, it.longitude) }
        ?: DEFAULT_USER_POSITION

    // 시작 시 전체 경로가 보이도록 줌아웃
    val midLat = (userPosition.latitude + destinationPosition.latitude) / 2
    val midLng = (userPosition.longitude + destinationPosition.longitude) / 2
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(midLat, midLng), 10.0)
    }

    // guideReady=true 시 GPS 위치로 깊게 줌인
    var zoomedIn by remember { mutableStateOf(false) }
    LaunchedEffect(guideReady) {
        if (guideReady && !zoomedIn) {
            zoomedIn = true
            cameraPositionState.animate(
                com.naver.maps.map.CameraUpdate.scrollAndZoomTo(userPosition, 19.0),
                animation = com.naver.maps.map.CameraAnimation.Fly,
                durationMs = 2500,
            )
        }
    }

    // 줌인 완료 후 GPS 위치 변경 시 카메라 따라가기
    LaunchedEffect(userPosition, zoomedIn) {
        if (zoomedIn) {
            cameraPositionState.animate(
                com.naver.maps.map.CameraUpdate.scrollTo(userPosition),
                animation = com.naver.maps.map.CameraAnimation.Easing,
                durationMs = 500,
            )
        }
    }

    val mapProperties = remember {
        MapProperties(
            locationTrackingMode = LocationTrackingMode.NoFollow,
        )
    }
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
            val mapLocale = LanguageManager.current.value.locale
            MapEffect(mapLocale) { naverMap ->
                naverMap.locale = mapLocale
            }

            if (naviRoute != null) {
                // Multi-colored polylines per segment
                naviRoute.segments.forEach { segment ->
                    val segCoords = segment.coords.map { LatLng(it.lat, it.lng) }
                    if (segCoords.size >= 2) {
                        PathOverlay(
                            coords = segCoords,
                            width = 8.dp,
                            color = segment.color,
                            outlineWidth = 2.dp,
                            outlineColor = segment.color.copy(alpha = 0.5f),
                        )
                    }
                }

                // Transfer markers at segment boundaries (skip first — near start)
                naviRoute.segments.forEachIndexed { index, segment ->
                    if (index > 1 && segment.coords.isNotEmpty()) {
                        val pt = segment.coords.first()
                        Marker(
                            state = rememberMarkerState(
                                key = "transfer_$index",
                                position = LatLng(pt.lat, pt.lng),
                            ),
                            icon = OverlayImage.fromResource(R.drawable.ic_marker_waypoint),
                            captionText = segment.lineName,
                            width = 24.dp,
                            height = 36.dp,
                        )
                    }
                }
            } else if (fallbackCoords != null && fallbackCoords.size >= 2) {
                val pathColor = when (mode) {
                    "car" -> TransitAmber
                    else -> TransitGray
                }
                PathOverlay(
                    coords = fallbackCoords,
                    width = 8.dp,
                    color = pathColor,
                    outlineWidth = 2.dp,
                    outlineColor = pathColor.copy(alpha = 0.5f),
                )
            }

            // 현재 위치 마커 (실시간 추적)
            val currentMarkerState = rememberMarkerState(key = "current_pos")
            LaunchedEffect(userPosition) {
                currentMarkerState.position = userPosition
            }
            Marker(
                state = currentMarkerState,
                icon = OverlayImage.fromResource(R.drawable.ic_marker_startpoint),
                width = 30.dp,
                height = 45.dp,
            )

            // 목적지 마커
            Marker(
                state = rememberMarkerState(position = destinationPosition),
                icon = OverlayImage.fromResource(R.drawable.ic_marker_destination),
                captionText = placeName,
                width = 36.dp,
                height = 54.dp,
            )

            // 코스 스팟 마커 (코스 모드일 때)
            if (course != null) {
                course.spots.forEachIndexed { index, spot ->
                    if (spot.id != placeId) {
                        val spotCoord = PLACE_COORDINATES[spot.id]
                        if (spotCoord != null) {
                            Marker(
                                state = rememberMarkerState(
                                    key = "course_spot_$index",
                                    position = spotCoord,
                                ),
                                captionText = "${index + 1}. ${spot.name}",
                                width = if (index <= currentSpotIndex) 28.dp else 24.dp,
                                height = if (index <= currentSpotIndex) 42.dp else 36.dp,
                                icon = if (index < currentSpotIndex) {
                                    OverlayImage.fromResource(R.drawable.ic_marker_waypoint)
                                } else {
                                    OverlayImage.fromResource(R.drawable.ic_marker_destination)
                                },
                                alpha = if (index <= currentSpotIndex) 1f else 0.6f,
                            )
                        }
                    }
                }
            }
        }

        // 코스 이동 중 배지 (좌측 상단)
        if (course != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(com.ohmyguide.app.ui.theme.Primary.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = BgWhite,
                )
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.material3.Text(
                    text = "${course.title} ${currentSpotIndex + 1}/${course.spots.size}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = BgWhite,
                )
            }
        }

        // Minimize 버튼 (우측 상단)
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
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minimize", modifier = Modifier.size(20.dp), tint = TextPrimary)
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