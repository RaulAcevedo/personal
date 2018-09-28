package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Activity;

import com.jjkeller.kmb.GeotabConfig;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.geotabengine.GeotabDataEnhanced;
import com.jjkeller.kmbapi.geotabengine.IGeotabListener;
import com.jjkeller.kmbui.R;

public class GeotabConfigFrag extends BaseFragment implements IGeotabListener {

    TextView _txtConnectionStatus;
    TextView _hosDataView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_geotabconfig, container, false);

        _txtConnectionStatus = (TextView) v.findViewById(R.id.txtConnectionStatus);
        _hosDataView = (TextView) v.findViewById(R.id.hosDataView);
        _hosDataView.setMovementMethod(new ScrollingMovementMethod());
        addChangeListeners();
        setConnectionStatus();

        return v;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        removeChangeListeners();
}

    @Override
    public void receiveGeotabData(final GeotabDataEnhanced data) {
        if (isAdded()) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _hosDataView.clearComposingText();
                    _hosDataView.setText(data.toString());
                }
            });
        }
    }



    /**
     * Helper functions
     */

    /**
     * Hook up appropriate Listeners.
     */
    private void addChangeListeners() {
        if (getActivity() != null) {
            GeotabController geotabController = ((GeotabConfig) getActivity()).getGeoTabController();
            if (geotabController != null) {
                geotabController.addListener(this);
            }
        }
    }

    private void removeChangeListeners()
    {
        if (getActivity() != null) {
            GeotabController geotabController = ((GeotabConfig) getActivity()).getGeoTabController();
            if (geotabController != null)
            {
                geotabController.removeListener(this);
            }
        }
    }


    private void setConnectionStatus() {
        String statusText = "";

        if (getActivity() != null) {
            GeotabController geotabController = ((GeotabConfig) getActivity()).getGeoTabController();
            if (geotabController != null) {
                if (!geotabController.IsDeviceAttached()) {
                    statusText = getString(R.string.geotabdevicenotattached);
                } else if (!geotabController.IsDeviceConnected()) {
                    statusText = getString(R.string.geotabdevicenotconnected);
                } else if (!geotabController.IsCurrentDeviceGeotab()) {
                    statusText = getString(R.string.attacheddevicenotgeotab);
                }
                else
                    statusText = getString(R.string.connected);
            }
        }

        _txtConnectionStatus.setText(String.format(getString(R.string.geotabconnectionstatus), statusText));
    }
}
