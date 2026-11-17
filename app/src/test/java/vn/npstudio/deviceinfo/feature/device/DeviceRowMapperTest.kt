package vn.npstudio.deviceinfo.feature.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import vn.npstudio.deviceinfo.domain.model.HardwareFeature
import vn.npstudio.deviceinfo.domain.model.VendorDetails
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleDeviceInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.RowAction

class DeviceRowMapperTest {

    private val rows = DeviceRowMapper.sections(sampleDeviceInfo).flatMap { it.rows }

    @Test
    fun sections_formatsHardwareRows() {
        val values = rows.byLabel()

        assertEquals(UiText.Dynamic("SM-N950U1"), values[UiText.res(R.string.label_device_model)])
        assertEquals(UiText.Dynamic("Viettel"), values[UiText.res(R.string.label_device_sim_operator)])
        assertEquals(UiText.res(R.string.common_on), values[UiText.res(R.string.label_device_usb_debugging)])
    }

    @Test
    fun sections_showsVendorRowsOnlyWhenSet() {
        val values = rows.byLabel()

        assertEquals(UiText.Dynamic("Galaxy Note8"), values[UiText.res(R.string.label_device_name)])
        assertEquals(
            UiText.res(R.string.value_bootloader_locked),
            values[UiText.res(R.string.label_device_bootloader_state)],
        )
        assertEquals(UiText.Dynamic("USA (US) - XAA"), values[UiText.res(R.string.label_device_sales_region)])
        assertEquals(UiText.res(R.string.value_knox_secure, 0), values[UiText.res(R.string.label_device_knox_fuse)])
        assertEquals(UiText.Dynamic("R28J9470XTT"), values[UiText.res(R.string.label_device_serial_number)])
        assertFalse(UiText.res(R.string.label_device_csc_firmware) in values)

        val plain = DeviceRowMapper.sections(sampleDeviceInfo.copy(vendor = VendorDetails()))
            .flatMap { it.rows }.byLabel()
        assertEquals(ValueFormatter.unavailable, plain[UiText.res(R.string.label_device_bootloader_state)])
        assertFalse(UiText.res(R.string.label_device_product_code) in plain)
        assertFalse(UiText.res(R.string.label_device_serial_number) in plain)
    }

    @Test
    fun salesRegion_usesAvailableParts() {
        assertEquals(UiText.Dynamic("XAA"), DeviceRowMapper.salesRegion(VendorDetails(salesCode = "XAA")))
        assertNull(DeviceRowMapper.salesRegion(VendorDetails()))
    }

    @Test
    fun hero_showsDeviceNameAndBrand() {
        val hero = DeviceRowMapper.hero(sampleDeviceInfo)

        assertEquals(UiText.Dynamic("Galaxy Note8"), hero.title)
        assertEquals(UiText.Dynamic("SAMSUNG"), hero.subtitle)
        assertEquals(UiText.Dynamic("SM-N950U1"), hero.detail)

        val unnamed = DeviceRowMapper.hero(sampleDeviceInfo.copy(deviceName = null))
        assertEquals(UiText.Dynamic("SM-N950U1"), unnamed.title)
        assertNull(unnamed.detail)
    }

    @Test
    fun map_leavesOutVendorSerial() {
        assertFalse(UiText.res(R.string.label_device_serial_number) in DeviceRowMapper.map(sampleDeviceInfo).byLabel())
    }

    @Test
    fun guardedRow_mapsEveryState() {
        val label = R.string.label_device_imei
        val permission = "android.permission.READ_PHONE_STATE"

        assertEquals(UiText.Dynamic("123"), DeviceRowMapper.guardedRow(label, GuardedValue.Available("123")).value)
        val needsPermission = DeviceRowMapper.guardedRow(label, GuardedValue.NeedsPermission(permission))
        assertEquals(UiText.res(R.string.value_permission_required), needsPermission.value)
        assertEquals(RowAction.RequestPermission(permission), needsPermission.action)
        val restricted = DeviceRowMapper.guardedRow(label, GuardedValue.Restricted)
        assertEquals(UiText.res(R.string.value_restricted), restricted.value)
        assertNull(restricted.action)
        assertEquals(ValueFormatter.unavailable, DeviceRowMapper.guardedRow(label, GuardedValue.Unavailable).value)
    }

    @Test
    fun sections_featuresCoverEveryEntryAndMissingIsUnsupported() {
        val features = DeviceRowMapper.sections(sampleDeviceInfo).flatMap { it.features }

        assertEquals(HardwareFeature.entries.size, features.size)
        assertTrue(features.first { it.label == UiText.res(R.string.feature_nfc) }.supported)
        assertFalse(features.first { it.label == UiText.res(R.string.feature_uwb) }.supported)
        assertFalse(features.first { it.label == UiText.res(R.string.feature_gps) }.supported)
    }

    @Test
    fun sections_codecRowsOpenSortedLists() {
        val video = rows.first { it.label == UiText.res(R.string.label_device_video_codecs) }

        assertEquals(UiText.Dynamic("2"), video.value)
        val entries = (video.action as RowAction.ShowList).entries
        assertEquals(listOf("c2.android.avc.decoder", "c2.qti.avc.encoder"), entries.map { it.title })
        assertEquals(
            UiText.Joined(
                listOf(
                    UiText.Dynamic("video/avc"),
                    UiText.res(R.string.value_codec_encoder),
                    UiText.res(R.string.value_codec_hardware),
                ),
                " · ",
            ),
            entries[1].subtitle,
        )
    }

    @Test
    fun sections_noCodecsIsUnavailableWithoutAction() {
        val rows = DeviceRowMapper.sections(sampleDeviceInfo.copy(codecs = emptyList())).flatMap { it.rows }
        val audio = rows.first { it.label == UiText.res(R.string.label_device_audio_codecs) }

        assertEquals(ValueFormatter.unavailable, audio.value)
        assertNull(audio.action)
    }

    @Test
    fun map_leavesOutIdentifiersAndAddsFeatureSummary() {
        val report = DeviceRowMapper.map(sampleDeviceInfo).byLabel()

        assertFalse(UiText.res(R.string.label_device_android_id) in report)
        assertFalse(UiText.res(R.string.label_device_imei) in report)
        assertEquals(UiText.Joined(listOf(UiText.res(R.string.feature_nfc)), ", "), report[UiText.res(R.string.label_features_supported)])
    }
}
