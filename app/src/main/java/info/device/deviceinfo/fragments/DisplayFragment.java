package info.device.deviceinfo.fragments;

import java.util.List;

import info.device.deviceinfo.utils.AppManager;
import info.device.deviceinfo.utils.Item;

/**
 * Created by namphan on 11/16/15.
 */
public class DisplayFragment extends ListFragment {
    @Override
    protected List<Item> getList() {
        return AppManager.getInstance().getDisplayInfoList();
    }
}
