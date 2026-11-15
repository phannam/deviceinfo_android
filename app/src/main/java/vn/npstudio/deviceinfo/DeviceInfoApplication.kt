package vn.npstudio.deviceinfo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import vn.npstudio.deviceinfo.ui.theme.ThemeApplier
import javax.inject.Inject

@HiltAndroidApp
class DeviceInfoApplication : Application() {

    @Inject
    lateinit var themeApplier: ThemeApplier

    override fun onCreate() {
        super.onCreate()
        themeApplier.install(this)
    }
}
