package vn.npstudio.deviceinfo.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.CPUINFO_PATH
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.POSSIBLE_PATH
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.curFreqPath
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.maxFreqPath
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.minFreqPath
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser.onlinePath
import vn.npstudio.deviceinfo.domain.model.CpuCluster
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.testing.FakeCpuInfoDataSource
import vn.npstudio.deviceinfo.testing.FakeSysFsReader

class CpuRepositoryImplTest {

    private val dataSource = FakeCpuInfoDataSource()
    private val reader = FakeSysFsReader()

    private fun TestScope.createRepository() = CpuRepositoryImpl(
        cpuInfoDataSource = dataSource,
        sysFsReader = reader,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        pollingIntervals = PollingIntervals(cpuFrequencyMillis = 1_000L),
    )

    private fun octaCore() {
        reader.files[POSSIBLE_PATH] = "0-7"
        for (id in 0..7) {
            reader.files[minFreqPath(id)] = "300000"
            reader.files[maxFreqPath(id)] = if (id < 4) "1900800" else "2361600"
        }
    }

    @Test
    fun getCpuInfo_groupsClustersByMaxFrequency() = runTest {
        octaCore()

        val info = createRepository().getCpuInfo()

        assertEquals(8, info.cores.size)
        assertEquals(listOf(CpuCluster(4, 1_900_800L), CpuCluster(4, 2_361_600L)), info.clusters)
    }

    @Test
    fun getCpuInfo_fallsBackForCoresAndSocModel() = runTest {
        dataSource.processors = 4
        reader.files[CPUINFO_PATH] = "processor\t: 0\nHardware\t: Qualcomm MSM8998"

        val info = createRepository().getCpuInfo()

        assertEquals(listOf(0, 1, 2, 3), info.cores.map { it.id })
        assertEquals(emptyList<CpuCluster>(), info.clusters)
        assertEquals("Qualcomm MSM8998", info.socModel)
    }

    @Test
    fun getCpuInfo_usesBuildHardwareWhenCpuinfoHasNoHardware() = runTest {
        assertEquals("qcom", createRepository().getCpuInfo().socModel)
    }

    @Test
    fun observeFrequencies_marksOfflineCoresAndPollsEachInterval() = runTest {
        reader.files[POSSIBLE_PATH] = "0-1"
        reader.files[curFreqPath(0)] = "1100400"
        reader.files[curFreqPath(1)] = "1100400"
        reader.files[onlinePath(1)] = "0"

        createRepository().observeFrequencies().test {
            assertEquals(listOf(CpuCoreFrequency(0, 1_100_400L), CpuCoreFrequency(1, null)), awaitItem())

            reader.files[curFreqPath(0)] = "1900800"
            val second = awaitItem()

            assertEquals(1_000L, testScheduler.currentTime)
            assertEquals(CpuCoreFrequency(0, 1_900_800L), second.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFrequencies_emitsEmptyAndCompletesWhenUnreadable() = runTest {
        reader.files[POSSIBLE_PATH] = "0-7"

        createRepository().observeFrequencies().test {
            assertEquals(emptyList<CpuCoreFrequency>(), awaitItem())
            awaitComplete()
        }
    }
}
