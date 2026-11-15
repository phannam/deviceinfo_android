package vn.npstudio.deviceinfo.fragments;

import java.util.List;

import vn.npstudio.deviceinfo.utils.AppManager;
import vn.npstudio.deviceinfo.utils.Item;

/**
 * Created by namphan on 11/16/15.
 */
public class DeviceFragment extends ListFragment {
    @Override
    protected List<Item> getList() {
        return AppManager.getInstance().getDeviceInfoList();
    }
}
