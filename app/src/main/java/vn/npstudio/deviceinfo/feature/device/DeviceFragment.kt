package vn.npstudio.deviceinfo.feature.device

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState

@AndroidEntryPoint
class DeviceFragment : InfoListFragment() {

    private val viewModel: DeviceViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState

    override fun onResume() {
        super.onResume()
        viewModel.onResumed()
    }

    override fun onPermissionGranted() = viewModel.refresh()
}
