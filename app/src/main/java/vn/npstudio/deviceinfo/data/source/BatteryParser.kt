package vn.npstudio.deviceinfo.data.source

import android.os.BatteryManager
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryInfo
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.PowerSource

/** Pure normalization of raw battery values into [BatteryInfo]. */
object BatteryParser {

    /** `BatteryManager.BATTERY_PLUGGED_DOCK`, added in API 33. */
    private const val PLUGGED_DOCK = 8

    fun parse(broadcast: BatteryBroadcast, properties: BatteryProperties): BatteryInfo = BatteryInfo(
        levelPercent = levelPercent(broadcast.level, broadcast.scale),
        status = status(broadcast.status),
        powerSource = powerSource(broadcast.plugged),
        health = health(broadcast.health),
        // -2730 (absolute zero) and below is a missing or broken sensor.
        temperatureTenthsCelsius = broadcast.temperature.takeIf { it > -2730 },
        voltageMillivolts = broadcast.voltage.takeIf { it > 0 },
        technology = broadcast.technology?.trim()?.ifEmpty { null },
        present = broadcast.present,
        cycleCount = broadcast.cycleCount.takeIf { it >= 0 },
        currentNowMicroamps = current(properties.currentNow),
        currentAverageMicroamps = current(properties.currentAverage),
        chargeCounterMicroampHours = properties.chargeCounter.takeIf { it > 0 },
        chargeTimeRemainingMillis = properties.chargeTimeRemainingMillis.takeIf { it >= 0 },
        powerSaveMode = properties.powerSaveMode,
    )

    fun levelPercent(level: Int, scale: Int): Int? =
        if (level < 0 || scale <= 0) null else (level * 100 / scale).coerceIn(0, 100)

    /** `Int.MIN_VALUE` means unsupported; `0` is what many devices report when unsupported. */
    fun current(raw: Int): Int? = raw.takeUnless { it == Int.MIN_VALUE || it == 0 }

    fun status(raw: Int): BatteryStatus = when (raw) {
        BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
        BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
        BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
        else -> BatteryStatus.UNKNOWN
    }

    fun powerSource(raw: Int): PowerSource? = when (raw) {
        0 -> PowerSource.NONE
        BatteryManager.BATTERY_PLUGGED_AC -> PowerSource.AC
        BatteryManager.BATTERY_PLUGGED_USB -> PowerSource.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> PowerSource.WIRELESS
        PLUGGED_DOCK -> PowerSource.DOCK
        else -> null
    }

    fun health(raw: Int): BatteryHealth = when (raw) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.FAILURE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
        else -> BatteryHealth.UNKNOWN
    }
}
