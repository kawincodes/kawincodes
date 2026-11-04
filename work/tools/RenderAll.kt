package render

import android.view.View
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class Spec(val name: String, val layout: String, val theme: String, val mode: String, val locale: String) {
    override fun toString() = name
}

@RunWith(Parameterized::class)
class RenderAll(private val spec: Spec) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<Any>> {
            val filter = System.getProperty("render.filter") ?: ""
            return RenderAll::class.java.getResourceAsStream("/specs.tsv")!!.bufferedReader().readLines()
                .filter { it.isNotBlank() }
                .map { it.split('\t') }
                .filter { filter.isEmpty() || Regex(filter).containsMatchIn(it[0]) }
                .map { p -> arrayOf<Any>(Spec(p[0], p[1], p[2], p[3], p[4])) }
        }
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(locale = spec.locale.ifEmpty { null }),
        theme = spec.theme,
        renderingMode = when (spec.mode) {
            "SHRINK" -> RenderingMode.SHRINK
            "V_SCROLL" -> RenderingMode.V_SCROLL
            else -> RenderingMode.NORMAL
        },
        showSystemUi = false,
        useDeviceResolution = true,
    )

    @Test
    fun render() {
        val ctx = paparazzi.context
        val id = ctx.resources.getIdentifier(spec.layout, "layout", ctx.packageName)
        require(id != 0) { "layout not found: ${spec.layout}" }
        val view = paparazzi.inflate<View>(id)
        paparazzi.snapshot(view, spec.name)
    }
}
