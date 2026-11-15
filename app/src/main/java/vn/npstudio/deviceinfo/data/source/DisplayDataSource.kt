package vn.npstudio.deviceinfo.data.source

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.model.HdrType
import javax.inject.Inject

interface DisplayDataSource {
    fun getDisplayInfo(): DisplayInfo
}

class DisplayDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DisplayDataSource {

    override fun getDisplayInfo(): DisplayInfo {
        val display = context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        val metrics = context.resources.displayMetrics
        return DisplayInfo(
            widthPx = mode?.physicalWidth,
            heightPx = mode?.physicalHeight,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            xdpi = metrics.xdpi,
            ydpi = metrics.ydpi,
            fontScale = context.resources.configuration.fontScale,
            refreshRateHz = mode?.refreshRate?.takeIf { it > 0f },
            supportedRefreshRatesHz = display?.supportedModes.orEmpty()
                .filter { mode != null && it.physicalWidth == mode.physicalWidth && it.physicalHeight == mode.physicalHeight }
                .map { it.refreshRate }
                .distinct()
                .sorted(),
            hdrTypes = display?.let(::hdrTypes),
            wideColorGamut = display?.isWideColorGamut,
        )
    }

    private fun hdrTypes(display: Display): List<HdrType>? {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            display.mode.supportedHdrTypes
        } else {
            @Suppress("DEPRECATION")
            display.hdrCapabilities?.supportedHdrTypes
        } ?: return null
        return raw.toList().mapNotNull(::hdrType).distinct()
    }

    private fun hdrType(raw: Int): HdrType? = when (raw) {
        Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> HdrType.DOLBY_VISION
        Display.HdrCapabilities.HDR_TYPE_HDR10 -> HdrType.HDR10
        Display.HdrCapabilities.HDR_TYPE_HLG -> HdrType.HLG
        HDR_TYPE_HDR10_PLUS -> HdrType.HDR10_PLUS
        else -> null
    }

    private companion object {
        /** `Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS`, added in API 29. */
        const val HDR_TYPE_HDR10_PLUS = 4
    }
}
