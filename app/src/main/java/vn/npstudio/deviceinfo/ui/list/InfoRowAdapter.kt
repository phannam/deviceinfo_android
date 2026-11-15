package vn.npstudio.deviceinfo.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vn.npstudio.deviceinfo.databinding.ItemInfoRowBinding

/** Rows drawn as one rounded card group; each row's background depends on its [RowPosition]. */
class InfoRowAdapter : ListAdapter<InfoRowUiModel, InfoRowAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemInfoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.bindPosition(RowPosition.of(position, itemCount))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty() && payloads.all { it == PAYLOAD_POSITION }) {
            holder.bindPosition(RowPosition.of(position, itemCount))
        } else {
            onBindViewHolder(holder, position)
        }
    }

    // DiffUtil does not rebind unchanged rows whose first/last position changed, so refresh the corners.
    override fun onCurrentListChanged(previousList: List<InfoRowUiModel>, currentList: List<InfoRowUiModel>) {
        val endsChanged = previousList.size != currentList.size ||
            previousList.firstOrNull()?.label != currentList.firstOrNull()?.label ||
            previousList.lastOrNull()?.label != currentList.lastOrNull()?.label
        if (endsChanged && currentList.isNotEmpty()) {
            notifyItemRangeChanged(0, currentList.size, PAYLOAD_POSITION)
        }
    }

    class ViewHolder(private val binding: ItemInfoRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: InfoRowUiModel) {
            val context = binding.root.context
            binding.nameText.text = row.label.asString(context)
            binding.valueText.text = row.value.asString(context)
            binding.progressIndicator.isVisible = row.progress != null
            row.progress?.let { binding.progressIndicator.setProgressCompat(it, false) }
        }

        fun bindPosition(position: RowPosition) {
            binding.root.setBackgroundResource(position.backgroundRes)
            binding.divider.isVisible = position.showsDivider
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<InfoRowUiModel>() {
        override fun areItemsTheSame(oldItem: InfoRowUiModel, newItem: InfoRowUiModel) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: InfoRowUiModel, newItem: InfoRowUiModel) =
            oldItem == newItem
    }

    private companion object {
        const val PAYLOAD_POSITION = "position"
    }
}
