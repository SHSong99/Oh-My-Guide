package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.fixtures.COMPANION_OPTIONS
import com.ohmyguide.app.fixtures.COUNTRY_OPTIONS
import com.ohmyguide.app.fixtures.GENDER_OPTIONS
import com.ohmyguide.app.fixtures.LANGUAGE_OPTIONS
import com.ohmyguide.app.ui.theme.AppLanguage
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.PrimaryButton
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import kotlinx.coroutines.flow.distinctUntilChanged

private enum class OnboardStep { LANGUAGE, GENDER, AGE, COUNTRY, COMPANION, GPS }

@Composable
fun GpsPermissionScreen(
    onAllow: (gender: String, age: Int, country: String, companion: String) -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var step by remember { mutableStateOf(OnboardStep.LANGUAGE) }
    var languageLabel by remember { mutableStateOf("") }
    var genderLabel by remember { mutableStateOf("") }
    var ageLabel by remember { mutableStateOf("") }
    var countryLabel by remember { mutableStateOf("") }
    var companionLabel by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val intent = Intent(context, LocationForegroundService::class.java)
            context.startForegroundService(intent)
        }
        onAllow(genderLabel, ageLabel.toIntOrNull() ?: 25, countryLabel, companionLabel)
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(step) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgWhite, PrimaryBgLight))
            ),
    ) {
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 32.dp),
    ) {
        // Step 1: Language
        GuideBubble(text = strings.onboardLangPrompt)

        if (step == OnboardStep.LANGUAGE) {
            PillOptionButtons(
                options = LANGUAGE_OPTIONS.map { it.label },
                onSelect = { label ->
                    languageLabel = label
                    val langCode = LANGUAGE_OPTIONS.find { it.label == label }?.id ?: "en"
                    LanguageManager.setLanguage(context, AppLanguage.fromCode(langCode))
                    step = OnboardStep.GENDER
                },
            )
        }

        // Step 2: Gender
        if (step > OnboardStep.LANGUAGE) {
            UserBubble(text = languageLabel)
            GuideBubble(text = strings.onboardGenderPrompt)

            if (step == OnboardStep.GENDER) {
                PillOptionButtons(
                    options = GENDER_OPTIONS,
                    onSelect = { label ->
                        genderLabel = label
                        step = OnboardStep.AGE
                    },
                )
            }
        }

        // Step 3: Age
        if (step > OnboardStep.GENDER) {
            UserBubble(text = genderLabel)
            GuideBubble(text = strings.onboardAgePrompt)

            if (step == OnboardStep.AGE) {
                AgeWheelPicker(
                    onConfirm = { age ->
                        ageLabel = "$age ${strings.yearsOld}"
                        step = OnboardStep.COUNTRY
                    },
                )
            }

            if (step > OnboardStep.AGE) {
                UserBubble(text = ageLabel)
            }
        }

        // Step 4: Country
        if (step > OnboardStep.AGE) {
            GuideBubble(text = strings.onboardCountryPrompt)

            if (step == OnboardStep.COUNTRY) {
                CountrySelector(
                    onSelect = { label ->
                        countryLabel = label
                        step = OnboardStep.COMPANION
                    },
                )
            }
        }

        // Step 5: Companion
        if (step > OnboardStep.COUNTRY) {
            UserBubble(text = countryLabel)
            GuideBubble(text = strings.onboardCompanionPrompt)

            if (step == OnboardStep.COMPANION) {
                CompanionButtons(
                    onSelect = { label ->
                        companionLabel = label
                        step = OnboardStep.GPS
                    },
                )
            }
        }

        // Step 6: GPS
        if (step == OnboardStep.GPS) {
            UserBubble(text = companionLabel)
            GuideBubble(text = strings.onboardGpsPrompt)

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = strings.allowLocation,
                onClick = {
                    val permissions = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                },
                modifier = Modifier.padding(start = 46.dp),
                icon = Icons.Filled.LocationOn,
            )
        }
    }
    }
}

// ── Age Wheel Picker ──

@Composable
private fun AgeWheelPicker(
    onConfirm: (Int) -> Unit,
) {
    val ageRange = (10..99).toList()
    val defaultIndex = ageRange.indexOf(20)
    val itemHeightDp = 44.dp
    val visibleItems = 5
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.roundToPx() }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = defaultIndex - visibleItems / 2,
    )

    val selectedAge by remember {
        derivedStateOf {
            val centerOffset = listState.firstVisibleItemIndex +
                visibleItems / 2
            ageRange.getOrElse(centerOffset) { 20 }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex +
                listState.firstVisibleItemScrollOffset / itemHeightPx
        }
            .distinctUntilChanged()
            .collect { /* keep derivedStateOf reactive */ }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgWhite)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Center selection highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(itemHeightDp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBgLight),
            )

            // Wheel list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp * visibleItems)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.3f to Color.White,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                0.7f to Color.White,
                                1f to Color.Transparent,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                flingBehavior = androidx.compose.foundation.gestures.snapping
                    .rememberSnapFlingBehavior(listState),
            ) {
                items(ageRange.size) { index ->
                    val age = ageRange[index]
                    val isSelected = age == selectedAge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$age",
                            style = if (isSelected) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) Primary else TextCaption,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryGradient)
                .clickable { onConfirm(selectedAge) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "I'm $selectedAge years old",
                style = MaterialTheme.typography.titleMedium,
                color = BgWhite,
            )
        }
    }
}

// ── Pill Option Buttons ──

@Composable
private fun PillOptionButtons(
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgWhite)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                    ) { onSelect(label) }
                    .padding(14.dp),
            )
        }
    }
}

// ── Companion Buttons ──

@Composable
private fun CompanionButtons(
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 46.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        COMPANION_OPTIONS.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgWhite)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                    ) { onSelect(option.label) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(option.bgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        tint = option.iconColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
            }
        }
    }
}

// ── Country Selector (Toggle Dropdown) ──

@Composable
private fun CountrySelector(
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp, bottom = 12.dp),
    ) {
        // Toggle header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgWhite)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                ) { expanded = !expanded }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select your country",
                style = MaterialTheme.typography.bodyMedium,
                color = TextCaption,
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Toggle",
                tint = TextCaption,
                modifier = Modifier.size(20.dp),
            )
        }

        // Expandable country list
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgWhite)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .verticalScroll(rememberScrollState()),
            ) {
                COUNTRY_OPTIONS.forEach { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                            ) { onSelect("${country.flag} ${country.name}") }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = country.flag,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GpsPermissionScreenPreview() {
    OhMyGuideTheme {
        GpsPermissionScreen(onAllow = { _, _, _, _ -> })
    }
}