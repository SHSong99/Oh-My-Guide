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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.R
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.fixtures.KoreanPhrase
import com.ohmyguide.app.fixtures.PhraseSection
import com.ohmyguide.app.ui.theme.BgDivider
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.ContentBgTop
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.SavedBg
import com.ohmyguide.app.ui.theme.SavedHighlight
import com.ohmyguide.app.ui.theme.SavedText
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun PhrasesHeader(savedCount: Int) {
    val strings = LocalStrings.current
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
                text = strings.koreanPhrasesTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Text(
                text = strings.koreanPhrasesSubtitle,
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }
        if (savedCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SavedBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = SavedText)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$savedCount ${strings.saved}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SavedText,
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

@Composable
fun MascotTip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(PrimaryBg, ContentBgTop))
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.face),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            contentScale = ContentScale.Fit,
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
                text = LocalStrings.current.bookmarkHint,
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

@Composable
fun PhraseSectionCard(
    section: PhraseSection,
    isOpen: Boolean,
    savedPhrases: Set<String>,
    onToggle: () -> Unit,
    onSaveToggle: (String, KoreanPhrase) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite),
    ) {
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
                    text = "${section.phrases.size} ${LocalStrings.current.phrasesUnit}",
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
                        onSaveToggle = { onSaveToggle(key, phrase) },
                        showDivider = index < section.phrases.size - 1,
                    )
                }
            }
        }
    }
}

@Composable
fun PhraseRow(
    phrase: KoreanPhrase,
    sectionColor: Color,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSaved) SavedHighlight else BgWhite)
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
                .background(BgDivider),
        )
    }
}
