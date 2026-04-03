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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalDensity
import coil.imageLoader
import coil.request.ImageRequest
import com.ohmyguide.app.R
import com.ohmyguide.app.domain.model.NaviRouteData
import com.ohmyguide.app.fixtures.FALLBACK_ROUTES
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.common.ConfirmDialog
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.common.buildCircleMarker
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
    val ttsSpeaking by ttsManager.isSpeaking.collectAsState()
    val ttsLoading by ttsManager.isLoading.collectAsState()
    var ttsSpeakingText by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var storyPlaceId by remember { mutableStateOf<String?>(null) }
    val onStoryPlaceClick = remember<(String) -> Unit> { { id -> storyPlaceId = id } }
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

    // 도착 확인 후 1.5초 딜레이 → RatingScreen 전환
    LaunchedEffect(state.arrived) {
        if (state.arrived) {
            delay(1500L)
            navController.navigate(Screen.Rating.createRoute(placeId, placeName)) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }
    }

    // 줌인 완료 후 바텀시트를 화면 끝까지 올림
    LaunchedEffect(state.guideReady) {
        if (state.guideReady) {
            delay(6000L) // 깨비 인사(3s) + 줌인(2.5s) + 여유
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
                // Action buttons header
                NaviSheetHeader(
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

                    items(
                        count = state.chatMessages.size,
                        key = { index ->
                            val msg = state.chatMessages[index]
                            when (msg) {
                                is NaviChatMessage.BotText -> "bot_$index"
                                is NaviChatMessage.BotTyping -> "typing_$index"
                                is NaviChatMessage.PlaceIntro -> "intro_${msg.detail.place.id}"
                                is NaviChatMessage.TransitInfo -> "transit_$index"
                                is NaviChatMessage.TransitGuide -> "guide_$index"
                                is NaviChatMessage.DestinationDetail -> "dest_${msg.detail.place.id}"
                                is NaviChatMessage.NearbyPlaces -> "nearby_$index"
                                is NaviChatMessage.NearbySpotCard -> "spot_${msg.spot.placeId}"
                                is NaviChatMessage.Phrases -> "phrases_$index"
                                is NaviChatMessage.ArrivalConfirm -> "arrival_$index"
                                is NaviChatMessage.NearbyRecommendations -> "reco_$index"
                                is NaviChatMessage.Weather -> "weather_$index"
                                is NaviChatMessage.StoryPrompt -> "story_$index"
                            }
                        },
                    ) { index ->
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
                                is NaviChatMessage.TransitGuide -> {
                                    TransitGuideCard(info = msg.info)
                                }
                                is NaviChatMessage.DestinationDetail -> {
                                    DestinationDetailCard(
                                        detail = msg.detail,
                                        onClick = { onStoryPlaceClick(msg.detail.place.id) },
                                    )
                                }
                                is NaviChatMessage.NearbyPlaces -> {
                                    NearbyPlaceCarousel(
                                        places = msg.places,
                                        onPlaceClick = onStoryPlaceClick,
                                    )
                                }
                                is NaviChatMessage.NearbySpotCard -> {
                                    NearbySpotDashboard(
                                        spot = msg.spot,
                                        onClick = { onStoryPlaceClick(msg.spot.placeId) },
                                    )
                                }
                                is NaviChatMessage.Phrases -> {
                                    PhrasesDashboard(
                                        items = msg.items,
                                        speakingText = ttsSpeakingText,
                                        isSpeaking = ttsSpeaking,
                                        isLoading = ttsLoading,
                                        onSpeak = { text ->
                                            if (ttsSpeakingText == text && ttsSpeaking) {
                                                ttsManager.pause()
                                            } else if (ttsSpeakingText == text && ttsManager.hasPaused()) {
                                                ttsManager.resume()
                                            } else {
                                                ttsSpeakingText = text
                                                scope.launch { ttsManager.speak(text) }
                                            }
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
                                        text = strings.storyPromptHint,
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
                nearbySpots = state.pickedNearbySpots,
                progressPct = state.progressPct,
                onStop = { showStopDialog = true },
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
    nearbySpots: List<NearbySpotInfo> = emptyList(),
    progressPct: Float = 0f,
    onStop: () -> Unit = {},
) {
    // 근처 장소 원형 이미지 마커 로드
    val mapContext = androidx.compose.ui.platform.LocalContext.current
    val density = LocalDensity.current
    val nearbyMarkerSizeDp = 44.dp
    val nearbyMarkerSizePx = with(density) { nearbyMarkerSizeDp.toPx().toInt() }
    val nearbyBorderPx = with(density) { 3.dp.toPx() }
    val nearbyMarkerIcons = remember { mutableStateMapOf<String, OverlayImage>() }

    LaunchedEffect(nearbySpots) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            nearbySpots.forEach { spot ->
                if (!spot.imageUrl.isNullOrBlank() && spot.placeId !in nearbyMarkerIcons) {
                    val request = ImageRequest.Builder(mapContext)
                        .data(spot.imageUrl)
                        .size(nearbyMarkerSizePx)
                        .allowHardware(false)
                        .build()
                    val result = mapContext.imageLoader.execute(request)
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val icon = buildCircleMarker(
                            bitmap, nearbyMarkerSizePx, nearbyBorderPx, android.graphics.Color.WHITE
                        )
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            nearbyMarkerIcons[spot.placeId] = icon
                        }
                    }
                }
            }
        }
    }

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

    // 시작 시 목적지 중심으로 보여주고, GPS 잡히면 전체 경로로 전환
    val initialCenter = userPosition?.let {
        val midLat = (it.latitude + destinationPosition.latitude) / 2
        val midLng = (it.longitude + destinationPosition.longitude) / 2
        LatLng(midLat, midLng)
    } ?: destinationPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(initialCenter, 13.0)
    }

    // GPS가 처음 잡히면 전체 경로 보이도록 카메라 이동
    var initialCameraSet by remember { mutableStateOf(userPosition != null) }
    LaunchedEffect(userPosition) {
        if (!initialCameraSet && userPosition != null) {
            initialCameraSet = true
            val midLat = (userPosition.latitude + destinationPosition.latitude) / 2
            val midLng = (userPosition.longitude + destinationPosition.longitude) / 2
            cameraPositionState.animate(
                com.naver.maps.map.CameraUpdate.scrollAndZoomTo(LatLng(midLat, midLng), 10.0),
                animation = com.naver.maps.map.CameraAnimation.Easing,
                durationMs = 800,
            )
        }
    }

    // 줌인 상태 관리
    var zoomPhase by remember { mutableStateOf(0) } // 0=줌아웃, 1=줌인중, 2=줌인완료

    // guideReady 후 3초 대기(깨비 인사 보여준 뒤) → 줌인 시작
    LaunchedEffect(guideReady) {
        if (guideReady && zoomPhase == 0) {
            delay(3000L) // 깨비 인사 보여주는 시간
            val target = userPosition ?: destinationPosition
            zoomPhase = 1
            cameraPositionState.animate(
                com.naver.maps.map.CameraUpdate.scrollAndZoomTo(target, 17.0),
                animation = com.naver.maps.map.CameraAnimation.Fly,
                durationMs = 2500,
            )
            zoomPhase = 2
        }
    }

    // 줌인 완료 후에만 GPS 따라가기
    LaunchedEffect(userPosition, zoomPhase) {
        if (zoomPhase == 2 && userPosition != null) {
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

            // 현재 위치 마커 (실시간 추적, GPS 잡힌 경우에만)
            if (userPosition != null) {
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
            }

            // 목적지 마커
            Marker(
                state = rememberMarkerState(position = destinationPosition),
                icon = OverlayImage.fromResource(R.drawable.ic_marker_destination),
                captionText = placeName,
                width = 36.dp,
                height = 54.dp,
            )

            // 근처 추천 장소 마커 (원형 이미지)
            nearbySpots.forEachIndexed { index, spot ->
                val icon = nearbyMarkerIcons[spot.placeId]
                if (icon != null) {
                    Marker(
                        state = rememberMarkerState(
                            key = "nearby_$index",
                            position = LatLng(spot.lat, spot.lng),
                        ),
                        icon = icon,
                        captionText = spot.name,
                        width = nearbyMarkerSizeDp,
                        height = nearbyMarkerSizeDp,
                    )
                } else {
                    Marker(
                        state = rememberMarkerState(
                            key = "nearby_$index",
                            position = LatLng(spot.lat, spot.lng),
                        ),
                        icon = OverlayImage.fromResource(R.drawable.ic_marker_waypoint),
                        captionText = spot.name,
                        width = 24.dp,
                        height = 36.dp,
                        alpha = 0.8f,
                    )
                }
            }

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

        // 상단 네비게이션 바 (줌인 완료 후 표시)
        if (zoomPhase == 2) {
            val route = FALLBACK_ROUTES[placeId to mode]
            MapNavBar(
                distance = route?.let { "${it.distanceMeters}m" } ?: "350m",
                eta = route?.let { "${it.durationMin} min" } ?: "5 min",
                placeName = placeName,
                progressPct = progressPct,
                onStop = onStop,
            )
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


        // 깨비 인사 오버레이 — guideReady 시 등장, 줌인 완료 후 사라짐
        var showGreeting by remember { mutableStateOf(false) }
        LaunchedEffect(guideReady) {
            if (guideReady) {
                showGreeting = true   // 즉시 등장
                delay(6000L)          // 줌인(3초 대기 + 2.5초 애니메이션) 완료 후
                showGreeting = false  // 페이드아웃
            }
        }
        val greetingAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (showGreeting) 1f else 0f,
            animationSpec = androidx.compose.animation.core.tween(600),
            label = "greetingAlpha",
        )
        if (greetingAlpha > 0f) {
            // 펄스 애니메이션
            val pulseTransition = rememberInfiniteTransition(label = "greetPulse")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "pulseScale",
            )

            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
                    .graphicsLayer {
                        alpha = greetingAlpha
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(com.ohmyguide.app.ui.theme.Primary.copy(alpha = 0.85f))
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.masot),
                    contentDescription = "Kkaebi",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(BgWhite),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                androidx.compose.material3.Text(
                    text = "안녕하세요! 👋",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = BgWhite,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = "도착까지 친절하게 안내해 드릴게요!",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = BgWhite.copy(alpha = 0.9f),
                )
            }
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