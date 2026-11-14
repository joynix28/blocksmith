package com.botillon.blocksmith.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botillon.blocksmith.game.Chapters
import com.botillon.blocksmith.game.Level
import com.botillon.blocksmith.game.Levels

@Composable
fun LevelsScreen(
    progress: Progress,
    onBack: () -> Unit,
    onPlay: (Int) -> Unit,
) {
    val strings = LocalStrings.current
    val highestDone = progress.stars.keys.filter { it > 0 }.maxOrNull() ?: 0
    val unlocked = (highestDone + 1).coerceAtMost(Levels.all.size)

    Backdrop {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleButton(onClick = onBack) {
                    drawChevronLeft(center, size.minDimension, Ink.TextSoft)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    AppText(strings.levels, style = Typo.label, color = Ink.Accent)
                    AppText(strings.chooseLevel, style = Typo.heading, color = Ink.Text)
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Ink.Surface.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(15.dp)) {
                        drawStar(center, size.minDimension * 0.48f, Ink.Accent)
                    }
                    Spacer(Modifier.width(6.dp))
                    AppText("${progress.totalStars}", style = Typo.body, color = Ink.Accent)
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Chapters.all.forEach { chapter ->
                    item(key = "chapter-${chapter.index}") {
                        ChapterHeader(
                            title = chapter.title,
                            subtitle = chapter.subtitle,
                            stars = Levels.chapterLevels(chapter.index).sumOf { progress.stars[it.id] ?: 0 },
                            total = Levels.PER_CHAPTER * 3,
                        )
                    }
                    val levels = Levels.chapterLevels(chapter.index)
                    items(levels.chunked(4).size) { rowIndex ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            levels.chunked(4)[rowIndex].forEach { level ->
                                LevelCard(
                                    level = level,
                                    stars = progress.stars[level.id] ?: 0,
                                    unlocked = level.id <= unlocked,
                                    current = level.id == unlocked && (progress.stars[level.id] ?: 0) == 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onPlay(level.id) },
                                )
                            }
                            repeat(4 - levels.chunked(4)[rowIndex].size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(6.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ChapterHeader(title: String, subtitle: String, stars: Int, total: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            AppText(title, style = Typo.title.copy(fontSize = 20.sp), color = Ink.Text)
            AppText(subtitle, style = Typo.body.copy(fontSize = 12.sp), color = Ink.Muted)
        }
        AppText("$stars/$total", style = Typo.label, color = Ink.Faint)
    }
}

@Composable
private fun LevelCard(
    level: Level,
    stars: Int,
    unlocked: Boolean,
    current: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val done = stars > 0
    val background = when {
        !unlocked -> Ink.SurfaceLow.copy(alpha = 0.55f)
        done -> Ink.Surface
        else -> Ink.SurfaceHigh.copy(alpha = 0.9f)
    }
    Box(
        modifier
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .then(
                if (current) {
                    Modifier.border(1.5.dp, Ink.Accent, RoundedCornerShape(18.dp))
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                },
            )
            .clickable(enabled = unlocked, onClick = onClick),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = "${level.id}",
                    style = Typo.heading.copy(fontSize = 21.sp),
                    color = if (unlocked) Ink.Text else Ink.Faint,
                )
                Spacer(Modifier.weight(1f))
                if (!unlocked) {
                    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
                        drawLock(center, size.minDimension, Ink.Faint)
                    }
                }
            }
            if (unlocked) {
                StarRow(stars = stars, size = 13.dp, gap = 2.dp)
            } else {
                AppText(LocalStrings.current.locked, style = Typo.label.copy(fontSize = 9.sp, letterSpacing = 0.6.sp), color = Ink.Faint)
            }
        }
        if (current) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(listOf(Ink.Accent, Ink.AccentDeep))),
            )
        }
    }
}
