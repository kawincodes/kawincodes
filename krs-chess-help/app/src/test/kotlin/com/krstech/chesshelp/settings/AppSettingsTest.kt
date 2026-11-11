package com.krstech.chesshelp.settings

import com.krstech.chesshelp.FakeSharedPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppSettingsTest {
    private val prefs = FakeSharedPreferences()
    private val settings = AppSettings(prefs)

    @Test
    fun `defaults match the spec`() {
        assertEquals("claude-opus-5-5", settings.modelId)
        assertEquals(EffortSetting.MEDIUM, settings.effort)
        assertEquals(AdviseForSetting.BOTTOM, settings.adviseFor)
        assertTrue(settings.autoAdvise)
        assertTrue(settings.drawOnBoard)
        assertTrue(settings.sendScreenContext)
        assertEquals("", settings.customBaseUrl)
        assertNull(settings.baseUrlOrNull)
        assertFalse(settings.disclosureAccepted)
        assertEquals(AppSettings.DEFAULT_BUBBLE_POSITION, settings.bubblePosition)
        assertNull(settings.panelPosition)
    }

    @Test
    fun `values round trip and are visible to another instance`() {
        settings.modelId = "claude-haiku-5-5"
        settings.effort = EffortSetting.HIGH
        settings.adviseFor = AdviseForSetting.BOTH
        settings.autoAdvise = false
        settings.drawOnBoard = false
        settings.sendScreenContext = false
        settings.customBaseUrl = " https://gateway.example.com/anthropic/ "
        settings.disclosureAccepted = true
        settings.bubblePosition = FractionPoint(0.25f, 0.75f)
        settings.panelPosition = FractionPoint(0f, 1f)

        val other = AppSettings(prefs)
        assertEquals("claude-haiku-5-5", other.modelId)
        assertEquals(EffortSetting.HIGH, other.effort)
        assertEquals(AdviseForSetting.BOTH, other.adviseFor)
        assertFalse(other.autoAdvise)
        assertFalse(other.drawOnBoard)
        assertFalse(other.sendScreenContext)
        assertEquals("https://gateway.example.com/anthropic", other.customBaseUrl)
        assertEquals("https://gateway.example.com/anthropic", other.baseUrlOrNull)
        assertTrue(other.disclosureAccepted)
        assertEquals(FractionPoint(0.25f, 0.75f), other.bubblePosition)
        assertEquals(FractionPoint(0f, 1f), other.panelPosition)
    }

    @Test
    fun `unknown stored values fall back to defaults`() {
        prefs.values["model_id"] = "claude-3-opus"
        prefs.values["effort"] = "EXTREME"
        prefs.values["advise_for"] = ""
        prefs.values["custom_base_url"] = "http://insecure.example.com"
        assertEquals(ModelCatalog.DEFAULT_ID, settings.modelId)
        assertEquals(EffortSetting.MEDIUM, settings.effort)
        assertEquals(AdviseForSetting.BOTTOM, settings.adviseFor)
        assertEquals("", settings.customBaseUrl)
    }

    @Test
    fun `setting an unknown model stores the default`() {
        settings.modelId = "gpt-4"
        assertEquals(ModelCatalog.DEFAULT_ID, prefs.values["model_id"])
    }

    @Test
    fun `positions are clamped and can be cleared`() {
        settings.bubblePosition = FractionPoint(-3f, 7f)
        assertEquals(FractionPoint(0f, 1f), settings.bubblePosition)
        settings.panelPosition = FractionPoint(0.5f, 0.5f)
        settings.panelPosition = null
        assertNull(settings.panelPosition)
        settings.panelPosition = FractionPoint(Float.NaN, 0.5f)
        assertNull(settings.panelPosition)
    }

    @Test
    fun `enum parsing is lenient about case and whitespace`() {
        assertEquals(EffortSetting.LOW, EffortSetting.parse(" low "))
        assertEquals(EffortSetting.HIGH, EffortSetting.parse("High"))
        assertEquals(EffortSetting.MEDIUM, EffortSetting.parse(null))
        assertEquals(EffortSetting.MEDIUM, EffortSetting.parse("max"))
        assertEquals(AdviseForSetting.WHITE, AdviseForSetting.parse("white"))
        assertEquals(AdviseForSetting.BLACK, AdviseForSetting.parse("BLACK"))
        assertEquals(AdviseForSetting.BOTH, AdviseForSetting.parse("Both"))
        assertEquals(AdviseForSetting.BOTTOM, AdviseForSetting.parse("top"))
    }

    @Test
    fun `setting names line up with the analysis module's enums`() {
        assertEquals(listOf("LOW", "MEDIUM", "HIGH"), EffortSetting.entries.map { it.name })
        assertEquals(listOf("BOTTOM", "WHITE", "BLACK", "BOTH"), AdviseForSetting.entries.map { it.name })
    }
}

class BaseUrlValidatorTest {
    @Test
    fun `blank means default endpoint`() {
        assertEquals("", BaseUrlValidator.normalize(null))
        assertEquals("", BaseUrlValidator.normalize(""))
        assertEquals("", BaseUrlValidator.normalize("   "))
    }

    @Test
    fun `https urls are accepted and normalised`() {
        assertEquals("https://api.example.com", BaseUrlValidator.normalize("https://api.example.com"))
        assertEquals("https://api.example.com", BaseUrlValidator.normalize("  https://api.example.com/// "))
        assertEquals("https://proxy.example.com:8443/v1/anthropic", BaseUrlValidator.normalize("https://proxy.example.com:8443/v1/anthropic/"))
        assertEquals("HTTPS://API.EXAMPLE.COM", BaseUrlValidator.normalize("HTTPS://API.EXAMPLE.COM"))
    }

    @Test
    fun `everything else is rejected`() {
        for (bad in listOf(
            "http://api.example.com", "api.example.com", "ftp://x.y", "https://", "https:///path",
            "https://exa mple.com", "https://user:pw@example.com", "https://example.com?x=1",
            "https://example.com#frag", "javascript:alert(1)", "https://exa\tmple.com", "not a url",
        )) {
            assertNull(BaseUrlValidator.normalize(bad), bad)
        }
    }
}

class ModelCatalogTest {
    @Test
    fun `lists the four models with exact ids and opus as default`() {
        assertEquals(
            listOf("claude-opus-5-5", "claude-sonnet-5-5", "claude-haiku-5-5", "claude-fable-5-2"),
            ModelCatalog.ALL.map { it.id },
        )
        assertEquals("claude-opus-5-5", ModelCatalog.DEFAULT_ID)
        assertEquals("Claude Opus 5.5", ModelCatalog.DEFAULT.label)
        assertEquals(4, ModelCatalog.ALL.map { it.label }.toSet().size)
        assertEquals(4, ModelCatalog.ALL.map { it.noteRes }.toSet().size)
    }

    @Test
    fun `lookup falls back to the default`() {
        assertEquals("claude-fable-5-2", ModelCatalog.byId("claude-fable-5-2").id)
        assertEquals(ModelCatalog.DEFAULT, ModelCatalog.byId(null))
        assertEquals(ModelCatalog.DEFAULT, ModelCatalog.byId("nope"))
        assertEquals(2, ModelCatalog.indexOf("claude-haiku-5-5"))
        assertEquals(0, ModelCatalog.indexOf("nope"))
    }
}
