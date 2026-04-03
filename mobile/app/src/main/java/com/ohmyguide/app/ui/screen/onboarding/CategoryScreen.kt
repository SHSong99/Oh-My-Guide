package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.NaverGeocodingApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.fixtures.CATEGORIES
import com.ohmyguide.app.fixtures.Category
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.TypingIndicator
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.BorderCategory
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBgChat
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CategoryScreenEntryPoint {
    fun naverGeocodingApi(): NaverGeocodingApi
}

private enum class ChatStep { GPS, MSG1, MSG2, CARDS, SENT, DONE }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryScreen(
    onConfirm: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val selected = remember { mutableStateListOf<String>() }
    var step by remember { mutableStateOf(ChatStep.GPS) }
    var locationName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val locationData by LocationForegroundService.locationFlow.collectAsState()

    // GPS 좌표 → Naver Reverse Geocoding으로 주소 변환
    val naverGeoApi = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            CategoryScreenEntryPoint::class.java,
        ).naverGeocodingApi()
    }
    LaunchedEffect(locationData) {
        if (locationName.isNotEmpty()) return@LaunchedEffect
        val loc = locationData ?: return@LaunchedEffect
        try {
            val response = naverGeoApi.reverseGeocode(
                clientId = BuildConfig.NAVER_MAP_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_MAP_CLIENT_SECRET,
                coords = "${loc.longitude},${loc.latitude}",
            )
            val region = response.results?.firstOrNull()?.region
            val city = region?.area1?.name ?: ""
            val district = region?.area2?.name ?: ""
            locationName = when {
                district.isNotEmpty() && city.isNotEmpty() -> "$district, $city"
                city.isNotEmpty() -> city
                else -> strings.yourLocation
            }
        } catch (_: Exception) {
            // API 실패 시 무시, 타이머 폴백으로 처리
        }
    }

    // 채팅 애니메이션 (한 번만 실행)
    LaunchedEffect(Unit) {
        delay(1800L)
        if (locationName.isEmpty()) locationName = strings.yourLocation
        step = ChatStep.MSG1
        delay(600L)
        step = ChatStep.MSG2
        delay(800L)
        step = ChatStep.CARDS
    }

    // Auto-scroll
    LaunchedEffect(step, selected.size) {
        delay(100L)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PrimaryBgChat, BgWhite))
            ),
    ) {
        // Chat area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 12.dp),
        ) {
            // GPS detection
            AnimatedVisibility(visible = step == ChatStep.GPS) {
                Row(
                    modifier = Modifier
                        .padding(start = 46.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgWhite)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.detectingLocation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // Location badge
            AnimatedVisibility(visible = locationName.isNotEmpty() && step >= ChatStep.MSG1) {
                Row(
                    modifier = Modifier
                        .padding(start = 46.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgWhite)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                }
            }

            // Bot message 1
            AnimatedVisibility(
                visible = step >= ChatStep.MSG1,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                GuideBubble(text = strings.categoryGreeting)
            }

            if (step == ChatStep.MSG1) {
                TypingIndicator(showAvatar = false)
            }

            // Bot message 2
            AnimatedVisibility(
                visible = step >= ChatStep.MSG2,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                GuideBubble(text = strings.categoryPrompt, showAvatar = false)
            }

            if (step == ChatStep.MSG2) {
                TypingIndicator(showAvatar = false)
            }

            // Category cards — left-aligned white cards (like GpsPermissionScreen options)
            AnimatedVisibility(
                visible = step >= ChatStep.CARDS,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                Column(
                    modifier = Modifier.padding(start = 46.dp, bottom = 12.dp),
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
                                    onClick = {
                                        if (step == ChatStep.CARDS) {
                                            if (selected.contains(cat.id)) selected.remove(cat.id)
                                            else selected.add(cat.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // After send: user bubble + bot response
            if (step >= ChatStep.SENT) {
                Spacer(modifier = Modifier.height(8.dp))
                val names = selected.mapNotNull { id ->
                    strings.categoryNames[id] ?: CATEGORIES.find { it.id == id }?.name
                }
                UserBubble(text = names.joinToString(", "))

                if (step == ChatStep.SENT) {
                    TypingIndicator(showAvatar = true)
                }
            }

            if (step == ChatStep.DONE) {
                GuideBubble(text = strings.categoryDone)
            }
        }

        // Bottom input bar with chips + send
        if (step == ChatStep.CARDS) {
            CategoryInputBar(
                selected = selected,
                onRemove = { id -> selected.remove(id) },
                onSend = {
                    if (selected.isNotEmpty()) {
                        step = ChatStep.SENT
                    }
                },
            )
        }
    }

    // Send → typing → done
    LaunchedEffect(step) {
        if (step == ChatStep.SENT) {
            delay(1500L)
            step = ChatStep.DONE
        } else if (step == ChatStep.DONE) {
            delay(1200L)
            onConfirm(selected.toList())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryInputBar(
    selected: List<String>,
    onRemove: (String) -> Unit,
    onSend: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        if (selected.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                selected.forEach { id ->
                    val cat = CATEGORIES.find { it.id == id } ?: return@forEach
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(cat.color.copy(alpha = 0.1f))
                            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val catName = LocalStrings.current.categoryNames[cat.id] ?: cat.name
                        Text(
                            text = "${cat.emoji} $catName",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = cat.color,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = LocalStrings.current.remove,
                            modifier = Modifier.size(16.dp).clip(CircleShape).clickable { onRemove(id) },
                            tint = cat.color,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (selected.size) {
                    0 -> LocalStrings.current.chooseInterests
                    1 -> LocalStrings.current.addMoreOrSend
                    else -> "${selected.size} ${LocalStrings.current.categoriesSelected}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected.isNotEmpty()) PrimaryGradient
                        else Brush.linearGradient(listOf(BgSub, BgSub))
                    )
                    .then(
                        if (selected.isNotEmpty()) Modifier.clickable(onClick = onSend)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = LocalStrings.current.send,
                    modifier = Modifier.size(18.dp),
                    tint = if (selected.isNotEmpty()) BgWhite else TextCaption,
                )
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
        val strings = LocalStrings.current
        Text(
            text = strings.categoryNames[category.id] ?: category.name,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) category.color else TextPrimary,
        )
        Text(
            text = strings.categorySubs[category.id] ?: category.sub,
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