package vn.npstudio.deviceinfo.domain.repository

import vn.npstudio.deviceinfo.domain.model.DeviceInfo

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}
