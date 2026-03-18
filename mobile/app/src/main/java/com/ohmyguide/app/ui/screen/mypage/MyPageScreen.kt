package com.ohmyguide.app.ui.screen.mypage

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.Error
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

private data class MenuItem(
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val label: String,
    val desc: String,
    val count: Int? = null,
)

private val MENU_ITEMS = listOf(
    MenuItem(Icons.Filled.LocationOn, Primary, PrimaryBg, "Visit History", "Places you've been", count = 0),
    MenuItem(Icons.Filled.Bookmark, Color(0xFFF59E0B), Color(0xFFFFFBEB), "Bookmarks", "Saved places & phrases", count = 0),
    MenuItem(Icons.Filled.Headphones, Color(0xFF8B5CF6), Color(0xFFF5F3FF), "Story Archive", "Stories you've listened", count = 0),
    MenuItem(Icons.Filled.Language, Color(0xFF06B6D4), Color(0xFFECFEFF), "Language", "English"),
    MenuItem(Icons.Filled.Notifications, Color(0xFFEF4444), Color(0xFFFEF2F2), "Notifications", "On"),
    MenuItem(Icons.Filled.Palette, Color(0xFFEC4899), Color(0xFFFDF2F8), "Theme", "Light"),
)

sealed class MyPageUiState {
    object Loading : MyPageUiState()
    object Idle : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}

@Composable
fun MyPageScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF)),
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "My Page",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderLight),
        )

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Profile card
            ProfileCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Menu items
            MenuCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Sign out
            SignOutButton(onClick = { /* TODO */ })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Profile Card ──

@Composable
private fun ProfileCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.masot),
            contentDescription = "Profile",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(3.dp, Primary, CircleShape)
                .background(PrimaryBg),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Traveler",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Seoul, Jongno area",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InterestTag(icon = Icons.Filled.Restaurant, color = Primary, label = "Food")
                InterestTag(icon = Icons.Filled.AccountBalance, color = Primary, label = "Culture")
            }
        }
    }
}

@Composable
private fun InterestTag(icon: ImageVector, color: Color, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryBgLight)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Primary,
        )
    }
}

// ── Menu Card ──

@Composable
private fun MenuCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp)),
    ) {
        MENU_ITEMS.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO */ }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.bgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = item.color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = item.desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                }
                if (item.count != null) {
                    Text(
                        text = "${item.count}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextCaption)
            }
            if (index < MENU_ITEMS.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color(0xFFF8F9FB)),
                )
            }
        }
    }
}

// ── Sign Out ──

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = Error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.titleSmall,
                color = Error,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyPageScreenPreview() {
    OhMyGuideTheme {
        MyPageScreen(rememberNavController())
    }
}