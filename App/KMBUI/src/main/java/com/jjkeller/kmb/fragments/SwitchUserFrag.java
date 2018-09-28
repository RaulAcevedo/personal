package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.jjkeller.kmb.interfaces.ISwitchUser.SwitchUserFragActions;
import com.jjkeller.kmb.interfaces.ISwitchUser.SwitchUserFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;

public class SwitchUserFrag extends BaseFragment{
	SwitchUserFragControllerMethods controlListener;
	SwitchUserFragActions actionsListener;
	
	private String _currentDesignatedDriverId;
	private int _selectItem = -1;
	private Spinner _spnSwitchUser;
	private CheckBox _chkDesignatedDriver;
	private ArrayList<String> _driverList;
	private ArrayList<LoginCredentials> _userCredentials;
	private Button _btnOK;
	private Button _btnCancel;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		int layout = R.layout.f_switchusernew;
		
		View v = inflater.inflate(layout, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		// build the cbo of available drivers
		ArrayList<User> list = controlListener.getMyController().getLoggedInUserList();
				
		_driverList = new ArrayList<String>();
		_userCredentials = new ArrayList<LoginCredentials>();
				
		// build a list of all the current logged in users,
		// select the first one in the list which is not the logged in one
		String currentActiveUserId = controlListener.getMyController().getCurrentUser().getCredentials().getEmployeeId();
		_currentDesignatedDriverId = controlListener.getMyController().getCurrentDesignatedDriver().getCredentials().getEmployeeId();

		boolean userIdFound = false; 
				
		
		for (int i = 0; i < list.size(); i++) {
			User usr = list.get(i);
			String empId = usr.getCredentials().getEmployeeId();
			
			_driverList.add(usr.getCredentials().getUsername());

			_userCredentials.add(usr.getCredentials());
			if(empId != currentActiveUserId && !userIdFound){
				userIdFound = true;
				_selectItem = i; 
			}
		}

		ArrayAdapter<LoginCredentials> adapter = new ArrayAdapter<LoginCredentials>(getActivity(), R.layout.kmb_spinner_item, _userCredentials);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spnSwitchUser.setAdapter(adapter);
		
		if(_selectItem >= 0)
			_spnSwitchUser.setSelection(_selectItem);
	}
	
	protected void findControls(View v){
		_spnSwitchUser = (Spinner)v.findViewById(R.id.spnswitchuser);		
		_chkDesignatedDriver = (CheckBox)v.findViewById(R.id.chkDesignatedDriver);
		_btnOK = (Button)v.findViewById(R.id.su_btnOk);
		_btnCancel = (Button)v.findViewById(R.id.su_btnCancel);
		
		_btnOK.setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
						GlobalState.getInstance().setReviewLogEditsDialogBeenDisplayedOnceOnRODS(false);
	            		actionsListener.HandleOKButtonClick();
	            	}
	            });
		
		_btnCancel.setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.HandleCancelButtonClick();
	            	}
	            });
		
		_spnSwitchUser.setOnItemSelectedListener(
        		new AdapterView.OnItemSelectedListener() {
        		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
   	            		actionsListener.handleUserSelect();
    			   }
					public void onNothingSelected(AdapterView<?> arg0) {
					}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (SwitchUserFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SwitchUserFragActions");
        }
        
        try{
        	controlListener = (SwitchUserFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement SwitchUserFragControllerMethods");
        }
    }
	
	public CheckBox getDesignatedDriverCheckbox(){
		if(_chkDesignatedDriver == null)
			_chkDesignatedDriver = (CheckBox)getView().findViewById(R.id.chkDesignatedDriver);
		return _chkDesignatedDriver;
	}
	
	public Spinner getSwitchUserSpinner(){
		if(_spnSwitchUser == null)
			_spnSwitchUser = (Spinner)getView().findViewById(R.id.spnswitchuser);
		return _spnSwitchUser;
	}
	
	public ArrayList<LoginCredentials> getUserCredentials(){
		return _userCredentials;
	}
	
	public String getCurrentDesignatedDriver(){
		return _currentDesignatedDriverId;
	}
}
