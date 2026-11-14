package com.botillon.blocksmith.game

/**
 * Petit moteur d'évaluation : sert à la fois d'indice pour le joueur et de
 * joueur automatique pour les tests de non-régression.
 *
 * L'idée est celle d'un bon joueur humain : effacer des lignes quand c'est
 * rentable, coller les blocs aux murs et entre eux, et surtout ne pas creuser
 * de trous impossibles à combler.
 */
object Advisor {

    data class Suggestion(
        val slot: Int,
        val x: Int,
        val y: Int,
        val lines: Int,
        val rating: Double,
    )

    fun suggest(state: GameState, goal: Goal? = null): Suggestion? {
        var best: Suggestion? = null
        for (slot in state.tray.indices) {
            val piece = state.tray[slot] ?: continue
            val others = state.tray.filterIndexed { index, _ -> index != slot }.filterNotNull()
            for (pos in Rules.placements(state.board, piece)) {
                val sim = Rules.simulate(state, piece, pos.x, pos.y) ?: continue
                val rating = rate(state.board, sim, piece) +
                    goalBonus(state.board, sim, goal) +
                    flexibility(sim.board, others)
                if (best == null || rating > best!!.rating) {
                    best = Suggestion(slot, pos.x, pos.y, sim.lines, rating)
                }
            }
        }
        return best
    }

    /**
     * Critère décisif : après la pose, les pièces encore en main doivent pouvoir
     * se poser. C'est ce qui distingue un bon joueur d'un joueur qui « remplit
     * juste les trous » et se retrouve coincé au tour suivant.
     */
    private fun flexibility(board: Board, remaining: List<Piece>): Double {
        if (remaining.isEmpty()) return 0.0
        var score = 0.0
        for (piece in remaining) {
            val count = Rules.placements(board, piece).size
            score += if (count == 0) -1_200.0 else minOf(count, 14) * 7.0
        }
        return score
    }

    /** Un bon indice sert l'objectif du niveau, pas seulement le score. */
    private fun goalBonus(before: Board, sim: Rules.Simulation, goal: Goal?): Double = when (goal) {
        is Goal.CollectGems -> sim.cleared.count { before.hasGem(it.x, it.y) } * 900.0
        is Goal.ClearLines -> sim.lines * 60.0
        is Goal.ReachScore -> sim.lines * 40.0
        is Goal.Survive -> 0.0
        null -> 0.0
    }

    /** Note une pose simulée. Plus c'est haut, mieux c'est. */
    fun rate(before: Board, sim: Rules.Simulation, piece: Piece): Double {
        var score = 0.0

        // Effacer est toujours prioritaire, mais pas au point de tout sacrifier.
        score += sim.lines * 140.0
        if (sim.perfect) score += 600.0

        // Contact avec les murs et les blocs existants : un plateau compact.
        var contacts = 0
        for (c in sim.coords) {
            if (c.x == 0 || c.x == SIZE - 1) contacts += 2
            if (c.y == SIZE - 1) contacts += 3
            for (d in NEIGHBORS) {
                val nx = c.x + d.x
                val ny = c.y + d.y
                if (before.isFilled(nx, ny)) contacts += 2
            }
        }
        score += contacts * 1.5

        // Trous : une case vide sous un bloc plein est très difficile à récupérer.
        score -= holes(sim.board) * 22.0

        // Cases vides isolées : elles ne pourront jamais être remplies.
        score -= isolatedCells(sim.board) * 14.0

        // Les grosses pièces sont plus faciles à placer tôt : inutile de sur-pénaliser.
        score -= piece.area * 0.4

        return score
    }

    fun holes(board: Board): Int {
        var holes = 0
        for (x in 0 until SIZE) {
            var seenBlock = false
            for (y in 0 until SIZE) {
                if (board.isFilled(x, y)) seenBlock = true
                else if (seenBlock) holes++
            }
        }
        return holes
    }

    fun isolatedCells(board: Board): Int {
        var isolated = 0
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            if (board.isFilled(x, y)) continue
            val freeNeighbors = NEIGHBORS.count { !board.isFilled(x + it.x, y + it.y) }
            if (freeNeighbors == 0) isolated++
        }
        return isolated
    }

    private val NEIGHBORS = listOf(Coord(0, -1), Coord(0, 1), Coord(-1, 0), Coord(1, 0))
}
