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
}
