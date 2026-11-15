package vn.npstudio.deviceinfo.feature.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.npstudio.deviceinfo.feature.battery.BatteryFragment
import vn.npstudio.deviceinfo.feature.cpu.CpuFragment
import vn.npstudio.deviceinfo.feature.device.DeviceFragment
import vn.npstudio.deviceinfo.feature.display.DisplayFragment
import vn.npstudio.deviceinfo.feature.memory.MemoryFragment
import vn.npstudio.deviceinfo.feature.screentest.ScreenTestFragment

class MainTabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = MainTab.entries.size

    override fun createFragment(position: Int): Fragment = when (MainTab.entries[position]) {
        MainTab.DEVICE -> DeviceFragment()
        MainTab.CPU -> CpuFragment()
        MainTab.BATTERY -> BatteryFragment()
        MainTab.DISPLAY -> DisplayFragment()
        MainTab.MEMORY -> MemoryFragment()
        MainTab.SCREEN_TEST -> ScreenTestFragment()
    }
}
