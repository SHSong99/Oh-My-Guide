package com.ohmyguide.app.ui.theme

import androidx.compose.ui.graphics.Brush

// ── Core Gradients (디자인 가이드) ──

val MainGradient = Brush.linearGradient(
    colors = listOf(PrimaryDeep, Primary)
)

val SubGradient = Brush.linearGradient(
    colors = listOf(SecondaryDark, Secondary)
)

// ── Auxiliary Gradients ──

val Main3DGradient = Brush.linearGradient(
    colors = listOf(PrimaryDark, Primary, PrimaryLight)
)

val Sub3DGradient = Brush.linearGradient(
    colors = listOf(SecondaryAmber, Secondary, SecondaryLight)
)

val DirectMixGradient = Brush.linearGradient(
    colors = listOf(Primary, Secondary)
)

// ── Legacy aliases (기존 CommonComponents에서 이동) ──

val PrimaryGradient = Main3DGradient

val PrimaryGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(PrimaryDark, PrimaryLight)
)
