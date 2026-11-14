package com.botillon.blocksmith

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import com.botillon.blocksmith.ui.App
import com.botillon.blocksmith.ui.Haptics
import com.botillon.blocksmith.ui.Ink
import com.botillon.blocksmith.ui.NoopHaptics
import com.botillon.blocksmith.ui.NoopSound
import com.botillon.blocksmith.ui.Progress
import com.botillon.blocksmith.ui.ProgressStore
import com.botillon.blocksmith.ui.Sound
import com.botillon.blocksmith.ui.SoundPlayer
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/**
 * Harnais de rendu : rejoue des scénarios sur l'interface réelle et écrit des
 * captures PNG. Utilisé pour la revue visuelle et les captures du README.
 *
 *   ./gradlew :desktop:gallery
 */
private class GalleryStore(private var progress: Progress) : ProgressStore {
    override fun load(): Progress = progress
    override fun save(progress: Progress) {
        this.progress = progress
    }
}

private class LogSound : SoundPlayer {
    val played = mutableListOf<Sound>()
    override fun play(sound: Sound) {
        played += sound
    }
}

private class LogHaptics : Haptics {
    var count = 0
    override fun tap() { count++ }
    override fun thud() { count++ }
    override fun success() { count++ }
}

fun main(args: Array<String>) {
    val outDir = File(args.firstOrNull() ?: "/tmp/opencode/gallery").apply { mkdirs() }
    val density = 2f
    val store = GalleryStore(
        Progress(best = 18_420, stars = mapOf(1 to 3, 2 to 3, 3 to 2, 4 to 3, 5 to 1, 6 to 2)),
    )
    val sound = LogSound()
    val haptics = LogHaptics()

    val scene = ImageComposeScene(
        width = (440 * density).toInt(),
        height = (900 * density).toInt(),
        density = Density(density),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Ink.BgTop, Ink.BgMid, Ink.BgBottom))),
        ) {
            App(store = store, sound = sound, haptics = haptics)
        }
    }

    var clock = 0L
    fun advance(millis: Long = 700) {
        val target = clock + millis * 1_000_000
        while (clock < target) {
            clock += 16_000_000
            scene.render(clock)
        }
    }

    fun snap(name: String) {
        advance(260)
        val image = scene.render(clock)
        val data = image.encodeToData(EncodedImageFormat.PNG) ?: error("PNG encode failed")
        File(outDir, "$name.png").writeBytes(data.bytes)
        println("capture  $name.png")
    }

    fun frames(count: Int) {
        repeat(count) {
            clock += 16_000_000
            scene.render(clock)
        }
    }

    fun tapNow(x: Float, y: Float) {
        scene.sendPointerEvent(PointerEventType.Press, Offset(x * density, y * density), timeMillis = clock / 1_000_000)
        frames(1)
        scene.sendPointerEvent(PointerEventType.Release, Offset(x * density, y * density), timeMillis = clock / 1_000_000)
        frames(2)
    }

    fun snapNow(name: String) {
        val image = scene.render(clock)
        val data = image.encodeToData(EncodedImageFormat.PNG) ?: error("PNG encode failed")
        File(outDir, "$name.png").writeBytes(data.bytes)
        println("capture  $name.png")
    }

    fun press(x: Float, y: Float) {
        scene.sendPointerEvent(PointerEventType.Move, Offset(x * density, y * density), timeMillis = clock / 1_000_000)
        scene.sendPointerEvent(PointerEventType.Press, Offset(x * density, y * density), timeMillis = clock / 1_000_000)
    }

    fun release(x: Float, y: Float) {
        scene.sendPointerEvent(PointerEventType.Release, Offset(x * density, y * density), timeMillis = clock / 1_000_000)
    }

    fun click(x: Float, y: Float) {
        press(x, y)
        advance(60)
        release(x, y)
        advance(520)
    }

    fun drag(x1: Float, y1: Float, x2: Float, y2: Float, steps: Int = 12) {
        press(x1, y1)
        advance(40)
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val x = x1 + (x2 - x1) * t
            val y = y1 + (y2 - y1) * t
            scene.sendPointerEvent(
                PointerEventType.Move,
                Offset(x * density, y * density),
                timeMillis = clock / 1_000_000,
            )
            advance(24)
        }
        advance(120)
        release(x2, y2)
        advance(620)
    }

    snap("01-accueil")

    click(x = 220f, y = 758f)
    snap("02-partie-vide")
    tapNow(x = 400f, y = 32f)
    snapNow("02b-indice")

    // Trois poses simples pour voir le plateau se remplir.
    drag(82f, 803f, 300f, 520f)
    snap("03-pose-1")
    drag(220f, 803f, 180f, 470f)
    snap("04-pose-2")
    drag(358f, 803f, 260f, 420f)
    snap("05-pose-3")

    // Indice.
    click(x = 404f, y = 31f)
    snap("06-indice")

    // Retour accueil puis niveaux.
    click(x = 38f, y = 31f)
    click(x = 126f, y = 826f)
    snap("07-niveaux")

    // Premier niveau : carte en haut à gauche de la grille.
    click(x = 66f, y = 178f)
    snap("07b-niveau-1")
    click(x = 38f, y = 31f)
    snap("07c-retour-niveaux")

    // Réglages.
    click(x = 38f, y = 31f)
    click(x = 404f, y = 41f)
    snap("08-reglages")
    click(x = 220f, y = 200f)
    snap("09-reglages-son-off")

    // Panneaux de fin, rendus hors partie pour la revue visuelle.
    val overlayScene = ImageComposeScene(
        width = (440 * density).toInt(),
        height = (900 * density).toInt(),
        density = Density(density),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Ink.BgTop, Ink.BgMid, Ink.BgBottom))),
        ) {
            com.botillon.blocksmith.ui.ResultOverlay(
                won = true,
                stars = 3,
                score = 1_240,
                best = 980,
                level = com.botillon.blocksmith.game.Levels.byId(7),
                onReplay = {},
                onNext = {},
                onExit = {},
            )
        }
    }
    val overlayImage = overlayScene.render(60_000_000)
    File(outDir, "10-victoire.png").writeBytes(
        overlayImage.encodeToData(EncodedImageFormat.PNG)!!.bytes,
    )
    println("capture  10-victoire.png")
    overlayScene.close()

    println("sons joués : ${sound.played}")
    println("vibrations : ${haptics.count}")
    scene.close()
}
