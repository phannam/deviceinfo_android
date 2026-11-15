package vn.npstudio.deviceinfo.feature.device

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import java.util.Locale

class DeviceRowMapperTest {

    @Test
    fun map_formatsRows() {
        val rows = DeviceRowMapper.map(sampleDeviceInfo, Locale.US).byLabel()

        assertEquals(UiText.Dynamic("SM-N950U1"), rows[UiText.res(R.string.label_device_model)])
        assertEquals(UiText.Dynamic("28"), rows[UiText.res(R.string.label_device_api_level)])
        assertEquals(UiText.Dynamic("Mar 1, 2021"), rows[UiText.res(R.string.label_device_security_patch)])
        assertEquals(UiText.res(R.string.value_duration_dhm, 1L, 1L, 1L), rows[UiText.res(R.string.label_device_uptime)])
    }

    @Test
    fun map_missingValuesAreUnavailable() {
        val info = sampleDeviceInfo.copy(bootloader = null, securityPatch = null)

        val rows = DeviceRowMapper.map(info, Locale.US).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_device_bootloader)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_device_security_patch)])
    }
}
