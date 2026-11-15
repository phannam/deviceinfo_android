package vn.npstudio.deviceinfo.feature.cpu

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.ThermalStatus
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleCpuInfo
import vn.npstudio.deviceinfo.testing.sampleFrequencies
import vn.npstudio.deviceinfo.ui.format.ValueFormatter

class CpuRowMapperTest {

    @Test
    fun map_formatsStaticRows() {
        val info = sampleCpuInfo.copy(thermalStatus = ThermalStatus.MODERATE, thermalHeadroom = 0.5f)
        val rows = CpuRowMapper.map(info, sampleFrequencies).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_cpu_soc_manufacturer)])
        assertEquals(UiText.Dynamic("arm64-v8a, armeabi-v7a, armeabi"), rows[UiText.res(R.string.label_cpu_abis)])
        assertEquals(UiText.Dynamic("8"), rows[UiText.res(R.string.label_cpu_cores)])
        assertEquals(
            UiText.Joined(
                listOf(
                    UiText.res(R.string.value_cpu_cluster, 4, UiText.res(R.string.value_frequency_ghz, 1.9008)),
                    UiText.res(R.string.value_cpu_cluster, 4, UiText.res(R.string.value_frequency_ghz, 2.3616)),
                ),
                " + ",
            ),
            rows[UiText.res(R.string.label_cpu_clusters)],
        )
        assertEquals(UiText.res(R.string.value_thermal_moderate), rows[UiText.res(R.string.label_cpu_thermal_status)])
        assertEquals(UiText.res(R.string.value_percent_decimal, 50f), rows[UiText.res(R.string.label_cpu_thermal_headroom)])
    }

    @Test
    fun map_showsCoreFrequencyWithProgressAndOfflineCores() {
        val rows = CpuRowMapper.map(sampleCpuInfo, sampleFrequencies)
        val core0 = rows.single { it.label == UiText.res(R.string.label_cpu_core, 0) }
        val core7 = rows.single { it.label == UiText.res(R.string.label_cpu_core, 7) }

        assertEquals(
            UiText.res(
                R.string.value_cpu_core_frequency,
                UiText.res(R.string.value_frequency_ghz, 1.1004),
                UiText.res(R.string.value_frequency_mhz, 300L),
                UiText.res(R.string.value_frequency_ghz, 1.9008),
            ),
            core0.value,
        )
        assertEquals(50, core0.progress)
        assertEquals(UiText.res(R.string.value_cpu_offline), core7.value)
        assertEquals(null, core7.progress)
    }

    @Test
    fun map_showsNoticeWhenLiveFrequencyIsUnavailable() {
        val rows = CpuRowMapper.map(sampleCpuInfo, emptyList())

        assertEquals(UiText.res(R.string.label_cpu_live_frequency), rows.last().label)
        assertEquals(UiText.res(R.string.value_cpu_live_unavailable), rows.last().value)
        assertEquals(1, rows.count { it.label == UiText.res(R.string.label_cpu_live_frequency) })
    }

    @Test
    fun map_coreWithoutRangeShowsCurrentOnly() {
        val info = sampleCpuInfo.copy(cores = emptyList())
        val rows = CpuRowMapper.map(info, listOf(CpuCoreFrequency(0, 900_000L)))

        assertEquals(UiText.res(R.string.value_frequency_mhz, 900L), rows.last().value)
        assertEquals(null, rows.last().progress)
    }

    @Test
    fun map_labelsAreUnique() {
        val labels = CpuRowMapper.map(sampleCpuInfo, sampleFrequencies).map { it.label }

        assertEquals(labels.size, labels.toSet().size)
    }
}
