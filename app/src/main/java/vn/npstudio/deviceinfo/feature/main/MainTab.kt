package vn.npstudio.deviceinfo.feature.main

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.TabId

enum class MainTab(@param:StringRes val titleRes: Int, val id: TabId) {
    DEVICE(R.string.tab_device, TabId.DEVICE),
    CPU(R.string.tab_cpu, TabId.CPU),
    BATTERY(R.string.tab_battery, TabId.BATTERY),
    DISPLAY(R.string.tab_display, TabId.DISPLAY),
    MEMORY(R.string.tab_memory, TabId.MEMORY),
    SCREEN_TEST(R.string.tab_screen_test, TabId.SCREEN_TEST),
    ;

    companion object {
        fun of(id: TabId): MainTab = entries.first { it.id == id }
    }
}
