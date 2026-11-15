package vn.npstudio.deviceinfo.feature.tabmanager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.databinding.ItemTabManagerBinding
import vn.npstudio.deviceinfo.domain.model.TabId

/**
 * Keeps its own list so drags move rows at once; [submit] then ignores the equal list from the ViewModel.
 * [onMove] receives adapter positions and must call [move].
 */
class TabManagerAdapter(
    private val onToggled: (TabId, Boolean) -> Unit,
    private val onMove: (from: Int, to: Int) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
) : RecyclerView.Adapter<TabManagerAdapter.ViewHolder>() {

    private var items: List<TabManagerItem> = emptyList()

    fun submit(newItems: List<TabManagerItem>) {
        if (newItems == items) return
        val old = items
        items = newItems
        DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getOldListSize() = old.size

                override fun getNewListSize() = newItems.size

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int) =
                    old[oldPosition].id == newItems[newPosition].id

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int) =
                    old[oldPosition] == newItems[newPosition]
            },
        ).dispatchUpdatesTo(this)
    }

    fun move(from: Int, to: Int) {
        items = items.toMutableList().apply { add(to, removeAt(from)) }
        notifyItemMoved(from, to)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemTabManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    @SuppressLint("ClickableViewAccessibility") // TalkBack users move rows with the custom actions instead.
    inner class ViewHolder(private val binding: ItemTabManagerBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) onStartDrag(this)
                false
            }
            binding.root.setOnClickListener {
                if (binding.tabSwitch.isEnabled) binding.tabSwitch.toggle()
            }
            val context = binding.root.context
            ViewCompat.addAccessibilityAction(binding.root, context.getString(R.string.tab_manager_move_up)) { _, _ ->
                moveBy(-1)
            }
            ViewCompat.addAccessibilityAction(binding.root, context.getString(R.string.tab_manager_move_down)) { _, _ ->
                moveBy(1)
            }
        }

        fun bind(item: TabManagerItem) {
            val title = binding.root.context.getString(item.title)
            binding.title.text = title
            binding.tabSwitch.setOnCheckedChangeListener(null)
            binding.tabSwitch.isChecked = item.visible
            binding.tabSwitch.isEnabled = item.toggleEnabled
            binding.tabSwitch.contentDescription = title
            binding.tabSwitch.setOnCheckedChangeListener { _, checked -> onToggled(item.id, checked) }
        }

        private fun moveBy(offset: Int): Boolean {
            val from = bindingAdapterPosition
            val to = from + offset
            if (from == RecyclerView.NO_POSITION || to !in items.indices) return false
            onMove(from, to)
            return true
        }
    }
}
