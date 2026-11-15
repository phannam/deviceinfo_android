package vn.npstudio.deviceinfo.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import java.util.Locale

class ValueFormatterTest {

    @Test
    fun duration_splitsIntoDaysHoursMinutes() {
        val millis = ((2 * 24 + 3) * 60 + 15) * 60_000L + 59_000L

        assertEquals(UiText.res(R.string.value_duration_dhm, 2L, 3L, 15L), ValueFormatter.duration(millis))
    }

    @Test
    fun duration_negativeIsZero() {
        assertEquals(UiText.res(R.string.value_duration_dhm, 0L, 0L, 0L), ValueFormatter.duration(-5L))
    }

    @Test
    fun isoDate_parsableIsLocalized() {
        assertEquals(UiText.Dynamic("Mar 1, 2021"), ValueFormatter.isoDate("2021-03-01", Locale.US))
    }

    @Test
    fun isoDate_unparsableKeepsRawText() {
        assertEquals(UiText.Dynamic("2021-3"), ValueFormatter.isoDate("2021-3", Locale.US))
    }

    @Test
    fun nullValues_areUnavailable() {
        assertEquals(ValueFormatter.unavailable, ValueFormatter.isoDate(null, Locale.US))
        assertEquals(ValueFormatter.unavailable, ValueFormatter.text(null))
    }

    @Test
    fun frequencyKhz_usesMhzBelowOneGhz() {
        assertEquals(UiText.res(R.string.value_frequency_mhz, 999L), ValueFormatter.frequencyKhz(999_999L))
        assertEquals(UiText.res(R.string.value_frequency_ghz, 1.0), ValueFormatter.frequencyKhz(1_000_000L))
        assertEquals(UiText.res(R.string.value_frequency_ghz, 2.3616), ValueFormatter.frequencyKhz(2_361_600L))
        assertEquals(ValueFormatter.unavailable, ValueFormatter.frequencyKhz(null))
    }

    @Test
    fun hoursMinutes_splitsHoursAndMinutes() {
        assertEquals(UiText.res(R.string.value_duration_hm, 26L, 5L), ValueFormatter.hoursMinutes((26 * 60 + 5) * 60_000L))
    }

    @Test
    fun refreshRate_dropsDecimalsWhenIntegral() {
        assertEquals(UiText.res(R.string.value_hz_int, 60), ValueFormatter.refreshRate(60.000004f))
        assertEquals(UiText.res(R.string.value_hz_decimal, 59.94f), ValueFormatter.refreshRate(59.94f))
        assertEquals(ValueFormatter.unavailable, ValueFormatter.refreshRate(null))
    }

    @Test
    fun bytes_usesDecimalUnits() {
        assertEquals(UiText.res(R.string.value_bytes, 999L), ValueFormatter.bytes(999L))
        assertEquals(UiText.res(R.string.value_kilobytes, 1.5), ValueFormatter.bytes(1_500L))
        assertEquals(UiText.res(R.string.value_megabytes, 226.0), ValueFormatter.bytes(226_000_000L))
        assertEquals(UiText.res(R.string.value_gigabytes, 64.0), ValueFormatter.bytes(64_000_000_000L))
        assertEquals(UiText.res(R.string.value_terabytes, 2_000.0), ValueFormatter.bytes(2_000_000_000_000_000L))
        assertEquals(ValueFormatter.unavailable, ValueFormatter.bytes(null))
    }

    @Test
    fun bytes_movesToNextUnitWhenRoundingReachesOneThousand() {
        assertEquals(UiText.res(R.string.value_megabytes, 0.999999), ValueFormatter.bytes(999_999L))
    }
}
