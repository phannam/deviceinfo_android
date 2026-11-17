package vn.npstudio.deviceinfo.data.source

import vn.npstudio.deviceinfo.domain.model.SelinuxMode
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Build properties, SELinux, and root checks. Blocking; call on the IO dispatcher. Never throws. */
interface PlatformDataSource {
    /** Parsed `getprop` output; empty when the command fails. */
    fun systemProperties(): Map<String, String>
    fun selinuxMode(): SelinuxMode?
    fun suBinaryFound(): Boolean
}

class PlatformDataSourceImpl @Inject constructor(
    private val sysFsReader: SysFsReader,
) : PlatformDataSource {

    // getprop is a public shell tool; reflection on android.os.SystemProperties is a hidden API.
    override fun systemProperties(): Map<String, String> =
        run(GETPROP)?.let(SystemPropertiesParser::parse).orEmpty()

    override fun selinuxMode(): SelinuxMode? =
        SystemPropertiesParser.selinux(sysFsReader.read(SELINUX_ENFORCE) ?: run(GETENFORCE))

    override fun suBinaryFound(): Boolean = SU_PATHS.any { path ->
        try {
            File(path).exists()
        } catch (_: SecurityException) {
            false
        }
    }

    private fun run(command: String): String? = try {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) process.destroy()
        output.trim().ifEmpty { null }
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val GETPROP = "/system/bin/getprop"
        const val GETENFORCE = "/system/bin/getenforce"
        const val SELINUX_ENFORCE = "/sys/fs/selinux/enforce"
        const val COMMAND_TIMEOUT_SECONDS = 2L
        val SU_PATHS = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/sd/xbin/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/su/bin/su",
            "/debug_ramdisk/su",
        )
    }
}
