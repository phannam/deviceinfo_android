package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.StorageInfo

interface MemoryRepository {
    /** Polls RAM on the IO dispatcher. Never completes. */
    fun observeRam(): Flow<MemoryInfo>

    suspend fun getStorageInfo(): StorageInfo
}
