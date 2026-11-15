package vn.npstudio.deviceinfo.feature.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class DisplayViewModel @Inject constructor(
    repository: DisplayRepository,
) : ViewModel() {

    val uiState: StateFlow<InfoListUiState> = flow {
        emit(InfoListUiState(LoadingMode.IDLE, DisplayRowMapper.map(repository.getDisplayInfo())))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
