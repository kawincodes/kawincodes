package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class AttacksTest {
    @Test
    fun magicLookupsMatchRayWalkingForRandomOccupancies() {
        val random = Random(7)
        repeat(20_000) {
            // Mix dense and sparse boards.
            val occ = when (it % 3) {
                0 -> random.nextLong()
                1 -> random.nextLong() and random.nextLong()
                else -> random.nextLong() and random.nextLong() and random.nextLong()
            }
            for (sq in 0 until 64) {
                assertEquals(Attacks.slowRook(sq, occ), Attacks.rook(sq, occ), "rook on ${Square.name(sq)}")
                assertEquals(Attacks.slowBishop(sq, occ), Attacks.bishop(sq, occ), "bishop on ${Square.name(sq)}")
            }
        }
    }

    @Test
    fun magicLookupsMatchRayWalkingForEveryRelevantBlockerSet() {
        // Every subset of the squares a slider can see from each square (edges included, which is a
        // superset of the relevant-occupancy masks used by the tables) for the bishop; for the rook
        // every subset of its rank and file neighbours.
        for (sq in 0 until 64) {
            val rookRays = Attacks.slowRook(sq, 0L)
            var subset = 0L
            do {
                assertEquals(Attacks.slowRook(sq, subset), Attacks.rook(sq, subset), "rook on ${Square.name(sq)}")
                subset = (subset - rookRays) and rookRays
            } while (subset != 0L)

            val bishopRays = Attacks.slowBishop(sq, 0L)
            subset = 0L
            do {
                assertEquals(Attacks.slowBishop(sq, subset), Attacks.bishop(sq, subset), "bishop on ${Square.name(sq)}")
                subset = (subset - bishopRays) and bishopRays
            } while (subset != 0L)
        }
    }

    @Test
    fun leaperTables() {
        assertEquals(squares("b3", "c2"), Attacks.KNIGHT[sq("a1")])
        assertEquals(squares("c6", "e6", "f5", "f3", "e2", "c2", "b3", "b5"), Attacks.KNIGHT[sq("d4")])
        assertEquals(squares("g8", "g7", "h7"), Attacks.KING[sq("h8")])
        assertEquals(8, java.lang.Long.bitCount(Attacks.KING[sq("e4")]))
        // White pawns attack up the board, black pawns down; nothing wraps around the edge.
        assertEquals(squares("d5", "f5"), Attacks.PAWN[sq("e4")])
        assertEquals(squares("d3", "f3"), Attacks.PAWN[64 + sq("e4")])
        assertEquals(squares("b3"), Attacks.PAWN[sq("a2")])
        assertEquals(squares("g6"), Attacks.PAWN[64 + sq("h7")])
        assertEquals(0L, Attacks.PAWN[sq("e8")])
        assertEquals(0L, Attacks.PAWN[64 + sq("e1")])
    }

    @Test
    fun betweenAndLine() {
        assertEquals(squares("b1", "c1", "d1"), Attacks.BETWEEN[sq("a1") * 64 + sq("e1")])
        assertEquals(squares("b1", "c1", "d1"), Attacks.BETWEEN[sq("e1") * 64 + sq("a1")])
        assertEquals(squares("b2", "c3", "d4", "e5", "f6", "g7"), Attacks.BETWEEN[sq("a1") * 64 + sq("h8")])
        assertEquals(0L, Attacks.BETWEEN[sq("a1") * 64 + sq("b2")])
        assertEquals(0L, Attacks.BETWEEN[sq("a1") * 64 + sq("b3")])
        assertEquals(0L, Attacks.LINE[sq("a1") * 64 + sq("b3")])
        assertEquals(Attacks.FILE_A shl 4, Attacks.LINE[sq("e2") * 64 + sq("e7")])
        assertEquals(squares("a1", "b2", "c3", "d4", "e5", "f6", "g7", "h8"), Attacks.LINE[sq("c3") * 64 + sq("f6")])
        assertEquals(squares("h1", "g2", "f3", "e4", "d5", "c6", "b7", "a8"), Attacks.LINE[sq("g2") * 64 + sq("f3")])
        for (a in 0 until 64) {
            for (b in 0 until 64) {
                assertEquals(Attacks.BETWEEN[a * 64 + b], Attacks.BETWEEN[b * 64 + a])
                assertEquals(Attacks.LINE[a * 64 + b], Attacks.LINE[b * 64 + a])
            }
        }
    }

    private fun sq(name: String): Int = Square.parse(name)

    private fun squares(vararg names: String): Long = names.fold(0L) { acc, name -> acc or (1L shl sq(name)) }
}
