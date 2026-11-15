package vn.npstudio.deviceinfo.feature.memory

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState

@AndroidEntryPoint
class MemoryFragment : InfoListFragment() {

    private val viewModel: MemoryViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState

    override fun onResume() {
        super.onResume()
        viewModel.onResumed()
    }
}
