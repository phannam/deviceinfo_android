package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText

/**
 * One rounded card on an info tab: an optional header, [rows], then an optional [features] checklist.
 * [inReport] is false for sections the share report must leave out, such as device identifiers.
 */
data class InfoSection(
    val title: UiText? = null,
    @param:DrawableRes val icon: Int? = null,
    val rows: List<InfoRowUiModel> = emptyList(),
    val features: List<FeatureUiModel> = emptyList(),
    val inReport: Boolean = true,
) {
    constructor(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        rows: List<InfoRowUiModel> = emptyList(),
        features: List<FeatureUiModel> = emptyList(),
        inReport: Boolean = true,
    ) : this(UiText.res(title), icon, rows, features, inReport)
}

data class FeatureUiModel(val label: UiText, val supported: Boolean)

/** Rows for the plain-text share report; a checklist becomes Supported/Not supported rows. */
fun List<InfoSection>.reportRows(): List<InfoRowUiModel> = filter { it.inReport }.flatMap { section ->
    if (section.features.isEmpty()) {
        section.rows
    } else {
        val (supported, missing) = section.features.partition { it.supported }
        section.rows + listOf(
            InfoRowUiModel(R.string.label_features_supported, featureList(supported)),
            InfoRowUiModel(R.string.label_features_unsupported, featureList(missing)),
        )
    }
}

private fun featureList(features: List<FeatureUiModel>): UiText =
    if (features.isEmpty()) UiText.res(R.string.common_none) else UiText.Joined(features.map { it.label }, ", ")
