package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.npstudio.deviceinfo.domain.model.SystemInfo

interface SystemInfoRepository {
    suspend fun getSystemInfo(): SystemInfo

    /** Time since boot in milliseconds, emitted once per polling interval. */
    fun observeUptime(): Flow<Long>
}
