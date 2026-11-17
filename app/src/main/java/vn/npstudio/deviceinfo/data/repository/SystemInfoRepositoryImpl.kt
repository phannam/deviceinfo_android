package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.PollingIntervals
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSource
import vn.npstudio.deviceinfo.data.source.FeatureDataSource
import vn.npstudio.deviceinfo.data.source.MediaDataSource
import vn.npstudio.deviceinfo.data.source.PlatformDataSource
import vn.npstudio.deviceinfo.data.source.RuntimeDataSource
import vn.npstudio.deviceinfo.data.source.SystemPropertiesParser
import vn.npstudio.deviceinfo.domain.model.SystemInfo
import vn.npstudio.deviceinfo.domain.repository.SystemInfoRepository
import javax.inject.Inject

class SystemInfoRepositoryImpl @Inject constructor(
    private val buildInfoDataSource: BuildInfoDataSource,
    private val platformDataSource: PlatformDataSource,
    private val featureDataSource: FeatureDataSource,
    private val runtimeDataSource: RuntimeDataSource,
    private val mediaDataSource: MediaDataSource,
    private val pollingIntervals: PollingIntervals,
    private val ioDispatcher: CoroutineDispatcher,
) : SystemInfoRepository {

    override suspend fun getSystemInfo(): SystemInfo = withContext(ioDispatcher) {
        val props = platformDataSource.systemProperties()
        val build = buildInfoDataSource.androidBuild()
        SystemInfo(
            build = build.copy(baseband = build.baseband ?: props[SystemPropertiesParser.KEY_BASEBAND]?.ifBlank { null }),
            security = SystemPropertiesParser.security(
                props = props,
                rooted = platformDataSource.suBinaryFound(),
                selinux = platformDataSource.selinuxMode(),
                apiLevel = build.apiLevel,
            ),
            systemFeatures = featureDataSource.systemFeatures(),
            runtime = runtimeDataSource.runtime(),
            region = runtimeDataSource.region(),
            drm = mediaDataSource.widevine(),
        )
    }

    override fun observeUptime(): Flow<Long> = flow {
        while (true) {
            emit(buildInfoDataSource.uptimeMillis())
            delay(pollingIntervals.uptimeMillis)
        }
    }
}
