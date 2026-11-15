package vn.npstudio.deviceinfo.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.databinding.FragmentInfoListBinding

/** Shared list screen for every info tab. Subclasses only provide the ViewModel state. */
abstract class InfoListFragment : Fragment() {

    private var _binding: FragmentInfoListBinding? = null
    private val binding get() = checkNotNull(_binding)

    protected abstract val uiState: StateFlow<InfoListUiState>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInfoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = InfoRowAdapter()
        binding.infoList.adapter = adapter
        binding.infoList.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL)
                .apply { isLastItemDecorated = false },
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.infoList) { list, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            list.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }

        uiState.collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { state ->
            binding.loadingIndicator.isVisible = state.loadingMode == LoadingMode.INITIAL
            adapter.submitList(state.rows)
        }
    }

    override fun onDestroyView() {
        binding.infoList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
