package vn.npstudio.deviceinfo.feature.display

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.model.HdrType
import vn.npstudio.deviceinfo.domain.model.WindowSnapshot
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import vn.npstudio.deviceinfo.ui.list.InfoSection
import vn.npstudio.deviceinfo.ui.list.reportRows
import kotlin.math.sqrt

object DisplayRowMapper {

    /** [window] is null when the Activity window has not been measured (for example, in the share report). */
    fun sections(info: DisplayInfo, window: WindowSnapshot?): List<InfoSection> = listOf(
        InfoSection(R.string.section_screen, R.drawable.ic_smartphone, screenRows(info, window)),
        InfoSection(R.string.section_refresh_color, R.drawable.ic_tune, refreshColorRows(info)),
        InfoSection(R.string.section_window, R.drawable.ic_web_asset, windowRows(info, window)),
    )

    fun map(info: DisplayInfo, window: WindowSnapshot?): List<InfoRowUiModel> = sections(info, window).reportRows()

    private fun screenRows(info: DisplayInfo, window: WindowSnapshot?) = listOf(
        InfoRowUiModel(R.string.label_display_resolution, resolution(info.widthPx, info.heightPx)),
        InfoRowUiModel(
            R.string.label_display_density_dpi,
            UiText.res(R.string.value_dpi_bucket, info.densityDpi, densityBucket(info.densityDpi)),
        ),
        InfoRowUiModel(R.string.label_display_density, UiText.res(R.string.value_scale, info.density)),
        InfoRowUiModel(R.string.label_display_xdpi, UiText.res(R.string.value_dpi_decimal, info.xdpi)),
        InfoRowUiModel(R.string.label_display_ydpi, UiText.res(R.string.value_dpi_decimal, info.ydpi)),
        InfoRowUiModel(
            R.string.label_display_estimated_size,
            estimatedDiagonalInches(info)?.let { UiText.res(R.string.value_inches, it) } ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(R.string.label_display_cutout, ValueFormatter.yesNo(window?.hasCutout)),
    )

    private fun refreshColorRows(info: DisplayInfo) = listOf(
        InfoRowUiModel(R.string.label_display_refresh_rate, ValueFormatter.refreshRate(info.refreshRateHz)),
        InfoRowUiModel(R.string.label_display_supported_refresh_rates, refreshRates(info.supportedRefreshRatesHz)),
        InfoRowUiModel(R.string.label_display_hdr, hdr(info.hdrTypes)),
        InfoRowUiModel(R.string.label_display_wide_color_gamut, ValueFormatter.yesNo(info.wideColorGamut)),
    )

    private fun windowRows(info: DisplayInfo, window: WindowSnapshot?) = listOf(
        InfoRowUiModel(R.string.label_display_window_size, resolution(window?.widthPx, window?.heightPx)),
        InfoRowUiModel(R.string.label_display_font_scale, UiText.res(R.string.value_scale, info.fontScale)),
    )

    fun densityBucket(dpi: Int): String = when {
        dpi <= 120 -> "ldpi"
        dpi <= 160 -> "mdpi"
        dpi <= 213 -> "tvdpi"
        dpi <= 240 -> "hdpi"
        dpi <= 320 -> "xhdpi"
        dpi <= 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }

    /** Diagonal from physical pixels and OEM-reported dpi; some builds report wrong dpi. */
    fun estimatedDiagonalInches(info: DisplayInfo): Double? {
        val width = info.widthPx ?: return null
        val height = info.heightPx ?: return null
        if (info.xdpi <= 0f || info.ydpi <= 0f) return null
        val w = width / info.xdpi.toDouble()
        val h = height / info.ydpi.toDouble()
        return sqrt(w * w + h * h)
    }

    private fun resolution(width: Int?, height: Int?): UiText {
        if (width == null || height == null) return ValueFormatter.unavailable
        return UiText.res(R.string.value_resolution, width, height)
    }

    private fun refreshRates(rates: List<Float>): UiText =
        if (rates.isEmpty()) ValueFormatter.unavailable else UiText.Joined(rates.map(ValueFormatter::refreshRate), ", ")

    private fun hdr(types: List<HdrType>?): UiText = when {
        types == null -> ValueFormatter.unavailable
        types.isEmpty() -> UiText.res(R.string.value_hdr_none)
        else -> UiText.Dynamic(types.sortedBy { it.ordinal }.joinToString(", ", transform = ::hdrName))
    }

    private fun hdrName(type: HdrType): String = when (type) {
        HdrType.DOLBY_VISION -> "Dolby Vision"
        HdrType.HDR10 -> "HDR10"
        HdrType.HLG -> "HLG"
        HdrType.HDR10_PLUS -> "HDR10+"
    }
}
