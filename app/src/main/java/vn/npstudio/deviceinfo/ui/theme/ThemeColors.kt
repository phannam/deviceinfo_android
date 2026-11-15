package vn.npstudio.deviceinfo.ui.theme

import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.AppConfig

/** The colors baked into an Activity's theme; a change requires recreating the Activity. */
data class ThemeColors(val primary: AppColor, val accent: AppColor) {
    companion object {
        fun of(config: AppConfig) = ThemeColors(config.primaryColor, config.accentColor)
    }
}
