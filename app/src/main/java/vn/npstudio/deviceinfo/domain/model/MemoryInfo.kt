package vn.npstudio.deviceinfo.domain.model

/** RAM from `ActivityManager.MemoryInfo`. Null means the value is unavailable. */
data class MemoryInfo(
    val totalBytes: Long?,
    val availableBytes: Long?,
    val lowMemoryThresholdBytes: Long?,
    val lowMemory: Boolean?,
)

/** Internal (primary) storage volume from `StorageStatsManager`. Null means unavailable. */
data class StorageInfo(
    val totalBytes: Long?,
    val freeBytes: Long?,
)
