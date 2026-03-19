package com.ohmyguide.app.ui.screen.transport

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.common.OmgButton
import com.ohmyguide.app.ui.common.OmgTopBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.InfoPurple
import com.ohmyguide.app.ui.theme.MenuStoryBg
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun TransportPickerScreen(navController: NavController, placeId: String) {
    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Destination"
    val placeNameKr = detail?.place?.nameKr ?: ""

    var selectedMode by remember { mutableStateOf(TransportMode.Walk) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
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

            if (selectedMode == TransportMode.Transit) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MenuStoryBg)
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp), tint = InfoPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap below to see available routes",
                            style = MaterialTheme.typography.labelMedium,
                            color = InfoPurple,
                        )
                    }
                }
            }
        }

        OmgButton(
            text = if (selectedMode == TransportMode.Transit) "View Transit Routes"
            else "Start Navigation",
            onClick = {
                if (selectedMode == TransportMode.Transit) {
                    navController.navigate(Screen.TransitDetail.createRoute(placeId))
                } else {
                    navController.navigate(
                        Screen.Navi.createRoute(placeId, selectedMode.name.lowercase())
                    )
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TransportPickerScreenPreview() {
    OhMyGuideTheme {
        TransportPickerScreen(rememberNavController(), placeId = "dm3")
    }
}
