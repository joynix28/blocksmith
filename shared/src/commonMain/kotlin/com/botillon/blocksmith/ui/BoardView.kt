package com.botillon.blocksmith.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.botillon.blocksmith.game.Board
import com.botillon.blocksmith.game.ClearFx
import com.botillon.blocksmith.game.Coord
import com.botillon.blocksmith.game.GameState
import com.botillon.blocksmith.game.Piece
import com.botillon.blocksmith.game.Rules
import com.botillon.blocksmith.game.SIZE
import kotlin.math.roundToInt

/** Cible calculée pendant un glisser-déposer. */
data class Preview(val x: Int, val y: Int, val valid: Boolean)

/** Tout ce qui doit être animé au-dessus du plateau. */
data class BoardFx(
    val clear: ClearFx? = null,
    val clearBoard: Board? = null,
    val clearProgress: Float = 1f,
    val placed: List<Coord> = emptyList(),
    val placedProgress: Float = 1f,
    val hint: List<Coord> = emptyList(),
)

/** État d'un doigt qui déplace une pièce depuis la main. */
data class DragState(
    val slot: Int,
    val piece: Piece,
    val pointer: Offset,
    val grab: Offset,
)

/**
 * Plateau de jeu dessiné au Canvas : cases vides, blocs, fantôme de pose,
 * effacement animé et particules.
 */
@Composable
fun GameBoard(
    state: GameState,
    fx: BoardFx,
    preview: Preview?,
    dragPiece: Piece?,
    modifier: Modifier = Modifier,
    onGeometry: (Offset, Size) -> Unit,
) {
    Canvas(
        modifier
            .onGloballyPositioned { coords ->
                onGeometry(coords.positionInRoot(), coords.size.toSize())
            },
    ) {
        val cell = size.width / SIZE
        val full = cell * SIZE
        val corner = CornerRadius(cell * 0.62f)

        drawRoundRect(
            color = Ink.BoardFrame,
            size = Size(full, full),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.05f),
            size = Size(full, full),
            cornerRadius = corner,
            style = Stroke(width = (cell * 0.02f).coerceAtLeast(1f)),
        )

        // Cases vides.
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            if (!state.board.isFilled(x, y)) {
                drawEmptyCell(Offset(x * cell, y * cell), cell)
            }
        }

        // Blocs posés.
        val placedSet = fx.placed.toHashSet()
        val popT = fx.placedProgress
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            val color = state.board.colorAt(x, y) ?: continue
            val isPlacedNow = Coord(x, y) in placedSet && popT < 1f
            val scale = if (isPlacedNow) 0.86f + 0.14f * easeOutBack(popT) else 1f
            drawBlock(
                topLeft = Offset(x * cell, y * cell),
                size = cell,
                style = blockStyle(color),
                scale = scale,
                gem = state.board.hasGem(x, y),
            )
        }

        // Effacement : les cases disparaissent en rétrécissant, avec des particules.
        val clear = fx.clear
        val clearBoard = fx.clearBoard
        if (clear != null && clearBoard != null && fx.clearProgress < 1f) {
            val t = fx.clearProgress
            for ((index, c) in clear.coords.withIndex()) {
                val color = clearBoard.colorAt(c.x, c.y) ?: continue
                val style = blockStyle(color)
                val scale = (1f - t * 0.85f).coerceAtLeast(0.15f)
                drawBlock(
                    topLeft = Offset(c.x * cell, c.y * cell),
                    size = cell,
                    style = style,
                    scale = scale,
                    alpha = (1f - t).coerceIn(0f, 1f),
                )
                // Particules déterministes.
                val seedX = ((c.x * 37 + c.y * 91 + index * 13) % 7) - 3
                val seedY = ((c.x * 53 + c.y * 29 + index * 7) % 7) - 3
                val dir = Offset(seedX / 3f, seedY / 3f - 0.4f)
                val distance = cell * 0.9f * easeOutCubic(t)
                val center = Offset(c.x * cell + cell / 2f, c.y * cell + cell / 2f) + dir * distance
                drawCircle(
                    color = style.light.copy(alpha = (1f - t) * 0.9f),
                    radius = cell * (0.055f - 0.02f * t) * (if (index % 3 == 0) 1.3f else 1f),
                    center = center,
                )
            }

            // Éclat blanc sur les lignes effacées.
            val flash = (1f - t) * 0.35f
            clear.rows.forEach { row ->
                drawRect(
                    color = Color.White.copy(alpha = flash),
                    topLeft = Offset(0f, row * cell),
                    size = Size(full, cell),
                )
            }
            clear.cols.forEach { col ->
                drawRect(
                    color = Color.White.copy(alpha = flash),
                    topLeft = Offset(col * cell, 0f),
                    size = Size(cell, full),
                )
            }
        }

        // Indice : contour animé sur les cases conseillées.
        if (fx.hint.isNotEmpty()) {
            for (c in fx.hint) {
                val topLeft = Offset(c.x * cell + cell * 0.05f, c.y * cell + cell * 0.05f)
                val side = cell * 0.90f
                drawRoundRect(
                    color = Ink.Accent.copy(alpha = 0.22f),
                    topLeft = topLeft,
                    size = Size(side, side),
                    cornerRadius = CornerRadius(blockCorner(side)),
                )
                drawRoundRect(
                    color = Ink.Accent,
                    topLeft = topLeft,
                    size = Size(side, side),
                    cornerRadius = CornerRadius(blockCorner(side)),
                    style = Stroke(width = (cell * 0.05f).coerceAtLeast(2f)),
                )
            }
        }

        // Fantôme de la pièce en cours de déplacement.
        if (preview != null && dragPiece != null) {
            for (c in dragPiece.cells) {
                val px = preview.x + c.x
                val py = preview.y + c.y
                if (px !in 0 until SIZE || py !in 0 until SIZE) continue
                if (preview.valid) {
                    drawBlock(
                        topLeft = Offset(px * cell, py * cell),
                        size = cell,
                        style = blockStyle(dragPiece.color),
                        alpha = 0.42f,
                    )
                } else {
                    drawRoundRect(
                        color = Ink.Danger.copy(alpha = 0.28f),
                        topLeft = Offset(px * cell + cell * 0.08f, py * cell + cell * 0.08f),
                        size = Size(cell * 0.84f, cell * 0.84f),
                        cornerRadius = CornerRadius(blockCorner(cell * 0.84f)),
                    )
                }
            }
            if (preview.valid) {
                val minX = dragPiece.cells.minOf { it.x }
                val minY = dragPiece.cells.minOf { it.y }
                val maxX = dragPiece.cells.maxOf { it.x }
                val maxY = dragPiece.cells.maxOf { it.y }
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset((preview.x + minX) * cell + cell * 0.05f, (preview.y + minY) * cell + cell * 0.05f),
                    size = Size((maxX - minX + 1) * cell - cell * 0.1f, (maxY - minY + 1) * cell - cell * 0.1f),
                    cornerRadius = CornerRadius(cell * 0.22f),
                    style = Stroke(width = cell * 0.035f),
                )
            }
        }
    }
}

/** Pièce qui suit le doigt, dessinée au-dessus de tout. */
@Composable
fun DragGizmo(drag: DragState?, cell: Float, modifier: Modifier = Modifier) {
    if (drag == null) return
    Canvas(modifier.fillMaxSize()) {
        val width = drag.piece.width * cell
        val height = drag.piece.height * cell
        val topLeft = drag.pointer - drag.grab
        // Ombre portée.
        drawRoundRect(
            color = Ink.Shadow.copy(alpha = 0.35f),
            topLeft = Offset(topLeft.x + cell * 0.12f, topLeft.y + cell * 0.18f),
            size = Size(width - cell * 0.1f, height - cell * 0.1f),
            cornerRadius = CornerRadius(cell * 0.32f),
        )
        for (c in drag.piece.cells) {
            drawBlock(
                topLeft = Offset(topLeft.x + c.x * cell, topLeft.y + c.y * cell),
                size = cell,
                style = blockStyle(drag.piece.color),
                scale = 1.06f,
            )
        }
    }
}

/** Main de trois pièces. */
@Composable
fun Tray(
    pieces: List<Piece?>,
    drag: DragState?,
    boardCell: Float,
    onDragStart: (slot: Int, piece: Piece, pointerRoot: Offset, grab: Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier.fillMaxWidth().height(118.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) { slot ->
            val piece = pieces.getOrNull(slot)
            TraySlot(
                piece = piece,
                isDragging = drag?.slot == slot,
                boardCell = boardCell,
                modifier = Modifier.weight(1f),
                onDragStart = { pointer, grab -> piece?.let { onDragStart(slot, it, pointer, grab) } },
                onDrag = onDrag,
                onDragEnd = onDragEnd,
            )
        }
    }
}

@Composable
private fun TraySlot(
    piece: Piece?,
    isDragging: Boolean,
    boardCell: Float,
    modifier: Modifier,
    onDragStart: (pointerRoot: Offset, grab: Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    var slotSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier
            .onGloballyPositioned {
                origin = it.positionInRoot()
                slotSize = it.size
            },
    ) {
        val slotW = constraints.maxWidth.toFloat()
        val slotH = constraints.maxHeight.toFloat()
        val cell = if (piece == null) 0f else trayCell(piece, slotW, slotH)
        val pieceW = (piece?.width ?: 0) * cell
        val pieceH = (piece?.height ?: 0) * cell
        val topLeft = Offset((slotW - pieceW) / 2f, (slotH - pieceH) / 2f)

        Canvas(Modifier.fillMaxSize()) {
            if (piece == null || isDragging) {
                // Emplacement vide : petit socle discret.
                val base = minOf(size.width, size.height) * 0.28f
                drawRoundRect(
                    color = Ink.SurfaceLow.copy(alpha = if (isDragging) 0.9f else 0.45f),
                    topLeft = Offset((size.width - base) / 2f, (size.height - base) / 2f),
                    size = Size(base, base),
                    cornerRadius = CornerRadius(base * 0.32f),
                    style = Stroke(width = base * 0.06f),
                )
                return@Canvas
            }
            for (c in piece.cells) {
                drawBlock(
                    topLeft = Offset(topLeft.x + c.x * cell, topLeft.y + c.y * cell),
                    size = cell,
                    style = blockStyle(piece.color),
                )
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(piece, boardCell, slotW, slotH) {
                    if (piece == null) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            val scale = if (cell > 0f) boardCell / cell else 1f
                            val grab = Offset(
                                (offset.x - topLeft.x) * scale,
                                (offset.y - topLeft.y) * scale,
                            )
                            onDragStart(origin + offset, grab)
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                    )
                },
        )
    }
}

/** Taille d'une case dans la main, pour que toutes les pièces tiennent. */
fun trayCell(piece: Piece, slotW: Float, slotH: Float): Float {
    val byWidth = slotW / (piece.width + 0.9f)
    val byHeight = slotH / (piece.height + 0.7f)
    return minOf(byWidth, byHeight, slotH * 0.36f)
}

fun targetCell(pointer: Offset, grab: Offset, boardOrigin: Offset, cell: Float): Pair<Int, Int> {
    val x = ((pointer.x - grab.x - boardOrigin.x) / cell).roundToInt()
    val y = ((pointer.y - grab.y - boardOrigin.y) / cell).roundToInt()
    return x to y
}

fun Preview?.isSameAs(other: Preview?): Boolean =
    this?.x == other?.x && this?.y == other?.y && this?.valid == other?.valid

private fun easeOutCubic(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t)
private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f) * (t - 1f) * (t - 1f) + c1 * (t - 1f) * (t - 1f)
}

/** Petit utilitaire de dessin pour l'écran de jeu. */
fun DrawScope.drawFrameGlow(cell: Float, color: Color, alpha: Float) {
    drawRoundRect(
        color = color.copy(alpha = alpha),
        size = Size(cell * SIZE, cell * SIZE),
        cornerRadius = CornerRadius(cell * 0.62f),
        style = Stroke(width = cell * 0.05f),
    )
}
