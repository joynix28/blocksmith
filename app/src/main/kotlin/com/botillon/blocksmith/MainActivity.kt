package com.botillon.blocksmith

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.botillon.blocksmith.ui.App
import com.botillon.blocksmith.ui.Ink

class MainActivity : ComponentActivity() {

    private lateinit var soundPlayer: AndroidSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        soundPlayer = AndroidSoundPlayer(this)
        val store = AndroidProgressStore(this)
        val haptics = AndroidHaptics(this)

        setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Ink.BgTop, Ink.BgMid, Ink.BgBottom))),
            ) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    App(store = store, sound = soundPlayer, haptics = haptics)
                }
            }
        }
    }

    override fun onDestroy() {
        soundPlayer.release()
        super.onDestroy()
    }
}
