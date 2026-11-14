package com.botillon.blocksmith

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.botillon.blocksmith.ui.App
import com.botillon.blocksmith.ui.Ink
import com.botillon.blocksmith.ui.NoopHaptics

/**
 * Runner desktop : sert à voir et à jouer le jeu pendant le développement,
 * exactement avec le même code d'interface que l'application Android.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Blocksmith",
        state = rememberWindowState(width = 440.dp, height = 900.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Ink.BgTop, Ink.BgMid, Ink.BgBottom))),
        ) {
            App(
                store = DesktopProgressStore(),
                sound = DesktopSoundPlayer(),
                haptics = NoopHaptics,
            )
        }
    }
}
