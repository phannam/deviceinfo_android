package vn.npstudio.deviceinfo.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.npstudio.deviceinfo.domain.model.GuardedValue
import java.net.NetworkInterface
import javax.inject.Inject

/** Identifiers that need no runtime permission. Never throws. */
interface DeviceIdentifierDataSource {
    fun deviceName(): String?
    fun androidId(): String?
    fun gsfId(): String?

    /** Blocking IPC to Google Play services; call on the IO dispatcher. */
    fun advertisingId(): String?
    fun wifiMac(): GuardedValue<String>
    fun bluetoothMac(): GuardedValue<String>
    fun usbDebugging(): Boolean?
    fun developerOptions(): Boolean?
}

// This app exists to show hardware identifiers.
@SuppressLint("HardwareIds")
class DeviceIdentifierDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DeviceIdentifierDataSource {

    override fun deviceName(): String? = safe {
        Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    } ?: safe { Settings.Secure.getString(context.contentResolver, BLUETOOTH_NAME) }

    override fun androidId(): String? = safe {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun gsfId(): String? = safe {
        context.contentResolver.query(GSF_URI, null, null, arrayOf(GSF_ID_KEY), null)?.use { cursor ->
            if (cursor.moveToFirst() && cursor.columnCount >= 2) {
                cursor.getString(1)?.toLongOrNull()?.let { java.lang.Long.toHexString(it).uppercase() }
            } else {
                null
            }
        }
    }

    override fun advertisingId(): String? = safe { AdvertisingIdClient.getAdvertisingIdInfo(context).id }

    // Apps targeting Android 11+ get no MAC from NetworkInterface; older versions may return the real one.
    override fun wifiMac(): GuardedValue<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return GuardedValue.Restricted
        val mac = safe {
            NetworkInterface.getByName(WIFI_INTERFACE)?.hardwareAddress
                ?.joinToString(":") { "%02X".format(it) }
        }
        return when (mac) {
            null -> GuardedValue.Unavailable
            PLACEHOLDER_MAC -> GuardedValue.Restricted
            else -> GuardedValue.Available(mac)
        }
    }

    // Android 6+ returns 02:00:00:00:00:00 to apps without LOCAL_MAC_ADDRESS (system only).
    override fun bluetoothMac(): GuardedValue<String> = GuardedValue.Restricted

    override fun usbDebugging(): Boolean? = globalFlag(Settings.Global.ADB_ENABLED)

    override fun developerOptions(): Boolean? = globalFlag(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)

    private fun globalFlag(name: String): Boolean? = safe {
        Settings.Global.getInt(context.contentResolver, name, 0) == 1
    }

    private inline fun <T> safe(read: () -> T?): T? = try {
        read()?.takeUnless { it is String && it.isBlank() }
    } catch (_: Exception) {
        null
    }

    private companion object {
        val GSF_URI: Uri = Uri.parse("content://com.google.android.gsf.gservices")
        const val GSF_ID_KEY = "android_id"
        const val BLUETOOTH_NAME = "bluetooth_name"
        const val WIFI_INTERFACE = "wlan0"
        const val PLACEHOLDER_MAC = "02:00:00:00:00:00"
    }
}
