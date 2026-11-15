package vn.npstudio.deviceinfo.ui.theme

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.AppColor

/**
 * Maps [AppColor] to the theme overlays in `res/values/theme_overlays.xml`, the swatch colors in
 * `palette.xml` / `colors.xml` (night variants resolve automatically), and the color names.
 */
object ThemeOverlays {

    @StyleRes
    fun primary(color: AppColor): Int = resources(color).primaryOverlay

    @StyleRes
    fun accent(color: AppColor): Int = resources(color).accentOverlay

    @StyleRes
    fun overlay(role: ColorRole, color: AppColor): Int = when (role) {
        ColorRole.PRIMARY -> primary(color)
        ColorRole.ACCENT -> accent(color)
    }

    @ColorRes
    fun swatch(role: ColorRole, color: AppColor): Int = resources(color).let {
        if (role == ColorRole.PRIMARY) it.primarySwatch else it.accentSwatch
    }

    @ColorRes
    fun onSwatch(role: ColorRole, color: AppColor): Int = resources(color).let {
        if (role == ColorRole.PRIMARY) it.primaryOnSwatch else it.accentOnSwatch
    }

    @StringRes
    fun name(color: AppColor): Int = resources(color).name

    private class ColorResources(
        @param:StyleRes val primaryOverlay: Int,
        @param:StyleRes val accentOverlay: Int,
        @param:ColorRes val primarySwatch: Int,
        @param:ColorRes val primaryOnSwatch: Int,
        @param:ColorRes val accentSwatch: Int,
        @param:ColorRes val accentOnSwatch: Int,
        @param:StringRes val name: Int,
    )

    /** Palette colors look the same in both roles; only BRAND differs (brand primary vs brand accent). */
    private fun palette(
        @StyleRes primaryOverlay: Int,
        @StyleRes accentOverlay: Int,
        @ColorRes swatch: Int,
        @ColorRes onSwatch: Int,
        @StringRes name: Int,
    ) = ColorResources(primaryOverlay, accentOverlay, swatch, onSwatch, swatch, onSwatch, name)

    private fun resources(color: AppColor): ColorResources = when (color) {
        AppColor.BRAND -> ColorResources(
            primaryOverlay = R.style.ThemeOverlay_DeviceInfo_Primary_Brand,
            accentOverlay = R.style.ThemeOverlay_DeviceInfo_Accent_Brand,
            primarySwatch = R.color.brand_primary,
            primaryOnSwatch = R.color.brand_on_primary,
            accentSwatch = R.color.brand_accent,
            accentOnSwatch = R.color.brand_on_accent,
            name = R.string.color_brand,
        )
        AppColor.RED -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Red,
            R.style.ThemeOverlay_DeviceInfo_Accent_Red,
            R.color.palette_red,
            R.color.palette_red_on,
            R.string.color_red,
        )
        AppColor.PINK -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Pink,
            R.style.ThemeOverlay_DeviceInfo_Accent_Pink,
            R.color.palette_pink,
            R.color.palette_pink_on,
            R.string.color_pink,
        )
        AppColor.PURPLE -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Purple,
            R.style.ThemeOverlay_DeviceInfo_Accent_Purple,
            R.color.palette_purple,
            R.color.palette_purple_on,
            R.string.color_purple,
        )
        AppColor.DEEP_PURPLE -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_DeepPurple,
            R.style.ThemeOverlay_DeviceInfo_Accent_DeepPurple,
            R.color.palette_deep_purple,
            R.color.palette_deep_purple_on,
            R.string.color_deep_purple,
        )
        AppColor.INDIGO -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Indigo,
            R.style.ThemeOverlay_DeviceInfo_Accent_Indigo,
            R.color.palette_indigo,
            R.color.palette_indigo_on,
            R.string.color_indigo,
        )
        AppColor.BLUE -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Blue,
            R.style.ThemeOverlay_DeviceInfo_Accent_Blue,
            R.color.palette_blue,
            R.color.palette_blue_on,
            R.string.color_blue,
        )
        AppColor.TEAL -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Teal,
            R.style.ThemeOverlay_DeviceInfo_Accent_Teal,
            R.color.palette_teal,
            R.color.palette_teal_on,
            R.string.color_teal,
        )
        AppColor.GREEN -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Green,
            R.style.ThemeOverlay_DeviceInfo_Accent_Green,
            R.color.palette_green,
            R.color.palette_green_on,
            R.string.color_green,
        )
        AppColor.LIGHT_GREEN -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_LightGreen,
            R.style.ThemeOverlay_DeviceInfo_Accent_LightGreen,
            R.color.palette_light_green,
            R.color.palette_light_green_on,
            R.string.color_light_green,
        )
        AppColor.AMBER -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Amber,
            R.style.ThemeOverlay_DeviceInfo_Accent_Amber,
            R.color.palette_amber,
            R.color.palette_amber_on,
            R.string.color_amber,
        )
        AppColor.ORANGE -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_Orange,
            R.style.ThemeOverlay_DeviceInfo_Accent_Orange,
            R.color.palette_orange,
            R.color.palette_orange_on,
            R.string.color_orange,
        )
        AppColor.DEEP_ORANGE -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_DeepOrange,
            R.style.ThemeOverlay_DeviceInfo_Accent_DeepOrange,
            R.color.palette_deep_orange,
            R.color.palette_deep_orange_on,
            R.string.color_deep_orange,
        )
        AppColor.BLUE_GREY -> palette(
            R.style.ThemeOverlay_DeviceInfo_Primary_BlueGrey,
            R.style.ThemeOverlay_DeviceInfo_Accent_BlueGrey,
            R.color.palette_blue_grey,
            R.color.palette_blue_grey_on,
            R.string.color_blue_grey,
        )
    }
}
