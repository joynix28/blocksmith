package com.botillon.blocksmith

import com.botillon.blocksmith.ui.Progress
import com.botillon.blocksmith.ui.ProgressStore
import com.botillon.blocksmith.ui.Sound
import com.botillon.blocksmith.ui.SoundPlayer
import java.io.File
import java.util.Properties
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/** Store de progression pour le runner desktop : un fichier Properties. */
class DesktopProgressStore : ProgressStore {

    private val file = File(System.getProperty("user.home"), ".blocksmith/progress.properties")

    override fun load(): Progress {
        if (!file.exists()) return Progress()
        return runCatching {
            val props = Properties()
            file.inputStream().use { props.load(it) }
            Progress(
                best = props.getProperty("best")?.toLongOrNull() ?: 0L,
                stars = props.getProperty("stars").orEmpty()
                    .split(',')
                    .mapNotNull { entry ->
                        val parts = entry.split(':')
                        if (parts.size != 2) return@mapNotNull null
                        val id = parts[0].toIntOrNull() ?: return@mapNotNull null
                        val stars = parts[1].toIntOrNull() ?: return@mapNotNull null
                        id to stars
                    }
                    .toMap(),
                sound = props.getProperty("sound")?.toBooleanStrictOrNull() ?: true,
                haptics = props.getProperty("haptics")?.toBooleanStrictOrNull() ?: true,
                dailyDay = props.getProperty("dailyDay")?.toIntOrNull() ?: 0,
                dailyBest = props.getProperty("dailyBest")?.toLongOrNull() ?: 0L,
                lang = runCatching {
                    com.botillon.blocksmith.ui.Lang.valueOf(props.getProperty("lang", "FR"))
                }.getOrDefault(com.botillon.blocksmith.ui.Lang.FR),
                zoom = props.getProperty("zoom")?.toFloatOrNull() ?: 1f,
            )
        }.getOrDefault(Progress())
    }

    override fun save(progress: Progress) {
        runCatching {
            file.parentFile?.mkdirs()
            val props = Properties()
            props.setProperty("best", progress.best.toString())
            props.setProperty(
                "stars",
                progress.stars.entries.joinToString(",") { "${it.key}:${it.value}" },
            )
            props.setProperty("sound", progress.sound.toString())
            props.setProperty("haptics", progress.haptics.toString())
            props.setProperty("dailyDay", progress.dailyDay.toString())
            props.setProperty("dailyBest", progress.dailyBest.toString())
            props.setProperty("lang", progress.lang.name)
            props.setProperty("zoom", progress.zoom.toString())
            file.outputStream().use { props.store(it, "Blocksmith") }
        }
    }
}

/** Joue les WAV embarqués, avec un cache de Clip par son. */
class DesktopSoundPlayer : SoundPlayer {

    private val clips = mutableMapOf<Sound, Clip>()

    private fun clipFor(sound: Sound): Clip? = clips.getOrPut(sound) {
        val name = when (sound) {
            Sound.PLACE -> "place"
            Sound.CLEAR -> "clear"
            Sound.CLEAR_BIG -> "clear_big"
            Sound.COMBO -> "combo"
            Sound.WIN -> "win"
            Sound.LOSE -> "lose"
            Sound.UI -> "ui"
        }
        val stream = javaClass.getResourceAsStream("/sounds/$name.wav") ?: return null
        val audio: AudioInputStream = AudioSystem.getAudioInputStream(stream)
        AudioSystem.getClip().apply {
            open(audio)
            audio.close()
        }
    }.takeIf { it.isOpen }

    override fun play(sound: Sound) {
        val clip = clipFor(sound) ?: return
        runCatching {
            clip.stop()
            clip.framePosition = 0
            clip.start()
        }
    }
}
