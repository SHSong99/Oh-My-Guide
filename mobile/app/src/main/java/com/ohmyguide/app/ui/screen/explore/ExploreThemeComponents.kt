package com.ohmyguide.app.ui.screen.explore

import android.view.ViewGroup
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.FeaturedTheme
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.ExploreDarkBg
import com.ohmyguide.app.ui.theme.ExploreProgressFilled
import com.ohmyguide.app.ui.theme.ExploreProgressTrack
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryDark
import com.ohmyguide.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay

// ── Video Background ──

@Composable
fun VideoBackground(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Box(modifier = modifier.background(Color.DarkGray))
        return
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                controllerAutoShow = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        update = { view ->
            view.player = player
            view.useController = false
            view.hideController()
        },
        modifier = modifier,
    )
}

// ── Story Progress Bar ──

@Composable
fun StoryProgressBar(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(pageCount) { index ->
            val progress by animateFloatAsState(
                targetValue = when {
                    index < currentPage -> 1f
                    index == currentPage -> 1f
                    else -> 0f
                },
                animationSpec = tween(300),
                label = "progress",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ExploreProgressTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ExploreProgressFilled),
                )
            }
        }
    }
}

// ── Netflix-style Dot Indicator ──

@Composable
fun NetflixDotIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 20.dp else 6.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "dotWidth",
            )
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.4f,
                animationSpec = tween(300),
                label = "dotAlpha",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .alpha(alpha)
                    .background(BgWhite),
            )
        }
    }
}

// ── Theme Page with Kinetic + Parallax effects ──

@Composable
fun ThemePage(
    theme: FeaturedTheme,
    player: Player?,
    isActive: Boolean,
    pageOffset: Float,
    onExploreClick: () -> Unit,
    onSeeAllCourses: (() -> Unit)? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    val localTitle = theme.title
    val localSubtitle = theme.subtitle

    // Staggered reveal: each element appears with spring delay
    var showBadge by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showCta by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        if (isActive) {
            showBadge = true
            delay(150)
            showTitle = true
            delay(150)
            showSubtitle = true
            delay(150)
            showCta = true
            delay(200)
            showHint = true
        } else {
            showBadge = false
            showTitle = false
            showSubtitle = false
            showCta = false
            showHint = false
        }
    }

    // Spring-based animations for each element
    val badgeAlpha by animateFloatAsState(
        targetValue = if (showBadge) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "badgeAlpha",
    )
    val badgeOffsetY by animateDpAsState(
        targetValue = if (showBadge) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "badgeY",
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "titleAlpha",
    )
    val titleOffsetY by animateDpAsState(
        targetValue = if (showTitle) 0.dp else 40.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "titleY",
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSubtitle) 1f else 0f,
        animationSpec = tween(400),
        label = "subtitleAlpha",
    )
    val subtitleOffsetY by animateDpAsState(
        targetValue = if (showSubtitle) 0.dp else 24.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "subtitleY",
    )

    val ctaScale by animateFloatAsState(
        targetValue = if (showCta) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ctaScale",
    )
    val ctaAlpha by animateFloatAsState(
        targetValue = if (showCta) 1f else 0f,
        animationSpec = tween(400),
        label = "ctaAlpha",
    )

    val hintAlpha by animateFloatAsState(
        targetValue = if (showHint) 0.6f else 0f,
        animationSpec = tween(600),
        label = "hintAlpha",
    )

    val themeColor = Color(theme.dominantColor)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.8f),
                        themeColor.copy(alpha = 0.4f),
                        Color.Black,
                    ),
                )
            )
            .then(if (compact) Modifier.clickable(onClick = onExploreClick) else Modifier),
    ) {
        // Video
        if (player != null) {
            VideoBackground(
                player = player,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Strong dark gradient overlay - bottom 75%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.75f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f),
                        ),
                    )
                ),
        )

        // Top gradient for status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                    )
                ),
        )

        // Content overlay with parallax counter-movement
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = if (compact) 20.dp else 24.dp)
                .padding(
                    top = if (compact) 12.dp else 40.dp,
                    bottom = if (compact) 24.dp else 80.dp,
                )
                .graphicsLayer {
                    translationX = size.width * pageOffset * -0.2f
                },
        ) {
            // Badge with spring bounce
            Box(
                modifier = Modifier
                    .offset(y = badgeOffsetY)
                    .alpha(badgeAlpha)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = theme.badge,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = BgWhite,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title with spring bounce
            Text(
                text = localTitle,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp,
                    )
                } else {
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 40.sp,
                    )
                },
                color = BgWhite,
                modifier = Modifier
                    .offset(y = titleOffsetY)
                    .alpha(titleAlpha),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = localSubtitle,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                color = BgWhite.copy(alpha = 0.9f),
                modifier = Modifier
                    .offset(y = subtitleOffsetY)
                    .alpha(subtitleAlpha),
            )

            if (!compact) {
            Spacer(modifier = Modifier.height(16.dp))

            // CTA Button with spring scale
            Row(
                modifier = Modifier
                    .scale(ctaScale)
                    .alpha(ctaAlpha)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgWhite)
                    .clickable(onClick = onExploreClick)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.exploreThisCourse,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (onSeeAllCourses != null) {
                // "See All Courses" button on last page
                Row(
                    modifier = Modifier
                        .alpha(hintAlpha * 2f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary.copy(alpha = 0.9f))
                        .clickable(onClick = onSeeAllCourses)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.seeAllCourses,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BgWhite,
                    )
                }
            } else {
                // Swipe hint
                Text(
                    text = strings.swipeToDiscover,
                    style = MaterialTheme.typography.labelMedium,
                    color = BgWhite,
                    modifier = Modifier.alpha(hintAlpha),
                )
            }
            } // end if (!compact)
        }
    }
}

// ── Mascot Outro Page ──

@Composable
fun MascotOutroPage(
    onEnterMain: () -> Unit,
) {
    val strings = LocalStrings.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    val mascotScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "mascotScale",
    )
    val mascotAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "mascotAlpha",
    )
    val textOffsetY by animateDpAsState(
        targetValue = if (showContent) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "textY",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(600, delayMillis = 200),
        label = "textAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ExploreDarkBg,
                        PrimaryDark.copy(alpha = 0.3f),
                        ExploreDarkBg,
                    ),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Mascot with spring scale
            Image(
                painter = painterResource(R.drawable.masot),
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .scale(mascotScale)
                    .alpha(mascotAlpha),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text
            Text(
                text = strings.diveIntoKCulture,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = BgWhite,
                modifier = Modifier
                    .offset(y = textOffsetY)
                    .alpha(textAlpha),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.curatedCourses,
                style = MaterialTheme.typography.bodyMedium,
                color = BgWhite.copy(alpha = 0.7f),
                modifier = Modifier
                    .offset(y = textOffsetY)
                    .alpha(textAlpha),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CTA Button
            Row(
                modifier = Modifier
                    .offset(y = textOffsetY)
                    .alpha(textAlpha)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .clickable(onClick = onEnterMain)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.seeAllCourses,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Primary,
                )
            }
        }
    }
}
