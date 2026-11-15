package vn.npstudio.deviceinfo.feature.screentest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.ScreenTestResultRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

/** Last result rows for the Screen Test tab. */
@HiltViewModel
class ScreenTestResultViewModel @Inject constructor(
    repository: ScreenTestResultRepository,
) : ViewModel() {

    val uiState: StateFlow<InfoListUiState> = repository.result
        .map { InfoListUiState(LoadingMode.IDLE, ScreenTestRowMapper.map(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
