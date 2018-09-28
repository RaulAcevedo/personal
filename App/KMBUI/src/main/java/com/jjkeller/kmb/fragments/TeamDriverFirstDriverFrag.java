package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.ITeamDriverFirstDriver.TeamDriverFirstDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverFirstDriver.TeamDriverFirstDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;

public class TeamDriverFirstDriverFrag extends BaseFragment{
	TeamDriverFirstDriverFragActions actionsListener;
	TeamDriverFirstDriverFragControllerMethods controlListener;
	
	private Button _btnDriverOne;
	private Button _btnDriverTwo;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdriverfirstdriver, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
	}
	
	protected void findControls(View v){	
		_btnDriverOne = (Button)v.findViewById(R.id.btnDriverOne);
		_btnDriverTwo = (Button)v.findViewById(R.id.btnDriverTwo);

		ArrayList<User> userList = GlobalState.getInstance().getLoggedInUserList();
		
		final User user1 = userList.get(0);
		final User user2 = userList.get(1);
		
		_btnDriverOne.setText(user1.getCredentials().getEmployeeFullName());
		
		_btnDriverTwo.setText(user2.getCredentials().getEmployeeFullName());
		
		
		_btnDriverOne.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDriverButtonClick(user1.getCredentials().getEmployeeId());
	            	}
	            });
		_btnDriverTwo.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDriverButtonClick(user2.getCredentials().getEmployeeId());
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (TeamDriverFirstDriverFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverFirstDriverFragActions");
        }
        
        try{
        	controlListener = (TeamDriverFirstDriverFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverFirstDriverFragControllerMethods");
        }
    }
}
