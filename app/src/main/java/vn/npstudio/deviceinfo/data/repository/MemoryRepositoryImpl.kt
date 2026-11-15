package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.data.source.MemoryDataSource
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.StorageInfo
import vn.npstudio.deviceinfo.domain.repository.MemoryRepository
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val memoryDataSource: MemoryDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val pollingIntervals: PollingIntervals,
) : MemoryRepository {

    override fun observeRam(): Flow<MemoryInfo> = flow {
        while (true) {
            emit(memoryDataSource.readRam())
            delay(pollingIntervals.memoryMillis)
        }
    }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    override suspend fun getStorageInfo(): StorageInfo = withContext(ioDispatcher) {
        memoryDataSource.readStorage()
    }
}
