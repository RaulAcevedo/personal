package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.SwitchUserFrag;
import com.jjkeller.kmb.interfaces.ISwitchUser.SwitchUserFragActions;
import com.jjkeller.kmb.interfaces.ISwitchUser.SwitchUserFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbui.R;

public class SwitchUser extends BaseActivity 
							implements SwitchUserFragActions, SwitchUserFragControllerMethods,
							LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{
	SwitchUserFrag _contentFrag;	
	
	private boolean _isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	@Override
	protected void InitController() {
		TeamDriverController ctrlr = new TeamDriverController(this);
		this.setController(ctrlr);
		
	}

	public TeamDriverController getMyController()
	{
		return (TeamDriverController)this.getController();
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState){	
		super.loadControls(savedInstanceState);
		loadContentFragment(new SwitchUserFrag());
		loadLeftNavFragment();
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (SwitchUserFrag)f;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
	public void HandleCancelButtonClick(){
		Return();
	}
	
	public void handleUserSelect()
    {
        LoginCredentials activeUserCred = (LoginCredentials)_contentFrag.getUserCredentials().get(_contentFrag.getSwitchUserSpinner().getSelectedItemPosition());
        if (activeUserCred.getEmployeeId().compareTo(_contentFrag.getCurrentDesignatedDriver()) == 0)
        {
        	_contentFrag.getDesignatedDriverCheckbox().setChecked(true);
        	_contentFrag.getDesignatedDriverCheckbox().setEnabled(false);
        }
        else if (this.getMyController().IsVehicleInMotion()) {
			_contentFrag.getDesignatedDriverCheckbox().setChecked(false);
			_contentFrag.getDesignatedDriverCheckbox().setEnabled(false);
		}
		else
		{
        	_contentFrag.getDesignatedDriverCheckbox().setChecked(false);
        	_contentFrag.getDesignatedDriverCheckbox().setEnabled(true);
        }
    }
	
	public void HandleOKButtonClick(){

		LoginCredentials activeUserCred;
		Boolean canSwitch = true; 
        for (int i = 0; i < _contentFrag.getUserCredentials().size(); i++) {
        	String match =  _contentFrag.getUserCredentials().get(i).getEmployeeFullName();
			if(match ==  _contentFrag.getSwitchUserSpinner().getSelectedItem().toString())
			{
				activeUserCred = _contentFrag.getUserCredentials().get(i);
				
		        String newDesignatedDriverId = null;
		        if (_contentFrag.getDesignatedDriverCheckbox().isChecked())
		        {
		            newDesignatedDriverId = activeUserCred.getEmployeeId();
		        }

		        String canSwitchMessage = this.getMyController().canSwitchDriver(newDesignatedDriverId);

		        if(canSwitchMessage == "")
		        	this.getMyController().TeamDriverSwitch(activeUserCred.getEmployeeId(), newDesignatedDriverId);
		        else 
		        {
		        	canSwitch = false;
		        	this.ShowMessage(this, 0, canSwitchMessage, onOkSubmitMessage);
		        }
			}
		}

        // DialogBox will handle the Return call if Team Driver is not allowed to Switch
        if(canSwitch)
        	this.Return();
		
	}

	@Override
    protected void Return(boolean success)
	{
		if(success)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
	}    
	
	@Override
	protected void loadLeftNavFragment()
	{		
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				setLeftNavFragment(new LeftNavImgFrag(R.layout.leftnav_item_imageandtext));
				
				
				// Replace fragment
				transaction.replace(R.id.leftnav_fragment, getLeftNavFragment());

				// Commit the transaction
				transaction.commit();
				
				setLeftNavSelectionItems();
				leftNavLayout.setBackgroundColor(getResources().getColor(R.color.menugray));
			}
		}
	}	
	
	@Override
	public String getActivityMenuItemList()
	{
		if(_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
		{
			return getString(R.string.tripinfo_actionitems_shareddevice);				
		}
		else
		{
			return getString(R.string.tripinfo_actionitems_separatedevice);
		}
	}

	public String getActivityMenuIconList()
	{
		if(_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
		{
			return getString(R.string.tripinfo_actionitemsicons_shareddevice);				
		}
		else
		{
			return getString(R.string.tripinfo_actionitemsicons_separatedevice);
		}
	}
	
	ShowMessageClickListener onOkSubmitMessage = new ShowMessageClickListener() {
	      @Override
	      public void onClick(DialogInterface dialog, int id) {
	 
	        // Call super.onClick to release screen orientation lock
	        super.onClick(dialog, id);
	      }
	   };
}
