package vn.npstudio.deviceinfo.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vn.npstudio.deviceinfo.databinding.ItemInfoRowBinding

class InfoRowAdapter : ListAdapter<InfoRowUiModel, InfoRowAdapter.ViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).label.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemInfoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemInfoRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: InfoRowUiModel) {
            val context = binding.root.context
            binding.nameText.setText(row.label)
            binding.valueText.text = row.value.asString(context)
            binding.progressIndicator.isVisible = row.progress != null
            row.progress?.let { binding.progressIndicator.progress = it }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<InfoRowUiModel>() {
        override fun areItemsTheSame(oldItem: InfoRowUiModel, newItem: InfoRowUiModel) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: InfoRowUiModel, newItem: InfoRowUiModel) =
            oldItem == newItem
    }
}
