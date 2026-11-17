package vn.npstudio.deviceinfo.data.source

import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import vn.npstudio.deviceinfo.domain.model.CodecInfo
import vn.npstudio.deviceinfo.domain.model.CodecKind
import vn.npstudio.deviceinfo.domain.model.DrmInfo
import java.util.UUID
import javax.inject.Inject

/** Media codecs and Widevine DRM. Blocking; call on the IO dispatcher. Never throws. */
interface MediaDataSource {
    fun codecs(): List<CodecInfo>

    /** Null when Widevine is not supported. */
    fun widevine(): DrmInfo?
}

class MediaDataSourceImpl @Inject constructor() : MediaDataSource {

    override fun codecs(): List<CodecInfo> = try {
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.mapNotNull { info ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isAlias) return@mapNotNull null
            val types = info.supportedTypes.toList()
            val kind = when {
                types.any { it.startsWith("video/") } -> CodecKind.VIDEO
                types.any { it.startsWith("audio/") } -> CodecKind.AUDIO
                else -> return@mapNotNull null
            }
            CodecInfo(
                name = info.name,
                kind = kind,
                mimeTypes = types,
                encoder = info.isEncoder,
                hardwareAccelerated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    info.isHardwareAccelerated
                } else {
                    null
                },
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    override fun widevine(): DrmInfo? {
        val drm = try {
            if (!MediaDrm.isCryptoSchemeSupported(WIDEVINE_UUID)) return null
            MediaDrm(WIDEVINE_UUID)
        } catch (_: Exception) {
            return null
        }
        return try {
            DrmInfo(
                vendor = drm.property("vendor"),
                version = drm.property("version"),
                description = drm.property("description"),
                algorithms = drm.property("algorithms"),
                securityLevel = drm.property("securityLevel"),
                systemId = drm.property("systemId"),
                hdcpLevel = drm.property("hdcpLevel"),
                maxHdcpLevel = drm.property("maxHdcpLevel"),
                usageReportingSupport = drm.property("usageReportingSupport"),
                maxSessions = drm.property("maxNumberOfSessions"),
                openSessions = drm.property("numberOfOpenSessions"),
            )
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                drm.close()
            } else {
                @Suppress("DEPRECATION")
                drm.release()
            }
        }
    }

    private fun MediaDrm.property(name: String): String? = try {
        getPropertyString(name).trim().ifEmpty { null }
    } catch (_: Exception) {
        null
    }

    private companion object {
        val WIDEVINE_UUID: UUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")
    }
}
