package com.ohmyguide.app.ui.screen.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.common.RadioIndicator
import com.ohmyguide.app.ui.theme.AppStrings
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.InfoPurple
import com.ohmyguide.app.ui.theme.InfoRose
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

internal enum class TransportMode(
    val labelKey: (AppStrings) -> String,
    val icon: ImageVector,
    val descKey: (AppStrings) -> String,
    val color: Color,
) {
    Walk({ it.walk }, Icons.AutoMirrored.Filled.DirectionsWalk, { it.enjoyScenery }, InfoGreen),
    Transit({ it.transit }, Icons.Filled.DirectionsBus, { it.busAndSubway }, InfoPurple),
    Car({ it.taxi }, Icons.Filled.LocalTaxi, { it.taxiCar }, InfoRose),
}

@Composable
internal fun TransportModeCard(
    mode: TransportMode,
    selected: Boolean,
    time: String,
    eta: String,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Primary else Border
    val bgColor = if (selected) PrimaryBg else BgWhite
    val borderWidth = if (selected) 2.dp else 1.dp

    val strings = LocalStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (selected) 6.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (selected) mode.color.copy(alpha = 0.2f) else mode.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                mode.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) mode.color else TextCaption,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.labelKey(strings),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (selected) Primary else TextPrimary,
            )
            Text(
                text = mode.descKey(strings),
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = time,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Primary else mode.color,
            )
            Text(
                text = eta,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        RadioIndicator(selected = selected)
    }
}
