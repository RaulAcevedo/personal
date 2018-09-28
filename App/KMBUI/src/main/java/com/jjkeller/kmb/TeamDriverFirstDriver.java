package com.jjkeller.kmb;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.TeamDriverFirstDriverFrag;
import com.jjkeller.kmb.interfaces.ITeamDriverFirstDriver.TeamDriverFirstDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverFirstDriver.TeamDriverFirstDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbui.R;

public class TeamDriverFirstDriver extends BaseActivity 
							implements TeamDriverFirstDriverFragActions, TeamDriverFirstDriverFragControllerMethods,
							LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{	
	TeamDriverFirstDriverFrag _contentFrag;
	
	private boolean _isSharedDevice;
	
	public BaseActivity getActivity() {
		return this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		_isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new TeamDriverFirstDriverFrag());
		
		loadLeftNavFragment();
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (TeamDriverFirstDriverFrag)f;
	}
	
	public TeamDriverController getMyController()
	{
		return (TeamDriverController)this.getController();
	}
	
	@Override
	protected void InitController() {
		TeamDriverController teamDriverCtrl = new TeamDriverController(this);
	
		this.setController(teamDriverCtrl);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	@Override
    protected void Return(boolean success)
	{
		finish();
	}
	

	public void handleDriverButtonClick(String activeUserId) {

		TeamDriverController ctrlr = new TeamDriverController(this.getActivity());
		ctrlr.TeamDriverSwitch(activeUserId, activeUserId);
		
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		Return();
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
	
}
