package com.krstech.chesshelp.brain

import com.krstech.chesshelp.claude.AdviceRequest
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeChessClient
import com.krstech.chesshelp.claude.ClaudeConfig
import com.krstech.chesshelp.claude.PositionOracle
import com.krstech.chesshelp.claude.VisionRequest

/** The real [AdviceService]: a [ClaudeChessClient] for one configuration. */
class ClaudeAdviceService(config: ClaudeConfig) : AdviceService {
    private val client = ClaudeChessClient(config)

    override fun analyzeBoard(request: VisionRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult =
        client.analyzeBoard(request, oracle, onProgress)

    override fun advise(request: AdviceRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult =
        client.advise(request, oracle, onProgress)

    override fun close() = client.close()

    companion object {
        /** Pass this as the `claude` argument of [ChessBrain]. */
        val Factory: AdviceServiceFactory = AdviceServiceFactory { config -> ClaudeAdviceService(config) }
    }
}
