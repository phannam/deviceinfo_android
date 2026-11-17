package vn.npstudio.deviceinfo.data.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.domain.model.SelinuxMode

class SystemPropertiesParserTest {

    @Test
    fun parse_readsKeyValueLinesAndSkipsOthers() {
        val output = """
            [ro.treble.enabled]: [true]
            [gsm.version.baseband]: [N950U1UES9DUC1,N950U1UES9DUC1]
            [ro.empty]: []
            garbage line
        """.trimIndent()

        assertEquals(
            mapOf(
                "ro.treble.enabled" to "true",
                "gsm.version.baseband" to "N950U1UES9DUC1,N950U1UES9DUC1",
                "ro.empty" to "",
            ),
            SystemPropertiesParser.parse(output),
        )
    }

    @Test
    fun selinux_acceptsSysFsAndGetenforceOutput() {
        assertEquals(SelinuxMode.ENFORCING, SystemPropertiesParser.selinux("1\n"))
        assertEquals(SelinuxMode.PERMISSIVE, SystemPropertiesParser.selinux("Permissive"))
        assertEquals(SelinuxMode.DISABLED, SystemPropertiesParser.selinux("Disabled"))
        assertNull(SystemPropertiesParser.selinux(null))
    }

    @Test
    fun security_missingFlagIsFalseAndSystemAsRootFromApi29() {
        val props = mapOf("ro.treble.enabled" to "true", "ro.vndk.version" to "30")

        val security = SystemPropertiesParser.security(props, rooted = false, selinux = null, apiLevel = 30)

        assertEquals(true, security.treble)
        assertEquals("30", security.vndkVersion)
        assertEquals(false, security.seamlessUpdates)
        assertEquals(true, security.systemAsRoot)
        assertEquals(false, SystemPropertiesParser.security(props, false, null, apiLevel = 28).systemAsRoot)
    }

    @Test
    fun security_noPropsMeansUnknownFlags() {
        val security = SystemPropertiesParser.security(emptyMap(), rooted = true, selinux = null, apiLevel = 34)

        assertNull(security.treble)
        assertNull(security.systemAsRoot)
        assertEquals(true, security.rooted)
    }

    @Test
    fun vendor_readsSamsungPropertiesAndSkipsPlaceholders() {
        val vendor = SystemPropertiesParser.vendor(
            mapOf(
                "ril.product_code" to "SM-N950UZKAXAA",
                "ril.serialnumber" to "00000000000",
                "ro.serialno" to "R28J9470XTT",
                "ro.csc.sales_code" to "XAA",
                "ril.official_cscver" to "unknown",
                "ro.boot.warranty_bit" to "0",
                "ro.boot.flash.locked" to "1",
            ),
        )

        assertEquals("SM-N950UZKAXAA", vendor.productCode)
        assertEquals("R28J9470XTT", vendor.serialNumber)
        assertEquals("XAA", vendor.salesCode)
        assertNull(vendor.cscVersion)
        assertEquals(0, vendor.warrantyBit)
        assertEquals(true, vendor.bootloaderLocked)
    }

    @Test
    fun vendor_bootloaderFallsBackToVerifiedBootState() {
        assertEquals(false, SystemPropertiesParser.vendor(mapOf("ro.boot.verifiedbootstate" to "orange")).bootloaderLocked)
        assertEquals(true, SystemPropertiesParser.vendor(mapOf("ro.boot.vbmeta.device_state" to "locked")).bootloaderLocked)
        assertNull(SystemPropertiesParser.vendor(emptyMap()).bootloaderLocked)
    }
}
