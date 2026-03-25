package com.ohmyguide.app.ui.screen.story

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DarkBg
import com.ohmyguide.app.ui.theme.DarkBorder
import com.ohmyguide.app.ui.theme.DarkPageCounter
import com.ohmyguide.app.ui.theme.DarkSurface
import com.ohmyguide.app.ui.theme.DarkText
import com.ohmyguide.app.ui.theme.DarkWaveInactive
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.PrimaryLight

@Composable
fun StoryTopBar(currentPage: Int, totalPages: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                text = LocalStrings.current.map,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DarkText,
            )
        }
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
        Text(
            text = "${currentPage + 1}/$totalPages",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = DarkPageCounter,
        )
    }
}

@Composable
fun AudioPlayerBar(isPlaying: Boolean, isLoading: Boolean = false, onToggle: () -> Unit) {
    // Pulse for play button when paused
    val pulseScale = if (!isPlaying) {
        val pulseTransition = rememberInfiniteTransition(label = "playPulse")
        val scale by pulseTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseScale",
        )
        scale
    } else 1f

    // Wave animation phase for playing state
    val wavePhase = if (isPlaying) {
        val waveTransition = rememberInfiniteTransition(label = "wave")
        val phase by waveTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28f, // 2 * PI
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Restart,
            ),
            label = "wavePhase",
        )
        phase
    } else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Play/Pause button with pulse when paused
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isPlaying) Brush.linearGradient(listOf(Primary, PrimaryLight))
                    else Brush.linearGradient(listOf(Primary.copy(alpha = 0.6f), PrimaryLight.copy(alpha = 0.6f)))
                )
                .clickable(enabled = !isLoading, onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BgWhite,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = BgWhite,
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = when {
                    isLoading -> LocalStrings.current.loading
                    isPlaying -> LocalStrings.current.nowPlaying
                    else -> LocalStrings.current.paused
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isPlaying) Primary else DarkText,
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Animated wave bars
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                repeat(24) { i ->
                    val h = if (isPlaying) {
                        val wave = kotlin.math.sin(i * 0.7 + wavePhase.toDouble())
                        (4 + (wave * 8 + 6)).dp
                    } else 3.dp
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isPlaying) Primary else DarkWaveInactive
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun StoryBottomNav(
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
                    colors = listOf(androidx.compose.ui.graphics.Color.Transparent, DarkBg, DarkBg),
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
                    text = if (isLastPage) LocalStrings.current.backToGuide else LocalStrings.current.next,
                    style = MaterialTheme.typography.titleMedium,
                    color = BgWhite,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = BgWhite)
            }
        }
    }
}
