package com.botillon.blocksmith.game

import kotlin.random.Random

/**
 * Les niveaux sont générés de façon déterministe : même identifiant = même
 * plateau, même graine de pièces. Aucune donnée réseau, aucun fichier à charger.
 *
 * La courbe a été calibrée en faisant jouer un bot sur les 48 niveaux : les
 * objectifs restent atteignables, mais demandent de mieux en mieux jouer.
 */
data class Level(
    val id: Int,
    val chapter: Int,
    val name: String,
    val goal: Goal,
    val par: Int,
    val seed: Long,
    val start: List<PlacedCell> = emptyList(),
)

object Chapters {
    data class Info(val index: Int, val title: String, val subtitle: String)

    val all = listOf(
        Info(0, "Le Jardin", "Premiers pas, lignes tranquilles"),
        Info(1, "Crépuscule", "Les gemmes s'invitent"),
        Info(2, "Nébuleuse", "La course au score"),
        Info(3, "Aurore", "Tout se mélange"),
    )

    fun of(index: Int): Info = all[index.coerceIn(0, all.lastIndex)]
}

object Levels {

    const val PER_CHAPTER = 12
    val all: List<Level> by lazy { build() }

    fun byId(id: Int): Level? = all.firstOrNull { it.id == id }

    fun chapterLevels(chapter: Int): List<Level> = all.filter { it.chapter == chapter }

    /**
     * Graines et « par » calibrés : chaque niveau a été joué par le bot jusqu'à
     * une victoire, et on garde la graine qui demande le moins de poses.
     * Le par vaut ce minimum + 2, pour laisser une marge au joueur.
     */
    private val tuning: Map<Int, Pair<Long, Int>> = mapOf(
        1 to (8746L to 10),
        2 to (16479L to 12),
        3 to (24212L to 14),
        4 to (31945L to 16),
        5 to (668052L to 9),
        6 to (466327L to 8),
        7 to (264602L to 11),
        8 to (1529083L to 11),
        9 to (175339L to 11),
        10 to (287801L to 12),
        11 to (295534L to 13),
        12 to (407996L to 13),
        13 to (101542L to 11),
        14 to (1261294L to 7),
        15 to (1269027L to 11),
        16 to (543657L to 9),
        17 to (1075035L to 10),
        18 to (1082768L to 12),
        19 to (1509417L to 11),
        20 to (1726608L to 9),
        21 to (1001238L to 14),
        22 to (1637345L to 16),
        23 to (1435620L to 16),
        24 to (1443353L to 18),
        25 to (299067L to 13),
        26 to (1039903L to 13),
        27 to (1257094L to 17),
        28 to (1264827L to 18),
        29 to (329999L to 22),
        30 to (233003L to 21),
        31 to (554923L to 22),
        32 to (876843L to 22),
        33 to (1094034L to 24),
        34 to (578122L to 27),
        35 to (481126L to 27),
        36 to (907775L to 27),
        37 to (287134L to 18),
        38 to (399596L to 16),
        39 to (302600L to 12),
        40 to (519791L to 39),
        41 to (318066L to 22),
        42 to (1687276L to 18),
        43 to (752448L to 12),
        44 to (445994L to 40),
        45 to (348998L to 26),
        46 to (1089834L to 24),
        47 to (573922L to 15),
        48 to (1628945L to 36),
    )

    private fun build(): List<Level> = buildList {
        for (chapter in 0..3) {
            repeat(PER_CHAPTER) { i ->
                val id = chapter * PER_CHAPTER + i + 1
                val (seed, par) = tuning.getValue(id)
                val goal = goalFor(chapter, i)
                val gems = (goal as? Goal.CollectGems)?.target ?: 0
                add(
                    Level(
                        id = id,
                        chapter = chapter,
                        name = "Niveau $id",
                        goal = goal,
                        par = par,
                        seed = seed,
                        start = decoration(chapter, i, seed, gems),
                    ),
                )
            }
        }
    }

    private fun goalFor(chapter: Int, i: Int): Goal = when (chapter) {
        0 -> if (i < 4) Goal.Survive(8 + i * 2) else Goal.ClearLines(2 + i / 3)

        1 -> when {
            i % 3 == 2 -> Goal.ClearLines(4 + i / 3)
            else -> Goal.CollectGems(2 + i / 4)
        }

        2 -> Goal.ReachScore(250 + i * 55)

        else -> when (i % 4) {
            0 -> Goal.Survive(16 + i)
            1 -> Goal.ClearLines(7 + i / 3)
            2 -> Goal.CollectGems(2 + i / 4)
            else -> Goal.ReachScore(900 + i * 60)
        }
    }


    /**
     * Motif de départ. Volontairement léger : quelques blocs suffisent à changer
     * la lecture du plateau sans rendre la partie injouable. Les gemmes sont
     * dispersées pour forcer de vraies lignes à compléter.
     */
    private fun decoration(chapter: Int, index: Int, seed: Long, gemsNeeded: Int): List<PlacedCell> {
        if (chapter == 0 && index < 4) return emptyList()

        val random = Random(seed)
        val palette = Palette.entries
        val cells = mutableListOf<PlacedCell>()

        fun put(x: Int, y: Int, gem: Boolean = false) {
            if (x !in 0 until SIZE || y !in 0 until SIZE) return
            if (cells.any { it.x == x && it.y == y }) return
            cells += PlacedCell(x, y, palette[random.nextInt(palette.size)], gem)
        }

        if (gemsNeeded > 0) {
            // Gemmes réparties : chacune demande de compléter sa ligne ou sa colonne.
            val spots = listOf(
                1 to 5, 6 to 2, 3 to 6, 7 to 3,
                0 to 2, 5 to 7, 2 to 1, 4 to 4,
            ).shuffled(random)

            var placed = 0
            for ((x, y) in spots) {
                if (placed >= gemsNeeded) break
                put(x, y, gem = true)
                placed++
                // Un compagnon pour éviter la case totalement isolée.
                if (placed % 2 == 0) put(x, y - 1)
            }
            repeat(2) { k -> put(k, SIZE - 1) }
            return cells
        }

        when (chapter) {
            0 -> {
                repeat(2 + index % 2) { k -> put(k, 0) }
                repeat(2) { k -> put(SIZE - 1, SIZE - 1 - k) }
            }

            1 -> {
                repeat(4) { k -> put(1 + k, 0) }
                repeat(3) { k -> put(1 + k, 1) }
            }

            2 -> {
                repeat(3 + index % 2) { k -> put(k, 0) }
                repeat(2) { k -> put(0, 1 + k) }
                repeat(2) { k -> put(SIZE - 1, SIZE - 1 - k) }
                put(SIZE - 2, SIZE - 1)
            }

            else -> {
                repeat(4) { k -> put(1 + k, 0) }
                repeat(3) { k -> put(6, 6 - k) }
                put(0, 7)
                put(0, 6)
            }
        }
        return cells
    }
}
