package com.ohmyguide.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.InfoGreen
import com.ohmyguide.app.ui.theme.InfoGreenBg
import com.ohmyguide.app.ui.theme.InfoPurple
import com.ohmyguide.app.ui.theme.InfoPurpleBg
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
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
                contentDescription = label ?: value,
                modifier = Modifier.size(18.dp),
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

@Preview(showBackground = true)
@Composable
private fun InfoCardWithLabelPreview() {
    OhMyGuideTheme {
        InfoCard(
            icon = Icons.Filled.AccessTime,
            iconTint = InfoPurple,
            label = "Hours",
            value = "09:00-18:00",
            bgColor = InfoPurpleBg,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoCardWithoutLabelPreview() {
    OhMyGuideTheme {
        InfoCard(
            icon = Icons.Filled.AttachMoney,
            iconTint = InfoGreen,
            value = "Free",
            bgColor = InfoGreenBg,
        )
    }
}
