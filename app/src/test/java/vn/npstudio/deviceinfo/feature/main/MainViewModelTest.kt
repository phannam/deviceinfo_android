package vn.npstudio.deviceinfo.feature.main

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.data.repository.AppConfigRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.ScreenTestResultRepositoryImpl
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.feature.battery.BatteryRowMapper
import vn.npstudio.deviceinfo.feature.cpu.CpuRowMapper
import vn.npstudio.deviceinfo.feature.device.DeviceRowMapper
import vn.npstudio.deviceinfo.feature.display.DisplayRowMapper
import vn.npstudio.deviceinfo.feature.memory.MemoryRowMapper
import vn.npstudio.deviceinfo.feature.screentest.ScreenTestRowMapper
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource
import vn.npstudio.deviceinfo.testing.FakeBatteryRepository
import vn.npstudio.deviceinfo.testing.FakeCpuRepository
import vn.npstudio.deviceinfo.testing.FakeDeviceInfoRepository
import vn.npstudio.deviceinfo.testing.FakeDisplayRepository
import vn.npstudio.deviceinfo.testing.FakeMemoryRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo
import vn.npstudio.deviceinfo.testing.sampleCpuInfo
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo
import vn.npstudio.deviceinfo.testing.sampleDisplayInfo
import vn.npstudio.deviceinfo.testing.sampleFrequencies
import vn.npstudio.deviceinfo.testing.sampleMemoryInfo
import vn.npstudio.deviceinfo.testing.sampleStorageInfo
import vn.npstudio.deviceinfo.testing.sampleWindowSnapshot

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        config: AppConfigRepositoryImpl = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource()),
    ) =
        MainViewModel(
            handle,
            FakeDeviceInfoRepository(),
            FakeCpuRepository(),
            FakeBatteryRepository(),
            FakeDisplayRepository(),
            FakeMemoryRepository(),
            ScreenTestResultRepositoryImpl(),
            config,
        )

    @Test
    fun selectedTab_restoresFromSavedState() {
        val handle = SavedStateHandle()
        createViewModel(handle).onTabSelected(MainTab.DISPLAY)

        assertEquals(MainTab.DISPLAY, createViewModel(handle).selectedTab.value)
    }

    @Test
    fun visibleTabs_followsTabPreferences() = runTest(mainDispatcherRule.dispatcher) {
        val config = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource())
        config.saveTabs(TabPreferences(order = TabId.entries.reversed(), hidden = setOf(TabId.CPU)))
        val viewModel = createViewModel(config = config)

        viewModel.visibleTabs.test {
            assertEquals(
                listOf(MainTab.SCREEN_TEST, MainTab.MEMORY, MainTab.DISPLAY, MainTab.BATTERY, MainTab.DEVICE),
                awaitItem(),
            )
            config.saveTabs(TabPreferences())
            assertEquals(MainTab.entries, awaitItem())
        }
    }

    @Test
    fun resolveSelectedTab_keepsVisibleSelection() {
        val viewModel = createViewModel()
        viewModel.onTabSelected(MainTab.DISPLAY)

        assertEquals(MainTab.DISPLAY, viewModel.resolveSelectedTab(listOf(MainTab.CPU, MainTab.DISPLAY)))
        assertEquals(MainTab.DISPLAY, viewModel.selectedTab.value)
    }

    @Test
    fun resolveSelectedTab_fallsBackToFirstVisibleTab() {
        val viewModel = createViewModel()
        viewModel.onTabSelected(MainTab.DISPLAY)

        assertEquals(MainTab.MEMORY, viewModel.resolveSelectedTab(listOf(MainTab.MEMORY, MainTab.CPU)))
        assertEquals(MainTab.MEMORY, viewModel.selectedTab.value)
    }

    @Test
    fun onShareClicked_ignoresTabPreferences() = runTest(mainDispatcherRule.dispatcher) {
        val config = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource())
        config.saveTabs(TabPreferences(order = TabId.entries.reversed(), hidden = setOf(TabId.CPU)))
        val viewModel = createViewModel(config = config)

        viewModel.effects.test {
            viewModel.onShareClicked(sampleWindowSnapshot)

            val sections = (awaitItem() as MainUiEffect.Share).sections
            assertEquals(MainTab.entries.map { it.titleRes }, sections.map { it.title })
        }
    }

    @Test
    fun onShareClicked_emitsAllSections() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onShareClicked(sampleWindowSnapshot)

            val expected = MainUiEffect.Share(
                listOf(
                    ReportSection(MainTab.DEVICE.titleRes, DeviceRowMapper.map(sampleDeviceInfo)),
                    ReportSection(MainTab.CPU.titleRes, CpuRowMapper.map(sampleCpuInfo, sampleFrequencies)),
                    ReportSection(MainTab.BATTERY.titleRes, BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.CELSIUS)),
                    ReportSection(MainTab.DISPLAY.titleRes, DisplayRowMapper.map(sampleDisplayInfo, sampleWindowSnapshot)),
                    ReportSection(MainTab.MEMORY.titleRes, MemoryRowMapper.map(sampleMemoryInfo, sampleStorageInfo)),
                    ReportSection(MainTab.SCREEN_TEST.titleRes, ScreenTestRowMapper.map(null)),
                ),
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun onShareClicked_usesTemperatureUnit() = runTest(mainDispatcherRule.dispatcher) {
        val config = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource())
        config.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        val viewModel = createViewModel(config = config)

        viewModel.effects.test {
            viewModel.onShareClicked(sampleWindowSnapshot)

            val sections = (awaitItem() as MainUiEffect.Share).sections
            val battery = sections.single { it.title == MainTab.BATTERY.titleRes }
            assertEquals(BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.FAHRENHEIT), battery.rows)
        }
    }
}
