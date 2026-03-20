package com.ohmyguide.app.ui.screen.transport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.ui.common.RadioIndicator
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

internal data class TransitSegment(
    val type: String,
    val lineName: String,
    val color: Color,
    val from: String,
    val to: String,
    val stops: Int,
    val duration: String,
)

internal data class TransitRoute(
    val id: String,
    val title: String,
    val badge: String,
    val badgeColor: Color,
    val totalTime: String,
    val eta: String,
    val segments: List<TransitSegment>,
)

@Composable
internal fun RouteCard(
    route: TransitRoute,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Primary else Border
    val bgColor = if (selected) PrimaryBg else BgWhite

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioIndicator(selected = selected)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = route.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = route.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        ),
                        color = BgWhite,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(route.badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = route.totalTime,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                )
                Text(
                    text = route.eta,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCaption,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
        ) {
            route.segments.forEach { segment ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(segment.color),
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            ) {
                route.segments.forEachIndexed { index, segment ->
                    SegmentRow(segment = segment, isLast = index == route.segments.lastIndex)
                }
            }
        }

        if (!expanded) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SegmentRow(segment: TransitSegment, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(segment.color),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(segment.color.copy(alpha = 0.3f)),
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = segment.lineName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = segment.color,
                )
                Text(
                    text = segment.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCaption,
                )
                if (segment.stops > 0) {
                    Text(
                        text = "${segment.stops} stops",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${segment.from} \u2192 ${segment.to}",
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
            if (!isLast) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
