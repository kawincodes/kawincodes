package com.krstech.chesshelp.chess

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The search behind [Engine]: iterative deepening, principal-variation alpha-beta with a
 * transposition table, quiescence search, and MultiPV at the root. One instance holds all the
 * memory it needs (about 8.5 MB) and can be reused for any number of [analyze] calls; it is not
 * thread-safe.
 *
 * Scores are centipawns for the side to move. A mate is `MATE - plies from the root`, so the
 * shortest mate always has the best score. Such a score is only ever produced by a line in which
 * every reply of the losing side was searched, so a reported mate is a proven one: the selective
 * parts (null move, late-move reductions, the stand-pat of the quiescence search) can make the
 * search miss or delay a mate but can not invent one.
 */
internal class Searcher {
    /** The board being searched. Exposed, like [quiesce], for the reference search in the tests. */
    var board = Board()
        private set

    /**
     * `false` switches off everything that can change a score (null move, late-move reductions and
     * transposition cutoffs); what is left must return exactly the minimax value, which the tests
     * compare with a plain reference search.
     */
    var selective = true

    var nodes = 0L
        private set

    private val table = TranspositionTable()
    private val moveStack = Array(MAX_PLY) { IntArray(Board.MAX_MOVES) }
    private val scoreStack = Array(MAX_PLY) { IntArray(Board.MAX_MOVES) }
    private val pv = Array(MAX_PLY) { IntArray(MAX_PLY) }
    private val pvLength = IntArray(MAX_PLY)
    private val killers = IntArray(2 * MAX_PLY)
    private val history = IntArray(2 * 64 * 64)
    private val exchange = IntArray(40)

    private var rootMoves = IntArray(0)
    private var rootCount = 0
    private var rootInCheck = false
    private var rootBest = -1

    private var startNanos = 0L
    private var budgetNanos = 0L
    private var shouldStop: () -> Boolean = NEVER
    private var canAbort = false
    private var aborted = false

    private class Lines(count: Int) {
        val moves = Array(count) { IntArray(MAX_PLY) }
        val length = IntArray(count)
        val score = IntArray(count)
    }

    fun analyze(position: Position, limits: SearchLimits, stop: () -> Boolean): EngineResult {
        startNanos = System.nanoTime()
        prepare(position)
        if (rootCount == 0) return EngineResult(emptyList(), 0, 0L, elapsedMs())

        val lineCount = min(max(limits.multiPv, 1), rootCount)
        val maxDepth = min(max(limits.maxDepth, 1), MAX_DEPTH)
        budgetNanos = min(max(limits.timeMs, 0L), MAX_TIME_MS) * 1_000_000L
        shouldStop = stop

        var done = Lines(lineCount)
        var work = Lines(lineCount)
        var completed = 0
        try {
            var lastIteration = 0L
            for (depth in 1..maxDepth) {
                val iterationStart = System.nanoTime()
                // A half-finished iteration is thrown away, so do not start one that is unlikely
                // to finish. The first iteration always runs: it is what guarantees a result.
                if (depth > 1 && (iterationStart - startNanos + 2 * lastIteration > budgetNanos || stop())) break
                canAbort = depth > 1
                for (k in 0 until lineCount) {
                    val score = searchLine(depth, k, if (completed > 0) done.score[k] else 0)
                    if (aborted) break
                    work.score[k] = score
                    work.length[k] = pvLength[0]
                    System.arraycopy(pv[0], 0, work.moves[k], 0, pvLength[0])
                }
                if (aborted) break
                val swap = done
                done = work
                work = swap
                completed = depth
                lastIteration = System.nanoTime() - iterationStart
            }
        } finally {
            shouldStop = NEVER
        }

        val lines = if (completed == 0) staticLines(position, lineCount) else searchedLines(position, done, lineCount, completed)
        return EngineResult(lines, completed, nodes, elapsedMs())
    }

    /** Loads [position] and resets everything a previous search left behind. */
    fun prepare(position: Position) {
        board = position.toBoard()
        var moves = IntArray(Board.MAX_MOVES)
        var count = board.generateMoves(moves)
        if (count == moves.size) {
            // Only placements that can not occur in a game (dozens of queens) have this many moves.
            moves = IntArray(ROOT_CAPACITY)
            count = board.generateMoves(moves)
        }
        rootMoves = moves
        rootCount = count
        rootInCheck = board.inCheck()
        rootBest = -1
        nodes = 0L
        aborted = false
        canAbort = false
        budgetNanos = Long.MAX_VALUE
        shouldStop = NEVER
        // Starting from a clean table makes a result depend on the position and the limits only,
        // never on what this instance analysed before.
        table.clear()
        history.fill(0)
        killers.fill(0)
        pvLength.fill(0)

        if (count > 1) {
            val scores = IntArray(count)
            scoreMoves(moves, scores, count, Moves.NONE, 0)
            for (i in 0 until count) pickNext(moves, scores, i, count)
        }
    }

    private fun elapsedMs(): Long = (System.nanoTime() - startNanos) / 1_000_000L

    // ------------------------------------------------------------------ root

    /**
     * Finds the best root move among `rootMoves[index until rootCount]` (the moves before [index]
     * are this iteration's better lines), moves it to [index] and leaves its line in `pv[0]`.
     * Searches a narrow window around [previousScore] first and widens it until the score fits.
     */
    private fun searchLine(depth: Int, index: Int, previousScore: Int): Int {
        var alpha = -INFINITE
        var beta = INFINITE
        var delta = ASPIRATION_WINDOW
        if (depth >= ASPIRATION_MIN_DEPTH && abs(previousScore) < MATE_BOUND) {
            alpha = previousScore - delta
            beta = previousScore + delta
        }
        while (true) {
            val score = searchRoot(depth, alpha, beta, index)
            if (aborted) return 0
            if (rootBest > index) {
                val move = rootMoves[rootBest]
                System.arraycopy(rootMoves, index, rootMoves, index + 1, rootBest - index)
                rootMoves[index] = move
            }
            if (score > alpha && score < beta) return score
            delta *= 2
            val open = delta > ASPIRATION_LIMIT || abs(score) >= MATE_BOUND
            if (score <= alpha) {
                alpha = if (open) -INFINITE else max(-INFINITE, score - delta)
            } else {
                beta = if (open) INFINITE else min(INFINITE, score + delta)
            }
        }
    }

    private fun searchRoot(depth: Int, alphaIn: Int, beta: Int, first: Int): Int {
        var alpha = alphaIn
        var best = -INFINITE
        pvLength[0] = 0
        rootBest = -1
        for (i in first until rootCount) {
            val m = rootMoves[i]
            board.make(m)
            val givesCheck = board.inCheck()
            val newDepth = if (givesCheck) depth else depth - 1
            var score: Int
            if (i == first) {
                score = -search(newDepth, -beta, -alpha, 1, givesCheck, true)
            } else {
                var reduction = 0
                if (selective && depth >= 3 && i - first >= LMR_FROM && isQuiet(m) && !rootInCheck && !givesCheck) {
                    reduction = reduction(depth, i - first, true)
                }
                score = -search(newDepth - reduction, -alpha - 1, -alpha, 1, givesCheck, true)
                if (reduction > 0 && score > alpha && !aborted) {
                    score = -search(newDepth, -alpha - 1, -alpha, 1, givesCheck, true)
                }
                if (score > alpha && score < beta && !aborted) {
                    score = -search(newDepth, -beta, -alpha, 1, givesCheck, true)
                }
            }
            board.unmake()
            if (aborted) return 0
            if (score > best) {
                best = score
                if (score > alpha) {
                    rootBest = i
                    alpha = score
                    extendPv(0, m)
                    if (score >= beta) break
                }
            }
        }
        return best
    }

    // ------------------------------------------------------------------ main search

    private fun search(depth: Int, alphaIn: Int, betaIn: Int, ply: Int, inCheck: Boolean, allowNull: Boolean): Int {
        if (depth <= 0) return quiesce(alphaIn, betaIn, ply, inCheck)
        pvLength[ply] = 0
        if (++nodes and POLL_MASK == 0L) poll()
        if (aborted) return 0

        val board = board
        if (board.isRepetition() || board.isInsufficientMaterial()) return DRAW
        if (board.halfmoveClock >= 100) {
            // A mate delivered by the hundredth half-move still counts.
            return if (inCheck && !board.hasLegalMoves()) -MATE + ply else DRAW
        }
        if (ply >= MAX_PLY - 1) return Evaluation.evaluate(board)

        // Mate-distance pruning: nothing here can beat a mate already found closer to the root.
        var alpha = max(alphaIn, -MATE + ply)
        val beta = min(betaIn, MATE - ply - 1)
        if (alpha >= beta) return alpha
        val pvNode = betaIn - alphaIn > 1

        val key = board.key
        var hashMove = Moves.NONE
        val entry = table.probe(key)
        if (entry != 0L) {
            hashMove = TranspositionTable.move(entry)
            if (selective && !pvNode && TranspositionTable.depth(entry) >= depth) {
                val score = fromTable(TranspositionTable.score(entry), ply)
                val bound = TranspositionTable.bound(entry)
                if (bound == TranspositionTable.EXACT ||
                    (bound == TranspositionTable.LOWER && score >= beta) ||
                    (bound == TranspositionTable.UPPER && score <= alpha)
                ) {
                    return score
                }
            }
        }

        // Null move: if passing still beats beta, a real move will too. Unsound in zugzwang, so it
        // is skipped in check and without enough pieces, never proves a mate (mate bounds are
        // excluded and the result is capped at beta) and is never played twice in a row.
        if (selective && allowNull && !pvNode && !inCheck && depth >= 3 &&
            beta < MATE_BOUND && beta > -MATE_BOUND && hasNullMoveMaterial(board.side) &&
            Evaluation.evaluate(board) >= beta
        ) {
            board.makeNull()
            val score = -search(depth - 3 - depth / 4, -beta, -beta + 1, ply + 1, board.inCheck(), false)
            board.unmake()
            if (aborted) return 0
            if (score >= beta) return beta
        }

        val moves = moveStack[ply]
        val scores = scoreStack[ply]
        val count = board.generateMoves(moves)
        if (count == 0) return if (inCheck) -MATE + ply else DRAW
        scoreMoves(moves, scores, count, hashMove, ply)

        val alphaStart = alpha
        var best = -INFINITE
        var bestMove = Moves.NONE
        for (i in 0 until count) {
            pickNext(moves, scores, i, count)
            val m = moves[i]
            board.make(m)
            val givesCheck = board.inCheck()
            val newDepth = if (givesCheck) depth else depth - 1
            var score: Int
            if (i == 0) {
                score = -search(newDepth, -beta, -alpha, ply + 1, givesCheck, true)
            } else {
                var reduction = 0
                if (selective && depth >= 3 && i >= LMR_FROM && scores[i] < KILLER_FLOOR && isQuiet(m) &&
                    !inCheck && !givesCheck
                ) {
                    reduction = reduction(depth, i, pvNode)
                }
                score = -search(newDepth - reduction, -alpha - 1, -alpha, ply + 1, givesCheck, true)
                if (reduction > 0 && score > alpha && !aborted) {
                    score = -search(newDepth, -alpha - 1, -alpha, ply + 1, givesCheck, true)
                }
                if (pvNode && score > alpha && score < beta && !aborted) {
                    score = -search(newDepth, -beta, -alpha, ply + 1, givesCheck, true)
                }
            }
            board.unmake()
            if (aborted) return 0
            if (score > best) {
                best = score
                if (score > alpha) {
                    bestMove = m
                    alpha = score
                    if (pvNode) extendPv(ply, m)
                    if (score >= beta) {
                        if (isQuiet(m)) rewardQuietCutoff(moves, i, depth, ply)
                        break
                    }
                }
            }
        }

        val bound = when {
            best >= beta -> TranspositionTable.LOWER
            best > alphaStart -> TranspositionTable.EXACT
            else -> TranspositionTable.UPPER
        }
        table.store(key, bestMove, toTable(best, ply), depth, bound)
        return best
    }

    /**
     * Resolves captures and promotions so that the static evaluation is only trusted in quiet
     * positions. In check there is no standing pat and every evasion is searched.
     */
    fun quiesce(alphaIn: Int, beta: Int, ply: Int, inCheck: Boolean): Int {
        pvLength[ply] = 0
        if (++nodes and POLL_MASK == 0L) poll()
        if (aborted) return 0

        val board = board
        if (board.isInsufficientMaterial()) return DRAW
        if (ply >= MAX_PLY - 1) return Evaluation.evaluate(board)

        var alpha = alphaIn
        var best: Int
        val moves = moveStack[ply]
        val scores = scoreStack[ply]
        val count: Int
        if (inCheck) {
            if (board.isRepetition()) return DRAW
            count = board.generateMoves(moves)
            if (count == 0) return -MATE + ply
            best = -INFINITE
        } else {
            // Standing pat assumes there is a move to make; a lone king (with blocked pawns) may have none.
            if (hasOnlyKingAndPawns(board.side) && !board.hasLegalMoves()) return DRAW
            best = Evaluation.evaluate(board)
            if (best >= beta) return best
            if (best > alpha) alpha = best
            count = board.generateCaptures(moves)
        }
        scoreMoves(moves, scores, count, Moves.NONE, ply)

        for (i in 0 until count) {
            pickNext(moves, scores, i, count)
            // Not in check, what is left are captures that lose material and under-promotions.
            if (!inCheck && scores[i] < 0) break
            board.make(moves[i])
            val score = -quiesce(-beta, -alpha, ply + 1, board.inCheck())
            board.unmake()
            if (aborted) return 0
            if (score > best) {
                best = score
                if (score > alpha) {
                    alpha = score
                    if (score >= beta) break
                }
            }
        }
        return best
    }

    private fun poll() {
        if (!canAbort) {
            // The first iteration normally takes a few thousand nodes and is left alone so that
            // even a zero time budget gets a searched answer; it only becomes interruptible in
            // placements where capture sequences explode.
            if (nodes < UNINTERRUPTED_NODES) return
            canAbort = true
        }
        if (System.nanoTime() - startNanos >= budgetNanos || shouldStop()) aborted = true
    }

    private fun extendPv(ply: Int, move: Int) {
        val line = pv[ply]
        val childLength = pvLength[ply + 1]
        line[0] = move
        System.arraycopy(pv[ply + 1], 0, line, 1, childLength)
        pvLength[ply] = childLength + 1
    }

    private fun reduction(depth: Int, moveIndex: Int, pvNode: Boolean): Int {
        var r = 1
        if (depth >= 6 && moveIndex >= 8) r++
        if (depth >= 10 && moveIndex >= 16) r++
        return if (pvNode) r - 1 else r
    }

    private fun hasNullMoveMaterial(side: Int): Boolean {
        if (board.pieces(side, Board.ROOK) or board.pieces(side, Board.QUEEN) != 0L) return true
        val minors = board.pieces(side, Board.KNIGHT) or board.pieces(side, Board.BISHOP)
        return minors and (minors - 1) != 0L
    }

    private fun hasOnlyKingAndPawns(side: Int): Boolean {
        val pieces = board.pieces(Board.KNIGHT) or board.pieces(Board.BISHOP) or
            board.pieces(Board.ROOK) or board.pieces(Board.QUEEN)
        return pieces and board.occupancy(side) == 0L
    }

    // ------------------------------------------------------------------ move ordering

    private fun isQuiet(m: Int): Boolean = !Moves.isCapture(m) && !Moves.isPromotion(m)

    /**
     * Order: hash move, queen promotions and captures that do not lose material (most valuable
     * victim first, least valuable attacker first), killers, quiet moves by history, then losing
     * captures and under-promotions (the only negative scores besides bad history).
     */
    private fun scoreMoves(moves: IntArray, scores: IntArray, count: Int, hashMove: Int, ply: Int) {
        val killer1 = killers[2 * ply]
        val killer2 = killers[2 * ply + 1]
        val historyBase = board.side shl 12
        for (i in 0 until count) {
            val m = moves[i]
            scores[i] = when {
                m == hashMove -> HASH_SCORE
                !isQuiet(m) -> noisyScore(m)
                m == killer1 -> KILLER_FLOOR + 1
                m == killer2 -> KILLER_FLOOR
                else -> history[historyBase or (m and 4095)]
            }
        }
    }

    private fun noisyScore(m: Int): Int {
        val victim = Moves.captured(m)
        val victimValue = if (victim >= 0) EXCHANGE_VALUE[victim] else 0
        val promotion = Moves.promotion(m)
        if (promotion != 0) {
            return if (promotion == Board.QUEEN) GOOD_CAPTURE + 8 * (EXCHANGE_VALUE[Board.QUEEN] + victimValue) else UNDER_PROMOTION + promotion
        }
        val attacker = Moves.piece(m)
        val order = 8 * victimValue - attacker
        // A legal king capture is never recaptured, and taking a piece worth at least the
        // attacker can not lose material; only the other captures need the exchange evaluation.
        val sound = attacker == Board.KING || EXCHANGE_VALUE[attacker] <= victimValue || see(m) >= 0
        return if (sound) GOOD_CAPTURE + order else BAD_CAPTURE + order
    }

    /** Moves the best-scored of `moves[index until count]` to [index] (lazy selection sort). */
    private fun pickNext(moves: IntArray, scores: IntArray, index: Int, count: Int) {
        var best = index
        for (j in index + 1 until count) {
            if (scores[j] > scores[best]) best = j
        }
        if (best != index) {
            val m = moves[best]
            moves[best] = moves[index]
            moves[index] = m
            val s = scores[best]
            scores[best] = scores[index]
            scores[index] = s
        }
    }

    /** The quiet move `moves[index]` refuted the position: remember it, demote the quiet moves tried before it. */
    private fun rewardQuietCutoff(moves: IntArray, index: Int, depth: Int, ply: Int) {
        val m = moves[index]
        if (killers[2 * ply] != m) {
            killers[2 * ply + 1] = killers[2 * ply]
            killers[2 * ply] = m
        }
        val base = board.side shl 12
        val bonus = min(depth * depth, HISTORY_MAX_BONUS)
        adjustHistory(base or (m and 4095), bonus)
        for (j in 0 until index) {
            val earlier = moves[j]
            if (isQuiet(earlier)) adjustHistory(base or (earlier and 4095), -bonus)
        }
    }

    /** Moves the entry towards `±HISTORY_LIMIT` without ever passing it, so it stays below the killers. */
    private fun adjustHistory(index: Int, bonus: Int) {
        val old = history[index]
        history[index] = old + bonus - old * abs(bonus) / HISTORY_LIMIT
    }

    /**
     * Static exchange evaluation: the material balance (in [EXCHANGE_VALUE] units) for the side
     * to move after playing [m] and the best sequence of recaptures on the target square, each
     * side always taking with its least valuable piece and free to stop. Pins are ignored.
     */
    fun see(m: Int): Int {
        val board = board
        val from = Moves.from(m)
        val to = Moves.to(m)
        val gain = exchange
        val promotion = Moves.promotion(m)
        val captured = Moves.captured(m)

        gain[0] = if (captured >= 0) EXCHANGE_VALUE[captured] else 0
        // Value of the piece standing on the target square, i.e. of what the next capture wins.
        var onTarget = EXCHANGE_VALUE[Moves.piece(m)]
        if (promotion != 0) {
            gain[0] += EXCHANGE_VALUE[promotion] - EXCHANGE_VALUE[Board.PAWN]
            onTarget = EXCHANGE_VALUE[promotion]
        }

        var occupied = board.occupied xor (1L shl from)
        if (Moves.isEnPassant(m)) {
            occupied = occupied xor (1L shl (if (board.side == Board.WHITE) to - 8 else to + 8))
        }
        val diagonal = board.pieces(Board.BISHOP) or board.pieces(Board.QUEEN)
        val straight = board.pieces(Board.ROOK) or board.pieces(Board.QUEEN)
        var attackers = (
            (Attacks.PAWN[Board.BLACK * 64 + to] and board.pieces(Board.WHITE, Board.PAWN)) or
                (Attacks.PAWN[Board.WHITE * 64 + to] and board.pieces(Board.BLACK, Board.PAWN)) or
                (Attacks.KNIGHT[to] and board.pieces(Board.KNIGHT)) or
                (Attacks.KING[to] and board.pieces(Board.KING)) or
                (Attacks.bishop(to, occupied) and diagonal) or
                (Attacks.rook(to, occupied) and straight)
            ) and occupied

        var side = board.side xor 1
        var depth = 0
        while (depth < gain.size - 1) {
            val candidates = attackers and board.occupancy(side)
            if (candidates == 0L) break
            var type = Board.PAWN
            var pieces = candidates and board.pieces(type)
            while (pieces == 0L) {
                type++
                pieces = candidates and board.pieces(type)
            }
            // A king can not take a piece that is still defended.
            if (type == Board.KING && attackers and board.occupancy(side xor 1) != 0L) break

            depth++
            gain[depth] = onTarget - gain[depth - 1]
            onTarget = EXCHANGE_VALUE[type]
            occupied = occupied xor (pieces and -pieces)
            // The capturing piece may have been shielding a slider behind it.
            if (type == Board.PAWN || type == Board.BISHOP || type == Board.QUEEN) {
                attackers = attackers or (Attacks.bishop(to, occupied) and diagonal)
            }
            if (type == Board.ROOK || type == Board.QUEEN) {
                attackers = attackers or (Attacks.rook(to, occupied) and straight)
            }
            attackers = attackers and occupied
            side = side xor 1
        }
        while (depth > 0) {
            gain[depth - 1] = -max(-gain[depth - 1], gain[depth])
            depth--
        }
        return gain[0]
    }

    // ------------------------------------------------------------------ results

    private fun searchedLines(position: Position, lines: Lines, count: Int, depth: Int): List<EngineLine> {
        val result = ArrayList<EngineLine>(count)
        for (k in 0 until count) {
            val first = lines.moves[k][0]
            val sans = sanLine(position, lines.moves[k], lines.length[k])
            val score = lines.score[k]
            result.add(EngineLine(Moves.toMove(first), sans[0], score, mateIn(score), sans, depth))
        }
        // Each line was searched with the better ones excluded, so they are normally in order
        // already; the stable sort only matters when two searches disagree by a few centipawns.
        return result.sortedByDescending { it.scoreCp }
    }

    /**
     * SAN of a line from the root. Every move is checked against the generator before it is
     * played, so a line can be cut short but can never contain an illegal move.
     */
    private fun sanLine(position: Position, line: IntArray, length: Int): List<String> {
        val replay = position.toBoard()
        val sans = ArrayList<String>(length)
        var legal = IntArray(Board.MAX_MOVES)
        for (i in 0 until length) {
            var count = replay.generateMoves(legal)
            if (count == legal.size) {
                legal = IntArray(ROOT_CAPACITY)
                count = replay.generateMoves(legal)
            }
            var found = false
            for (j in 0 until count) {
                if (legal[j] == line[i]) {
                    found = true
                    break
                }
            }
            if (!found) break
            sans.add(San.format(replay, line[i], legal, count))
            replay.make(line[i])
        }
        if (sans.isEmpty()) sans.add(position.san(Moves.toMove(line[0])))
        return sans
    }

    /**
     * Fallback when not even the first iteration could finish (see [poll]): every root move judged
     * by the static evaluation of the position it leads to. Reported as depth 0.
     */
    private fun staticLines(position: Position, count: Int): List<EngineLine> {
        board = position.toBoard()
        val scored = ArrayList<EngineLine>(rootCount)
        val legal = rootMoves
        for (i in 0 until rootCount) {
            val m = legal[i]
            val san = San.format(board, m, legal, rootCount)
            board.make(m)
            val score = when {
                !board.hasLegalMoves() -> if (board.inCheck()) MATE - 1 else DRAW
                board.isInsufficientMaterial() -> DRAW
                else -> -Evaluation.evaluate(board)
            }
            board.unmake()
            scored.add(EngineLine(Moves.toMove(m), san, score, mateIn(score), listOf(san), 0))
        }
        return scored.sortedByDescending { it.scoreCp }.take(count)
    }

    companion object {
        const val MAX_PLY = 128
        const val MAX_DEPTH = 64
        const val INFINITE = 32_500
        const val MATE = 32_000

        /** Scores at or beyond this (in absolute value) are mates. */
        const val MATE_BOUND = MATE - 2 * MAX_PLY
        const val DRAW = 0

        private const val MAX_TIME_MS = 24L * 60 * 60 * 1000
        private const val ROOT_CAPACITY = 2048
        private const val POLL_MASK = 2047L
        private const val UNINTERRUPTED_NODES = 30_000L

        private const val ASPIRATION_MIN_DEPTH = 5
        private const val ASPIRATION_WINDOW = 30
        private const val ASPIRATION_LIMIT = 500

        /** Late-move reductions start with the move at this index. */
        private const val LMR_FROM = 3

        private const val HASH_SCORE = 3_000_000
        private const val GOOD_CAPTURE = 1_000_000
        private const val KILLER_FLOOR = 900_000
        private const val BAD_CAPTURE = -1_000_000
        private const val UNDER_PROMOTION = -2_000_000
        private const val HISTORY_LIMIT = 16_384
        private const val HISTORY_MAX_BONUS = 1_024

        /** Piece values for exchanges; knight = bishop so that trading one for the other is not a loss. */
        private val EXCHANGE_VALUE = intArrayOf(100, 325, 325, 500, 1000, 20_000)

        private val NEVER: () -> Boolean = { false }

        /** Mate scores are stored relative to the node, not the root, so they stay valid at any ply. */
        private fun toTable(score: Int, ply: Int): Int = when {
            score >= MATE_BOUND -> score + ply
            score <= -MATE_BOUND -> score - ply
            else -> score
        }

        private fun fromTable(score: Int, ply: Int): Int = when {
            score >= MATE_BOUND -> score - ply
            score <= -MATE_BOUND -> score + ply
            else -> score
        }

        /** Moves until mate: positive when the side to move mates, negative when it is mated, null otherwise. */
        fun mateIn(score: Int): Int? = when {
            score >= MATE_BOUND -> (MATE - score + 1) / 2
            score <= -MATE_BOUND -> -((MATE + score) / 2)
            else -> null
        }
    }
}
