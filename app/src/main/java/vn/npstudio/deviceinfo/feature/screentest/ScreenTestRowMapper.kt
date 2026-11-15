package vn.npstudio.deviceinfo.feature.screentest

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.domain.model.ScreenDefect
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

object ScreenTestRowMapper {

    /** One row per defect: Yes/No from [result], or Not tested when there is no result. */
    fun map(result: ScreenTestResult?): List<InfoRowUiModel> = ScreenDefect.entries.map { defect ->
        InfoRowUiModel(
            labelRes(defect),
            if (result == null) UiText.res(R.string.screen_test_not_tested) else ValueFormatter.yesNo(defect in result.defects),
        )
    }

    @StringRes
    fun labelRes(defect: ScreenDefect): Int = when (defect) {
        ScreenDefect.DEAD_OR_STUCK_PIXELS -> R.string.label_screen_test_dead_pixels
        ScreenDefect.LINES_OR_BANDING -> R.string.label_screen_test_lines
        ScreenDefect.BURN_IN_OR_DISCOLORATION -> R.string.label_screen_test_burn_in
    }
}
