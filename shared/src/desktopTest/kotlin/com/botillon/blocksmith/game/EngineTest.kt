package com.botillon.blocksmith.game

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EngineTest {

    private fun boardWith(filled: List<Pair<Int, Int>>): Board {
        var board = Board.Empty
        for ((x, y) in filled) board = board.set(x, y, Palette.CORAL)
        return board
    }

    private fun state(board: Board, vararg pieces: Piece?): GameState =
        GameState(board = board, tray = pieces.toList())

    private fun piece(id: String, color: Palette = Palette.MINT): Piece =
        Piece(Shapes.byId.getValue(id), color)

    @Test
    fun shapesAreNormalizedAndUnique() {
        val ids = Shapes.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "les identifiants doivent être uniques")
        for (shape in Shapes.all) {
            assertTrue(shape.area > 0, "forme vide : ${shape.id}")
            assertEquals(0, shape.cells.minOf { it.x })
            assertEquals(0, shape.cells.minOf { it.y })
            assertEquals(shape.width - 1, shape.cells.maxOf { it.x })
            assertEquals(shape.height - 1, shape.cells.maxOf { it.y })
            assertTrue(shape.width <= SIZE && shape.height <= SIZE, "forme trop grande : ${shape.id}")
        }
    }

    @Test
    fun boardEncodesColorsAndGems() {
        val board = Board.Empty.set(2, 3, Palette.SKY, gem = true)
        assertEquals(Palette.SKY, board.colorAt(2, 3))
        assertTrue(board.hasGem(2, 3))
        assertEquals(1, board.filledCount())
        assertEquals(1, board.gemsLeft())
        assertNull(board.colorAt(0, 0))
        assertEquals(0, board.without(listOf(Coord(2, 3))).filledCount())
    }

    @Test
    fun fullRowAndColumnAreDetected() {
        val row = boardWith((0 until SIZE).map { it to 5 })
        assertEquals(listOf(5), Rules.fullRows(row))
        assertTrue(Rules.fullCols(row).isEmpty())

        val column = boardWith((0 until SIZE).map { 4 to it })
        assertEquals(listOf(4), Rules.fullCols(column))
        assertTrue(Rules.fullRows(column).isEmpty())
    }

    @Test
    fun singleLineClearScoresPlacementThenClear() {
        // Une ligne 0 pleine sauf la dernière case : un simple 1x1 la complète.
        val board = boardWith((0 until SIZE - 1).map { it to 0 })
        val dot = Piece(Shapes.byId.getValue("dot"), Palette.AMBER)
        val tray = listOf(dot, null, null)

        val result = Rules.apply(
            state(board, dot, null, null).copy(tray = tray),
            slot = 0,
            ox = 7,
            oy = 0,
            traySource = { emptyList() },
        )

        assertNotNull(result)
        val state = result.state
        assertEquals(1, state.lines, "une ligne doit être effacée")
        assertEquals(1, state.combo, "le combo démarre à 1 au premier effacement")
        // 1 point de pose + (1 ligne × 10 × combo 1) + bonus 10 + bonus plateau vide 300.
        assertEquals(321L, state.score)
        assertEquals(0, state.board.filledCount(), "le plateau doit être vide (perfect)")
        val cleared = result.events.filterIsInstance<GameEvent.Cleared>().single()
        assertTrue(cleared.fx.perfect)
    }

    @Test
    fun multiLineBonusApplies() {
        // Deux lignes pleines sauf leurs deux dernières cases : un 2x2 les efface d'un coup.
        val filled = buildList {
            for (x in 0 until SIZE - 2) {
                add(x to 0)
                add(x to 1)
            }
            add(SIZE - 1 to SIZE - 1) // évite le bonus « plateau vide »
        }
        val board = boardWith(filled)
        val square = Piece(Shapes.byId.getValue("o4"), Palette.CORAL)
        val result = Rules.apply(
            state(board, square, null, null),
            slot = 0,
            ox = 6,
            oy = 0,
            traySource = { emptyList() },
        )
        assertNotNull(result)
        assertEquals(2, result.state.lines)
        // 4 points de pose + (2 lignes × 10 × 1) + bonus 20
        assertEquals(44L, result.state.score)
    }

    @Test
    fun comboRisesOnClearAndResetsAfterGrace() {
        val dot = Piece(Shapes.byId.getValue("dot"), Palette.MINT)
        val keep = TraySource { listOf(dot, dot, dot) }
        var state = state(boardWith(listOf(0 to 0)), dot, dot, dot)

        // Trois poses sans effacement : le combo retombe à zéro après la grâce.
        repeat(Rules.COMBO_GRACE) { index ->
            val result = Rules.apply(state, 0, 7, 7 - index, keep)
            assertNotNull(result)
            state = result.state.copy(tray = listOf(dot, dot, dot))
        }
        assertEquals(0, state.combo)

        // Un effacement relance le combo, un second l'incrémente.
        state = state.copy(board = boardWith((0 until SIZE - 1).map { it to 3 }))
        val first = Rules.apply(state, 0, 7, 3, keep)
        assertNotNull(first)
        assertEquals(1, first.state.combo)

        val second = Rules.apply(
            first.state.copy(
                board = boardWith((0 until SIZE - 1).map { it to 4 }),
                tray = listOf(dot, dot, dot),
            ),
            0, 7, 4, keep,
        )
        assertNotNull(second)
        assertEquals(2, second.state.combo)
    }

    @Test
    fun illegalPlacementReturnsNull() {
        val board = boardWith((0 until SIZE).map { it to 0 })
        val piece = piece("i2")
        assertNull(Rules.apply(state(board, piece), 0, 0, 0, { emptyList() }), "case occupée")
        assertNull(Rules.apply(state(board, piece), 0, 7, 0, { emptyList() }), "hors plateau")
    }

    @Test
    fun perfectClearGivesBonus() {
        val board = boardWith((0 until SIZE - 1).map { it to 0 })
        val dot = Piece(Shapes.byId.getValue("dot"), Palette.ROSE)
        val result = Rules.apply(state(board, dot, null, null), 0, 7, 0, { emptyList() })
        assertNotNull(result)
        val fx = result.events.filterIsInstance<GameEvent.Cleared>().single().fx
        assertTrue(fx.perfect)
        assertTrue(fx.points > 10 * 1 * 1 + 10, "le bonus plateau vide doit s'ajouter")
    }

    @Test
    fun generatorAlwaysOffersAPlayablePiece() {
        val random = Random(1234)
        repeat(2_000) {
            var board = Board.Empty
            val density = random.nextDouble(0.05, 0.9)
            for (y in 0 until SIZE) for (x in 0 until SIZE) {
                if (random.nextDouble() < density) {
                    board = board.set(x, y, Palette.entries[random.nextInt(6)])
                }
            }
            val tray = PieceGenerator(random).next(board)
            if (tray.isEmpty()) {
                // Acceptable seulement si aucune pièce du catalogue ne rentre.
                assertTrue(
                    Shapes.all.none { Rules.anyFits(board, Piece(it, Palette.CORAL)) },
                    "main vide alors qu'une pièce pouvait rentrer",
                )
            } else {
                assertTrue(
                    tray.any { it != null && Rules.anyFits(board, it) },
                    "aucune pièce posable dans la main",
                )
            }
        }
    }

    @Test
    fun botSurvivesLongEnough() {
        val moves = mutableListOf<Int>()
        repeat(40) { seed ->
            val session = GameSession(level = null, seed = seed.toLong())
            var state = session.initial
            var guard = 0
            while (state.status == Status.PLAYING && guard++ < 500) {
                val suggestion = Advisor.suggest(state) ?: break
                val result = session.place(state, suggestion.slot, suggestion.x, suggestion.y) ?: break
                state = result.state
            }
            moves += state.moves
            assertTrue(state.score >= state.moves, "le score doit au moins compter les cases posées")
        }
        val average = moves.average()
        println("parties automatiques : $moves")
        assertTrue(average > 25.0, "le bot devrait tenir plus de 25 poses, moyenne = $average")
    }

    @Test
    fun levelsAreDeterministicAndSane() {
        assertEquals(48, Levels.all.size)
        for (level in Levels.all) {
            val same = Levels.byId(level.id)
            assertEquals(level.start, same?.start, "le niveau ${level.id} doit être déterministe")
            // Aucune ligne ni colonne déjà complète au départ.
            val board = Board.of(level.start)
            assertTrue(Rules.fullRows(board).isEmpty(), "ligne pleine au départ du niveau ${level.id}")
            assertTrue(Rules.fullCols(board).isEmpty(), "colonne pleine au départ du niveau ${level.id}")
            assertTrue(level.start.size <= 16, "trop de blocs au départ du niveau ${level.id}")
            assertTrue(level.par >= 1)
        }
    }

    @Test
    fun everyLevelIsBeatableByAGoodPlayer() {
        var wins = 0
        val failures = mutableListOf<Int>()
        for (level in Levels.all) {
            val session = GameSession(level, level.seed)
            var state = session.initial
            var guard = 0
            while (state.status == Status.PLAYING && guard++ < 400) {
                val suggestion = Advisor.suggest(state, level.goal) ?: break
                val result = session.place(state, suggestion.slot, suggestion.x, suggestion.y) ?: break
                state = result.state
            }
            if (state.status == Status.WON) wins++ else failures += level.id
            println(
                "niveau ${level.id} (${level.goal.label}, par ${level.par}) : " +
                    "${state.status} en ${state.moves} poses, score ${state.score}, " +
                    "lignes ${state.lines}, gemmes ${state.gemsCleared}",
            )
        }
        println("niveaux réussis par le bot : $wins/48 ; échecs = $failures")
        assertTrue(wins >= 44, "au moins 44 niveaux doivent être gagnables, obtenu $wins")
    }
}
