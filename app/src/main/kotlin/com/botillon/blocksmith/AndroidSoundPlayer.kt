package com.botillon.blocksmith

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.botillon.blocksmith.ui.Sound
import com.botillon.blocksmith.ui.SoundPlayer

/** Effets sonores synthétisés, chargés une fois, joués via SoundPool. */
class AndroidSoundPlayer(context: Context) : SoundPlayer {

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids: Map<Sound, Int> = mapOf(
        Sound.PLACE to pool.load(context, R.raw.place, 1),
        Sound.CLEAR to pool.load(context, R.raw.clear, 1),
        Sound.CLEAR_BIG to pool.load(context, R.raw.clear_big, 1),
        Sound.COMBO to pool.load(context, R.raw.combo, 1),
        Sound.WIN to pool.load(context, R.raw.win, 1),
        Sound.LOSE to pool.load(context, R.raw.lose, 1),
        Sound.UI to pool.load(context, R.raw.ui, 1),
    )

    private val volumes = mapOf(
        Sound.PLACE to 0.55f,
        Sound.CLEAR to 0.75f,
        Sound.CLEAR_BIG to 0.85f,
        Sound.COMBO to 0.8f,
        Sound.WIN to 0.9f,
        Sound.LOSE to 0.7f,
        Sound.UI to 0.4f,
    )

    override fun play(sound: Sound) {
        val id = ids[sound] ?: return
        pool.play(id, volumes[sound] ?: 0.7f, volumes[sound] ?: 0.7f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
