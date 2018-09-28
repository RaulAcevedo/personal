package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IOdometerCalibration.OdometerCalibrationFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class OdometerCalibrationFrag extends BaseFragment
{
	private OdometerCalibrationFragActions _actionsListener;
	
	private EditText _txtEditOdometer;
	private TextView _tvMessage;
	private Button _btnSave;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_odometercalibration, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_txtEditOdometer = (EditText)v.findViewById(R.id.txtEditOdometer);
		_tvMessage = (TextView)v.findViewById(R.id.tvMessage);
		
		_btnSave = (Button)v.findViewById(R.id.btnSave);
		_btnSave.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						_actionsListener.handleSaveButtonClick();
					}
				}
			);
		
		v.findViewById(R.id.btnCancel).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v){
						_actionsListener.handleCancelButtonClick();
					}
				}
		);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_actionsListener = (OdometerCalibrationFragActions)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OdometerCalibrationFragActions");
		}
	}

	public EditText getEditOdometerTextBox()
	{
		return _txtEditOdometer;
	}

	public TextView getMessageTextView()
	{
		return _tvMessage;
	}

	public Button getSaveButton()
	{
		return _btnSave;
	}
}
