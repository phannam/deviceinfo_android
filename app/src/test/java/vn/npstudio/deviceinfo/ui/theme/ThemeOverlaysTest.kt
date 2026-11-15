package vn.npstudio.deviceinfo.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.AppColor

class ThemeOverlaysTest {

    private val colors = AppColor.entries

    @Test
    fun everyColorHasDistinctOverlaysPerRole() {
        val overlays = ColorRole.entries.flatMap { role -> colors.map { ThemeOverlays.overlay(role, it) } }

        assertEquals(colors.size * ColorRole.entries.size, overlays.toSet().size)
    }

    @Test
    fun everyColorHasDistinctSwatchAndName() {
        ColorRole.entries.forEach { role ->
            assertEquals(colors.size, colors.map { ThemeOverlays.swatch(role, it) }.toSet().size)
        }
        assertEquals(colors.size, colors.map(ThemeOverlays::name).toSet().size)
    }

    @Test
    fun brandUsesBrandPrimaryOrAccent() {
        assertEquals(R.color.brand_primary, ThemeOverlays.swatch(ColorRole.PRIMARY, AppColor.BRAND))
        assertEquals(R.color.brand_accent, ThemeOverlays.swatch(ColorRole.ACCENT, AppColor.BRAND))
        assertEquals(R.color.palette_red, ThemeOverlays.swatch(ColorRole.ACCENT, AppColor.RED))
    }
}
