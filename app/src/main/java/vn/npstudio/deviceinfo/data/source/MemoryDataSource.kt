package vn.npstudio.deviceinfo.data.source

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.StorageInfo
import java.io.IOException
import javax.inject.Inject

interface MemoryDataSource {
    fun readRam(): MemoryInfo

    /** Blocking; call on the IO dispatcher. */
    fun readStorage(): StorageInfo
}

class MemoryDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MemoryDataSource {

    override fun readRam(): MemoryInfo {
        val activityManager = context.getSystemService(ActivityManager::class.java)
            ?: return MemoryInfo(null, null, null, null)
        val info = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        return MemoryInfo(
            totalBytes = info.totalMem.takeIf { it > 0 },
            availableBytes = info.availMem.takeIf { it >= 0 },
            lowMemoryThresholdBytes = info.threshold.takeIf { it > 0 },
            lowMemory = info.lowMemory,
        )
    }

    // StorageStatsManager needs no permission for the default volume and matches Settings > Storage.
    override fun readStorage(): StorageInfo {
        val stats = context.getSystemService(StorageStatsManager::class.java)
            ?: return StorageInfo(null, null)
        return StorageInfo(
            totalBytes = readBytes { stats.getTotalBytes(StorageManager.UUID_DEFAULT) }?.takeIf { it > 0 },
            freeBytes = readBytes { stats.getFreeBytes(StorageManager.UUID_DEFAULT) }?.takeIf { it >= 0 },
        )
    }

    private inline fun readBytes(block: () -> Long): Long? = try {
        block()
    } catch (_: IOException) {
        null
    }
}
