package vn.npstudio.deviceinfo.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import vn.npstudio.deviceinfo.domain.model.DarkMode

object NightModeMapper {

    fun toAppCompat(mode: DarkMode): Int = when (mode) {
        DarkMode.OFF -> AppCompatDelegate.MODE_NIGHT_NO
        DarkMode.ON -> AppCompatDelegate.MODE_NIGHT_YES
        DarkMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
