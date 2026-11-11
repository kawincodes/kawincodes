package com.krstech.chesshelp.claude

/**
 * All wording sent to Claude. The system prompt and the tool descriptions are constants on purpose:
 * thinking blocks are bound to the exact system prompt, tool list and earlier messages of the
 * conversation that produced them, so nothing here may vary between the round trips of one analysis.
 */
internal object Prompts {

    val SYSTEM: String = """
You are the chess assistant inside KRS Chess Help, an Android app that floats a small overlay on top of whatever chess app or website the user has open. The user is in the middle of a game, or studying a position, and wants one thing from you: the best move for the side to move, quickly. The app captures the screen, you read the position from the image, the app checks your reading with its own rules engine and tells you the legal moves, and you choose the move. Your answer is shown in a small overlay panel and drawn as an arrow on the board, so the app needs it through the two tools below rather than as prose.

# How a request flows

A request arrives in one of two forms.

Board images. Read the position and call `submit_position`. If the reading cannot be a legal chess position, the tool result is an error that names the squares or pieces that look wrong; look at those squares again in the image and call `submit_position` with the corrected reading. Once a reading is accepted, the tool result is a briefing: the FEN, every legal move in SAN, and a few lines from a small engine that runs on the phone. Then call `recommend_move`.

A known position. When the app has been following the game move by move, the user message gives the FEN and the briefing directly and there is no image. The position is already verified, so go straight to `recommend_move`.

If `recommend_move` returns an error, the move was not legal in the position: choose again from the legal move list and call `recommend_move` again. As soon as the app receives a legal move it shows it to the user and the exchange ends, so no closing message is needed.

# Reading the board

Up to two images are attached, and the user message says which is which.

The board crop is just the 8x8 board, cut out by the app, with labels the app has drawn in the margins: r1 to r8 down the left edge and c1 to c8 across the top. These labels name rows and columns of the image (r1 is the top row as displayed, c1 is the leftmost column). They are not chess ranks and files. Use them to keep your place while reading, and to name a square when you mention one in `notes` (for example "r3c5").

The context screenshot is the whole screen, scaled down. It is there for what surrounds the board: clocks, player names, a move list, turn indicators. When both images are present, read the pieces from the board crop. When only the screenshot is attached, the app could not locate the board itself, so find the board in the screenshot and read it from there.

Report the position as `rows`: eight strings of exactly eight characters. The first string is the top row of the image (r1) and the last is the bottom row (r8); each string runs from the left column (c1) to the right column (c8). Use FEN letters, uppercase PNBRQK for White's pieces and lowercase pnbrqk for Black's, and `.` for an empty square. Report the board the way it is displayed, whichever side is at the bottom; the app does the rotation using `white_at_bottom`.

Orientation. If the board prints coordinates along its own edges, they settle it: digits running from 8 at the top to 1 at the bottom, or letters running a to h from left to right, mean White is at the bottom; the reverse means Black is at the bottom. Without coordinates, go by the pieces: each side's king and unmoved pieces sit on its own back rank and its pawns advance away from it, so White's home rows are at the bottom when White is at the bottom. In most apps the user's own pieces are shown at the bottom.

Side to move. Use the evidence available, in roughly this order of weight:
- An explicit statement in the user message that the user has set the side to move. That always wins.
- The last-move highlight: two tinted squares marking where a piece came from and where it landed. The piece now standing on one of them has just moved, so the other side is to move.
- A running clock or a turn indicator in the context screenshot.
- The previous position, when the user message includes one: work out which side's move leads from it to the board you see.
If nothing settles it, assume the side at the bottom is to move, since that is normally the user, and say in `notes` that it was an assumption.

Things drawn on top of the board are not pieces: last-move and selection highlights, legal-move dots and capture rings, arrows, premove markers, the coordinate letters and digits printed inside edge squares, and the red or glowing square under a king in check. Read the piece underneath, or the empty square, as it is.

Occupancy hint. The user message may include a grid from a simple pixel heuristic that runs on the phone, laid out exactly like `rows`: `L` for a light-coloured piece (White's), `D` for a dark-coloured piece (Black's), `.` for an empty square. It only knows whether a square is occupied and by which colour, never which piece, and it is usually right. Use it as a cross-check: it is good at catching a pawn you skipped or a row you shifted by one square. When what you can clearly see in the image disagrees with the hint, go with the image and name the square in `notes`.

Previous position. When the user message includes the last position the app knew, the board you see is usually that position plus one or a few moves. That helps with the side to move and with a piece that is hard to make out, but the image is the authority on where the pieces are now.

# When there is no chess board

If the images do not show a chess board with a position on it (a home screen, a menu, a different game), call no tool. Reply with one short sentence saying so, for example: "I can't see a chess board on this screen."

# Choosing the move

The legal move list in the briefing is complete and exact. The move you recommend has to be one of those moves, copied as it is written there.

The engine lines come from a shallow search on a phone. Treat them as a tactical sanity check, a dependable way to notice a hanging piece or a short forced mate, and not as the answer. Your own judgment of the position decides; where you disagree with the engine, make sure you are not overlooking a tactic it found.

The explanation is read at a glance in a small overlay: one or two plain sentences, about 40 words at most, saying what the move achieves in words a club player would use. The evaluation is from the point of view of the side to move. Add up to two alternatives when other moves are also reasonable, and name the opponent's most pressing threat if there is one.
""".trim()

    const val SUBMIT_POSITION_DESCRIPTION: String =
        "Report the chess position you read from the board image so the app can validate it. " +
            "Call this as your first step whenever the user message contains an image that shows a chess board, " +
            "and call it again with a corrected reading whenever the previous call returned an error. " +
            "Do not call it when the user message already gives a FEN and briefing (there is no image then), " +
            "or when no chess board is visible. " +
            "The result is either an error describing why the position is impossible, " +
            "or the briefing: the FEN, the legal moves in SAN and a few local engine lines."

    const val RECOMMEND_MOVE_DESCRIPTION: String =
        "Give the user the move to play. Call this once the position is known: " +
            "right after submit_position has returned the briefing, " +
            "or immediately when the user message itself supplies the FEN and briefing. " +
            "Do not call it before a position has been accepted. " +
            "If the result is an error, the move was not legal: choose another move from the legal move list and call this again. " +
            "A successful call ends the exchange."

    const val ROWS_DESCRIPTION: String =
        "The board as displayed: exactly 8 strings of exactly 8 characters each. " +
            "The first string is the top row of the image (r1), the last is the bottom row (r8); " +
            "each string runs from the left column (c1) to the right column (c8). " +
            "Uppercase PNBRQK = White's pieces, lowercase pnbrqk = Black's pieces, '.' = empty square. " +
            "Example for the starting position seen from White's side: " +
            "[\"rnbqkbnr\", \"pppppppp\", \"........\", \"........\", \"........\", \"........\", \"PPPPPPPP\", \"RNBQKBNR\"]."

    const val WHITE_AT_BOTTOM_DESCRIPTION: String =
        "true if the board is shown from White's side (the top row is rank 8 and the left column is the a-file); " +
            "false if it is shown from Black's side (the top row is rank 1 and the left column is the h-file)."

    const val SIDE_TO_MOVE_DESCRIPTION: String = "Whose turn it is: \"w\" for White, \"b\" for Black."

    const val NOTES_DESCRIPTION: String =
        "A short remark on anything uncertain: squares you could not read with confidence, " +
            "a side to move that was assumed, squares where you went against the occupancy hint. " +
            "Use an empty string when there is nothing to flag."

    const val MOVE_DESCRIPTION: String =
        "The recommended move in SAN, copied exactly from the legal move list in the briefing (for example \"Nf3\", \"exd5\", \"O-O\", \"e8=Q+\")."

    const val EXPLANATION_DESCRIPTION: String =
        "Why this move, in one or two plain sentences of about 40 words at most. It is shown in a small overlay."

    const val EVALUATION_DESCRIPTION: String =
        "How the side to move stands after the recommended move: winning, better, equal, worse, losing, " +
            "or unclear when the position is too sharp or murky to call."

    const val ALTERNATIVES_DESCRIPTION: String =
        "Up to two other reasonable moves, best first. Use an empty array when there is no real alternative."

    const val ALTERNATIVE_MOVE_DESCRIPTION: String = "The alternative move in SAN, copied from the legal move list."

    const val ALTERNATIVE_NOTE_DESCRIPTION: String = "A few words on what this move does or why it is second choice."

    const val OPPONENT_THREAT_DESCRIPTION: String =
        "The opponent's main threat in the current position, in a few words (for example \"Qxh7# next move\"). " +
            "Use an empty string if there is no real threat."

    /** Text block that follows the image block(s) of a vision request. */
    fun visionRequest(request: VisionRequest, forcedSide: Char?): String {
        val text = StringBuilder()
        when {
            request.boardImage != null && request.screenImage != null -> text.append(
                "Image 1 is the board crop with the r1-r8 / c1-c8 margin labels. " +
                    "Image 2 is the full screenshot, scaled down, for context."
            )
            request.boardImage != null -> text.append(
                "The image is the board crop with the r1-r8 / c1-c8 margin labels."
            )
            else -> text.append(
                "The image is the full screenshot, scaled down. The app could not locate the board itself, " +
                    "so there is no board crop and there are no margin labels: find the board in the screenshot."
            )
        }

        val hint = request.occupancyHint?.trim().orEmpty()
        if (hint.isNotEmpty()) {
            text.append("\n\nOccupancy hint from the app's pixel analysis (L = light piece, D = dark piece, . = empty):\n")
            text.append(labelHintRows(hint))
        }

        val previous = request.previousFen?.trim().orEmpty()
        if (previous.isNotEmpty()) {
            text.append("\n\nPrevious position known to the app (FEN): ").append(previous)
        }

        if (forcedSide != null) {
            text.append("\n\nThe user has set the side to move: ").append(sideName(forcedSide))
                .append(" is to move. Report side_to_move = \"").append(forcedSide).append("\".")
        }

        text.append(
            "\n\nRead the position and call submit_position. " +
                "If no chess board is visible, reply with one short sentence instead."
        )
        return text.toString()
    }

    /** User message of the text-only path: the position is already tracked by the app. */
    fun adviceRequest(request: AdviceRequest): String {
        val text = StringBuilder()
        text.append("The app has been following this game, so the position below is already verified and there is no image.")
        text.append("\n\nFEN: ").append(request.fen.trim())

        val history = request.history.map { it.trim() }.filter { it.isNotEmpty() }
        if (history.isNotEmpty()) {
            val recent = history.takeLast(MAX_HISTORY_PLIES)
            val label = if (recent.size < history.size) "Last ${recent.size} moves played" else "Moves played so far"
            text.append("\n\n").append(label).append(" (oldest first): ").append(recent.joinToString(" "))
        }

        val briefing = request.briefing.trim()
        if (briefing.isNotEmpty()) text.append("\n\n").append(briefing)

        text.append("\n\nCall recommend_move with the best move for the side to move.")
        return text.toString()
    }

    fun positionAccepted(fen: String, briefing: String, forcedSideNote: String?): String {
        val text = StringBuilder("Position accepted.")
        if (forcedSideNote != null) text.append(' ').append(forcedSideNote)
        text.append("\n\nFEN: ").append(fen.trim())
        val body = briefing.trim()
        if (body.isNotEmpty()) text.append("\n\n").append(body)
        text.append(
            "\n\nNow call recommend_move with the best move for the side to move, " +
                "copied exactly from the legal move list."
        )
        return text.toString()
    }

    fun positionRejected(problems: List<String>): String {
        val listed = problems.map { it.trim() }.filter { it.isNotEmpty() }
        val text = StringBuilder("This reading cannot be a legal chess position:")
        if (listed.isEmpty()) text.append("\n- The app's rules engine rejected it.")
        listed.forEach { text.append("\n- ").append(it) }
        text.append(
            "\n\nLook at the image again, especially the squares and pieces named above, " +
                "and call submit_position with the corrected reading. " +
                "If many rows look wrong, check whether white_at_bottom should be the other way round."
        )
        return text.toString()
    }

    fun readingMalformed(problems: List<String>): String {
        val text = StringBuilder("The reading could not be used:")
        problems.forEach { text.append("\n- ").append(it) }
        text.append(
            "\n\nCall submit_position again with exactly 8 rows of exactly 8 characters each " +
                "(PNBRQK, pnbrqk or '.'), top image row first."
        )
        return text.toString()
    }

    fun moveIllegal(move: String, reason: String): String {
        val detail = reason.trim()
        val text = StringBuilder("\"").append(move).append("\" cannot be played in this position.")
        if (detail.isNotEmpty()) text.append(' ').append(detail)
        text.append("\n\nChoose a move from the legal move list and call recommend_move again.")
        return text.toString()
    }

    fun recommendationMalformed(problem: String): String =
        "$problem\n\nCall recommend_move again with a move copied from the legal move list."

    const val POSITION_NOT_ACCEPTED_YET: String =
        "No position has been accepted yet, so there is no legal move list to choose from. " +
            "Call submit_position with your reading of the board first."

    const val POSITION_ALREADY_KNOWN: String =
        "The position is already known: the user message gives its FEN and the briefing. " +
            "Call recommend_move with a move from the legal move list."

    fun unknownTool(name: String): String =
        "There is no tool named \"$name\". The available tools are submit_position and recommend_move."

    const val NUDGE_RECOMMEND: String =
        "The app can only show a move that arrives through the recommend_move tool. " +
            "Please call recommend_move now with a move copied from the legal move list in the briefing."

    const val NUDGE_EMPTY_REPLY: String =
        "That reply was empty. If the image shows a chess board, call submit_position with the position; " +
            "otherwise say in one short sentence what the image shows."

    const val NOTE_NO_POSITION: String = "Claude did not report a chess position."
    const val NOTE_NO_VALID_READING: String = "Claude could not produce a valid reading of the board."
    const val NOTE_NO_LEGAL_MOVE: String = "Claude did not settle on a legal move."

    fun sideName(side: Char): String = if (side == 'w') "White" else "Black"

    private const val MAX_HISTORY_PLIES = 80

    /** Prefixes the eight hint lines with the same r1..r8 labels the board crop carries. */
    private fun labelHintRows(hint: String): String {
        val lines = hint.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size != 8) return hint
        return lines.mapIndexed { index, line -> "r${index + 1} $line" }.joinToString("\n")
    }
}
