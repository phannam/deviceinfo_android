package vn.npstudio.deviceinfo.feature.memory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.domain.model.StorageInfo
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleMemoryInfo
import vn.npstudio.deviceinfo.testing.sampleStorageInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter

class MemoryRowMapperTest {

    @Test
    fun map_formatsBytesAndUsageBars() {
        val rows = MemoryRowMapper.map(sampleMemoryInfo, sampleStorageInfo)
        val values = rows.byLabel()

        assertEquals(UiText.res(R.string.value_gigabytes, 6.0), values[UiText.res(R.string.label_memory_ram_total)])
        assertEquals(UiText.res(R.string.value_gigabytes, 1.5), values[UiText.res(R.string.label_memory_ram_available)])
        assertEquals(UiText.res(R.string.value_megabytes, 226.0), values[UiText.res(R.string.label_memory_low_threshold)])
        assertEquals(UiText.res(R.string.common_no), values[UiText.res(R.string.label_memory_low_now)])
        assertEquals(UiText.res(R.string.value_gigabytes, 64.0), values[UiText.res(R.string.label_memory_storage_total)])
        assertEquals(UiText.res(R.string.value_gigabytes, 16.0), values[UiText.res(R.string.label_memory_storage_free)])

        val progress = rows.associate { it.label to it.progress }
        assertEquals(75, progress[UiText.res(R.string.label_memory_ram_available)])
        assertEquals(75, progress[UiText.res(R.string.label_memory_storage_free)])
    }

    @Test
    fun map_missingValuesAreUnavailableWithoutBars() {
        val rows = MemoryRowMapper.map(MemoryInfo(null, null, null, null), StorageInfo(null, null))

        rows.forEach {
            assertEquals(ValueFormatter.unavailable, it.value)
            assertNull(it.progress)
        }
    }

    @Test
    fun usedPercent_clampsAndRounds() {
        assertEquals(33, MemoryRowMapper.usedPercent(3L, 2L))
        assertEquals(0, MemoryRowMapper.usedPercent(100L, 150L))
        assertNull(MemoryRowMapper.usedPercent(0L, 0L))
        assertNull(MemoryRowMapper.usedPercent(100L, null))
    }
}
