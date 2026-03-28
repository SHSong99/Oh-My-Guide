package com.ohmyguide.app.ui.screen.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ohmyguide.app.R
import com.ohmyguide.app.service.TtsManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.fixtures.Spot
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.common.OmgTopBar
import com.ohmyguide.app.ui.common.PrimaryButton
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: String,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CourseDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BgWhite),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
        is CourseDetailUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().background(BgWhite),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "Retry", onClick = { viewModel.retry() })
            }
        }
        is CourseDetailUiState.Success -> {
            CourseDetailContent(
                navController = navController,
                course = state.course,
            )
        }
    }
}

@Composable
private fun CourseDetailContent(
    navController: NavController,
    course: Course,
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var showGuidePopup by remember { mutableStateOf(false) }
    val guideMessage = "안녕하세요! ${course.title}를 소개드리겠습니다. " +
        "체험을 원하시면 코스 시작하기를 눌러주세요!"

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // 화면 진입 시 1초 후 깨비 팝업 + TTS 시작
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000L)
        showGuidePopup = true
        ttsManager.speak(guideMessage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        OmgTopBar(
            title = course.title,
            onBack = { navController.popBackStack() },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero section
            CourseHero(course = course)

            // Course info
            CourseInfo(course = course)

            Spacer(modifier = Modifier.height(24.dp))

            // Course route
            Text(
                text = LocalStrings.current.courseRoute,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = TextCaption,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            course.spots.forEachIndexed { index, spot ->
                SpotCard(
                    spot = spot,
                    index = index + 1,
                    isLast = index == course.spots.lastIndex,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Start navigation button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            PrimaryButton(
                text = LocalStrings.current.startCourseNavi,
                onClick = {
                    navController.navigate(
                        Screen.Transport.createRoute(
                            placeId = course.id,
                            courseId = course.id,
                        )
                    )
                },
                icon = Icons.Filled.Navigation,
            )
        }

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
        )
    }

    // 깨비 가이드 팝업 오버레이 (CourseNaviScreen과 동일 스타일)
    if (showGuidePopup) {
        val ttsProgress by ttsManager.progress.collectAsState()
        val sentences = remember(guideMessage) {
            guideMessage.split(Regex("(?<=[.!?。])(\\s*)")).filter { it.isNotBlank() }
                .let { if (it.isEmpty()) listOf(guideMessage) else it }
        }
        val displaySentence = if (sentences.size <= 1) {
            guideMessage
        } else {
            val totalChars = sentences.sumOf { it.length }
            val cumWeights = sentences.runningFold(0f) { acc, s -> acc + s.length.toFloat() / totalChars }.drop(1)
            val idx = cumWeights.indexOfFirst { it > ttsProgress }.let { if (it == -1) sentences.lastIndex else it }
            sentences[idx]
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 24.dp, end = 24.dp, top = 70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BgWhite.copy(alpha = 0.95f))
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Image(
                    painter = painterResource(R.drawable.masot),
                    contentDescription = "Kkaebi",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedContent(
                        targetState = displaySentence,
                        transitionSpec = {
                            (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 })
                                .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 })
                        },
                        label = "guidePopupSentence",
                    ) { sentence ->
                        Text(
                            text = sentence,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
    }
    } // Box 닫기
}

@Composable
private fun CourseHero(course: Course) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(BgSub),
    ) {
        if (course.imageUrl != null) {
            AsyncImage(
                model = course.imageUrl,
                contentDescription = course.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = course.emoji, fontSize = 64.sp)
            }
        }
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.4f)),
                    )
                ),
        )
        // Category badge
        Text(
            text = "${course.emoji} ${course.category.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = BgWhite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Primary.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun CourseInfo(course: Course) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = course.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = course.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Stats row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (course.spotCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${course.spotCount} ${LocalStrings.current.spots}", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            if (course.duration.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = course.duration, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = course.region.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            }
        }

        if (course.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(course.tags) { tag ->
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PrimaryBgLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpotCard(
    spot: Spot,
    index: Int,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
    ) {
        // Number + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                )
            }
            if (!isLast) {
                // Walk time indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(12.dp)
                            .background(Border),
                    )
                    if (spot.walkMin > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(10.dp), tint = TextCaption)
                            Text(
                                text = "${spot.walkMin}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCaption,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(12.dp)
                            .background(Border),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Spot content card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
                .clip(RoundedCornerShape(16.dp))
                .background(BgWhite),
        ) {
            // Spot image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(BgSub),
                contentAlignment = Alignment.Center,
            ) {
                if (spot.imageUrl != null) {
                    AsyncImage(
                        model = spot.imageUrl,
                        contentDescription = spot.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(text = "📍", fontSize = 28.sp)
                }
                // Name overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            )
                        )
                        .padding(12.dp),
                ) {
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                    Text(
                        text = spot.nameKr,
                        style = MaterialTheme.typography.labelSmall,
                        color = BgWhite.copy(alpha = 0.8f),
                    )
                }
            }
            // Description
            Text(
                text = spot.desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CourseDetailScreenPreview() {
    OhMyGuideTheme {
        CourseDetailScreen(rememberNavController(), courseId = "demon-hunters")
    }
}