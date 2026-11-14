package com.botillon.blocksmith.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    progress: Progress,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
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
                    AppText("RÉGLAGES", style = Typo.label, color = Ink.Accent)
                    AppText("À ta façon", style = Typo.heading, color = Ink.Text)
                }
            }

            Spacer(Modifier.height(22.dp))

            Panel(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    SettingRow(
                        title = "Sons",
                        subtitle = "Poses, effacements, combos",
                        checked = progress.sound,
                        onChange = onToggleSound,
                        icon = { color -> drawSpeaker(center, size.minDimension, color, progress.sound) },
                    )
                    Divider()
                    SettingRow(
                        title = "Vibrations",
                        subtitle = "Retour haptique discret",
                        checked = progress.haptics,
                        onChange = onToggleHaptics,
                        icon = { color -> drawHeart(center, size.minDimension * 0.9f, color) },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Panel(Modifier.fillMaxWidth(), color = Ink.Surface.copy(alpha = 0.7f)) {
                Column(Modifier.padding(18.dp)) {
                    AppText("STATISTIQUES", style = Typo.label, color = Ink.Muted)
                    Spacer(Modifier.height(10.dp))
                    StatLine("Meilleur score", if (progress.best > 0) "${progress.best}" else "—")
                    StatLine("Étoiles", "${progress.totalStars} / ${com.botillon.blocksmith.game.Levels.all.size * 3}")
                    StatLine("Niveaux terminés", "${progress.clearedLevels} / ${com.botillon.blocksmith.game.Levels.all.size}")
                }
            }

            Spacer(Modifier.weight(1f))

            SecondaryButton(
                label = "RÉINITIALISER LA PROGRESSION",
                onClick = { confirmReset = true },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))

            AppText(
                text = "Blocksmith 1.0",
                style = Typo.label,
                color = Ink.Faint,
                modifier = Modifier.fillMaxWidth(),
                align = androidx.compose.ui.text.style.TextAlign.Center,
            )
            AppText(
                text = "Sans publicité. Sans compte. Sans connexion.",
                style = Typo.body.copy(fontSize = 12.sp),
                color = Ink.Faint,
                modifier = Modifier.fillMaxWidth(),
                align = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        if (confirmReset) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070510).copy(alpha = 0.75f))
                    .clickable { confirmReset = false },
                contentAlignment = Alignment.Center,
            ) {
                Panel(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
                    Column(
                        Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AppText("Tout effacer ?", style = Typo.title, color = Ink.Text)
                        Spacer(Modifier.height(8.dp))
                        AppText(
                            "Le score record et les étoiles des niveaux seront perdus.",
                            style = Typo.body,
                            color = Ink.Muted,
                            align = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 3,
                            softWrap = true,
                        )
                        Spacer(Modifier.height(20.dp))
                        PrimaryButton(
                            label = "EFFACER",
                            onClick = {
                                onReset()
                                confirmReset = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            label = "ANNULER",
                            onClick = { confirmReset = false },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
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
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Ink.SurfaceHigh),
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
                if (checked) {
                    Brush.horizontalGradient(listOf(Ink.Accent, Ink.AccentDeep))
                } else {
                    Brush.horizontalGradient(listOf(Ink.SurfaceLow, Ink.SurfaceLow))
                },
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
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(label, style = Typo.body, color = Ink.Muted, modifier = Modifier.weight(1f))
        AppText(value, style = Typo.body.copy(fontSize = 15.sp), color = Ink.Text)
    }
}
