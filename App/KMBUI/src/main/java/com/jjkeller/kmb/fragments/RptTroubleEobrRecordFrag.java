package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRptTroubleEobrRecord.RptTroubleEobrRecordControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.common.TabDataConversionUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbui.R;

public class RptTroubleEobrRecordFrag extends BaseFragment
{
	private RptTroubleEobrRecordControllerMethods _controllerListener;
	
	private TextView _lblTimestamp;
	private TextView _lblOverallStatus;
	private TextView _lblRecordId;
	private TextView _lblIsEngineRunning;
	private TextView _lblSpeedometer;
	private TextView _lblOdometer;
	private TextView _lblDashboardOdometer;
	private TextView _lblTachometer;
	private TextView _lblGpsTimestamp;
	private TextView _lblGpsLatitude;
	private TextView _lblGpsLongitude;
	private TextView _lblGpsFixIndicator;
	private TextView _lblDisableReadEldVin;
	private TextView _lblDisableReadEldVinLabel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rpttroubleeobrrecord, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_lblTimestamp = (TextView)v.findViewById(R.id.lblTimestamp);
		_lblOverallStatus = (TextView)v.findViewById(R.id.lblOverallStatus);
		_lblRecordId = (TextView)v.findViewById(R.id.lblRecordId);
		_lblIsEngineRunning = (TextView)v.findViewById(R.id.lblIsEngineRunning);
		_lblSpeedometer = (TextView)v.findViewById(R.id.lblSpeedometer);
		_lblOdometer = (TextView)v.findViewById(R.id.lblOdometer);
		_lblDashboardOdometer = (TextView)v.findViewById(R.id.lblDashboardOdometer);
		_lblTachometer = (TextView)v.findViewById(R.id.lblTachometer);
		_lblGpsTimestamp = (TextView)v.findViewById(R.id.lblGPSTimestamp);
		_lblGpsLatitude = (TextView)v.findViewById(R.id.lblGPSLatitude);
		_lblGpsLongitude = (TextView)v.findViewById(R.id.lblGPSLongitude);
		_lblGpsFixIndicator = (TextView)v.findViewById(R.id.lblGPSFixIndicator);
		_lblDisableReadEldVin = (TextView)v.findViewById(R.id.lblDisableReadEldVin);
		_lblDisableReadEldVinLabel = (TextView)v.findViewById(R.id.lblDisableReadEldVinLabel);
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			TextView title = (TextView) v.findViewById(R.id.lblTitle);
			title.setText(getString(R.string.lbleobrrecordtitle));
			_lblDisableReadEldVinLabel.setText(getString(R.string.lblDisableReadEobrVin));
		}
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_controllerListener = (RptTroubleEobrRecordControllerMethods)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement RptTroubleEobrRecordControllerMethods");
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
		StatusRecord rec = _controllerListener.getMyController().GetCurrentEOBRData(false);
		if (rec.getTimestampUtc() != null)
			_lblTimestamp.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(rec.getTimestampUtc()));

		_lblOverallStatus.setText(String.format("0x%x", rec.getOverallStatus()));
		_lblRecordId.setText(String.format("0x%08x", (0xFFFFFFFF & rec.getRecordId())));

		if (rec.getIsEngineRunning())
			_lblIsEngineRunning.setText(this.getString(R.string.btnyes));
		else
			_lblIsEngineRunning.setText(this.getString(R.string.btnno));

		_lblSpeedometer.setText(Float.toString(rec.getSpeedometerReading()));

		_lblTachometer.setText(Float.toString(rec.getTachometer()));

		if (rec.getGpsTimestampUtc() != null)
			_lblGpsTimestamp.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(rec.getGpsTimestampUtc()));
		_lblGpsLatitude.setText(Float.toString(rec.getGpsLatitude()) + ' ' + rec.getNorthSouthInd());
		_lblGpsLongitude.setText(Float.toString(rec.getGpsLongitude()) + ' ' + rec.getEastWestInd());
		_lblGpsFixIndicator.setText(String.valueOf(rec.getPosFixIndicator()));

		float dashboardOdometer = _controllerListener.getMyController().GetEobrDashboardOdometerValue(rec.getOdometerReading());
		if (_controllerListener.getMyController().getCurrentUser().getDistanceUnits().equalsIgnoreCase(this.getString(R.string.kilometers)))
			dashboardOdometer = dashboardOdometer * GlobalState.MilesToKilometers;

		_lblOdometer.setText(TabDataConversionUtil.getOdometerValueForDisplay(rec.getOdometerReading()));
		_lblDashboardOdometer.setText(TabDataConversionUtil.getOdometerValueForDisplay(dashboardOdometer));

		
		EobrReader eobr = EobrReader.getInstance();
		if (eobr.isEobrGen1())
		{
			_lblDisableReadEldVin.setVisibility(View.GONE);
			_lblDisableReadEldVinLabel.setVisibility(View.GONE);
		}
		else
		{
			_lblDisableReadEldVin.setText(eobr.GetDisableReadEldVin() == true ? "Yes" : "No");
		}
	}
}
