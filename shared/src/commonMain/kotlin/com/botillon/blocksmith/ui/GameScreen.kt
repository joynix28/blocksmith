package com.botillon.blocksmith.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botillon.blocksmith.game.Advisor
import com.botillon.blocksmith.game.Board
import com.botillon.blocksmith.game.ClearFx
import com.botillon.blocksmith.game.Coord
import com.botillon.blocksmith.game.GameEvent
import com.botillon.blocksmith.game.GameSession
import com.botillon.blocksmith.game.GameState
import com.botillon.blocksmith.game.Level
import com.botillon.blocksmith.game.Rules
import com.botillon.blocksmith.game.SIZE
import com.botillon.blocksmith.game.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private sealed interface Overlay {
    data class Won(val stars: Int) : Overlay
    data object Lost : Overlay
}

private data class Banner(val text: String, val color: Color, val stamp: Long)

@Composable
fun GameScreen(
    level: Level?,
    seed: Long,
    best: Long,
    onExit: () -> Unit,
    onResult: (GameResult) -> Unit,
    onReplay: () -> Unit,
    onNextLevel: (() -> Unit)?,
) {
    val sound = LocalSound.current
    val haptics = LocalHaptics.current

    val session = remember(level?.id, seed) { GameSession(level, seed) }
    var state by remember { mutableStateOf(session.initial) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var banner by remember { mutableStateOf<Banner?>(null) }

    var drag by remember { mutableStateOf<DragState?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var boardOrigin by remember { mutableStateOf(Offset.Zero) }
    var boardSize by remember { mutableStateOf(Size.Zero) }

    var hint by remember { mutableStateOf<List<Coord>>(emptyList()) }

    val clearProgress = remember { Animatable(1f) }
    val popProgress = remember { Animatable(1f) }
    val shake = remember { Animatable(0f) }
    var clearFx by remember { mutableStateOf<ClearFx?>(null) }
    var clearBoard by remember { mutableStateOf<Board?>(null) }
    var placedCells by remember { mutableStateOf<List<Coord>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val boardCell = if (boardSize.width > 0f) boardSize.width / SIZE else 0f
    val shownScore = remember { Animatable(0f) }
    LaunchedEffect(state.score) {
        val gap = state.score - shownScore.value
        shownScore.animateTo(
            targetValue = state.score.toFloat(),
            animationSpec = tween(if (gap > 400) 420 else 200, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(banner) {
        if (banner != null) {
            delay(1_500)
            banner = null
        }
    }
    LaunchedEffect(hint) {
        if (hint.isNotEmpty()) {
            delay(3_500)
            hint = emptyList()
        }
    }

    fun applyMove(slot: Int, x: Int, y: Int) {
        if (state.status != Status.PLAYING) return
        val before = state.board
        val result = session.place(state, slot, x, y) ?: return
        state = result.state

        for (event in result.events) {
            when (event) {
                is GameEvent.Placed -> {
                    placedCells = event.cells
                    scope.launch {
                        popProgress.snapTo(0f)
                        popProgress.animateTo(1f, tween(190, easing = FastOutSlowInEasing))
                    }
                    sound.play(Sound.PLACE)
                    haptics.tap()
                }

                is GameEvent.Cleared -> {
                    clearFx = event.fx
                    clearBoard = before
                    scope.launch {
                        clearProgress.snapTo(0f)
                        clearProgress.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
                    }
                    if (event.fx.lines >= 3 || event.fx.perfect) {
                        sound.play(Sound.CLEAR_BIG)
                        scope.launch {
                            shake.snapTo(1f)
                            shake.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
                        }
                    } else {
                        sound.play(Sound.CLEAR)
                    }
                    haptics.thud()
                    banner = bannerFor(event.fx)
                }

                is GameEvent.ComboUp -> if (event.combo >= 5) sound.play(Sound.COMBO)

                is GameEvent.Won -> {
                    sound.play(Sound.WIN)
                    haptics.success()
                    onResult(GameResult(level?.id, result.state.score, event.stars, true))
                    scope.launch {
                        delay(480)
                        overlay = Overlay.Won(event.stars)
                    }
                }

                is GameEvent.GameOver -> {
                    sound.play(Sound.LOSE)
                    onResult(GameResult(level?.id, result.state.score, 0, false))
                    scope.launch {
                        delay(520)
                        overlay = Overlay.Lost
                    }
                }
            }
        }
    }

    fun endDrag() {
        val current = drag
        val target = preview
        drag = null
        preview = null
        if (current != null && target != null && target.valid) {
            applyMove(current.slot, target.x, target.y)
        }
    }

    fun showHint() {
        if (state.status != Status.PLAYING) return
        val suggestion = Advisor.suggest(state) ?: return
        val piece = state.pieceAt(suggestion.slot) ?: return
        hint = piece.cells.map { Coord(suggestion.x + it.x, suggestion.y + it.y) }
        sound.play(Sound.UI)
    }

    Backdrop {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircleButton(onClick = onExit) {
                        drawChevronLeft(center, size.minDimension, Ink.TextSoft)
                    }
                    Spacer(Modifier.width(10.dp))
                    BestPill(best)
                    Spacer(Modifier.weight(1f))
                    if (level != null) {
                        Chip("${state.moves}/${level.par} poses")
                        Spacer(Modifier.width(8.dp))
                    }
                    CircleButton(onClick = { showHint() }) {
                        drawBulb(center, size.minDimension, Ink.Accent)
                    }
                }

                Spacer(Modifier.height(2.dp))

                ScoreHeader(
                    score = shownScore.value.toLong(),
                    combo = state.combo,
                    banner = banner,
                )

                if (level != null) {
                    Spacer(Modifier.height(8.dp))
                    GoalCard(level = level, state = state)
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.layout.BoxWithConstraints {
                        val side = minOf(constraints.maxWidth, constraints.maxHeight).toFloat() / density.density
                        GameBoard(
                            state = state,
                            fx = BoardFx(
                                clear = clearFx,
                                clearBoard = clearBoard,
                                clearProgress = clearProgress.value,
                                placed = placedCells,
                                placedProgress = popProgress.value,
                                hint = hint,
                            ),
                            preview = preview,
                            dragPiece = drag?.piece,
                            modifier = Modifier
                                .size(side.dp)
                                .graphicsLayer {
                                    translationX = shake.value * with(density) { 7.dp.toPx() } * if (shake.value > 0.5f) 1f else -1f
                                },
                            onGeometry = { origin, size ->
                                boardOrigin = origin
                                boardSize = size
                            },
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Tray(
                    pieces = state.tray,
                    drag = drag,
                    boardCell = boardCell,
                    onDragStart = { slot, piece, pointer, grab ->
                        if (state.status == Status.PLAYING) {
                            drag = DragState(slot, piece, pointer, grab)
                            preview = null
                            haptics.tap()
                        }
                    },
                    onDrag = { amount ->
                        val current = drag
                        if (current != null) {
                            val moved = current.copy(pointer = current.pointer + amount)
                            drag = moved
                            if (boardCell > 0f) {
                                val (cx, cy) = targetCell(moved.pointer, moved.grab, boardOrigin, boardCell)
                                preview = Preview(cx, cy, Rules.fits(state.board, moved.piece, cx, cy))
                            }
                        }
                    },
                    onDragEnd = { endDrag() },
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        text = footerText(state, level),
                        style = Typo.body.copy(fontSize = 12.sp),
                        color = Ink.Muted,
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        DragGizmo(
            drag = drag,
            cell = boardCell,
            modifier = Modifier.fillMaxSize(),
        )

        val currentOverlay = overlay
        if (currentOverlay != null) {
            ResultOverlay(
                won = currentOverlay is Overlay.Won,
                stars = (currentOverlay as? Overlay.Won)?.stars ?: 0,
                score = state.score,
                best = best,
                level = level,
                onReplay = onReplay,
                onNext = onNextLevel,
                onExit = onExit,
            )
        }
    }
}

@Composable
private fun ScoreHeader(score: Long, combo: Int, banner: Banner?) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(
                text = "$score",
                style = Typo.score,
                color = Ink.Text,
            )
            if (combo >= 2) {
                Spacer(Modifier.width(10.dp))
                ComboBadge(combo)
            }
        }
        val text = banner?.text
        if (text != null) {
            AppText(
                text = text,
                style = Typo.label.copy(letterSpacing = 2.sp),
                color = banner.color,
            )
        }
    }
}

@Composable
private fun ComboBadge(combo: Int) {
    val pulse by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "combo",
    )
    Box(
        Modifier
            .graphicsLayer { scaleX = 0.9f + pulse * 0.1f; scaleY = 0.9f + pulse * 0.1f }
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Ink.Combo, Color(0xFF8A6BFF))))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        AppText("×$combo", style = Typo.heading.copy(fontSize = 15.sp), color = Color.White)
    }
}

@Composable
private fun GoalCard(level: Level, state: GameState) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Ink.Surface.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(
                text = level.goal.label,
                style = Typo.body.copy(fontSize = 13.sp),
                color = Ink.TextSoft,
                modifier = Modifier.weight(1f),
            )
            AppText(
                text = "${state.progress(level.goal).times(100).toInt()} %",
                style = Typo.label,
                color = Ink.Accent,
            )
        }
        Spacer(Modifier.height(7.dp))
        ProgressLine(progress = state.progress(level.goal))
    }
}

@Composable
fun ResultOverlay(
    won: Boolean,
    stars: Int,
    score: Long,
    best: Long,
    level: Level?,
    onReplay: () -> Unit,
    onNext: (() -> Unit)?,
    onExit: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(if (visible) 1f else 0.88f, tween(220), label = "panel")
    val fade by animateFloatAsState(if (visible) 1f else 0f, tween(220), label = "fade")

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF070510).copy(alpha = 0.72f * fade)),
        contentAlignment = Alignment.Center,
    ) {
        Panel(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppText(
                    text = if (won) "NIVEAU RÉUSSI" else if (level != null) "OBJECTIF MANQUÉ" else "PARTIE TERMINÉE",
                    style = Typo.label.copy(letterSpacing = 2.sp),
                    color = if (won) Ink.Mint else Ink.Muted,
                )
                Spacer(Modifier.height(10.dp))
                AppText(
                    text = "$score",
                    style = Typo.score,
                    color = Ink.Text,
                )
                if (won) {
                    Spacer(Modifier.height(4.dp))
                    StarRow(stars = stars, size = 30.dp, gap = 8.dp)
                }
                if (score >= best && best > 0) {
                    Spacer(Modifier.height(8.dp))
                    Chip("NOUVEAU RECORD", color = Color(0xFF22180A), background = Ink.Accent)
                }
                if (!won && level != null) {
                    Spacer(Modifier.height(8.dp))
                    AppText(
                        text = level.goal.label,
                        style = Typo.body,
                        color = Ink.Muted,
                        align = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(22.dp))

                PrimaryButton(
                    label = if (won && onNext != null) "CONTINUER" else "REJOUER",
                    onClick = { if (won && onNext != null) onNext() else onReplay() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    label = "MENU",
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun bannerFor(fx: ClearFx): Banner? {
    val text = when {
        fx.perfect -> "PARFAIT !"
        fx.lines >= 4 -> "QUADRUPLÉ !"
        fx.lines == 3 -> "TRIPLE !"
        fx.combo >= 10 -> "LÉGENDAIRE"
        fx.combo >= 8 -> "INCROYABLE"
        fx.combo >= 6 -> "EN FEU"
        fx.combo >= 4 -> "SUPERBE"
        fx.lines == 2 -> "DOUBLE !"
        else -> return null
    }
    val color = if (fx.perfect) Ink.Mint else Ink.Accent
    return Banner(text, color, fx.combo.toLong())
}

private fun footerText(state: GameState, level: Level?): String {
    if (level != null) return "Objectif : ${level.goal.label.lowercase()}"
    return if (state.combo >= 2) "Combo ×${state.combo}" else "Continue comme ça"
}
