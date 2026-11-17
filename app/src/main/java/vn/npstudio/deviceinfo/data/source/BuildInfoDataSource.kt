package vn.npstudio.deviceinfo.data.source

import android.os.Build
import android.os.SystemClock
import vn.npstudio.deviceinfo.domain.model.AndroidBuild
import vn.npstudio.deviceinfo.domain.model.DeviceHardware
import javax.inject.Inject

/** Reads `android.os.Build` and the boot clock. */
interface BuildInfoDataSource {
    fun hardware(): DeviceHardware
    fun fingerprint(): String?
    fun androidBuild(): AndroidBuild
    fun uptimeMillis(): Long
}

class BuildInfoDataSourceImpl @Inject constructor() : BuildInfoDataSource {

    override fun hardware(): DeviceHardware = DeviceHardware(
        manufacturer = Build.MANUFACTURER.orNullIfUnknown(),
        brand = Build.BRAND.orNullIfUnknown(),
        model = Build.MODEL.orNullIfUnknown(),
        device = Build.DEVICE.orNullIfUnknown(),
        product = Build.PRODUCT.orNullIfUnknown(),
        board = Build.BOARD.orNullIfUnknown(),
        hardware = Build.HARDWARE.orNullIfUnknown(),
    )

    override fun fingerprint(): String? = Build.FINGERPRINT.orNullIfUnknown()

    override fun androidBuild(): AndroidBuild = AndroidBuild(
        release = Build.VERSION.RELEASE.orNullIfUnknown(),
        apiLevel = Build.VERSION.SDK_INT,
        securityPatch = Build.VERSION.SECURITY_PATCH.orNullIfUnknown(),
        displayId = Build.DISPLAY.orNullIfUnknown(),
        buildId = Build.ID.orNullIfUnknown(),
        buildTimeMillis = Build.TIME.takeIf { it > 0 },
        type = Build.TYPE.orNullIfUnknown(),
        tags = Build.TAGS.orNullIfUnknown(),
        bootloader = Build.BOOTLOADER.orNullIfUnknown(),
        baseband = Build.getRadioVersion().orNullIfUnknown(),
    )

    override fun uptimeMillis(): Long = SystemClock.elapsedRealtime()

    private fun String?.orNullIfUnknown(): String? =
        this?.trim()?.takeUnless { it.isEmpty() || it == Build.UNKNOWN }
}
