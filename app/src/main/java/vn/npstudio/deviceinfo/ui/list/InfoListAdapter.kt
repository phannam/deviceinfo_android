package vn.npstudio.deviceinfo.ui.list

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.databinding.ItemInfoFeaturesBinding
import vn.npstudio.deviceinfo.databinding.ItemInfoHeaderBinding
import vn.npstudio.deviceinfo.databinding.ItemInfoHeroBinding
import vn.npstudio.deviceinfo.databinding.ItemInfoRowBinding

/**
 * Info tab list: a hero card, then section cards drawn from rounded row backgrounds (spec 003 section 4.1).
 * [onAction] handles clickable rows; [onCopy] receives the label and value of a long-pressed row.
 */
class InfoListAdapter(
    private val onAction: (RowAction) -> Unit = {},
    private val onCopy: (label: String, value: String) -> Unit = { _, _ -> },
) : ListAdapter<InfoListItem, InfoListAdapter.ItemViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is InfoListItem.Hero -> TYPE_HERO
        is InfoListItem.Header -> TYPE_HEADER
        is InfoListItem.Row -> TYPE_ROW
        is InfoListItem.Features -> TYPE_FEATURES
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HERO -> HeroViewHolder(ItemInfoHeroBinding.inflate(inflater, parent, false))
            TYPE_HEADER -> HeaderViewHolder(ItemInfoHeaderBinding.inflate(inflater, parent, false))
            TYPE_ROW -> RowViewHolder(ItemInfoRowBinding.inflate(inflater, parent, false), onAction, onCopy)
            else -> FeaturesViewHolder(ItemInfoFeaturesBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bindCard(item.position, isFirst = position == 0)
        holder.bind(item)
    }

    abstract class ItemViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: InfoListItem)

        /** Rounded corners per [position]; a gap above each card except the first. */
        open fun bindCard(position: RowPosition, isFirst: Boolean) {
            itemView.setBackgroundResource(position.backgroundRes)
            val startsCard = position == RowPosition.TOP || position == RowPosition.SINGLE
            val gap = itemView.resources.getDimensionPixelSize(R.dimen.cardGap)
            itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = if (startsCard && !isFirst) gap else 0
            }
        }
    }

    private class HeroViewHolder(private val binding: ItemInfoHeroBinding) : ItemViewHolder(binding) {
        override fun bindCard(position: RowPosition, isFirst: Boolean) = Unit

        override fun bind(item: InfoListItem) {
            val model = (item as InfoListItem.Hero).model
            val context = binding.root.context
            binding.badgeIcon.isVisible = model.badgeIcon != null
            model.badgeIcon?.let(binding.badgeIcon::setImageResource)
            binding.badgeValue.setOptionalText(model.badge)
            binding.badgeLabel.setOptionalText(model.badgeLabel?.asString(context))
            binding.heroTitle.text = model.title.asString(context)
            binding.heroSubtitle.setOptionalText(model.subtitle?.asString(context))
            binding.heroDetail.setOptionalText(model.detail?.asString(context))
        }

        private fun TextView.setOptionalText(value: String?) {
            text = value
            isVisible = value != null
        }
    }

    private class HeaderViewHolder(private val binding: ItemInfoHeaderBinding) : ItemViewHolder(binding) {
        init {
            ViewCompat.setAccessibilityHeading(binding.root, true)
        }

        override fun bind(item: InfoListItem) {
            val header = item as InfoListItem.Header
            binding.headerTitle.text = header.title.asString(binding.root.context)
            binding.headerIcon.isVisible = header.icon != null
            header.icon?.let(binding.headerIcon::setImageResource)
        }
    }

    private class RowViewHolder(
        private val binding: ItemInfoRowBinding,
        private val onAction: (RowAction) -> Unit,
        private val onCopy: (String, String) -> Unit,
    ) : ItemViewHolder(binding) {

        override fun bindCard(position: RowPosition, isFirst: Boolean) {
            super.bindCard(position, isFirst)
            binding.divider.isVisible = position.showsDivider
        }

        override fun bind(item: InfoListItem) {
            val row = (item as InfoListItem.Row).row
            val context = binding.root.context
            val label = row.label.asString(context)
            val value = when (row.action) {
                is RowAction.RequestPermission -> context.getString(R.string.value_tap_to_grant)
                is RowAction.ShowList -> context.getString(R.string.value_view_count, row.value.asString(context))
                null -> row.value.asString(context)
            }
            binding.nameText.text = label
            binding.valueText.text = value
            binding.valueText.setTextColor(
                MaterialColors.getColor(
                    binding.valueText,
                    if (row.action != null) R.attr.colorPrimaryOnBackground else MaterialR.attr.colorOnSurface,
                ),
            )
            binding.actionIcon.isVisible = row.action != null
            when (row.action) {
                is RowAction.RequestPermission -> binding.actionIcon.setImageResource(R.drawable.ic_key)
                is RowAction.ShowList -> binding.actionIcon.setImageResource(R.drawable.ic_chevron_right)
                null -> Unit
            }
            binding.progressIndicator.isVisible = row.progress != null
            row.progress?.let { binding.progressIndicator.setProgressCompat(it, false) }

            val action = row.action
            if (action != null) {
                binding.root.setOnClickListener { onAction(action) }
                binding.root.setOnLongClickListener(null)
                binding.root.isLongClickable = false
            } else {
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
                binding.root.setOnLongClickListener {
                    onCopy(label, value)
                    true
                }
            }
        }
    }

    private class FeaturesViewHolder(private val binding: ItemInfoFeaturesBinding) : ItemViewHolder(binding) {

        override fun bind(item: InfoListItem) {
            val features = (item as InfoListItem.Features).features
            val group = binding.featureChips
            val context = group.context
            group.removeAllViews()
            val supportedBackground = MaterialColors.getColor(group, MaterialR.attr.colorSecondaryContainer)
            val supportedContent = MaterialColors.getColor(group, MaterialR.attr.colorOnSecondaryContainer)
            val missingContent = MaterialColors.getColor(group, MaterialR.attr.colorOnSurfaceVariant)
            val outline = MaterialColors.getColor(group, MaterialR.attr.colorOutlineVariant)
            features.forEach { feature ->
                val label = feature.label.asString(context)
                val content = if (feature.supported) supportedContent else missingContent
                group.addView(
                    Chip(context).apply {
                        text = label
                        isCheckable = false
                        isClickable = false
                        isFocusable = false
                        chipIcon = ContextCompat.getDrawable(
                            context,
                            if (feature.supported) R.drawable.ic_check else R.drawable.ic_close,
                        )
                        isChipIconVisible = true
                        chipIconTint = ColorStateList.valueOf(content)
                        setTextColor(content)
                        chipBackgroundColor = ColorStateList.valueOf(
                            if (feature.supported) supportedBackground else Color.TRANSPARENT,
                        )
                        chipStrokeWidth = if (feature.supported) 0f else resources.getDimension(R.dimen.dividerHeight)
                        chipStrokeColor = ColorStateList.valueOf(outline)
                        alpha = if (feature.supported) 1f else UNSUPPORTED_ALPHA
                        contentDescription = context.getString(
                            if (feature.supported) R.string.feature_supported else R.string.feature_not_supported,
                            label,
                        )
                    },
                )
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<InfoListItem>() {
        override fun areItemsTheSame(oldItem: InfoListItem, newItem: InfoListItem) =
            oldItem::class == newItem::class && oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: InfoListItem, newItem: InfoListItem) = oldItem == newItem
    }

    private companion object {
        const val TYPE_HERO = 0
        const val TYPE_HEADER = 1
        const val TYPE_ROW = 2
        const val TYPE_FEATURES = 3
        const val UNSUPPORTED_ALPHA = 0.7f
    }
}
