package vn.npstudio.deviceinfo.feature.device

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.testing.FakeDeviceInfoRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo

class DeviceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_startsInitialThenShowsRows() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = DeviceViewModel(FakeDeviceInfoRepository())

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(DeviceRowMapper.map(sampleDeviceInfo), loaded.rows)
        }
    }
}
