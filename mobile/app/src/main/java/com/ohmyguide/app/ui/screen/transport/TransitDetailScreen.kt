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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.common.GuideBubble
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

// ── Data models ──

private data class TransitSegment(
    val type: String,        // "bus" | "subway" | "walk"
    val lineName: String,
    val color: Color,
    val from: String,
    val to: String,
    val stops: Int,
    val duration: String,
)

private data class TransitRoute(
    val id: String,
    val title: String,
    val badge: String,
    val badgeColor: Color,
    val totalTime: String,
    val eta: String,
    val segments: List<TransitSegment>,
)

private val SAMPLE_ROUTES = listOf(
    TransitRoute(
        id = "route1",
        title = "Bus 144 + 272",
        badge = "TRANSFER",
        badgeColor = Color(0xFFF59E0B),
        totalTime = "12 min",
        eta = "ETA 9:53 AM",
        segments = listOf(
            TransitSegment("bus", "Bus 144", Color(0xFF16A34A), "Gwanghwamun", "Jongno 3-ga", 3, "7 min"),
            TransitSegment("walk", "Transfer", Color(0xFF9CA3AF), "Jongno 3-ga", "Jongno 3-ga", 0, "~2 min walk"),
            TransitSegment("bus", "Bus 272", Color(0xFF2563EB), "Jongno 3-ga", "Jongno 5-ga", 2, "5 min"),
        ),
    ),
    TransitRoute(
        id = "route2",
        title = "Subway Line 1",
        badge = "SUBWAY",
        badgeColor = Color(0xFF2563EB),
        totalTime = "15 min",
        eta = "ETA 9:56 AM",
        segments = listOf(
            TransitSegment("subway", "Line 1", Color(0xFF1D4ED8), "Euljiro 1-ga", "Jongno 5-ga", 4, "15 min"),
        ),
    ),
    TransitRoute(
        id = "route3",
        title = "Bus 272",
        badge = "DIRECT",
        badgeColor = Color(0xFF16A34A),
        totalTime = "18 min",
        eta = "ETA 9:59 AM",
        segments = listOf(
            TransitSegment("bus", "Bus 272", Color(0xFF2563EB), "Gwanghwamun", "Jongno 5-ga", 5, "18 min"),
        ),
    ),
)

@Composable
fun TransitDetailScreen(navController: NavController, placeId: String) {
    var selectedRouteId by remember { mutableStateOf(SAMPLE_ROUTES[0].id) }
    var expandedRouteId by remember { mutableStateOf<String?>(SAMPLE_ROUTES[0].id) }

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
                text = "Transit Routes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ── Route list ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SAMPLE_ROUTES.forEach { route ->
                RouteCard(
                    route = route,
                    selected = selectedRouteId == route.id,
                    expanded = expandedRouteId == route.id,
                    onClick = {
                        selectedRouteId = route.id
                        expandedRouteId = if (expandedRouteId == route.id) null else route.id
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kkaebi tip
            GuideBubble(
                text = "Bus 144 + 272 is the fastest option. The transfer at Jongno 3-ga takes only 2 minutes!",
            )

            Spacer(modifier = Modifier.height(16.dp))
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
                        navController.navigate(Screen.Navi.createRoute(placeId, "transit"))
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Start Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    color = BgWhite,
                )
            }
        }
    }
}

// ── Route card ──

@Composable
private fun RouteCard(
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
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Radio
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
                    // Badge
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

        // Color bar
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

        // Expanded segment details
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

// ── Segment row (timeline style) ──

@Composable
private fun SegmentRow(segment: TransitSegment, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Timeline
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

        // Info
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TransitDetailScreenPreview() {
    OhMyGuideTheme {
        TransitDetailScreen(rememberNavController(), placeId = "dm3")
    }
}