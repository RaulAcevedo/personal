package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRptTroubleEOBRDevice.RptTroubleEOBRDeviceControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.Date;

public class RptTroubleEOBRDeviceFrag extends BaseFragment
{
	private RptTroubleEOBRDeviceControllerMethods _controllerListener;

	private TextView _lblTractorNumber;
	private TextView _lblSerialNumber;
	private TextView _lblDiscoverPasskey;
	private TextView _lblDataCollection;
	private TextView _lblSleepModeTimeout;
	private TextView _lblEngineDataBus;
	private TextView _lblEobrVersionNbr;
	private TextView _lblBootloaderVersion;
	private TextView _lblUsbVersionNbr;
	private TextView _lblRecordVersionNbr;
	private TextView _lblDllVersionNbr;
	private TextView _lblEobrClock;
	private TextView _lblDeviceClock;
	private TextView _lblReference;
	private TextView _lblOdometerOffset;
	private TextView _lblOdometerMultiplier;
	private TextView _lblTachometerThreshold;
	private TextView _lblSpeedometerThreshold;
	private TextView _lblHardBrakeThreshold;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rpttroubleeobrdevice, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_lblTractorNumber = (TextView)v.findViewById(R.id.lblTractorNumber);
		_lblSerialNumber = (TextView)v.findViewById(R.id.lblSerialNumber);
		_lblDiscoverPasskey = (TextView)v.findViewById(R.id.lblDiscoveryPasskey);
		_lblDataCollection = (TextView)v.findViewById(R.id.lblDataCollection);
		_lblSleepModeTimeout = (TextView)v.findViewById(R.id.lblSleepModeTimeout);
		_lblEngineDataBus = (TextView)v.findViewById(R.id.lblEngineDataBus);
		_lblEobrVersionNbr = (TextView)v.findViewById(R.id.lblEobrVersionNbr);
		_lblBootloaderVersion = (TextView)v.findViewById(R.id.lblBootloaderVersion);
		_lblUsbVersionNbr = (TextView)v.findViewById(R.id.lblUsbVersionNbr);
		_lblRecordVersionNbr = (TextView)v.findViewById(R.id.lblRecordVersionNbr);
		_lblDllVersionNbr = (TextView)v.findViewById(R.id.lblDllVersionNbr);
		_lblEobrClock = (TextView)v.findViewById(R.id.lblEobrClock);
		_lblDeviceClock = (TextView)v.findViewById(R.id.lblDeviceClock);
		_lblReference = (TextView)v.findViewById(R.id.lblReference);
		_lblOdometerOffset = (TextView)v.findViewById(R.id.lblOdometerOffset);
		_lblOdometerMultiplier = (TextView)v.findViewById(R.id.lblOdometerMultiplier);
		_lblTachometerThreshold = (TextView)v.findViewById(R.id.lblTachometerThreshold);
		_lblSpeedometerThreshold = (TextView)v.findViewById(R.id.lblSpeedometerThreshold);
		_lblHardBrakeThreshold = (TextView)v.findViewById(R.id.lblHardBrakeThreshold);
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			TextView title = (TextView) v.findViewById(R.id.lblTitle);
			TextView eobrVersionNbrLabel = (TextView) v.findViewById(R.id.lblEobrVersionNbrLabel);
			TextView eobrClockLabel = (TextView) v.findViewById(R.id.lblEobrClockLabel);
			eobrClockLabel.setText(getString(R.string.lbleobrclock));
			eobrVersionNbrLabel.setText(getString(R.string.lbleobrversionnbr));
			title.setText(getString(R.string.lbleobrconfiguration));
		}
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_controllerListener = (RptTroubleEOBRDeviceControllerMethods)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement RptTroubleEOBRDeviceControllerMethods");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		loadControls();
	}

	private void loadControls()
	{
		_lblTractorNumber.setText(_controllerListener.getMyController().GetEobrIdentifier());
		_lblSerialNumber.setText(_controllerListener.getMyController().GetEobrSerialNumber());
		_lblDiscoverPasskey.setText(_controllerListener.getMyController().GetEobrCompanyPassKey());
		_lblDataCollection.setText(String.valueOf(_controllerListener.getMyController().GetEobrDataInterval()));
		_lblSleepModeTimeout.setText(String.valueOf(_controllerListener.getMyController().GetEngineTimeoutDuration()));
		if(GlobalState.getInstance().getCompanyConfigSettings(getActivity()).getIsGeotabEnabled())
		{
			_lblEngineDataBus.setText("");
		} else {
			_lblEngineDataBus.setText(_controllerListener.getMyController().GetBusType().getString(getActivity()));
		}

		Bundle versionInfo = _controllerListener.getMyController().GetEobrVersionInfo();
		if (versionInfo != null)
		{
			_lblEobrVersionNbr.setText(versionInfo.getString(this.getString(R.string.mainfirmwarerevision)));
			_lblRecordVersionNbr.setText(versionInfo.getString(this.getString(R.string.recordrevision)));
			_lblUsbVersionNbr.setText(versionInfo.getString(this.getString(R.string.usbfirmwarerevision)));
			_lblDllVersionNbr.setText(versionInfo.getString(this.getString(R.string.eobrdllsrevision)));
			_lblBootloaderVersion.setText(versionInfo.getString(this.getString(R.string.bootloaderrevision)));
		}
		else
		{
			_lblEobrVersionNbr.setText("");
			_lblRecordVersionNbr.setText("");
			_lblUsbVersionNbr.setText("");
			_lblDllVersionNbr.setText("");
			_lblBootloaderVersion.setText("");
		}

		Date timestamp = _controllerListener.getMyController().GetEobrDeviceTime();
		if (timestamp != null)
			_lblEobrClock.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(timestamp));

		_lblDeviceClock.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(DateUtility.getCurrentDateTimeWithSecondsUTC()));

		timestamp = _controllerListener.getMyController().GetEobrReferenceTimestamp();
		if (timestamp != null)
			_lblReference.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(_controllerListener.getMyController().GetEobrReferenceTimestamp()));

		_lblOdometerMultiplier.setText(String.valueOf(_controllerListener.getMyController().GetEobrOdometerMultiplier()));

		float offset = _controllerListener.getMyController().GetEobrOdometerOffset();
		if (_controllerListener.getMyController().getCurrentUser().getDistanceUnits().equalsIgnoreCase(getString(R.string.kilometers)))
			offset = offset * GlobalState.MilesToKilometers;

		_lblOdometerOffset.setText(String.valueOf(offset));
		
		Bundle thresholdValues = _controllerListener.getMyController().GetEobrThresholdValues();
		if(thresholdValues != null){
			_lblTachometerThreshold.setText(Integer.toString(thresholdValues.getInt(this.getString(R.string.rpmthreshold))));
			_lblSpeedometerThreshold.setText(Float.toString(thresholdValues.getFloat(this.getString(R.string.speedthreshold))));
			_lblHardBrakeThreshold.setText(Float.toString(thresholdValues.getFloat(this.getString(R.string.hardbrakethreshold))));
		}else{
			_lblTachometerThreshold.setText("");
			_lblSpeedometerThreshold.setText("");
			_lblHardBrakeThreshold.setText("");
		}
	}
}
