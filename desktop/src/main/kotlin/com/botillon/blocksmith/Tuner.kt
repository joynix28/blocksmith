package com.botillon.blocksmith

import com.botillon.blocksmith.game.Advisor
import com.botillon.blocksmith.game.GameSession
import com.botillon.blocksmith.game.Levels
import com.botillon.blocksmith.game.Status

/**
 * Outil de calibration hors production : rejoue chaque niveau avec le bot et
 * affiche le nombre de poses nécessaire. Utile après une modification des
 * règles ou des objectifs.
 *
 *   ./gradlew :desktop:tune
 */
fun main() {
    var wins = 0
    for (level in Levels.all) {
        var state = GameSession(level, level.seed).let { session ->
            var current = session.initial
            var guard = 0
            while (current.status == Status.PLAYING && guard++ < 400) {
                val suggestion = Advisor.suggest(current, level.goal) ?: break
                val result = session.place(current, suggestion.slot, suggestion.x, suggestion.y) ?: break
                current = result.state
            }
            current
        }
        if (state.status == Status.WON) wins++
        println(
            "niveau ${level.id.toString().padStart(2)}  ${level.goal.label.padEnd(26)} " +
                "par ${level.par.toString().padStart(2)}  ->  ${state.status.name.padEnd(9)} " +
                "${state.moves} poses, ${state.score} pts, ${state.stars}★",
        )
    }
    println("$wins/48 niveaux gagnés par le bot")
}
