package vn.npstudio.deviceinfo.domain.model

data class AppConfig(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val primaryColor: AppColor = AppColor.BRAND,
    val accentColor: AppColor = AppColor.BRAND,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
)

enum class DarkMode { OFF, ON, SYSTEM }

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

/** Color picker palette. [BRAND] maps to the brand primary or brand accent depending on the role. */
enum class AppColor {
    BRAND,
    RED,
    PINK,
    PURPLE,
    DEEP_PURPLE,
    INDIGO,
    BLUE,
    TEAL,
    GREEN,
    LIGHT_GREEN,
    AMBER,
    ORANGE,
    DEEP_ORANGE,
    BLUE_GREY,
}
