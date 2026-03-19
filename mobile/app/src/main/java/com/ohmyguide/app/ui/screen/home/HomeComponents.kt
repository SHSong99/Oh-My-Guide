package com.ohmyguide.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.Success
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun HomeHeader(onReset: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite.copy(alpha = 0.8f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Image(
                painter = painterResource(R.drawable.masot),
                contentDescription = "Guide",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary, CircleShape)
                    .background(PrimaryBg),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(BgWhite)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Success),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Oh My Guide",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = "Curating spots...",
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BgSub)
                .clickable(onClick = onReset)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextCaption)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reset",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }
}

@Composable
fun LocationBar(spotCount: Int, locationName: String = "your area") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Near $locationName",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
        }
        Text(
            text = "$spotCount spots",
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(Border),
    )
}

@Composable
fun RecommendationBlock(
    section: RecommendationSection,
    onPlaceClick: (String) -> Unit,
    onShowMore: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(section.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = section.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (section.label == "Big Data") TextCaption else Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (section.label == "Big Data") BgSub else PrimaryBgLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(section.places) { place ->
                PlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place.id) },
                )
            }
        }

        if (onShowMore != null && section.btnText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .clickable(onClick = onShowMore)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (section.title.contains("Picks")) Icons.Filled.Favorite else Icons.Filled.Whatshot,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = section.btnText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Primary,
                )
            }
        }
    }
}

@Composable
fun FindOtherPlacesButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Secondary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Find other places",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
    }
}

// ── Place Detail Sheet ──

@Composable
fun PlaceDetailSheet(
    detail: PlaceDetail,
    onBack: () -> Unit,
    onGoHere: (String) -> Unit,
) {
    val place = detail.place

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // Back button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = TextSecondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Back to list",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Place name & rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.nameKr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBgLight)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Star)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${place.rating}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tag & distance
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${place.tag}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryBgLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextCaption)
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = place.distance,
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = detail.desc,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoChip(
                icon = Icons.Filled.AccessTime,
                label = detail.hours,
                modifier = Modifier.weight(1f),
            )
            InfoChip(
                icon = Icons.Filled.ConfirmationNumber,
                label = detail.fee,
                modifier = Modifier.weight(1f),
            )
            InfoChip(
                icon = Icons.Filled.DirectionsWalk,
                label = detail.walkTime,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Go here button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(100.dp), ambientColor = Primary.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(100.dp))
                .background(PrimaryGradient)
                .clickable { onGoHere(place.id) }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(20.dp), tint = BgWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Go here",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .background(BgWhite)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
        )
    }
}

// ── Chat Option Buttons ──

@Composable
fun ChatOptionButtons(
    options: List<String>,
    answered: Boolean,
    selectedOption: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 46.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val bgColor = when {
                isSelected -> PrimaryGradient
                answered -> androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(BgSub, BgSub),
                )
                else -> PrimaryGradient
            }
            val textColor = when {
                isSelected -> BgWhite
                answered -> TextCaption
                else -> BgWhite
            }

            Text(
                text = option,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(bgColor)
                    .then(
                        if (!answered) {
                            Modifier.clickable { onSelect(option) }
                        } else Modifier
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
