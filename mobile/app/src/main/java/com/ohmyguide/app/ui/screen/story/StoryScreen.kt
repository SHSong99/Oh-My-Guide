package com.ohmyguide.app.ui.screen.story

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ohmyguide.app.R
import com.ohmyguide.app.domain.model.PlaceDetailCache
import com.ohmyguide.app.fixtures.SAMPLE_PLACE_DETAILS
import com.ohmyguide.app.service.TtsManager
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.DarkBg
import com.ohmyguide.app.ui.theme.DarkMid
import com.ohmyguide.app.ui.theme.DarkSurface
import com.ohmyguide.app.ui.theme.DarkTextLight
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.TextPrimary

private val MOCK_TEXT = "Built in 1394 during the early Joseon Dynasty, Gwangjang Market was originally a textile trading hub. Over six centuries, it evolved into one of Seoul's most beloved food destinations. The famous bindaetteok vendors have been perfecting their craft for generations. Each vendor has their own secret recipe, passed down through families. Today, Gwangjang Market attracts over 100,000 visitors daily."

private fun splitIntoSentences(text: String): List<String> {
    return text.split(Regex("(?<=[.!?。])(\\s*)")).filter { it.isNotBlank() }
        .let { if (it.size <= 1) text.chunked((text.length / 5).coerceAtLeast(30)) else it }
}

@Composable
fun StoryOverlay(placeId: String, onDismiss: () -> Unit) {
    val detail = PlaceDetailCache.get(placeId)
        ?: SAMPLE_PLACE_DETAILS[placeId]
        ?: SAMPLE_PLACE_DETAILS.values.firstOrNull()

    val guideData = PlaceDetailCache.getGuide(placeId)

    // 제목: guideData → detail → fallback
    val placeName = guideData?.destination?.title
        ?: detail?.place?.name
        ?: "Place"
    val placeNameKr = detail?.place?.nameKr ?: ""

    val ttsText = guideData?.destination?.overviewTts
    val overviewText = guideData?.destination?.overview ?: detail?.desc
    val imageUrl = guideData?.destination?.firstImage1 ?: detail?.place?.imageUrl

    val fullText = ttsText ?: overviewText ?: MOCK_TEXT
    val sentences = remember(fullText) { splitIntoSentences(fullText) }

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val isLoadingTts by ttsManager.isLoading.collectAsState()
    val ttsProgress by ttsManager.progress.collectAsState()

    val scope = rememberCoroutineScope()

    // 현재 문장 인덱스 계산
    val totalChars = sentences.sumOf { it.length }
    val cumWeights = remember(sentences) {
        var cum = 0f
        sentences.map { s ->
            cum += s.length.toFloat() / totalChars
            cum
        }
    }
    val isPlaying = isSpeaking || ttsProgress > 0f
    var activeSentenceIndex by remember { mutableIntStateOf(0) }
    if (isPlaying) {
        activeSentenceIndex = cumWeights.indexOfFirst { it > ttsProgress }
            .let { if (it == -1) sentences.lastIndex else it }
    }

    // 재생 중이면 이미지 축소
    val imageAspect by animateFloatAsState(
        targetValue = if (isPlaying) 16f / 5f else 16f / 9f,
        animationSpec = tween(500),
        label = "imageAspect",
    )

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
                currentPage = 0,
                totalPages = 1,
                onBack = onDismiss,
            )

            // Title
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = placeName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BgWhite,
                )
                if (placeNameKr.isNotEmpty()) {
                    Text(
                        text = placeNameKr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audio player bar
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                AudioPlayerBar(
                    isPlaying = isSpeaking,
                    isLoading = isLoadingTts,
                    onToggle = {
                        if (isSpeaking) {
                            ttsManager.pause()
                        } else if (ttsManager.hasPaused()) {
                            ttsManager.resume()
                        } else {
                            scope.launch { ttsManager.speak(fullText) }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image (shrinks when playing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(imageAspect)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 문장 영역 — 고정 위치, 내용만 바뀜
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 이전 문장 (2개)
                    val prev2 = if (activeSentenceIndex >= 2 && isPlaying) sentences[activeSentenceIndex - 2] else ""
                    val prev1 = if (activeSentenceIndex >= 1 && isPlaying) sentences[activeSentenceIndex - 1] else ""

                    Spacer(modifier = Modifier.weight(1f))

                    // 이전 2번째 문장
                    if (prev2.isNotEmpty()) {
                        Text(
                            text = prev2,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = DarkTextLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = 0.15f }
                                .padding(start = 58.dp, end = 8.dp, bottom = 4.dp),
                        )
                    }

                    // 이전 1번째 문장
                    if (prev1.isNotEmpty()) {
                        Text(
                            text = prev1,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = DarkTextLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = 0.3f }
                                .padding(start = 58.dp, end = 8.dp, bottom = 6.dp),
                        )
                    }

                    // 현재 문장 — 깨비 + 박스 (고정 위치)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.masot),
                            contentDescription = "Kkaebi",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.15f)),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Primary.copy(alpha = 0.15f))
                                .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text = if (isPlaying) sentences[activeSentenceIndex] else sentences.firstOrNull() ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                ),
                                color = BgWhite,
                            )
                        }
                    }

                    // 다음 문장들
                    val next1 = if (isPlaying && activeSentenceIndex < sentences.lastIndex) sentences[activeSentenceIndex + 1] else if (!isPlaying && sentences.size > 1) sentences[1] else ""
                    val next2 = if (isPlaying && activeSentenceIndex + 2 <= sentences.lastIndex) sentences[activeSentenceIndex + 2] else ""

                    // 다음 1번째 문장
                    if (next1.isNotEmpty()) {
                        Text(
                            text = next1,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = DarkTextLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = 0.3f }
                                .padding(start = 58.dp, end = 8.dp, top = 6.dp),
                        )
                    }

                    // 다음 2번째 문장
                    if (next2.isNotEmpty()) {
                        Text(
                            text = next2,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = DarkTextLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = 0.15f }
                                .padding(start = 58.dp, end = 8.dp, top = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StoryOverlayPreview() {
    OhMyGuideTheme {
        StoryOverlay(placeId = "dm3", onDismiss = {})
    }
}
