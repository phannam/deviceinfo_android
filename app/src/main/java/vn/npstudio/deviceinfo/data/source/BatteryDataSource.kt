package vn.npstudio.deviceinfo.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/** Raw `ACTION_BATTERY_CHANGED` extras. `-1` means the extra is missing. */
data class BatteryBroadcast(
    val level: Int,
    val scale: Int,
    val status: Int,
    val plugged: Int,
    val health: Int,
    val temperature: Int,
    val voltage: Int,
    val technology: String?,
    val present: Boolean?,
    val cycleCount: Int,
)

/** Raw `BatteryManager` / `PowerManager` values, not yet normalized. */
data class BatteryProperties(
    val currentNow: Int,
    val currentAverage: Int,
    val chargeCounter: Int,
    val chargeTimeRemainingMillis: Long,
    val powerSaveMode: Boolean?,
)

interface BatteryDataSource {
    /** Emits the sticky battery intent on collection, then every change. */
    fun batteryChanges(): Flow<BatteryBroadcast>

    fun readProperties(): BatteryProperties
}

class BatteryDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : BatteryDataSource {

    override fun batteryChanges(): Flow<BatteryBroadcast> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent.toBroadcast())
            }
        }
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        sticky?.let { trySend(it.toBroadcast()) }
        awaitClose { context.unregisterReceiver(receiver) }
    }

    override fun readProperties(): BatteryProperties {
        val batteryManager = context.getSystemService(BatteryManager::class.java)
        return BatteryProperties(
            currentNow = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: Int.MIN_VALUE,
            currentAverage = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                ?: Int.MIN_VALUE,
            chargeCounter = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                ?: Int.MIN_VALUE,
            chargeTimeRemainingMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                batteryManager?.computeChargeTimeRemaining() ?: -1L
            } else {
                -1L
            },
            powerSaveMode = context.getSystemService(PowerManager::class.java)?.isPowerSaveMode,
        )
    }

    private fun Intent.toBroadcast() = BatteryBroadcast(
        level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
        scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        status = getIntExtra(BatteryManager.EXTRA_STATUS, -1),
        plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1),
        health = getIntExtra(BatteryManager.EXTRA_HEALTH, -1),
        temperature = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE),
        voltage = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1),
        technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
        present = if (hasExtra(BatteryManager.EXTRA_PRESENT)) getBooleanExtra(BatteryManager.EXTRA_PRESENT, false) else null,
        cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
        } else {
            -1
        },
    )
}
