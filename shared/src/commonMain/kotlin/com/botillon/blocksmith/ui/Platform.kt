package com.botillon.blocksmith.ui

import com.botillon.blocksmith.game.Goal
import com.botillon.blocksmith.game.Level
import androidx.compose.runtime.staticCompositionLocalOf

/** Sons disponibles. Chaque plateforme les implémente à sa façon. */
enum class Sound { PLACE, CLEAR, CLEAR_BIG, COMBO, WIN, LOSE, UI }

interface SoundPlayer {
    fun play(sound: Sound)
}

interface Haptics {
    fun tap()
    fun thud()
    fun success()
}

object NoopSound : SoundPlayer {
    override fun play(sound: Sound) = Unit
}

object NoopHaptics : Haptics {
    override fun tap() = Unit
    override fun thud() = Unit
    override fun success() = Unit
}

/** Progression locale. Aucun compte, aucun réseau. */
data class Progress(
    val best: Long = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val dailyDay: Int = 0,
    val dailyBest: Long = 0,
    val lang: Lang = Lang.FR,
    val zoom: Float = 1f,
) {
    val totalStars: Int get() = stars.values.sum()
    val clearedLevels: Int get() = stars.size
}

interface ProgressStore {
    fun load(): Progress
    fun save(progress: Progress)
}

val LocalSound = staticCompositionLocalOf<SoundPlayer> { NoopSound }
val LocalHaptics = staticCompositionLocalOf<Haptics> { NoopHaptics }

/** Résultat d'une partie, remonté à la navigation pour la sauvegarde. */
data class GameResult(
    val levelId: Int?,
    val score: Long,
    val stars: Int,
    val won: Boolean,
)

/** Écran de jeu encodé de façon simple pour la navigation. */
sealed interface Screen {
    data object Home : Screen
    data object Levels : Screen
    data object Settings : Screen
    data object Daily : Screen
    data class Play(val levelId: Int?) : Screen
}

/** Objectif du jour, déterministe à partir du numéro de jour. */
object Daily {
    fun forDay(day: Int) = Level(
        id = -day,
        chapter = 0,
        name = "Défi du jour",
        goal = Goal.ReachScore(1_500 + (day % 7) * 400),
        par = 26,
        seed = day * 7919L + 17L,
        start = emptyList(),
    )
}

/** Numéro de jour (epoch), pour le défi quotidien. */
expect fun todayEpochDay(): Int

/** Bouton retour système (Android) ; sans effet sur desktop. */
@androidx.compose.runtime.Composable
expect fun HandleBack(enabled: Boolean, onBack: () -> Unit)
