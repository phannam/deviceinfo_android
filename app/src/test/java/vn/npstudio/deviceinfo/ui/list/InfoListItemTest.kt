package vn.npstudio.deviceinfo.ui.list

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText

class InfoListItemTest {

    private val row = InfoRowUiModel(UiText.Dynamic("Model"), UiText.Dynamic("X"))
    private val feature = FeatureUiModel(UiText.Dynamic("NFC"), supported = true)

    @Test
    fun from_headerRowsAndFeaturesShareOneCard() {
        val hero = HeroUiModel(badge = "34", badgeLabel = UiText.Dynamic("API"), title = UiText.Dynamic("Android 14"))
        val state = InfoListUiState(
            sections = listOf(
                InfoSection(UiText.Dynamic("Device"), R.drawable.ic_phone_android, listOf(row), listOf(feature)),
                InfoSection(rows = listOf(row)),
                InfoSection(UiText.Dynamic("Empty"), R.drawable.ic_code),
            ),
            hero = hero,
        )

        val items = InfoListItem.from(state)

        assertEquals(
            listOf(
                InfoListItem.Hero(hero),
                InfoListItem.Header(0, UiText.Dynamic("Device"), R.drawable.ic_phone_android, RowPosition.TOP),
                InfoListItem.Row(0, row, RowPosition.MIDDLE),
                InfoListItem.Features(0, listOf(feature), RowPosition.BOTTOM),
                InfoListItem.Row(1, row, RowPosition.SINGLE),
            ),
            items,
        )
        assertEquals(items.size, items.map { it::class to it.key }.toSet().size)
    }

    @Test
    fun reportRows_skipsHiddenSectionsAndSummarizesFeatures() {
        val sections = listOf(
            InfoSection(rows = listOf(row), inReport = false),
            InfoSection(rows = listOf(row), features = listOf(feature, feature.copy(UiText.Dynamic("UWB"), false))),
            InfoSection(features = listOf(feature)),
        )

        assertEquals(
            listOf(
                row,
                InfoRowUiModel(R.string.label_features_supported, UiText.Joined(listOf(UiText.Dynamic("NFC")), ", ")),
                InfoRowUiModel(R.string.label_features_unsupported, UiText.Joined(listOf(UiText.Dynamic("UWB")), ", ")),
                InfoRowUiModel(R.string.label_features_supported, UiText.Joined(listOf(UiText.Dynamic("NFC")), ", ")),
                InfoRowUiModel(R.string.label_features_unsupported, UiText.res(R.string.common_none)),
            ),
            sections.reportRows(),
        )
    }
}
