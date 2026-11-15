package vn.npstudio.deviceinfo.feature.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.PowerSource
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter

class BatteryRowMapperTest {

    @Test
    fun map_formatsUnits() {
        val rows = BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.CELSIUS).byLabel()

        assertEquals(UiText.res(R.string.value_percent_int, 80), rows[UiText.res(R.string.label_battery_level)])
        assertEquals(UiText.res(R.string.value_temperature_celsius, 31.2), rows[UiText.res(R.string.label_battery_temperature)])
        assertEquals(UiText.res(R.string.value_voltage, 4.215), rows[UiText.res(R.string.label_battery_voltage)])
        assertEquals(UiText.res(R.string.value_milliamps, -450), rows[UiText.res(R.string.label_battery_current_now)])
        assertEquals(UiText.res(R.string.value_milliamp_hours, 2_640), rows[UiText.res(R.string.label_battery_charge_counter)])
        assertEquals(UiText.res(R.string.value_milliamp_hours, 3_300), rows[UiText.res(R.string.label_battery_estimated_capacity)])
        assertEquals(UiText.res(R.string.value_duration_hm, 1L, 25L), rows[UiText.res(R.string.label_battery_time_to_full)])
        assertEquals(UiText.res(R.string.common_off), rows[UiText.res(R.string.label_battery_saver)])
        assertEquals(UiText.res(R.string.common_yes), rows[UiText.res(R.string.label_battery_present)])
    }

    @Test
    fun map_fahrenheitConvertsTemperature() {
        val value = BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.FAHRENHEIT)
            .byLabel()[UiText.res(R.string.label_battery_temperature)] as UiText.Resource

        assertEquals(R.string.value_temperature_fahrenheit, value.resId)
        assertEquals(88.16, value.args.single() as Double, 1e-9)
    }

    @Test
    fun map_levelRowHasProgress() {
        val level = BatteryRowMapper.map(sampleBatteryInfo, TemperatureUnit.CELSIUS).first()

        assertEquals(UiText.res(R.string.label_battery_level), level.label)
        assertEquals(80, level.progress)
    }

    @Test
    fun map_enumValues() {
        val info = sampleBatteryInfo.copy(
            status = BatteryStatus.NOT_CHARGING,
            powerSource = PowerSource.USB,
            health = BatteryHealth.OVER_VOLTAGE,
        )
        val rows = BatteryRowMapper.map(info, TemperatureUnit.CELSIUS).byLabel()

        assertEquals(UiText.res(R.string.value_battery_status_not_charging), rows[UiText.res(R.string.label_battery_status)])
        assertEquals(UiText.res(R.string.value_power_source_usb), rows[UiText.res(R.string.label_battery_power_source)])
        assertEquals(UiText.res(R.string.value_battery_health_over_voltage), rows[UiText.res(R.string.label_battery_health)])
    }

    @Test
    fun map_nullValuesAreUnavailable() {
        val info = sampleBatteryInfo.copy(
            levelPercent = null,
            powerSource = null,
            currentAverageMicroamps = null,
            cycleCount = null,
            powerSaveMode = null,
        )
        val rows = BatteryRowMapper.map(info, TemperatureUnit.CELSIUS).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_level)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_power_source)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_current_average)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_cycle_count)])
        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_saver)])
    }

    @Test
    fun timeToFull_onlyWhileCharging() {
        val discharging = sampleBatteryInfo.copy(status = BatteryStatus.DISCHARGING)
        val rows = BatteryRowMapper.map(discharging, TemperatureUnit.CELSIUS).byLabel()

        assertEquals(ValueFormatter.unavailable, rows[UiText.res(R.string.label_battery_time_to_full)])
    }

    @Test
    fun estimatedCapacity_requiresCounterAndLevelAtLeastTen() {
        assertEquals(3_300, BatteryRowMapper.estimatedCapacityMah(sampleBatteryInfo))
        assertEquals(26_400, BatteryRowMapper.estimatedCapacityMah(sampleBatteryInfo.copy(levelPercent = 10)))
        assertNull(BatteryRowMapper.estimatedCapacityMah(sampleBatteryInfo.copy(levelPercent = 9)))
        assertNull(BatteryRowMapper.estimatedCapacityMah(sampleBatteryInfo.copy(chargeCounterMicroampHours = null)))
    }
}
