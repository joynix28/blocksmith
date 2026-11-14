package com.botillon.blocksmith.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class Lang { FR, EN }

class Strings(val lang: Lang) {
    private fun t(fr: String, en: String) = if (lang == Lang.FR) fr else en

    val best = t("record", "best")
    val play = t("JOUER", "PLAY")
    val levels = t("NIVEAUX", "LEVELS")
    val daily = t("DÉFI", "DAILY")
    val stars = t("étoiles", "stars")
    val cleared = t("niveaux", "levels")
    val tagline = t("Aligne. Efface. Recommence.", "Line up. Clear. Repeat.")
    val eyebrow = t("PUZZLE DE BLOCS", "BLOCK PUZZLE")
    val offline = t("Sans publicité · Sans compte · Hors-ligne", "No ads · No account · Offline")

    val chooseLevel = t("Choisis ton défi", "Pick a challenge")
    val locked = t("verrouillé", "locked")

    val settings = t("RÉGLAGES", "SETTINGS")
    val settingsSub = t("À ta façon", "Your way")
    val sound = t("Sons", "Sound")
    val soundSub = t("Poses, effacements, combos", "Placements, clears, combos")
    val haptics = t("Vibrations", "Vibration")
    val hapticsSub = t("Retour haptique discret", "Subtle haptic feedback")
    val stats = t("STATISTIQUES", "STATS")
    val bestScore = t("Meilleur score", "Best score")
    val totalStars = t("Étoiles", "Stars")
    val levelsDone = t("Niveaux terminés", "Levels cleared")
    val resetProgress = t("RÉINITIALISER LA PROGRESSION", "RESET PROGRESS")
    val about = t("Sans publicité. Sans compte. Sans connexion.", "No ads. No account. No connection.")
    val resetTitle = t("Tout effacer ?", "Erase everything?")
    val resetBody = t(
        "Le score record et les étoiles des niveaux seront perdus.",
        "Your best score and level stars will be lost.",
    )
    val erase = t("EFFACER", "ERASE")
    val cancel = t("ANNULER", "CANCEL")
    val language = t("Langue", "Language")
    val zoom = t("Taille de l'interface", "Interface size")
    val zoomSmall = t("Petit", "Small")
    val zoomNormal = t("Normal", "Normal")
    val zoomLarge = t("Grand", "Large")

    val level = t("Niveau", "Level")
    val moves = t("poses", "moves")
    val hint = t("Indice", "Hint")
    val keepGoing = t("Continue comme ça", "Keep going")
    val combo = t("Combo", "Combo")

    val win = t("NIVEAU RÉUSSI", "LEVEL CLEARED")
    val lose = t("OBJECTIF MANQUÉ", "GOAL MISSED")
    val gameOver = t("PARTIE TERMINÉE", "GAME OVER")
    val newRecord = t("NOUVEAU RECORD", "NEW RECORD")
    val continueLabel = t("CONTINUER", "CONTINUE")
    val replay = t("REJOUER", "PLAY AGAIN")
    val menu = t("MENU", "MENU")
    val dailyTitle = t("Défi du jour", "Daily challenge")

    val perfect = t("PARFAIT !", "PERFECT!")
    val quad = t("QUADRUPLÉ !", "QUAD!")
    val triple = t("TRIPLE !", "TRIPLE!")
    val legendary = t("LÉGENDAIRE", "LEGENDARY")
    val amazing = t("INCROYABLE", "AMAZING")
    val onFire = t("EN FEU", "ON FIRE")
    val superb = t("SUPERBE", "SUPERB")
    val double = t("DOUBLE !", "DOUBLE!")

    fun goal(goal: com.botillon.blocksmith.game.Goal): String = when (goal) {
        is com.botillon.blocksmith.game.Goal.ClearLines ->
            t("Efface ${goal.target} lignes", "Clear ${goal.target} lines")
        is com.botillon.blocksmith.game.Goal.ReachScore ->
            t("Marque ${goal.target} points", "Score ${goal.target} points")
        is com.botillon.blocksmith.game.Goal.CollectGems ->
            t("Récupère ${goal.target} gemmes", "Collect ${goal.target} gems")
        is com.botillon.blocksmith.game.Goal.Survive ->
            t("Pose ${goal.target} pièces", "Place ${goal.target} pieces")
    }
}

val LocalStrings = staticCompositionLocalOf { Strings(Lang.FR) }
