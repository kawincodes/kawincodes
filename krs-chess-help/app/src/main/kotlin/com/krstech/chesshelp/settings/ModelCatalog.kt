package com.krstech.chesshelp.settings

import androidx.annotation.StringRes
import com.krstech.chesshelp.R

/** [label] is the product name (not translated); [noteRes] is the one-line description shown next to it. */
data class ModelOption(val id: String, val label: String, @param:StringRes val noteRes: Int)

object ModelCatalog {
    const val DEFAULT_ID = "claude-opus-5-5"

    val ALL: List<ModelOption> = listOf(
        ModelOption("claude-opus-5-5", "Claude Opus 5.5", R.string.model_note_opus),
        ModelOption("claude-sonnet-5-5", "Claude Sonnet 5.5", R.string.model_note_sonnet),
        ModelOption("claude-haiku-5-5", "Claude Haiku 5.5", R.string.model_note_haiku),
        ModelOption("claude-fable-5-2", "Claude Fable 5.2", R.string.model_note_fable),
    )

    val DEFAULT: ModelOption = ALL.first { it.id == DEFAULT_ID }

    /** The model with this id, or the default when the id is null or unknown. */
    fun byId(id: String?): ModelOption = ALL.firstOrNull { it.id == id } ?: DEFAULT

    fun indexOf(id: String?): Int = ALL.indexOf(byId(id))
}
