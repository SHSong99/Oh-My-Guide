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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
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
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary
import com.ohmyguide.app.ui.theme.TransitAmber
import com.ohmyguide.app.ui.theme.TransitGray

data class TransitSegment(
    val type: String,
    val lineName: String,
    val color: Color,
    val from: String,
    val fromKr: String,
    val to: String,
    val toKr: String,
    val stops: Int,
    val duration: String,
    val sectionTime: Int,
    val realtimeMin: Int? = null,
    val realtimeStations: Int? = null,
    val coords: List<Pair<Double, Double>> = emptyList(),
)

data class TransitRoute(
    val id: String,
    val pathType: Int,
    val title: String,
    val badge: String,
    val badgeColor: Color,
    val totalTime: String,
    val totalMinutes: Int,
    val eta: String,
    val timeRange: String,
    val payment: String,
    val segments: List<TransitSegment>,
)

// ── Filter Tab ──

enum class TransitFilter(val label: String) {
    All("All"),
    Bus("Bus"),
    Subway("Subway"),
    BusSubway("Bus+Subway"),
}

@Composable
fun TransitFilterTabs(
    routes: List<TransitRoute>,
    selected: TransitFilter,
    onSelect: (TransitFilter) -> Unit,
) {
    val counts = mapOf(
        TransitFilter.All to routes.size,
        TransitFilter.Bus to routes.count { it.pathType == 2 },
        TransitFilter.Subway to routes.count { it.pathType == 1 },
        TransitFilter.BusSubway to routes.count { it.pathType == 3 },
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TransitFilter.entries.forEach { filter ->
            val isSelected = selected == filter
            val count = counts[filter] ?: 0
            Text(
                text = "${filter.label} $count",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (isSelected) Primary else TextCaption,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isSelected) Modifier.background(Primary.copy(alpha = 0.1f))
                        else Modifier
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

// ── Route Card ──

@Composable
fun RouteCard(
    route: TransitRoute,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = route.totalTime,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(route.badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    val badgeIcon = when (route.pathType) {
                        1 -> Icons.Filled.Train
                        2 -> Icons.Filled.DirectionsBus
                        else -> null
                    }
                    if (route.pathType == 3) {
                        Icon(Icons.Filled.DirectionsBus, contentDescription = null, modifier = Modifier.size(10.dp), tint = BgWhite)
                        Icon(Icons.Filled.Train, contentDescription = null, modifier = Modifier.size(10.dp), tint = BgWhite)
                    } else if (badgeIcon != null) {
                        Icon(badgeIcon, contentDescription = null, modifier = Modifier.size(10.dp), tint = BgWhite)
                    }
                    Text(
                        text = route.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        ),
                        color = BgWhite,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${route.timeRange}  \u00B7  ${route.payment}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }

        // Proportional segment bar with icons
        val totalSec = route.segments.sumOf { it.sectionTime }.coerceAtLeast(1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            route.segments.forEach { segment ->
                val weight = segment.sectionTime.toFloat() / totalSec
                Row(
                    modifier = Modifier
                        .weight(weight.coerceAtLeast(0.05f))
                        .height(28.dp)
                        .background(segment.color),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    val icon = when (segment.type) {
                        "bus" -> Icons.Filled.DirectionsBus
                        "subway" -> Icons.Filled.Train
                        else -> Icons.AutoMirrored.Filled.DirectionsWalk
                    }
                    val tint = if (segment.type == "walk") TextCaption else BgWhite
                    if (segment.sectionTime >= 2) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = tint)
                    }
                    if (segment.sectionTime >= 4) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${segment.sectionTime}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = tint,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
            ) {
                route.segments.forEachIndexed { index, segment ->
                    SegmentRow(segment = segment, isLast = index == route.segments.lastIndex)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ── Segment Row ──

@Composable
private fun SegmentRow(segment: TransitSegment, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp),
        ) {
            val icon = when (segment.type) {
                "bus" -> Icons.Filled.DirectionsBus
                "subway" -> Icons.Filled.Train
                else -> Icons.AutoMirrored.Filled.DirectionsWalk
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(segment.color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = BgWhite)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (segment.type == "walk" && segment.fromKr.isEmpty()) 24.dp else 44.dp)
                        .background(segment.color.copy(alpha = 0.3f)),
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Line name + duration + stops
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

            // Realtime arrival info
            if (segment.type == "bus" && segment.realtimeMin != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(InfoGreen),
                    )
                    Text(
                        text = "${segment.realtimeMin} min (${segment.realtimeStations} stops away)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = InfoGreen,
                    )
                }
            }

            // Station names (bus/subway only)
            if (segment.type != "walk" && segment.fromKr.isNotEmpty() && segment.toKr.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${segment.from} \u2192 ${segment.to}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "${segment.fromKr} \u2192 ${segment.toKr}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextCaption,
                )
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
