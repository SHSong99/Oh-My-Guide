package com.ohmyguide.app.ui.screen.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ohmyguide.app.domain.model.PhraseBookmarkStore
import com.ohmyguide.app.fixtures.KoreanPhrase
import com.ohmyguide.app.fixtures.Place
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.InfoBlueBg
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

// ── Phrases Dashboard ──

@Composable
fun PhrasesDashboard(
    items: List<PhraseItem>,
    onSpeak: (String) -> Unit,
    speakingText: String? = null,
    isSpeaking: Boolean = false,
    isLoading: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { phrase ->
            val isThisPlaying = speakingText == phrase.korean && isSpeaking
            val isThisLoading = speakingText == phrase.korean && isLoading
            PhraseCard(
                phrase = phrase,
                onSpeak = onSpeak,
                isPlaying = isThisPlaying,
                isLoading = isThisLoading,
            )
        }
    }
}

@Composable
private fun PhraseCard(
    phrase: PhraseItem,
    onSpeak: (String) -> Unit,
    isPlaying: Boolean = false,
    isLoading: Boolean = false,
) {
    val key = "navi-${phrase.korean}"
    val bookmarkMap by PhraseBookmarkStore.bookmarks.collectAsState()
    val bookmarked = bookmarkMap.containsKey(key)

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
                .clickable {
                    PhraseBookmarkStore.toggle(
                        key,
                        KoreanPhrase(phrase.korean, phrase.romanization, phrase.english),
                        "Navi Phrases",
                    )
                },
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
                .clickable(enabled = !isLoading) { onSpeak(phrase.korean) },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BgWhite,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(20.dp),
                    tint = BgWhite,
                )
            }
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
            Text(text = place.emoji.ifEmpty { "\uD83D\uDCCD" }, fontSize = 32.sp)
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
                        text = "${info.temperature.toInt()}\u00B0",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = mainText,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Feels ${info.feelsLike.toInt()}\u00B0",
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
                emoji = "\uD83D\uDCA8",
                label = "%.1f m/s".format(info.windSpeed),
                chipBg = chipBg,
                textColor = mainText,
                modifier = Modifier.weight(1f),
            )
            WeatherInfoChip(
                emoji = "\uD83D\uDCA7",
                label = "${info.precipProbability}%",
                chipBg = chipBg,
                textColor = mainText,
                modifier = Modifier.weight(1f),
            )
            WeatherInfoChip(
                emoji = if (info.isDay) "\uD83C\uDF05" else "\uD83C\uDF03",
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
                            text = "${h.temp.toInt()}\u00B0",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = mainText,
                        )
                        if (h.precipProb > 0) {
                            Text(
                                text = "\uD83D\uDCA7${h.precipProb}%",
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
                Text(text = "\uD83D\uDCA1", style = MaterialTheme.typography.bodyMedium)
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
        // Image area - full width
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
                    "\uD83D\uDCCD",
                    fontSize = 48.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            // More button - top right overlay
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
                text = spot.overview?.take(60)?.plus("\u2026") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
