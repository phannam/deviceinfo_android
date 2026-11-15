package vn.npstudio.deviceinfo.feature.display

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleDisplayInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter

class DisplayRowMapperTest {

    @Test
    fun map_formatsResolution() {
        val rows = DisplayRowMapper.map(sampleDisplayInfo).byLabel()

        assertEquals(UiText.res(R.string.value_resolution, 1440, 2960), rows[UiText.res(R.string.label_display_resolution)])
    }

    @Test
    fun map_missingResolutionIsUnavailable() {
        val rows = DisplayRowMapper.map(sampleDisplayInfo.copy(heightPx = null))
            .byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_resolution)])
    }
}
