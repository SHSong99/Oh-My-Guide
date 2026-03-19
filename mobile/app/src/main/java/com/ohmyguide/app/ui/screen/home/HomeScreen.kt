package com.ohmyguide.app.ui.screen.home

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

private val PLACE_MARKERS = mapOf(
    "dm3" to ("Gwangjang Market" to LatLng(37.5700, 126.9990)),
    "dm4" to ("Bukchon Hanok Village" to LatLng(37.5826, 126.9831)),
    "dm5" to ("Namsan Tower" to LatLng(37.5512, 126.9882)),
    "dm6" to ("Ikseon-dong" to LatLng(37.5735, 126.9920)),
    "dm7" to ("Cheonggyecheon Stream" to LatLng(37.5690, 126.9780)),
)

private val DEFAULT_POSITION = LatLng(37.5700, 126.9920)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(DEFAULT_POSITION, 14.0)
    }

    val mapProperties = remember { MapProperties() }
    val mapUiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = false,
            isLocationButtonEnabled = false,
        )
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
    )

    // When place selected → expand sheet & move camera
    LaunchedEffect(state.selectedDetail) {
        val detail = state.selectedDetail
        if (detail != null) {
            sheetState.expand()
            val coord = PLACE_MARKERS[detail.place.id]?.second
            if (coord != null) {
                cameraPositionState.animate(
                    CameraUpdate.scrollAndZoomTo(coord, 16.0),
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
                            onPlaceClick = { placeId -> viewModel.selectPlace(placeId) },
                            onShowMore = { title -> viewModel.onShowMore(title) },
                            onSelectOption = { option -> viewModel.onSelectOption(option) },
                        )
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
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NaverMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = mapUiSettings,
                    ) {
                        PLACE_MARKERS.forEach { (_, pair) ->
                            val (name, position) = pair
                            Marker(
                                state = rememberMarkerState(position = position),
                                captionText = name,
                            )
                        }
                    }
                }
            }

            // Find other places — floating at bottom of Box, above sheet
            if (showFindBtn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(BgWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    FindOtherPlacesButton(onClick = { viewModel.onFindOtherPlaces() })
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

@Composable
private fun RecommendationsSheet(
    state: HomeUiState,
    onPlaceClick: (String) -> Unit,
    onShowMore: (String) -> Unit,
    onSelectOption: (String) -> Unit,
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
        LocationBar(spotCount = state.spotCount)

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
                else -> {}
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
