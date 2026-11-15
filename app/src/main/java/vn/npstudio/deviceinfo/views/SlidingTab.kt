package vn.npstudio.deviceinfo.views

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

class SlidingTab(
    val type: Int,
    @param:DrawableRes private val iconDrawableNormal: Int,
    val name: String,
    @param:ColorInt private val textColorNormal: Int,
) {
    @DrawableRes
    private var iconDrawableSelected = 0

    @ColorInt
    private var textColorSelected = 0

    var isSelected = false

    val iconDrawable: Int
        get() = if (isSelected) iconDrawableSelected else iconDrawableNormal

    val textColor: Int
        get() = if (isSelected) textColorSelected else textColorNormal

    fun setSelectedResource(@DrawableRes iconDrawableSelected: Int, @ColorInt textColorSelected: Int) {
        this.iconDrawableSelected = iconDrawableSelected
        this.textColorSelected = textColorSelected
    }
}
