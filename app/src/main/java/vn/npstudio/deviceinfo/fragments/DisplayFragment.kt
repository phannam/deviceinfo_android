package vn.npstudio.deviceinfo.fragments

import vn.npstudio.deviceinfo.utils.AppManager
import vn.npstudio.deviceinfo.utils.Item

class DisplayFragment : ListFragment() {
    override fun getList(): List<Item> = AppManager.getInstance().displayInfoList
}
