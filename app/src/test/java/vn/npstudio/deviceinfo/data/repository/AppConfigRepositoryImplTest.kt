package vn.npstudio.deviceinfo.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.data.source.AppConfigKeys
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.AppConfig
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource

class AppConfigRepositoryImplTest {

    private val dataSource = FakeAppConfigLocalDataSource()

    @Test
    fun emptyStorage_usesDefaults() {
        val repository = AppConfigRepositoryImpl(dataSource)

        assertEquals(AppConfig(), repository.config.value)
        assertEquals(TabPreferences(), repository.tabs.value)
    }

    @Test
    fun storedValues_areLoadedOnCreation() {
        dataSource.values += mapOf(
            AppConfigKeys.DARK_MODE to "ON",
            AppConfigKeys.PRIMARY_COLOR to "RED",
            AppConfigKeys.ACCENT_COLOR to "NOT_A_COLOR",
            AppConfigKeys.TEMPERATURE_UNIT to "FAHRENHEIT",
            AppConfigKeys.TAB_HIDDEN to "CPU",
        )

        val repository = AppConfigRepositoryImpl(dataSource)

        assertEquals(
            AppConfig(DarkMode.ON, AppColor.RED, AppColor.BRAND, TemperatureUnit.FAHRENHEIT),
            repository.config.value,
        )
        assertEquals(setOf(TabId.CPU), repository.tabs.value.hidden)
    }

    @Test
    fun setters_updateStateAndPersist() {
        val repository = AppConfigRepositoryImpl(dataSource)

        repository.setDarkMode(DarkMode.OFF)
        repository.setPrimaryColor(AppColor.INDIGO)
        repository.setAccentColor(AppColor.AMBER)
        repository.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)

        val expected = AppConfig(DarkMode.OFF, AppColor.INDIGO, AppColor.AMBER, TemperatureUnit.FAHRENHEIT)
        assertEquals(expected, repository.config.value)
        assertEquals(expected, AppConfigRepositoryImpl(dataSource).config.value)
    }

    @Test
    fun saveTabs_normalizesAndPersists() {
        val repository = AppConfigRepositoryImpl(dataSource)

        repository.saveTabs(
            TabPreferences(order = listOf(TabId.MEMORY, TabId.MEMORY, TabId.DEVICE), hidden = setOf(TabId.DEVICE)),
        )

        val expected = TabPreferences(
            order = listOf(TabId.MEMORY, TabId.DEVICE, TabId.CPU, TabId.BATTERY, TabId.DISPLAY, TabId.SCREEN_TEST),
            hidden = setOf(TabId.DEVICE),
        )
        assertEquals(expected, repository.tabs.value)
        assertEquals(expected, AppConfigRepositoryImpl(dataSource).tabs.value)
        assertEquals(listOf(TabId.MEMORY, TabId.CPU, TabId.BATTERY, TabId.DISPLAY, TabId.SCREEN_TEST), expected.visible)
    }
}
