package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.IConsoleDump.ConsoleDumpFragActions;
import com.jjkeller.kmb.interfaces.IConsoleDump.ConsoleDumpFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.Date;

public class ConsoleDumpFrag extends BaseFragment{
	ConsoleDumpFragActions actionsListener;
	ConsoleDumpFragControllerMethods controlListener;
	
	private Button _btnStartDate;
	private Button _btnStartTime;
	private Button _btnEndDate;
	private Button _btnEndTime;	
	private Button _btnOK;
	private Button _btnCancel;	
	
	private Date _startDate = DateUtility.AddDays(DateUtility.getCurrentDateTimeUTC(), -1);
	private Date _endDate = DateUtility.getCurrentDateTimeUTC();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_consoledump, container, false);
		findControls(v);
		
		loadSavedState(savedInstanceState); 
		
		return v;
	}

	private void loadSavedState(Bundle savedInstanceState) {
		if(savedInstanceState != null)
		{
			if(savedInstanceState.containsKey("startTime"))
				SetStartDate(new Date(savedInstanceState.getLong("startTime"))); 
			
			if(savedInstanceState.containsKey("endTime"))
				SetEndDate(new Date(savedInstanceState.getLong("endTime")));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {	
		outState.putLong("startTime", GetStartDate().getTime());
		outState.putLong("endTime", GetEndDate().getTime()); 
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		updateDateDisplays();
	}
	
	protected void findControls(View v)
	{
		_btnStartDate = (Button)v.findViewById(R.id.btnConsoleDumpStartDate);
		_btnStartTime = (Button)v.findViewById(R.id.btnConsoleDumpStartTime);
		_btnEndDate = (Button)v.findViewById(R.id.btnConsoleDumpEndDate);
		_btnEndTime = (Button)v.findViewById(R.id.btnConsoleDumpEndTime);
		_btnCancel = (Button)v.findViewById(R.id.cp_btnCancel);
		_btnOK = (Button)v.findViewById(R.id.cp_btnOk);
		
		_btnStartDate.setOnClickListener(
			new OnClickListener() {
				public void onClick(View v) {
					ShowDatePickerDialog(_btnStartDate);
				}
			});

		_btnStartTime.setOnClickListener(
			new OnClickListener() {
				public void onClick(View v) {
					ShowTimePickerDialog(_btnStartTime);
				}
			});		
		
		_btnEndDate.setOnClickListener(
			new OnClickListener() {
				public void onClick(View v) {
					ShowDatePickerDialog(_btnEndDate);
				}
			});
		
		_btnEndTime.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						ShowTimePickerDialog(_btnEndTime);
					}
				});		
		
		_btnOK.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleOkButtonClick();
				}
			});

		_btnCancel.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleCancelButtonClick();
				}
			});
	}
	
	private void updateDateDisplays()
	{
		_btnStartDate.setText(DateUtility.getDateFormat().format(_startDate));
		_btnStartTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(_startDate));
		_btnEndDate.setText(DateUtility.getDateFormat().format(_endDate));
		_btnEndTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(_endDate));
	}
	
	public void SetStartDate(Date startDate)
	{
		_startDate = startDate;
		updateDateDisplays();
	}
	
	public Date GetStartDate()
	{
		return _startDate;
	}
	
	public void SetEndDate(Date endDate)
	{
		_endDate = endDate;
		updateDateDisplays();
	}
	
	public Date GetEndDate()
	{
		return _endDate;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (ConsoleDumpFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ConsoleDumpFragActions");
        }
        
        try{
        	controlListener = (ConsoleDumpFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ConsoleDumpFragControllerMethods");
        }
    }
	

}
