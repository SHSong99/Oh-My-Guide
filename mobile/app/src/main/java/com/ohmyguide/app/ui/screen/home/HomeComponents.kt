package com.ohmyguide.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.PlaceDetail
import com.ohmyguide.app.fixtures.RecommendationSection
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.LocalStrings
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
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite.copy(alpha = 0.8f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Image(
                painter = painterResource(R.drawable.face),
                contentDescription = "Guide",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit,
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
                text = strings.appName,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = strings.curatingSpots,
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
                text = strings.reset,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
            )
        }
    }
}

@Composable
fun LocationBar(spotCount: Int, locationName: String = "") {
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
                text = locationName.ifEmpty { LocalStrings.current.yourArea },
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
        }
        Text(
            text = "$spotCount ${LocalStrings.current.spots}",
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
                text = LocalStrings.current.findOtherPlaces,
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

    // 텍스트 스크롤이 시트를 당기지 않도록 nestedScroll 차단
    val consumeNestedScroll = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource,
            ) = available.copy(x = 0f) // 세로 스크롤 소비하여 시트 전파 차단
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize(),
    ) {
        // 드래그 핸들 영역 — 이 부분으로만 시트를 당길 수 있음
        // (BottomSheetScaffold의 dragHandle이 이미 처리)

        // Hero image (스크롤 밖, 고정)
        if (place.imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            ) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            )
                        ),
                )
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                )
            }
        }

        // Back button (고정)
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
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
                text = LocalStrings.current.backToList,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }

        // 텍스트 영역만 독립 스크롤 (시트 당기기 차단)
        Column(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(consumeNestedScroll)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
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
        }

        // Go here button — fixed at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
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
                        text = LocalStrings.current.goHere,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
        }
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

// ── Chat Option Buttons (Numbered Selection) ──

@Composable
fun ChatOptionButtons(
    options: List<String>,
    answered: Boolean,
    selectedOption: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 46.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, option ->
            val number = index + 1
            val isSelected = option == selectedOption
            NumberedOptionCard(
                number = number,
                text = option,
                isSelected = isSelected,
                enabled = !answered,
                onClick = { onSelect(option) },
            )
        }

        // "Other" option
        val otherSelected = answered && selectedOption != null && selectedOption !in options
        val otherNumber = options.size + 1
        NumberedOptionCard(
            number = otherNumber,
            text = strings.findOtherPlaces,
            isSelected = otherSelected,
            enabled = !answered,
            onClick = { onSelect("__OTHER__") },
        )
    }
}

@Composable
private fun NumberedOptionCard(
    number: Int,
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PrimaryBg else BgWhite)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else Border,
                shape = RoundedCornerShape(14.dp),
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isSelected) Primary else BgSub),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) BgWhite else TextCaption,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (isSelected) Primary else if (enabled) TextPrimary else TextCaption,
        )
    }
}

// ── Chat Text Input (for "Other" option) ──

@Composable
fun ChatTextInput(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 46.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = LocalStrings.current.categoryPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { if (text.isNotBlank()) onSubmit(text.trim()) },
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) PrimaryGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(BgSub, BgSub)))
                .then(
                    if (text.isNotBlank()) Modifier.clickable { onSubmit(text.trim()) }
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (text.isNotBlank()) BgWhite else TextCaption,
            )
        }
    }
}
