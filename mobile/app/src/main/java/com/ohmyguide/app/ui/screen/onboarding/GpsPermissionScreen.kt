package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.fixtures.COMPANION_OPTIONS
import com.ohmyguide.app.fixtures.COUNTRY_OPTIONS
import com.ohmyguide.app.fixtures.GENDER_OPTIONS
import com.ohmyguide.app.fixtures.LANGUAGE_OPTIONS
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.PrimaryButton
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.TextPrimary

private enum class OnboardStep { LANGUAGE, GENDER, COUNTRY, COMPANION, GPS }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpsPermissionScreen(
    onAllow: () -> Unit,
) {
    var step by remember { mutableStateOf(OnboardStep.LANGUAGE) }
    var languageLabel by remember { mutableStateOf("") }
    var genderLabel by remember { mutableStateOf("") }
    var countryLabel by remember { mutableStateOf("") }
    var companionLabel by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(step) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgWhite, PrimaryBgLight))
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        // Step 1: Language
        GuideBubble(text = "Find the Best Spots Near You!\nWhat language do you prefer?")

        if (step == OnboardStep.LANGUAGE) {
            OptionButtons(
                options = LANGUAGE_OPTIONS.map { it.label },
                onSelect = { label ->
                    languageLabel = label
                    step = OnboardStep.GENDER
                },
            )
        }

        // Step 2: Gender
        if (step > OnboardStep.LANGUAGE) {
            UserBubble(text = languageLabel)
            GuideBubble(text = "Great! And what is your gender?")

            if (step == OnboardStep.GENDER) {
                OptionButtons(
                    options = GENDER_OPTIONS,
                    onSelect = { label ->
                        genderLabel = label
                        step = OnboardStep.COUNTRY
                    },
                )
            }
        }

        // Step 3: Country
        if (step > OnboardStep.GENDER) {
            UserBubble(text = genderLabel)
            GuideBubble(text = "Where are you from?")

            if (step == OnboardStep.COUNTRY) {
                CountrySelector(
                    onSelect = { label ->
                        countryLabel = label
                        step = OnboardStep.COMPANION
                    },
                )
            }
        }

        // Step 4: Companion
        if (step > OnboardStep.COUNTRY) {
            UserBubble(text = countryLabel)
            GuideBubble(text = "Awesome! Who are you traveling with?")

            if (step == OnboardStep.COMPANION) {
                OptionButtons(
                    options = COMPANION_OPTIONS.map { "${it.emoji} ${it.label}" },
                    onSelect = { label ->
                        companionLabel = label
                        step = OnboardStep.GPS
                    },
                )
            }
        }

        // Step 5: GPS
        if (step == OnboardStep.GPS) {
            UserBubble(text = companionLabel)
            GuideBubble(text = "Perfect! Lastly, please allow location access so we can explore together.")

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Allow Location Access",
                onClick = onAllow,
                modifier = Modifier.padding(start = 46.dp),
            )
        }
    }
}

@Composable
private fun OptionButtons(
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 46.dp, bottom = 12.dp),
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
                    .clickable { onSelect(label) }
                    .padding(12.dp, 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountrySelector(
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(start = 46.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        COUNTRY_OPTIONS.forEach { country ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .clickable { onSelect("${country.flag} ${country.name}") }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(text = country.flag)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GpsPermissionScreenPreview() {
    OhMyGuideTheme {
        GpsPermissionScreen(onAllow = {})
    }
}