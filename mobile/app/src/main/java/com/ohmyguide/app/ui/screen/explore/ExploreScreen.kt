package com.ohmyguide.app.ui.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.ContentBgTop
import com.ohmyguide.app.ui.theme.ExploreDarkBg
import com.ohmyguide.app.ui.theme.HotBadge
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg

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
            .background(ExploreDarkBg),
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                HeroBanner()
            }

            item {
                CategoryCardsRow(
                    selectedCategory = selectedCategory,
                    onCategoryClick = { key ->
                        selectedCategory = if (selectedCategory == key) null else key
                    },
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(ContentBgTop, PrimaryBg)
                            )
                        )
                        .padding(top = 20.dp),
                ) {
                    RegionChips(
                        selectedRegion = selectedRegion,
                        onRegionClick = { selectedRegion = it },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (featuredCourse != null && selectedCategory == null) {
                        SectionHeader(
                            icon = Icons.Filled.Whatshot,
                            iconTint = HotBadge,
                            title = "Hot This Week",
                            iconBg = HotBadge.copy(alpha = 0.2f),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CourseCard(
                            course = featuredCourse,
                            onClick = { navController.navigate(Screen.CourseDetail.createRoute(featuredCourse.id)) },
                            featured = true,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

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
                                onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExploreScreenPreview() {
    OhMyGuideTheme {
        ExploreScreen(rememberNavController())
    }
}
