package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDeviceDiscovery.DeviceDiscoveryActions;
import com.jjkeller.kmb.interfaces.IDeviceDiscovery.DeviceDiscoveryControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbui.R;

public class DeviceDiscoveryFrag extends BaseFragment {
	DeviceDiscoveryActions actionsListener;
	DeviceDiscoveryControllerMethods controllerListener;
	TextView _lblMessage;
	Button _btnDiscover;
	Button _btnActivate;
	Button _btnRelease;
	Button _btnCancel;
	Spinner _cboDeviceList;
	CheckBox _chkDefaultDevice;
	CheckBox _chkProvisionNewDevice;
	TextView _lblSerialNumber;
	TextView _tvSerialNumber;
	private String defaultDeviceString;
	private String setDefaultDeviceString;


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_devicediscovery, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)
	{
		_btnDiscover = (Button)v.findViewById(R.id.btnDiscover);
		_btnDiscover.setOnClickListener(
            new OnClickListener() {
            	public void onClick(View v) {
            		actionsListener.handleDiscoverButtonClick();
            	}
            });
		
		_btnActivate = (Button)v.findViewById(R.id.btnActivate);
		_btnActivate.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleActivateButtonClick();
	            	}
	            });

		_btnRelease = (Button)v.findViewById(R.id.btnRelease);
		_btnRelease.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleReleaseButtonClick();
	            	}
	            });

		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		_btnCancel.setOnClickListener(
            new OnClickListener() {
            	public void onClick(View v) {
            		actionsListener.handleCancelButtonClick();
            	}
            });	
		
		_lblMessage = (TextView)v.findViewById(R.id.lblMessage);	
		_cboDeviceList = (Spinner)v.findViewById(R.id.cboDeviceList);
		_chkDefaultDevice = (CheckBox)v.findViewById(R.id.chkDefault);
		_chkDefaultDevice.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDefaultEobrClick();
	            	}
	            });		
		
		_chkProvisionNewDevice = (CheckBox)v.findViewById(R.id.chkProvisionNewDevice);
		_chkProvisionNewDevice.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleProvisionNewEobrClick();
	            	}
	            });		
		
		_lblSerialNumber = (TextView)v.findViewById(R.id.lblSerialNumber);
		_tvSerialNumber = (TextView)v.findViewById(R.id.txtSerialNumber);
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			TextView title = (TextView) v.findViewById(R.id.lblTitle);
			title.setText(getString(R.string.lbleobrdiscoverytitle));
			_lblSerialNumber.setText(getString(R.string.lblentereobrserialnumber));
			_chkProvisionNewDevice.setText(getString(R.string.lblprovisionneweobr));
			defaultDeviceString = getString(R.string.lbldefaulteobrcolon);
			setDefaultDeviceString = getString(R.string.set_default_eobr);
		} else {
			defaultDeviceString = getString(R.string.lbldefaulteld);
			setDefaultDeviceString = getString(R.string.set_default_eld);
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (DeviceDiscoveryActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceDiscoveryActions");
        }
        try {
        	controllerListener = (DeviceDiscoveryControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceDiscoveryControllerMethods");
        }
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
    	// 11/14/11 JHM - Save "discovery" boolean in state bundle
		outState.putBoolean("initialdiscovercomplete", true);
		outState.putBoolean("btnDiscoverEnabled", this.GetDiscoverButton().isEnabled());
		outState.putBoolean("btnActivateEnabled", this.GetActivateButton().isEnabled());
		outState.putBoolean("btnReleaseEnabled", this.GetReleaseButton().isEnabled());
		outState.putBoolean("chkDefaultEobrEnabled", this.GetDefaultDeviceCheckbox().isEnabled());
		outState.putBoolean("chkProvisionNewEobrChecked", this.GetProvisionNewDeviceCheckbox().isChecked());
		outState.putString("tvSerialNumberText", this.GetSerialNumberTV().getText().toString());
		outState.putString("lblMessage", this.GetMessageLabel().getText().toString());
		if(_cboDeviceList.getVisibility() != View.GONE)
		{
			EobrDeviceDescriptor[] devices = new EobrDeviceDescriptor[this.GetDeviceListDropdown().getAdapter().getCount()];
			for( int i = 0; i < this.GetDeviceListDropdown().getAdapter().getCount(); i++)
			{
				devices[i] = (EobrDeviceDescriptor) this.GetDeviceListDropdown().getItemAtPosition(i);
			}
			outState.putParcelableArray("cboDeviceList", devices);
		}
		
		super.onSaveInstanceState(outState);
		
	}	

	private void loadControls(Bundle savedInstanceState)
	{
        try 
        {
        	EobrDeviceDescriptor[] deviceList = null;
        	
    		if(getActivity().getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess))) {
    			deviceList = (EobrDeviceDescriptor[]) getActivity().getIntent().getParcelableArrayExtra("devicelist");
    			this.GetCancelButton().setText(this.getString(com.jjkeller.kmbui.R.string.lblcontinue));
    		}
        	
        	if(savedInstanceState != null)
        	{
        		this.GetDiscoverButton().setEnabled(savedInstanceState.getBoolean("btnDiscoverEnabled"));
        		this.GetActivateButton().setEnabled(savedInstanceState.getBoolean("btnActivateEnabled"));
        		this.GetReleaseButton().setEnabled(savedInstanceState.getBoolean("btnReleaseEnabled"));
        		this.GetDefaultDeviceCheckbox().setEnabled(savedInstanceState.getBoolean("chkDefaultEobrEnabled"));
        		this.GetMessageLabel().setText(savedInstanceState.getString("lblMessage"));
        		this.GetProvisionNewDeviceCheckbox().setChecked(savedInstanceState.getBoolean("chkProvisionNewEobrChecked"));
        		actionsListener.handleProvisionNewEobrClick();
        		this.GetSerialNumberTV().setText(savedInstanceState.getString("tvSerialNumberText"));

        		deviceList = (EobrDeviceDescriptor[]) savedInstanceState.getParcelableArray("cboDeviceList");
        		
        		if(deviceList != null) {
        			// build the cbo with each named device
            		ArrayAdapter<EobrDeviceDescriptor> eobrDeviceAdapter = new ArrayAdapter<EobrDeviceDescriptor>(getActivity(), R.layout.kmb_spinner_item, deviceList);
            		eobrDeviceAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            	    this.GetDeviceListDropdown().setAdapter(eobrDeviceAdapter);
            	    this.GetDeviceListDropdown().setVisibility(View.VISIBLE);
        		} else {
					this.GetDeviceListDropdown().setVisibility(View.GONE);
				}
        	}
        	else
        	{
	            // turn off Activate until discover is successful
            	this.GetActivateButton().setEnabled(false);
	        	this.GetReleaseButton().setEnabled(false);
                //_chkDefaultDevice.setEnabled(true);

        		if(deviceList != null)
        		{
        			// build the cbo with each named device
            		ArrayAdapter<EobrDeviceDescriptor> eobrDeviceAdapter = new ArrayAdapter<EobrDeviceDescriptor>(getActivity(), R.layout.kmb_spinner_item, deviceList);
            		eobrDeviceAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            		this.GetDeviceListDropdown().setAdapter(eobrDeviceAdapter);
            		this.GetDeviceListDropdown().setVisibility(View.VISIBLE);
	                this.GetActivateButton().setEnabled(true);
        		}
        		else
        			this.GetDeviceListDropdown().setVisibility(View.GONE);
	        		    		
	            String _newEobrName = controllerListener.getCurrentEobrIdentifier();
	            String _newEobrAddress = controllerListener.getCurrentEobrMacAddress();
	            if (_newEobrName == null || _newEobrName.compareTo("") == 0)
	            {
	            	this.GetMessageLabel().setText(getString(R.string.msg_nopartnershipestablished));
	            }
	            else
	            {
	                String connectionState = "Disconnected";	                
	                if (controllerListener.IsEobrDeviceOnline())
	                {
	                    connectionState = "Online";
	                    this.GetReleaseButton().setEnabled(true);
	                    
	                    // 10/7/11 JHM - Prevent Discovery without releasing current EOBR.
	                    this.GetDiscoverButton().setEnabled(false);
	                }
	                this.GetMessageLabel().setText(String.format(getString(R.string.msg_currentpartnership), _newEobrName, connectionState));
	            }

	    		SharedPreferences userPref = getActivity().getSharedPreferences(getString(R.string.sharedpreferencefile), 0);
	    		String defaultEobr = userPref.getString(getString(R.string.defaulteobr), "");
	    		String defaultMacAddress = userPref.getString(getString(R.string.macaddress), "");
                if(_newEobrAddress != null && _newEobrName != null && defaultMacAddress.compareTo(_newEobrAddress) == 0 && defaultEobr.compareTo(_newEobrName) != 0){
                	// found a device with a different device name than the saved name,
                	// so update the saved userPreference for the eobr name
                	defaultEobr = _newEobrName;
        			SharedPreferences.Editor editor = userPref.edit();
        			editor.putString(getString(R.string.defaulteobr), defaultEobr);
        			editor.commit();
                }
        	}
			SharedPreferences userPref = getActivity().getSharedPreferences(getString(R.string.sharedpreferencefile), 0);
			String defaultEobr = userPref.getString(getString(R.string.defaulteobr), "");
			if(defaultEobr.length() > 0) {
				String defaultDeviceMessage = defaultDeviceString + " " + defaultEobr;
				this.GetDefaultDeviceCheckbox().setChecked(true);
				this.GetDefaultDeviceCheckbox().setText(defaultDeviceMessage);
			} else {
				this.GetDefaultDeviceCheckbox().setText(setDefaultDeviceString);
			}
        }
        catch (KmbApplicationException kae)
        {
            this.HandleFragmentException(kae);
        }
    }

    public Button GetDiscoverButton()
    {
    	if (_btnDiscover == null)
    	{
    		_btnDiscover = (Button)getView().findViewById(R.id.btnDiscover);
    	}
    	return _btnDiscover;
    }
    public Button GetActivateButton()
    {
    	if (_btnActivate == null)
    	{
    		_btnActivate = (Button)getView().findViewById(R.id.btnActivate);
    	}
    	return _btnActivate;
    }
    public Button GetReleaseButton()
    {
    	if (_btnRelease == null)
    	{
    		_btnRelease = (Button)getView().findViewById(R.id.btnRelease);
    	}
    	return _btnRelease;
    }
    public Button GetCancelButton()
    {
    	if (_btnCancel == null)
    	{
    		_btnCancel = (Button)getView().findViewById(R.id.btnCancel);
    	}
    	return _btnCancel;
    }
    public TextView GetMessageLabel()
    {
    	if (_lblMessage == null)
    	{
    		_lblMessage = (TextView)getView().findViewById(R.id.lblMessage);
    	}
    	return _lblMessage;
    }    
	public Spinner GetDeviceListDropdown()
    {
    	if (_cboDeviceList == null)
    	{
    		_cboDeviceList = (Spinner)getView().findViewById(R.id.cboDeviceList);
    	}
    	return _cboDeviceList;
    }
	public CheckBox GetDefaultDeviceCheckbox()
    {
    	if (_chkDefaultDevice == null)
    	{
    		_chkDefaultDevice = (CheckBox)getView().findViewById(R.id.chkDefault);
    	}
    	return _chkDefaultDevice;
    }
	public CheckBox GetProvisionNewDeviceCheckbox()
    {
    	if (_chkProvisionNewDevice == null)
    	{
    		_chkProvisionNewDevice = (CheckBox)getView().findViewById(R.id.chkProvisionNewDevice);
    	}
    	return _chkProvisionNewDevice;
    }
	public TextView GetSerialNumberLabel()
    {
    	if (_lblSerialNumber == null)
    	{
    		_lblSerialNumber = (TextView)getView().findViewById(R.id.lblSerialNumber);
    	}
    	return _lblSerialNumber;
    }
	public TextView GetSerialNumberTV()
    {
    	if (_tvSerialNumber == null)
    	{
    		_tvSerialNumber = (TextView)getView().findViewById(R.id.txtSerialNumber);
    	}
    	return _tvSerialNumber;
    }

}
