package vn.npstudio.deviceinfo.feature.battery

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.testing.FakeBatteryRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo

class BatteryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_followsLiveBatteryUpdates() = runTest(mainDispatcherRule.dispatcher) {
        val updates = MutableSharedFlow<BatteryInfo>(replay = 1)
        val viewModel = BatteryViewModel(FakeBatteryRepository(updates))

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)

            updates.emit(sampleBatteryInfo)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(BatteryRowMapper.map(sampleBatteryInfo), loaded.rows)

            val unplugged = sampleBatteryInfo.copy(levelPercent = 79)
            updates.emit(unplugged)
            assertEquals(BatteryRowMapper.map(unplugged), awaitItem().rows)
        }
    }

    @Test
    fun uiState_stopsUpstreamFiveSecondsAfterLastSubscriber() = runTest(mainDispatcherRule.dispatcher) {
        val updates = MutableSharedFlow<BatteryInfo>(replay = 1)
        updates.emit(sampleBatteryInfo)
        val viewModel = BatteryViewModel(FakeBatteryRepository(updates))

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            assertEquals(LoadingMode.IDLE, awaitItem().loadingMode)
            assertEquals(1, updates.subscriptionCount.value)
            cancelAndIgnoreRemainingEvents()
        }
        testScheduler.advanceTimeBy(5_001L)
        testScheduler.runCurrent()

        assertEquals(0, updates.subscriptionCount.value)
    }
}
