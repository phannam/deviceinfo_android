package vn.npstudio.deviceinfo.feature.main

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

class ShareReportRendererTest {

    private val resolve: (UiText) -> String = { text -> resolveText(text) }

    private fun resolveText(text: UiText): String =
        when (text) {
            is UiText.Dynamic -> text.value
            is UiText.Resource -> when (text.resId) {
                R.string.tab_device -> "Device"
                R.string.tab_display -> "Display"
                R.string.label_device_model -> "Model"
                R.string.label_display_font_scale -> "Font scale"
                else -> error("Unexpected resource ${text.resId}")
            }
            is UiText.Joined -> text.parts.joinToString(text.separator) { resolveText(it) }
        }

    @Test
    fun render_writesHeaderThenSections() {
        val sections = listOf(
            ReportSection(R.string.tab_device, listOf(InfoRowUiModel(R.string.label_device_model, UiText.Dynamic("Pixel")))),
            ReportSection(R.string.tab_display, listOf(InfoRowUiModel(R.string.label_display_font_scale, UiText.Dynamic("1.00×")))),
        )

        val text = ShareReportRenderer.render(sections, "Device Info 1.0.0", resolve)

        assertEquals("Device Info 1.0.0\n\nDevice\nModel: Pixel\n\nDisplay\nFont scale: 1.00×\n", text)
    }
}
