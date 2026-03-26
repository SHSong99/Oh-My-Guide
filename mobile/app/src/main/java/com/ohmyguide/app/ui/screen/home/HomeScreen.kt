package com.ohmyguide.app.ui.screen.home

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.location.Geocoder
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.ohmyguide.app.ui.common.buildCircleMarker
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

private val DEFAULT_POSITION = LatLng(37.5700, 126.9920)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    category: String = "",
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // 초기 추천 로드
    LaunchedEffect(Unit) {
        viewModel.loadInitialRecommendation(category)
    }

    val context = LocalContext.current
    val locationData by LocationForegroundService.locationFlow.collectAsState()
    val locationSource = rememberFusedLocationSource()
    var locationName by remember { mutableStateOf("") }
    val strings = LocalStrings.current

    // GPS 좌표 → 영어 주소 변환
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

    // GPS 위치를 화면 상단 절반의 중심에 배치하기 위해 약간 남쪽으로 오프셋
    val offsetLat = 0.025 // 줌 11 기준으로 바텀시트 절반 보정
    val initialPosition = locationData?.let {
        LatLng(it.latitude - offsetLat, it.longitude)
    } ?: DEFAULT_POSITION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(initialPosition, 11.0)
    }

    // 첫 GPS 위치가 들어오면 카메라를 보정된 위치로 이동
    LaunchedEffect(locationData) {
        val loc = locationData ?: return@LaunchedEffect
        if (cameraPositionState.position.target == DEFAULT_POSITION) {
            cameraPositionState.animate(
                CameraUpdate.scrollAndZoomTo(
                    LatLng(loc.latitude - offsetLat, loc.longitude), 11.0
                ),
            )
        }
    }

    val mapProperties = remember {
        MapProperties(
            locationTrackingMode = LocationTrackingMode.NoFollow,
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = false,
            isLocationButtonEnabled = true,
        )
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
    )

    // Collect all recommended places for map markers
    val markerPlaces = remember(state.chatMessages) {
        state.chatMessages
            .filterIsInstance<ChatMessage.BotRecommendation>()
            .flatMap { it.section.places }
            .filter { it.lat != 0.0 && it.lng != 0.0 }
            .distinctBy { it.id }
    }

    // Load place images as circular marker icons (normal + selected)
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
                    // Normal marker (white border)
                    markerIcons[place.id] = buildCircleMarker(srcBitmap, markerSizePx, borderPx, android.graphics.Color.WHITE)
                    // Selected marker (blue border, larger)
                    selectedMarkerIcons[place.id] = buildCircleMarker(srcBitmap, selectedMarkerSizePx, selectedBorderPx, 0xFF5478FF.toInt())
                }
            }
        }
    }

    // When place selected → expand sheet & move camera
    LaunchedEffect(state.selectedDetail) {
        val detail = state.selectedDetail
        if (detail != null) {
            sheetState.expand()
            val place = detail.place
            if (place.lat != 0.0 && place.lng != 0.0) {
                cameraPositionState.animate(
                    CameraUpdate.scrollAndZoomTo(LatLng(place.lat, place.lng), 16.0),
                )
            }
        }
    }

    // When sheet collapses → clear selection
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }.collect { value ->
            if (value == SheetValue.PartiallyExpanded && state.sheetMode == SheetMode.PLACE_DETAIL) {
                viewModel.clearSelection()
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

        Box(modifier = Modifier.weight(1f)) {
            val showFindBtn = state.sheetMode == SheetMode.RECOMMENDATIONS &&
                state.chatMessages.any { it is ChatMessage.FindOtherPlacesBtn }

            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 360.dp,
                sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                sheetContainerColor = BgWhite,
                sheetDragHandle = {
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
                },
                sheetContent = {
                    when (state.sheetMode) {
                        SheetMode.RECOMMENDATIONS -> RecommendationsSheet(
                            state = state,
                            locationName = locationName,
                            showFindBtn = showFindBtn,
                            onPlaceClick = { placeId -> viewModel.selectPlace(placeId) },
                            onShowMore = { title -> viewModel.onShowMore(title) },
                            onSelectOption = { option -> viewModel.onSelectOption(option) },
                            onFindOtherPlaces = { viewModel.onFindOtherPlaces() },
                        )
                        SheetMode.PLACE_DETAIL -> {
                            state.selectedDetail?.let { detail ->
                                PlaceDetailSheet(
                                    detail = detail,
                                    onBack = { viewModel.clearSelection() },
                                    onGoHere = { placeId ->
                                        viewModel.startGuide(placeId)
                                        navController.navigate(Screen.Transport.createRoute(placeId))
                                    },
                                )
                            }
                        }
                    }
                },
            ) {
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
            }

            // Find other places button removed from here — moved into RecommendationsSheet
        }

        BottomNavBar(
            activeTab = "main",
            onTabChange = { tab ->
                when (tab) {
                    "explore" -> navController.navigate(Screen.Explore.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    "phrases" -> navController.navigate(Screen.Phrases.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
}

@Composable
private fun RecommendationsSheet(
    state: HomeUiState,
    locationName: String,
    showFindBtn: Boolean,
    onPlaceClick: (String) -> Unit,
    onShowMore: (String) -> Unit,
    onSelectOption: (String) -> Unit,
    onFindOtherPlaces: () -> Unit,
) {
    val scrollState = rememberScrollState()

    // Auto-scroll when messages change
    LaunchedEffect(state.chatMessages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 12.dp),
    ) {
        LocationBar(spotCount = state.spotCount, locationName = locationName)

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
                        onPlaceClick = onPlaceClick,
                        onShowMore = if (msg.section.btnText.isNotEmpty()) {
                            { onShowMore(msg.section.title) }
                        } else null,
                    )
                }
                is ChatMessage.BotOptions -> {
                    ChatOptionButtons(
                        options = msg.options,
                        answered = msg.answered,
                        selectedOption = msg.selectedOption,
                        onSelect = onSelectOption,
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

        if (showFindBtn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                FindOtherPlacesButton(onClick = onFindOtherPlaces)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    OhMyGuideTheme {
        HomeScreen(rememberNavController())
    }
}
