package vn.npstudio.deviceinfo.domain.model

/** System tab data (spec 003). Null fields mean the platform did not report a usable value. */
data class SystemInfo(
    val build: AndroidBuild,
    val security: SystemSecurity,
    val systemFeatures: List<String>,
    val runtime: RuntimeInfo,
    val region: RegionInfo,
    val drm: DrmInfo?,
)

data class AndroidBuild(
    val release: String?,
    val apiLevel: Int,
    val securityPatch: String?,
    val displayId: String?,
    val buildId: String?,
    val buildTimeMillis: Long?,
    val type: String?,
    val tags: String?,
    val bootloader: String?,
    val baseband: String?,
)

/** Partition flags are null when the build property is missing. */
data class SystemSecurity(
    val rooted: Boolean,
    val selinux: SelinuxMode?,
    val systemAsRoot: Boolean?,
    val seamlessUpdates: Boolean?,
    val dynamicPartitions: Boolean?,
    val treble: Boolean?,
    val vndkVersion: String?,
)

enum class SelinuxMode { ENFORCING, PERMISSIVE, DISABLED }

data class RuntimeInfo(
    val kernelArchitecture: String?,
    val kernelVersion: String?,
    val javaRuntime: String?,
    val javaVm: String?,
    val vmHeapMaxBytes: Long?,
    val securityProvider: String?,
    val playServicesVersion: String?,
    val playSystemUpdate: String?,
)

/** [languageTag] is a BCP 47 tag; [timeZoneId] an IANA id such as `Asia/Ho_Chi_Minh`. */
data class RegionInfo(
    val languageTag: String,
    val timeZoneId: String,
    val bootCount: Int?,
)

/** Widevine properties as reported by MediaDrm. */
data class DrmInfo(
    val vendor: String?,
    val version: String?,
    val description: String?,
    val algorithms: String?,
    val securityLevel: String?,
    val systemId: String?,
    val hdcpLevel: String?,
    val maxHdcpLevel: String?,
    val usageReportingSupport: String?,
    val maxSessions: String?,
    val openSessions: String?,
)
