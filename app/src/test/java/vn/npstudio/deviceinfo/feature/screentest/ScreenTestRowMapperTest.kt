package vn.npstudio.deviceinfo.feature.screentest

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.ScreenDefect
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.testing.byLabel

class ScreenTestRowMapperTest {

    @Test
    fun map_noResultIsNotTested() {
        val rows = ScreenTestRowMapper.map(null)

        assertEquals(ScreenDefect.entries.size, rows.size)
        rows.forEach { assertEquals(UiText.res(R.string.screen_test_not_tested), it.value) }
    }

    @Test
    fun map_reportedDefectsAreYes() {
        val rows = ScreenTestRowMapper.map(ScreenTestResult(setOf(ScreenDefect.LINES_OR_BANDING))).byLabel()

        assertEquals(UiText.res(R.string.common_no), rows[UiText.res(R.string.label_screen_test_dead_pixels)])
        assertEquals(UiText.res(R.string.common_yes), rows[UiText.res(R.string.label_screen_test_lines)])
        assertEquals(UiText.res(R.string.common_no), rows[UiText.res(R.string.label_screen_test_burn_in)])
    }
}
