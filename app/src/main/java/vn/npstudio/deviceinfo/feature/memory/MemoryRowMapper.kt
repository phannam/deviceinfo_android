package vn.npstudio.deviceinfo.feature.memory

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.StorageInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import vn.npstudio.deviceinfo.ui.list.InfoSection
import vn.npstudio.deviceinfo.ui.list.reportRows
import kotlin.math.roundToInt

object MemoryRowMapper {

    fun sections(ram: MemoryInfo, storage: StorageInfo): List<InfoSection> = listOf(
        InfoSection(R.string.section_ram, R.drawable.ic_memory, ramRows(ram)),
        InfoSection(R.string.section_storage, R.drawable.ic_sd_storage, storageRows(storage)),
    )

    fun map(ram: MemoryInfo, storage: StorageInfo): List<InfoRowUiModel> = sections(ram, storage).reportRows()

    private fun ramRows(ram: MemoryInfo) = listOf(
        InfoRowUiModel(R.string.label_memory_ram_total, ValueFormatter.bytes(ram.totalBytes)),
        InfoRowUiModel(
            R.string.label_memory_ram_available,
            ValueFormatter.bytes(ram.availableBytes),
            usedPercent(ram.totalBytes, ram.availableBytes),
        ),
        InfoRowUiModel(R.string.label_memory_low_threshold, ValueFormatter.bytes(ram.lowMemoryThresholdBytes)),
        InfoRowUiModel(R.string.label_memory_low_now, ValueFormatter.yesNo(ram.lowMemory)),
    )

    private fun storageRows(storage: StorageInfo) = listOf(
        InfoRowUiModel(R.string.label_memory_storage_total, ValueFormatter.bytes(storage.totalBytes)),
        InfoRowUiModel(
            R.string.label_memory_storage_free,
            ValueFormatter.bytes(storage.freeBytes),
            usedPercent(storage.totalBytes, storage.freeBytes),
        ),
    )

    /** Percent of [total] in use, 0-100, or null when either value is missing. */
    fun usedPercent(total: Long?, free: Long?): Int? {
        if (total == null || free == null || total <= 0) return null
        return ((total - free) * 100.0 / total).roundToInt().coerceIn(0, 100)
    }
}
