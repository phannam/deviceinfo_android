package vn.npstudio.deviceinfo.feature.display

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

object DisplayRowMapper {

    fun map(info: DisplayInfo): List<InfoRowUiModel> = listOf(
        InfoRowUiModel(R.string.label_display_resolution, resolution(info)),
        InfoRowUiModel(R.string.label_display_density_dpi, UiText.res(R.string.value_dpi_int, info.densityDpi)),
        InfoRowUiModel(R.string.label_display_density, UiText.res(R.string.value_scale, info.density)),
        InfoRowUiModel(R.string.label_display_xdpi, UiText.res(R.string.value_dpi_decimal, info.xdpi)),
        InfoRowUiModel(R.string.label_display_ydpi, UiText.res(R.string.value_dpi_decimal, info.ydpi)),
        InfoRowUiModel(R.string.label_display_font_scale, UiText.res(R.string.value_scale, info.fontScale)),
    )

    private fun resolution(info: DisplayInfo): UiText {
        val width = info.widthPx ?: return ValueFormatter.unavailable
        val height = info.heightPx ?: return ValueFormatter.unavailable
        return UiText.res(R.string.value_resolution, width, height)
    }
}
