package vn.npstudio.deviceinfo.domain.model

/** Null fields mean the platform did not report a usable value. */
data class DeviceInfo(
    val manufacturer: String?,
    val brand: String?,
    val model: String?,
    val device: String?,
    val product: String?,
    val board: String?,
    val hardware: String?,
    val androidVersion: String?,
    val apiLevel: Int,
    val securityPatch: String?,
    val buildNumber: String?,
    val fingerprint: String?,
    val bootloader: String?,
    val kernelVersion: String?,
    val javaVmVersion: String?,
    val uptimeMillis: Long,
)
