package vn.npstudio.deviceinfo.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.databinding.BottomSheetListEntriesBinding
import vn.npstudio.deviceinfo.databinding.ItemListEntryBinding

/** Full list behind a "View all" row, such as codecs or system features (spec 003 section 4.1). */
class ListEntriesBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetListEntriesBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        BottomSheetListEntriesBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val titles = args.getStringArray(ARG_TITLES).orEmpty()
        val subtitles = args.getStringArray(ARG_SUBTITLES).orEmpty()
        binding.sheetTitle.text = args.getString(ARG_TITLE)
        binding.sheetCount.text = resources.getQuantityString(R.plurals.list_sheet_count, titles.size, titles.size)
        binding.entryList.adapter = EntryAdapter(titles, subtitles)
    }

    override fun onDestroyView() {
        binding.entryList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private class EntryAdapter(
        private val titles: Array<out String>,
        private val subtitles: Array<out String>,
    ) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

        override fun getItemCount() = titles.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EntryViewHolder(ItemListEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
            holder.binding.entryTitle.text = titles[position]
            val subtitle = subtitles.getOrNull(position).orEmpty()
            holder.binding.entrySubtitle.text = subtitle
            holder.binding.entrySubtitle.isVisible = subtitle.isNotEmpty()
        }

        class EntryViewHolder(val binding: ItemListEntryBinding) : RecyclerView.ViewHolder(binding.root)
    }

    companion object {
        private const val TAG = "ListEntriesBottomSheet"
        private const val ARG_TITLE = "title"
        private const val ARG_TITLES = "titles"
        private const val ARG_SUBTITLES = "subtitles"

        /** Subtitles are already resolved; an empty subtitle hides the second line. */
        fun show(fragmentManager: FragmentManager, title: String, titles: List<String>, subtitles: List<String>) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return
            ListEntriesBottomSheet().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_TITLES to titles.toTypedArray(),
                    ARG_SUBTITLES to subtitles.toTypedArray(),
                )
            }.show(fragmentManager, TAG)
        }
    }
}
