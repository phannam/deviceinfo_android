package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.common.text.UiText

/** [progress] is 0-100 and renders a progress bar under the value when not null. */
data class InfoRowUiModel(
    @param:StringRes val label: Int,
    val value: UiText,
    val progress: Int? = null,
)
