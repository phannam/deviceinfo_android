package vn.npstudio.deviceinfo.ui.format

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

/** Pure formatting helpers shared by row mappers. */
object ValueFormatter {

    val unavailable: UiText = UiText.res(R.string.common_unavailable)

    fun text(value: String?): UiText = value?.let(UiText::Dynamic) ?: unavailable

    fun number(value: Int): UiText = UiText.Dynamic(value.toString())

    /** Formats kHz as integer MHz below 1 GHz, otherwise as GHz with two decimals. */
    fun frequencyKhz(khz: Long?): UiText = when {
        khz == null -> unavailable
        khz < 1_000_000 -> UiText.res(R.string.value_frequency_mhz, khz / 1_000)
        else -> UiText.res(R.string.value_frequency_ghz, khz / 1_000_000.0)
    }

    /** Joins values with `, `, or returns unavailable when empty. */
    fun list(values: List<String>): UiText =
        if (values.isEmpty()) unavailable else UiText.Dynamic(values.joinToString(", "))

    /** Formats elapsed time as days, hours and minutes, for example `2d 3h 15m`. */
    fun duration(millis: Long): UiText {
        val totalMinutes = millis.coerceAtLeast(0) / 60_000
        return UiText.res(
            R.string.value_duration_dhm,
            totalMinutes / (24 * 60),
            totalMinutes / 60 % 24,
            totalMinutes % 60,
        )
    }

    /** Formats Hz without decimals when integral (within 0.01), otherwise with two decimals. */
    fun refreshRate(hz: Float?): UiText {
        if (hz == null) return unavailable
        val rounded = kotlin.math.round(hz)
        return if (kotlin.math.abs(hz - rounded) < 0.01f) {
            UiText.res(R.string.value_hz_int, rounded.toInt())
        } else {
            UiText.res(R.string.value_hz_decimal, hz)
        }
    }

    /** Formats a duration as hours and minutes, for example `1h 25m`. */
    fun hoursMinutes(millis: Long): UiText {
        val totalMinutes = millis.coerceAtLeast(0) / 60_000
        return UiText.res(R.string.value_duration_hm, totalMinutes / 60, totalMinutes % 60)
    }

    fun yesNo(value: Boolean?): UiText = when (value) {
        null -> unavailable
        true -> UiText.res(R.string.common_yes)
        false -> UiText.res(R.string.common_no)
    }

    fun onOff(value: Boolean?): UiText = when (value) {
        null -> unavailable
        true -> UiText.res(R.string.common_on)
        false -> UiText.res(R.string.common_off)
    }

    /** Formats an ISO `yyyy-MM-dd` date in [locale]; keeps the raw text when it does not parse. */
    fun isoDate(raw: String?, locale: Locale): UiText {
        if (raw == null) return unavailable
        return try {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
            UiText.Dynamic(LocalDate.parse(raw).format(formatter))
        } catch (_: DateTimeParseException) {
            UiText.Dynamic(raw)
        }
    }
}
