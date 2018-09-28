package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IWifiSettings.WifiSettingsFragActions;
import com.jjkeller.kmb.interfaces.IWifiSettings.WifiSettingsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class WifiSettingsFrag extends BaseFragment
{
	private WifiSettingsFragControllerMethods _controllerListener;
	private WifiSettingsFragActions _actionsListener;
	
	private CheckBox _isEnabledCheckbox;
	private TextView _enabledStatusText;
	private View _currentConnectionContainer;
	private ListView _availableNetworks;
	private View _isScanningContainer;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

        try {
        	_controllerListener = (WifiSettingsFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement WifiSettingsFragControllerMethods");
        }

        try {
        	_actionsListener = (WifiSettingsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement WifiSettingsFragActions");
        }
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_wifisettings, container, false);
		findControls(v);
		loadControls();
		return v;
	}

	protected void findControls(View v)
	{
		_isEnabledCheckbox = (CheckBox) v.findViewById(R.id.is_enabled);
		_isEnabledCheckbox.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleIsEnabledClicked();
			}
		});
		
		_enabledStatusText = (TextView) v.findViewById(R.id.enabled_status);
		
		_currentConnectionContainer = v.findViewById(R.id.current_connection_container);
		_isScanningContainer = v.findViewById(R.id.is_scanning_container);
		_availableNetworks = (ListView) v.findViewById(R.id.available_networks);
		_availableNetworks.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				_actionsListener.handleAvailableNetworkClicked(position);
			}
		});
		_availableNetworks.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				_actionsListener.handleAvailableNetworkLongClicked(position);
				return true;
			}
		});
		
		View availableNetworksEmpty = v.findViewById(R.id.available_networks_empty_message);
		_availableNetworks.setEmptyView(availableNetworksEmpty);
	}
	
	protected void loadControls()
	{
		_controllerListener.updateWifiStatusViews();
	}
	
	public CheckBox getIsEnabledCheckbox()
	{
		return _isEnabledCheckbox;
	}
	
	public TextView getEnabledStatusText()
	{
		return _enabledStatusText;
	}
	
	public ListView getAvailableNetworks()
	{
		return _availableNetworks;
	}
	
	public void showAvailableNetworksList(boolean showContainer)
	{
		if (_currentConnectionContainer != null)
			_currentConnectionContainer.setVisibility(showContainer ? View.VISIBLE : View.GONE);
	}
	
	public void showIsScanning(boolean isScanning)
	{
		if (_isScanningContainer != null)
			_isScanningContainer.setVisibility(isScanning ? View.VISIBLE : View.GONE);
	}
}
