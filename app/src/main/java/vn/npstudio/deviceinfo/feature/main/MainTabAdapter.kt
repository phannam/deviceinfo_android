package vn.npstudio.deviceinfo.feature.main

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.npstudio.deviceinfo.feature.battery.BatteryFragment
import vn.npstudio.deviceinfo.feature.cpu.CpuFragment
import vn.npstudio.deviceinfo.feature.device.DeviceFragment
import vn.npstudio.deviceinfo.feature.display.DisplayFragment
import vn.npstudio.deviceinfo.feature.memory.MemoryFragment
import vn.npstudio.deviceinfo.feature.screentest.ScreenTestFragment

/** Item ids are [MainTab.ordinal], never positions, so fragments of tabs that stay are kept when the list changes. */
class MainTabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    var tabs: List<MainTab> = emptyList()
        private set

    // FragmentStateAdapter needs a full refresh with stable ids; it matches fragments by getItemId.
    @SuppressLint("NotifyDataSetChanged")
    fun submit(newTabs: List<MainTab>) {
        if (newTabs == tabs) return
        tabs = newTabs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tabs.size

    override fun getItemId(position: Int): Long = tabs[position].ordinal.toLong()

    override fun containsItem(itemId: Long): Boolean = tabs.any { it.ordinal.toLong() == itemId }

    override fun createFragment(position: Int): Fragment = when (tabs[position]) {
        MainTab.DEVICE -> DeviceFragment()
        MainTab.CPU -> CpuFragment()
        MainTab.BATTERY -> BatteryFragment()
        MainTab.DISPLAY -> DisplayFragment()
        MainTab.MEMORY -> MemoryFragment()
        MainTab.SCREEN_TEST -> ScreenTestFragment()
    }
}
