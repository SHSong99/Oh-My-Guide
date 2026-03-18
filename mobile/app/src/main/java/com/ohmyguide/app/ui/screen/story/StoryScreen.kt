package com.ohmyguide.app.ui.screen.story

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.common.PrimaryGradient
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryLight

private val DarkBg = Color(0xFF0D1117)
private val DarkSurface = Color(0xFF1E2A3E)
private val DarkBorder = Color(0xFF2A3A52)
private val DarkText = Color(0xFF8892A4)
private val DarkTextLight = Color(0xFFE8ECF4)

private val MOCK_PAGES = listOf(
    "Built in 1394 during the early Joseon Dynasty, Gwangjang Market was originally a textile trading hub. Over six centuries, it evolved into one of Seoul's most beloved food destinations.",
    "The famous bindaetteok (mung bean pancake) vendors have been perfecting their craft for generations. Each vendor has their own secret recipe, passed down through families.",
    "Today, Gwangjang Market attracts over 100,000 visitors daily. From mayak gimbap to yukhoe, every corner offers an authentic taste of Korean culinary heritage.",
)

@Composable
fun StoryOverlay(placeId: String, onDismiss: () -> Unit) {
    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Place"
    val placeNameKr = detail?.place?.nameKr ?: ""

    var currentPage by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    val totalPages = MOCK_PAGES.size
    val isLastPage = currentPage == totalPages - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF141B2D), Color(0xFF1A1A2E))
                )
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──
            TopBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onBack = onDismiss,
            )

            // ── Content ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 120.dp),
            ) {
                // POI header
                if (currentPage == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = placeName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = BgWhite,
                    )
                    Text(
                        text = placeNameKr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = placeName,
                            style = MaterialTheme.typography.titleLarge,
                            color = BgWhite,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = placeNameKr,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Audio player bar
                AudioPlayerBar(isPlaying = isPlaying, onToggle = { isPlaying = !isPlaying })

                Spacer(modifier = Modifier.height(20.dp))

                // Image placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail?.place?.emoji ?: "\uD83C\uDFDE\uFE0F",
                        fontSize = 48.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Story text
                Text(
                    text = MOCK_PAGES[currentPage],
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp),
                    color = DarkTextLight,
                )
            }
        }

        // ── Bottom navigation ──
        BottomNav(
            currentPage = currentPage,
            isLastPage = isLastPage,
            onPrev = { if (currentPage > 0) currentPage-- },
            onNext = {
                if (isLastPage) onDismiss()
                else currentPage++
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Top bar ──

@Composable
private fun TopBar(currentPage: Int, totalPages: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkText)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Map",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DarkText,
            )
        }
        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalPages) { i ->
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (i == currentPage) 20.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                i == currentPage -> Brush.horizontalGradient(listOf(Primary, PrimaryLight))
                                i < currentPage -> Brush.horizontalGradient(listOf(Primary, Primary))
                                else -> Brush.horizontalGradient(listOf(DarkBorder, DarkBorder))
                            }
                        ),
                )
            }
        }
        // Page counter
        Text(
            text = "${currentPage + 1}/$totalPages",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF5A6A82),
        )
    }
}

// ── Audio player bar ──

@Composable
private fun AudioPlayerBar(isPlaying: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Play/Pause button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isPlaying) Brush.linearGradient(listOf(Primary, PrimaryLight))
                    else Brush.linearGradient(listOf(DarkBorder, DarkBorder))
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = BgWhite,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = if (isPlaying) "Now playing\u2026" else "Paused",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Waveform bars
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                repeat(24) { i ->
                    val h = if (isPlaying) (4 + (kotlin.math.sin(i * 0.8) * 8 + 6)).dp else 3.dp
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isPlaying) Primary else Color(0xFF3A4A6B)
                            ),
                    )
                }
            }
        }
    }
}

// ── Bottom navigation ──

@Composable
private fun BottomNav(
    currentPage: Int,
    isLastPage: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DarkBg, DarkBg),
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (currentPage > 0) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .clickable(onClick = onPrev),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = DarkText)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryGradient)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLastPage) "Back to Guide" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    color = BgWhite,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = BgWhite)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StoryOverlayPreview() {
    OhMyGuideTheme {
        StoryOverlay(placeId = "dm3", onDismiss = {})
    }
}