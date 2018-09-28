package com.jjkeller.kmb.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jjkeller.kmbapi.enums.Describable;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbui.R;

import java.util.List;

/**
 * Created by T000684 on 4/4/2017.
 */

public class DescribableAdapter extends ArrayAdapter<Describable> {

        private Context context;
        private Describable[] data = null;
        private boolean useLongDescription = false;

        public DescribableAdapter(Context context, Describable[] data) {
            super(context, android.R.layout.simple_spinner_dropdown_item, data);
            this.context = context;
            this.data = data;
        }


    public DescribableAdapter(Context context, Describable[] data, boolean useLongDescript) {
        super(context, android.R.layout.simple_spinner_dropdown_item, data);
        this.context = context;
        this.data = data;
        this.useLongDescription = useLongDescript;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getAdapterView(position, convertView, R.layout.kmb_spinner_item );
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getAdapterView(position, convertView,  android.R.layout.simple_spinner_dropdown_item);
    }


    public View getAdapterView(int position, View convertView, int resourceId) {
        View row = convertView;
        EnumDropdownHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(resourceId, null);
            holder = new EnumDropdownHolder();
            holder.textView = (TextView) row.findViewById(android.R.id.text1);
            row.setTag(holder);
        } else {
            holder = (EnumDropdownHolder) row.getTag();
        }
        Describable describable = data[position];
        if(useLongDescription){
            holder.textView.setText(row.getResources().getString(describable.getFullDescriptionKey()));
        } else {
            holder.textView.setText(row.getResources().getString(describable.getDescriptionKey()));
        }
        return row;
    }

    private static class EnumDropdownHolder {
        TextView textView;
    }

}
