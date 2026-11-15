package vn.npstudio.deviceinfo.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.repository.MemoryRepository
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    repository: MemoryRepository,
) : ViewModel() {

    // Each resume re-reads storage; RAM polls only while subscribed.
    private val resumes = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val uiState: StateFlow<InfoListUiState> = combine(
        repository.observeRam(),
        resumes.map { repository.getStorageInfo() },
    ) { ram, storage -> InfoListUiState(LoadingMode.IDLE, MemoryRowMapper.map(ram, storage)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoListUiState())

    fun onResumed() {
        resumes.tryEmit(Unit)
    }
}
