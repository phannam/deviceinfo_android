package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.AppConfig
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit

/** Persisted user configuration. Values load synchronously so the theme can be applied before views inflate. */
interface AppConfigRepository {
    val config: StateFlow<AppConfig>
    val tabs: StateFlow<TabPreferences>

    fun setDarkMode(mode: DarkMode)

    fun setPrimaryColor(color: AppColor)

    fun setAccentColor(color: AppColor)

    fun setTemperatureUnit(unit: TemperatureUnit)

    /** Normalizes [tabs] (see `AppConfigParser.normalize`) before saving. */
    fun saveTabs(tabs: TabPreferences)
}
