package vn.npstudio.deviceinfo.data.source

import android.os.Build
import android.os.SystemClock
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import javax.inject.Inject

interface BuildInfoDataSource {
    fun getDeviceInfo(): DeviceInfo
}

class BuildInfoDataSourceImpl @Inject constructor() : BuildInfoDataSource {

    override fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = Build.MANUFACTURER.orNullIfUnknown(),
        brand = Build.BRAND.orNullIfUnknown(),
        model = Build.MODEL.orNullIfUnknown(),
        device = Build.DEVICE.orNullIfUnknown(),
        product = Build.PRODUCT.orNullIfUnknown(),
        board = Build.BOARD.orNullIfUnknown(),
        hardware = Build.HARDWARE.orNullIfUnknown(),
        androidVersion = Build.VERSION.RELEASE.orNullIfUnknown(),
        apiLevel = Build.VERSION.SDK_INT,
        securityPatch = Build.VERSION.SECURITY_PATCH.orNullIfUnknown(),
        buildNumber = Build.DISPLAY.orNullIfUnknown(),
        fingerprint = Build.FINGERPRINT.orNullIfUnknown(),
        bootloader = Build.BOOTLOADER.orNullIfUnknown(),
        kernelVersion = System.getProperty("os.version").orNullIfUnknown(),
        javaVmVersion = System.getProperty("java.vm.version").orNullIfUnknown(),
        uptimeMillis = SystemClock.elapsedRealtime(),
    )

    private fun String?.orNullIfUnknown(): String? =
        this?.trim()?.takeUnless { it.isEmpty() || it == Build.UNKNOWN }
}
