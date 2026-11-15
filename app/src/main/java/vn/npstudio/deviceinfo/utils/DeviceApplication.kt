package vn.npstudio.deviceinfo.utils

import android.app.Application

class DeviceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppManager.create(this)
    }
}
