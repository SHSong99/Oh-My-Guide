package com.ohmyguide.app.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (showAvatar) {
            Image(
                painter = painterResource(R.drawable.face),
                contentDescription = "Guide",
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            Spacer(modifier = Modifier.width(36.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp, topEnd = 18.dp,
                        bottomStart = 18.dp, bottomEnd = 18.dp,
                    )
                )
                .background(BgWhite)
                .border(
                    1.dp, Border,
                    RoundedCornerShape(
                        topStart = 4.dp, topEnd = 18.dp,
                        bottomStart = 18.dp, bottomEnd = 18.dp,
                    )
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            BouncingDots()
        }
    }
}

@Composable
private fun BouncingDots() {
    val transition = rememberInfiniteTransition(label = "typing")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = index * 150,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset { IntOffset(0, offsetY.dp.roundToPx()) }
                    .clip(CircleShape)
                    .background(TextCaption),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TypingIndicatorPreview() {
    OhMyGuideTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TypingIndicator()
        }
    }
}