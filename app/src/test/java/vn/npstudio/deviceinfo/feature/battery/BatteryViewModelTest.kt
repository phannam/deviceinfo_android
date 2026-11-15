package vn.npstudio.deviceinfo.feature.battery

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.data.repository.AppConfigRepositoryImpl
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource
import vn.npstudio.deviceinfo.testing.FakeBatteryRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo

class BatteryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(updates: MutableSharedFlow<BatteryInfo>) =
        BatteryViewModel(FakeBatteryRepository(updates), AppConfigRepositoryImpl(FakeAppConfigLocalDataSource()))

    @Test
    fun uiState_followsLiveBatteryUpdates() = runTest(mainDispatcherRule.dispatcher) {
        val updates = MutableSharedFlow<BatteryInfo>(replay = 1)
        val viewModel = createViewModel(updates)

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)

            updates.emit(sampleBatteryInfo)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.CELSIUS), loaded.rows)

            val unplugged = sampleBatteryInfo.copy(levelPercent = 79)
            updates.emit(unplugged)
            assertEquals(BatteryRowMapper.map(unplugged, TemperatureUnit.CELSIUS), awaitItem().rows)
        }
    }

    @Test
    fun uiState_remapsWhenTemperatureUnitChanges() = runTest(mainDispatcherRule.dispatcher) {
        val config = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource())
        val viewModel = BatteryViewModel(FakeBatteryRepository(), config)

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            assertEquals(BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.CELSIUS), awaitItem().rows)

            config.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
            assertEquals(BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.FAHRENHEIT), awaitItem().rows)
        }
    }

    @Test
    fun uiState_stopsUpstreamFiveSecondsAfterLastSubscriber() = runTest(mainDispatcherRule.dispatcher) {
        val updates = MutableSharedFlow<BatteryInfo>(replay = 1)
        updates.emit(sampleBatteryInfo)
        val viewModel = createViewModel(updates)

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
