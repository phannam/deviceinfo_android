package vn.npstudio.deviceinfo.ui.list

import vn.npstudio.deviceinfo.common.stateflow.LoadingMode

data class InfoListUiState(
    val loadingMode: LoadingMode = LoadingMode.INITIAL,
    val sections: List<InfoSection> = emptyList(),
    val hero: HeroUiModel? = null,
) {
    /** Every row in display order, without checklists. */
    val rows: List<InfoRowUiModel> get() = sections.flatMap { it.rows }
}
