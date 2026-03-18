package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.PrimaryDark
import com.ohmyguide.app.ui.theme.PrimaryLight
import com.ohmyguide.app.ui.theme.TextCaption
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit,
) {
    // Bounce-in animation
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "bounceIn",
    )

    // Spinner rotation
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
        ),
        label = "rotate",
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2500L)
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgWhite, PrimaryBgLight),
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        // Mascot with bounce-in
        Image(
            painter = painterResource(R.drawable.masot),
            contentDescription = "Oh My Guide mascot",
            modifier = Modifier
                .size(140.dp)
                .scale(scale),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logo with gradient text
        Text(
            text = "Oh My Guide!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryDark, Primary, PrimaryLight),
                    ),
                    blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop,
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Your personal Korea travel buddy",
            style = MaterialTheme.typography.bodySmall,
            color = TextCaption,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Loading spinner
        Box(
            modifier = Modifier
                .size(32.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .drawWithContent {
                    drawCircle(
                        color = Border,
                        style = Stroke(width = 3.dp.toPx()),
                    )
                    drawArc(
                        color = Primary,
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx()),
                    )
                },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    OhMyGuideTheme {
        SplashScreen(onFinish = {})
    }
}