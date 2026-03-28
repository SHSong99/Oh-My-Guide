package com.ohmyguide.app.ui.screen.explore

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.request.ImageRequest
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
import com.ohmyguide.app.R
import com.ohmyguide.app.service.TtsManager
import com.ohmyguide.app.ui.common.ConfirmDialog
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.common.buildCircleMarker
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.Error
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    var showStopDialog by remember { mutableStateOf(false) }

    // TTS
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var isMuted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // 큐 기반 가이드: 하나씩 꺼내서 TTS + 팝업
    val guideQueue = uiState.guideQueue
    val currentGuideText = guideQueue.firstOrNull()
    val isSpeakingTts by ttsManager.isSpeaking.collectAsState()

    var popupText by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }

    // 큐 처리: 하나씩 꺼내서 TTS + 팝업 → 끝나면 다음
    LaunchedEffect(currentGuideText) {
        if (currentGuideText == null) return@LaunchedEffect

        // 특수 명령은 바로 소비
        if (currentGuideText.startsWith("__")) {
            viewModel.dequeueGuide()
            delay(300L)
            return@LaunchedEffect
        }

        // 팝업 표시 + TTS 재생
        popupText = currentGuideText
        showPopup = true

        if (!isMuted) {
            ttsManager.speak(currentGuideText)

            // 다음 큐 프리페치 (현재 TTS 재생 중에 미리 다운로드)
            val nextText = guideQueue.getOrNull(1)
            if (nextText != null && !nextText.startsWith("__")) {
                scope.launch { ttsManager.prefetch(nextText) }
            }

            // TTS 시작을 최대 3초 대기
            var started = false
            for (i in 0 until 30) {
                delay(100L)
                if (ttsManager.isSpeaking.value) { started = true; break }
            }
            // TTS 시작되었으면 끝날 때까지 대기
            if (started) {
                while (ttsManager.isSpeaking.value || ttsManager.isLoading.value) {
                    delay(200L)
                }
            }
        } else {
            val readTimeMs = ((currentGuideText.length / 5.0) * 1000).toLong().coerceIn(3000L, 15000L)
            delay(readTimeMs)
        }

        // 팝업 닫기 → 다음 큐
        delay(500L)
        showPopup = false
        delay(300L)
        viewModel.dequeueGuide()
    }


    androidx.activity.compose.BackHandler { showStopDialog = true }

    // Image markers
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
        position = CameraPosition(LatLng(firstSpot.lat, firstSpot.lng), 15.0)
    }
    val locationSource = com.naver.maps.map.compose.rememberFusedLocationSource()
    val mapProperties = remember {
        MapProperties(locationTrackingMode = com.naver.maps.map.compose.LocationTrackingMode.NoFollow)
    }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = false)
    }

    // 스팟 변경 시 해당 스팟으로 카메라 이동
    LaunchedEffect(uiState.currentSpotIndex) {
        val spot = currentCourse.spots.getOrNull(uiState.currentSpotIndex) ?: return@LaunchedEffect
        cameraPositionState.animate(
            com.naver.maps.map.CameraUpdate.scrollAndZoomTo(LatLng(spot.lat, spot.lng), 16.0),
            animation = com.naver.maps.map.CameraAnimation.Fly,
            durationMs = 1500,
        )
    }

    val popupAlpha by animateFloatAsState(
        targetValue = if (showPopup) 1f else 0f,
        animationSpec = tween(600),
        label = "popupAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── 지도 (전체 화면) ──

        // ── Map (전체 화면) ──
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

                // GPS user position: SDK 파란 점으로 표시 (locationSource + NoFollow)
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }

        // ── 상단 네비바 (오버레이) ──
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Primary.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgWhite.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${uiState.currentSpotIndex + 1} / $totalSpots",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = currentSpot.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = BgWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(BgWhite.copy(alpha = 0.2f))
                    .clickable {
                        isMuted = !isMuted
                        if (isMuted) ttsManager.stop()
                        else if (popupText.isNotBlank()) scope.launch { ttsManager.speak(popupText) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = BgWhite,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(BgWhite.copy(alpha = 0.2f))
                    .clickable { showStopDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = BgWhite, modifier = Modifier.size(16.dp))
            }
        }

        // ── 깨비 팝업 카드 (오버레이) ──
        if (popupAlpha > 0f) {
            // 긴 텍스트는 문장 단위로 나눠서 TTS progress와 동기화
            val ttsProgress by ttsManager.progress.collectAsState()
            val popupSentences = remember(popupText) {
                popupText.split(Regex("(?<=[.!?。])(\\s*)")).filter { it.isNotBlank() }
                    .let { if (it.isEmpty()) listOf(popupText) else it }
            }
            val displaySentence = if (popupSentences.size <= 1) {
                popupText
            } else {
                val totalChars = popupSentences.sumOf { it.length }
                val cumWeights = popupSentences.runningFold(0f) { acc, s -> acc + s.length.toFloat() / totalChars }.drop(1)
                val idx = cumWeights.indexOfFirst { it > ttsProgress }.let { if (it == -1) popupSentences.lastIndex else it }
                popupSentences[idx]
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 24.dp, end = 24.dp, top = 70.dp)
                    .graphicsLayer { alpha = popupAlpha }
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite.copy(alpha = 0.95f))
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Image(
                        painter = painterResource(R.drawable.masot),
                        contentDescription = "Kkaebi",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSpot.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Primary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedContent(
                            targetState = displaySentence,
                            transitionSpec = {
                                (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 })
                                    .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 })
                            },
                            label = "popupSentence",
                        ) { sentence ->
                            Text(
                                text = sentence,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }

    // Stop confirmation dialog
    val strings = LocalStrings.current
    if (showStopDialog) {
        ConfirmDialog(
            title = strings.endNaviTitle,
            message = strings.endNaviMessage,
            confirmText = strings.confirm,
            dismissText = strings.cancel,
            onConfirm = {
                ttsManager.shutdown()
                navController.popBackStack()
            },
            onDismiss = { showStopDialog = false },
        )
    }

    // 투어 완료 오버레이 (대시보드 스타일)
    if (uiState.tourCompleted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "투어가 끝났어요!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "이번 가이드는 어떠셨나요?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BgWhite.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2A3E))
                            .clickable {
                                ttsManager.shutdown()
                                navController.popBackStack(Screen.Explore.route, inclusive = false)
                            }
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_feedback_good),
                            contentDescription = "Good",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "좋았어요",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BgWhite,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2A3E))
                            .clickable {
                                ttsManager.shutdown()
                                navController.popBackStack(Screen.Explore.route, inclusive = false)
                            }
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_feedback_bad),
                            contentDescription = "Bad",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "아쉬웠어요",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BgWhite.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
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
