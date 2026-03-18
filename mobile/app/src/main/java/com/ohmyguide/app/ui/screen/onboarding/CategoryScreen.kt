package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.fixtures.CATEGORIES
import com.ohmyguide.app.fixtures.Category
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.PrimaryButton
import com.ohmyguide.app.ui.common.PrimaryGradient
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderCategory
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.PrimaryBgChat
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

@Composable
fun CategoryScreen(
    onConfirm: (List<String>) -> Unit,
) {
    val selected = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PrimaryBgChat, BgWhite))
            ),
    ) {
        // Chat + Grid area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
        ) {
            GuideBubble(text = "Hey! I'm Oh My Guide\nI'll find the perfect spots near you!")
            Spacer(modifier = Modifier.height(4.dp))
            GuideBubble(text = "Just tell me what you're into", showAvatar = false)
            Spacer(modifier = Modifier.height(12.dp))

            // Category grid (inline, not lazy)
            CategoryGrid(
                selected = selected,
                onToggle = { id ->
                    if (selected.contains(id)) selected.remove(id) else selected.add(id)
                },
            )
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = when (selected.size) {
                    0 -> "Choose what interests you!"
                    1 -> "Great pick! Add more or let's go!"
                    else -> "Awesome combo! ${selected.size} selected"
                },
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PrimaryButton(
                text = if (selected.isEmpty()) "Select at least one" else "Find My Perfect Spots!",
                onClick = { onConfirm(selected.toList()) },
                enabled = selected.isNotEmpty(),
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    selected: List<String>,
    onToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 46.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CATEGORIES.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { cat ->
                    CategoryCard(
                        category = cat,
                        isSelected = selected.contains(cat.id),
                        onClick = { onToggle(cat.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Border,
        label = "border",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) category.color.copy(alpha = 0.05f) else BgWhite,
        label = "bg",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) category.color.copy(alpha = 0.15f)
                        else category.color.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = category.emoji)
            }
            // Check circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(PrimaryGradient)
                        else Modifier.border(2.dp, BorderCategory, CircleShape)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = BgWhite)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) category.color else TextPrimary,
        )
        Text(
            text = category.sub,
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoryScreenPreview() {
    OhMyGuideTheme {
        CategoryScreen(onConfirm = {})
    }
}