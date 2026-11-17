package vn.npstudio.deviceinfo.data.source

import vn.npstudio.deviceinfo.domain.model.SelinuxMode
import vn.npstudio.deviceinfo.domain.model.SystemSecurity
import vn.npstudio.deviceinfo.domain.model.VendorDetails

/** Pure parsing of `getprop` and SELinux output. */
object SystemPropertiesParser {

    private val LINE = Regex("""^\[(.+?)]: \[(.*)]$""")

    /** Parses `[key]: [value]` lines; multi-line values and other lines are skipped. */
    fun parse(output: String): Map<String, String> = output.lineSequence()
        .mapNotNull { LINE.matchEntire(it.trim()) }
        .associate { it.groupValues[1] to it.groupValues[2] }

    /** Accepts `/sys/fs/selinux/enforce` (`1`/`0`) or `getenforce` output. */
    fun selinux(raw: String?): SelinuxMode? = when (raw?.trim()?.lowercase()) {
        "1", "enforcing" -> SelinuxMode.ENFORCING
        "0", "permissive" -> SelinuxMode.PERMISSIVE
        "disabled" -> SelinuxMode.DISABLED
        else -> null
    }

    /**
     * Partition flags are null when [props] is empty (getprop failed). A missing flag means false, except
     * system-as-root, which Android 10+ requires even though the property is no longer set.
     */
    fun security(
        props: Map<String, String>,
        rooted: Boolean,
        selinux: SelinuxMode?,
        apiLevel: Int,
    ): SystemSecurity {
        fun flag(key: String): Boolean? = if (props.isEmpty()) null else props[key] == "true"
        return SystemSecurity(
            rooted = rooted,
            selinux = selinux,
            systemAsRoot = flag(KEY_SYSTEM_ROOT)?.let { it || apiLevel >= API_SYSTEM_AS_ROOT_REQUIRED },
            seamlessUpdates = flag(KEY_AB_UPDATE),
            dynamicPartitions = flag(KEY_DYNAMIC_PARTITIONS),
            treble = flag(KEY_TREBLE),
            vndkVersion = props[KEY_VNDK]?.ifBlank { null },
        )
    }

    fun vendor(props: Map<String, String>): VendorDetails {
        fun first(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
            props[key]?.trim()?.takeUnless { it.isEmpty() || it.lowercase() in PLACEHOLDERS || it.all { c -> c == '0' } }
        }
        return VendorDetails(
            bootloaderLocked = bootloaderLocked(props),
            productCode = first("ril.product_code"),
            serialNumber = first("ril.serialnumber", "ro.serialno", "ro.boot.serialno"),
            salesCode = first("ro.csc.sales_code", "ril.sales_code"),
            country = first("ro.csc.country_code"),
            countryIso = first("ro.csc.countryiso_code"),
            cscVersion = first("ril.official_cscver"),
            warrantyBit = props["ro.boot.warranty_bit"]?.trim()?.toIntOrNull(),
            manufactureDate = first("ril.rfcal_date"),
        )
    }

    private fun bootloaderLocked(props: Map<String, String>): Boolean? =
        when (props["ro.boot.flash.locked"]?.trim()) {
            "1" -> true
            "0" -> false
            else -> when (props["ro.boot.vbmeta.device_state"]?.trim()) {
                "locked" -> true
                "unlocked" -> false
                // yellow = locked with a custom key; orange = unlocked.
                else -> when (props["ro.boot.verifiedbootstate"]?.trim()) {
                    "green", "yellow" -> true
                    "orange" -> false
                    else -> null
                }
            }
        }

    private val PLACEHOLDERS = setOf("unknown", "none", "null")

    const val KEY_BASEBAND = "gsm.version.baseband"
    private const val KEY_SYSTEM_ROOT = "ro.build.system_root_image"
    private const val KEY_AB_UPDATE = "ro.build.ab_update"
    private const val KEY_DYNAMIC_PARTITIONS = "ro.boot.dynamic_partitions"
    private const val KEY_TREBLE = "ro.treble.enabled"
    private const val KEY_VNDK = "ro.vndk.version"
    private const val API_SYSTEM_AS_ROOT_REQUIRED = 29
}
