package vn.npstudio.deviceinfo.feature.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.HdrType
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleDisplayInfo
import vn.npstudio.deviceinfo.testing.sampleWindowSnapshot
import vn.npstudio.deviceinfo.ui.format.ValueFormatter

class DisplayRowMapperTest {

    @Test
    fun map_formatsDisplayAndWindowRows() {
        val rows = DisplayRowMapper.map(sampleDisplayInfo, sampleWindowSnapshot.copy(heightPx = 2768)).byLabel()

        assertEquals(UiText.res(R.string.value_resolution, 1440, 2960), rows[UiText.res(R.string.label_display_resolution)])
        assertEquals(UiText.res(R.string.value_resolution, 1440, 2768), rows[UiText.res(R.string.label_display_window_size)])
        assertEquals(UiText.res(R.string.value_dpi_bucket, 560, "xxxhdpi"), rows[UiText.res(R.string.label_display_density_dpi)])
        assertEquals(UiText.Joined(listOf(UiText.res(R.string.value_hz_int, 60)), ", "), rows[UiText.res(R.string.label_display_supported_refresh_rates)])
        assertEquals(UiText.Dynamic("HDR10"), rows[UiText.res(R.string.label_display_hdr)])
        assertEquals(UiText.res(R.string.common_yes), rows[UiText.res(R.string.label_display_wide_color_gamut)])
        assertEquals(UiText.res(R.string.common_no), rows[UiText.res(R.string.label_display_cutout)])
    }

    @Test
    fun map_withoutWindowShowsUnavailableWindowRows() {
        val rows = DisplayRowMapper.map(sampleDisplayInfo, null).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_window_size)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_cutout)])
    }

    @Test
    fun map_missingValuesAreUnavailable() {
        val info = sampleDisplayInfo.copy(heightPx = null, supportedRefreshRatesHz = emptyList(), hdrTypes = null)
        val rows = DisplayRowMapper.map(info, sampleWindowSnapshot).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_resolution)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_estimated_size)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_supported_refresh_rates)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_display_hdr)])
    }

    @Test
    fun hdr_emptyIsNotSupportedAndListIsOrdered() {
        val none = DisplayRowMapper.map(sampleDisplayInfo.copy(hdrTypes = emptyList()), null).byLabel()
        val many = DisplayRowMapper.map(
            sampleDisplayInfo.copy(hdrTypes = listOf(HdrType.HDR10_PLUS, HdrType.HDR10, HdrType.DOLBY_VISION)),
            null,
        ).byLabel()

        assertEquals(UiText.res(R.string.value_hdr_none), none[UiText.res(R.string.label_display_hdr)])
        assertEquals(UiText.Dynamic("Dolby Vision, HDR10, HDR10+"), many[UiText.res(R.string.label_display_hdr)])
    }

    @Test
    fun densityBucket_usesUpperBounds() {
        assertEquals("mdpi", DisplayRowMapper.densityBucket(160))
        assertEquals("xhdpi", DisplayRowMapper.densityBucket(280))
        assertEquals("xxhdpi", DisplayRowMapper.densityBucket(480))
        assertEquals("xxxhdpi", DisplayRowMapper.densityBucket(560))
    }

    @Test
    fun estimatedDiagonal_usesPhysicalPixelsAndDpi() {
        val info = sampleDisplayInfo.copy(widthPx = 1500, heightPx = 2000, xdpi = 500f, ydpi = 500f)

        assertEquals(5.0, DisplayRowMapper.estimatedDiagonalInches(info)!!, 0.0001)
        assertNull(DisplayRowMapper.estimatedDiagonalInches(info.copy(xdpi = 0f)))
    }
}
