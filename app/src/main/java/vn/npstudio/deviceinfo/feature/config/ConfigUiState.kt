package vn.npstudio.deviceinfo.feature.config

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.AppConfig
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit

data class ConfigUiState(
    val darkMode: DarkMode,
    val temperatureUnit: TemperatureUnit,
    val primaryColor: AppColor,
    val accentColor: AppColor,
) {
    val darkModeLabel: UiText get() = UiText.res(darkMode.labelRes)
    val temperatureUnitLabel: UiText get() = UiText.res(temperatureUnit.labelRes)

    companion object {
        fun from(config: AppConfig) =
            ConfigUiState(config.darkMode, config.temperatureUnit, config.primaryColor, config.accentColor)
    }
}

@get:StringRes
val DarkMode.labelRes: Int
    get() = when (this) {
        DarkMode.OFF -> R.string.config_dark_mode_off
        DarkMode.ON -> R.string.config_dark_mode_on
        DarkMode.SYSTEM -> R.string.config_dark_mode_system
    }

@get:StringRes
val TemperatureUnit.labelRes: Int
    get() = when (this) {
        TemperatureUnit.CELSIUS -> R.string.config_temperature_celsius
        TemperatureUnit.FAHRENHEIT -> R.string.config_temperature_fahrenheit
    }
