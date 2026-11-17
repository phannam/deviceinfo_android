package vn.npstudio.deviceinfo.domain.model

/** Device tab data (spec 003). Null fields mean the platform did not report a usable value. */
data class DeviceInfo(
    /** User-visible device name from system settings, for example `Galaxy Note8`. */
    val deviceName: String?,
    val hardware: DeviceHardware,
    val vendor: VendorDetails,
    val identifiers: DeviceIdentifiers,
    val telephony: TelephonyInfo,
    val features: Map<HardwareFeature, Boolean>,
    val codecs: List<CodecInfo>,
)

data class DeviceHardware(
    val manufacturer: String?,
    val brand: String?,
    val model: String?,
    val device: String?,
    val product: String?,
    val board: String?,
    val hardware: String?,
)

/** Values from vendor build properties, mostly set by Samsung. Null when the device does not set them. */
data class VendorDetails(
    val bootloaderLocked: Boolean? = null,
    val productCode: String? = null,
    val serialNumber: String? = null,
    val salesCode: String? = null,
    val country: String? = null,
    val countryIso: String? = null,
    val cscVersion: String? = null,
    /** Samsung Knox warranty bit: 0 = never tripped. */
    val warrantyBit: Int? = null,
    val manufactureDate: String? = null,
)

data class DeviceIdentifiers(
    val androidId: String?,
    val gsfId: String?,
    val advertisingId: String?,
    val hardwareSerial: GuardedValue<String>,
    val fingerprint: String?,
    val imei: GuardedValue<String>,
    val simSerial: GuardedValue<String>,
    val subscriberId: GuardedValue<String>,
    val wifiMac: GuardedValue<String>,
    val bluetoothMac: GuardedValue<String>,
    val usbDebugging: Boolean?,
    val developerOptions: Boolean?,
)

data class TelephonyInfo(
    val phoneType: PhoneType,
    val simSlots: Int?,
    val simOperator: String?,
    val networkOperator: String?,
    /** Readable network type name, for example `LTE` or `NR (5G)`. */
    val networkType: GuardedValue<String>,
)

/** A value an app may not be allowed to read. */
sealed interface GuardedValue<out T> {
    data class Available<T>(val value: T) : GuardedValue<T>

    /** Readable after the user grants the runtime [permission]. */
    data class NeedsPermission(val permission: String) : GuardedValue<Nothing>

    /** Android blocks third-party apps from this value on this OS version. */
    data object Restricted : GuardedValue<Nothing>

    data object Unavailable : GuardedValue<Nothing>
}

enum class PhoneType { NONE, GSM, CDMA, SIP, UNKNOWN }

/** Hardware features shown as the Device tab checklist; [systemName] is the PackageManager feature. */
enum class HardwareFeature(val systemName: String) {
    NFC("android.hardware.nfc"),
    NFC_HCE("android.hardware.nfc.hce"),
    FINGERPRINT("android.hardware.fingerprint"),
    FACE("android.hardware.biometrics.face"),
    IRIS("android.hardware.biometrics.iris"),
    BLUETOOTH("android.hardware.bluetooth"),
    BLUETOOTH_LE("android.hardware.bluetooth_le"),
    IR_EMITTER("android.hardware.consumerir"),
    UWB("android.hardware.uwb"),
    WIFI("android.hardware.wifi"),
    WIFI_DIRECT("android.hardware.wifi.direct"),
    ETHERNET("android.hardware.ethernet"),
    GPS("android.hardware.location.gps"),
    USB_HOST("android.hardware.usb.host"),
    FLASHLIGHT("android.hardware.camera.flash"),
    TELEPHONY("android.hardware.telephony"),
    ESIM("android.hardware.telephony.euicc"),
}

enum class CodecKind { VIDEO, AUDIO }

/** [hardwareAccelerated] is null below API 29, where Android does not report it. */
data class CodecInfo(
    val name: String,
    val kind: CodecKind,
    val mimeTypes: List<String>,
    val encoder: Boolean,
    val hardwareAccelerated: Boolean?,
)
