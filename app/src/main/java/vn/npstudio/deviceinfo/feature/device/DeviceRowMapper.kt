package vn.npstudio.deviceinfo.feature.device

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.CodecInfo
import vn.npstudio.deviceinfo.domain.model.CodecKind
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import vn.npstudio.deviceinfo.domain.model.HardwareFeature
import vn.npstudio.deviceinfo.domain.model.PhoneType
import vn.npstudio.deviceinfo.domain.model.VendorDetails
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.FeatureUiModel
import vn.npstudio.deviceinfo.ui.list.HeroUiModel
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import vn.npstudio.deviceinfo.ui.list.InfoSection
import vn.npstudio.deviceinfo.ui.list.ListEntryUiModel
import vn.npstudio.deviceinfo.ui.list.RowAction
import vn.npstudio.deviceinfo.ui.list.reportRows
import java.util.Locale

/** Device tab sections (spec 003 section 5.1). Identifiers never go into the share report. */
object DeviceRowMapper {

    /** Containers the Android media framework can read on every supported version. */
    val containerFormats = listOf(
        "3GPP (.3gp)", "MPEG-4 (.mp4, .m4a)", "Matroska (.mkv)", "WebM (.webm)", "MPEG-TS (.ts)",
        "Ogg (.ogg)", "FLAC (.flac)", "WAVE (.wav)", "ADTS AAC (.aac)", "AMR (.amr)", "MP3 (.mp3)",
        "MIDI (.mid, .xmf, .ota, .imy, .rtttl)",
    )

    fun sections(info: DeviceInfo): List<InfoSection> = listOf(
        InfoSection(R.string.section_device, R.drawable.ic_phone_android, deviceRows(info)),
        InfoSection(
            R.string.section_identifiers,
            R.drawable.ic_perm_identity,
            identifierRows(info),
            inReport = false,
        ),
        InfoSection(R.string.section_telephony, R.drawable.ic_sim_card, telephonyRows(info)),
        InfoSection(
            R.string.section_developer,
            R.drawable.ic_code,
            listOf(
                InfoRowUiModel(R.string.label_device_usb_debugging, ValueFormatter.onOff(info.identifiers.usbDebugging)),
                InfoRowUiModel(
                    R.string.label_device_developer_options,
                    ValueFormatter.onOff(info.identifiers.developerOptions),
                ),
            ),
        ),
        InfoSection(R.string.section_features, R.drawable.ic_done_all, features = features(info)),
        InfoSection(R.string.section_multimedia, R.drawable.ic_movie, multimediaRows(info.codecs)),
    )

    fun map(info: DeviceInfo): List<InfoRowUiModel> = sections(info).reportRows()

    /** Device name as the title, brand above the model as the subtitle and detail. */
    fun hero(info: DeviceInfo): HeroUiModel = with(info.hardware) {
        val title = info.deviceName ?: model
        HeroUiModel(
            badgeIcon = R.drawable.ic_smartphone,
            title = ValueFormatter.text(title),
            subtitle = (brand ?: manufacturer)?.uppercase(Locale.ROOT)?.let(UiText::Dynamic),
            detail = model?.takeIf { it != title }?.let(UiText::Dynamic),
        )
    }

    private fun deviceRows(info: DeviceInfo) = with(info.hardware) {
        // Vendor rows appear only on devices that set them (mostly Samsung).
        val vendor = info.vendor
        listOfNotNull(
            InfoRowUiModel(R.string.label_device_name, ValueFormatter.text(info.deviceName)),
            InfoRowUiModel(R.string.label_device_model, ValueFormatter.text(model)),
            InfoRowUiModel(R.string.label_device_manufacturer, ValueFormatter.text(manufacturer)),
            InfoRowUiModel(R.string.label_device_brand, ValueFormatter.text(brand)),
            InfoRowUiModel(R.string.label_device_codename, ValueFormatter.text(device)),
            InfoRowUiModel(R.string.label_device_product, ValueFormatter.text(product)),
            InfoRowUiModel(R.string.label_device_board, ValueFormatter.text(board)),
            InfoRowUiModel(R.string.label_device_hardware, ValueFormatter.text(hardware)),
            InfoRowUiModel(R.string.label_device_bootloader_state, bootloader(vendor.bootloaderLocked)),
            vendor.productCode?.let { InfoRowUiModel(R.string.label_device_product_code, UiText.Dynamic(it)) },
            salesRegion(vendor)?.let { InfoRowUiModel(R.string.label_device_sales_region, it) },
            vendor.cscVersion?.let { InfoRowUiModel(R.string.label_device_csc_firmware, UiText.Dynamic(it)) },
            vendor.warrantyBit?.let { InfoRowUiModel(R.string.label_device_knox_fuse, knoxFuse(it)) },
            vendor.manufactureDate?.let {
                InfoRowUiModel(R.string.label_device_manufacture_date, UiText.Dynamic(it))
            },
            InfoRowUiModel(R.string.label_device_fingerprint, ValueFormatter.text(info.identifiers.fingerprint)),
        )
    }

    private fun bootloader(locked: Boolean?): UiText = when (locked) {
        true -> UiText.res(R.string.value_bootloader_locked)
        false -> UiText.res(R.string.value_bootloader_unlocked)
        null -> ValueFormatter.unavailable
    }

    private fun knoxFuse(bit: Int): UiText =
        UiText.res(if (bit == 0) R.string.value_knox_secure else R.string.value_knox_tripped, bit)

    /** `USA (US) - XAA`, or whichever parts the device reports. */
    fun salesRegion(vendor: VendorDetails): UiText? {
        val country = listOfNotNull(vendor.country, vendor.countryIso?.let { "($it)" })
            .joinToString(" ").ifEmpty { null }
        return listOfNotNull(country, vendor.salesCode).joinToString(" - ").ifEmpty { null }?.let(UiText::Dynamic)
    }

    private fun identifierRows(info: DeviceInfo) = with(info.identifiers) {
        listOfNotNull(
            InfoRowUiModel(R.string.label_device_android_id, ValueFormatter.text(androidId)),
            InfoRowUiModel(R.string.label_device_gsf_id, ValueFormatter.text(gsfId)),
            InfoRowUiModel(R.string.label_device_advertising_id, ValueFormatter.text(advertisingId)),
            guardedRow(R.string.label_device_serial, hardwareSerial),
            info.vendor.serialNumber?.let { InfoRowUiModel(R.string.label_device_serial_number, UiText.Dynamic(it)) },
            guardedRow(R.string.label_device_imei, imei),
            guardedRow(R.string.label_device_sim_serial, simSerial),
            guardedRow(R.string.label_device_subscriber_id, subscriberId),
            guardedRow(R.string.label_device_wifi_mac, wifiMac),
            guardedRow(R.string.label_device_bluetooth_mac, bluetoothMac),
        )
    }

    private fun telephonyRows(info: DeviceInfo) = with(info.telephony) {
        listOf(
            InfoRowUiModel(R.string.label_device_phone_type, UiText.res(phoneTypeRes(phoneType))),
            InfoRowUiModel(R.string.label_device_sim_slots, simSlots?.let(ValueFormatter::number) ?: ValueFormatter.unavailable),
            InfoRowUiModel(R.string.label_device_sim_operator, ValueFormatter.text(simOperator)),
            InfoRowUiModel(R.string.label_device_network_operator, ValueFormatter.text(networkOperator)),
            guardedRow(R.string.label_device_network_type, networkType),
        )
    }

    /** Permission rows keep "Permission required" as the value so the report stays readable. */
    fun guardedRow(@StringRes label: Int, value: GuardedValue<String>): InfoRowUiModel = when (value) {
        is GuardedValue.Available -> InfoRowUiModel(label, UiText.Dynamic(value.value))
        is GuardedValue.NeedsPermission -> InfoRowUiModel(
            label,
            UiText.res(R.string.value_permission_required),
            action = RowAction.RequestPermission(value.permission),
        )
        GuardedValue.Restricted -> InfoRowUiModel(label, UiText.res(R.string.value_restricted))
        GuardedValue.Unavailable -> InfoRowUiModel(label, ValueFormatter.unavailable)
    }

    private fun features(info: DeviceInfo): List<FeatureUiModel> = HardwareFeature.entries.map { feature ->
        FeatureUiModel(UiText.res(featureNameRes(feature)), info.features[feature] == true)
    }

    private fun multimediaRows(codecs: List<CodecInfo>): List<InfoRowUiModel> {
        val (video, audio) = codecs.sortedBy { it.name }.partition { it.kind == CodecKind.VIDEO }
        return listOf(
            listRow(R.string.label_device_video_codecs, video.map(::codecEntry)),
            listRow(R.string.label_device_audio_codecs, audio.map(::codecEntry)),
            listRow(R.string.label_device_container_formats, containerFormats.map { ListEntryUiModel(it) }),
        )
    }

    private fun listRow(@StringRes label: Int, entries: List<ListEntryUiModel>): InfoRowUiModel =
        if (entries.isEmpty()) {
            InfoRowUiModel(label, ValueFormatter.unavailable)
        } else {
            InfoRowUiModel(
                label,
                ValueFormatter.number(entries.size),
                action = RowAction.ShowList(UiText.res(label), entries),
            )
        }

    fun codecEntry(codec: CodecInfo): ListEntryUiModel {
        val parts = buildList {
            add(UiText.Dynamic(codec.mimeTypes.joinToString(", ")))
            add(UiText.res(if (codec.encoder) R.string.value_codec_encoder else R.string.value_codec_decoder))
            codec.hardwareAccelerated?.let {
                add(UiText.res(if (it) R.string.value_codec_hardware else R.string.value_codec_software))
            }
        }
        return ListEntryUiModel(codec.name, UiText.Joined(parts, " · "))
    }

    @StringRes
    private fun phoneTypeRes(type: PhoneType): Int = when (type) {
        PhoneType.NONE -> R.string.value_phone_type_none
        PhoneType.GSM -> R.string.value_phone_type_gsm
        PhoneType.CDMA -> R.string.value_phone_type_cdma
        PhoneType.SIP -> R.string.value_phone_type_sip
        PhoneType.UNKNOWN -> R.string.common_unavailable
    }

    @StringRes
    fun featureNameRes(feature: HardwareFeature): Int = when (feature) {
        HardwareFeature.NFC -> R.string.feature_nfc
        HardwareFeature.NFC_HCE -> R.string.feature_nfc_hce
        HardwareFeature.FINGERPRINT -> R.string.feature_fingerprint
        HardwareFeature.FACE -> R.string.feature_face
        HardwareFeature.IRIS -> R.string.feature_iris
        HardwareFeature.BLUETOOTH -> R.string.feature_bluetooth
        HardwareFeature.BLUETOOTH_LE -> R.string.feature_bluetooth_le
        HardwareFeature.IR_EMITTER -> R.string.feature_ir_emitter
        HardwareFeature.UWB -> R.string.feature_uwb
        HardwareFeature.WIFI -> R.string.feature_wifi
        HardwareFeature.WIFI_DIRECT -> R.string.feature_wifi_direct
        HardwareFeature.ETHERNET -> R.string.feature_ethernet
        HardwareFeature.GPS -> R.string.feature_gps
        HardwareFeature.USB_HOST -> R.string.feature_usb_host
        HardwareFeature.FLASHLIGHT -> R.string.feature_flashlight
        HardwareFeature.TELEPHONY -> R.string.feature_telephony
        HardwareFeature.ESIM -> R.string.feature_esim
    }
}
