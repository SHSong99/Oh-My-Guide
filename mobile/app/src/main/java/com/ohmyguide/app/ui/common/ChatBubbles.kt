package com.ohmyguide.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextPrimary

@Composable
fun GuideBubble(
    text: String,
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
                .padding(14.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
    }
}

@Composable
fun UserBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp, topEnd = 18.dp,
                        bottomStart = 18.dp, bottomEnd = 4.dp,
                    )
                )
                .background(PrimaryGradient)
                .padding(12.dp, 10.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = BgWhite,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatBubblesPreview() {
    OhMyGuideTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GuideBubble(text = "Nice to meet you! What language do you prefer?")
            UserBubble(text = "English")
        }
    }
}