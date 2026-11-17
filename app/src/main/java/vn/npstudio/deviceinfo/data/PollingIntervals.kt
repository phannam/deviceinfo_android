package vn.npstudio.deviceinfo.data

/** Ticker intervals for live data. Provided by Hilt so tests can override them. */
data class PollingIntervals(
    val cpuFrequencyMillis: Long = 1_000L,
    val batteryPropertiesMillis: Long = 2_000L,
    val memoryMillis: Long = 2_000L,
    val uptimeMillis: Long = 1_000L,
)
