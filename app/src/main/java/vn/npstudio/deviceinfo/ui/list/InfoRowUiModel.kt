package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.common.text.UiText

/**
 * [label] must be unique within one section; DiffUtil uses it as the row identity.
 * [progress] is 0-100 and renders a progress bar under the value when not null.
 * [action] makes the row clickable (spec 003 section 4.1).
 */
data class InfoRowUiModel(
    val label: UiText,
    val value: UiText,
    val progress: Int? = null,
    val action: RowAction? = null,
) {
    constructor(@StringRes label: Int, value: UiText, progress: Int? = null, action: RowAction? = null) :
        this(UiText.res(label), value, progress, action)
}

sealed interface RowAction {
    /** Opens [entries] in a bottom sheet titled [title]. */
    data class ShowList(val title: UiText, val entries: List<ListEntryUiModel>) : RowAction

    /** Asks for the runtime [permission]; the screen reloads when it is granted. */
    data class RequestPermission(val permission: String) : RowAction
}

data class ListEntryUiModel(val title: String, val subtitle: UiText? = null)
