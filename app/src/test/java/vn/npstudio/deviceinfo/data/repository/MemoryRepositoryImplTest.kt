package vn.npstudio.deviceinfo.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.testing.FakeMemoryDataSource
import vn.npstudio.deviceinfo.testing.sampleMemoryInfo
import vn.npstudio.deviceinfo.testing.sampleStorageInfo

class MemoryRepositoryImplTest {

    private val dataSource = FakeMemoryDataSource()

    private fun TestScope.createRepository() = MemoryRepositoryImpl(
        memoryDataSource = dataSource,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        pollingIntervals = PollingIntervals(memoryMillis = 2_000L),
    )

    @Test
    fun observeRam_pollsAndSkipsUnchangedValues() = runTest {
        createRepository().observeRam().test {
            assertEquals(sampleMemoryInfo, awaitItem())

            testScheduler.advanceTimeBy(2_001L)
            expectNoEvents()
            assertEquals(2, dataSource.ramReads)

            val changed = sampleMemoryInfo.copy(availableBytes = 900_000_000L)
            dataSource.ram = changed
            assertEquals(changed, awaitItem())
            assertEquals(4_000L, testScheduler.currentTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getStorageInfo_readsDataSource() = runTest {
        assertEquals(sampleStorageInfo, createRepository().getStorageInfo())
    }
}
