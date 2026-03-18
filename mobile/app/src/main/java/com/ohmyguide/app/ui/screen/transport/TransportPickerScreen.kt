package com.ohmyguide.app.ui.screen.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.common.PrimaryGradient
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

private enum class TransportMode(
    val label: String,
    val icon: ImageVector,
    val desc: String,
    val time: String,
    val eta: String,
    val color: Color,
) {
    Walk("Walk", Icons.AutoMirrored.Filled.DirectionsWalk, "Enjoy the scenery", "5 min", "ETA 9:46 AM", Color(0xFF16A34A)),
    Transit("Transit", Icons.Filled.DirectionsBus, "Bus & Subway", "12 min", "ETA 9:53 AM", Color(0xFF7C3AED)),
    Taxi("Taxi", Icons.Filled.LocalTaxi, "Taxi / Car", "3 min", "ETA 9:44 AM", Color(0xFFE11D48)),
}

@Composable
fun TransportPickerScreen(navController: NavController, placeId: String) {
    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Destination"
    val placeNameKr = detail?.place?.nameKr ?: ""

    var selectedMode by remember { mutableStateOf(TransportMode.Walk) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSub)
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary)
            }
            Text(
                text = "Choose Transport",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ── Destination info ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSub)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail?.place?.emoji ?: "\uD83D\uDCCD",
                        fontSize = 22.sp,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = placeName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = placeNameKr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${detail?.place?.distance ?: "350m"} \u00B7 ${detail?.walkTime ?: "5 min walk"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Transport mode cards ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TransportMode.entries.forEach { mode ->
                TransportModeCard(
                    mode = mode,
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                )
            }

            // Transit hint
            if (selectedMode == TransportMode.Transit) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F3FF))
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF7C3AED))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap below to see available routes",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF7C3AED),
                        )
                    }
                }
            }
        }

        // ── CTA Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGradient)
                    .clickable {
                        if (selectedMode == TransportMode.Transit) {
                            navController.navigate(Screen.TransitDetail.createRoute(placeId))
                        } else {
                            navController.navigate(
                                Screen.Navi.createRoute(placeId, selectedMode.name.lowercase())
                            )
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (selectedMode == TransportMode.Transit) "View Transit Routes"
                    else "Start Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    color = BgWhite,
                )
            }
        }
    }
}

@Composable
private fun TransportModeCard(
    mode: TransportMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Primary else Border
    val bgColor = if (selected) PrimaryBg else BgWhite

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (selected) 4.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(mode.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(mode.icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = mode.color)
        }
        Spacer(modifier = Modifier.width(14.dp))

        // Label & description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = mode.desc,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }

        // Time & ETA
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = mode.time,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = mode.color,
            )
            Text(
                text = mode.eta,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Radio indicator
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) Primary else Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TransportPickerScreenPreview() {
    OhMyGuideTheme {
        TransportPickerScreen(rememberNavController(), placeId = "dm3")
    }
}