package vn.npstudio.deviceinfo.testing

import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository

val sampleDeviceInfo = DeviceInfo(
    manufacturer = "samsung",
    brand = "samsung",
    model = "SM-N950U1",
    device = "greatqlte",
    product = "greatqlteue",
    board = "msm8998",
    hardware = "qcom",
    androidVersion = "9",
    apiLevel = 28,
    securityPatch = "2021-03-01",
    buildNumber = "PPR1.180610.011",
    fingerprint = "samsung/greatqlteue/greatqlte:9/PPR1/N950U1:user/release-keys",
    bootloader = "N950U1UES9DUC1",
    kernelVersion = "4.4.153",
    javaVmVersion = "2.1.0",
    uptimeMillis = 90_061_000L,
)

val sampleDisplayInfo = DisplayInfo(
    widthPx = 1440,
    heightPx = 2960,
    densityDpi = 560,
    density = 3.5f,
    xdpi = 521.0f,
    ydpi = 520.5f,
    fontScale = 1.0f,
)

class FakeDeviceInfoRepository(var info: DeviceInfo = sampleDeviceInfo) : DeviceInfoRepository {
    override suspend fun getDeviceInfo(): DeviceInfo = info
}

class FakeDisplayRepository(var info: DisplayInfo = sampleDisplayInfo) : DisplayRepository {
    override suspend fun getDisplayInfo(): DisplayInfo = info
}
