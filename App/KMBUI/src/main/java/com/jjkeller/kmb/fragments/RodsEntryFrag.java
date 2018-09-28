package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRodsEditLocation.RodsEditLocationFragActions;
import com.jjkeller.kmb.interfaces.IRodsEntry.RodsEntryFragActions;
import com.jjkeller.kmb.interfaces.IRodsEntry.RodsEntryFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbui.R;

public class RodsEntryFrag extends BaseFragment {

	RodsEntryFragActions actionsListener;
	RodsEntryFragControllerMethods controlListener;
	private ImageButton _btnViewLog;	
	private ImageButton _btnVehicleInspection;
	private TextView _tvEobrConnection;
	private ImageButton _btnEobrConnection;
	private ImageButton _btnLogoff;
	private LinearLayout _roadsideInspectionRow;
	private ImageButton _btnRoadsideInspection;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rodsentry, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		// Determine whether we are in RoadsideInspectionMode or not (Some items should not be clickable)
		boolean roadsideInspection = GlobalState.getInstance().getRoadsideInspectionMode();
				
		_btnViewLog = (ImageButton)v.findViewById(R.id.btnViewLog);
		if(_btnViewLog != null)
		{
			_btnViewLog.setOnClickListener(
		            new OnClickListener() {
		            	public void onClick(View v) {
		            		actionsListener.handleViewLogClick();
		            	}
		            });
		}
		
		_btnVehicleInspection = (ImageButton)v.findViewById(R.id.btnVehicleInspection);
		if(_btnVehicleInspection != null && !roadsideInspection)
		{
			_btnVehicleInspection.setOnClickListener(
		            new OnClickListener() {
		            	public void onClick(View v) {
		            		actionsListener.handleVehicleInspectionClick();
		            	}
		            });
		}

		_tvEobrConnection = (TextView)v.findViewById(R.id.tvLabelEobrConnection);
		_btnEobrConnection = (ImageButton)v.findViewById(R.id.btnEobrConnection);
		if(_btnEobrConnection != null && !roadsideInspection)
		{
			_btnEobrConnection.setOnClickListener(
		            new OnClickListener() {
		            	public void onClick(View v) {
		            		actionsListener.handleEobrConnectionClick();
		            	}
		            });
		}
		
		
		_btnLogoff = (ImageButton)v.findViewById(R.id.btnLogoff);
		if(_btnLogoff != null && !roadsideInspection)
		{
			_btnLogoff.setOnClickListener(
		            new OnClickListener() {
		            	public void onClick(View v) {
		            		actionsListener.handleLogoffClick();
		            	}
		            });	
		}

		_roadsideInspectionRow = (LinearLayout)v.findViewById(R.id.tblRoadsideInspectionRow);
		_btnRoadsideInspection = (ImageButton)v.findViewById(R.id.btnRoadsideInspection);
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			_btnEobrConnection.setContentDescription(getString(R.string.home_eobrconnection));
			//Hide Roadside Inspection Layout
			if(_roadsideInspectionRow != null)
				_roadsideInspectionRow.setVisibility(View.GONE);

		}
		else{
			if (_btnRoadsideInspection != null){
				_btnRoadsideInspection.setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								actionsListener.handleRoadsideInspectionClick();
							}
						}
				);
			}
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (RodsEntryFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + RodsEditLocationFragActions.class.getSimpleName());
        }
        try{
        	controlListener = (RodsEntryFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement " + RodsEntryFragControllerMethods.class.getSimpleName());
        }
    }
	
	protected void loadControls()
	{
		// 9/19/12 JHM - Check for a null user so an exception isn't thrown.
		// Was seen during app termination due to a prior unhandled exception and we're
		// trying to clean up/end the app.
		if(GlobalState.getInstance().getCurrentUser() != null)
			controlListener.ShowCurrentStatus(false);
	}
	
    public TextView getEobrConnectionLabel()
    {
    	if (_tvEobrConnection == null)
    	{
    		_tvEobrConnection = (TextView)getView().findViewById(R.id.tvLabelEobrConnection);
    	}
    	return _tvEobrConnection;
    }

	public void updateEOBRConnectionState(ConnectionState state) {
		int selector = -1;
		
		switch(state)
		{
		case ONLINE:
			selector = R.drawable.connecteobr_green_selector;
			break;
			
		case DEVICEFAILURE:
			selector = R.drawable.connecteobr_red_selector;
			break;
			
		case READINGHISTORICAL:
		case FIRMWAREUPDATE:
			selector = R.drawable.connecteobr_blue_selector;
			break;

		case OFFLINE:
		case SHUTDOWN:
		default:
			selector = R.drawable.connecteobr_gray_selector;
			break;
		}
		
		this._btnEobrConnection.setImageResource(selector);
	}
}
