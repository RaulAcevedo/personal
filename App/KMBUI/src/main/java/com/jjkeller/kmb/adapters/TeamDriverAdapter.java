package com.jjkeller.kmb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.jjkeller.kmbui.R;

import java.util.List;

/**
 * Custom adapter for the Team Driver pick list.
 */
public class TeamDriverAdapter extends BaseAdapter {
    private Context _context;
    private List<TeamDriverItem> _items;
    private LayoutInflater _inflater;
    private int _selectedPosition = 0;

    private class DropDownViewHolder {
        TextView textEmployeeCode;
        CheckedTextView textDisplayName;
    }

    private class ViewHolder {
        TextView textView;
    }

    public TeamDriverAdapter(Context applicationContext, List<TeamDriverItem> items) {
        this._context = applicationContext;
        this._items = items;
        this._inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return _items.size();
    }

    @Override
    public Object getItem(int i) {
        return _items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {

        DropDownViewHolder holder = null;

        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.teamdriver_list_item_checked, null);

            holder = new DropDownViewHolder();
            holder.textEmployeeCode = (TextView) convertView.findViewById(R.id.textEmployeeCode);
            holder.textDisplayName = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else {
            holder = (DropDownViewHolder) convertView.getTag();
        }

        TeamDriverItem item = _items.get(position);

        holder.textEmployeeCode.setText(item.getEmployeeCode());
        holder.textDisplayName.setText(item.getDisplayName());
        holder.textDisplayName.setChecked(position == _selectedPosition);

        return convertView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        _selectedPosition = position;

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = _inflater.inflate(android.R.layout.simple_spinner_item, parent, false);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            holder.textView.setTextColor(Color.BLACK);  // for some reason it's not picking up the style="@style/Theme.SpinnerLight" defined in the layout .xml
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(_items.get(position).getDisplayName());
        return convertView;
    }

}