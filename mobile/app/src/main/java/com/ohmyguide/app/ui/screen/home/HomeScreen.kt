package com.ohmyguide.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.HOME_RECOMMENDATIONS
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.Success
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.navi.Screen

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Idle : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        // ── Header ──
        HomeHeader()

        // ── Location bar + Chat ──
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                // Location bar
                item {
                    LocationBar(spotCount = 6)
                }
                // Bot greeting
                item {
                    GuideBubble(
                        text = "Based on your choices, I've found perfect matches for you.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                // Recommendation sections
                items(HOME_RECOMMENDATIONS) { section ->
                    RecommendationBlock(
                        section = section,
                        onPlaceClick = { placeId ->
                            navController.navigate("place/$placeId")
                        },
                    )
                }
            }

            // ── Bottom CTA gradient overlay ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BgWhite.copy(alpha = 0f),
                                BgWhite.copy(alpha = 0.9f),
                                BgWhite,
                            ),
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                FindOtherPlacesButton(onClick = { /* TODO */ })
            }
        }

        // ── Bottom Nav ──
        BottomNavBar(
            activeTab = "main",
            onTabChange = { tab ->
                when (tab) {
                    "explore" -> navController.navigate(Screen.Explore.route)
                    "phrases" -> navController.navigate(Screen.Phrases.route)
                }
            },
        )
    }
}

// ── Header ──

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite.copy(alpha = 0.8f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mascot avatar with online indicator
        Box {
            Image(
                painter = painterResource(R.drawable.masot),
                contentDescription = "Guide",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary, CircleShape)
                    .background(PrimaryBg),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(BgWhite)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Success),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Oh My Guide",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = "Curating spots...",
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        // Reset button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BgSub)
                .clickable { /* TODO */ }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextCaption)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reset",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }
}

// ── Location bar ──

@Composable
private fun LocationBar(spotCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Near Jongno",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
        }
        Text(
            text = "$spotCount spots",
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(Border),
    )
}

// ── Recommendation block ──

@Composable
private fun RecommendationBlock(
    section: RecommendationSection,
    onPlaceClick: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        // Section header
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(section.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = section.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (section.label == "Big Data") TextCaption else Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (section.label == "Big Data") BgSub else PrimaryBgLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal place cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(section.places) { place ->
                PlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // "Show more" button
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .background(BgWhite)
                .clickable { /* TODO */ }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (section.title.contains("Picks")) Icons.Filled.Favorite else Icons.Filled.Whatshot,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Primary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = section.btnText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Primary,
            )
        }
    }
}

// ── Find other places CTA ──

@Composable
private fun FindOtherPlacesButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Secondary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Find other places",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    OhMyGuideTheme {
        HomeScreen(rememberNavController())
    }
}
