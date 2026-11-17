package vn.npstudio.deviceinfo.feature.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class BatteryViewModel @Inject constructor(
    repository: BatteryRepository,
    appConfigRepository: AppConfigRepository,
) : ViewModel() {

    // The receiver and polling run only while subscribed; they stop 5 s after the tab leaves RESUMED.
    val uiState: StateFlow<InfoListUiState> = combine(
        repository.observe(),
        appConfigRepository.config.map { it.temperatureUnit }.distinctUntilChanged(),
    ) { info, unit -> InfoListUiState(LoadingMode.IDLE, BatteryRowMapper.sections(info, unit)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
