package vn.npstudio.deviceinfo.feature.screentest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.databinding.FragmentScreenTestBinding
import vn.npstudio.deviceinfo.ui.list.InfoRowAdapter

@AndroidEntryPoint
class ScreenTestFragment : Fragment(R.layout.fragment_screen_test) {

    private val viewModel: ScreenTestResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentScreenTestBinding.bind(view)
        val adapter = InfoRowAdapter()
        binding.resultList.adapter = adapter
        binding.resultList.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL)
                .apply { isLastItemDecorated = false },
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            root.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }
        binding.startButton.setOnClickListener {
            startActivity(Intent(requireContext(), ScreenTestActivity::class.java))
        }

        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { state ->
            adapter.submitList(state.rows)
        }
    }
}
