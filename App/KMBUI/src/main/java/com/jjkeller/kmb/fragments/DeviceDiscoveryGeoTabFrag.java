package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDeviceDiscoveryGeoTab.GeotabDeviceDiscoveryActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class DeviceDiscoveryGeoTabFrag extends BaseFragment {


    private TextView _lblMessage;
    private Button _btnActivate;
    private Button _btnCancel;
    private Button _btnRelease;
    private GeotabDeviceDiscoveryActions _geotabActionsListener;
    protected boolean _initialDiscoveryPerformed = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.f_devicediscoverygeotab, container, false);
        findControls(v);

        if(savedInstanceState != null)
        {
            if(savedInstanceState.containsKey("initialgeotabdiscovercomplete")){
                _initialDiscoveryPerformed = savedInstanceState.getBoolean("initialgeotabdiscovercomplete");
            }
            if(savedInstanceState.containsKey("btnGeotabActivateEnabled")){
                _btnActivate.setEnabled(savedInstanceState.getBoolean("btnGeotabActivateEnabled"));
            }
            if(savedInstanceState.containsKey("lblGeotabMessage")){
                _lblMessage.setText(savedInstanceState.getString("lblGeotabMessage"));
            }
            if(savedInstanceState.containsKey("btnGeotabReleaseEnabled")){
                _btnRelease.setEnabled(savedInstanceState.getBoolean("btnGeotabReleaseEnabled"));
            }
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.loadControls(savedInstanceState);
    }

    protected void findControls(View v) {
        _btnActivate = (Button) v.findViewById(R.id.btnActivate);
        _btnActivate.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        _geotabActionsListener.handleActivateButtonClick();
                    }
                });

        _btnCancel = (Button) v.findViewById(R.id.btnCancel);
        _btnCancel.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        _geotabActionsListener.handleCancelButtonClick();
                    }
                });

        _lblMessage = (TextView) v.findViewById(R.id.lblMessage);

        _btnRelease = (Button) v.findViewById(R.id.btnRelease);
        _btnRelease.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        _geotabActionsListener.handleReleaseButtonClick();
                    }
                });
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        try
        {
            _geotabActionsListener = (GeotabDeviceDiscoveryActions)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + "must implement GeotaDeviceDiscoveryActions");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("initialgeotabdiscovercomplete", _initialDiscoveryPerformed);

        boolean button = GetActivateButton().isEnabled();
        outState.putBoolean("btnGeotabActivateEnabled", button);
        outState.putString("lblGeotabMessage", this.GetMessageLabel().getText().toString());
        outState.putBoolean("btnGeotabReleaseEnabled", this.GetReleaseButton().isEnabled());
    }


    private void loadControls(Bundle savedInstanceState)
    {

        if(getActivity().getIntent().hasExtra("isloginprocess")){
            this.GetCancelButton().setText(this.getString(com.jjkeller.kmbui.R.string.lblcontinue));
        }

        if(savedInstanceState != null)
        {
            this.GetActivateButton().setEnabled(savedInstanceState.getBoolean("btnGeotabActivateEnabled"));
            this.GetMessageLabel().setText(savedInstanceState.getString("lblGeotabMessage"));
            this.GetReleaseButton().setEnabled(savedInstanceState.getBoolean("btnGeotabReleaseEnabled"));
        }
    }

    public Button GetActivateButton()
    {
        if (_btnActivate == null)
        {
            _btnActivate = (Button)getView().findViewById(R.id.btnActivate);
        }
        return _btnActivate;
    }

    public Button GetCancelButton()
    {
        if (_btnCancel == null)
        {
            _btnCancel = (Button)getView().findViewById(R.id.btnCancel);
        }
        return _btnCancel;
    }

    public Button GetReleaseButton()
    {
        if (_btnRelease == null)
        {
            _btnRelease = (Button)getView().findViewById(R.id.btnRelease);
        }
        return _btnRelease;
    }

    public TextView GetMessageLabel()
    {
        if (_lblMessage == null)
        {
            _lblMessage = (TextView)getView().findViewById(R.id.lblMessage);
        }
        return _lblMessage;
    }
}