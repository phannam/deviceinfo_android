package vn.npstudio.deviceinfo.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import vn.npstudio.deviceinfo.domain.model.PhoneType
import vn.npstudio.deviceinfo.domain.model.TelephonyInfo
import javax.inject.Inject

/** TelephonyManager and READ_PHONE_STATE gated identifiers. */
interface TelephonyDataSource {
    fun hasPhonePermission(): Boolean
    fun telephony(): TelephonyInfo
    fun imei(): GuardedValue<String>
    fun simSerial(): GuardedValue<String>
    fun subscriberId(): GuardedValue<String>
    fun hardwareSerial(): GuardedValue<String>
}

// This app exists to show hardware identifiers; every read is guarded by hasPhonePermission().
@SuppressLint("HardwareIds", "MissingPermission")
class TelephonyDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TelephonyDataSource {

    private val manager: TelephonyManager? get() = context.getSystemService(TelephonyManager::class.java)

    override fun hasPhonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    override fun telephony(): TelephonyInfo {
        val tm = manager ?: return TelephonyInfo(PhoneType.UNKNOWN, null, null, null, GuardedValue.Unavailable)
        return TelephonyInfo(
            phoneType = when (tm.phoneType) {
                TelephonyManager.PHONE_TYPE_NONE -> PhoneType.NONE
                TelephonyManager.PHONE_TYPE_GSM -> PhoneType.GSM
                TelephonyManager.PHONE_TYPE_CDMA -> PhoneType.CDMA
                TelephonyManager.PHONE_TYPE_SIP -> PhoneType.SIP
                else -> PhoneType.UNKNOWN
            },
            simSlots = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tm.activeModemCount
            } else {
                @Suppress("DEPRECATION")
                tm.phoneCount
            },
            simOperator = tm.simOperatorName?.trim()?.ifEmpty { null },
            networkOperator = tm.networkOperatorName?.trim()?.ifEmpty { null },
            networkType = phoneStateValue(restrictedFromQ = false) { networkTypeName(tm.dataNetworkType) },
        )
    }

    override fun imei(): GuardedValue<String> = phoneStateValue(restrictedFromQ = true) { manager?.imei }

    override fun simSerial(): GuardedValue<String> =
        phoneStateValue(restrictedFromQ = true) { manager?.simSerialNumber }

    override fun subscriberId(): GuardedValue<String> =
        phoneStateValue(restrictedFromQ = true) { manager?.subscriberId }

    override fun hardwareSerial(): GuardedValue<String> =
        phoneStateValue(restrictedFromQ = true) { Build.getSerial().takeUnless { it == Build.UNKNOWN } }

    /** Android 10+ keeps device identifiers for privileged apps only (spec 003 section 4.2). */
    private inline fun phoneStateValue(restrictedFromQ: Boolean, read: () -> String?): GuardedValue<String> {
        if (restrictedFromQ && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return GuardedValue.Restricted
        if (!hasPhonePermission()) return GuardedValue.NeedsPermission(Manifest.permission.READ_PHONE_STATE)
        return try {
            read()?.trim()?.ifEmpty { null }?.let { GuardedValue.Available(it) } ?: GuardedValue.Unavailable
        } catch (_: SecurityException) {
            GuardedValue.Restricted
        }
    }

    companion object {
        /** Maps `TelephonyManager.NETWORK_TYPE_*` to a display name; null for unknown. */
        fun networkTypeName(type: Int): String? = when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev. A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev. B"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            NETWORK_TYPE_NR -> "NR (5G)"
            else -> null
        }

        // TelephonyManager.NETWORK_TYPE_NR is API 29; the value is stable.
        private const val NETWORK_TYPE_NR = 20
    }
}
