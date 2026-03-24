package com.ohmyguide.app.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.naver.maps.map.overlay.OverlayImage

fun buildCircleMarker(
    srcBitmap: Bitmap,
    sizePx: Int,
    borderWidth: Float,
    borderColor: Int,
): OverlayImage {
    val scaled = Bitmap.createScaledBitmap(srcBitmap, sizePx, sizePx, true)
    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = sizePx / 2f
    paint.shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    canvas.drawCircle(r, r, r - borderWidth, paint)
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderWidth
    paint.color = borderColor
    canvas.drawCircle(r, r, r - borderWidth / 2, paint)
    return OverlayImage.fromBitmap(output)
}