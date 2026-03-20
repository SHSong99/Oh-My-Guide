package com.ohmyguide.app.ui.screen.navi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

// ── Sheet Header (unified: place info + progress + stop) ──

@Composable
fun NaviSheetHeader(
    placeName: String,
    placeNameKr: String,
    distance: String,
    eta: String,
    modeLabel: String,
    progressPct: Float,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = modeLabel, style = MaterialTheme.typography.labelSmall, color = TextCaption)
                Text(text = placeName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = placeNameKr, style = MaterialTheme.typography.labelSmall, color = TextCaption)
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text(text = distance, style = MaterialTheme.typography.headlineSmall, color = Primary)
                Text(text = eta, style = MaterialTheme.typography.labelSmall, color = TextCaption)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(BgSub)
                    .clickable(onClick = onStop)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextCaption)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = LocalStrings.current.stop,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextCaption,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { progressPct },
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Primary,
                trackColor = Border,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(progressPct * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(BorderLight),
    )
}

// ── Kkaebi Header ──

@Composable
fun KkaebiHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
            text = "Get off at ${info.exitStopName}",
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
                text = "Listen to Story",
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
                text = "Phrases",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
    }
}

// ── Phrases Dashboard ──

@Composable
fun PhrasesDashboard(items: List<PhraseItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { phrase ->
            PhraseCard(phrase = phrase)
        }
    }
}

@Composable
private fun PhraseCard(phrase: PhraseItem) {
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
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryGradient)
                .clickable { /* TODO: TTS playback */ },
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
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.08f))
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
