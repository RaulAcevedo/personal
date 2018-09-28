package com.jjkeller.kmb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbui.R;

import java.util.List;

/**
 * Custom adapter for the DutyStaus pick list. The list is no longer static but can be dynamic.
 *
 * For example, if your editing an ELD Event record that was automatically recorded driving time
 * the only DutyStatus options you can choose from are:  Off Duty - PC | Driving | On Duty - YM
 */
public class EldEventDutyStatusAdapter extends BaseAdapter {
    private Context _context;
    private List<EldEventDutyStatusItem> _items;
    private LayoutInflater _inflater;
    private int _subNameColor;

    private class DropDownViewHolder {
        TextView textView;
    }

    private class ViewHolder {
        TextView textView;
    }

    public EldEventDutyStatusAdapter(Context applicationContext, List<EldEventDutyStatusItem> items) {
        this._context = applicationContext;
        this._items = items;
        this._inflater = (LayoutInflater.from(applicationContext));
        this._subNameColor = applicationContext.getResources().getColor(R.color.header_orange);
    }

    /**
     * Determine the position the initial selection should be
     */
    public int getSelectionIndex(Enums.EmployeeLogEldEventType eventType, int eventCode) {
        for (int i = 0; i < _items.size(); i++) {
            EldEventDutyStatusItem item = _items.get(i);
            if (item.getEventType() == eventType && item.getEventCode() == eventCode) {
                return i;
            }
        }

        return 0;
    }

    public int getSelectionIndex(Enums.SpecialDrivingCategory subStatus) {
        int selection = -1;
        for (int i = 0; i < _items.size(); i++) {
            EldEventDutyStatusItem item = _items.get(i);
            if (item.getSubStatus() == subStatus) {
                selection = i;
            }
        }

        return selection;
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
            convertView = _inflater.inflate(android.R.layout.simple_list_item_checked, null);

            holder = new DropDownViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else {
            holder = (DropDownViewHolder) convertView.getTag();
        }

        EldEventDutyStatusItem item = _items.get(position);

        String name = item.getName();
        if (item.getSubName() != null && item.getSubName().length() > 0) {
            name += " - " + item.getSubName();

            Spannable wordtoSpan = new SpannableString(name);
            wordtoSpan.setSpan(new ForegroundColorSpan(_subNameColor), item.getName().length(), name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textView.setText(wordtoSpan);
        }
        else
            holder.textView.setText(name);

        return convertView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

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

        holder.textView.setText(_items.get(position).getName());
        return convertView;
    }
}