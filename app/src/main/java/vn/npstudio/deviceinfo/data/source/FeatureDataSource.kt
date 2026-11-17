package vn.npstudio.deviceinfo.data.source

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** PackageManager system features. */
interface FeatureDataSource {
    fun hasFeature(name: String): Boolean

    /** Every feature the system declares, sorted; the OpenGL ES entry reads as `OpenGL ES 3.2`. */
    fun systemFeatures(): List<String>
}

class FeatureDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : FeatureDataSource {

    override fun hasFeature(name: String): Boolean = context.packageManager.hasSystemFeature(name)

    override fun systemFeatures(): List<String> = context.packageManager.systemAvailableFeatures
        .mapNotNull { it.name ?: glEsName(it.reqGlEsVersion) }
        .sorted()

    companion object {
        /** `reqGlEsVersion` keeps the major version in the upper 16 bits. */
        fun glEsName(version: Int): String? =
            if (version <= 0) null else "OpenGL ES ${version shr 16}.${version and 0xFFFF}"
    }
}
