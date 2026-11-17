package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.DrawableRes
import vn.npstudio.deviceinfo.common.text.UiText

/**
 * Large colored card at the top of a tab. The round badge shows [badgeIcon], or [badge] (a short value such
 * as the API level) with [badgeLabel] under it.
 */
data class HeroUiModel(
    val badge: String? = null,
    val badgeLabel: UiText? = null,
    @param:DrawableRes val badgeIcon: Int? = null,
    val title: UiText,
    val subtitle: UiText? = null,
    val detail: UiText? = null,
)
