package vn.npstudio.deviceinfo.domain.repository

import vn.npstudio.deviceinfo.domain.model.DeviceInfo

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo

    /** Whether READ_PHONE_STATE is granted; the Device tab reloads when this changes. */
    fun isPhonePermissionGranted(): Boolean
}
