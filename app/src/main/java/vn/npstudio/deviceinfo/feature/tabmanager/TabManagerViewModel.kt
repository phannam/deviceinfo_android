package vn.npstudio.deviceinfo.feature.tabmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import javax.inject.Inject

/** Edits a draft of the tab settings; nothing is saved until [onSave]. */
@HiltViewModel
class TabManagerViewModel @Inject constructor(
    private val repository: AppConfigRepository,
) : ViewModel() {

    private val draft = MutableStateFlow(repository.tabs.value)

    val uiState: StateFlow<TabManagerUiState> = draft
        .map(TabManagerUiState::from)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TabManagerUiState.from(draft.value))

    private val _effects = Channel<TabManagerUiEffect>(Channel.BUFFERED)
    val effects: Flow<TabManagerUiEffect> = _effects.receiveAsFlow()

    fun onMoved(from: Int, to: Int) {
        draft.update { tabs ->
            val indices = tabs.order.indices
            if (from == to || from !in indices || to !in indices) return@update tabs
            tabs.copy(order = tabs.order.toMutableList().apply { add(to, removeAt(from)) })
        }
    }

    fun onToggled(id: TabId, visible: Boolean) {
        draft.update { tabs ->
            when {
                visible -> tabs.copy(hidden = tabs.hidden - id)
                tabs.visible == listOf(id) -> tabs
                else -> tabs.copy(hidden = tabs.hidden + id)
            }
        }
    }

    fun onReset() {
        draft.value = TabPreferences()
    }

    fun onSave() {
        repository.saveTabs(draft.value)
        _effects.trySend(TabManagerUiEffect.Dismiss)
    }
}
