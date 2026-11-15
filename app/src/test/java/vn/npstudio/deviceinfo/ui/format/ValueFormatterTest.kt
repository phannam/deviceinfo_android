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
}
