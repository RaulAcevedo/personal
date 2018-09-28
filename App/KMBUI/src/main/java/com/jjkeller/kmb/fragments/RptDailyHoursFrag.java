package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRptDailyHours.RptDailyHoursFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.calcengine.LogSummary;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbui.R;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RptDailyHoursFrag extends BaseFragment
{
	protected RptDailyHoursFragControllerMethods controlListener;

	//NOTE:  These all need to be changed to the text views for this specific report (not the Location Codes report).
	private GridView _grid;
	private List<LogSummary> _logSummaryList;
	private ArrayList<Bundle> _displayArray;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptdailyhours, container, false);
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
		_logSummaryList = controlListener.getMyController().EmployeeLogsForDailyHoursReport();
		_grid =(GridView)v.findViewById(R.id.rdh_griddailyhours);
	}

	protected void loadControls(Bundle savedInstanceState) {
		if(savedInstanceState == null || savedInstanceState.getParcelableArrayList("displayArray") == null)
			this.buildDisplayList(_logSummaryList);
		else
			_displayArray = savedInstanceState.getParcelableArrayList("displayArray");

		this.loadGrid();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("displayArray", _displayArray);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try{
			controlListener = (RptDailyHoursFragControllerMethods) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DailyHoursFragControllerMethods");
		}
	}

	public ArrayList<Bundle> getDisplayArray(){
		return _displayArray;
	}

	private void loadGrid()
	{
		if(_displayArray != null)
		{
			LogSummaryAdapter adapter = new LogSummaryAdapter(getActivity(), R.layout.grddailyhours, _displayArray);
			_grid.setAdapter(adapter);

		}
	}

	private class LogSummaryAdapter extends ArrayAdapter<Bundle> {

		private ArrayList<Bundle> items;
		public LogSummaryAdapter(Context context, int textViewResourceId, ArrayList<Bundle> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View v = convertView;
			if(v == null){
				LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grddailyhours, null);
			}

			Bundle displayBundle = items.get(position);

			TextView tvDate = (TextView)v.findViewById(R.id.gdh_tvDateValue);
			TextView tvDrive = (TextView)v.findViewById(R.id.gdh_tvDriveValue);
			TextView tvDuty = (TextView)v.findViewById(R.id.gdh_tvDutyValue);
			TextView tvMiles = (TextView)v.findViewById(R.id.gdh_tvMilesValue);
			TextView tvMilesLbl = (TextView)v.findViewById(R.id.gdh_tvMilesLabel);
			TextView tvWeekly = (TextView)v.findViewById(R.id.gdh_tvWeeklyValue);
			TextView _exemptlbl = (TextView)v.findViewById(R.id.txtExemptLog);
			TextView tvDaily = (TextView)v.findViewById(R.id.gdh_tvDailyValue);

			tvDate.setText(displayBundle.getString("date"));
			tvMiles.setText(displayBundle.getString("miles"));
			tvWeekly.setText(displayBundle.getString("weekly"));
			tvDrive.setText(displayBundle.getString("drive"));
			tvDuty.setText(displayBundle.getString("duty"));
			tvDaily.setText(displayBundle.getString("daily"));

			// if user is setup to display as kilometers, change label
			if (((APIControllerBase)controlListener.getMyController()).getCurrentUser().getDistanceUnits().equalsIgnoreCase(getActivity().getString(R.string.kilometers)))
			{
				tvMilesLbl.setText(getActivity().getString(R.string.lblkm));
			}
			else
			{
				tvMilesLbl.setText(getActivity().getString(R.string.lblMiles));
			}


			if (_exemptlbl != null)
			{
				if (displayBundle.getString("isExemptLog") == "true")
					_exemptlbl.setVisibility(View.VISIBLE);
				else
					_exemptlbl.setVisibility(View.GONE);
			}
			return v;

		}
	}

	private void buildDisplayList(List<LogSummary> logSummaryList){

		_displayArray = new ArrayList<Bundle>();

		Bundle displayBundle;
		LogSummary logSummary;
        boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

		for (int i = 0; i < logSummaryList.size(); i++) {

			logSummary = logSummaryList.get(i);
			displayBundle = new Bundle();

			String date = DateUtility.getHomeTerminalDateFormat().format(logSummary.getLogDate());
			displayBundle.putString("date", date);

			if (logSummary.getExemptLogType() != null && logSummary.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL)
			{
				displayBundle.putString("isExemptLog", "true");
			}
			else
			{
				displayBundle.putString("isExemptLog", "false");
			}

			if (logSummary.getDistance() == -1)
			{
				displayBundle.putString("miles", "-");
			}
			else if (((APIControllerBase)controlListener.getMyController()).getCurrentUser().getDistanceUnits().equalsIgnoreCase(getActivity().getString(R.string.kilometers)))
			{
				String distance = String.format("%.1f", logSummary.getDistance() * GlobalState.MilesToKilometers);
				displayBundle.putString("miles", distance);
			}
			else
			{
				String distance = String.format("%.1f", logSummary.getDistance());
				displayBundle.putString("miles", distance);
			}

			if (logSummary.getWeeklyTotalDuration() == null) {
				displayBundle.putString("weekly", "-");
			} else {
                String weeklyTotal = DateUtility.createTimeDurationString(logSummary.getWeeklyTotalDuration(), isMandateEnabled, false);
				displayBundle.putString("weekly", weeklyTotal);
			}

			if (logSummary.getDrivingDuration() == null){
				displayBundle.putString("drive", "-");
			} else {
                String driveTotal = DateUtility.createTimeDurationString(logSummary.getDrivingDuration(), isMandateEnabled, false);
				displayBundle.putString("drive", driveTotal);
			}

			if (logSummary.getOnDutyDuration() == null) {
				displayBundle.putString("duty", "-");
			} else {
                String dutyTotal = DateUtility.createTimeDurationString(logSummary.getOnDutyDuration(), isMandateEnabled, false);
				displayBundle.putString("duty", dutyTotal);
			}

			if (logSummary.getDailyDurationTotal() == null || ! logSummary.getLogExists())
			{
				displayBundle.putString("daily", "-");
			}
			else
			{
                String dailyTotal = DateUtility.createTimeDurationString(logSummary.getDailyDurationTotal(), isMandateEnabled, false);
				displayBundle.putString("daily", dailyTotal);
			}

			_displayArray.add(displayBundle);
		}

	}
}
