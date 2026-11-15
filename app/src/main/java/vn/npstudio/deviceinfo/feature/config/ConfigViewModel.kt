package vn.npstudio.deviceinfo.feature.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val repository: AppConfigRepository,
) : ViewModel() {

    val uiState: StateFlow<ConfigUiState> = repository.config
        .map(ConfigUiState::from)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ConfigUiState.from(repository.config.value),
        )

    /** ThemeApplier applies the new night mode; AppCompat then recreates open activities. */
    fun onDarkModeSelected(mode: DarkMode) = repository.setDarkMode(mode)

    fun onTemperatureUnitSelected(unit: TemperatureUnit) = repository.setTemperatureUnit(unit)
}
