package com.botillon.blocksmith.game

/**
 * Catalogue de pièces. Les formes de base sont décrites en ASCII puis
 * déclinées dans leurs quatre rotations : les pièces ne tournent pas en jeu,
 * donc chaque orientation est une entrée à part entière.
 */
object Shapes {

    private fun base(id: String, weight: Int, tier: Int, vararg rows: String): Shape {
        val cells = buildList {
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, c ->
                    if (c != '.' && c != ' ') add(Coord(x, y))
                }
            }
        }
        return Shape(id, cells, weight, tier)
    }

    private fun rotated(shape: Shape, quarterTurns: Int): Shape {
        var cells = shape.cells
        repeat(quarterTurns) {
            val height = cells.maxOf { it.y } + 1
            cells = cells.map { Coord(height - 1 - it.y, it.x) }
        }
        val minX = cells.minOf { it.x }
        val minY = cells.minOf { it.y }
        return shape.copy(cells = cells.map { Coord(it.x - minX, it.y - minY) })
    }

    private fun canonical(cells: List<Coord>): String =
        cells.sortedWith(compareBy({ it.y }, { it.x })).joinToString(";") { "${it.x},${it.y}" }

    /** Toutes les orientations uniques du catalogue, avec leurs poids. */
    val all: List<Shape> by lazy {
        val bases = listOf(
            base("dot", weight = 1, tier = 0, "*"),

            base("i2", weight = 9, tier = 1, "**"),
            base("i3", weight = 9, tier = 1, "***"),
            base("i4", weight = 5, tier = 2, "****"),
            base("i5", weight = 2, tier = 3, "*****"),

            base("o4", weight = 10, tier = 1, "**", "**"),
            base("o6", weight = 4, tier = 2, "***", "***"),
            base("o9", weight = 1, tier = 3, "***", "***", "***"),

            base("l3", weight = 7, tier = 1, "*", "**"),
            base("l4", weight = 6, tier = 2, "*", "*", "**"),
            base("j4", weight = 6, tier = 2, "..*", "..*", "***"),
            base("t4", weight = 5, tier = 2, "***", ".*."),
            base("s4", weight = 3, tier = 2, ".**", "**."),
            base("z4", weight = 3, tier = 2, "**.", ".**"),

            base("l5", weight = 2, tier = 3, "*", "*", "*", "**"),
            base("p5", weight = 3, tier = 2, "**", "**", "*."),
            base("t5", weight = 2, tier = 3, "***", ".*.", ".*."),
            base("u5", weight = 1, tier = 3, "*.*", "***"),
            base("plus5", weight = 1, tier = 3, ".*.", "***", ".*."),

            base("corner2x2", weight = 3, tier = 1, "**", "*."),
            base("l3wide", weight = 6, tier = 1, "**", ".*"),
            base("step4", weight = 2, tier = 2, "*..", "**.", ".**"),
        )

        val seen = mutableSetOf<String>()
        buildList {
            bases.forEach { shape ->
                for (turn in 0..3) {
                    val candidate = rotated(shape, turn)
                    val key = canonical(candidate.cells)
                    if (seen.add(key)) {
                        add(
                            candidate.copy(
                                id = if (turn == 0) shape.id else "${shape.id}r$turn",
                                weight = shape.weight,
                                tier = shape.tier,
                            ),
                        )
                    }
                }
            }
        }
    }

    val byId: Map<String, Shape> by lazy { all.associateBy { it.id } }
}
