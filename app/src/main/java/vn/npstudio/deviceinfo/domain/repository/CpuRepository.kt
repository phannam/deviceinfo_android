package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.CpuInfo

interface CpuRepository {
    suspend fun getCpuInfo(): CpuInfo

    /**
     * Polls current per-core frequencies. Emits an empty list and completes when no
     * frequency is readable on the first poll (for example, blocked by SELinux).
     */
    fun observeFrequencies(): Flow<List<CpuCoreFrequency>>
}
