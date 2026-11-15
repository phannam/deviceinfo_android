package vn.npstudio.deviceinfo.data.source

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import javax.inject.Inject

interface DisplayDataSource {
    fun getDisplayInfo(): DisplayInfo
}

class DisplayDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DisplayDataSource {

    override fun getDisplayInfo(): DisplayInfo {
        val mode = context.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
            ?.mode
        val metrics = context.resources.displayMetrics
        return DisplayInfo(
            widthPx = mode?.physicalWidth,
            heightPx = mode?.physicalHeight,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            xdpi = metrics.xdpi,
            ydpi = metrics.ydpi,
            fontScale = context.resources.configuration.fontScale,
        )
    }
}
