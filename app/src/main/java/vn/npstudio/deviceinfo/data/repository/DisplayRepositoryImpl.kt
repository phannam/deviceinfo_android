package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.source.DisplayDataSource
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import javax.inject.Inject

class DisplayRepositoryImpl @Inject constructor(
    private val displayDataSource: DisplayDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : DisplayRepository {

    override suspend fun getDisplayInfo(): DisplayInfo = withContext(ioDispatcher) {
        displayDataSource.getDisplayInfo()
    }
}
