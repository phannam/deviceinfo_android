package vn.npstudio.deviceinfo.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
) : ViewModel() {

    private val reloads = MutableStateFlow(0)
    private var phonePermissionGranted = repository.isPhonePermissionGranted()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InfoListUiState> = reloads
        .mapLatest {
            val info = repository.getDeviceInfo()
            InfoListUiState(LoadingMode.IDLE, DeviceRowMapper.sections(info), DeviceRowMapper.hero(info))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())

    /** Reads everything again, for example after a permission was granted from a row. */
    fun refresh() {
        phonePermissionGranted = repository.isPhonePermissionGranted()
        reloads.update { it + 1 }
    }

    /** The user may grant or revoke the phone permission in system settings while the app is paused. */
    fun onResumed() {
        if (repository.isPhonePermissionGranted() != phonePermissionGranted) refresh()
    }
}
