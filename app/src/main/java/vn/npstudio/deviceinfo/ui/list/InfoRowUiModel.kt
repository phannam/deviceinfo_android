package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.common.text.UiText

/**
 * [label] must be unique within one list; DiffUtil uses it as the row identity.
 * [progress] is 0-100 and renders a progress bar under the value when not null.
 */
data class InfoRowUiModel(
    val label: UiText,
    val value: UiText,
    val progress: Int? = null,
) {
    constructor(@StringRes label: Int, value: UiText, progress: Int? = null) :
        this(UiText.res(label), value, progress)
}
