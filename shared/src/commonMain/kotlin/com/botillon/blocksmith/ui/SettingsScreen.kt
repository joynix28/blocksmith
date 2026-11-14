package com.botillon.blocksmith.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botillon.blocksmith.game.Levels

@Composable
fun SettingsScreen(
    progress: Progress,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onLang: (Lang) -> Unit,
    onZoom: (Float) -> Unit,
    onReset: () -> Unit,
) {
    val s = LocalStrings.current
    var confirmReset by remember { mutableStateOf(false) }

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
                Column {
                    AppText(s.settings, style = Typo.label, color = Ink.Accent)
                    AppText(s.settingsSub, style = Typo.heading, color = Ink.Text)
                }
            }

            Spacer(Modifier.height(20.dp))

            Panel(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    SettingRow(
                        title = s.sound,
                        subtitle = s.soundSub,
                        checked = progress.sound,
                        onChange = onToggleSound,
                        icon = { color -> drawSpeaker(center, size.minDimension, color, progress.sound) },
                    )
                    Divider()
                    SettingRow(
                        title = s.haptics,
                        subtitle = s.hapticsSub,
                        checked = progress.haptics,
                        onChange = onToggleHaptics,
                        icon = { color -> drawHeart(center, size.minDimension * 0.9f, color) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Panel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    AppText(s.language, style = Typo.body.copy(fontSize = 15.sp), color = Ink.Text)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        ChoiceChip("Français", progress.lang == Lang.FR) { onLang(Lang.FR) }
                        Spacer(Modifier.width(8.dp))
                        ChoiceChip("English", progress.lang == Lang.EN) { onLang(Lang.EN) }
                    }
                    Spacer(Modifier.height(16.dp))
                    AppText(s.zoom, style = Typo.body.copy(fontSize = 15.sp), color = Ink.Text)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        ChoiceChip(s.zoomSmall, progress.zoom < 0.95f) { onZoom(0.88f) }
                        Spacer(Modifier.width(8.dp))
                        ChoiceChip(s.zoomNormal, progress.zoom in 0.95f..1.05f) { onZoom(1f) }
                        Spacer(Modifier.width(8.dp))
                        ChoiceChip(s.zoomLarge, progress.zoom > 1.05f) { onZoom(1.14f) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Panel(Modifier.fillMaxWidth(), color = Ink.Surface.copy(alpha = 0.7f)) {
                Column(Modifier.padding(16.dp)) {
                    AppText(s.stats, style = Typo.label, color = Ink.Muted)
                    Spacer(Modifier.height(8.dp))
                    StatLine(s.bestScore, if (progress.best > 0) "${progress.best}" else "—")
                    StatLine(s.totalStars, "${progress.totalStars} / ${Levels.all.size * 3}")
                    StatLine(s.levelsDone, "${progress.clearedLevels} / ${Levels.all.size}")
                }
            }

            Spacer(Modifier.weight(1f))

            SecondaryButton(s.resetProgress, { confirmReset = true }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            AppText(
                "Blocksmith 1.1",
                style = Typo.label,
                color = Ink.Faint,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
            AppText(
                s.about,
                style = Typo.body.copy(fontSize = 12.sp),
                color = Ink.Faint,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
        }

        if (confirmReset) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070510).copy(alpha = 0.78f))
                    .clickable { confirmReset = false },
                contentAlignment = Alignment.Center,
            ) {
                Panel(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText(s.resetTitle, style = Typo.title, color = Ink.Text)
                        Spacer(Modifier.height(8.dp))
                        AppText(
                            s.resetBody,
                            style = Typo.body,
                            color = Ink.Muted,
                            align = TextAlign.Center,
                            maxLines = 3,
                            softWrap = true,
                        )
                        Spacer(Modifier.height(20.dp))
                        PrimaryButton(
                            s.erase,
                            {
                                onReset()
                                confirmReset = false
                            },
                            Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(s.cancel, { confirmReset = false }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Ink.Accent else Ink.SurfaceHigh)
            .then(
                if (selected) Modifier else Modifier.border(1.dp, Ink.Faint.copy(alpha = 0.5f), RoundedCornerShape(50)),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        AppText(
            label,
            style = Typo.body.copy(fontSize = 12.sp),
            color = if (selected) Color(0xFF22180A) else Ink.TextSoft,
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.drawscope.DrawScope.(Color) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(Ink.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(19.dp)) { icon(Ink.Accent) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            AppText(title, style = Typo.body.copy(fontSize = 15.sp), color = Ink.Text)
            AppText(subtitle, style = Typo.body.copy(fontSize = 11.sp), color = Ink.Muted)
        }
        Switch(checked = checked, onChange = onChange)
    }
}

@Composable
private fun Switch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val knob by animateFloatAsState(if (checked) 1f else 0f, tween(160), label = "switch")
    Box(
        Modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (checked) Brush.horizontalGradient(listOf(Ink.Accent, Ink.AccentDeep))
                else Brush.horizontalGradient(listOf(Ink.SurfaceLow, Ink.SurfaceLow)),
            )
            .clickable { onChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = (3 + knob * 20).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFF22180A) else Ink.Muted),
        )
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.05f)),
    )
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        AppText(label, style = Typo.body, color = Ink.Muted, modifier = Modifier.weight(1f))
        AppText(value, style = Typo.body.copy(fontSize = 15.sp), color = Ink.Text)
    }
}
