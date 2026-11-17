package vn.npstudio.deviceinfo.ui.list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.extension.applySystemBarPadding
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.databinding.FragmentInfoListBinding

/**
 * Shared list screen for every info tab. Subclasses provide the ViewModel state and may react to
 * [onPermissionGranted]. Long-press copies a row; action rows open a list sheet or ask for a permission.
 */
abstract class InfoListFragment : Fragment() {

    private var _binding: FragmentInfoListBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var pendingPermission: String? = null

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val permission = pendingPermission
        pendingPermission = null
        when {
            granted -> onPermissionGranted()
            permission != null && !shouldShowRequestPermissionRationale(permission) -> showOpenAppDetails()
        }
    }

    protected abstract val uiState: StateFlow<InfoListUiState>

    /** Called after the user grants a permission requested from a row. */
    protected open fun onPermissionGranted() = Unit

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
        pendingPermission = savedInstanceState?.getString(KEY_PENDING_PERMISSION)
        val adapter = InfoListAdapter(onAction = ::onRowAction, onCopy = ::copyToClipboard)
        binding.infoList.adapter = adapter
        // Live tabs rebind every second; skip the change cross-fade.
        (binding.infoList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        binding.infoList.applySystemBarPadding()

        uiState.collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { state ->
            binding.loadingIndicator.isVisible = state.loadingMode == LoadingMode.INITIAL
            adapter.submitList(InfoListItem.from(state))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingPermission?.let { outState.putString(KEY_PENDING_PERMISSION, it) }
    }

    override fun onDestroyView() {
        binding.infoList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun onRowAction(action: RowAction) {
        when (action) {
            is RowAction.RequestPermission -> {
                pendingPermission = action.permission
                permissionLauncher.launch(action.permission)
            }
            is RowAction.ShowList -> {
                val context = requireContext()
                ListEntriesBottomSheet.show(
                    childFragmentManager,
                    title = action.title.asString(context),
                    titles = action.entries.map { it.title },
                    subtitles = action.entries.map { it.subtitle?.asString(context).orEmpty() },
                )
            }
        }
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = requireContext().getSystemService<ClipboardManager>() ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        // Android 13+ shows its own copy confirmation.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }

    /** The user chose "Don't ask again"; only the system app details screen can grant it now. */
    private fun showOpenAppDetails() {
        val root = _binding?.root ?: return
        Snackbar.make(root, R.string.permission_denied_message, Snackbar.LENGTH_LONG)
            .setAction(R.string.permission_open_app_details) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null),
                )
                runCatching { startActivity(intent) }
            }
            .show()
    }

    private companion object {
        const val KEY_PENDING_PERMISSION = "pending_permission"
    }
}
