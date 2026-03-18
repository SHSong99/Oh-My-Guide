package com.ohmyguide.app.ui.screen.phrases

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.KoreanPhrase
import com.ohmyguide.app.fixtures.PHRASE_SECTIONS
import com.ohmyguide.app.fixtures.PhraseSection
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

sealed class PhrasesUiState {
    object Loading : PhrasesUiState()
    object Idle : PhrasesUiState()
    data class Error(val message: String) : PhrasesUiState()
}

@Composable
fun PhrasesScreen(navController: NavController) {
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var savedPhrases by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        // ── Header ──
        PhrasesHeader(savedCount = savedPhrases.size)

        // ── Mascot tip ──
        MascotTip()

        // ── Sections list ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF8FAFF))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(PHRASE_SECTIONS) { _, section ->
                val isOpen = expandedSection == section.title || expandedSection == null
                PhraseSectionCard(
                    section = section,
                    isOpen = isOpen,
                    savedPhrases = savedPhrases,
                    onToggle = {
                        expandedSection = if (expandedSection == section.title) null else section.title
                    },
                    onSaveToggle = { key ->
                        savedPhrases = if (key in savedPhrases) savedPhrases - key else savedPhrases + key
                    },
                )
            }
        }

        // ── Bottom Nav ──
        BottomNavBar(
            activeTab = "phrases",
            onTabChange = { tab ->
                when (tab) {
                    "main" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                    "explore" -> navController.navigate(Screen.Explore.route)
                }
            },
        )
    }
}

// ── Header ──

@Composable
private fun PhrasesHeader(savedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "\uD55C\uAD6D\uC5B4 \uAD6C\uBB38",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Text(
                text = "Korean Phrases for Travelers",
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }
        if (savedCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF9C4))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFB8960C))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$savedCount saved",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFB8960C),
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderLight),
    )
}

// ── Mascot tip ──


@Composable
private fun MascotTip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(PrimaryBg, Color(0xFFFAFBFF)))
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.masot),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(2.dp, Primary, CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp, topEnd = 14.dp,
                        bottomStart = 14.dp, bottomEnd = 14.dp,
                    )
                )
                .background(BgWhite)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Tap the bookmark to save phrases for quick access offline!",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderLight),
    )
}

// ── Phrase section card ──

@Composable
private fun PhraseSectionCard(
    section: PhraseSection,
    isOpen: Boolean,
    savedPhrases: Set<String>,
    onToggle: () -> Unit,
    onSaveToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite),
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(section.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = section.emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${section.phrases.size} phrases",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = section.color,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextCaption,
                )
            }
        }

        // Phrases
        AnimatedVisibility(visible = isOpen) {
            Column {
                if (isOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(section.color.copy(alpha = 0.12f)),
                    )
                }
                section.phrases.forEachIndexed { index, phrase ->
                    val key = "${section.title}-$index"
                    val isSaved = key in savedPhrases
                    PhraseRow(
                        phrase = phrase,
                        sectionColor = section.color,
                        isSaved = isSaved,
                        onSaveToggle = { onSaveToggle(key) },
                        showDivider = index < section.phrases.size - 1,
                    )
                }
            }
        }
    }
}

// ── Phrase row ──

@Composable
private fun PhraseRow(
    phrase: KoreanPhrase,
    sectionColor: Color,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSaved) Color(0xFFFFFDE7) else BgWhite)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phrase.kr,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = phrase.pron,
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = sectionColor,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = phrase.en,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Icon(
            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onSaveToggle)
                .padding(4.dp),
            tint = if (isSaved) Star else TextCaption,
        )
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(Color(0xFFF8F9FB)),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PhrasesScreenPreview() {
    OhMyGuideTheme {
        PhrasesScreen(rememberNavController())
    }
}