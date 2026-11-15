package vn.npstudio.deviceinfo.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

class AppManager private constructor(private val context: Context) {

    var deviceInfoList: List<Item> = emptyList()
        private set

    var displayInfoList: List<Item> = emptyList()
        private set

    fun getAllInfos(): String = buildString {
        append("Device\n")
        deviceInfoList.forEach { append("\t${it.name}: ${it.value}\n") }
        append("\n")
        append("Display\n")
        displayInfoList.forEach { append("\t${it.name}: ${it.value}\n") }
        append("\n")
    }

    private fun initializeInstance() {
        initializeGeneralDeviceInfo()
        initializeDisplayInfo()
    }

    private fun initializeGeneralDeviceInfo() {
        deviceInfoList = listOf(
            Item("Product", Build.PRODUCT),
            Item("Device", Build.DEVICE),
            Item("Board", Build.BOARD),
            Item("Manufacturer", Build.MANUFACTURER),
            Item("Brand", Build.BRAND),
            Item("Model", Build.MODEL),
        )
    }

    @Suppress("DEPRECATION")
    private fun initializeDisplayInfo() {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        displayInfoList = listOf(
            Item("Display", Build.DISPLAY),
            Item("Width Pixels", metrics.widthPixels.toString()),
            Item("Height Pixels", metrics.heightPixels.toString()),
            Item("Density", metrics.density.toString()),
            Item("DensityDpi", metrics.densityDpi.toString()),
            Item("Scaled Density", metrics.scaledDensity.toString()),
            Item("x dpi", metrics.xdpi.toString()),
            Item("y dpi", metrics.ydpi.toString()),
            Item("Default Density", DisplayMetrics.DENSITY_DEFAULT.toString()),
            Item("Low Density", DisplayMetrics.DENSITY_LOW.toString()),
            Item("Medium Density", DisplayMetrics.DENSITY_MEDIUM.toString()),
            Item("High Density", DisplayMetrics.DENSITY_HIGH.toString()),
        )
    }

    companion object {
        private var instance: AppManager? = null

        @Synchronized
        fun create(context: Context) {
            val manager = instance ?: AppManager(context).also { instance = it }
            manager.initializeInstance()
        }

        fun getInstance(): AppManager =
            checkNotNull(instance) { "AppManager.create() must be called first" }
    }
}
