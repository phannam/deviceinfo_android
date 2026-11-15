package vn.npstudio.deviceinfo.fragments

import vn.npstudio.deviceinfo.utils.AppManager
import vn.npstudio.deviceinfo.utils.Item

class DeviceFragment : ListFragment() {
    override fun getList(): List<Item> = AppManager.getInstance().deviceInfoList
}
