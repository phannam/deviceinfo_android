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
