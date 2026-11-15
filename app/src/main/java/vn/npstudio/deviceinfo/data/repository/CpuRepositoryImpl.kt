package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.data.source.CpuInfoDataSource
import vn.npstudio.deviceinfo.data.source.CpuSysFsParser
import vn.npstudio.deviceinfo.data.source.SysFsReader
import vn.npstudio.deviceinfo.domain.model.CpuCluster
import vn.npstudio.deviceinfo.domain.model.CpuCore
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.CpuInfo
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import javax.inject.Inject

class CpuRepositoryImpl @Inject constructor(
    private val cpuInfoDataSource: CpuInfoDataSource,
    private val sysFsReader: SysFsReader,
    private val ioDispatcher: CoroutineDispatcher,
    private val pollingIntervals: PollingIntervals,
) : CpuRepository {

    override suspend fun getCpuInfo(): CpuInfo = withContext(ioDispatcher) {
        val cores = coreIds().map { id ->
            CpuCore(
                id = id,
                minFreqKhz = CpuSysFsParser.parseKhz(sysFsReader.read(CpuSysFsParser.minFreqPath(id))),
                maxFreqKhz = CpuSysFsParser.parseKhz(sysFsReader.read(CpuSysFsParser.maxFreqPath(id))),
            )
        }
        CpuInfo(
            socManufacturer = cpuInfoDataSource.socManufacturer(),
            socModel = cpuInfoDataSource.socModel()
                ?: CpuSysFsParser.parseHardware(sysFsReader.read(CpuSysFsParser.CPUINFO_PATH))
                ?: cpuInfoDataSource.hardware(),
            supportedAbis = cpuInfoDataSource.supportedAbis(),
            cores = cores,
            clusters = clusters(cores),
            governors = cores.mapNotNull { sysFsReader.read(CpuSysFsParser.governorPath(it.id)) }.distinct(),
            thermalStatus = cpuInfoDataSource.thermalStatus(),
            thermalHeadroom = cpuInfoDataSource.thermalHeadroom(),
        )
    }

    override fun observeFrequencies(): Flow<List<CpuCoreFrequency>> = flow {
        val ids = coreIds()
        var firstPoll = true
        while (true) {
            val frequencies = ids.map { CpuCoreFrequency(it, readCurrentKhz(it)) }
            if (firstPoll && frequencies.all { it.currentKhz == null }) {
                emit(emptyList())
                return@flow
            }
            firstPoll = false
            emit(frequencies)
            delay(pollingIntervals.cpuFrequencyMillis)
        }
    }.flowOn(ioDispatcher)

    /** `possible` also lists offline cores; fall back to the runtime count. */
    private fun coreIds(): List<Int> =
        CpuSysFsParser.parseCpuList(sysFsReader.read(CpuSysFsParser.POSSIBLE_PATH))
            .ifEmpty { (0 until cpuInfoDataSource.availableProcessors()).toList() }

    private fun readCurrentKhz(core: Int): Long? {
        if (sysFsReader.read(CpuSysFsParser.onlinePath(core)) == "0") return null
        return CpuSysFsParser.parseKhz(sysFsReader.read(CpuSysFsParser.curFreqPath(core)))
    }

    private fun clusters(cores: List<CpuCore>): List<CpuCluster> = cores
        .mapNotNull { it.maxFreqKhz }
        .groupingBy { it }
        .eachCount()
        .map { (maxKhz, count) -> CpuCluster(count, maxKhz) }
        .sortedBy { it.maxFreqKhz }
}
