package com.ohmyguide.app.ui.common

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
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
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.DisabledBg
import com.ohmyguide.app.ui.theme.DisabledText
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryDark
import com.ohmyguide.app.ui.theme.PrimaryLight
import com.ohmyguide.app.ui.theme.TextInactive
import com.ohmyguide.app.ui.theme.TextPrimary

// ── Gradient Brushes ──

val PrimaryGradient = Brush.linearGradient(
    colors = listOf(PrimaryDark, Primary, PrimaryLight)
)

val PrimaryGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(PrimaryDark, PrimaryLight)
)

// ── Primary CTA Button ──

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (enabled) Modifier.background(PrimaryGradient)
                else Modifier.background(DisabledBg)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) BgWhite else DisabledText,
        )
    }
}

// ── Bottom Navigation Bar ──

data class NavTab(
    val id: String,
    val label: String,
)

val MAIN_TABS = listOf(
    NavTab("main", "Home"),
    NavTab("explore", "Explore"),
    NavTab("phrases", "Phrases"),
)

@Composable
fun BottomNavBar(
    activeTab: String,
    onTabChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(BgWhite)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MAIN_TABS.forEach { tab ->
            val active = activeTab == tab.id
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabChange(tab.id) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (active) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(PrimaryGradientHorizontal)
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Icon
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (tab.id) {
                            "main" -> Icons.Filled.Home
                            "explore" -> Icons.Filled.Star
                            "phrases" -> Icons.AutoMirrored.Filled.Chat
                            else -> Icons.Filled.Home
                        },
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp),
                        tint = if (active) Primary else TextInactive,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Primary else TextInactive,
                    ),
                )
            }
        }
    }
}

// ── Mascot Avatar ──

@Composable
fun MascotAvatar(
    size: Int = 36,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .then(
                if (showBorder) Modifier.background(Primary.copy(alpha = 0.1f))
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ── Feature Pill ──

@Composable
fun FeaturePill(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
        )
    }
}

// ── Divider ──

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderLight)
    )
}

@Preview(showBackground = true)
@Composable
private fun CommonPreview() {
    OhMyGuideTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(text = "Let's Get Started!", onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(text = "Disabled", onClick = {}, enabled = false)
            Spacer(modifier = Modifier.height(16.dp))
            FeaturePill(emoji = "\uD83D\uDCCD", label = "GPS Guide")
            Spacer(modifier = Modifier.height(16.dp))
            BottomNavBar(activeTab = "main", onTabChange = {})
        }
    }
}