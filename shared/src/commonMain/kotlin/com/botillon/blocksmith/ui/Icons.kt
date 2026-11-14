package com.botillon.blocksmith.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.starPath(center: Offset, outer: Float, inner: Float = outer * 0.45f): Path {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.PI / 2 + i * Math.PI / 5
        val r = if (i % 2 == 0) outer else inner
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y - (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color,
    filled: Boolean = true,
    strokeWidth: Float = radius * 0.34f,
) {
    val path = starPath(center, radius)
    if (filled) drawPath(path, color)
    else drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

fun DrawScope.drawCrown(center: Offset, width: Float, color: Color) {
    val h = width * 0.68f
    val left = center.x - width / 2f
    val top = center.y - h / 2f
    val path = Path().apply {
        moveTo(left, top + h * 0.78f)
        lineTo(left + width * 0.12f, top + h * 0.18f)
        lineTo(left + width * 0.32f, top + h * 0.55f)
        lineTo(left + width * 0.5f, top)
        lineTo(left + width * 0.68f, top + h * 0.55f)
        lineTo(left + width * 0.88f, top + h * 0.18f)
        lineTo(left + width, top + h * 0.78f)
        close()
    }
    drawPath(path, color)
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top + h * 0.80f),
        size = Size(width, h * 0.20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.06f),
    )
}

fun DrawScope.drawGear(center: Offset, size: Float, color: Color) {
    val radius = size * 0.5f
    val teeth = 8
    repeat(teeth) { i ->
        rotate(degrees = i * 360f / teeth, pivot = center) {
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - radius * 0.14f, center.y - radius * 1.08f),
                size = Size(radius * 0.28f, radius * 0.34f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.10f),
            )
        }
    }
    drawCircle(color, radius = radius * 0.74f, center = center, style = Stroke(width = radius * 0.30f))
}

fun DrawScope.drawPlay(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - size * 0.28f, center.y - size * 0.45f)
        lineTo(center.x + size * 0.45f, center.y)
        lineTo(center.x - size * 0.28f, center.y + size * 0.45f)
        close()
    }
    drawPath(path, color)
}

fun DrawScope.drawChevronLeft(center: Offset, size: Float, color: Color, width: Float = size * 0.22f) {
    val p = Path().apply {
        moveTo(center.x + size * 0.22f, center.y - size * 0.42f)
        lineTo(center.x - size * 0.18f, center.y)
        lineTo(center.x + size * 0.22f, center.y + size * 0.42f)
    }
    drawPath(p, color, style = Stroke(width = width, cap = StrokeCap.Round))
}

fun DrawScope.drawPause(center: Offset, size: Float, color: Color) {
    val w = size * 0.20f
    val h = size * 0.72f
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - size * 0.30f, center.y - h / 2f),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.4f),
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x + size * 0.10f, center.y - h / 2f),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.4f),
    )
}

fun DrawScope.drawLock(center: Offset, size: Float, color: Color) {
    val bodyW = size * 0.62f
    val bodyH = size * 0.48f
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - bodyW / 2f, center.y - bodyH * 0.2f),
        size = Size(bodyW, bodyH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.16f),
    )
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - bodyW * 0.34f, center.y - bodyH * 0.95f),
        size = Size(bodyW * 0.68f, bodyH * 1.05f),
        style = Stroke(width = size * 0.11f, cap = StrokeCap.Round),
    )
}

fun DrawScope.drawBulb(center: Offset, size: Float, color: Color) {
    drawCircle(color, radius = size * 0.26f, center = Offset(center.x, center.y - size * 0.10f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - size * 0.11f, center.y + size * 0.16f),
        size = Size(size * 0.22f, size * 0.10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.05f),
    )
    repeat(6) { i ->
        val angle = i * 60f
        rotate(angle, pivot = center) {
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - size * 0.025f, center.y - size * 0.50f),
                size = Size(size * 0.05f, size * 0.13f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.025f),
            )
        }
    }
}

fun DrawScope.drawSpeaker(center: Offset, size: Float, color: Color, on: Boolean) {
    val path = Path().apply {
        moveTo(center.x - size * 0.30f, center.y - size * 0.13f)
        lineTo(center.x - size * 0.12f, center.y - size * 0.13f)
        lineTo(center.x + size * 0.08f, center.y - size * 0.32f)
        lineTo(center.x + size * 0.08f, center.y + size * 0.32f)
        lineTo(center.x - size * 0.12f, center.y + size * 0.13f)
        lineTo(center.x - size * 0.30f, center.y + size * 0.13f)
        close()
    }
    drawPath(path, color)
    if (on) {
        drawArc(
            color = color,
            startAngle = -55f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(center.x - size * 0.10f, center.y - size * 0.24f),
            size = Size(size * 0.42f, size * 0.48f),
            style = Stroke(width = size * 0.075f, cap = StrokeCap.Round),
        )
    } else {
        drawLine(
            color = color,
            start = Offset(center.x + size * 0.14f, center.y - size * 0.16f),
            end = Offset(center.x + size * 0.36f, center.y + size * 0.16f),
            strokeWidth = size * 0.075f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(center.x + size * 0.36f, center.y - size * 0.16f),
            end = Offset(center.x + size * 0.14f, center.y + size * 0.16f),
            strokeWidth = size * 0.075f,
            cap = StrokeCap.Round,
        )
    }
}

fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.34f)
        cubicTo(
            center.x - size * 0.62f, center.y - size * 0.06f,
            center.x - size * 0.30f, center.y - size * 0.52f,
            center.x, center.y - size * 0.18f,
        )
        cubicTo(
            center.x + size * 0.30f, center.y - size * 0.52f,
            center.x + size * 0.62f, center.y - size * 0.06f,
            center.x, center.y + size * 0.34f,
        )
    }
    drawPath(path, color)
}

fun DrawScope.drawCheck(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - size * 0.30f, center.y)
        lineTo(center.x - size * 0.06f, center.y + size * 0.24f)
        lineTo(center.x + size * 0.32f, center.y - size * 0.24f)
    }
    drawPath(path, color, style = Stroke(width = size * 0.16f, cap = StrokeCap.Round))
}

fun DrawScope.drawGift(center: Offset, size: Float, color: Color) {
    val w = size * 0.62f
    val h = size * 0.44f
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w / 2f, center.y - h * 0.40f),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.05f),
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.58f, center.y - h * 0.62f),
        size = Size(w * 1.16f, h * 0.30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size * 0.04f),
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(center.x - size * 0.035f, center.y - h * 0.40f),
        size = Size(size * 0.07f, h),
    )
}
