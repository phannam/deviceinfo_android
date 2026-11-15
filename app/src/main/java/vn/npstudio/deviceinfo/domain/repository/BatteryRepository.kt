package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.npstudio.deviceinfo.domain.model.BatteryInfo

interface BatteryRepository {
    /** Emits on every battery broadcast and on every property poll. Never completes. */
    fun observe(): Flow<BatteryInfo>
}
