package vn.npstudio.deviceinfo.ui.window

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.window.layout.WindowMetricsCalculator
import vn.npstudio.deviceinfo.domain.model.WindowSnapshot

object WindowSnapshotReader {

    /** Call after [view] is laid out so root window insets are available. */
    fun read(activity: Activity, view: View): WindowSnapshot {
        val bounds = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity).bounds
        val insets = ViewCompat.getRootWindowInsets(view)
        return WindowSnapshot(
            widthPx = bounds.width(),
            heightPx = bounds.height(),
            hasCutout = insets?.let { it.displayCutout != null },
        )
    }
}
