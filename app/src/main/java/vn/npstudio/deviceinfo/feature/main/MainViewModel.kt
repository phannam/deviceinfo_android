package vn.npstudio.deviceinfo.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.model.WindowSnapshot
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.domain.repository.MemoryRepository
import vn.npstudio.deviceinfo.domain.repository.ScreenTestResultRepository
import vn.npstudio.deviceinfo.domain.repository.SystemInfoRepository
import vn.npstudio.deviceinfo.feature.battery.BatteryRowMapper
import vn.npstudio.deviceinfo.feature.cpu.CpuRowMapper
import vn.npstudio.deviceinfo.feature.device.DeviceRowMapper
import vn.npstudio.deviceinfo.feature.display.DisplayRowMapper
import vn.npstudio.deviceinfo.feature.memory.MemoryRowMapper
import vn.npstudio.deviceinfo.feature.screentest.ScreenTestRowMapper
import vn.npstudio.deviceinfo.feature.system.SystemRowMapper
import vn.npstudio.deviceinfo.ui.list.reportRows
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val systemInfoRepository: SystemInfoRepository,
    private val cpuRepository: CpuRepository,
    private val batteryRepository: BatteryRepository,
    private val displayRepository: DisplayRepository,
    private val memoryRepository: MemoryRepository,
    private val screenTestResultRepository: ScreenTestResultRepository,
    private val appConfigRepository: AppConfigRepository,
) : ViewModel() {

    val selectedTab: StateFlow<MainTab> = savedStateHandle.getStateFlow(KEY_SELECTED_TAB, MainTab.DEVICE)

    /** Tabs the user keeps visible, in the user's order (spec 002 section 4.5). */
    val visibleTabs: StateFlow<List<MainTab>> = appConfigRepository.tabs
        .map(::toMainTabs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), toMainTabs(appConfigRepository.tabs.value))

    private val _effects = Channel<MainUiEffect>(Channel.BUFFERED)
    val effects: Flow<MainUiEffect> = _effects.receiveAsFlow()

    fun onTabSelected(tab: MainTab) {
        savedStateHandle[KEY_SELECTED_TAB] = tab
    }

    /** Returns the saved tab if it is in [tabs], otherwise the first of [tabs], and stores the result. */
    fun resolveSelectedTab(tabs: List<MainTab>): MainTab {
        val tab = selectedTab.value.takeIf { it in tabs } ?: tabs.first()
        onTabSelected(tab)
        return tab
    }

    /**
     * [window] is measured by the Activity so the report includes window size and cutout.
     * The report always has all sections in default order, whatever the tab settings.
     * Device identifiers are left out (spec 003 section 6).
     */
    fun onShareClicked(window: WindowSnapshot?) {
        viewModelScope.launch {
            val sections = listOf(
                ReportSection(MainTab.DEVICE.titleRes, DeviceRowMapper.map(deviceInfoRepository.getDeviceInfo())),
                ReportSection(MainTab.SYSTEM.titleRes, systemRows()),
                ReportSection(
                    MainTab.CPU.titleRes,
                    CpuRowMapper.map(cpuRepository.getCpuInfo(), cpuRepository.observeFrequencies().first()),
                ),
                ReportSection(
                    MainTab.BATTERY.titleRes,
                    BatteryRowMapper.map(
                        batteryRepository.observe().first(),
                        appConfigRepository.config.value.temperatureUnit,
                    ),
                ),
                ReportSection(MainTab.DISPLAY.titleRes, DisplayRowMapper.map(displayRepository.getDisplayInfo(), window)),
                ReportSection(
                    MainTab.MEMORY.titleRes,
                    MemoryRowMapper.map(memoryRepository.observeRam().first(), memoryRepository.getStorageInfo()),
                ),
                ReportSection(MainTab.SCREEN_TEST.titleRes, ScreenTestRowMapper.map(screenTestResultRepository.result.value)),
            )
            _effects.send(MainUiEffect.Share(sections))
        }
    }

    private suspend fun systemRows() = SystemRowMapper.sections(
        systemInfoRepository.getSystemInfo(),
        systemInfoRepository.observeUptime().first(),
        Locale.getDefault(),
        ZoneId.systemDefault(),
    ).reportRows()

    private fun toMainTabs(tabs: TabPreferences): List<MainTab> = tabs.visible.map(MainTab::of)

    private companion object {
        const val KEY_SELECTED_TAB = "selected_tab"
    }
}
