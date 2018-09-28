package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjkeller.kmb.interfaces.IAdminMalfunctionAndDataDiagnostic;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbui.R;

public class AdminMissingDataMalfunctionFrag extends BaseFragment {

    private IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticFragActions actionsListener;

    private Spinner selectEventType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_adminmissingdatamalfunction, container, false);
        findControls(v);
        loadControls();
        return v;
    }

    protected void findControls(View v) {
        selectEventType = (Spinner) v.findViewById(R.id.select_eventtype);

        v.findViewById(R.id.btn_add_malfunction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onAddMalfunctionEventClick(getSelectedEventType());
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
    }

    public void loadControls() {
        String[] eventTypes = Enums.EmployeeLogEldEventType.getList(getActivity());
        ArrayAdapter<String> selectEventTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.kmb_spinner_item, eventTypes);
        selectEventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectEventType.setAdapter(selectEventTypeAdapter);
    }

    private Enums.EmployeeLogEldEventType getSelectedEventType() {
        if (selectEventType != null && selectEventType.getSelectedItem() != null) {
            return Enums.EmployeeLogEldEventType.valueOf(getActivity(), (String) selectEventType.getSelectedItem());
        }
        return null;
    }
}
