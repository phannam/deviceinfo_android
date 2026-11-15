package vn.npstudio.deviceinfo.testing

import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

/** Look up values with `rows[UiText.res(R.string.label_x)]`. */
fun List<InfoRowUiModel>.byLabel(): Map<UiText, UiText> = associate { it.label to it.value }
