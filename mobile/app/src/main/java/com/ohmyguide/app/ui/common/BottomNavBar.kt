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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryGradientHorizontal
import com.ohmyguide.app.ui.theme.TextInactive

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
