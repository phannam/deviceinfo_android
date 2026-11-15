package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.DrawableRes
import vn.npstudio.deviceinfo.R

/** Where a row sits in its card group; decides the rounded corners and the divider. */
enum class RowPosition(@param:DrawableRes val backgroundRes: Int, val showsDivider: Boolean) {
    SINGLE(R.drawable.bg_row_single, false),
    TOP(R.drawable.bg_row_top, true),
    MIDDLE(R.drawable.bg_row_middle, true),
    BOTTOM(R.drawable.bg_row_bottom, false),
    ;

    companion object {
        fun of(index: Int, count: Int): RowPosition = when {
            count <= 1 -> SINGLE
            index == 0 -> TOP
            index == count - 1 -> BOTTOM
            else -> MIDDLE
        }
    }
}
