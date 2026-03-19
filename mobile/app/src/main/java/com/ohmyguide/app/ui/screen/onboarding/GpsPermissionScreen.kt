package com.ohmyguide.app.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.fixtures.COMPANION_OPTIONS
import com.ohmyguide.app.fixtures.COUNTRY_OPTIONS
import com.ohmyguide.app.fixtures.GENDER_OPTIONS
import com.ohmyguide.app.fixtures.LANGUAGE_OPTIONS
import com.ohmyguide.app.service.LocationForegroundService
import com.ohmyguide.app.ui.common.GuideBubble
import com.ohmyguide.app.ui.common.PrimaryButton
import com.ohmyguide.app.ui.common.UserBubble
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.PrimaryGradient
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

private enum class OnboardStep { LANGUAGE, GENDER, AGE, COUNTRY, COMPANION, GPS }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpsPermissionScreen(
    onAllow: () -> Unit,
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(OnboardStep.LANGUAGE) }
    var languageLabel by remember { mutableStateOf("") }
    var genderLabel by remember { mutableStateOf("") }
    var ageLabel by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
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
        onAllow()
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
        GuideBubble(text = "Find the Best Spots Near You!\nTo give you the best experience,\nwhat language do you prefer?")

        if (step == OnboardStep.LANGUAGE) {
            PillOptionButtons(
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
            GuideBubble(text = "Nice! How old are you?")

            if (step > OnboardStep.AGE) {
                UserBubble(text = ageLabel)
            }
        }

        // Step 4: Country
        if (step > OnboardStep.AGE) {
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
                CompanionButtons(
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

    // Age input bar — only visible during AGE step
    if (step == OnboardStep.AGE) {
        AgeInputBar(
            value = ageInput,
            onValueChange = { ageInput = it },
            onSend = {
                if (ageInput.isNotBlank()) {
                    ageLabel = "${ageInput} years old"
                    ageInput = ""
                    step = OnboardStep.COUNTRY
                }
            },
        )
    }
    }
}

@Composable
private fun AgeInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgWhite)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newVal ->
                if (newVal.all { it.isDigit() } && newVal.length <= 3) {
                    onValueChange(newVal)
                }
            },
            placeholder = {
                Text(
                    text = "Enter your age",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption,
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedContainerColor = BgWhite,
                unfocusedContainerColor = BgWhite,
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (value.isNotBlank()) PrimaryGradient
                    else Brush.linearGradient(listOf(BgSub, BgSub))
                )
                .then(
                    if (value.isNotBlank()) Modifier.clickable(onClick = onSend)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier.size(20.dp),
                tint = if (value.isNotBlank()) BgWhite else TextCaption,
            )
        }
    }
}

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountrySelector(
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        COUNTRY_OPTIONS.forEach { country ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .border(1.dp, Border, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                    ) { onSelect("${country.flag} ${country.name}") }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = country.flag,
                    style = MaterialTheme.typography.bodySmall,
                )
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
