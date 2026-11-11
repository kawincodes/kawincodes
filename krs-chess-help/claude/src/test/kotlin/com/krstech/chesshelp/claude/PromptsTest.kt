package com.krstech.chesshelp.claude

import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromptsTest {

    @Test
    fun `system prompt covers the conventions the app relies on`() {
        val prompt = Prompts.SYSTEM
        for (needle in listOf(
            "submit_position", "recommend_move", "r1 to r8", "c1 to c8", "not chess ranks and files",
            "white_at_bottom", "last-move highlight", "Occupancy hint", "`L`", "`D`", "PNBRQK", "pnbrqk",
            "call no tool", "legal move list", "sanity check", "40 words",
        )) {
            assertContains(prompt, needle)
        }
        assertEquals(prompt.trim(), prompt)
        assertFalse(prompt.contains("\r"))
    }

    @Test
    fun `nothing in the wording asks for reasoning to be written out`() {
        // Asking a model to reproduce its reasoning in the reply invites "reasoning_extraction" refusals.
        val everything = listOf(
            Prompts.SYSTEM, Prompts.SUBMIT_POSITION_DESCRIPTION, Prompts.RECOMMEND_MOVE_DESCRIPTION,
            Prompts.NOTES_DESCRIPTION, Prompts.EXPLANATION_DESCRIPTION,
        ).joinToString("\n").lowercase()
        for (phrase in listOf("step by step", "your reasoning", "chain of thought", "think aloud", "thought process")) {
            assertFalse(everything.contains(phrase), phrase)
        }
    }

    @Test
    fun `vision request text is deterministic and labels the hint rows`() {
        val request = visionRequest(previousFen = START_FEN, forcedSideToMove = 'w')
        val text = Prompts.visionRequest(request, 'w')

        assertEquals(text, Prompts.visionRequest(request, 'w'))
        assertContains(text, "r1 DDDDDDDD\nr2 DDDDDDDD\nr3 ........")
        assertContains(text, "White is to move")
        assertTrue(text.indexOf("Occupancy hint") < text.indexOf("Previous position"))
        assertTrue(text.trimEnd().endsWith("reply with one short sentence instead."))
    }

    @Test
    fun `a hint that is not eight lines is passed on untouched`() {
        val text = Prompts.visionRequest(visionRequest(occupancyHint = "LD..\n...."), null)

        assertContains(text, "LD..\n....")
        assertFalse(text.contains("r1 LD.."))
    }

    @Test
    fun `advice request keeps only the recent part of a long game`() {
        val history = (1..200).map { "m$it" }
        val text = Prompts.adviceRequest(AdviceRequest(START_FEN, START_BRIEFING, history))

        assertContains(text, "Last 80 moves played (oldest first): m121 ")
        assertFalse(text.contains("m120 "))
        assertTrue(text.contains("m200"))

        val short = Prompts.adviceRequest(AdviceRequest(START_FEN, "", emptyList()))
        assertFalse(short.contains("moves played", ignoreCase = true))
        assertContains(short, "FEN: $START_FEN")
    }

    @Test
    fun `tool results say what to do next`() {
        assertContains(Prompts.positionAccepted(START_FEN, START_BRIEFING, null), "Now call recommend_move")
        val rejected = Prompts.positionRejected(listOf("Black has 2 kings (e8, g8)", " "))
        assertContains(rejected, "- Black has 2 kings (e8, g8)")
        assertContains(rejected, "call submit_position")
        assertContains(Prompts.positionRejected(emptyList()), "rejected it")
        assertContains(Prompts.moveIllegal("Qh5", "Legal moves: e4 d4"), "\"Qh5\" cannot be played in this position. Legal moves: e4 d4")
    }
}
