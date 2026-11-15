package vn.npstudio.deviceinfo.domain.repository

import vn.npstudio.deviceinfo.domain.model.DisplayInfo

interface DisplayRepository {
    suspend fun getDisplayInfo(): DisplayInfo
}
