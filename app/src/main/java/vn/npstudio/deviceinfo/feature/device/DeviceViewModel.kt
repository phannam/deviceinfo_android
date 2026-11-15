package vn.npstudio.deviceinfo.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    repository: DeviceInfoRepository,
) : ViewModel() {

    // Re-read on each new subscription so uptime is fresh when the tab comes back.
    val uiState: StateFlow<InfoListUiState> = flow {
        emit(InfoListUiState(LoadingMode.IDLE, DeviceRowMapper.map(repository.getDeviceInfo())))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
