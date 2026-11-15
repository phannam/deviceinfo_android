package vn.npstudio.deviceinfo.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.feature.battery.BatteryRowMapper
import vn.npstudio.deviceinfo.feature.cpu.CpuRowMapper
import vn.npstudio.deviceinfo.feature.device.DeviceRowMapper
import vn.npstudio.deviceinfo.feature.display.DisplayRowMapper
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val cpuRepository: CpuRepository,
    private val batteryRepository: BatteryRepository,
    private val displayRepository: DisplayRepository,
) : ViewModel() {

    val selectedTab: StateFlow<MainTab> = savedStateHandle.getStateFlow(KEY_SELECTED_TAB, MainTab.DEVICE)

    private val _effects = Channel<MainUiEffect>(Channel.BUFFERED)
    val effects: Flow<MainUiEffect> = _effects.receiveAsFlow()

    fun onTabSelected(tab: MainTab) {
        savedStateHandle[KEY_SELECTED_TAB] = tab
    }

    fun onShareClicked() {
        viewModelScope.launch {
            val sections = listOf(
                ReportSection(MainTab.DEVICE.titleRes, DeviceRowMapper.map(deviceInfoRepository.getDeviceInfo())),
                ReportSection(
                    MainTab.CPU.titleRes,
                    CpuRowMapper.map(cpuRepository.getCpuInfo(), cpuRepository.observeFrequencies().first()),
                ),
                ReportSection(MainTab.BATTERY.titleRes, BatteryRowMapper.map(batteryRepository.observe().first())),
                ReportSection(MainTab.DISPLAY.titleRes, DisplayRowMapper.map(displayRepository.getDisplayInfo())),
            )
            _effects.send(MainUiEffect.Share(sections))
        }
    }

    private companion object {
        const val KEY_SELECTED_TAB = "selected_tab"
    }
}
