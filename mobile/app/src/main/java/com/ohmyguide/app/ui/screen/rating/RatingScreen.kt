package com.ohmyguide.app.ui.screen.rating

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgScreen
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.Secondary
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

@Composable
fun RatingScreen(
    navController: NavController,
    viewModel: RatingViewModel = hiltViewModel(),
) {
    val strings = LocalStrings.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.submitted) {
        if (state.submitted) {
            navController.navigate(Screen.Home.createRoute()) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = strings.ratingTitle,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${strings.ratingSubtitle} ${viewModel.placeName}",
            style = MaterialTheme.typography.bodyLarge,
            color = TextCaption,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        StarRow(
            selected = state.star,
            onSelect = { viewModel.selectStar(it) },
        )

        Spacer(modifier = Modifier.height(40.dp))

        SubmitButton(
            text = strings.ratingSubmit,
            enabled = state.star > 0 && !state.submitting,
            loading = state.submitting,
            onClick = { viewModel.submit() },
        )
    }
}

@Composable
private fun StarRow(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..5) {
            val filled = i <= selected
            val scale by animateFloatAsState(
                targetValue = if (filled) 1.15f else 1f,
                animationSpec = tween(200),
                label = "star_scale_$i",
            )
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "$i star",
                tint = if (filled) Star else TextCaption,
                modifier = Modifier
                    .size(52.dp)
                    .scale(scale)
                    .clickable { onSelect(i) },
            )
        }
    }
}

@Composable
private fun SubmitButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (enabled) Primary else Primary.copy(alpha = 0.4f))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Secondary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) BgWhite else BgWhite.copy(alpha = 0.6f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRowPreview() {
    StarRow(selected = 3, onSelect = {})
}
