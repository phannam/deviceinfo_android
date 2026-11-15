package vn.npstudio.deviceinfo.feature.main

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R

enum class MainTab(@param:StringRes val titleRes: Int) {
    DEVICE(R.string.tab_device),
    DISPLAY(R.string.tab_display),
}
