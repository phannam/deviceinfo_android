package vn.npstudio.deviceinfo.data.source

import java.io.File
import javax.inject.Inject

/** The only class that reads `/proc` and `/sys`. Never throws. */
interface SysFsReader {
    /** Returns the trimmed file content, or null when missing, unreadable, or blank. */
    fun read(path: String): String?
}

class SysFsReaderImpl @Inject constructor() : SysFsReader {

    override fun read(path: String): String? = try {
        File(path).readText().trim().ifEmpty { null }
    } catch (_: Exception) {
        null
    }
}
