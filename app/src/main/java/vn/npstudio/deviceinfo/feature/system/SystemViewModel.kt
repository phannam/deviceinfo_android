package vn.npstudio.deviceinfo.feature.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.SystemInfoRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    repository: SystemInfoRepository,
) : ViewModel() {

    // Static info is read once per subscription; uptime ticks every second.
    val uiState: StateFlow<InfoListUiState> = combine(
        flow { emit(repository.getSystemInfo()) },
        repository.observeUptime(),
    ) { info, uptime ->
        val locale = Locale.getDefault()
        InfoListUiState(
            loadingMode = LoadingMode.IDLE,
            sections = SystemRowMapper.sections(info, uptime, locale, ZoneId.systemDefault()),
            hero = SystemRowMapper.hero(info.build, locale),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())
}
