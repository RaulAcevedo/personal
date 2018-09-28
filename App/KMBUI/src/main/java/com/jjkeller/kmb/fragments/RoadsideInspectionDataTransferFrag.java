package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjkeller.kmb.interfaces.IRoadsideInspectionDataTransfer;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class RoadsideInspectionDataTransferFrag extends BaseFragment {

    private IRoadsideInspectionDataTransfer.RoadsideInspectionDataTransferActions actionsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_roadsideinspectiondatatransfer, container, false);
        findControls(v);
        return v;
    }

    private void findControls(View v) {
        v.findViewById(R.id.data_transfer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onDataTransferButtonClick();
            }
        });

        v.findViewById(R.id.roadside_inspection_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onRoadsideInspectionModeButtonClick();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (IRoadsideInspectionDataTransfer.RoadsideInspectionDataTransferActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RoadsideInspectionDataTransferActions");
        }
    }

}
