package com.botillon.blocksmith.game

/** Le plateau fait 8x8 cases, comme la référence dont on reprend les règles. */
const val SIZE = 8

/** Les six couleurs de pièces. L'ordre est utilisé pour l'encodage du plateau. */
enum class Palette { CORAL, AMBER, MINT, SKY, VIOLET, ROSE }

data class Coord(val x: Int, val y: Int) {
    operator fun plus(other: Coord) = Coord(x + other.x, y + other.y)
}

/**
 * Plateau immuable. Chaque case est encodée sur un Int :
 * 0 = vide, bits 0..2 = index de couleur + 1, bit 4 = gemme.
 * Toutes les opérations renvoient un nouveau plateau : c'est ce qui rend
 * l'IA, les indices et les tests triviaux à écrire.
 */
class Board private constructor(internal val cells: IntArray) {

    companion object {
        const val GEM = 16
        private const val COLOR_MASK = 7

        val Empty = Board(IntArray(SIZE * SIZE))

        fun index(x: Int, y: Int) = y * SIZE + x

        fun of(placed: List<PlacedCell>): Board {
            val data = IntArray(SIZE * SIZE)
            for (cell in placed) {
                data[index(cell.x, cell.y)] =
                    (cell.color.ordinal + 1) or (if (cell.gem) GEM else 0)
            }
            return Board(data)
        }
    }

    fun inBounds(x: Int, y: Int) = x in 0 until SIZE && y in 0 until SIZE

    fun value(x: Int, y: Int): Int =
        if (!inBounds(x, y)) -1 else cells[index(x, y)]

    fun isFilled(x: Int, y: Int) = value(x, y) > 0

    fun colorAt(x: Int, y: Int): Palette? {
        val v = value(x, y)
        return if (v <= 0) null else Palette.entries[(v and COLOR_MASK) - 1]
    }

    fun hasGem(x: Int, y: Int) = value(x, y) and GEM != 0

    fun filledCount(): Int = cells.count { it != 0 }

    fun gemsLeft(): Int = cells.count { it and GEM != 0 }

    fun set(x: Int, y: Int, color: Palette, gem: Boolean = false): Board {
        val copy = cells.copyOf()
        copy[index(x, y)] = (color.ordinal + 1) or (if (gem) GEM else 0)
        return Board(copy)
    }

    fun without(coords: Collection<Coord>): Board {
        if (coords.isEmpty()) return this
        val copy = cells.copyOf()
        for (c in coords) copy[index(c.x, c.y)] = 0
        return Board(copy)
    }

    fun rowFull(y: Int): Boolean {
        for (x in 0 until SIZE) if (!isFilled(x, y)) return false
        return true
    }

    fun colFull(x: Int): Boolean {
        for (y in 0 until SIZE) if (!isFilled(x, y)) return false
        return true
    }

    fun copy(): Board = Board(cells.copyOf())
}

/** Une case posée sur le plateau (utilisée pour les niveaux et les tests). */
data class PlacedCell(
    val x: Int,
    val y: Int,
    val color: Palette,
    val gem: Boolean = false,
)

/**
 * Forme d'une pièce, exprimée en coordonnées normalisées (0,0 en haut à gauche).
 * [weight] pondère le tirage, [tier] sert à doser la difficulté (1 = petite pièce).
 */
data class Shape(
    val id: String,
    val cells: List<Coord>,
    val weight: Int,
    val tier: Int,
) {
    val width: Int = cells.maxOf { it.x } + 1
    val height: Int = cells.maxOf { it.y } + 1
    val area: Int = cells.size
}

/** Une pièce = une forme + une couleur. */
data class Piece(val shape: Shape, val color: Palette) {
    val cells: List<Coord> get() = shape.cells
    val area: Int get() = shape.area
    val width: Int get() = shape.width
    val height: Int get() = shape.height
}

enum class Status { PLAYING, GAME_OVER, WON }

/** Objet d'un niveau. */
sealed interface Goal {
    val label: String
    val target: Int

    data class ClearLines(override val target: Int) : Goal {
        override val label: String get() = "Efface $target lignes"
    }

    data class ReachScore(override val target: Int) : Goal {
        override val label: String get() = "Marque $target points"
    }

    data class CollectGems(override val target: Int) : Goal {
        override val label: String get() = "Récupère $target gemmes"
    }

    data class Survive(override val target: Int) : Goal {
        override val label: String get() = "Pose $target pièces"
    }
}

data class GameState(
    val board: Board,
    val tray: List<Piece?>,
    val score: Long = 0,
    val combo: Int = 0,
    val missStreak: Int = 0,
    val moves: Int = 0,
    val lines: Int = 0,
    val gemsCleared: Int = 0,
    val status: Status = Status.PLAYING,
    val stars: Int = 0,
) {
    fun pieceAt(slot: Int): Piece? = tray.getOrNull(slot)

    val trayEmpty: Boolean get() = tray.all { it == null }

    /** Progression vers l'objectif, entre 0 et 1. */
    fun progress(goal: Goal): Float = when (goal) {
        is Goal.ClearLines -> (lines.toFloat() / goal.target).coerceIn(0f, 1f)
        is Goal.ReachScore -> (score.toFloat() / goal.target).coerceIn(0f, 1f)
        is Goal.CollectGems -> (gemsCleared.toFloat() / goal.target).coerceIn(0f, 1f)
        is Goal.Survive -> (moves.toFloat() / goal.target).coerceIn(0f, 1f)
    }
}

/** Détail d'un effacement, utilisé par l'UI pour les animations. */
data class ClearFx(
    val lines: Int,
    val coords: List<Coord>,
    val points: Long,
    val combo: Int,
    val perfect: Boolean,
    val rows: List<Int>,
    val cols: List<Int>,
)

sealed interface GameEvent {
    data class Placed(
        val piece: Piece,
        val x: Int,
        val y: Int,
        val cells: List<Coord>,
        val points: Long,
    ) : GameEvent

    data class Cleared(val fx: ClearFx) : GameEvent
    data class ComboUp(val combo: Int) : GameEvent
    data object GameOver : GameEvent
    data class Won(val stars: Int) : GameEvent
}

data class MoveResult(val state: GameState, val events: List<GameEvent>)
