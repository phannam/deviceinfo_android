package vn.npstudio.deviceinfo.ui.theme

import androidx.annotation.StyleRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.AppColor

/** Maps [AppColor] to the theme overlays in `res/values/theme_overlays.xml`. */
object ThemeOverlays {

    @StyleRes
    fun primary(color: AppColor): Int = when (color) {
        AppColor.BRAND -> R.style.ThemeOverlay_DeviceInfo_Primary_Brand
        AppColor.RED -> R.style.ThemeOverlay_DeviceInfo_Primary_Red
        AppColor.PINK -> R.style.ThemeOverlay_DeviceInfo_Primary_Pink
        AppColor.PURPLE -> R.style.ThemeOverlay_DeviceInfo_Primary_Purple
        AppColor.DEEP_PURPLE -> R.style.ThemeOverlay_DeviceInfo_Primary_DeepPurple
        AppColor.INDIGO -> R.style.ThemeOverlay_DeviceInfo_Primary_Indigo
        AppColor.BLUE -> R.style.ThemeOverlay_DeviceInfo_Primary_Blue
        AppColor.TEAL -> R.style.ThemeOverlay_DeviceInfo_Primary_Teal
        AppColor.GREEN -> R.style.ThemeOverlay_DeviceInfo_Primary_Green
        AppColor.LIGHT_GREEN -> R.style.ThemeOverlay_DeviceInfo_Primary_LightGreen
        AppColor.AMBER -> R.style.ThemeOverlay_DeviceInfo_Primary_Amber
        AppColor.ORANGE -> R.style.ThemeOverlay_DeviceInfo_Primary_Orange
        AppColor.DEEP_ORANGE -> R.style.ThemeOverlay_DeviceInfo_Primary_DeepOrange
        AppColor.BLUE_GREY -> R.style.ThemeOverlay_DeviceInfo_Primary_BlueGrey
    }

    @StyleRes
    fun accent(color: AppColor): Int = when (color) {
        AppColor.BRAND -> R.style.ThemeOverlay_DeviceInfo_Accent_Brand
        AppColor.RED -> R.style.ThemeOverlay_DeviceInfo_Accent_Red
        AppColor.PINK -> R.style.ThemeOverlay_DeviceInfo_Accent_Pink
        AppColor.PURPLE -> R.style.ThemeOverlay_DeviceInfo_Accent_Purple
        AppColor.DEEP_PURPLE -> R.style.ThemeOverlay_DeviceInfo_Accent_DeepPurple
        AppColor.INDIGO -> R.style.ThemeOverlay_DeviceInfo_Accent_Indigo
        AppColor.BLUE -> R.style.ThemeOverlay_DeviceInfo_Accent_Blue
        AppColor.TEAL -> R.style.ThemeOverlay_DeviceInfo_Accent_Teal
        AppColor.GREEN -> R.style.ThemeOverlay_DeviceInfo_Accent_Green
        AppColor.LIGHT_GREEN -> R.style.ThemeOverlay_DeviceInfo_Accent_LightGreen
        AppColor.AMBER -> R.style.ThemeOverlay_DeviceInfo_Accent_Amber
        AppColor.ORANGE -> R.style.ThemeOverlay_DeviceInfo_Accent_Orange
        AppColor.DEEP_ORANGE -> R.style.ThemeOverlay_DeviceInfo_Accent_DeepOrange
        AppColor.BLUE_GREY -> R.style.ThemeOverlay_DeviceInfo_Accent_BlueGrey
    }
}
