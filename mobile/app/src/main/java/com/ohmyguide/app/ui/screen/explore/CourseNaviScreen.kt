package com.ohmyguide.app.ui.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberMarkerState
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.fixtures.Spot
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.DragHandle
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

// Mock coordinates for course spots
private val SPOT_COORDINATES = listOf(
    LatLng(37.5580, 126.9269),
    LatLng(37.5563, 126.9236),
    LatLng(37.5550, 126.9300),
    LatLng(37.5535, 126.9340),
    LatLng(37.5520, 126.9380),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun CourseNaviScreen(
    navController: NavController,
    courseId: String,
) {
    val course = EXPLORE_COURSES.find { it.id == courseId }
        ?: EXPLORE_COURSES.firstOrNull()
        ?: return

    var currentSpotIndex by remember { mutableIntStateOf(0) }
    val currentSpot = course.spots.getOrNull(currentSpotIndex) ?: return
    val totalSpots = course.spots.size
    val progressPct = (currentSpotIndex + 1).toFloat() / totalSpots

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            SPOT_COORDINATES.getOrElse(0) { LatLng(37.5560, 126.9270) },
            14.0,
        )
    }

    val mapProperties = remember { MapProperties() }
    val mapUiSettings = remember {
        MapUiSettings(isZoomControlEnabled = false, isLocationButtonEnabled = false)
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 320.dp,
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
                CourseNaviSheetContent(
                    course = course,
                    currentSpot = currentSpot,
                    currentSpotIndex = currentSpotIndex,
                    progressPct = progressPct,
                    onSpotTap = { index -> currentSpotIndex = index },
                    onNext = {
                        if (currentSpotIndex < totalSpots - 1) currentSpotIndex++
                    },
                )
            },
        ) {
            // Map with course markers
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
                    course.spots.forEachIndexed { index, spot ->
                        val coord = SPOT_COORDINATES.getOrElse(index) {
                            LatLng(37.556 + index * 0.002, 126.927 + index * 0.003)
                        }
                        Marker(
                            state = rememberMarkerState(position = coord),
                            captionText = "${index + 1}. ${spot.name}",
                        )
                    }
                }

                // Top bar overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Distance badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(BgWhite.copy(alpha = 0.95f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = course.duration,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Primary,
                        )
                        Text(
                            text = " · ${course.spotCount} ${LocalStrings.current.spots}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextCaption,
                        )
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BgWhite.copy(alpha = 0.95f))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp), tint = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseNaviSheetContent(
    course: Course,
    currentSpot: Spot,
    currentSpotIndex: Int,
    progressPct: Float,
    onSpotTap: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // Current spot header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = currentSpot.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(text = currentSpot.nameKr, style = MaterialTheme.typography.labelSmall, color = TextCaption)
            }
            LinearProgressIndicator(
                progress = { progressPct },
                modifier = Modifier.width(48.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Primary,
                trackColor = Border,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${(progressPct * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }

        // Spot hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSub),
        ) {
            // Image placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "📍", fontSize = 36.sp)
            }
            // Name overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        )
                    )
                    .padding(14.dp),
            ) {
                Text(
                    text = currentSpot.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
                Text(
                    text = currentSpot.nameKr,
                    style = MaterialTheme.typography.labelSmall,
                    color = BgWhite.copy(alpha = 0.8f),
                )
            }
            // "Right here" badge
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(BgWhite.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = LocalStrings.current.rightHere, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            }
        }

        // Description
        Text(
            text = currentSpot.desc,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Course progress section
        Text(
            text = LocalStrings.current.courseProgress,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            ),
            color = TextCaption,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        ) {
            itemsIndexed(course.spots) { index, spot ->
                SpotProgressChip(
                    index = index + 1,
                    name = spot.name,
                    isActive = index == currentSpotIndex,
                    isCompleted = index < currentSpotIndex,
                    onClick = { onSpotTap(index) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SpotProgressChip(
    index: Int,
    name: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                when {
                    isActive -> PrimaryGradient
                    isCompleted -> Brush.linearGradient(listOf(PrimaryBg, PrimaryBg))
                    else -> Brush.linearGradient(listOf(BgSub, BgSub))
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CourseNaviScreenPreview() {
    OhMyGuideTheme {
        CourseNaviScreen(rememberNavController(), courseId = "demon-hunters")
    }
}
