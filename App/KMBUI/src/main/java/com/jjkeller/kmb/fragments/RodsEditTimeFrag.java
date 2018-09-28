package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRodsEditTime.RodsEditTimeFragActions;
import com.jjkeller.kmb.interfaces.IRodsEditTime.RodsEditTimeFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RodsEditTimeFrag extends BaseFragment{
	RodsEditTimeFragActions actionsListener;
	RodsEditTimeFragControllerMethods controlListener;
	
	private Button _btnEditTime;
	private Button _btnOk;
	private Button _btnCancel;
	private Calendar cal = null;

	private Bundle _currentEvents;
	private Location _currentLocation;
	private TextView _tvlblStatusValue;
	private TextView _tvlblLocationValue;	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rodsedittime, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)	{	
		_currentEvents = controlListener.getMyController()
                .GetCurrentEventValues()
                .toBundle(
                        getResources().getString(R.string.eventvalues_logdate),
                        getResources().getString(R.string.eventvalues_starttime),
                        getResources().getString(R.string.eventvalues_dutystatus),
                        getResources().getString(R.string.eventvalues_isautomaticdrivingevent),
                        getActivity().getBaseContext());
		_currentLocation = controlListener.getMyController().GetCurrentEventValues_Location();
		
		_tvlblStatusValue = (TextView)v.findViewById(R.id.ret_lblStatusValue);
		_tvlblLocationValue = (TextView)v.findViewById(R.id.ret_lblLocationValue);
		_btnEditTime = (Button)v.findViewById(R.id.ret_btnTime);
			
		_btnOk = (Button)v.findViewById(R.id.ret_btnOk);
		_btnOk.setOnClickListener(
			new OnClickListener() {
	            public void onClick(View v) {
	            	actionsListener.HandleOKButtonClick();
	            }
	           });

		_btnCancel = (Button)v.findViewById(R.id.ret_btnCancel);
		_btnCancel.setOnClickListener(
			new OnClickListener() {
	           	public void onClick(View v) {
	            	actionsListener.HandleCancelButtonClick();
	            }
	        });
		
		_btnEditTime.setOnClickListener(
	        new OnClickListener() {
	            public void onClick(View v) {
	            	ShowTimePickerDialog(_btnEditTime);
	            }
	        });
	}
	
	protected void loadControls(Bundle savedInstanceState) {

		DutyStatusEnum statusEnum =  controlListener.getMyController()
				.GetCurrentEventValues().dutyStatusEnum;
		RuleSetTypeEnum rulesetEnum = controlListener.getMyController().GetCurrentEventValues_RulesetType();
		String statusText = ((BaseActivity)getActivity()).GetDutyStatusDisplayText(statusEnum,rulesetEnum);
		_tvlblStatusValue.setText(statusText);

		cal = Calendar.getInstance();
				
		Date currentEventStartTime = null;
				
		try {
			if(savedInstanceState != null && savedInstanceState.containsKey("onDutyTime"))
				_btnEditTime.setText(savedInstanceState.getString("onDutyTime"));
			else
			{
				currentEventStartTime = DateUtility.getHomeTerminalDateTimeFormat().parse(_currentEvents.getString(getResources().getString(R.string.eventvalues_starttime)));
				cal.setTime(currentEventStartTime);
				updateTimeDisplay(_btnEditTime, cal);
			}
		} catch (ParseException ex) {
			
        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}
				
		String locationName = _currentLocation.ToLocationString();
		if (locationName != null)
			_tvlblLocationValue.setText(_currentLocation.ToLocationString());
		else
			_tvlblLocationValue.setText("");
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{		
		outState.putString("onDutyTime", _btnEditTime.getText().toString());		
		super.onSaveInstanceState(outState);		
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (RodsEditTimeFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RodsEditTimeFragActions");
        }
        
        try{
        	controlListener = (RodsEditTimeFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement RodsEditTimeFragControllerMethods");
        }
    }
	
	public Button getEditTime()
	{
		if(_btnEditTime == null)
			_btnEditTime = (Button)getView().findViewById(R.id.ret_btnTime);
		return _btnEditTime;
	}
	
	public Calendar getCal(){
		return cal;
	}
	
	public void setTimeDisplay(Calendar c){
		_btnEditTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(c.getTime()));
	}
	
	public void setCal(String value, Calendar c){
		if(value == "Hour")
			cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
		else if(value == "Minute")
			cal.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
		else if(value == "Second")
			cal.set(Calendar.SECOND, 0);
	}
	
	public void setCalTimeZone(TimeZone tz){
		cal.setTimeZone(tz);
	}
	
}
