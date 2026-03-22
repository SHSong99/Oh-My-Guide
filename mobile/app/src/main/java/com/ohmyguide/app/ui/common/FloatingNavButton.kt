package com.ohmyguide.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.PrimaryGradient
import kotlin.math.roundToInt

// ── 내비 최소화 상태 ──

class NavMinimizedState {
    var isMinimized by mutableStateOf(false)
    var placeId by mutableStateOf<String?>(null)
    var mode by mutableStateOf("walk")

    fun minimize(placeId: String, mode: String) {
        this.isMinimized = true
        this.placeId = placeId
        this.mode = mode
    }

    fun restore() {
        isMinimized = false
    }

    fun stop() {
        isMinimized = false
        placeId = null
    }
}

// ── FloatingNavButton ──

@Composable
fun FloatingNavButton(
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(PrimaryGradient)
                .clickable(onClick = onRestore),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(28.dp), tint = BgWhite)
        }
    }
}