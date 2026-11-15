package vn.npstudio.deviceinfo.data.source

/** Pure parsers for CPU files in `/proc` and `/sys`. */
object CpuSysFsParser {

    const val CPUINFO_PATH = "/proc/cpuinfo"
    const val POSSIBLE_PATH = "/sys/devices/system/cpu/possible"

    fun cpuDir(core: Int) = "/sys/devices/system/cpu/cpu$core"
    fun minFreqPath(core: Int) = "${cpuDir(core)}/cpufreq/cpuinfo_min_freq"
    fun maxFreqPath(core: Int) = "${cpuDir(core)}/cpufreq/cpuinfo_max_freq"
    fun curFreqPath(core: Int) = "${cpuDir(core)}/cpufreq/scaling_cur_freq"
    fun governorPath(core: Int) = "${cpuDir(core)}/cpufreq/scaling_governor"
    fun onlinePath(core: Int) = "${cpuDir(core)}/online"

    /** Parses a CPU list such as `0-7` or `0-3,4-7` or `0,2`. Returns empty on bad input. */
    fun parseCpuList(text: String?): List<Int> {
        if (text.isNullOrBlank()) return emptyList()
        val ids = sortedSetOf<Int>()
        for (part in text.trim().split(',')) {
            val bounds = part.trim().split('-')
            val start = bounds[0].toIntOrNull() ?: return emptyList()
            val end = if (bounds.size == 2) bounds[1].toIntOrNull() ?: return emptyList() else start
            if (bounds.size > 2 || end < start) return emptyList()
            ids.addAll(start..end)
        }
        return ids.toList()
    }

    /** Parses a frequency file in kHz. Returns null for blank, missing, or non-positive values. */
    fun parseKhz(text: String?): Long? = text?.trim()?.toLongOrNull()?.takeIf { it > 0 }

    /** Returns the `Hardware` value of `/proc/cpuinfo`, or null when the line is absent. */
    fun parseHardware(cpuinfo: String?): String? = cpuinfo
        ?.lineSequence()
        ?.firstOrNull { it.substringBefore(':').trim().equals("Hardware", ignoreCase = true) }
        ?.substringAfter(':', "")
        ?.trim()
        ?.ifEmpty { null }
}
