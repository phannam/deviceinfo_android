package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSource
import vn.npstudio.deviceinfo.data.source.DeviceIdentifierDataSource
import vn.npstudio.deviceinfo.data.source.FeatureDataSource
import vn.npstudio.deviceinfo.data.source.MediaDataSource
import vn.npstudio.deviceinfo.data.source.PlatformDataSource
import vn.npstudio.deviceinfo.data.source.SystemPropertiesParser
import vn.npstudio.deviceinfo.data.source.TelephonyDataSource
import vn.npstudio.deviceinfo.domain.model.DeviceIdentifiers
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.model.HardwareFeature
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import javax.inject.Inject

class DeviceInfoRepositoryImpl @Inject constructor(
    private val buildInfoDataSource: BuildInfoDataSource,
    private val identifierDataSource: DeviceIdentifierDataSource,
    private val telephonyDataSource: TelephonyDataSource,
    private val featureDataSource: FeatureDataSource,
    private val mediaDataSource: MediaDataSource,
    private val platformDataSource: PlatformDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : DeviceInfoRepository {

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(ioDispatcher) {
        DeviceInfo(
            deviceName = identifierDataSource.deviceName(),
            hardware = buildInfoDataSource.hardware(),
            vendor = SystemPropertiesParser.vendor(platformDataSource.systemProperties()),
            identifiers = DeviceIdentifiers(
                androidId = identifierDataSource.androidId(),
                gsfId = identifierDataSource.gsfId(),
                advertisingId = identifierDataSource.advertisingId(),
                hardwareSerial = telephonyDataSource.hardwareSerial(),
                fingerprint = buildInfoDataSource.fingerprint(),
                imei = telephonyDataSource.imei(),
                simSerial = telephonyDataSource.simSerial(),
                subscriberId = telephonyDataSource.subscriberId(),
                wifiMac = identifierDataSource.wifiMac(),
                bluetoothMac = identifierDataSource.bluetoothMac(),
                usbDebugging = identifierDataSource.usbDebugging(),
                developerOptions = identifierDataSource.developerOptions(),
            ),
            telephony = telephonyDataSource.telephony(),
            features = HardwareFeature.entries.associateWith { featureDataSource.hasFeature(it.systemName) },
            codecs = mediaDataSource.codecs(),
        )
    }

    override fun isPhonePermissionGranted(): Boolean = telephonyDataSource.hasPhonePermission()
}
