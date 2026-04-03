package com.ohmyguide.app.ui.screen.navi

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.InfoBlue
import com.ohmyguide.app.ui.theme.InfoBlueBg
import com.ohmyguide.app.ui.theme.AppLanguage
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

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

// ── Transit Guide Card (Board / Alight) ──

@Composable
fun TransitGuideCard(info: TransitGuideInfo) {
    val isBoard = info.type == "board"
    val isBus = info.transitType == "bus"
    val accentColor = if (isBus) Primary else InfoBlue
    val bgColor = if (isBus) PrimaryBg else InfoBlueBg
    val icon = if (isBus) Icons.Filled.DirectionsBus else Icons.Filled.DirectionsBus // subway uses same
    val isKorean = LanguageManager.current.value == AppLanguage.KO
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(16.dp),
    ) {
        // Header: Board / Alight badge + line name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (isBoard) {
                        if (isKorean) "승차" else "BOARD"
                    } else {
                        if (isKorean) "하차" else "GET OFF"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = accentColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = info.lineName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isBoard) {
            // Board: station + where to get off
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor),
                    )
                    Box(
                        modifier = Modifier.width(2.dp).height(28.dp).background(Border),
                    )
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).border(2.dp, accentColor, CircleShape),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    if (isKorean) {
                        Text(
                            text = info.stationName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                        )
                    } else {
                        Text(
                            text = info.stationNameEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                        )
                        if (info.stationName != info.stationNameEn) {
                            Text(
                                text = info.stationName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCaption,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${info.stopsCount} ${strings.stopsUnit} \u2192",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isKorean) {
                        Text(
                            text = info.exitStation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor,
                        )
                    } else {
                        Text(
                            text = info.exitStationEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor,
                        )
                        if (info.exitStation != info.exitStationEn) {
                            Text(
                                text = info.exitStation,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCaption,
                            )
                        }
                    }
                }
            }
        } else {
            // Alight: exit station
            val stationDisplay = if (isKorean) info.stationName else info.stationNameEn
            Text(
                text = "\uD83D\uDCCD ${strings.getOffAt} $stationDisplay",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            if (!isKorean && info.stationName != info.stationNameEn) {
                Text(
                    text = info.stationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption,
                )
            }
            if (info.stopsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${info.stopsCount} ${strings.stopsUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCaption,
                )
            }
        }
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
                text = detail.place.emoji.ifEmpty { "\uD83D\uDCCD" },
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
            text = "\u261D\uFE0F",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset(y = bounceY.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = LocalStrings.current.storyAboutPlace.replace("%s", placeName),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = LocalStrings.current.storyPromptHint,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
