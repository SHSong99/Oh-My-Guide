package com.ohmyguide.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary

@Composable
fun MascotAvatar(
    size: Int = 36,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .then(
                if (showBorder) Modifier.background(Primary.copy(alpha = 0.1f))
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun MascotAvatarPreview() {
    OhMyGuideTheme {
        MascotAvatar {
            Text(text = "\uD83D\uDC3B")
        }
    }
}
