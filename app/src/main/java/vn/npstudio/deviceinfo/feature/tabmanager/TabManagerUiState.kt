package vn.npstudio.deviceinfo.feature.tabmanager

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.feature.main.MainTab

/** [toggleEnabled] is false for the last visible tab: at least one tab must stay on. */
data class TabManagerItem(
    val id: TabId,
    @param:StringRes val title: Int,
    val visible: Boolean,
    val toggleEnabled: Boolean,
)

data class TabManagerUiState(val items: List<TabManagerItem>) {
    companion object {
        fun from(tabs: TabPreferences): TabManagerUiState {
            val lastVisible = tabs.visible.singleOrNull()
            return TabManagerUiState(
                tabs.order.map { id ->
                    TabManagerItem(
                        id = id,
                        title = MainTab.of(id).titleRes,
                        visible = id !in tabs.hidden,
                        toggleEnabled = id != lastVisible,
                    )
                },
            )
        }
    }
}

sealed interface TabManagerUiEffect {
    data object Dismiss : TabManagerUiEffect
}
