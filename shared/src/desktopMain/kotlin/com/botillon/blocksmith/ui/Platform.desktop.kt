package com.botillon.blocksmith.ui

import androidx.compose.runtime.Composable

actual fun todayEpochDay(): Int = (System.currentTimeMillis() / 86_400_000L).toInt()

@Composable
actual fun HandleBack(enabled: Boolean, onBack: () -> Unit) = Unit
