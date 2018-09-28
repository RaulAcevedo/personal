package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRptTroubleApplication.RptTroubleApplicationFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LocationCodeDictionary;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.LocationCodeController;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbui.R;

public class RptTroubleApplicationFrag extends BaseFragment
{
	private RptTroubleApplicationFragControllerMethods _controllerListener;

	private int _localLogCount;
	private int _serverLogCount;
	private LocationCodeDictionary _lcDictionary;

	private TextView _lblAppVersion;
	private TextView _lblUserName;
	private TextView _lblEmployeeName;
	private TextView _lblEmployeeId;
	private TextView _lblIsUserTheDriver;
	private TextView _lblCommMode;
	private TextView _lblDiscoveryPasskey;
	private TextView _lblHomeTerminalDOTNumber;
	private TextView _lblDMOCompName;
	private TextView _lblHomeTerminalAddress;
	private TextView _lblIsNetworkAvailable;
	private TextView _lblLocationCodes;
	private TextView _lblLocalLogsCount;
	private TextView _lblServerLogsCount;
	private TextView _lblHardwareOS;
	private TextView _lblKMBWebservices;
	private TextView _lblKMBDBVersion;
	private TextView _lblKMBComplianceTablet;
	private TextView _lblKMBComplianceTabletLabel;
	private TextView _lblMultipleUsersAllowed;
	private TextView _lblDriverLicenseNumberLabel;
	private TextView _lblDriverLicenseNumber;
	private TextView _lblDriverLicenseStateLabel;
	private TextView _lblDriverLicenseState;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rpttroubleapplication, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_lblAppVersion = (TextView)v.findViewById(R.id.lblAppVersion);
		_lblUserName = (TextView)v.findViewById(R.id.lblUserName);
		_lblEmployeeName = (TextView)v.findViewById(R.id.lblEmployeeName);
		_lblEmployeeId = (TextView)v.findViewById(R.id.lblEmployeeId);
		_lblIsUserTheDriver = (TextView)v.findViewById(R.id.lblIsUserTheDriver);
		_lblCommMode = (TextView)v.findViewById(R.id.lblCommMode);
		_lblDiscoveryPasskey = (TextView)v.findViewById(R.id.lblDiscoveryPasskey);
		_lblHomeTerminalDOTNumber = (TextView)v.findViewById(R.id.lblHomeTerminalDOTNumber);
		_lblDMOCompName = (TextView)v.findViewById(R.id.lblDMOCompName);
		_lblHomeTerminalAddress = (TextView)v.findViewById(R.id.lblHomeTerminalAddress);
		_lblIsNetworkAvailable = (TextView)v.findViewById(R.id.lblIsNetworkAvailable);
		_lblLocationCodes = (TextView)v.findViewById(R.id.lblLocationCodes);
		_lblLocalLogsCount = (TextView)v.findViewById(R.id.lblLocalLogsCount);
		_lblServerLogsCount = (TextView)v.findViewById(R.id.lblServerLogsCount);
		_lblHardwareOS = (TextView)v.findViewById(R.id.lblHardwareOS);
		_lblKMBWebservices = (TextView)v.findViewById(R.id.lblKMBWebservices);
		_lblKMBDBVersion = (TextView)v.findViewById(R.id.lblKMBDBVersion);
		_lblKMBComplianceTablet = (TextView)v.findViewById(R.id.lblKMBComplianceTablet);
		_lblKMBComplianceTabletLabel = (TextView)v.findViewById(R.id.lblKMBComplianceTabletLabel);
		_lblMultipleUsersAllowed = (TextView)v.findViewById(R.id.lblMultipleUsersAllowed);
		_lblDriverLicenseNumberLabel = (TextView)v.findViewById(R.id.lblDriverLicenseNumberLabel);
		_lblDriverLicenseNumber = (TextView)v.findViewById(R.id.lblDriverLicenseNumber);
		_lblDriverLicenseStateLabel = (TextView)v.findViewById(R.id.lblDriverLicenseStateLabel);
		_lblDriverLicenseState = (TextView)v.findViewById(R.id.lblDriverLicenseState);

	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_controllerListener = (RptTroubleApplicationFragControllerMethods)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement RptTroubleApplicationFragControllerMethods");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		loadData();
		loadControls();
	}
	
	protected void loadData()
	{
		_localLogCount = _controllerListener.getMyController().GetLocalLogCount();
		_serverLogCount = _controllerListener.getMyController().GetServerLogCount();

		LocationCodeController lcc = new LocationCodeController(getActivity());
		_lcDictionary = lcc.getLocationCodes();
	}

	public void loadControls()
	{
		_lblAppVersion.setText(GlobalState.getInstance().getPackageVersionName());

		LoginCredentials credentials = _controllerListener.getMyController().getCurrentUser().getCredentials();
		_lblUserName.setText(credentials.getUsername());
		_lblEmployeeName.setText(credentials.getEmployeeFullName());
		_lblEmployeeId.setText(credentials.getEmployeeId());

		if (this.IsCurrentUserTheDriver())
			_lblIsUserTheDriver.setText(this.getString(R.string.btnyes));
		else
			_lblIsUserTheDriver.setText(this.getString(R.string.btnno));

		CompanyConfigSettings companyConfig = GlobalState.getInstance().getCompanyConfigSettings(getActivity());

		_lblCommMode.setText(companyConfig.getEobrCommunicationMode().getString(getActivity()));
		_lblDiscoveryPasskey.setText(companyConfig.getEobrDiscoveryPasskey());
		
		if (credentials.getHomeTerminalDOTNumber() == null || credentials.getHomeTerminalDOTNumber().trim().matches(""))
			_lblHomeTerminalDOTNumber.setText("[None]");
		else
			_lblHomeTerminalDOTNumber.setText(credentials.getHomeTerminalDOTNumber());
		
		_lblDMOCompName.setText(companyConfig.getDmoCompanyName());
		
		String addr = credentials.getHomeTerminalAddressLine1();
		if (credentials.getHomeTerminalAddressLine2() != null && !credentials.getHomeTerminalAddressLine2().trim().matches(""))
			addr += "\n" + credentials.getHomeTerminalAddressLine2();
		addr += "\n" + credentials.getHomeTerminalCity() + ", " + credentials.getHomeTerminalStateAbbrev() + " " + credentials.getHomeTerminalZipCode();
		_lblHomeTerminalAddress.setText(addr);

		if (_controllerListener.getMyController().getIsNetworkAvailable())
			_lblIsNetworkAvailable.setText(this.getString(R.string.btnyes));
		else
			_lblIsNetworkAvailable.setText(this.getString(R.string.btnno));

		if (!_lcDictionary.IsEmpty())
			_lblLocationCodes.setText(this.getString(R.string.btnyes));
		else
			_lblLocationCodes.setText(this.getString(R.string.btnno));

		if(companyConfig.getMultipleUsersAllowed())
			_lblMultipleUsersAllowed.setText(this.getString(R.string.btnyes));
		else
			_lblMultipleUsersAllowed.setText(this.getString(R.string.btnno));

		_lblLocalLogsCount.setText(Integer.toString(_localLogCount));

		_lblServerLogsCount.setText(Integer.toString(_serverLogCount));

		_lblHardwareOS.setText(android.os.Build.VERSION.RELEASE + " (SDK API: " + Integer.toString(android.os.Build.VERSION.SDK_INT) + ")");

		_lblKMBWebservices.setText(GlobalState.getInstance().getAppSettings(getActivity()).getKmbWebServiceRESTUrl());

		_lblKMBDBVersion.setText(Integer.toString(AbstractDBAdapter.BUILD_VERSION_CURRENT));
		
		if (DeviceInfo.IsComplianceTablet()) {
			_lblKMBComplianceTabletLabel.setText(R.string.lblKMBComplianceTabletLabel);
			_lblKMBComplianceTablet.setText(R.string.btnyes);
		}
		else {
			_lblKMBComplianceTabletLabel.setVisibility(View.GONE);
			_lblKMBComplianceTablet.setVisibility(View.GONE);
		}

		if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {

			_lblDriverLicenseNumberLabel.setVisibility(View.VISIBLE);
			_lblDriverLicenseNumber.setVisibility(View.VISIBLE);
			_lblDriverLicenseStateLabel.setVisibility(View.VISIBLE);
			_lblDriverLicenseState.setVisibility(View.VISIBLE);

			_lblDriverLicenseNumber.setText(credentials.getDriverLicenseNumber());
			_lblDriverLicenseState.setText(credentials.getDriverLicenseState());

		}
		else {
			_lblDriverLicenseNumberLabel.setVisibility(View.GONE);
			_lblDriverLicenseNumber.setVisibility(View.GONE);
			_lblDriverLicenseStateLabel.setVisibility(View.GONE);
			_lblDriverLicenseState.setVisibility(View.GONE);
		}


	}

	/**
	 * Answer if the currently active user of the app is also the designated
	 * driver.
	 */
	public boolean IsCurrentUserTheDriver()
	{
		boolean isDriver = false;
		User dd = _controllerListener.getMyController().getCurrentDesignatedDriver();
		if (dd != null && _controllerListener.getMyController().getCurrentUser().getCredentials().getEmployeeId().equals(dd.getCredentials().getEmployeeId()))
		{
			isDriver = true;
		}
		return isDriver;
	}
}
