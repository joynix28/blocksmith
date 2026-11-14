package com.botillon.blocksmith.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botillon.blocksmith.game.Palette

@Composable
fun HomeScreen(
    best: Long,
    totalStars: Int,
    clearedLevels: Int,
    onPlay: () -> Unit,
    onLevels: () -> Unit,
    onDaily: () -> Unit,
    onSettings: () -> Unit,
) {
    val s = LocalStrings.current
    Backdrop {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BestPill(best)
                Spacer(Modifier.weight(1f))
                CircleButton(onClick = onSettings) {
                    drawGear(center, size.minDimension, Ink.TextSoft)
                }
            }

            Spacer(Modifier.height(26.dp))

            AppText(s.eyebrow, style = Typo.label, color = Ink.Accent)
            Spacer(Modifier.height(6.dp))
            Row {
                AppText("Block", style = Typo.display, color = Ink.Text)
                AppText("smith", style = Typo.display, color = Ink.Accent)
            }
            Spacer(Modifier.height(6.dp))
            AppText(s.tagline, style = Typo.body, color = Ink.Muted)

            Spacer(Modifier.height(18.dp))

            HeroBoard(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { rotationZ = -2.2f },
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(
                    icon = { drawStar(center, size.minDimension * 0.46f, Ink.Accent) },
                    value = "$totalStars",
                    label = s.stars,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    icon = { drawCheck(center, size.minDimension, Ink.Mint) },
                    value = "$clearedLevels",
                    label = s.cleared,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                label = s.play,
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
                icon = { drawPlay(center, size.minDimension * 1.15f, Color(0xFF22180A)) },
            )

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    label = s.levels,
                    onClick = onLevels,
                    modifier = Modifier.weight(1f),
                    icon = { drawLevelsIcon(center, size.minDimension, Ink.Accent) },
                )
                SecondaryButton(
                    label = s.daily,
                    onClick = onDaily,
                    modifier = Modifier.weight(1f),
                    icon = { drawGift(center, size.minDimension, Ink.Combo) },
                )
            }

            Spacer(Modifier.height(16.dp))
            AppText(
                text = s.offline,
                style = Typo.label.copy(letterSpacing = 0.6.sp),
                color = Ink.Faint,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatChip(
    icon: DrawScope.() -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Ink.Surface.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(20.dp)) { icon() }
        Spacer(Modifier.width(9.dp))
        AppText(value, style = Typo.heading, color = Ink.Text)
        Spacer(Modifier.width(5.dp))
        AppText(label, style = Typo.body, color = Ink.Muted)
    }
}

/** Petite illustration : un plateau presque fini et une pièce qui flotte. */
@Composable
fun HeroBoard(modifier: Modifier) {
    Canvas(modifier) {
        val cols = 6
        val framePad = size.minDimension * 0.03f
        val boardSize = size.minDimension - framePad * 2
        val cell = boardSize / cols
        val originX = (size.width - boardSize) / 2f
        val originY = (size.height - boardSize) / 2f

        drawRoundRect(
            color = Ink.BoardFrame,
            topLeft = Offset(originX - framePad, originY - framePad),
            size = Size(boardSize + framePad * 2, boardSize + framePad * 2),
            cornerRadius = CornerRadius(cell * 0.7f),
        )

        val filled = listOf(
            Triple(0, 3, Palette.MINT),
            Triple(0, 4, Palette.MINT),
            Triple(1, 4, Palette.MINT),
            Triple(2, 4, Palette.CORAL),
            Triple(2, 5, Palette.CORAL),
            Triple(3, 5, Palette.CORAL),
            Triple(3, 3, Palette.VIOLET),
            Triple(4, 3, Palette.VIOLET),
            Triple(4, 2, Palette.VIOLET),
            Triple(5, 1, Palette.SKY),
            Triple(5, 2, Palette.SKY),
            Triple(5, 3, Palette.AMBER),
        )

        for (y in 0 until cols) for (x in 0 until cols) {
            val topLeft = Offset(originX + x * cell, originY + y * cell)
            val hit = filled.firstOrNull { it.first == x && it.second == y }
            if (hit == null) {
                drawEmptyCell(topLeft, cell, alpha = 0.9f)
            } else {
                drawBlock(topLeft, cell, blockStyle(hit.third))
            }
        }

        // Pièce flottante au-dessus du plateau.
        val floatCell = cell * 0.94f
        val fx = originX + cell * 1.5f
        val fy = originY - cell * 0.58f
        val style = blockStyle(Palette.AMBER)
        drawRoundRect(
            color = Ink.Shadow.copy(alpha = 0.28f),
            topLeft = Offset(fx + floatCell * 0.10f, fy + floatCell * 1.92f),
            size = Size(floatCell * 1.9f, floatCell * 0.35f),
            cornerRadius = CornerRadius(floatCell * 0.18f),
        )
        drawBlock(Offset(fx, fy), floatCell, style)
        drawBlock(Offset(fx + floatCell, fy), floatCell, style)
        drawBlock(Offset(fx, fy + floatCell), floatCell, style)
        drawBlock(Offset(fx + floatCell, fy + floatCell), floatCell, style)

        // Ligne prête à être effacée.
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = 0.30f), Color.Transparent),
            ),
            topLeft = Offset(originX, originY + cell * 5 + cell * 0.44f),
            size = Size(boardSize, cell * 0.10f),
            cornerRadius = CornerRadius(cell * 0.05f),
        )
    }
}

/** Icône « niveaux » : quatre carrés façon grille. */
fun DrawScope.drawLevelsIcon(center: Offset, size: Float, color: Color) {
    val w = size * 0.74f
    val gap = w * 0.18f
    val unit = (w - gap) / 2f
    val left = center.x - w / 2f
    val top = center.y - w / 2f
    drawRoundRect(color, Offset(left, top), Size(unit, unit), CornerRadius(unit * 0.30f))
    drawRoundRect(color, Offset(left + unit + gap, top), Size(unit, unit), CornerRadius(unit * 0.30f))
    drawRoundRect(
        color.copy(alpha = 0.5f),
        Offset(left, top + unit + gap),
        Size(unit, unit),
        CornerRadius(unit * 0.30f),
    )
    drawRoundRect(
        color.copy(alpha = 0.5f),
        Offset(left + unit + gap, top + unit + gap),
        Size(unit, unit),
        CornerRadius(unit * 0.30f),
    )
}
