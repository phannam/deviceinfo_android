package vn.npstudio.deviceinfo.feature.display

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState

@AndroidEntryPoint
class DisplayFragment : InfoListFragment() {

    private val viewModel: DisplayViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState
}
