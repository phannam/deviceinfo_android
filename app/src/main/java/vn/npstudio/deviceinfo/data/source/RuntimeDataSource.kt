package vn.npstudio.deviceinfo.data.source

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.RegionInfo
import vn.npstudio.deviceinfo.domain.model.RuntimeInfo
import java.security.Security
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/** Kernel, VM, Google Play components, language, and time zone. Never throws. */
interface RuntimeDataSource {
    fun runtime(): RuntimeInfo
    fun region(): RegionInfo
}

class RuntimeDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RuntimeDataSource {

    override fun runtime(): RuntimeInfo = RuntimeInfo(
        kernelArchitecture = property("os.arch"),
        kernelVersion = property("os.version"),
        javaRuntime = join(property("java.runtime.name"), property("java.runtime.version")),
        javaVm = join(property("java.vm.name"), property("java.vm.version")),
        vmHeapMaxBytes = Runtime.getRuntime().maxMemory().takeIf { it in 1 until Long.MAX_VALUE },
        securityProvider = try {
            Security.getProviders().firstOrNull()?.let {
                @Suppress("DEPRECATION")
                "${it.name} ${it.version}"
            }
        } catch (_: Exception) {
            null
        },
        playServicesVersion = packageVersion(PLAY_SERVICES_PACKAGE),
        playSystemUpdate = packageVersion(MODULE_METADATA_PACKAGE),
    )

    override fun region(): RegionInfo = RegionInfo(
        languageTag = Locale.getDefault().toLanguageTag(),
        timeZoneId = TimeZone.getDefault().id,
        bootCount = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
        } catch (_: Settings.SettingNotFoundException) {
            null
        },
    )

    private fun property(name: String): String? = System.getProperty(name)?.trim()?.ifEmpty { null }

    private fun join(vararg parts: String?): String? =
        parts.filterNotNull().joinToString(" ").ifEmpty { null }

    private fun packageVersion(packageName: String): String? = try {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        info.versionName?.trim()?.ifEmpty { null }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private companion object {
        const val PLAY_SERVICES_PACKAGE = "com.google.android.gms"

        // Mainline module metadata; its version name is the Google Play system update date.
        const val MODULE_METADATA_PACKAGE = "com.google.android.modulemetadata"
    }
}
