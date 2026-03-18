package com.ohmyguide.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

@Composable
fun InfoCard(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    bgColor: Color = iconTint.copy(alpha = 0.1f),
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (label != null) 16.dp else 12.dp))
            .background(BgSub)
            .padding(if (label != null) 16.dp else 12.dp),
        horizontalAlignment = if (label != null) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(if (label != null) 32.dp else 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(if (label != null) 18.dp else 18.dp),
                tint = iconTint,
            )
        }
        Spacer(modifier = Modifier.height(if (label != null) 10.dp else 4.dp))
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = value,
            style = if (label != null) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            maxLines = 1,
        )
    }
}
