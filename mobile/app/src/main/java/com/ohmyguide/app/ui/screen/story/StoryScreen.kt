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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DarkBg
import com.ohmyguide.app.ui.theme.DarkMid
import com.ohmyguide.app.ui.theme.DarkSurface
import com.ohmyguide.app.ui.theme.DarkTextLight
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryLight
import com.ohmyguide.app.ui.theme.TextPrimary
import coil.compose.AsyncImage

private fun splitIntoPages(text: String): List<String> {
    val sentences = text.split(Regex("(?<=\\.)\\s+")).filter { it.isNotBlank() }
    if (sentences.size <= 3) return if (sentences.isEmpty()) listOf(text) else sentences.chunked(1) { it.joinToString(" ") }
    val perPage = (sentences.size + 2) / 3
    return sentences.chunked(perPage) { it.joinToString(" ") }
}

private val MOCK_PAGES = listOf(
    "Built in 1394 during the early Joseon Dynasty, Gwangjang Market was originally a textile trading hub. Over six centuries, it evolved into one of Seoul's most beloved food destinations.",
    "The famous bindaetteok (mung bean pancake) vendors have been perfecting their craft for generations. Each vendor has their own secret recipe, passed down through families.",
    "Today, Gwangjang Market attracts over 100,000 visitors daily. From mayak gimbap to yukhoe, every corner offers an authentic taste of Korean culinary heritage.",
)

@Composable
fun StoryOverlay(placeId: String, onDismiss: () -> Unit) {
    val detail = PlaceDetailCache.get(placeId)
        ?: SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()
    val placeName = detail?.place?.name ?: "Place"
    val placeNameKr = detail?.place?.nameKr ?: ""

    val guideData = PlaceDetailCache.getGuide(placeId)
    val ttsText = guideData?.destination?.overviewTts
    val overviewText = guideData?.destination?.overview ?: detail?.desc

    // Use API text if available, split into pages; otherwise fallback to MOCK_PAGES
    val pages = remember(ttsText, overviewText) {
        val apiText = ttsText ?: overviewText
        if (!apiText.isNullOrBlank()) {
            splitIntoPages(apiText)
        } else {
            MOCK_PAGES
        }
    }

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val isLoadingTts by ttsManager.isLoading.collectAsState()
    val ttsProgress by ttsManager.progress.collectAsState()

    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = pages.size
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
                    isLoading = isLoadingTts,
                    onToggle = {
                        if (isSpeaking) {
                            ttsManager.pause()
                        } else if (ttsManager.hasPaused()) {
                            ttsManager.resume()
                        } else {
                            scope.launch { ttsManager.speak(pages[currentPage]) }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                val imageUrl = guideData?.destination?.firstImage1 ?: detail?.place?.imageUrl
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = placeName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = detail?.place?.emoji ?: "\uD83C\uDFDE\uFE0F",
                            fontSize = 48.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Split page text into sentences for highlight
                val pageText = pages[currentPage]
                val sentences = remember(pageText) {
                    pageText.split(Regex("(?<=[.!?。,])(\\s+)")).filter { it.isNotBlank() }
                        .let { if (it.size <= 1) pageText.chunked((pageText.length / 3).coerceAtLeast(1)) else it }
                }

                if (!isSpeaking && ttsProgress == 0f) {
                    Text(
                        text = pageText,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp),
                        color = if (isSpeaking) BgWhite else DarkTextLight,
                    )
                } else {
                    // Calculate cumulative character weights for each sentence
                    val totalChars = sentences.sumOf { it.length }
                    val cumWeights = remember(sentences) {
                        var cum = 0f
                        sentences.map { s ->
                            cum += s.length.toFloat() / totalChars
                            cum
                        }
                    }
                    val activeSentenceIndex = cumWeights.indexOfFirst { it > ttsProgress }
                        .let { if (it == -1) sentences.lastIndex else it }

                    val annotated = buildAnnotatedString {
                        sentences.forEachIndexed { i, sentence ->
                            val style = when {
                                i < activeSentenceIndex -> SpanStyle(color = DarkTextLight.copy(alpha = 0.45f))
                                i == activeSentenceIndex -> SpanStyle(color = PrimaryLight, fontWeight = FontWeight.SemiBold)
                                else -> SpanStyle(color = DarkTextLight.copy(alpha = 0.65f))
                            }
                            withStyle(style) { append(sentence) }
                            if (i < sentences.lastIndex) append(" ")
                        }
                    }

                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp),
                    )
                }
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
