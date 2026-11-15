package vn.npstudio.deviceinfo.domain.model

/** Default display values. Null fields mean the platform did not report a usable value. */
data class DisplayInfo(
    /** Native panel pixels from the active display mode. */
    val widthPx: Int?,
    val heightPx: Int?,
    val densityDpi: Int,
    val density: Float,
    val xdpi: Float,
    val ydpi: Float,
    val fontScale: Float,
    val refreshRateHz: Float?,
    /** Refresh rates offered at the native resolution, ascending. */
    val supportedRefreshRatesHz: List<Float>,
    /** Empty means no HDR support; null means unknown. */
    val hdrTypes: List<HdrType>?,
    val wideColorGamut: Boolean?,
)

enum class HdrType { DOLBY_VISION, HDR10, HLG, HDR10_PLUS }

/** Values that depend on the Activity window, read by the UI and passed down. */
data class WindowSnapshot(
    val widthPx: Int,
    val heightPx: Int,
    /** Null when window insets were not available yet. */
    val hasCutout: Boolean?,
)
