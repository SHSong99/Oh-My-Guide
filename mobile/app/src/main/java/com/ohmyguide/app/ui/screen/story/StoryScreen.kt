package com.ohmyguide.app.ui.screen.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.ohmyguide.app.service.TtsManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DarkBg
import com.ohmyguide.app.ui.theme.DarkMid
import com.ohmyguide.app.ui.theme.DarkSurface
import com.ohmyguide.app.ui.theme.DarkTextLight
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.TextPrimary

private val MOCK_PAGES = listOf(
    "Built in 1394 during the early Joseon Dynasty, Gwangjang Market was originally a textile trading hub. Over six centuries, it evolved into one of Seoul's most beloved food destinations.",
    "The famous bindaetteok (mung bean pancake) vendors have been perfecting their craft for generations. Each vendor has their own secret recipe, passed down through families.",
    "Today, Gwangjang Market attracts over 100,000 visitors daily. From mayak gimbap to yukhoe, every corner offers an authentic taste of Korean culinary heritage.",
)

@Composable
fun StoryOverlay(placeId: String, onDismiss: () -> Unit) {
    val detail = SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Place"
    val placeNameKr = detail?.place?.nameKr ?: ""

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = MOCK_PAGES.size
    val isLastPage = currentPage == totalPages - 1

    // Cleanup TTS on dismiss
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, DarkMid, TextPrimary)
                )
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StoryTopBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onBack = onDismiss,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 120.dp),
            ) {
                if (currentPage == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = placeName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = BgWhite,
                    )
                    Text(
                        text = placeNameKr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = placeName,
                            style = MaterialTheme.typography.titleLarge,
                            color = BgWhite,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = placeNameKr,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AudioPlayerBar(
                    isPlaying = isSpeaking,
                    onToggle = {
                        if (isSpeaking) {
                            ttsManager.pause()
                        } else if (ttsManager.hasPaused()) {
                            ttsManager.resume()
                        } else {
                            scope.launch { ttsManager.speak(MOCK_PAGES[currentPage]) }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail?.place?.emoji ?: "\uD83C\uDFDE\uFE0F",
                        fontSize = 48.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = MOCK_PAGES[currentPage],
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp),
                    color = DarkTextLight,
                )
            }
        }

        StoryBottomNav(
            currentPage = currentPage,
            isLastPage = isLastPage,
            onPrev = {
                if (currentPage > 0) {
                    ttsManager.stop()
                    currentPage--
                }
            },
            onNext = {
                ttsManager.stop()
                if (isLastPage) onDismiss()
                else currentPage++
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StoryOverlayPreview() {
    OhMyGuideTheme {
        StoryOverlay(placeId = "dm3", onDismiss = {})
    }
}
