package com.botillon.blocksmith.ui

import androidx.compose.animation.Crossfade
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import blocksmith.shared.generated.resources.Res
import blocksmith.shared.generated.resources.space_grotesk_bold
import blocksmith.shared.generated.resources.space_grotesk_medium
import blocksmith.shared.generated.resources.space_grotesk_regular
import com.botillon.blocksmith.game.Levels
import org.jetbrains.compose.resources.Font
import kotlin.math.max
import kotlin.random.Random

@Composable
fun App(
    store: ProgressStore,
    sound: SoundPlayer,
    haptics: Haptics,
) {
    val family = FontFamily(
        Font(Res.font.space_grotesk_regular, FontWeight.Normal),
        Font(Res.font.space_grotesk_medium, FontWeight.Medium),
        Font(Res.font.space_grotesk_medium, FontWeight.SemiBold),
        Font(Res.font.space_grotesk_bold, FontWeight.Bold),
        Font(Res.font.space_grotesk_bold, FontWeight.ExtraBold),
        Font(Res.font.space_grotesk_bold, FontWeight.Black),
    )
    Typo.family = family

    var progress by remember { mutableStateOf(store.load()) }
    val baseDensity = LocalDensity.current
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var seed by remember { mutableStateOf(Random.nextLong()) }

    val day = remember { todayEpochDay() }
    val dailyLevel = remember(day) { Daily.forDay(day) }

    fun update(transform: (Progress) -> Progress) {
        val next = transform(progress)
        progress = next
        store.save(next)
    }

    fun startClassic() {
        seed = Random.nextLong()
        screen = Screen.Play(null)
    }

    fun startLevel(levelId: Int) {
        seed = Levels.byId(levelId)?.seed ?: Random.nextLong()
        screen = Screen.Play(levelId)
    }

    fun startDaily() {
        seed = dailyLevel.seed
        screen = Screen.Daily
    }

    fun onResult(result: GameResult) {
        update { current ->
            var next = current
            val levelId = result.levelId
            if (levelId != null && levelId > 0 && result.won) {
                val previous = current.stars[levelId] ?: 0
                next = next.copy(stars = next.stars + (levelId to max(previous, result.stars)))
            }
            if (levelId == null) {
                next = next.copy(best = max(next.best, result.score))
            }
            if (levelId != null && levelId < 0) {
                next = next.copy(
                    dailyDay = day,
                    dailyBest = max(next.dailyBest, result.score),
                )
            }
            next
        }
    }

    val effectiveSound = remember(progress.sound, sound) { if (progress.sound) sound else NoopSound }
    val effectiveHaptics = remember(progress.haptics, haptics) { if (progress.haptics) haptics else NoopHaptics }

    CompositionLocalProvider(
        LocalSound provides effectiveSound,
        LocalHaptics provides effectiveHaptics,
        LocalStrings provides Strings(progress.lang),
        LocalDensity provides Density(baseDensity.density * progress.zoom, baseDensity.fontScale),
    ) {
        HandleBack(enabled = screen != Screen.Home) {
            screen = Screen.Home
        }

        Crossfade(targetState = screen, animationSpec = tween(240), label = "nav") { current ->
            when (current) {
                Screen.Home -> HomeScreen(
                    best = progress.best,
                    totalStars = progress.totalStars,
                    clearedLevels = progress.clearedLevels,
                    onPlay = { startClassic() },
                    onLevels = { screen = Screen.Levels },
                    onDaily = { startDaily() },
                    onSettings = { screen = Screen.Settings },
                )

                Screen.Levels -> LevelsScreen(
                    progress = progress,
                    onBack = { screen = Screen.Home },
                    onPlay = { levelId -> startLevel(levelId) },
                )

                Screen.Settings -> SettingsScreen(
                    progress = progress,
                    onBack = { screen = Screen.Home },
                    onToggleSound = { enabled -> update { it.copy(sound = enabled) } },
                    onToggleHaptics = { enabled -> update { it.copy(haptics = enabled) } },
                    onLang = { lang -> update { it.copy(lang = lang) } },
                    onZoom = { zoom -> update { it.copy(zoom = zoom) } },
                    onReset = {
                        update { p ->
                            Progress(
                                best = 0,
                                stars = emptyMap(),
                                sound = p.sound,
                                haptics = p.haptics,
                                lang = p.lang,
                                zoom = p.zoom,
                            )
                        }
                    },
                )

                is Screen.Play -> {
                    val level = current.levelId?.let { Levels.byId(it) }
                    val hasNext = level != null && Levels.byId(level.id + 1) != null
                    GameScreen(
                        level = level,
                        seed = seed,
                        best = progress.best,
                        onExit = { screen = if (level == null) Screen.Home else Screen.Levels },
                        onResult = ::onResult,
                        onReplay = {
                            seed = if (level == null) Random.nextLong() else level.seed
                            screen = Screen.Play(level?.id)
                        },
                        onNextLevel = if (hasNext && level != null) {
                            { startLevel(level.id + 1) }
                        } else {
                            null
                        },
                    )
                }

                Screen.Daily -> GameScreen(
                    level = dailyLevel,
                    seed = dailyLevel.seed,
                    best = progress.dailyBest,
                    onExit = { screen = Screen.Home },
                    onResult = ::onResult,
                    onReplay = {
                        seed = dailyLevel.seed
                        screen = Screen.Daily
                    },
                    onNextLevel = null,
                )
            }
        }
    }
}
