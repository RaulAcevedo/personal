package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.DOTClocks;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

public class ALKPersConvFrag extends BaseFragment
{
	protected TextView _driverNameText;
	protected TextView _personalConveyanceMessageText;
    protected LinearLayout _myLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_alkpersconv, container, false);
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
		_driverNameText = (TextView)v.findViewById(R.id.driver_name);
		_personalConveyanceMessageText = (TextView)v.findViewById(R.id.personalConveyanceMessageText);
		
		_myLayout = (LinearLayout) v.findViewById(R.id.alkPersConvLayout);
	}

	protected void loadControls(Bundle savedInstanceState)
	{
		// 11/28/12 AMO: Set the text to be for the current user, so that when in team driving the title is correct.
		_driverNameText.setText(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeFullName());
		
		// If DOT Clocks then use Night Mode
		Activity parent = getActivity();
		if(parent.getClass() == DOTClocks.class){
			setNightMode();
			dimScreen();
		}
	}
	
	public void setNightMode(){
		boolean nightMode = getDOTClocks_NightMode();
		if(nightMode){
			_myLayout.setBackgroundColor(Color.DKGRAY);
			_driverNameText.setTextColor(Color.GRAY);
			_personalConveyanceMessageText.setTextColor(Color.GRAY);
		}
		else
		{
			_myLayout.setBackgroundColor(Color.WHITE);
			_driverNameText.setTextColor(Color.BLACK);
			_personalConveyanceMessageText.setTextColor(Color.BLACK);
		}
	}
	
	public void dimScreen(){
		
		Activity parent = getActivity();
    	
    	WindowManager.LayoutParams lp = parent.getWindow().getAttributes();
    	
    	boolean nightMode = getDOTClocks_NightMode();
		if(nightMode)
			lp.screenBrightness = 0.1f;
		else
			lp.screenBrightness = -1.0f;
		
		parent.getWindow().setAttributes(lp);
		
	}
}
