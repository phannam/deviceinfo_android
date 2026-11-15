package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.data.source.BatteryDataSource
import vn.npstudio.deviceinfo.data.source.BatteryParser
import vn.npstudio.deviceinfo.data.source.BatteryProperties
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    private val batteryDataSource: BatteryDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val pollingIntervals: PollingIntervals,
) : BatteryRepository {

    override fun observe(): Flow<BatteryInfo> =
        combine(batteryDataSource.batteryChanges(), properties(), BatteryParser::parse)
            .distinctUntilChanged()
            .flowOn(ioDispatcher)

    private fun properties(): Flow<BatteryProperties> = flow {
        while (true) {
            emit(batteryDataSource.readProperties())
            delay(pollingIntervals.batteryPropertiesMillis)
        }
    }
}
