package vn.npstudio.deviceinfo.ui.format

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

    /** Formats bytes with decimal units (1 GB = 1000^3 bytes) to match Android Settings. */
    fun bytes(bytes: Long?): UiText {
        if (bytes == null) return unavailable
        if (bytes < 1_000) return UiText.res(R.string.value_bytes, bytes)
        var value = bytes / 1_000.0
        var unit = 0
        // 999.995 would print as "1000.00" with two decimals, so move to the next unit.
        while (value >= 999.995 && unit < BYTE_UNITS.lastIndex) {
            value /= 1_000
            unit++
        }
        return UiText.res(BYTE_UNITS[unit], value)
    }

    private val BYTE_UNITS = listOf(
        R.string.value_kilobytes,
        R.string.value_megabytes,
        R.string.value_gigabytes,
        R.string.value_terabytes,
    )

    /** Joins values with `, `, or returns unavailable when empty. */
    fun list(values: List<String>): UiText =
        if (values.isEmpty()) unavailable else UiText.Dynamic(values.joinToString(", "))

    /** Formats elapsed time with seconds, for example `2d 3h 15m 42s`, for live uptime. */
    fun durationSeconds(millis: Long): UiText {
        val totalSeconds = millis.coerceAtLeast(0) / 1_000
        return UiText.res(
            R.string.value_duration_dhms,
            totalSeconds / 86_400,
            totalSeconds / 3_600 % 24,
            totalSeconds / 60 % 60,
            totalSeconds % 60,
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

    fun supported(value: Boolean?): UiText = when (value) {
        null -> unavailable
        true -> UiText.res(R.string.common_supported)
        false -> UiText.res(R.string.common_not_supported)
    }

    /** Formats epoch [millis] as a medium date and short time in [zone] and [locale]. */
    fun dateTime(millis: Long?, zone: ZoneId, locale: Locale): UiText {
        if (millis == null || millis <= 0) return unavailable
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale)
        return UiText.Dynamic(Instant.ofEpochMilli(millis).atZone(zone).format(formatter))
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
