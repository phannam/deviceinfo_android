package vn.npstudio.deviceinfo.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import vn.npstudio.deviceinfo.data.source.AppConfigLocalDataSource
import vn.npstudio.deviceinfo.data.source.BatteryBroadcast
import vn.npstudio.deviceinfo.data.source.BatteryDataSource
import vn.npstudio.deviceinfo.data.source.BatteryProperties
import vn.npstudio.deviceinfo.data.source.CpuInfoDataSource
import vn.npstudio.deviceinfo.data.source.MemoryDataSource
import vn.npstudio.deviceinfo.data.source.SysFsReader
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.CpuCluster
import vn.npstudio.deviceinfo.domain.model.CpuCore
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.CpuInfo
import vn.npstudio.deviceinfo.domain.model.AndroidBuild
import vn.npstudio.deviceinfo.domain.model.CodecInfo
import vn.npstudio.deviceinfo.domain.model.CodecKind
import vn.npstudio.deviceinfo.domain.model.DeviceHardware
import vn.npstudio.deviceinfo.domain.model.DeviceIdentifiers
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.model.DisplayInfo
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import vn.npstudio.deviceinfo.domain.model.HardwareFeature
import vn.npstudio.deviceinfo.domain.model.HdrType
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.PhoneType
import vn.npstudio.deviceinfo.domain.model.PowerSource
import vn.npstudio.deviceinfo.domain.model.RegionInfo
import vn.npstudio.deviceinfo.domain.model.RuntimeInfo
import vn.npstudio.deviceinfo.domain.model.SelinuxMode
import vn.npstudio.deviceinfo.domain.model.StorageInfo
import vn.npstudio.deviceinfo.domain.model.SystemInfo
import vn.npstudio.deviceinfo.domain.model.SystemSecurity
import vn.npstudio.deviceinfo.domain.model.TelephonyInfo
import vn.npstudio.deviceinfo.domain.model.ThermalStatus
import vn.npstudio.deviceinfo.domain.model.VendorDetails
import vn.npstudio.deviceinfo.domain.model.WindowSnapshot
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.domain.repository.MemoryRepository
import vn.npstudio.deviceinfo.domain.repository.SystemInfoRepository

val sampleDeviceInfo = DeviceInfo(
    deviceName = "Galaxy Note8",
    hardware = DeviceHardware(
        manufacturer = "samsung",
        brand = "samsung",
        model = "SM-N950U1",
        device = "greatqlte",
        product = "greatqlteue",
        board = "msm8998",
        hardware = "qcom",
    ),
    vendor = VendorDetails(
        bootloaderLocked = true,
        productCode = "SM-N950UZKAXAA",
        serialNumber = "R28J9470XTT",
        salesCode = "XAA",
        country = "USA",
        countryIso = "US",
        warrantyBit = 0,
    ),
    identifiers = DeviceIdentifiers(
        androidId = "9774d56d682e549c",
        gsfId = "3A1B2C3D4E5F6071",
        advertisingId = null,
        hardwareSerial = GuardedValue.Restricted,
        fingerprint = "samsung/greatqlteue/greatqlte:9/PPR1/N950U1:user/release-keys",
        imei = GuardedValue.NeedsPermission("android.permission.READ_PHONE_STATE"),
        simSerial = GuardedValue.Restricted,
        subscriberId = GuardedValue.Unavailable,
        wifiMac = GuardedValue.Available("ac:5f:3e:00:11:22"),
        bluetoothMac = GuardedValue.Restricted,
        usbDebugging = true,
        developerOptions = true,
    ),
    telephony = TelephonyInfo(
        phoneType = PhoneType.GSM,
        simSlots = 2,
        simOperator = "Viettel",
        networkOperator = "Viettel",
        networkType = GuardedValue.NeedsPermission("android.permission.READ_PHONE_STATE"),
    ),
    features = mapOf(HardwareFeature.NFC to true, HardwareFeature.UWB to false),
    codecs = listOf(
        CodecInfo("c2.android.avc.decoder", CodecKind.VIDEO, listOf("video/avc"), false, false),
        CodecInfo("c2.qti.avc.encoder", CodecKind.VIDEO, listOf("video/avc"), true, true),
        CodecInfo("c2.android.aac.decoder", CodecKind.AUDIO, listOf("audio/mp4a-latm"), false, null),
    ),
)

val sampleDisplayInfo = DisplayInfo(
    widthPx = 1440,
    heightPx = 2960,
    densityDpi = 560,
    density = 3.5f,
    xdpi = 521.0f,
    ydpi = 520.5f,
    fontScale = 1.0f,
    refreshRateHz = 60.000004f,
    supportedRefreshRatesHz = listOf(60.000004f),
    hdrTypes = listOf(HdrType.HDR10),
    wideColorGamut = true,
)

val sampleWindowSnapshot = WindowSnapshot(widthPx = 1440, heightPx = 2960, hasCutout = false)

class FakeDeviceInfoRepository(
    var info: DeviceInfo = sampleDeviceInfo,
    var phonePermissionGranted: Boolean = false,
) : DeviceInfoRepository {
    var loadCount = 0
        private set

    override suspend fun getDeviceInfo(): DeviceInfo {
        loadCount++
        return info
    }

    override fun isPhonePermissionGranted(): Boolean = phonePermissionGranted
}

val sampleSystemInfo = SystemInfo(
    build = AndroidBuild(
        release = "9",
        apiLevel = 28,
        securityPatch = "2021-03-01",
        displayId = "PPR1.180610.011",
        buildId = "PPR1.180610.011",
        buildTimeMillis = 1_614_556_800_000L,
        type = "user",
        tags = "release-keys",
        bootloader = "N950U1UES9DUC1",
        baseband = "N950U1UES9DUC1",
    ),
    security = SystemSecurity(
        rooted = false,
        selinux = SelinuxMode.ENFORCING,
        systemAsRoot = false,
        seamlessUpdates = false,
        dynamicPartitions = false,
        treble = true,
        vndkVersion = "28",
    ),
    systemFeatures = listOf("android.hardware.nfc", "android.software.webview"),
    runtime = RuntimeInfo(
        kernelArchitecture = "aarch64",
        kernelVersion = "4.4.153",
        javaRuntime = "Android Runtime 0.9",
        javaVm = "Dalvik 2.1.0",
        vmHeapMaxBytes = 536_870_912L,
        securityProvider = "AndroidOpenSSL 1.0",
        playServicesVersion = "24.20.13",
        playSystemUpdate = null,
    ),
    region = RegionInfo(languageTag = "vi-VN", timeZoneId = "Asia/Ho_Chi_Minh", bootCount = 42),
    drm = null,
)

class FakeSystemInfoRepository(
    var info: SystemInfo = sampleSystemInfo,
    val uptime: Flow<Long> = flowOf(90_061_000L),
) : SystemInfoRepository {
    override suspend fun getSystemInfo(): SystemInfo = info
    override fun observeUptime(): Flow<Long> = uptime
}

class FakeDisplayRepository(var info: DisplayInfo = sampleDisplayInfo) : DisplayRepository {
    var loadCount = 0
        private set

    override suspend fun getDisplayInfo(): DisplayInfo {
        loadCount++
        return info
    }
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

val sampleMemoryInfo = MemoryInfo(
    totalBytes = 6_000_000_000L,
    availableBytes = 1_500_000_000L,
    lowMemoryThresholdBytes = 226_000_000L,
    lowMemory = false,
)

val sampleStorageInfo = StorageInfo(totalBytes = 64_000_000_000L, freeBytes = 16_000_000_000L)

class FakeMemoryRepository(
    val ram: Flow<MemoryInfo> = flowOf(sampleMemoryInfo),
    var storage: StorageInfo = sampleStorageInfo,
) : MemoryRepository {
    var storageLoads = 0
        private set

    override fun observeRam(): Flow<MemoryInfo> = ram

    override suspend fun getStorageInfo(): StorageInfo {
        storageLoads++
        return storage
    }
}

class FakeMemoryDataSource(
    var ram: MemoryInfo = sampleMemoryInfo,
    var storage: StorageInfo = sampleStorageInfo,
) : MemoryDataSource {
    var ramReads = 0
        private set

    override fun readRam(): MemoryInfo {
        ramReads++
        return ram
    }

    override fun readStorage(): StorageInfo = storage
}

class FakeAppConfigLocalDataSource(
    val values: MutableMap<String, String> = mutableMapOf(),
) : AppConfigLocalDataSource {
    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
