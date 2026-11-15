package vn.npstudio.deviceinfo.domain.model

/** Battery snapshot. Null fields mean the platform did not report a usable value. */
data class BatteryInfo(
    val levelPercent: Int?,
    val status: BatteryStatus,
    /** Null when the plug type is not a known value. */
    val powerSource: PowerSource?,
    val health: BatteryHealth,
    val temperatureTenthsCelsius: Int?,
    val voltageMillivolts: Int?,
    val technology: String?,
    val present: Boolean?,
    val cycleCount: Int?,
    /** Signed microamperes per the API contract; some OEMs report mA or flip the sign. */
    val currentNowMicroamps: Int?,
    val currentAverageMicroamps: Int?,
    val chargeCounterMicroampHours: Int?,
    val chargeTimeRemainingMillis: Long?,
    val powerSaveMode: Boolean?,
)

enum class BatteryStatus { CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN }

enum class PowerSource { AC, USB, WIRELESS, DOCK, NONE }

enum class BatteryHealth { GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, FAILURE, COLD, UNKNOWN }
