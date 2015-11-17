package info.device.deviceinfo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import info.device.deviceinfo.R;

/**
 * Created by namphan on 11/16/15.
 */
public class ItemListAdapter extends ArrayAdapter<Item> {
    public ItemListAdapter(Context context, int resource, List<Item> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlertViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_simpleitem, parent, false);
            viewHolder = new AlertViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tvValue = (TextView) convertView.findViewById(R.id.tv_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AlertViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        viewHolder.tvName.setText(item.getName());
        viewHolder.tvValue.setText(item.getValue());
        return convertView;
    }

    static class AlertViewHolder {
        TextView tvName;
        TextView tvValue;
    }
}
