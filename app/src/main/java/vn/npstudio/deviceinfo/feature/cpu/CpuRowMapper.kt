package vn.npstudio.deviceinfo.feature.cpu

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.CpuCore
import vn.npstudio.deviceinfo.domain.model.CpuCoreFrequency
import vn.npstudio.deviceinfo.domain.model.CpuInfo
import vn.npstudio.deviceinfo.domain.model.ThermalStatus
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

object CpuRowMapper {

    /** An empty [frequencies] list means live frequency is not readable on this device. */
    fun map(info: CpuInfo, frequencies: List<CpuCoreFrequency>): List<InfoRowUiModel> =
        staticRows(info) + liveRows(info.cores, frequencies)

    private fun staticRows(info: CpuInfo) = listOf(
        InfoRowUiModel(R.string.label_cpu_soc_manufacturer, ValueFormatter.text(info.socManufacturer)),
        InfoRowUiModel(R.string.label_cpu_soc_model, ValueFormatter.text(info.socModel)),
        InfoRowUiModel(R.string.label_cpu_abis, ValueFormatter.list(info.supportedAbis)),
        InfoRowUiModel(R.string.label_cpu_cores, ValueFormatter.number(info.cores.size)),
        InfoRowUiModel(R.string.label_cpu_clusters, clusters(info)),
        InfoRowUiModel(R.string.label_cpu_governor, ValueFormatter.list(info.governors)),
        InfoRowUiModel(R.string.label_cpu_thermal_status, thermalStatus(info.thermalStatus)),
        InfoRowUiModel(
            R.string.label_cpu_thermal_headroom,
            info.thermalHeadroom?.let { UiText.res(R.string.value_percent_decimal, it * 100f) }
                ?: ValueFormatter.unavailable,
        ),
    )

    private fun liveRows(cores: List<CpuCore>, frequencies: List<CpuCoreFrequency>): List<InfoRowUiModel> {
        if (frequencies.isEmpty()) {
            return listOf(
                InfoRowUiModel(R.string.label_cpu_live_frequency, UiText.res(R.string.value_cpu_live_unavailable)),
            )
        }
        val coresById = cores.associateBy { it.id }
        return frequencies.map { frequency -> coreRow(frequency, coresById[frequency.coreId]) }
    }

    private fun coreRow(frequency: CpuCoreFrequency, core: CpuCore?): InfoRowUiModel {
        val label = UiText.res(R.string.label_cpu_core, frequency.coreId)
        val current = frequency.currentKhz
            ?: return InfoRowUiModel(label, UiText.res(R.string.value_cpu_offline))
        val min = core?.minFreqKhz
        val max = core?.maxFreqKhz
        if (min == null || max == null || max <= min) {
            return InfoRowUiModel(label, ValueFormatter.frequencyKhz(current))
        }
        val value = UiText.res(
            R.string.value_cpu_core_frequency,
            ValueFormatter.frequencyKhz(current),
            ValueFormatter.frequencyKhz(min),
            ValueFormatter.frequencyKhz(max),
        )
        val progress = ((current - min) * 100 / (max - min)).toInt().coerceIn(0, 100)
        return InfoRowUiModel(label, value, progress)
    }

    private fun clusters(info: CpuInfo): UiText {
        if (info.clusters.isEmpty()) return ValueFormatter.unavailable
        return UiText.Joined(
            info.clusters.map {
                UiText.res(R.string.value_cpu_cluster, it.coreCount, ValueFormatter.frequencyKhz(it.maxFreqKhz))
            },
            separator = " + ",
        )
    }

    private fun thermalStatus(status: ThermalStatus?): UiText = when (status) {
        null -> ValueFormatter.unavailable
        ThermalStatus.NONE -> UiText.res(R.string.value_thermal_none)
        ThermalStatus.LIGHT -> UiText.res(R.string.value_thermal_light)
        ThermalStatus.MODERATE -> UiText.res(R.string.value_thermal_moderate)
        ThermalStatus.SEVERE -> UiText.res(R.string.value_thermal_severe)
        ThermalStatus.CRITICAL -> UiText.res(R.string.value_thermal_critical)
        ThermalStatus.EMERGENCY -> UiText.res(R.string.value_thermal_emergency)
        ThermalStatus.SHUTDOWN -> UiText.res(R.string.value_thermal_shutdown)
    }
}
