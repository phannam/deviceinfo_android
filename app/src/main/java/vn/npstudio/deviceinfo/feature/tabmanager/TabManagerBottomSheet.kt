package vn.npstudio.deviceinfo.feature.tabmanager

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.databinding.BottomSheetTabManagerBinding

/** Show, hide, and sort the main tabs (spec 002 section 4.5). Dismiss without Save discards the draft. */
@AndroidEntryPoint
class TabManagerBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: TabManagerViewModel by viewModels()

    private var _binding: BottomSheetTabManagerBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var adapter: TabManagerAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        BottomSheetTabManagerBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lateinit var touchHelper: ItemTouchHelper
        val adapter = TabManagerAdapter(
            onToggled = viewModel::onToggled,
            onMove = ::move,
            onStartDrag = { touchHelper.startDrag(it) },
        )
        this.adapter = adapter
        touchHelper = ItemTouchHelper(DragCallback())
        binding.tabList.adapter = adapter
        (binding.tabList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        touchHelper.attachToRecyclerView(binding.tabList)

        binding.resetButton.setOnClickListener { viewModel.onReset() }
        binding.saveButton.setOnClickListener { viewModel.onSave() }

        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { adapter.submit(it.items) }
        viewModel.effects.collectWithLifecycle(viewLifecycleOwner) { effect ->
            when (effect) {
                TabManagerUiEffect.Dismiss -> dismiss()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        adapter = null
        super.onDestroyView()
    }

    private fun move(from: Int, to: Int) {
        adapter?.move(from, to) ?: return
        viewModel.onMoved(from, to)
    }

    private inner class DragCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
            move(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        // Stop the sheet from taking the vertical drag while a row is being dragged.
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = actionState != ItemTouchHelper.ACTION_STATE_DRAG
        }
    }

    companion object {
        private const val TAG = "tab_manager"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return
            TabManagerBottomSheet().show(fragmentManager, TAG)
        }
    }
}
