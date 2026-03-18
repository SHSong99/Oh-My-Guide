package com.ohmyguide.app.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

sealed class MapUiState {
    object Loading : MapUiState()
    object Idle : MapUiState()
    data class Error(val message: String) : MapUiState()
}

// 서울 종로 기본 좌표
private val DEFAULT_POSITION = LatLng(37.5700, 126.9920)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(navController: NavController) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(DEFAULT_POSITION, 15.0)
    }

    val mapProperties = remember {
        MapProperties(
            locationTrackingMode = LocationTrackingMode.NoFollow,
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = true,
            isLocationButtonEnabled = true,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Naver Map
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
        )

        // Location badge (top center)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(BgWhite.copy(alpha = 0.95f))
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Jongno",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
            }
        }

        // Info card (bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(BgWhite.copy(alpha = 0.95f))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(22.dp), tint = Primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Map View",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Explore places around you",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextCaption,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapScreenPreview() {
    OhMyGuideTheme {
        MapScreen(rememberNavController())
    }
}
