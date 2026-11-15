package vn.npstudio.deviceinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vn.npstudio.deviceinfo.databinding.FragmentListitemsBinding
import vn.npstudio.deviceinfo.utils.Item
import vn.npstudio.deviceinfo.utils.ItemListAdapter

abstract class ListFragment : BaseFragment() {
    private var _binding: FragmentListitemsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListitemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupItemList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun getList(): List<Item>

    private fun setupItemList() {
        binding.infoList.adapter = ItemListAdapter(requireContext(), 0, getList().toMutableList())
    }
}
