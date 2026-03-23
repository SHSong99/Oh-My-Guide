package com.ohmyguide.app.ui.screen.home

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.location.Geocoder
import java.util.Locale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.compose.rememberMarkerState
import com.naver.maps.map.overlay.OverlayImage
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.service.LocationForegroundService
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val DEFAULT_POSITION = LatLng(37.5700, 126.9920)

private enum class SheetAnchor { COLLAPSED, HALF, EXPANDED }

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val locationData by LocationForegroundService.locationFlow.collectAsState()
    val locationSource = rememberFusedLocationSource()
    var locationName by remember { mutableStateOf("") }
    val strings = LocalStrings.current

    // GPS → English address
    LaunchedEffect(locationData) {
        if (locationName.isNotEmpty()) return@LaunchedEffect
        val loc = locationData ?: return@LaunchedEffect
        try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val district = address.subLocality ?: address.locality ?: ""
                val city = address.adminArea ?: ""
                locationName = if (district.isNotEmpty() && city.isNotEmpty()) "$district, $city"
                else city.ifEmpty { strings.yourArea }
            }
        } catch (_: Exception) {
            locationName = strings.yourArea
        }
    }

    val initialPosition = locationData?.let { LatLng(it.latitude, it.longitude) }
        ?: DEFAULT_POSITION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(initialPosition, 14.0)
    }

    LaunchedEffect(locationData) {
        val loc = locationData ?: return@LaunchedEffect
        if (cameraPositionState.position.target == DEFAULT_POSITION) {
            cameraPositionState.animate(
                CameraUpdate.scrollAndZoomTo(LatLng(loc.latitude, loc.longitude), 14.0),
            )
        }
    }

    val mapProperties = remember {
        MapProperties(locationTrackingMode = LocationTrackingMode.NoFollow)
    }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = true)
    }

    // Marker data
    val markerPlaces = remember(state.chatMessages) {
        state.chatMessages
            .filterIsInstance<ChatMessage.BotRecommendation>()
            .flatMap { it.section.places }
            .filter { it.lat != 0.0 && it.lng != 0.0 }
            .distinctBy { it.id }
    }

    val density = LocalDensity.current
    val markerSizeDp = 44.dp
    val selectedMarkerSizeDp = 56.dp
    val markerSizePx = with(density) { markerSizeDp.toPx().toInt() }
    val selectedMarkerSizePx = with(density) { selectedMarkerSizeDp.toPx().toInt() }
    val borderPx = with(density) { 3.dp.toPx() }
    val selectedBorderPx = with(density) { 4.dp.toPx() }
    val markerIcons = remember { mutableStateMapOf<String, OverlayImage>() }
    val selectedMarkerIcons = remember { mutableStateMapOf<String, OverlayImage>() }

    LaunchedEffect(markerPlaces) {
        markerPlaces.forEach { place ->
            if (place.imageUrl != null && place.id !in markerIcons) {
                val request = ImageRequest.Builder(context)
                    .data(place.imageUrl)
                    .size(selectedMarkerSizePx)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val srcBitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                    markerIcons[place.id] = buildCircleMarker(srcBitmap, markerSizePx, borderPx, android.graphics.Color.WHITE)
                    selectedMarkerIcons[place.id] = buildCircleMarker(srcBitmap, selectedMarkerSizePx, selectedBorderPx, 0xFF5478FF.toInt())
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        HomeHeader(
            onReset = {
                navController.navigate(Screen.InterestSelect.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
        )

        // Main content area with 3-stage sheet
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val maxHeightPx = with(density) { maxHeight.toPx() }
            val collapsedHeightDp = 72.dp
            val collapsedHeightPx = with(density) { collapsedHeightDp.toPx() }

            // Anchor positions (Y offset from top)
            val expandedY = 0f
            val halfY = maxHeightPx * 0.5f
            val collapsedY = maxHeightPx - collapsedHeightPx

            val scope = rememberCoroutineScope()
            val sheetOffsetY = remember { Animatable(halfY) }

            val currentAnchor = remember(sheetOffsetY.value) {
                val snapPoints = listOf(expandedY, halfY, collapsedY)
                val nearest = snapPoints.minByOrNull { abs(it - sheetOffsetY.value) } ?: halfY
                when (nearest) {
                    expandedY -> SheetAnchor.EXPANDED
                    collapsedY -> SheetAnchor.COLLAPSED
                    else -> SheetAnchor.HALF
                }
            }

            val dragState = rememberDraggableState { delta ->
                scope.launch {
                    val newY = (sheetOffsetY.value + delta).coerceIn(expandedY, collapsedY)
                    sheetOffsetY.snapTo(newY)
                }
            }

            fun snapToNearest() {
                val snapPoints = listOf(expandedY, halfY, collapsedY)
                val target = snapPoints.minByOrNull { abs(it - sheetOffsetY.value) } ?: halfY
                scope.launch { sheetOffsetY.animateTo(target, tween(300)) }
            }

            // When place selected → expand sheet & move camera
            LaunchedEffect(state.selectedDetail) {
                val detail = state.selectedDetail
                if (detail != null) {
                    sheetOffsetY.animateTo(expandedY, tween(300))
                    val place = detail.place
                    if (place.lat != 0.0 && place.lng != 0.0) {
                        cameraPositionState.animate(
                            CameraUpdate.scrollAndZoomTo(LatLng(place.lat, place.lng), 16.0),
                        )
                    }
                }
            }

            // When sheet collapses from PLACE_DETAIL → clear selection
            LaunchedEffect(currentAnchor) {
                if (currentAnchor != SheetAnchor.EXPANDED && state.sheetMode == SheetMode.PLACE_DETAIL) {
                    viewModel.clearSelection()
                }
            }

            // Map layer
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
                    val selectedId = state.selectedDetail?.place?.id
                    markerPlaces.forEach { place ->
                        val isSelected = place.id == selectedId
                        val icon = if (isSelected) selectedMarkerIcons[place.id] else markerIcons[place.id]
                        if (icon != null) {
                            Marker(
                                state = rememberMarkerState(
                                    key = place.id,
                                    position = LatLng(place.lat, place.lng),
                                ),
                                captionText = place.name,
                                icon = icon,
                                width = if (isSelected) selectedMarkerSizeDp else markerSizeDp,
                                height = if (isSelected) selectedMarkerSizeDp else markerSizeDp,
                                zIndex = if (isSelected) 1 else 0,
                                onClick = {
                                    viewModel.selectPlace(place.id)
                                    true
                                },
                            )
                        }
                    }
                }
            }

            // Sheet layer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                    .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(BgWhite),
            ) {
                // ── Drag area: handle + LocationBar ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            onDragStopped = { snapToNearest() },
                        ),
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
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

                    when (state.sheetMode) {
                        SheetMode.RECOMMENDATIONS -> {
                            LocationBar(
                                spotCount = state.spotCount,
                                locationName = locationName,
                            )
                        }
                        SheetMode.PLACE_DETAIL -> {}
                    }
                }

                // ── Content area ──
                val isExpanded = currentAnchor == SheetAnchor.EXPANDED
                val contentScrollState = rememberScrollState()

                // Auto-scroll when messages change (recommendations mode)
                LaunchedEffect(state.chatMessages.size) {
                    if (state.sheetMode == SheetMode.RECOMMENDATIONS && isExpanded) {
                        contentScrollState.animateScrollTo(contentScrollState.maxValue)
                    }
                }

                when (state.sheetMode) {
                    SheetMode.RECOMMENDATIONS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .then(
                                    if (isExpanded) Modifier.verticalScroll(contentScrollState)
                                    else Modifier
                                )
                                .padding(bottom = 12.dp),
                        ) {
                            state.chatMessages.forEachIndexed { index, msg ->
                                if (msg is ChatMessage.FindOtherPlacesBtn) return@forEachIndexed
                                val msgModifier = Modifier.padding(horizontal = 16.dp)
                                when (msg) {
                                    is ChatMessage.BotText -> {
                                        GuideBubble(
                                            text = msg.text,
                                            modifier = msgModifier.padding(vertical = 4.dp),
                                            showAvatar = index == 0 ||
                                                state.chatMessages.getOrNull(index - 1) !is ChatMessage.BotText,
                                        )
                                    }
                                    is ChatMessage.BotTyping -> {
                                        TypingIndicator(
                                            modifier = msgModifier.padding(vertical = 4.dp),
                                            showAvatar = state.chatMessages.getOrNull(index - 1)
                                                .let { it !is ChatMessage.BotText && it !is ChatMessage.BotRecommendation },
                                        )
                                    }
                                    is ChatMessage.UserText -> {
                                        UserBubble(
                                            text = msg.text,
                                            modifier = msgModifier.padding(vertical = 4.dp),
                                        )
                                    }
                                    is ChatMessage.BotRecommendation -> {
                                        RecommendationBlock(
                                            section = msg.section,
                                            onPlaceClick = { placeId -> viewModel.selectPlace(placeId) },
                                            onShowMore = if (msg.section.btnText.isNotEmpty()) {
                                                { viewModel.onShowMore(msg.section.title) }
                                            } else null,
                                        )
                                    }
                                    is ChatMessage.BotOptions -> {
                                        ChatOptionButtons(
                                            options = msg.options,
                                            answered = msg.answered,
                                            selectedOption = msg.selectedOption,
                                            onSelect = { option -> viewModel.onSelectOption(option) },
                                            modifier = msgModifier.padding(vertical = 4.dp),
                                        )
                                    }
                                    is ChatMessage.UserInput -> {
                                        ChatTextInput(
                                            onSubmit = msg.onSubmit,
                                            modifier = msgModifier.padding(vertical = 4.dp),
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }

                        // Find other places button — fixed at bottom, hidden in COLLAPSED
                        if (currentAnchor != SheetAnchor.COLLAPSED) {
                            val showFindBtn = state.chatMessages.any { it is ChatMessage.FindOtherPlacesBtn }
                            if (showFindBtn) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgWhite)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    FindOtherPlacesButton(onClick = { viewModel.onFindOtherPlaces() })
                                }
                            }
                        }
                    }
                    SheetMode.PLACE_DETAIL -> {
                        state.selectedDetail?.let { detail ->
                            PlaceDetailSheet(
                                detail = detail,
                                onBack = { viewModel.clearSelection() },
                                onGoHere = { placeId ->
                                    navController.navigate(Screen.Transport.createRoute(placeId))
                                },
                            )
                        }
                    }
                }
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

private fun buildCircleMarker(srcBitmap: Bitmap, sizePx: Int, borderWidth: Float, borderColor: Int): OverlayImage {
    val scaled = Bitmap.createScaledBitmap(srcBitmap, sizePx, sizePx, true)
    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = sizePx / 2f
    paint.shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    canvas.drawCircle(r, r, r - borderWidth, paint)
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderWidth
    paint.color = borderColor
    canvas.drawCircle(r, r, r - borderWidth / 2, paint)
    return OverlayImage.fromBitmap(output)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    OhMyGuideTheme {
        HomeScreen(rememberNavController())
    }
}
