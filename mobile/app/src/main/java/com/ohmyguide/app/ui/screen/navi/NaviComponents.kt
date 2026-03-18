package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.InfoCard
import com.ohmyguide.app.ui.common.OmgButton
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.InfoPurple
import com.ohmyguide.app.ui.theme.InfoRose
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

@Composable
fun EtaCard(placeName: String, distance: String, eta: String, modeLabel: String) {
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

@Composable
fun PoiHeroCard(emoji: String, name: String, nameKr: String) {
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

@Composable
fun NaviSheetContent(
    placeName: String,
    placeNameKr: String,
    distance: String,
    eta: String,
    modeLabel: String,
    detail: PlaceDetail?,
    onStory: () -> Unit,
) {
    EtaCard(placeName = placeName, distance = distance, eta = eta, modeLabel = modeLabel)

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

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        PoiHeroCard(detail?.place?.emoji ?: "\uD83C\uDFDE\uFE0F", placeName, placeNameKr)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCard(icon = Icons.Filled.AccessTime, iconTint = InfoPurple, value = detail?.hours ?: "09:00-18:00", modifier = Modifier.weight(1f))
            InfoCard(icon = Icons.Filled.AttachMoney, iconTint = InfoGreen, value = detail?.fee ?: "Free", modifier = Modifier.weight(1f))
            InfoCard(icon = Icons.AutoMirrored.Filled.DirectionsWalk, iconTint = InfoRose, value = eta, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        GuideBubble(text = "I'll guide you to $placeName! Keep walking straight ahead.")
        GuideBubble(
            text = "While walking, did you know this place has been here for over 100 years? I'll tell you more when you arrive!",
            showAvatar = false,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OmgButton(
            text = "Listen to Story",
            onClick = onStory,
            icon = Icons.Filled.Headphones,
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}
