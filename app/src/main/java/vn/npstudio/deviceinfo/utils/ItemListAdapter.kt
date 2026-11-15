package vn.npstudio.deviceinfo.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import vn.npstudio.deviceinfo.databinding.ItemInfoBinding

class ItemListAdapter(
    context: Context,
    resource: Int,
    objects: List<Item>,
) : ArrayAdapter<Item>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemInfoBinding.inflate(LayoutInflater.from(context), parent, false).also {
                it.root.tag = it
            }
        } else {
            convertView.tag as ItemInfoBinding
        }

        val item = getItem(position)
        binding.nameText.text = item?.name
        binding.valueText.text = item?.value
        return binding.root
    }
}
