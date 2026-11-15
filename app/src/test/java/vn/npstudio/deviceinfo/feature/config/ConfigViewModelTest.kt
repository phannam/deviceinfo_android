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
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.ui.theme.ColorRole

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
            val initial = awaitItem()
            assertEquals(
                ConfigUiState(DarkMode.SYSTEM, TemperatureUnit.CELSIUS, AppColor.BRAND, AppColor.BRAND),
                initial,
            )

            viewModel.onDarkModeSelected(DarkMode.OFF)
            assertEquals(initial.copy(darkMode = DarkMode.OFF), awaitItem())

            viewModel.onTemperatureUnitSelected(TemperatureUnit.FAHRENHEIT)
            assertEquals(
                initial.copy(darkMode = DarkMode.OFF, temperatureUnit = TemperatureUnit.FAHRENHEIT),
                awaitItem(),
            )
        }
        assertEquals("OFF", source.values[AppConfigKeys.DARK_MODE])
        assertEquals("FAHRENHEIT", source.values[AppConfigKeys.TEMPERATURE_UNIT])
    }

    @Test
    fun onColorSelected_setsTheMatchingRole() = runTest(mainDispatcherRule.dispatcher) {
        val source = FakeAppConfigLocalDataSource()
        val viewModel = ConfigViewModel(AppConfigRepositoryImpl(source))

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onColorSelected(ColorRole.PRIMARY, AppColor.RED)
            assertEquals(AppColor.RED to AppColor.BRAND, awaitItem().let { it.primaryColor to it.accentColor })

            viewModel.onColorSelected(ColorRole.ACCENT, AppColor.TEAL)
            assertEquals(AppColor.RED to AppColor.TEAL, awaitItem().let { it.primaryColor to it.accentColor })
        }
        assertEquals("RED", source.values[AppConfigKeys.PRIMARY_COLOR])
        assertEquals("TEAL", source.values[AppConfigKeys.ACCENT_COLOR])
    }
}
