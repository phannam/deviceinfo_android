package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.npstudio.deviceinfo.data.source.AppConfigKeys
import vn.npstudio.deviceinfo.data.source.AppConfigLocalDataSource
import vn.npstudio.deviceinfo.data.source.AppConfigParser
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.AppConfig
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Reads everything once on creation (main thread, small file; see spec 002 section 5.2). */
@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val dataSource: AppConfigLocalDataSource,
) : AppConfigRepository {

    private val _config = MutableStateFlow(
        AppConfig().let { defaults ->
            AppConfig(
                darkMode = read(AppConfigKeys.DARK_MODE, defaults.darkMode),
                primaryColor = read(AppConfigKeys.PRIMARY_COLOR, defaults.primaryColor),
                accentColor = read(AppConfigKeys.ACCENT_COLOR, defaults.accentColor),
                temperatureUnit = read(AppConfigKeys.TEMPERATURE_UNIT, defaults.temperatureUnit),
            )
        },
    )
    override val config: StateFlow<AppConfig> = _config.asStateFlow()

    private val _tabs = MutableStateFlow(
        AppConfigParser.parseTabs(
            order = dataSource.getString(AppConfigKeys.TAB_ORDER),
            hidden = dataSource.getString(AppConfigKeys.TAB_HIDDEN),
        ),
    )
    override val tabs: StateFlow<TabPreferences> = _tabs.asStateFlow()

    override fun setDarkMode(mode: DarkMode) {
        _config.update { it.copy(darkMode = mode) }
        dataSource.putString(AppConfigKeys.DARK_MODE, mode.name)
    }

    override fun setPrimaryColor(color: AppColor) {
        _config.update { it.copy(primaryColor = color) }
        dataSource.putString(AppConfigKeys.PRIMARY_COLOR, color.name)
    }

    override fun setAccentColor(color: AppColor) {
        _config.update { it.copy(accentColor = color) }
        dataSource.putString(AppConfigKeys.ACCENT_COLOR, color.name)
    }

    override fun setTemperatureUnit(unit: TemperatureUnit) {
        _config.update { it.copy(temperatureUnit = unit) }
        dataSource.putString(AppConfigKeys.TEMPERATURE_UNIT, unit.name)
    }

    override fun saveTabs(tabs: TabPreferences) {
        val normalized = AppConfigParser.normalize(tabs)
        _tabs.value = normalized
        dataSource.putString(AppConfigKeys.TAB_ORDER, AppConfigParser.joinTabIds(normalized.order))
        dataSource.putString(AppConfigKeys.TAB_HIDDEN, AppConfigParser.joinTabIds(normalized.hidden))
    }

    private inline fun <reified T : Enum<T>> read(key: String, default: T): T =
        AppConfigParser.enumOrDefault(dataSource.getString(key), default)
}
