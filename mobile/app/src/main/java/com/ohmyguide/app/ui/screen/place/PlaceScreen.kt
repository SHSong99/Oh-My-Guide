package com.ohmyguide.app.ui.screen.place

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.common.InfoCard
import com.ohmyguide.app.ui.common.OmgButton
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.InfoBlueBg
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.InfoGreenBg
import com.ohmyguide.app.ui.theme.InfoPurple
import com.ohmyguide.app.ui.theme.InfoPurpleBg
import com.ohmyguide.app.ui.theme.InfoRose
import com.ohmyguide.app.ui.theme.InfoRoseBg
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

sealed class PlaceUiState {
    object Loading : PlaceUiState()
    object Idle : PlaceUiState()
    data class Error(val message: String) : PlaceUiState()
}

@Composable
fun PlaceScreen(navController: NavController, placeId: String) {
    val strings = LocalStrings.current
    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
        ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            HeroSection(detail = detail)

            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Star)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${detail.place.rating}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Star,
                        )
                    }
                    Text(
                        text = detail.place.distance,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextCaption,
                    )
                    Text(
                        text = detail.place.tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = detail.place.color,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(detail.place.color.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = detail.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
            }
        }

        BottomButtons(
            onNo = { navController.popBackStack() },
            onGo = { navController.navigate(Screen.Transport.createRoute(placeId)) },
        )
    }
}

@Composable
private fun HeroSection(detail: PlaceDetail) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(BgSub),
    ) {
        if (detail.place.imageUrl != null) {
            coil.compose.AsyncImage(
                model = detail.place.imageUrl,
                contentDescription = detail.place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = detail.place.emoji.ifEmpty { "\uD83D\uDCCD" },
                    fontSize = 48.sp,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.5f)),
                    )
                ),
        )
        Text(
            text = detail.place.name,
            style = MaterialTheme.typography.headlineMedium,
            color = BgWhite,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        )
    }
}

@Composable
private fun BottomButtons(onNo: () -> Unit, onGo: () -> Unit) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSub)
                .clickable(onClick = onNo)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.placeNo,
                style = MaterialTheme.typography.titleMedium,
                color = TextCaption,
            )
        }
        OmgButton(
            text = strings.placeGo,
            onClick = onGo,
            modifier = Modifier.weight(2f),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PlaceScreenPreview() {
    OhMyGuideTheme {
        PlaceScreen(rememberNavController(), placeId = "dm3")
    }
}
