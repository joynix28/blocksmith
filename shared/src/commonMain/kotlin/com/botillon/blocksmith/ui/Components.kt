package com.botillon.blocksmith.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Typo.body,
    color: Color = Ink.Text,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
    softWrap: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, textAlign = align),
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Fond commun : nuit indigo, halo en haut, blocs flottants très discrets. */
@Composable
fun Backdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Ink.BgTop, Ink.BgMid, Ink.BgBottom),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B2A6B).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.06f),
                    radius = size.width * 0.95f,
                ),
                radius = size.width * 0.95f,
                center = Offset(size.width * 0.5f, size.height * 0.06f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2A5E6B).copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.92f),
                    radius = size.width * 0.8f,
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.85f, size.height * 0.92f),
            )
        }
        content()
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (DrawScope.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(90), label = "press")

    Box(
        modifier = modifier
            .height(62.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) {
                    Brush.verticalGradient(listOf(Ink.Accent, Ink.AccentDeep))
                } else {
                    Brush.verticalGradient(listOf(Ink.SurfaceHigh, Ink.Surface))
                },
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Canvas(Modifier.size(20.dp)) { icon() }
                Spacer(Modifier.width(10.dp))
            }
            AppText(
                text = label,
                style = Typo.button,
                color = if (enabled) Color(0xFF22180A) else Ink.Muted,
            )
        }
    }
}

@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (DrawScope.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(90), label = "press2")

    Box(
        modifier = modifier
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .background(Ink.Surface.copy(alpha = 0.72f))
            .border(1.dp, Ink.Faint.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Canvas(Modifier.size(17.dp)) { icon() }
                Spacer(Modifier.width(8.dp))
            }
            AppText(text = label, style = Typo.button.copy(fontSize = Typo.body.fontSize), color = Ink.TextSoft)
        }
    }
}

@Composable
fun CircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    icon: DrawScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, tween(90), label = "iconPress")
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(Ink.Surface.copy(alpha = 0.6f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * 0.5f)) { icon() }
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = Ink.Surface,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(26.dp)),
        content = content,
    )
}

@Composable
fun BestPill(best: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Ink.Surface.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(17.dp)) { drawCrown(center, size.minDimension, Ink.Accent) }
        Spacer(Modifier.width(7.dp))
        AppText(text = "$best", style = Typo.body, color = Ink.Accent)
    }
}

@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    gap: Dp = 3.dp,
    filledColor: Color = Ink.Accent,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(gap)) {
        repeat(3) { index ->
            Canvas(Modifier.size(size)) {
                drawStar(
                    center = center,
                    radius = this.size.minDimension * 0.46f,
                    color = if (index < stars) filledColor else Ink.Faint,
                    filled = index < stars,
                )
            }
        }
    }
}

@Composable
fun ProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Ink.Accent,
    height: Dp = 6.dp,
) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.35f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.75f)))),
        )
    }
}

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ink.TextSoft,
    background: Color = Ink.SurfaceHigh.copy(alpha = 0.8f),
) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        AppText(text, style = Typo.label, color = color)
    }
}
