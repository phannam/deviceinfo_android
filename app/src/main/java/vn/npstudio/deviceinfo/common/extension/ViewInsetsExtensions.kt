package vn.npstudio.deviceinfo.common.extension

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** Adds system bar and cutout insets to the padding the view had when this was called (left, right, bottom). */
fun View.applySystemBarPadding() {
    val initialLeft = paddingLeft
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            left = initialLeft + bars.left,
            right = initialRight + bars.right,
            bottom = initialBottom + bars.bottom,
        )
        insets
    }
}
