package com.botillon.blocksmith.game

import kotlin.random.Random

/**
 * Une partie : le plateau de départ, le générateur de pièces et la règle de pose.
 * L'état lui-même reste immuable et vit dans l'UI, ce qui rend le rendu Compose
 * trivial (un seul objet à observer).
 */
class GameSession(
    val level: Level?,
    seed: Long,
) {
    private val generator = PieceGenerator(Random(seed))

    val initial: GameState = run {
        val board = if (level == null) Board.Empty else Board.of(level.start)
        GameState(board = board, tray = generator.next(board))
    }

    fun place(state: GameState, slot: Int, x: Int, y: Int): MoveResult? =
        Rules.apply(
            state = state,
            slot = slot,
            ox = x,
            oy = y,
            traySource = generator,
            goal = level?.goal,
            par = level?.par ?: 0,
        )
}
