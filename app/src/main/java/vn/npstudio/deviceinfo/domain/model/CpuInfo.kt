package vn.npstudio.deviceinfo.domain.model

/** Static CPU information. Null fields mean the platform did not report a usable value. */
data class CpuInfo(
    val socManufacturer: String?,
    val socModel: String?,
    val supportedAbis: List<String>,
    val cores: List<CpuCore>,
    /** Cores grouped by max frequency, sorted by frequency ascending. */
    val clusters: List<CpuCluster>,
    val governors: List<String>,
    val thermalStatus: ThermalStatus?,
    /** 0.0 means no throttling expected, 1.0 means severe throttling. */
    val thermalHeadroom: Float?,
)

data class CpuCore(
    val id: Int,
    val minFreqKhz: Long?,
    val maxFreqKhz: Long?,
)

data class CpuCluster(
    val coreCount: Int,
    val maxFreqKhz: Long,
)

/** [currentKhz] is null when the core is offline or its frequency is unreadable. */
data class CpuCoreFrequency(
    val coreId: Int,
    val currentKhz: Long?,
)
