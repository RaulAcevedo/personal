package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

public class DOTClocksLeftFrag extends DOTClocksFrag {

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_dotclocks_left, container, false);
		findControls(v);
		return v;
	}

	protected void loadControls(Bundle savedInstanceState)
	{
		// 11/28/12 AMO: Set the text to be for the current user, so that when in team driving the title is correct.
		_driverNameText.setText(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeFullName());

		
		// If DOT Clocks then use Night Mode
//		Activity parent = getActivity();
//		if(parent.getClass() == DOTClocks.class){
//			setNightMode();
//			dimScreen();
//		}
	}
}
