package vn.npstudio.deviceinfo.feature.cpu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class CpuViewModel @Inject constructor(
    repository: CpuRepository,
) : ViewModel() {

    // Polling runs only while subscribed; it stops 5 s after the tab leaves RESUMED.
    val uiState: StateFlow<InfoListUiState> = flow {
        val info = repository.getCpuInfo()
        emitAll(
            repository.observeFrequencies().map { frequencies ->
                InfoListUiState(LoadingMode.IDLE, CpuRowMapper.sections(info, frequencies))
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
