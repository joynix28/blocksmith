package com.botillon.blocksmith.game

import kotlin.random.Random

/**
 * Règles pures du jeu. Aucune dépendance Android : tout est testable sur JVM
 * et réutilisable par l'IA d'indice.
 */
object Rules {

    /** Points gagnés par case posée. */
    const val POINT_PER_CELL = 1L

    /** Base d'une ligne effacée, multipliée par le combo courant. */
    const val POINT_PER_LINE = 10L

    /** Bonus quand le plateau est entièrement vidé. */
    const val PERFECT_BONUS = 300L

    /** Nombre de poses sans effacement avant de perdre le combo. */
    const val COMBO_GRACE = 3

    /** Bonus fixe selon le nombre de lignes effacées d'un coup. */
    fun multiLineBonus(lines: Int): Long = when (lines) {
        0 -> 0
        1 -> 10
        2 -> 20
        3 -> 60
        4 -> 120
        5 -> 200
        else -> 300
    }

    fun fits(board: Board, piece: Piece, ox: Int, oy: Int): Boolean {
        for (cell in piece.cells) {
            val x = ox + cell.x
            val y = oy + cell.y
            if (!board.inBounds(x, y)) return false
            if (board.isFilled(x, y)) return false
        }
        return true
    }

    fun anyFits(board: Board, piece: Piece): Boolean {
        val maxX = SIZE - piece.width
        val maxY = SIZE - piece.height
        if (maxX < 0 || maxY < 0) return false
        for (y in 0..maxY) for (x in 0..maxX) {
            if (fits(board, piece, x, y)) return true
        }
        return false
    }

    fun placements(board: Board, piece: Piece): List<Coord> = buildList {
        val maxX = SIZE - piece.width
        val maxY = SIZE - piece.height
        if (maxX < 0 || maxY < 0) return@buildList
        for (y in 0..maxY) for (x in 0..maxX) {
            if (fits(board, piece, x, y)) add(Coord(x, y))
        }
    }

    fun fullRows(board: Board): List<Int> = (0 until SIZE).filter { board.rowFull(it) }
    fun fullCols(board: Board): List<Int> = (0 until SIZE).filter { board.colFull(it) }

    /** Résultat d'une pose simulée, sans toucher à la main. */
    data class Simulation(
        val board: Board,
        val coords: List<Coord>,
        val lines: Int,
        val cleared: List<Coord>,
        val rows: List<Int>,
        val cols: List<Int>,
        val perfect: Boolean,
    )

    fun simulate(state: GameState, piece: Piece, ox: Int, oy: Int): Simulation? {
        if (!fits(state.board, piece, ox, oy)) return null
        val coords = piece.cells.map { Coord(ox + it.x, oy + it.y) }
        var board = state.board
        for (c in coords) board = board.set(c.x, c.y, piece.color)
        val rows = fullRows(board)
        val cols = fullCols(board)
        val cleared = buildList {
            rows.forEach { y -> for (x in 0 until SIZE) add(Coord(x, y)) }
            cols.forEach { x -> for (y in 0 until SIZE) add(Coord(x, y)) }
        }.distinct()
        board = board.without(cleared)
        return Simulation(
            board = board,
            coords = coords,
            lines = rows.size + cols.size,
            cleared = cleared,
            rows = rows,
            cols = cols,
            perfect = board.filledCount() == 0,
        )
    }

    /**
     * Applique une pose réelle. Renvoie null si la pose est illégale.
     * [traySource] fournit la main suivante quand les trois pièces sont posées.
     */
    fun apply(
        state: GameState,
        slot: Int,
        ox: Int,
        oy: Int,
        traySource: TraySource,
        goal: Goal? = null,
        par: Int = 0,
    ): MoveResult? {
        if (state.status != Status.PLAYING) return null
        val piece = state.pieceAt(slot) ?: return null
        val sim = simulate(state, piece, ox, oy) ?: return null

        val events = mutableListOf<GameEvent>()
        val placedPoints = piece.area * POINT_PER_CELL
        var score = state.score + placedPoints
        events += GameEvent.Placed(piece, ox, oy, sim.coords, placedPoints)

        var combo = state.combo
        var missStreak = state.missStreak
        var linesTotal = state.lines
        var gemsCleared = state.gemsCleared

        if (sim.lines > 0) {
            combo += 1
            missStreak = 0
            linesTotal += sim.lines
            gemsCleared += sim.cleared.count { state.board.hasGem(it.x, it.y) }

            val points = sim.lines * POINT_PER_LINE * combo + multiLineBonus(sim.lines)
            val perfectPoints = if (sim.perfect) PERFECT_BONUS * combo else 0
            score += points + perfectPoints
            events += GameEvent.ComboUp(combo)
            events += GameEvent.Cleared(
                ClearFx(
                    lines = sim.lines,
                    coords = sim.cleared,
                    points = points + perfectPoints,
                    combo = combo,
                    perfect = sim.perfect,
                    rows = sim.rows,
                    cols = sim.cols,
                ),
            )
        } else {
            missStreak += 1
            if (missStreak >= COMBO_GRACE) {
                combo = 0
                missStreak = 0
            }
        }

        val remaining = state.tray.toMutableList().also { it[slot] = null }
        val tray = if (remaining.all { it == null }) traySource.next(sim.board) else remaining
        val moves = state.moves + 1

        var status = Status.PLAYING
        var stars = 0

        val goalReached = goal != null && when (goal) {
            is Goal.ClearLines -> linesTotal >= goal.target
            is Goal.ReachScore -> score >= goal.target
            is Goal.CollectGems -> gemsCleared >= goal.target
            is Goal.Survive -> moves >= goal.target
        }

        if (goalReached) {
            status = Status.WON
            stars = starsFor(moves, par)
            events += GameEvent.Won(stars)
        } else if (tray.none { it != null && anyFits(sim.board, it) }) {
            status = Status.GAME_OVER
            events += GameEvent.GameOver
        }

        return MoveResult(
            state = state.copy(
                board = sim.board,
                tray = tray,
                score = score,
                combo = combo,
                missStreak = missStreak,
                moves = moves,
                lines = linesTotal,
                gemsCleared = gemsCleared,
                status = status,
                stars = stars,
            ),
            events = events,
        )
    }

    /** 3 étoiles si l'objectif est atteint dans le nombre de poses prévu. */
    fun starsFor(moves: Int, par: Int): Int = when {
        moves <= par -> 3
        moves <= par + 5 -> 2
        else -> 1
    }

    fun legalMoveCount(state: GameState): Int = state.tray.sumOf { piece ->
        if (piece == null) 0 else placements(state.board, piece).size
    }
}

/** Source des trois pièces suivantes. Interface pour pouvoir la simuler. */
fun interface TraySource {
    fun next(board: Board): List<Piece?>
}

/**
 * Générateur de main. Objectif : rester exigeant sans jamais tuer le joueur
 * gratuitement. On garantit qu'au moins une pièce de la nouvelle main est
 * posable, et on réduit le poids des grosses pièces quand le plateau se remplit.
 */
class PieceGenerator(
    private val random: Random = Random.Default,
    private val catalog: List<Shape> = Shapes.all,
) : TraySource {

    override fun next(board: Board): List<Piece?> {
        val filled = board.filledCount().toFloat() / (SIZE * SIZE)

        repeat(30) {
            val picks = List(3) { pickShape(filled) }
            if (picks.any { shape -> Rules.anyFits(board, Piece(shape, Palette.CORAL)) }) {
                return colorize(picks)
            }
        }

        val fitting = catalog.filter { Rules.anyFits(board, Piece(it, Palette.CORAL)) }
        if (fitting.isEmpty()) return emptyList()
        return colorize(List(3) { fitting[random.nextInt(fitting.size)] })
    }

    private fun colorize(shapes: List<Shape>): List<Piece> {
        val colors = pickColors()
        return shapes.mapIndexed { index, shape -> Piece(shape, colors[index]) }
    }

    private fun pickShape(filled: Float): Shape {
        val weights = catalog.map { shape ->
            val factor = when {
                filled > 0.72f -> if (shape.area <= 3) 2.4f else 0.35f
                filled > 0.55f -> if (shape.area <= 4) 1.6f else 0.7f
                else -> 1f
            }
            shape.weight * factor
        }
        return catalog[weightedIndex(weights)]
    }

    private fun weightedIndex(weights: List<Float>): Int {
        val total = weights.sum()
        var target = random.nextFloat() * total
        for (i in weights.indices) {
            target -= weights[i]
            if (target <= 0f) return i
        }
        return weights.lastIndex
    }

    private fun pickColors(): List<Palette> {
        val start = random.nextInt(Palette.entries.size)
        val step = if (random.nextBoolean()) 1 else 2
        return List(3) { i -> Palette.entries[(start + i * step) % Palette.entries.size] }
    }
}
