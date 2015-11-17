package info.device.deviceinfo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import info.device.deviceinfo.R;
import info.device.deviceinfo.utils.Item;
import info.device.deviceinfo.utils.ItemListAdapter;

/**
 * Created by namphan on 11/16/15.
 */
public abstract class ListFragment extends BaseFragment {
    private ListView lvItems;
    private ItemListAdapter listAdapter;
    private LinkedList<Item> lstItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listitems, container, false);
        lvItems = (ListView) rootView.findViewById(R.id.lst_items);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupItemList();
    }

    protected abstract List<Item> getList();

    private void setupItemList() {
        lstItems = new LinkedList<>();
        lstItems.addAll(getList());
        listAdapter = new ItemListAdapter(getActivity(), 0, lstItems);
        lvItems.setAdapter(listAdapter);
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
