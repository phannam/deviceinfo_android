package vn.npstudio.deviceinfo.feature.system

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState

@AndroidEntryPoint
class SystemFragment : InfoListFragment() {

    private val viewModel: SystemViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState
}
