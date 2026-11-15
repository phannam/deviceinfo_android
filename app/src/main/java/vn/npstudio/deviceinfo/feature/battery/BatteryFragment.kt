package vn.npstudio.deviceinfo.feature.battery

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState

@AndroidEntryPoint
class BatteryFragment : InfoListFragment() {

    private val viewModel: BatteryViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState
}
