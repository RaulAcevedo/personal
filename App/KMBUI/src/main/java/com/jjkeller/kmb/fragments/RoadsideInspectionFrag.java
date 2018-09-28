package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRoadsideInspection.RoadsideInspectionFragActions;
import com.jjkeller.kmb.interfaces.IRoadsideInspection.RoadsideInspectionFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class RoadsideInspectionFrag extends BaseFragment{
	RoadsideInspectionFragActions actionsListener;
	RoadsideInspectionFragControllerMethods controlListener;
	
	private Button _btnRoadsideInspectionOK;
	private Button _btnRoadsideInspectionCancel;
	private TextView _tvPassword;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_roadsideinspection, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		_tvPassword.requestFocus();
	}
	
	protected void findControls(View v)
	{
		_btnRoadsideInspectionOK = (Button)v.findViewById(R.id.btnroadsideinspectionok);
		_btnRoadsideInspectionCancel = (Button)v.findViewById(R.id.btnroadsideinspectioncancel);
		_tvPassword = (TextView)v.findViewById(R.id.txtpassword);

		_btnRoadsideInspectionOK.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		_btnRoadsideInspectionCancel.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (RoadsideInspectionFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RoadsideInspectionFragActions");
        }
        
        try{
        	controlListener = (RoadsideInspectionFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement RoadsideInspectionFragControllerMethods");
        }
    }
	
	public TextView getPasswordTextView()
	{
		if(_tvPassword == null)
			_tvPassword = (TextView)getView().findViewById(R.id.txtpassword);
		return _tvPassword;
	}
}
