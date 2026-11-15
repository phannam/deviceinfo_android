package vn.npstudio.deviceinfo.data

/** Ticker intervals for live data. Provided by Hilt so tests can override them. */
data class PollingIntervals(
    val cpuFrequencyMillis: Long = 1_000L,
)
