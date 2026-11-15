package vn.npstudio.deviceinfo.feature.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.model.WindowSnapshot
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class DisplayViewModel @Inject constructor(
    repository: DisplayRepository,
) : ViewModel() {

    // A SharedFlow (not StateFlow) so an equal snapshot after recreation still reloads display values.
    private val windowSnapshots = MutableSharedFlow<WindowSnapshot>(replay = 1)

    val uiState: StateFlow<InfoListUiState> = windowSnapshots
        .map { window -> InfoListUiState(LoadingMode.IDLE, DisplayRowMapper.map(repository.getDisplayInfo(), window)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())

    fun onWindowMeasured(window: WindowSnapshot) {
        windowSnapshots.tryEmit(window)
    }
}
