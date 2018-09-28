package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.adapters.DescribableAdapter;
import com.jjkeller.kmb.interfaces.IAdminMalfunctionAndDataDiagnostic;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminMalfunctionAndDataDiagnosticFrag extends BaseFragment {

    private IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticFragActions actionsListener;
    private IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticMethods controllerListener;

    private Spinner selectMalfunction;
    private Spinner selectDataDiagnostic;
    private ListView activeMalfunctionsAndDataDiagnostics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_adminmalfunctionanddatadiagnostic, container, false);
        findControls(v);
        loadControls();
        return v;
    }

    protected void findControls(View v) {
        selectMalfunction = (Spinner) v.findViewById(R.id.select_malfunction);
        selectDataDiagnostic = (Spinner) v.findViewById(R.id.select_data_diag);
        activeMalfunctionsAndDataDiagnostics = (ListView) v.findViewById(R.id.active_malfunctions_and_data_diagnostics);

        v.findViewById(R.id.btn_add_malfunction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onAddMalfunctionClick(getSelectedMalfunction());
            }
        });

        v.findViewById(R.id.btn_add_data_diagnostic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onAddDataDiagnosticClick(getSelectedDataDiagnostic());
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticFragActions");
        }
        try {
            controllerListener = (IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticMethods");
        }
    }

    /**
     * Loads all the views, like populating the active malfunction and data diagnostic list
     */
    public void loadControls() {
        DescribableAdapter selectMalfunctionAdapter = new DescribableAdapter(getActivity(), getAvailableMalfunctions());
        selectMalfunction.setAdapter(selectMalfunctionAdapter);

        DescribableAdapter selectDataDiagnosticAdapter = new DescribableAdapter(getActivity(), getAvailableDataDiagnostics());
        selectDataDiagnostic.setAdapter(selectDataDiagnosticAdapter);

        activeMalfunctionsAndDataDiagnostics.setAdapter(new ListAdapter(controllerListener.getActiveMalfunctions(), controllerListener.getActiveDataDiagnostics()));
    }

    private Malfunction[] getAvailableMalfunctions() {
        List<Malfunction> malfunctions = new ArrayList<>(Arrays.asList(Malfunction.values()));
        malfunctions.removeAll(controllerListener.getActiveMalfunctions());
        return malfunctions.toArray(new Malfunction[malfunctions.size()-1]);
    }

    private DataDiagnosticEnum[] getAvailableDataDiagnostics() {
        List<DataDiagnosticEnum> dataDiagnostics = new ArrayList<>(Arrays.asList(DataDiagnosticEnum.values()));
        dataDiagnostics.removeAll(controllerListener.getActiveDataDiagnostics());
        return dataDiagnostics.toArray(new DataDiagnosticEnum[dataDiagnostics.size() - 1]);
    }

    public Malfunction getSelectedMalfunction() {
        Malfunction selected = null;
        if (selectMalfunction != null) {
            selected = (Malfunction) selectMalfunction.getSelectedItem();
        }
        return selected;
    }

    public DataDiagnosticEnum getSelectedDataDiagnostic() {
        DataDiagnosticEnum selected = null;
        if (selectDataDiagnostic != null) {
            selected = (DataDiagnosticEnum) selectDataDiagnostic.getSelectedItem();
        }
        return selected;
    }

    private void onRemoveItemClick(ListItem item) {
        if (item instanceof MalfunctionListItem) {
            actionsListener.onRemoveClick(((MalfunctionListItem) item).malfunction);
        } else if (item instanceof DataDiagnosticListItem) {
            actionsListener.onRemoveClick(((DataDiagnosticListItem) item).dataDiagnostic);
        }
    }

    private static class ListItem {
        int codeBackgroundResId;
        String code;
        String description;

        ListItem(int codeBackgroundResId, String code, String description) {
            this.codeBackgroundResId = codeBackgroundResId;
            this.code = code;
            this.description = description;
        }
    }

    private static class MalfunctionListItem extends ListItem {
        Malfunction malfunction;

        MalfunctionListItem(Malfunction malfunction, String desc) {
            super(R.drawable.rounded_corner_red, malfunction.getDmoValue(), desc);
            this.malfunction = malfunction;
        }
    }

    private static class DataDiagnosticListItem extends ListItem {
        DataDiagnosticEnum dataDiagnostic;

        DataDiagnosticListItem(DataDiagnosticEnum dataDiagnostic, String desc) {
            super(R.drawable.rounded_corner_gold, dataDiagnostic.toDMOEnum(), desc);
            this.dataDiagnostic = dataDiagnostic;
        }
    }

    private static class ViewHolder {
        View codeWrapper;
        TextView code;
        TextView description;
        View removeButton;

        ViewHolder(View v) {
            codeWrapper = v.findViewById(R.id.code_wrapper);
            code = (TextView) v.findViewById(R.id.code);
            description = (TextView) v.findViewById(R.id.description);
            removeButton = v.findViewById(R.id.remove);
        }
    }

    private class ListAdapter extends BaseAdapter {

        private final List<ListItem> items;

        ListAdapter(List<Malfunction> malfunctions, List<DataDiagnosticEnum> dataDiagnostics) {
            super();
            Resources resources = getResources();


            items = new ArrayList<>();
            for (Malfunction malfunction : malfunctions)
                items.add(new MalfunctionListItem(malfunction, resources.getString(malfunction.getDescriptionKey())));
            for (DataDiagnosticEnum dataDiagnostic : dataDiagnostics)
                items.add(new DataDiagnosticListItem(dataDiagnostic, resources.getString(dataDiagnostic.getDescriptionKey())));
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grd_adminmalfunctionanddatadiagnostic, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ListItem item = (ListItem) getItem(position);
            holder.codeWrapper.setBackgroundResource(item.codeBackgroundResId);
            holder.code.setText(item.code);
            holder.description.setText(item.description);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRemoveItemClick(item);
                }
            });

            return convertView;
        }
    }
}
