package com.botillon.blocksmith

import android.content.Context
import android.content.SharedPreferences
import com.botillon.blocksmith.ui.Progress
import com.botillon.blocksmith.ui.ProgressStore

/** Progression locale : SharedPreferences, aucune synchronisation réseau. */
class AndroidProgressStore(context: Context) : ProgressStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blocksmith.progress", Context.MODE_PRIVATE)

    override fun load(): Progress {
        val stars = prefs.getString(KEY_STARS, "")
            .orEmpty()
            .split(',')
            .mapNotNull { entry ->
                val parts = entry.split(':')
                if (parts.size != 2) return@mapNotNull null
                val id = parts[0].toIntOrNull() ?: return@mapNotNull null
                val count = parts[1].toIntOrNull() ?: return@mapNotNull null
                id to count
            }
            .toMap()

        return Progress(
            best = prefs.getLong(KEY_BEST, 0L),
            stars = stars,
            sound = prefs.getBoolean(KEY_SOUND, true),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            dailyDay = prefs.getInt(KEY_DAILY_DAY, 0),
            dailyBest = prefs.getLong(KEY_DAILY_BEST, 0L),
        )
    }

    override fun save(progress: Progress) {
        val stars = progress.stars.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit()
            .putLong(KEY_BEST, progress.best)
            .putString(KEY_STARS, stars)
            .putBoolean(KEY_SOUND, progress.sound)
            .putBoolean(KEY_HAPTICS, progress.haptics)
            .putInt(KEY_DAILY_DAY, progress.dailyDay)
            .putLong(KEY_DAILY_BEST, progress.dailyBest)
            .apply()
    }

    private companion object {
        const val KEY_BEST = "best"
        const val KEY_STARS = "stars"
        const val KEY_SOUND = "sound"
        const val KEY_HAPTICS = "haptics"
        const val KEY_DAILY_DAY = "daily_day"
        const val KEY_DAILY_BEST = "daily_best"
    }
}
