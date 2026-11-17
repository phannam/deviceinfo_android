package vn.npstudio.deviceinfo.feature.device

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import vn.npstudio.deviceinfo.testing.FakeDeviceInfoRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo

class DeviceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_startsInitialThenShowsSections() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = DeviceViewModel(FakeDeviceInfoRepository())

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(DeviceRowMapper.sections(sampleDeviceInfo), loaded.sections)
            assertEquals(DeviceRowMapper.hero(sampleDeviceInfo), loaded.hero)
        }
    }

    @Test
    fun refresh_readsAgain() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeDeviceInfoRepository()
        val viewModel = DeviceViewModel(repository)

        viewModel.uiState.test {
            skipItems(2)
            val granted = sampleDeviceInfo.copy(
                telephony = sampleDeviceInfo.telephony.copy(networkType = GuardedValue.Available("LTE")),
            )
            repository.info = granted
            viewModel.refresh()
            assertEquals(DeviceRowMapper.sections(granted), awaitItem().sections)
        }
    }

    @Test
    fun onResumed_reloadsOnlyWhenPermissionChanged() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeDeviceInfoRepository()
        val viewModel = DeviceViewModel(repository)

        viewModel.uiState.test {
            skipItems(2)
            viewModel.onResumed()
            assertEquals(1, repository.loadCount)

            repository.phonePermissionGranted = true
            repository.info = sampleDeviceInfo.copy(
                telephony = sampleDeviceInfo.telephony.copy(networkType = GuardedValue.Available("NR (5G)")),
            )
            viewModel.onResumed()
            assertEquals(DeviceRowMapper.sections(repository.info), awaitItem().sections)
            assertEquals(2, repository.loadCount)
        }
    }
}
