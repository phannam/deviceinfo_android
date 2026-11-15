package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSource
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import javax.inject.Inject

class DeviceInfoRepositoryImpl @Inject constructor(
    private val buildInfoDataSource: BuildInfoDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : DeviceInfoRepository {

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(ioDispatcher) {
        buildInfoDataSource.getDeviceInfo()
    }
}
