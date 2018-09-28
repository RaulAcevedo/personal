package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.TeamDriverNextStepFrag;
import com.jjkeller.kmb.interfaces.ITeamDriverNextStep.TeamDriverNextStepFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverNextStep.TeamDriverNextStepFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbui.R;

public class TeamDriverNextStep extends BaseActivity 
							implements TeamDriverNextStepFragActions, TeamDriverNextStepFragControllerMethods,
							LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{	
	TeamDriverNextStepFrag _contentFrag;
	
	private boolean _isTeamDriver;
	private boolean _isSharedDevice;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		_isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
		_isTeamDriver = getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), false);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new TeamDriverNextStepFrag());
		
		if(_isTeamDriver)
			loadLeftNavFragment();
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (TeamDriverNextStepFrag)f;
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
	

	public void handleLoginButtonClick() {
		/*ShowMessage(this, "Not implemented yet.");*/
		this.startActivity(Login.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		Return();
		
	}

	public void handleDashboardButtonClick() {
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
		if(_isTeamDriver)
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
		else 
			return null;
	}
	

	public String getActivityMenuIconList()
	{
		if(_isTeamDriver)
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
		else 
			return null;
	}
	
}
