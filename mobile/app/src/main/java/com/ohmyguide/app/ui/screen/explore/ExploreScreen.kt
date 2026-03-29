package com.ohmyguide.app.ui.screen.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.ContentBgTop
import com.ohmyguide.app.ui.theme.ExploreDarkBg
import com.ohmyguide.app.ui.theme.HotBadge
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import kotlin.math.absoluteValue

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    object Idle : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

private const val COLLAPSED_HEIGHT_DP = 340f

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    var isShowcase by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var selectedRegion by remember { mutableStateOf("all") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val themes by viewModel.themes.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val exploreUiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = {
        if (isShowcase) themes.size + 1 else themes.size
    })

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.toFloat()

    // Hero height: animate between full screen and collapsed
    val heroHeight by animateDpAsState(
        targetValue = if (isShowcase) screenHeightDp.dp else COLLAPSED_HEIGHT_DP.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "heroHeight",
    )

    val heroCorner by animateDpAsState(
        targetValue = if (isShowcase) 0.dp else 20.dp,
        animationSpec = tween(300),
        label = "heroCorner",
    )

    val navBarOffset by animateDpAsState(
        targetValue = if (isShowcase) 80.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "navBarOffset",
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page < themes.size) {
                viewModel.onPageChanged(page)
            }
        }
    }

    // 트리거 페이지 도달 시 쇼케이스 종료 → 코스 목록 전환
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page >= themes.size && isShowcase) {
                viewModel.pauseAll()
                isShowcase = false
                pagerState.scrollToPage(0)
            }
        }
    }

    val filteredCourses = courses.filter { course ->
        (selectedRegion == "all" || course.region == selectedRegion) &&
                (selectedCategory == null || course.category == selectedCategory)
    }
    val featuredCourse = filteredCourses.firstOrNull()
    val otherCourses = filteredCourses.drop(1)

    // Hero section — outside LazyColumn to avoid touch conflict
    @Composable
    fun HeroSection() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
                .clip(
                    RoundedCornerShape(
                        bottomStart = heroCorner,
                        bottomEnd = heroCorner,
                    )
                ),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { page ->
                if (page < themes.size) {
                    val pageOffset = (pagerState.currentPage - page) +
                            pagerState.currentPageOffsetFraction
                    val isActive = pagerState.currentPage == page &&
                            pageOffset.absoluteValue < 0.5f

                    ThemePage(
                        theme = themes[page],
                        player = viewModel.getOrCreatePlayer(page),
                        isActive = isActive,
                        pageOffset = pageOffset,
                        compact = !isShowcase,
                        onExploreClick = {
                            if (isShowcase) {
                                viewModel.pauseAll()
                                isShowcase = false
                            }
                            navController.navigate(
                                Screen.CourseDetail.createRoute(themes[page].courseId.toString())
                            )
                        },
                    )
                } else {
                    // 트리거 페이지: 마지막 테마 색상 그라디언트
                    val lastTheme = themes.last()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(lastTheme.dominantColor).copy(alpha = 0.8f),
                                        Color(lastTheme.dominantColor).copy(alpha = 0.4f),
                                        Color.Black,
                                    ),
                                )
                            ),
                    )
                }
            }

            NetflixDotIndicator(
                pageCount = themes.size,
                currentPage = pagerState.currentPage.coerceAtMost(themes.size - 1),
                modifier = if (isShowcase) {
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                } else {
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ExploreDarkBg),
    ) {
        if (isShowcase) {
            // Showcase mode: HorizontalPager outside LazyColumn for swipe to work
            Box(modifier = Modifier.weight(1f)) {
                HeroSection()
            }
        } else {
            // Normal mode: hero + course list in LazyColumn
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                item {
                    HeroSection()
                }

            // Course list
            item {
                AnimatedVisibility(
                    visible = !isShowcase,
                    enter = expandVertically(
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(tween(300, delayMillis = 100)),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        CategoryCardsRow(
                            selectedCategory = selectedCategory,
                            onCategoryClick = { key ->
                                selectedCategory = if (selectedCategory == key) null else key
                            },
                        )

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
                                regions = regions,
                                selectedRegion = selectedRegion,
                                onRegionClick = { selectedRegion = it },
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val strings = LocalStrings.current

                            if (featuredCourse != null && selectedCategory == null) {
                                SectionHeader(
                                    icon = Icons.Filled.Whatshot,
                                    iconTint = HotBadge,
                                    title = strings.hotThisWeek,
                                    iconBg = HotBadge.copy(alpha = 0.2f),
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                CourseCard(
                                    course = featuredCourse,
                                    onClick = {
                                        navController.navigate(
                                            Screen.CourseDetail.createRoute(featuredCourse.id)
                                        )
                                    },
                                    featured = true,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            val displayCourses =
                                if (selectedCategory != null) filteredCourses else otherCourses
                            val sectionTitle = if (selectedCategory != null) {
                                EXPLORE_CATEGORY_GROUPS.find { it.key == selectedCategory }?.label
                                    ?: strings.courses
                            } else strings.allCourses

                            SectionHeader(
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconTint = Primary,
                                title = sectionTitle,
                                iconBg = if (selectedCategory != null) {
                                    EXPLORE_CATEGORY_GROUPS.find { it.key == selectedCategory }
                                        ?.color?.copy(alpha = 0.15f)
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
                                        onClick = {
                                            navController.navigate(
                                                Screen.CourseDetail.createRoute(course.id)
                                            )
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
            } // end LazyColumn
        } // end if/else

        BottomNavBar(
            activeTab = "explore",
            onTabChange = { tab ->
                when (tab) {
                    "main" -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                    "mypage" -> navController.navigate(Screen.MyPage.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier.offset(y = navBarOffset),
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
