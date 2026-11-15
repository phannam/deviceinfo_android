package vn.npstudio.deviceinfo.ui.list

import vn.npstudio.deviceinfo.common.stateflow.LoadingMode

data class InfoListUiState(
    val loadingMode: LoadingMode = LoadingMode.INITIAL,
    val rows: List<InfoRowUiModel> = emptyList(),
)
