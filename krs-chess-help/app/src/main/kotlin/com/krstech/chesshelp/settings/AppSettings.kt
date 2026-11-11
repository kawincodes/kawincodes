package com.krstech.chesshelp.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.net.URI
import java.net.URISyntaxException

enum class EffortSetting {
    LOW, MEDIUM, HIGH;

    companion object {
        val DEFAULT = MEDIUM

        fun parse(value: String?): EffortSetting = parseEnum(value, DEFAULT)
    }
}

/** Which side the app recommends moves for. BOTTOM = the side shown at the bottom of the board. */
enum class AdviseForSetting {
    BOTTOM, WHITE, BLACK, BOTH;

    companion object {
        val DEFAULT = BOTTOM

        fun parse(value: String?): AdviseForSetting = parseEnum(value, DEFAULT)
    }
}

private inline fun <reified T : Enum<T>> parseEnum(value: String?, default: T): T {
    val wanted = value?.trim().orEmpty()
    return enumValues<T>().firstOrNull { it.name.equals(wanted, ignoreCase = true) } ?: default
}

/** A window position as a fraction (0..1) of the free travel on each axis, so it survives rotation. */
data class FractionPoint(val x: Float, val y: Float)

object BaseUrlValidator {
    /**
     * Returns the normalised URL ("" for blank input), or null when the input is not acceptable.
     * Only https is accepted: the API key travels in a request header.
     */
    fun normalize(input: String?): String? {
        val text = input?.trim().orEmpty()
        if (text.isEmpty()) return ""
        if (text.any { it.isWhitespace() }) return null
        val uri = try {
            URI(text)
        } catch (_: URISyntaxException) {
            return null
        }
        if (!"https".equals(uri.scheme, ignoreCase = true)) return null
        if (uri.host.isNullOrEmpty() || uri.userInfo != null || uri.query != null || uri.fragment != null) return null
        return text.trimEnd('/')
    }
}

/** Typed access to the user's settings. Cheap to construct; every instance sees the same values. */
class AppSettings internal constructor(private val prefs: SharedPreferences) {

    constructor(context: Context) :
        this(context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    /** Always one of [ModelCatalog.ALL]; an unknown stored id falls back to the default model. */
    var modelId: String
        get() = ModelCatalog.byId(prefs.getString(KEY_MODEL, null)).id
        set(value) = prefs.edit { putString(KEY_MODEL, ModelCatalog.byId(value).id) }

    var effort: EffortSetting
        get() = EffortSetting.parse(prefs.getString(KEY_EFFORT, null))
        set(value) = prefs.edit { putString(KEY_EFFORT, value.name) }

    var adviseFor: AdviseForSetting
        get() = AdviseForSetting.parse(prefs.getString(KEY_ADVISE_FOR, null))
        set(value) = prefs.edit { putString(KEY_ADVISE_FOR, value.name) }

    var autoAdvise: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ADVISE, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_ADVISE, value) }

    var drawOnBoard: Boolean
        get() = prefs.getBoolean(KEY_DRAW_ON_BOARD, true)
        set(value) = prefs.edit { putBoolean(KEY_DRAW_ON_BOARD, value) }

    var sendScreenContext: Boolean
        get() = prefs.getBoolean(KEY_SEND_SCREEN_CONTEXT, true)
        set(value) = prefs.edit { putBoolean(KEY_SEND_SCREEN_CONTEXT, value) }

    /** "" = api.anthropic.com. Values that [BaseUrlValidator] rejects are stored as "". */
    var customBaseUrl: String
        get() = BaseUrlValidator.normalize(prefs.getString(KEY_BASE_URL, null)).orEmpty()
        set(value) = prefs.edit { putString(KEY_BASE_URL, BaseUrlValidator.normalize(value).orEmpty()) }

    /** [customBaseUrl] in the form the API client wants: null when the default endpoint is used. */
    val baseUrlOrNull: String?
        get() = customBaseUrl.ifEmpty { null }

    var disclosureAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLOSURE, false)
        set(value) = prefs.edit { putBoolean(KEY_DISCLOSURE, value) }

    var bubblePosition: FractionPoint
        get() = readPoint(KEY_BUBBLE_X, KEY_BUBBLE_Y) ?: DEFAULT_BUBBLE_POSITION
        set(value) = writePoint(KEY_BUBBLE_X, KEY_BUBBLE_Y, value)

    /** Where the user dragged the panel, or null while it still follows the bubble. */
    var panelPosition: FractionPoint?
        get() = readPoint(KEY_PANEL_X, KEY_PANEL_Y)
        set(value) = writePoint(KEY_PANEL_X, KEY_PANEL_Y, value)

    private fun readPoint(keyX: String, keyY: String): FractionPoint? {
        val x = prefs.getFloat(keyX, Float.NaN)
        val y = prefs.getFloat(keyY, Float.NaN)
        if (x.isNaN() || y.isNaN()) return null
        return FractionPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
    }

    private fun writePoint(keyX: String, keyY: String, value: FractionPoint?) = prefs.edit {
        if (value == null || value.x.isNaN() || value.y.isNaN()) {
            remove(keyX)
            remove(keyY)
        } else {
            putFloat(keyX, value.x.coerceIn(0f, 1f))
            putFloat(keyY, value.y.coerceIn(0f, 1f))
        }
    }

    companion object {
        const val PREFS_NAME = "krs_settings"

        /** Right edge, upper third: clear of most boards and of the navigation area. */
        val DEFAULT_BUBBLE_POSITION = FractionPoint(1f, 0.22f)

        private const val KEY_MODEL = "model_id"
        private const val KEY_EFFORT = "effort"
        private const val KEY_ADVISE_FOR = "advise_for"
        private const val KEY_AUTO_ADVISE = "auto_advise"
        private const val KEY_DRAW_ON_BOARD = "draw_on_board"
        private const val KEY_SEND_SCREEN_CONTEXT = "send_screen_context"
        private const val KEY_BASE_URL = "custom_base_url"
        private const val KEY_DISCLOSURE = "disclosure_accepted"
        private const val KEY_BUBBLE_X = "bubble_x"
        private const val KEY_BUBBLE_Y = "bubble_y"
        private const val KEY_PANEL_X = "panel_x"
        private const val KEY_PANEL_Y = "panel_y"
    }
}
