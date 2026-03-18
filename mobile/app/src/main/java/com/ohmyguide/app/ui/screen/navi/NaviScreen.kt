package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ohmyguide.app.ui.common.PrimaryGradient
import com.ohmyguide.app.ui.screen.story.StoryOverlay
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

// 샘플 장소별 좌표
private val PLACE_COORDINATES = mapOf(
    "dm3" to LatLng(37.5700, 126.9990),  // 광장시장
    "dm4" to LatLng(37.5826, 126.9831),  // 북촌한옥마을
    "dm5" to LatLng(37.5512, 126.9882),  // 남산타워
    "dm6" to LatLng(37.5735, 126.9920),  // 익선동
    "dm7" to LatLng(37.5690, 126.9780),  // 청계천
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
                            .background(Color(0xFFD1D5DB)),
                    )
                }
            },
            sheetContent = {
                SheetContent(
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
            // ── Map area (전체 화면) ──
            MapArea(
                placeId = placeId,
                placeName = placeName,
                mode = mode,
                onBack = { navController.popBackStack() },
                onMinimize = onMinimize,
            )
        }

        // Story overlay
        if (showStory) {
            StoryOverlay(placeId = placeId, onDismiss = { showStory = false })
        }
    }
}

// ── Sheet content ──

@Composable
private fun SheetContent(
    placeName: String,
    placeNameKr: String,
    distance: String,
    eta: String,
    modeLabel: String,
    detail: com.ohmyguide.app.fixtures.PlaceDetail?,
    onStory: () -> Unit,
) {
    // ETA card
    EtaCard(placeName = placeName, distance = distance, eta = eta, modeLabel = modeLabel)

    // Location info
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = placeName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(text = placeNameKr, style = MaterialTheme.typography.labelSmall, color = TextCaption)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { 0.35f },
                modifier = Modifier.width(64.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Primary,
                trackColor = Border,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "35%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(BorderLight),
    )

    // POI + chat content
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        PoiHeroCard(detail?.place?.emoji ?: "\uD83C\uDFDE\uFE0F", placeName, placeNameKr)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(icon = Icons.Filled.AccessTime, iconTint = Color(0xFF7C3AED), value = detail?.hours ?: "09:00-18:00", modifier = Modifier.weight(1f))
            InfoChip(icon = Icons.Filled.AttachMoney, iconTint = Color(0xFF16A34A), value = detail?.fee ?: "Free", modifier = Modifier.weight(1f))
            InfoChip(icon = Icons.AutoMirrored.Filled.DirectionsWalk, iconTint = Color(0xFFE11D48), value = eta, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        GuideBubble(text = "I'll guide you to $placeName! Keep walking straight ahead.")
        GuideBubble(
            text = "While walking, did you know this place has been here for over 100 years? I'll tell you more when you arrive!",
            showAvatar = false,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Listen to Story button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryGradient)
                .clickable(onClick = onStory)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Headphones, contentDescription = null, modifier = Modifier.size(20.dp), tint = BgWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Listen to Story", style = MaterialTheme.typography.titleMedium, color = BgWhite)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ── Map area (전체 화면) ──

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
            // 경로선
            if (routeCoords != null && routeCoords.size >= 2) {
                PathOverlay(
                    coords = routeCoords,
                    width = 8.dp,
                    color = Primary,
                    outlineWidth = 2.dp,
                    outlineColor = Color(0xFF325BFF),
                )
            }

            // 출발 마커
            Marker(
                state = rememberMarkerState(position = DEFAULT_USER_POSITION),
                captionText = "You",
            )

            // 도착 마커
            Marker(
                state = rememberMarkerState(position = destinationPosition),
                captionText = placeName,
            )
        }

        // Back button (top-left)
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

        // Minimize button (top-right)
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

// ── ETA card ──

@Composable
private fun EtaCard(placeName: String, distance: String, eta: String, modeLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = modeLabel, style = MaterialTheme.typography.labelSmall, color = TextCaption)
            Text(text = placeName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = distance, style = MaterialTheme.typography.headlineSmall, color = Primary)
            Text(text = eta, style = MaterialTheme.typography.labelSmall, color = TextCaption)
        }
    }
}

// ── POI Hero card ──

@Composable
private fun PoiHeroCard(emoji: String, name: String, nameKr: String) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BgSub),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(BgSub),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 48.sp)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(text = nameKr, style = MaterialTheme.typography.labelMedium, color = TextCaption)
            }
        }
    }
}

// ── Info chip ──

@Composable
private fun InfoChip(icon: ImageVector, iconTint: Color, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(BgSub).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            maxLines = 1,
        )
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