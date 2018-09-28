package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRodsBorder.RodsBorderFragActions;
import com.jjkeller.kmb.interfaces.IRodsBorder.RodsBorderFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Calendar;

public class RodsBorderFrag extends BaseFragment{
	RodsBorderFragActions actionsListener;
	RodsBorderFragControllerMethods controlListener;
	
	private TextView _lblCurrentRule;
	private TextView _lblNewRule;
	private Button _btnTime;
	private Button _btnOk;
	private Button _btnCancel;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rodsborder, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)	{
		_lblCurrentRule = (TextView)v.findViewById(R.id.lblCurrent);
		_lblNewRule = (TextView)v.findViewById(R.id.lblNew);
		_btnTime = (Button)v.findViewById(R.id.btnTime);

		_btnTime.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowTimePickerDialog(_btnTime);
	            	}
	            });
		
		_btnOk = (Button)v.findViewById(R.id.btnOK);
		_btnOk.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		_btnCancel.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });	
	}	

	@Override
	public void onSaveInstanceState(Bundle outState)
	{	
		outState.putString("borderTime", _btnTime.getText().toString());
		super.onSaveInstanceState(outState);
	}
	
	protected void loadControls(Bundle savedInstanceState) {
		RuleSetTypeEnum currentRule = null;
        EmployeeLogFacade empLogFacade = new EmployeeLogFacade(getActivity(), controlListener.getMyController().getCurrentUser());
        EmployeeLogEldEvent logEvent = empLogFacade.FetchMostRecentLogEventForUser();
        if (logEvent != null && logEvent.getRulesetType().getValue() != RuleSetTypeEnum.NULL)
        	currentRule = logEvent.getRulesetType();
        else
        	currentRule = controlListener.getMyController().getCurrentUser().getRulesetTypeEnum();

        RuleSetTypeEnum newRule = controlListener.getMyController().GetBorderCrossingRuleset(currentRule);            

        _lblCurrentRule.setText(currentRule.getString(getActivity()));
        _lblNewRule.setText(newRule.getString(getActivity()));

		if(savedInstanceState != null &&
				savedInstanceState.containsKey("borderTime"))
		{
			if(_btnTime != null)
				_btnTime.setText(savedInstanceState.getString("borderTime"));
		}
		else
		{
			Calendar cal = Calendar.getInstance();
    		cal.setTime(controlListener.getMyController().getCurrentClockHomeTerminalTime());
	
			updateTimeDisplay(_btnTime, cal);
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (RodsBorderFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RodsBorderFragActions");
        }
        
        try{
        	controlListener = (RodsBorderFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement RodsBorderFragControllerMethods");
        }
    }
	
	public Button getTimeButton(){
		if(_btnTime == null)
			_btnTime = (Button)getView().findViewById(R.id.btnTime);
		return _btnTime;
	}
}
