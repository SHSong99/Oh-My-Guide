package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.screen.auth.AuthState
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onSignIn: () -> Unit,
    authState: AuthState = AuthState.Idle,
    onDismissError: () -> Unit = {},
) {
    if (authState is AuthState.Error) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("로그인 오류") },
            text = { Text(authState.message) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("확인")
                }
            },
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgWhite, PrimaryBgLight),
                )
            ),
    ) {
        // ── Content Area ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Floating mascot
            Image(
                painter = painterResource(R.drawable.masot),
                contentDescription = "Oh My Guide mascot",
                modifier = Modifier
                    .size(180.dp)
                    .offset { IntOffset(0, -floatOffset.dp.roundToPx()) },
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Annyeong! \uD83D\uDC4B",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "I'm your Korea guide",
                style = MaterialTheme.typography.headlineMedium,
                color = Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Find amazing places,\nnavigate like a local,\nand get personalized guides.",
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

        }

        // ── Bottom CTA ──
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Google Sign-in button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Primary.copy(alpha = 0.35f),
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryGradient)
                    .clickable(onClick = onSignIn)
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Google icon circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BgWhite),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "G", color = Primary, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleLarge,
                    color = BgWhite,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "One-tap sign in to get your personalized guide",
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    OhMyGuideTheme {
        WelcomeScreen(onSignIn = {})
    }
}
