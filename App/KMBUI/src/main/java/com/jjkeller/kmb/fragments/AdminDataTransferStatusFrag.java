package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.jjkeller.kmb.interfaces.IAdminDataTransferStatus;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class AdminDataTransferStatusFrag  extends BaseFragment {
    IAdminDataTransferStatus.AdminDataTransferStatusFragActions actionsListener;
    IAdminDataTransferStatus.AdminDataTransferStatusFragControllerMethods controlListener;

    private Button _btnAddDataTransferFailures;
    private Button _btnAddDataTransferSuccesses;
    private Button _btnClearDataTransferRecords;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_admindatatransferstatus, container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void findControls(View v) {
        _btnAddDataTransferFailures = (Button) v.findViewById(R.id.btnAddDataTransferFailures);
        _btnAddDataTransferFailures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.handleAdminAddDataTransferFailureClick();
            }
        });

        _btnAddDataTransferSuccesses = (Button) v.findViewById(R.id.btnAddDataTransferSuccesses);
        _btnAddDataTransferSuccesses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.handleAdminAddDataTransferSuccessClick();
            }
        });

        _btnClearDataTransferRecords = (Button) v.findViewById(R.id.btnClearDataTransferRecords);
        _btnClearDataTransferRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.handleAdminClearDataTransferRecordsClick();
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (IAdminDataTransferStatus.AdminDataTransferStatusFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AdminFragActions");
        }

        try {
            controlListener = (IAdminDataTransferStatus.AdminDataTransferStatusFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AdminFragControllerMethods");
        }
    }

}


