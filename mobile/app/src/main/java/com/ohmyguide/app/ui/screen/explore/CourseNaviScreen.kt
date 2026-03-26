package com.ohmyguide.app.ui.screen.explore

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ohmyguide.app.R
import kotlinx.coroutines.delay
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState
import com.naver.maps.map.overlay.OverlayImage
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.Spot
import com.ohmyguide.app.ui.common.ConfirmDialog
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.common.buildCircleMarker
import com.ohmyguide.app.ui.screen.navi.AnimatedMessageItem
import com.ohmyguide.app.ui.screen.navi.KkaebiHeader
import com.ohmyguide.app.ui.screen.navi.KkaebiLabel
import com.ohmyguide.app.ui.screen.navi.NaviBotBubble
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.Error
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun CourseNaviScreen(
    navController: NavController,
    courseId: String,
    mode: String = "car",
    viewModel: CourseNaviViewModel = hiltViewModel(),
) {
    val course by viewModel.course.collectAsState()
    val currentCourse = course ?: return

    val uiState by viewModel.uiState.collectAsState()
    val routeData by viewModel.routeData.collectAsState()
    val currentSpot = currentCourse.spots.getOrNull(uiState.currentSpotIndex) ?: return
    val totalSpots = currentCourse.spots.size
    val progressPct = (uiState.currentSpotIndex + 1).toFloat() / totalSpots

    var storyPlaceId by remember { mutableStateOf<String?>(null) }
    var showStopDialog by remember { mutableStateOf(false) }
    var toastSpotName by remember { mutableStateOf<String?>(null) }

    // Collect spot advance event → show toast
    LaunchedEffect(Unit) {
        viewModel.spotAdvanceEvent.collect { spotName ->
            toastSpotName = spotName
            delay(3000L)
            toastSpotName = null
        }
    }

    androidx.activity.compose.BackHandler { showStopDialog = true }

    // Image markers
    val context = LocalContext.current
    val density = LocalDensity.current
    val markerSizePx = with(density) { 48.dp.roundToPx() }
    val markerIcons = remember { mutableStateMapOf<String, OverlayImage>() }

    currentCourse.spots.forEach { spot ->
        if (spot.imageUrl != null && !markerIcons.containsKey(spot.id)) {
            val request = ImageRequest.Builder(context)
                .data(spot.imageUrl)
                .size(markerSizePx)
                .allowHardware(false)
                .target { drawable ->
                    val bmp = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
                    val borderColor = if (spot.id == currentSpot.id) {
                        AndroidColor.parseColor("#5478FF")
                    } else {
                        AndroidColor.WHITE
                    }
                    markerIcons[spot.id] = buildCircleMarker(bmp, markerSizePx, 4f, borderColor)
                }
                .build()
            coil.ImageLoader(context).enqueue(request)
        }
    }

    // GPS location
    val locationData by com.ohmyguide.app.service.LocationForegroundService.locationFlow.collectAsState()
    val userPosition = locationData?.let { LatLng(it.latitude, it.longitude) }

    val firstSpot = currentCourse.spots.first()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(firstSpot.lat, firstSpot.lng), 13.0)
    }
    val mapProperties = remember { MapProperties() }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = false)
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    // Zoom to current spot when it changes
    LaunchedEffect(uiState.currentSpotIndex) {
        val spot = currentCourse.spots.getOrNull(uiState.currentSpotIndex) ?: return@LaunchedEffect
        cameraPositionState.animate(
            com.naver.maps.map.CameraUpdate.scrollAndZoomTo(LatLng(spot.lat, spot.lng), 15.0),
            animation = com.naver.maps.map.CameraAnimation.Fly,
            durationMs = 1500,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 280.dp,
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
                // Course header: spot info + progress + stop
                CourseNaviSheetHeader(
                    courseName = currentCourse.title,
                    currentSpot = currentSpot,
                    currentSpotIndex = uiState.currentSpotIndex,
                    totalSpots = totalSpots,
                    progressPct = progressPct,
                    onStory = { storyPlaceId = currentSpot.id },
                    onStop = { showStopDialog = true },
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
                    item {
                        KkaebiHeader()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.chatMessages.size) { index ->
                        val msg = uiState.chatMessages[index]
                        val prevMsg = uiState.chatMessages.getOrNull(index - 1)
                        val isNewTurn = prevMsg != null
                            && prevMsg !is CourseNaviChatMessage.BotText
                            && prevMsg !is CourseNaviChatMessage.BotTyping
                            && msg is CourseNaviChatMessage.BotText

                        if (isNewTurn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            KkaebiLabel()
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        AnimatedMessageItem {
                            when (msg) {
                                is CourseNaviChatMessage.BotText -> {
                                    NaviBotBubble(text = msg.text)
                                }
                                is CourseNaviChatMessage.BotTyping -> {
                                    TypingIndicator(showAvatar = false)
                                }
                                is CourseNaviChatMessage.SpotCard -> {
                                    SpotGuideCard(
                                        spot = msg.spot,
                                        spotIndex = msg.spotIndex,
                                        totalSpots = totalSpots,
                                        onClick = { storyPlaceId = msg.spot.id },
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
            Box(modifier = Modifier.fillMaxSize()) {
                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                ) {
                    val mapLocale = LanguageManager.current.value.locale
                    MapEffect(mapLocale) { naverMap ->
                        naverMap.setLocale(mapLocale)
                    }

                    routeData?.segments?.forEach { segment ->
                        val coords = segment.coords.map { LatLng(it.lat, it.lng) }
                        if (coords.size >= 2) {
                            PathOverlay(
                                coords = coords,
                                width = 6.dp,
                                color = segment.color,
                                outlineWidth = 2.dp,
                                outlineColor = segment.color.copy(alpha = 0.4f),
                            )
                        }
                    }

                    // Current spot pulsing ripple
                    val activeSpot = currentCourse.spots.getOrNull(uiState.currentSpotIndex)
                    if (activeSpot != null && activeSpot.lat != 0.0) {
                        val pulseTransition = rememberInfiniteTransition(label = "spotPulse")
                        val pulseRadius by pulseTransition.animateFloat(
                            initialValue = 20f,
                            targetValue = 45f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "pulseR",
                        )
                        val pulseAlpha by pulseTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "pulseA",
                        )
                        com.naver.maps.map.compose.CircleOverlay(
                            center = LatLng(activeSpot.lat, activeSpot.lng),
                            radius = pulseRadius.toDouble(),
                            color = Primary.copy(alpha = pulseAlpha),
                            outlineWidth = 0.dp,
                            outlineColor = Primary.copy(alpha = 0f),
                        )
                    }

                    currentCourse.spots.forEachIndexed { index, spot ->
                        if (spot.lat != 0.0 && spot.lng != 0.0) {
                            val icon = markerIcons[spot.id]
                            val isCurrent = index == uiState.currentSpotIndex
                            if (icon != null) {
                                Marker(
                                    state = rememberMarkerState(
                                        key = "spot_${spot.id}",
                                        position = LatLng(spot.lat, spot.lng),
                                    ),
                                    icon = icon,
                                    captionText = "${index + 1}. ${spot.name}",
                                    width = if (isCurrent) 56.dp else 48.dp,
                                    height = if (isCurrent) 56.dp else 48.dp,
                                    zIndex = if (isCurrent) 1 else 0,
                                )
                            } else {
                                Marker(
                                    state = rememberMarkerState(
                                        key = "spot_${spot.id}",
                                        position = LatLng(spot.lat, spot.lng),
                                    ),
                                    captionText = "${index + 1}. ${spot.name}",
                                )
                            }
                        }
                    }

                    // GPS user position: blue dot + pulsing ripple
                    if (userPosition != null) {
                        val userPulse = rememberInfiniteTransition(label = "userPulse")
                        val userRipple by userPulse.animateFloat(
                            initialValue = 8f,
                            targetValue = 30f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "userR",
                        )
                        val userAlpha by userPulse.animateFloat(
                            initialValue = 0.25f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "userA",
                        )
                        // Ripple circle
                        com.naver.maps.map.compose.CircleOverlay(
                            center = userPosition,
                            radius = userRipple.toDouble(),
                            color = Primary.copy(alpha = userAlpha),
                            outlineWidth = 0.dp,
                            outlineColor = Primary.copy(alpha = 0f),
                        )
                        // Blue dot center
                        com.naver.maps.map.compose.CircleOverlay(
                            center = userPosition,
                            radius = 5.0,
                            color = Primary,
                            outlineWidth = 2.dp,
                            outlineColor = BgWhite,
                        )
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                // Top bar: spot progress chips + close button
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val chipListState = androidx.compose.foundation.lazy.rememberLazyListState()
                    // Auto-scroll chips to current spot
                    LaunchedEffect(uiState.currentSpotIndex) {
                        // Each spot has chip + arrow = index * 2
                        val targetItem = (uiState.currentSpotIndex * 2).coerceAtMost(
                            (currentCourse.spots.size * 2 - 2).coerceAtLeast(0)
                        )
                        chipListState.animateScrollToItem(targetItem)
                    }
                    LazyRow(
                        state = chipListState,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        currentCourse.spots.forEachIndexed { index, spot ->
                            item(key = "chip_$index") {
                                SpotProgressChip(
                                    index = index + 1,
                                    name = spot.name,
                                    isActive = index == uiState.currentSpotIndex,
                                    isCompleted = index < uiState.currentSpotIndex,
                                    onClick = { viewModel.selectSpot(index) },
                                )
                            }
                            if (index < currentCourse.spots.lastIndex) {
                                item(key = "arrow_$index") {
                                    val arrowVisited = index < uiState.currentSpotIndex
                                    val isCurrentArrow = index == uiState.currentSpotIndex
                                    MovingArrow(
                                        isActive = isCurrentArrow,
                                        isVisited = arrowVisited,
                                    )
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BgWhite.copy(alpha = 0.95f))
                            .clickable { showStopDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp), tint = TextPrimary)
                    }
                }
            }
        }
    }

    // Story overlay (TTS)
    val currentStoryId = storyPlaceId
    if (currentStoryId != null) {
        com.ohmyguide.app.ui.screen.story.StoryOverlay(
            placeId = currentStoryId,
            onDismiss = { storyPlaceId = null },
        )
    }

    // Stop confirmation dialog
    val strings = LocalStrings.current
    if (showStopDialog) {
        ConfirmDialog(
            title = strings.endNaviTitle,
            message = strings.endNaviMessage,
            confirmText = strings.confirm,
            dismissText = strings.cancel,
            onConfirm = { navController.popBackStack() },
            onDismiss = { showStopDialog = false },
        )
    }

    // Mascot toast popup
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = toastSpotName != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 60.dp),
        ) {
            SpotAdvanceToast(spotName = toastSpotName ?: "")
        }
    }
}

@Composable
private fun CourseNaviSheetHeader(
    courseName: String,
    currentSpot: Spot,
    currentSpotIndex: Int,
    totalSpots: Int,
    progressPct: Float,
    onStory: () -> Unit,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(24.dp), tint = Primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSpot.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$courseName · ${currentSpotIndex + 1}/$totalSpots",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            // Story (TTS) button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryGradient)
                    .clickable(onClick = onStory)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Headphones, contentDescription = "Story", modifier = Modifier.size(14.dp), tint = BgWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Story",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
            }
            Spacer(modifier = Modifier.width(6.dp))

            // Stop button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = 0.1f))
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Stop", modifier = Modifier.size(18.dp), tint = Error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { progressPct },
                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Primary,
                trackColor = Border,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(progressPct * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(BorderLight),
    )
}

@Composable
private fun SpotGuideCard(
    spot: Spot,
    spotIndex: Int,
    totalSpots: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        // Spot image
        if (spot.imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgSub),
            ) {
                AsyncImage(
                    model = spot.imageUrl,
                    contentDescription = spot.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Spot number badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${spotIndex + 1}/$totalSpots",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
                // Listen badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PrimaryGradient)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Headphones, contentDescription = null, modifier = Modifier.size(12.dp), tint = BgWhite)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "듣기",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = spot.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        if (spot.nameKr.isNotBlank()) {
            Text(
                text = spot.nameKr,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        if (spot.desc.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spot.desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SpotProgressChip(
    index: Int,
    name: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                when {
                    isActive -> PrimaryGradient
                    isCompleted -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(PrimaryBg, PrimaryBg))
                    else -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(BgSub, BgSub))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isActive -> BgWhite
                        isCompleted -> Primary
                        else -> Border
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = when {
                    isActive -> Primary
                    isCompleted -> BgWhite
                    else -> TextCaption
                },
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isActive) BgWhite else if (isCompleted) Primary else TextCaption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SpotAdvanceToast(spotName: String) {
    Row(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.face),
            contentDescription = "Kkaebi",
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = LocalStrings.current.movingToNextCourse,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary,
            )
            Text(
                text = spotName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MovingArrow(isActive: Boolean, isVisited: Boolean) {
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "beacon")
        val rippleScale by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart,
            ),
            label = "rippleScale",
        )
        val rippleAlpha by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart,
            ),
            label = "rippleAlpha",
        )
        Box(contentAlignment = Alignment.Center) {
            // Ripple circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = rippleScale
                        scaleY = rippleScale
                        alpha = rippleAlpha
                    }
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.4f)),
            )
            // Arrow icon
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = BgWhite,
            )
        }
    } else {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isVisited) Primary else TextCaption,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CourseNaviScreenPreview() {
    OhMyGuideTheme {
        CourseNaviScreen(rememberNavController(), courseId = "demon-hunters")
    }
}
