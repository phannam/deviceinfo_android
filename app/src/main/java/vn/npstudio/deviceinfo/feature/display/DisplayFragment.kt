package vn.npstudio.deviceinfo.feature.display

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.ui.list.InfoListFragment
import vn.npstudio.deviceinfo.ui.list.InfoListUiState
import vn.npstudio.deviceinfo.ui.window.WindowSnapshotReader

@AndroidEntryPoint
class DisplayFragment : InfoListFragment() {

    private val viewModel: DisplayViewModel by viewModels()

    override val uiState: StateFlow<InfoListUiState> get() = viewModel.uiState

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Insets are dispatched before the first layout; a new view after recreation re-measures.
        view.doOnLayout { viewModel.onWindowMeasured(WindowSnapshotReader.read(requireActivity(), it)) }
    }
}
