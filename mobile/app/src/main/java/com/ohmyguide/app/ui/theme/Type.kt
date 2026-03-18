package com.ohmyguide.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Font Families ──
// 실제 .ttf 파일을 res/font/ 에 넣은 후 아래 주석을 해제하세요.
//
// val Pretendard = FontFamily(
//     Font(R.font.pretendard_regular, FontWeight.Normal),
//     Font(R.font.pretendard_medium, FontWeight.Medium),
//     Font(R.font.pretendard_semibold, FontWeight.SemiBold),
//     Font(R.font.pretendard_bold, FontWeight.Bold),
// )
//
// val Fredoka = FontFamily(
//     Font(R.font.fredoka_regular, FontWeight.Normal),
//     Font(R.font.fredoka_semibold, FontWeight.SemiBold),
//     Font(R.font.fredoka_bold, FontWeight.Bold),
// )
//
// val NotoSansJP = FontFamily(
//     Font(R.font.notosansjp_regular, FontWeight.Normal),
//     Font(R.font.notosansjp_medium, FontWeight.Medium),
//     Font(R.font.notosansjp_bold, FontWeight.Bold),
// )
//
// val NotoSansSC = FontFamily(
//     Font(R.font.notosanssc_regular, FontWeight.Normal),
//     Font(R.font.notosanssc_medium, FontWeight.Medium),
//     Font(R.font.notosanssc_bold, FontWeight.Bold),
// )

// 폰트 파일 추가 후 Pretendard → 실제 FontFamily로 교체
val Pretendard = FontFamily.Default
val Fredoka = FontFamily.Default

// ── Typography ──

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)
