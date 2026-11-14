package com.botillon.blocksmith.ui

import com.botillon.blocksmith.game.Board
import com.botillon.blocksmith.game.Coord
import com.botillon.blocksmith.game.Palette
import com.botillon.blocksmith.game.Piece
import com.botillon.blocksmith.game.Shapes
import com.botillon.blocksmith.game.SIZE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Le dépôt doit être tolérant : c'est le geste le plus fréquent du jeu. */
class PlacementTest {

    private val dot = Piece(Shapes.byId.getValue("dot"), Palette.AMBER)
    private val square = Piece(Shapes.byId.getValue("o4"), Palette.MINT)

    @Test
    fun exactPositionIsKept() {
        assertEquals(Coord(3, 4), snapPlacement(Board.Empty, dot, 3, 4))
    }

    @Test
    fun occupiedCellSnapsToNeighbour() {
        val board = Board.Empty.set(3, 4, Palette.CORAL)
        val snapped = snapPlacement(board, dot, 3, 4)
        // La case libre la plus proche est adjacente.
        assertEquals(1, kotlin.math.abs(snapped!!.x - 3) + kotlin.math.abs(snapped.y - 4))
    }

    @Test
    fun outOfBoardSnapsInside() {
        assertEquals(Coord(0, 0), snapPlacement(Board.Empty, dot, -1, -1))
        assertEquals(Coord(SIZE - 1, SIZE - 1), snapPlacement(Board.Empty, dot, SIZE, SIZE))
    }

    @Test
    fun noRoomReturnsNull() {
        var board = Board.Empty
        for (y in 0 until SIZE) for (x in 0 until SIZE) board = board.set(x, y, Palette.CORAL)
        assertNull(snapPlacement(board, square, 3, 3))
    }
}
