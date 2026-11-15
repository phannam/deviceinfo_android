package vn.npstudio.deviceinfo.feature.main

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.feature.device.DeviceRowMapper
import vn.npstudio.deviceinfo.feature.display.DisplayRowMapper
import vn.npstudio.deviceinfo.testing.FakeDeviceInfoRepository
import vn.npstudio.deviceinfo.testing.FakeDisplayRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo
import vn.npstudio.deviceinfo.testing.sampleDisplayInfo

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(handle: SavedStateHandle = SavedStateHandle()) =
        MainViewModel(handle, FakeDeviceInfoRepository(), FakeDisplayRepository())

    @Test
    fun selectedTab_restoresFromSavedState() {
        val handle = SavedStateHandle()
        createViewModel(handle).onTabSelected(MainTab.DISPLAY)

        assertEquals(MainTab.DISPLAY, createViewModel(handle).selectedTab.value)
    }

    @Test
    fun onShareClicked_emitsDeviceAndDisplaySections() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onShareClicked()

            val expected = MainUiEffect.Share(
                listOf(
                    ReportSection(MainTab.DEVICE.titleRes, DeviceRowMapper.map(sampleDeviceInfo)),
                    ReportSection(MainTab.DISPLAY.titleRes, DisplayRowMapper.map(sampleDisplayInfo)),
                ),
            )
            assertEquals(expected, awaitItem())
        }
    }
}
