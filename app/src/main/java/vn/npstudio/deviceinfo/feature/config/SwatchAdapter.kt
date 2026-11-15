package vn.npstudio.deviceinfo.feature.config

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import vn.npstudio.deviceinfo.databinding.ItemSwatchBinding
import vn.npstudio.deviceinfo.domain.model.AppColor
import vn.npstudio.deviceinfo.ui.theme.ColorRole
import vn.npstudio.deviceinfo.ui.theme.ThemeOverlays

/** Fixed list of every [AppColor] in enum order; the selected swatch is enlarged and checked. */
class SwatchAdapter(
    private val role: ColorRole,
    selected: AppColor,
    private val onClick: (AppColor) -> Unit,
) : RecyclerView.Adapter<SwatchAdapter.ViewHolder>() {

    private val colors = AppColor.entries

    var selected: AppColor = selected
        set(value) {
            if (field == value) return
            val old = field
            field = value
            notifyItemChanged(colors.indexOf(old))
            notifyItemChanged(colors.indexOf(value))
        }

    override fun getItemCount() = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemSwatchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, color == selected)
        holder.itemView.setOnClickListener { onClick(color) }
    }

    inner class ViewHolder(private val binding: ItemSwatchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: AppColor, isSelected: Boolean) {
            val context = binding.root.context
            ViewCompat.setBackgroundTintList(
                binding.swatch,
                ColorStateList.valueOf(ContextCompat.getColor(context, ThemeOverlays.swatch(role, color))),
            )
            binding.check.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, ThemeOverlays.onSwatch(role, color)))
            binding.check.isInvisible = !isSelected
            val scale = if (isSelected) SELECTED_SCALE else 1f
            binding.swatch.scaleX = scale
            binding.swatch.scaleY = scale
            binding.root.contentDescription = context.getString(ThemeOverlays.name(color))
            binding.root.isSelected = isSelected
        }
    }

    private companion object {
        const val SELECTED_SCALE = 1.2f
    }
}
