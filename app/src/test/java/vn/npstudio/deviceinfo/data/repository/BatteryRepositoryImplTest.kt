package vn.npstudio.deviceinfo.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.testing.FakeBatteryDataSource
import vn.npstudio.deviceinfo.testing.sampleBatteryBroadcast
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo
import vn.npstudio.deviceinfo.testing.sampleBatteryProperties

class BatteryRepositoryImplTest {

    private val dataSource = FakeBatteryDataSource()

    private fun TestScope.createRepository() = BatteryRepositoryImpl(
        batteryDataSource = dataSource,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        pollingIntervals = PollingIntervals(batteryPropertiesMillis = 2_000L),
    )

    @Test
    fun observe_combinesBroadcastAndPolledProperties() = runTest {
        dataSource.broadcasts.emit(sampleBatteryBroadcast)

        createRepository().observe().test {
            assertEquals(sampleBatteryInfo, awaitItem())

            dataSource.properties = sampleBatteryProperties.copy(currentNow = -300_000)
            val polled = awaitItem()
            assertEquals(2_000L, testScheduler.currentTime)
            assertEquals(-300_000, polled.currentNowMicroamps)

            dataSource.broadcasts.emit(sampleBatteryBroadcast.copy(level = 81))
            assertEquals(81, awaitItem().levelPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observe_skipsUnchangedPolls() = runTest {
        dataSource.broadcasts.emit(sampleBatteryBroadcast)

        createRepository().observe().test {
            awaitItem()
            testScheduler.advanceTimeBy(6_001L)
            expectNoEvents()
            assertEquals(4, dataSource.propertyReads)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
