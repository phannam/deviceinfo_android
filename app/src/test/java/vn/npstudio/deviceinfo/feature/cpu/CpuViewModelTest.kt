package vn.npstudio.deviceinfo.feature.cpu

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.testing.FakeCpuRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleCpuInfo

class CpuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_startsInitialThenFollowsLiveFrequencies() = runTest(mainDispatcherRule.dispatcher) {
        val frequencies = MutableSharedFlow<List<CpuCoreFrequency>>(replay = 1)
        val viewModel = CpuViewModel(FakeCpuRepository(frequencies = frequencies))

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)

            val first = listOf(CpuCoreFrequency(0, 300_000L))
            frequencies.emit(first)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(CpuRowMapper.map(sampleCpuInfo, first), loaded.rows)

            val second = listOf(CpuCoreFrequency(0, 1_900_800L))
            frequencies.emit(second)
            assertEquals(CpuRowMapper.map(sampleCpuInfo, second), awaitItem().rows)
        }
    }
}
