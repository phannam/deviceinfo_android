package vn.npstudio.deviceinfo.feature.main

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R

enum class MainTab(@param:StringRes val titleRes: Int) {
    DEVICE(R.string.tab_device),
    CPU(R.string.tab_cpu),
    BATTERY(R.string.tab_battery),
    DISPLAY(R.string.tab_display),
    MEMORY(R.string.tab_memory),
    SCREEN_TEST(R.string.tab_screen_test),
}
