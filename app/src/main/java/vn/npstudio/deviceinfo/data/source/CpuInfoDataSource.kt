package vn.npstudio.deviceinfo.data.source

import android.content.Context
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.ThermalStatus
import javax.inject.Inject

/** Platform (non-sysfs) CPU values. Null means unavailable on this device or API level. */
interface CpuInfoDataSource {
    fun socManufacturer(): String?
    fun socModel(): String?
    fun hardware(): String?
    fun supportedAbis(): List<String>
    fun availableProcessors(): Int
    fun thermalStatus(): ThermalStatus?
    fun thermalHeadroom(): Float?
}

class CpuInfoDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CpuInfoDataSource {

    private val powerManager: PowerManager? get() = context.getSystemService(PowerManager::class.java)

    override fun socManufacturer(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER.orNullIfUnknown() else null

    override fun socModel(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL.orNullIfUnknown() else null

    override fun hardware(): String? = Build.HARDWARE.orNullIfUnknown()

    override fun supportedAbis(): List<String> = Build.SUPPORTED_ABIS.toList()

    override fun availableProcessors(): Int = Runtime.getRuntime().availableProcessors()

    override fun thermalStatus(): ThermalStatus? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return when (powerManager?.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
            else -> null
        }
    }

    override fun thermalHeadroom(): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return powerManager?.getThermalHeadroom(HEADROOM_FORECAST_SECONDS)?.takeUnless { it.isNaN() }
    }

    private fun String?.orNullIfUnknown(): String? =
        this?.trim()?.takeUnless { it.isEmpty() || it == Build.UNKNOWN }

    private companion object {
        const val HEADROOM_FORECAST_SECONDS = 10
    }
}
