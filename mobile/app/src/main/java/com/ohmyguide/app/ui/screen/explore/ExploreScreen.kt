package com.ohmyguide.app.ui.screen.explore

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.ui.graphics.Brush
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
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.fixtures.EXPLORE_REGIONS
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    object Idle : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

@Composable
fun ExploreScreen(navController: NavController) {
    var selectedRegion by remember { mutableStateOf("all") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredCourses = EXPLORE_COURSES.filter { course ->
        (selectedRegion == "all" || course.region == selectedRegion) &&
                (selectedCategory == null || course.category == selectedCategory)
    }
    val featuredCourse = filteredCourses.firstOrNull()
    val otherCourses = filteredCourses.drop(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14)),
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            // ── Hero Banner ──
            item {
                HeroBanner()
            }

            // ── Category Cards ──
            item {
                CategoryCardsRow(
                    selectedCategory = selectedCategory,
                    onCategoryClick = { key ->
                        selectedCategory = if (selectedCategory == key) null else key
                    },
                )
            }

            // ── Content area (white bg) ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFAFBFF), PrimaryBg)
                            )
                        )
                        .padding(top = 20.dp),
                ) {
                    // Region chips
                    RegionChips(
                        selectedRegion = selectedRegion,
                        onRegionClick = { selectedRegion = it },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Featured course
                    if (featuredCourse != null && selectedCategory == null) {
                        SectionHeader(
                            icon = Icons.Filled.Whatshot,
                            iconTint = Color(0xFFFF6B6B),
                            title = "Hot This Week",
                            iconBg = Color(0x33FF6B6B),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CourseCard(
                            course = featuredCourse,
                            onClick = { /* TODO: navigate to detail */ },
                            featured = true,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // All / Filtered courses
                    val displayCourses = if (selectedCategory != null) filteredCourses else otherCourses
                    val sectionTitle = if (selectedCategory != null) {
                        EXPLORE_CATEGORY_GROUPS.find { it.key == selectedCategory }?.label ?: "Courses"
                    } else "All Courses"

                    SectionHeader(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = Primary,
                        title = sectionTitle,
                        iconBg = if (selectedCategory != null) {
                            EXPLORE_CATEGORY_GROUPS.find { it.key == selectedCategory }?.color?.copy(alpha = 0.15f)
                                ?: Primary.copy(alpha = 0.15f)
                        } else Primary.copy(alpha = 0.15f),
                        count = displayCourses.size,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (displayCourses.isEmpty()) {
                        EmptyState()
                    } else {
                        displayCourses.forEach { course ->
                            CourseCard(
                                course = course,
                                onClick = { /* TODO: navigate to detail */ },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // ── Bottom Nav ──
        BottomNavBar(
            activeTab = "explore",
            onTabChange = { tab ->
                when (tab) {
                    "main" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                    "phrases" -> navController.navigate(Screen.Phrases.route)
                }
            },
        )
    }
}

// ── Hero Banner ──

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color(0xFF0A0A14)),
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A14).copy(alpha = 0.5f),
                            Color(0xFF0A0A14).copy(alpha = 0.2f),
                            Color(0xFF0A0A14),
                        ),
                    )
                ),
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.masot),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(2.dp, BgWhite.copy(alpha = 0.3f), CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "K-Culture",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = BgWhite,
                )
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.labelSmall,
                    color = BgWhite.copy(alpha = 0.6f),
                )
            }
        }

        // Hero text
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCCFF6B6B))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Whatshot, contentDescription = null, modifier = Modifier.size(12.dp), tint = BgWhite)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TRENDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Dive into\nK-Culture",
                style = MaterialTheme.typography.displayLarge,
                color = BgWhite,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Curated courses by locals & K-fans",
                style = MaterialTheme.typography.bodySmall,
                color = BgWhite.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(if (i == 0) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i == 0) BgWhite else BgWhite.copy(alpha = 0.3f)),
                    )
                }
            }
        }
    }
}

// ── Category cards row ──

@Composable
private fun CategoryCardsRow(
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .background(Color(0xFF0A0A14))
            .padding(vertical = 8.dp),
    ) {
        items(EXPLORE_CATEGORY_GROUPS) { cg ->
            val active = selectedCategory == cg.key
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (active) cg.color.copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .then(
                        if (active) Modifier.border(2.dp, cg.color, RoundedCornerShape(18.dp))
                        else Modifier
                    )
                    .clickable { onCategoryClick(cg.key) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) BgWhite.copy(alpha = 0.3f)
                                else BgWhite.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = cg.emoji, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cg.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
        }
    }
}

// ── Region chips ──

@Composable
private fun RegionChips(
    selectedRegion: String,
    onRegionClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(EXPLORE_REGIONS) { region ->
            val active = selectedRegion == region.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) TextPrimary else BgWhite)
                    .then(
                        if (active) Modifier else Modifier.border(1.dp, Border, RoundedCornerShape(20.dp))
                    )
                    .clickable { onRegionClick(region.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = region.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (active) BgWhite else TextSecondary,
                )
            }
        }
    }
}

// ── Section header ──

@Composable
private fun SectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    iconBg: Color,
    count: Int? = null,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimary,
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count courses",
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }
    }
}

// ── Empty state ──

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(28.dp), tint = Primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No courses found",
            style = MaterialTheme.typography.titleSmall,
            color = TextCaption,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Try a different region or category",
            style = MaterialTheme.typography.labelMedium,
            color = TextCaption.copy(alpha = 0.7f),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExploreScreenPreview() {
    OhMyGuideTheme {
        ExploreScreen(rememberNavController())
    }
}