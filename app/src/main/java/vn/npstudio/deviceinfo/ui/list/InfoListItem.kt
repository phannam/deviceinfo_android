package vn.npstudio.deviceinfo.ui.list

import androidx.annotation.DrawableRes
import vn.npstudio.deviceinfo.common.text.UiText

/** Flat adapter items; [position] places each item inside its section's card. */
sealed interface InfoListItem {
    val key: Any
    val position: RowPosition

    data class Hero(val model: HeroUiModel) : InfoListItem {
        override val key: Any get() = "hero"
        override val position: RowPosition get() = RowPosition.SINGLE
    }

    data class Header(
        val section: Int,
        val title: UiText,
        @param:DrawableRes val icon: Int?,
        override val position: RowPosition,
    ) : InfoListItem {
        override val key: Any get() = "header" to section
    }

    data class Row(val section: Int, val row: InfoRowUiModel, override val position: RowPosition) : InfoListItem {
        override val key: Any get() = section to row.label
    }

    data class Features(
        val section: Int,
        val features: List<FeatureUiModel>,
        override val position: RowPosition,
    ) : InfoListItem {
        override val key: Any get() = "features" to section
    }

    companion object {
        fun from(state: InfoListUiState): List<InfoListItem> = buildList {
            state.hero?.let { add(Hero(it)) }
            state.sections.forEachIndexed { index, section ->
                val hasHeader = section.title != null
                val hasFeatures = section.features.isNotEmpty()
                val count = (if (hasHeader) 1 else 0) + section.rows.size + (if (hasFeatures) 1 else 0)
                if (section.rows.isEmpty() && !hasFeatures) return@forEachIndexed
                var itemIndex = 0
                section.title?.let { add(Header(index, it, section.icon, RowPosition.of(itemIndex++, count))) }
                section.rows.forEach { add(Row(index, it, RowPosition.of(itemIndex++, count))) }
                if (hasFeatures) add(Features(index, section.features, RowPosition.of(itemIndex, count)))
            }
        }
    }
}
