package vn.npstudio.deviceinfo.feature.config

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.data.repository.AppConfigRepositoryImpl
import vn.npstudio.deviceinfo.data.source.AppConfigKeys
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource
import vn.npstudio.deviceinfo.testing.MainDispatcherRule

class ConfigViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_reflectsStoredConfig() {
        val source = FakeAppConfigLocalDataSource(
            mutableMapOf(AppConfigKeys.DARK_MODE to "ON", AppConfigKeys.TEMPERATURE_UNIT to "FAHRENHEIT"),
        )
        val state = ConfigViewModel(AppConfigRepositoryImpl(source)).uiState.value

        assertEquals(DarkMode.ON, state.darkMode)
        assertEquals(UiText.res(R.string.config_dark_mode_on), state.darkModeLabel)
        assertEquals(UiText.res(R.string.config_temperature_fahrenheit), state.temperatureUnitLabel)
    }

    @Test
    fun selections_updateStateAndPersist() = runTest(mainDispatcherRule.dispatcher) {
        val source = FakeAppConfigLocalDataSource()
        val viewModel = ConfigViewModel(AppConfigRepositoryImpl(source))

        viewModel.uiState.test {
            assertEquals(ConfigUiState(DarkMode.SYSTEM, TemperatureUnit.CELSIUS), awaitItem())

            viewModel.onDarkModeSelected(DarkMode.OFF)
            assertEquals(ConfigUiState(DarkMode.OFF, TemperatureUnit.CELSIUS), awaitItem())

            viewModel.onTemperatureUnitSelected(TemperatureUnit.FAHRENHEIT)
            assertEquals(ConfigUiState(DarkMode.OFF, TemperatureUnit.FAHRENHEIT), awaitItem())
        }
        assertEquals("OFF", source.values[AppConfigKeys.DARK_MODE])
        assertEquals("FAHRENHEIT", source.values[AppConfigKeys.TEMPERATURE_UNIT])
    }
}
