package com.botillon.blocksmith

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.botillon.blocksmith.ui.Haptics

/** Vibrations courtes et sobres, désactivables dans les réglages. */
class AndroidHaptics(context: Context) : Haptics {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun buzz(millis: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching {
            v.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        }
    }

    override fun tap() = buzz(8, 60)
    override fun thud() = buzz(18, 120)
    override fun success() = buzz(28, 160)
}
