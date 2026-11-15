package vn.npstudio.deviceinfo.feature.screentest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.npstudio.deviceinfo.domain.model.ScreenDefect
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.domain.repository.ScreenTestResultRepository
import javax.inject.Inject

/** Drives [ScreenTestActivity]: pattern sequence, hint timeout, and the result dialog. */
@HiltViewModel
class ScreenTestViewModel @Inject constructor(
    private val repository: ScreenTestResultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenTestUiState())
    val uiState: StateFlow<ScreenTestUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ScreenTestUiEffect>(Channel.BUFFERED)
    val effects: Flow<ScreenTestUiEffect> = _effects.receiveAsFlow()

    private var hintJob: Job? = null

    init {
        scheduleHintHide()
    }

    fun onTap() {
        val state = _uiState.value
        if (state.askingResult) return
        val next = ScreenTestPattern.entries.getOrNull(state.pattern.ordinal + 1)
        if (next == null) {
            hintJob?.cancel()
            _uiState.update { it.copy(hintVisible = false, askingResult = true) }
        } else {
            _uiState.update { it.copy(pattern = next, hintVisible = true) }
            scheduleHintHide()
        }
    }

    fun onDefectChecked(defect: ScreenDefect, checked: Boolean) {
        _uiState.update { it.copy(defects = if (checked) it.defects + defect else it.defects - defect) }
    }

    fun onSaveClicked() {
        repository.save(ScreenTestResult(_uiState.value.defects))
        _effects.trySend(ScreenTestUiEffect.Finish)
    }

    fun onDiscardClicked() {
        _effects.trySend(ScreenTestUiEffect.Finish)
    }

    private fun scheduleHintHide() {
        hintJob?.cancel()
        hintJob = viewModelScope.launch {
            delay(HINT_DURATION_MILLIS)
            _uiState.update { it.copy(hintVisible = false) }
        }
    }

    companion object {
        const val HINT_DURATION_MILLIS = 2_000L
    }
}
