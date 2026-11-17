package vn.npstudio.deviceinfo.feature.system

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.AndroidBuild
import vn.npstudio.deviceinfo.domain.model.DrmInfo
import vn.npstudio.deviceinfo.domain.model.RegionInfo
import vn.npstudio.deviceinfo.domain.model.SelinuxMode
import vn.npstudio.deviceinfo.domain.model.SystemInfo
import vn.npstudio.deviceinfo.domain.model.SystemSecurity
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.HeroUiModel
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import vn.npstudio.deviceinfo.ui.list.InfoSection
import vn.npstudio.deviceinfo.ui.list.ListEntryUiModel
import vn.npstudio.deviceinfo.ui.list.RowAction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** System tab sections and hero card (spec 003 section 5.2). */
object SystemRowMapper {

    fun hero(build: AndroidBuild, locale: Locale): HeroUiModel {
        val release = AndroidReleases.of(build.apiLevel)
        return HeroUiModel(
            badge = build.apiLevel.toString(),
            badgeLabel = UiText.res(R.string.hero_api),
            title = UiText.res(R.string.value_android_version, build.release ?: build.apiLevel.toString()),
            subtitle = release?.codename?.let(UiText::Dynamic),
            detail = release?.let {
                val date = it.releaseDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
                UiText.res(R.string.value_release_date, date)
            },
        )
    }

    fun sections(
        info: SystemInfo,
        uptimeMillis: Long,
        locale: Locale,
        zone: ZoneId,
        now: Instant = Instant.now(),
    ): List<InfoSection> = listOf(
        InfoSection(R.string.section_android, R.drawable.ic_android, androidRows(info, locale, zone)),
        InfoSection(R.string.section_security, R.drawable.ic_security, securityRows(info.security, info.systemFeatures)),
        InfoSection(R.string.section_kernel_runtime, R.drawable.ic_memory, runtimeRows(info)),
        InfoSection(R.string.section_status, R.drawable.ic_schedule, statusRows(info.region, uptimeMillis, locale, now)),
        InfoSection(R.string.section_drm, R.drawable.ic_lock, drmRows(info.drm)),
    )

    private fun androidRows(info: SystemInfo, locale: Locale, zone: ZoneId) = with(info.build) {
        listOf(
            InfoRowUiModel(R.string.label_system_android_version, ValueFormatter.text(release)),
            InfoRowUiModel(R.string.label_system_api_level, ValueFormatter.number(apiLevel)),
            InfoRowUiModel(R.string.label_system_security_patch, ValueFormatter.isoDate(securityPatch, locale)),
            InfoRowUiModel(R.string.label_system_build_number, ValueFormatter.text(displayId)),
            InfoRowUiModel(R.string.label_system_build_id, ValueFormatter.text(buildId)),
            InfoRowUiModel(R.string.label_system_build_time, ValueFormatter.dateTime(buildTimeMillis, zone, locale)),
            InfoRowUiModel(R.string.label_system_build_type, ValueFormatter.text(type)),
            InfoRowUiModel(R.string.label_system_build_tags, ValueFormatter.text(tags)),
            InfoRowUiModel(R.string.label_system_bootloader, ValueFormatter.text(bootloader)),
            InfoRowUiModel(R.string.label_system_baseband, ValueFormatter.text(baseband)),
            InfoRowUiModel(R.string.label_system_play_services, ValueFormatter.text(info.runtime.playServicesVersion)),
            InfoRowUiModel(
                R.string.label_system_play_system_update,
                ValueFormatter.isoDate(info.runtime.playSystemUpdate, locale),
            ),
        )
    }

    private fun securityRows(security: SystemSecurity, systemFeatures: List<String>) = listOf(
        InfoRowUiModel(R.string.label_system_root, ValueFormatter.yesNo(security.rooted)),
        InfoRowUiModel(R.string.label_system_selinux, selinux(security.selinux)),
        InfoRowUiModel(R.string.label_system_as_root, ValueFormatter.supported(security.systemAsRoot)),
        InfoRowUiModel(R.string.label_system_seamless_updates, ValueFormatter.supported(security.seamlessUpdates)),
        InfoRowUiModel(R.string.label_system_dynamic_partitions, ValueFormatter.supported(security.dynamicPartitions)),
        InfoRowUiModel(R.string.label_system_treble, treble(security)),
        if (systemFeatures.isEmpty()) {
            InfoRowUiModel(R.string.label_system_features, ValueFormatter.unavailable)
        } else {
            InfoRowUiModel(
                R.string.label_system_features,
                ValueFormatter.number(systemFeatures.size),
                action = RowAction.ShowList(
                    UiText.res(R.string.label_system_features),
                    systemFeatures.map { ListEntryUiModel(it) },
                ),
            )
        },
    )

    private fun runtimeRows(info: SystemInfo) = with(info.runtime) {
        listOf(
            InfoRowUiModel(R.string.label_system_kernel_arch, ValueFormatter.text(kernelArchitecture)),
            InfoRowUiModel(R.string.label_system_kernel_version, ValueFormatter.text(kernelVersion)),
            InfoRowUiModel(R.string.label_system_java_runtime, ValueFormatter.text(javaRuntime)),
            InfoRowUiModel(R.string.label_system_java_vm, ValueFormatter.text(javaVm)),
            InfoRowUiModel(R.string.label_system_vm_heap, ValueFormatter.bytes(vmHeapMaxBytes)),
            InfoRowUiModel(R.string.label_system_security_provider, ValueFormatter.text(securityProvider)),
        )
    }

    private fun statusRows(region: RegionInfo, uptimeMillis: Long, locale: Locale, now: Instant) = listOf(
        InfoRowUiModel(R.string.label_system_uptime, ValueFormatter.durationSeconds(uptimeMillis)),
        InfoRowUiModel(
            R.string.label_system_boot_count,
            region.bootCount?.let(ValueFormatter::number) ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(R.string.label_system_language, language(region.languageTag, locale)),
        InfoRowUiModel(R.string.label_system_time_zone, timeZone(region.timeZoneId, locale, now)),
    )

    private fun drmRows(drm: DrmInfo?): List<InfoRowUiModel> {
        if (drm == null) {
            return listOf(InfoRowUiModel(R.string.label_drm_widevine, UiText.res(R.string.value_widevine_not_supported)))
        }
        return listOf(
            R.string.label_drm_vendor to drm.vendor,
            R.string.label_drm_version to drm.version,
            R.string.label_drm_description to drm.description,
            R.string.label_drm_algorithms to drm.algorithms,
            R.string.label_drm_security_level to drm.securityLevel,
            R.string.label_drm_system_id to drm.systemId,
            R.string.label_drm_hdcp_level to drm.hdcpLevel,
            R.string.label_drm_max_hdcp_level to drm.maxHdcpLevel,
            R.string.label_drm_usage_reporting to drm.usageReportingSupport,
            R.string.label_drm_max_sessions to drm.maxSessions,
            R.string.label_drm_open_sessions to drm.openSessions,
        ).map { (label, value) -> InfoRowUiModel(label, ValueFormatter.text(value)) }
    }

    private fun selinux(mode: SelinuxMode?): UiText = when (mode) {
        null -> ValueFormatter.unavailable
        SelinuxMode.ENFORCING -> UiText.res(R.string.value_selinux_enforcing)
        SelinuxMode.PERMISSIVE -> UiText.res(R.string.value_selinux_permissive)
        SelinuxMode.DISABLED -> UiText.res(R.string.value_selinux_disabled)
    }

    private fun treble(security: SystemSecurity): UiText {
        val supported = ValueFormatter.supported(security.treble)
        val vndk = security.vndkVersion ?: return supported
        if (security.treble != true) return supported
        return UiText.res(R.string.value_treble_vndk, supported, vndk)
    }

    /** Display name in the app [locale], for example `Vietnamese (Vietnam)`. */
    fun language(tag: String, locale: Locale): UiText {
        val name = Locale.forLanguageTag(tag).getDisplayName(locale)
        return if (name.isBlank()) UiText.Dynamic(tag) else UiText.Dynamic(name)
    }

    /** Zone name with its offset at [now], for example `Indochina Time (GMT+07:00)`. */
    fun timeZone(id: String, locale: Locale, now: Instant): UiText {
        val zone = runCatching { ZoneId.of(id) }.getOrNull() ?: return UiText.Dynamic(id)
        val offset = zone.rules.getOffset(now)
        val offsetText = if (offset.totalSeconds == 0) "GMT" else "GMT${offset.id}"
        val name = zone.getDisplayName(TextStyle.FULL, locale)
        return UiText.Dynamic("$name ($offsetText)")
    }
}
