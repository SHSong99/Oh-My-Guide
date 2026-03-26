package com.ohmyguide.app.ui.screen.navi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.Error
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.InfoBlueBg
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary
import coil.compose.AsyncImage

// ── Map Top Navigation Bar ──

@Composable
fun MapNavBar(
    distance: String,
    eta: String,
    placeName: String,
    progressPct: Float,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Straight",
                    modifier = Modifier.size(32.dp),
                    tint = BgWhite,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = distance,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
                Text(
                    text = "$eta · $placeName",
                    style = MaterialTheme.typography.bodySmall,
                    color = BgWhite.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BgWhite.copy(alpha = 0.2f))
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Stop", modifier = Modifier.size(18.dp), tint = BgWhite)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { progressPct },
                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = BgWhite,
                trackColor = BgWhite.copy(alpha = 0.3f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(progressPct * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = BgWhite.copy(alpha = 0.8f),
            )
        }
    }
}

// ── Sheet Header (action buttons) ──

@Composable
fun NaviSheetHeader(
    onStop: () -> Unit,
    onStory: () -> Unit = {},
    onPhrases: () -> Unit = {},
    storyHighlight: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Primary.copy(alpha = 0.1f))
                .clickable(onClick = onStory)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Headphones,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Listen to Place Guide",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(BgSub)
                .clickable(onClick = onPhrases)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Translate,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Korean Phrases",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(BorderLight),
    )
}

// ── Story Wave Button (TTS style with animated waves) ──

@Composable
private fun StoryWaveButton(onClick: () -> Unit, highlight: Boolean = false) {
    // Pulse scale when highlighted
    val pulseScale = if (highlight) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseScale",
        )
        scale
    } else 1f
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave1",
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave2",
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave3",
    )

    Row(
        modifier = Modifier
            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
            .then(
                if (highlight) Modifier.shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Primary)
                else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryGradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Headphones,
            contentDescription = "Story",
            modifier = Modifier.size(14.dp),
            tint = BgWhite,
        )
        Spacer(modifier = Modifier.width(4.dp))
        // Animated wave bars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            listOf(wave1, wave2, wave3, wave1 * 0.7f).forEach { height ->
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height((12 * height).dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(BgWhite.copy(alpha = 0.8f)),
                )
            }
        }
    }
}

// ── Kkaebi Header ──

@Composable
fun KkaebiHeader(
    onStory: (() -> Unit)? = null,
    onPhrases: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.masot),
            contentDescription = "Kkaebi",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(2.dp, Primary, CircleShape)
                .background(PrimaryBg),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Kkaebi",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (onStory != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryGradient)
                    .clickable(onClick = onStory)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Headphones, contentDescription = null, modifier = Modifier.size(14.dp), tint = BgWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Story",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
            }
        }
        if (onPhrases != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgSub)
                    .clickable(onClick = onPhrases)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Phrases",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
            }
        }
    }
}

// ── Kkaebi Label (same as header, for new turns) ──

@Composable
fun KkaebiLabel() {
    KkaebiHeader()
}

// ── Animated Message Item ──

@Composable
fun AnimatedMessageItem(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(animationSpec = tween(300)),
    ) {
        content()
    }
}

// ── Transit Info Card ──

@Composable
fun TransitInfoCard(info: TransitStopInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = info.busNumber,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(Border),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .border(2.dp, Secondary, CircleShape),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = info.stopName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${info.remainingStops} ${LocalStrings.current.stopsUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCaption,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = info.exitStopName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Secondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${LocalStrings.current.getOffAt} ${info.exitStopName}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Primary,
        )
    }
}

// ── Destination Detail Card ──

@Composable
fun DestinationDetailCard(
    detail: PlaceDetail,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .clickable(onClick = onClick),
    ) {
        // Image area with play overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(BgSub),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = detail.place.emoji.ifEmpty { "📍" },
                fontSize = 48.sp,
            )
            // Play overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Listen to guide",
                    modifier = Modifier.size(22.dp),
                    tint = BgWhite,
                )
            }
        }
        // Detail info
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = detail.place.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Text(
                text = detail.place.nameKr,
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = detail.desc,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = TextCaption,
                maxLines = 3,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DetailChip(icon = Icons.Filled.AccessTime, text = detail.hours)
                DetailChip(icon = Icons.Filled.LocalOffer, text = detail.fee)
            }
        }
    }
}

@Composable
private fun DetailChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextCaption)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption,
        )
    }
}

// ── Bot Bubble (no avatar, left-aligned) ──

@Composable
fun NaviBotBubble(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = TextPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .background(BgSub)
                .padding(14.dp),
        )
    }
}

// ── POI Hero Card ──

@Composable
fun PoiHeroCard(emoji: String, name: String, nameKr: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BgSub),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(BgSub),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 48.sp)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(text = nameKr, style = MaterialTheme.typography.labelMedium, color = TextCaption)
            }
        }
    }
}

// ── Quick Action Buttons (always at bottom) ──

@Composable
fun NaviQuickActions(
    onStory: () -> Unit,
    onPhrases: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryGradient)
                .clickable(onClick = onStory)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Headphones, contentDescription = null, modifier = Modifier.size(18.dp), tint = BgWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.listenToStory,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = BgWhite,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSub)
                .clickable(onClick = onPhrases)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.navPhrases,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
    }
}

// ── Phrases Dashboard ──

@Composable
fun PhrasesDashboard(items: List<PhraseItem>, onSpeak: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { phrase ->
            PhraseCard(phrase = phrase, onSpeak = onSpeak)
        }
    }
}

@Composable
private fun PhraseCard(phrase: PhraseItem, onSpeak: (String) -> Unit) {
    var bookmarked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phrase.korean,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phrase.romanization,
                style = MaterialTheme.typography.bodyLarge,
                color = Primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phrase.english,
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Bookmark button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (bookmarked) Primary.copy(alpha = 0.1f) else BgSub)
                .clickable { bookmarked = !bookmarked },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (bookmarked) Icons.Filled.CheckCircle else Icons.Filled.LocalOffer,
                contentDescription = "Bookmark",
                modifier = Modifier.size(18.dp),
                tint = if (bookmarked) Primary else TextCaption,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // TTS play button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryGradient)
                .clickable { onSpeak(phrase.korean) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(20.dp),
                tint = BgWhite,
            )
        }
    }
}

// ── Nearby Place Carousel ──

@Composable
fun NearbyPlaceCarousel(
    places: List<Place>,
    onPlaceClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(places) { place ->
            NearbyPlaceCard(
                place = place,
                description = NEARBY_DESCRIPTIONS[place.id] ?: "A great spot to explore nearby",
                onClick = { onPlaceClick(place.id) },
            )
        }
    }
}

@Composable
private fun NearbyPlaceCard(
    place: Place,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(BgSub),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = place.emoji.ifEmpty { "📍" }, fontSize = 32.sp)
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
                maxLines = 2,
            )
        }
    }
}

private val NEARBY_DESCRIPTIONS = mapOf(
    "dm3" to "Famous traditional market with street food",
    "dm4" to "Beautiful hanok village with scenic views",
    "dm5" to "Iconic tower with panoramic city views",
    "dm6" to "Trendy alley with cafes and restaurants",
    "dm7" to "Peaceful urban stream for a relaxing walk",
)

// ── Arrival Confirm ──

@Composable
fun ArrivalConfirmButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(100.dp), ambientColor = Primary.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(100.dp))
            .background(Secondary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.iveArrived,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }
    }
}

// ── Nearby Recommendations ──

@Composable
fun NearbyPlaceCards(
    places: List<Place>,
    onPlaceClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(places) { place ->
            NearbyPlaceCard(
                place = place,
                description = NEARBY_DESCRIPTIONS[place.id] ?: "A great spot to explore nearby",
                onClick = { onPlaceClick(place.id) },
            )
        }
    }
}

// ── Story Prompt Bubble (with bouncing arrow) ──

@Composable
fun StoryPromptBubble(placeName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceArrow",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryBg)
            .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        // Bouncing arrow pointing up
        Text(
            text = "☝️",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset(y = bounceY.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "I have a story about $placeName!",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Tap the 🎧 Story button above to listen while you walk.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

// ── Weather Card ──

@Composable
fun WeatherCard(info: WeatherInfo) {
    val cardBg = if (info.isDay) InfoBlueBg else Color(0xFF1A1A2E)
    val cardBorder = if (info.isDay) InfoBlue.copy(alpha = 0.15f) else Color(0xFF2A3A52)
    val chipBg = if (info.isDay) BgWhite.copy(alpha = 0.6f) else Color(0xFF2A3A52)
    val mainText = if (info.isDay) TextPrimary else Color(0xFFE8ECF4)
    val subText = if (info.isDay) TextSecondary else Color(0xFF8892A4)
    val accentColor = if (info.isDay) InfoBlue else Color(0xFF7CB3FF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        // Current weather header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Big emoji with subtle background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(chipBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = info.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.weatherDesc,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = mainText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${info.temperature.toInt()}°",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = mainText,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Feels ${info.feelsLike.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = subText,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeatherInfoChip(
                emoji = "💨",
                label = "%.1f m/s".format(info.windSpeed),
                chipBg = chipBg,
                textColor = mainText,
                modifier = Modifier.weight(1f),
            )
            WeatherInfoChip(
                emoji = "💧",
                label = "${info.precipProbability}%",
                chipBg = chipBg,
                textColor = mainText,
                modifier = Modifier.weight(1f),
            )
            WeatherInfoChip(
                emoji = if (info.isDay) "🌅" else "🌃",
                label = if (info.isDay) "Day" else "Night",
                chipBg = chipBg,
                textColor = mainText,
                modifier = Modifier.weight(1f),
            )
        }

        // Hourly forecast
        if (info.hourlyForecast.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(chipBg)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                info.hourlyForecast.forEach { h ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%02d:00".format(h.hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = subText,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = h.emoji,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${h.temp.toInt()}°",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = mainText,
                        )
                        if (h.precipProb > 0) {
                            Text(
                                text = "💧${h.precipProb}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                            )
                        }
                    }
                }
            }
        }

        // Tip bubble
        if (info.tip.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(text = "💡", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = info.tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                )
            }
        }
    }
}

@Composable
private fun WeatherInfoChip(
    emoji: String,
    label: String,
    chipBg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

// ── Nearby Spot Dashboard Card ──

@Composable
fun NearbySpotDashboard(spot: NearbySpotInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        // Image area — full width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(BgSub),
        ) {
            if (!spot.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = spot.imageUrl,
                    contentDescription = spot.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    "📍",
                    fontSize = 48.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            // More button — top right overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgWhite.copy(alpha = 0.85f))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary,
                    )
                }
            }
        }
        // Title + description
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = spot.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spot.overview?.take(60)?.plus("…") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
