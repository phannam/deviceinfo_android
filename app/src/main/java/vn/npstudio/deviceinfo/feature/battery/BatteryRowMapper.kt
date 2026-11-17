package vn.npstudio.deviceinfo.feature.battery

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.PowerSource
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import vn.npstudio.deviceinfo.ui.list.InfoSection
import vn.npstudio.deviceinfo.ui.list.reportRows

object BatteryRowMapper {

    /** Below this level the charge counter is too coarse to estimate full capacity. */
    private const val MIN_LEVEL_FOR_CAPACITY = 10

    fun sections(info: BatteryInfo, unit: TemperatureUnit): List<InfoSection> = listOf(
        InfoSection(R.string.section_battery_status, R.drawable.ic_battery_std, statusRows(info, unit)),
        InfoSection(R.string.section_electrical, R.drawable.ic_flash_on, electricalRows(info)),
    )

    fun map(info: BatteryInfo, unit: TemperatureUnit): List<InfoRowUiModel> = sections(info, unit).reportRows()

    private fun statusRows(info: BatteryInfo, unit: TemperatureUnit) = listOf(
        InfoRowUiModel(
            R.string.label_battery_level,
            info.levelPercent?.let { UiText.res(R.string.value_percent_int, it) } ?: ValueFormatter.unavailable,
            info.levelPercent,
        ),
        InfoRowUiModel(R.string.label_battery_status, status(info.status)),
        InfoRowUiModel(R.string.label_battery_power_source, powerSource(info.powerSource)),
        InfoRowUiModel(R.string.label_battery_health, health(info.health)),
        InfoRowUiModel(
            R.string.label_battery_temperature,
            info.temperatureTenthsCelsius?.let { temperature(it, unit) } ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(R.string.label_battery_technology, ValueFormatter.text(info.technology)),
        InfoRowUiModel(R.string.label_battery_present, ValueFormatter.yesNo(info.present)),
        InfoRowUiModel(
            R.string.label_battery_cycle_count,
            info.cycleCount?.let(ValueFormatter::number) ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(R.string.label_battery_saver, ValueFormatter.onOff(info.powerSaveMode)),
    )

    private fun electricalRows(info: BatteryInfo) = listOf(
        InfoRowUiModel(
            R.string.label_battery_voltage,
            info.voltageMillivolts?.let { UiText.res(R.string.value_voltage, it / 1_000.0) }
                ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(R.string.label_battery_current_now, milliamps(info.currentNowMicroamps)),
        InfoRowUiModel(R.string.label_battery_current_average, milliamps(info.currentAverageMicroamps)),
        InfoRowUiModel(
            R.string.label_battery_charge_counter,
            info.chargeCounterMicroampHours?.let { UiText.res(R.string.value_milliamp_hours, it / 1_000) }
                ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(
            R.string.label_battery_estimated_capacity,
            estimatedCapacityMah(info)?.let { UiText.res(R.string.value_milliamp_hours, it) }
                ?: ValueFormatter.unavailable,
        ),
        InfoRowUiModel(
            R.string.label_battery_time_to_full,
            info.chargeTimeRemainingMillis
                ?.takeIf { info.status == BatteryStatus.CHARGING }
                ?.let(ValueFormatter::hoursMinutes)
                ?: ValueFormatter.unavailable,
        ),
    )

    /** Charge counter scaled to 100 %, in mAh. */
    fun estimatedCapacityMah(info: BatteryInfo): Int? {
        val counter = info.chargeCounterMicroampHours ?: return null
        val level = info.levelPercent ?: return null
        if (counter <= 0 || level < MIN_LEVEL_FOR_CAPACITY) return null
        // µAh / 1000 * 100 / level
        return (counter.toLong() / 10 / level).toInt()
    }

    private fun temperature(tenthsCelsius: Int, unit: TemperatureUnit): UiText {
        val celsius = tenthsCelsius / 10.0
        return when (unit) {
            TemperatureUnit.CELSIUS -> UiText.res(R.string.value_temperature_celsius, celsius)
            TemperatureUnit.FAHRENHEIT -> UiText.res(R.string.value_temperature_fahrenheit, celsius * 9 / 5 + 32)
        }
    }

    private fun milliamps(microamps: Int?): UiText =
        microamps?.let { UiText.res(R.string.value_milliamps, it / 1_000) } ?: ValueFormatter.unavailable

    private fun status(status: BatteryStatus): UiText = UiText.res(
        when (status) {
            BatteryStatus.CHARGING -> R.string.value_battery_status_charging
            BatteryStatus.DISCHARGING -> R.string.value_battery_status_discharging
            BatteryStatus.FULL -> R.string.value_battery_status_full
            BatteryStatus.NOT_CHARGING -> R.string.value_battery_status_not_charging
            BatteryStatus.UNKNOWN -> R.string.value_battery_status_unknown
        },
    )

    private fun powerSource(source: PowerSource?): UiText = when (source) {
        null -> ValueFormatter.unavailable
        PowerSource.AC -> UiText.res(R.string.value_power_source_ac)
        PowerSource.USB -> UiText.res(R.string.value_power_source_usb)
        PowerSource.WIRELESS -> UiText.res(R.string.value_power_source_wireless)
        PowerSource.DOCK -> UiText.res(R.string.value_power_source_dock)
        PowerSource.NONE -> UiText.res(R.string.value_power_source_none)
    }

    private fun health(health: BatteryHealth): UiText = UiText.res(
        when (health) {
            BatteryHealth.GOOD -> R.string.value_battery_health_good
            BatteryHealth.OVERHEAT -> R.string.value_battery_health_overheat
            BatteryHealth.DEAD -> R.string.value_battery_health_dead
            BatteryHealth.OVER_VOLTAGE -> R.string.value_battery_health_over_voltage
            BatteryHealth.FAILURE -> R.string.value_battery_health_failure
            BatteryHealth.COLD -> R.string.value_battery_health_cold
            BatteryHealth.UNKNOWN -> R.string.value_battery_health_unknown
        },
    )
}
