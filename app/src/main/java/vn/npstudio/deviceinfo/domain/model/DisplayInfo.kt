package vn.npstudio.deviceinfo.domain.model

/** Null fields mean the platform did not report a usable value. */
data class DisplayInfo(
    val widthPx: Int?,
    val heightPx: Int?,
    val densityDpi: Int,
    val density: Float,
    val xdpi: Float,
    val ydpi: Float,
    val fontScale: Float,
)
