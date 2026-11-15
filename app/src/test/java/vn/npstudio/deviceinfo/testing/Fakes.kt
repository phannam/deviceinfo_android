package vn.npstudio.deviceinfo.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import vn.npstudio.deviceinfo.data.source.BatteryBroadcast
import vn.npstudio.deviceinfo.data.source.BatteryDataSource
import vn.npstudio.deviceinfo.data.source.BatteryProperties
import vn.npstudio.deviceinfo.data.source.CpuInfoDataSource
import vn.npstudio.deviceinfo.data.source.SysFsReader
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.CpuCluster
import vn.npstudio.deviceinfo.domain.model.CpuCore
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.CpuInfo
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.model.PowerSource
import vn.npstudio.deviceinfo.domain.model.ThermalStatus
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
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

val sampleCpuInfo = CpuInfo(
    socManufacturer = null,
    socModel = "Qualcomm Technologies, Inc MSM8998",
    supportedAbis = listOf("arm64-v8a", "armeabi-v7a", "armeabi"),
    cores = (0..7).map { id ->
        if (id < 4) CpuCore(id, 300_000L, 1_900_800L) else CpuCore(id, 300_000L, 2_361_600L)
    },
    clusters = listOf(CpuCluster(4, 1_900_800L), CpuCluster(4, 2_361_600L)),
    governors = listOf("schedutil"),
    thermalStatus = null,
    thermalHeadroom = null,
)

val sampleFrequencies = (0..7).map { CpuCoreFrequency(it, if (it == 7) null else 1_100_400L) }

class FakeCpuRepository(
    var info: CpuInfo = sampleCpuInfo,
    val frequencies: Flow<List<CpuCoreFrequency>> = flowOf(sampleFrequencies),
) : CpuRepository {
    override suspend fun getCpuInfo(): CpuInfo = info
    override fun observeFrequencies(): Flow<List<CpuCoreFrequency>> = frequencies
}

/** In-memory `/proc` and `/sys`; missing paths read as null like the real reader. */
class FakeSysFsReader(val files: MutableMap<String, String> = mutableMapOf()) : SysFsReader {
    override fun read(path: String): String? = files[path]?.trim()?.ifEmpty { null }
}

class FakeCpuInfoDataSource(
    var socManufacturer: String? = null,
    var socModel: String? = null,
    var hardware: String? = "qcom",
    var processors: Int = 8,
) : CpuInfoDataSource {
    override fun socManufacturer() = socManufacturer
    override fun socModel() = socModel
    override fun hardware() = hardware
    override fun supportedAbis() = listOf("arm64-v8a")
    override fun availableProcessors() = processors
    override fun thermalStatus(): ThermalStatus? = ThermalStatus.NONE
    override fun thermalHeadroom(): Float? = null
}

val sampleBatteryInfo = BatteryInfo(
    levelPercent = 80,
    status = BatteryStatus.CHARGING,
    powerSource = PowerSource.AC,
    health = BatteryHealth.GOOD,
    temperatureTenthsCelsius = 312,
    voltageMillivolts = 4_215,
    technology = "Li-ion",
    present = true,
    cycleCount = null,
    currentNowMicroamps = -450_000,
    currentAverageMicroamps = null,
    chargeCounterMicroampHours = 2_640_000,
    chargeTimeRemainingMillis = 5_100_000L,
    powerSaveMode = false,
)

class FakeBatteryRepository(
    val flow: Flow<BatteryInfo> = flowOf(sampleBatteryInfo),
) : BatteryRepository {
    override fun observe(): Flow<BatteryInfo> = flow
}

val sampleBatteryBroadcast = BatteryBroadcast(
    level = 80,
    scale = 100,
    status = 2,
    plugged = 1,
    health = 2,
    temperature = 312,
    voltage = 4_215,
    technology = "Li-ion",
    present = true,
    cycleCount = -1,
)

val sampleBatteryProperties = BatteryProperties(
    currentNow = -450_000,
    currentAverage = Int.MIN_VALUE,
    chargeCounter = 2_640_000,
    chargeTimeRemainingMillis = 5_100_000L,
    powerSaveMode = false,
)

class FakeBatteryDataSource(
    var properties: BatteryProperties = sampleBatteryProperties,
) : BatteryDataSource {
    val broadcasts = MutableSharedFlow<BatteryBroadcast>(replay = 1)
    var propertyReads = 0
        private set

    override fun batteryChanges(): Flow<BatteryBroadcast> = broadcasts

    override fun readProperties(): BatteryProperties {
        propertyReads++
        return properties
    }
}
