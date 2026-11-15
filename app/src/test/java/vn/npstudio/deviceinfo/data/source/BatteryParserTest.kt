package vn.npstudio.deviceinfo.data.source

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.domain.model.BatteryHealth
import vn.npstudio.deviceinfo.domain.model.BatteryStatus
import vn.npstudio.deviceinfo.domain.model.PowerSource
import vn.npstudio.deviceinfo.testing.sampleBatteryBroadcast
import vn.npstudio.deviceinfo.testing.sampleBatteryInfo
import vn.npstudio.deviceinfo.testing.sampleBatteryProperties

class BatteryParserTest {

    @Test
    fun parse_normalizesRawValues() {
        assertEquals(sampleBatteryInfo, BatteryParser.parse(sampleBatteryBroadcast, sampleBatteryProperties))
    }

    @Test
    fun levelPercent_scalesAndRejectsMissing() {
        assertEquals(50, BatteryParser.levelPercent(25, 50))
        assertNull(BatteryParser.levelPercent(-1, 100))
        assertNull(BatteryParser.levelPercent(50, 0))
    }

    @Test
    fun current_treatsMinValueAndZeroAsUnsupported() {
        assertEquals(-450_000, BatteryParser.current(-450_000))
        assertNull(BatteryParser.current(Int.MIN_VALUE))
        assertNull(BatteryParser.current(0))
    }

    @Test
    fun enums_mapPlatformConstants() {
        assertEquals(BatteryStatus.NOT_CHARGING, BatteryParser.status(BatteryManager.BATTERY_STATUS_NOT_CHARGING))
        assertEquals(BatteryStatus.UNKNOWN, BatteryParser.status(-1))
        assertEquals(PowerSource.NONE, BatteryParser.powerSource(0))
        assertEquals(PowerSource.WIRELESS, BatteryParser.powerSource(BatteryManager.BATTERY_PLUGGED_WIRELESS))
        assertEquals(PowerSource.DOCK, BatteryParser.powerSource(8))
        assertNull(BatteryParser.powerSource(-1))
        assertEquals(BatteryHealth.OVERHEAT, BatteryParser.health(BatteryManager.BATTERY_HEALTH_OVERHEAT))
        assertEquals(BatteryHealth.FAILURE, BatteryParser.health(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE))
        assertEquals(BatteryHealth.UNKNOWN, BatteryParser.health(-1))
    }

    @Test
    fun parse_missingExtrasBecomeNull() {
        val broadcast = sampleBatteryBroadcast.copy(
            temperature = Int.MIN_VALUE,
            voltage = -1,
            technology = " ",
            present = null,
            cycleCount = -1,
        )
        val properties = sampleBatteryProperties.copy(chargeCounter = 0, chargeTimeRemainingMillis = -1L)

        val info = BatteryParser.parse(broadcast, properties)

        assertNull(info.temperatureTenthsCelsius)
        assertNull(info.voltageMillivolts)
        assertNull(info.technology)
        assertNull(info.present)
        assertNull(info.cycleCount)
        assertNull(info.chargeCounterMicroampHours)
        assertNull(info.chargeTimeRemainingMillis)
    }
}
