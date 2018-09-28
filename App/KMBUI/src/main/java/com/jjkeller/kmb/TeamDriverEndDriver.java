package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.TeamDriverEndDriverFrag;
import com.jjkeller.kmb.interfaces.ITeamDriverEndDriver.TeamDriverEndDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverEndDriver.TeamDriverEndDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;

;

public class TeamDriverEndDriver extends BaseActivity 
									implements TeamDriverEndDriverFragControllerMethods, TeamDriverEndDriverFragActions,
									LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
									LeftNavImgFrag.ActivityMenuIconItems{
	TeamDriverEndDriverFrag _contentFrag;

	String _empCode;
	String _displayName;
	Date _endTime;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
					super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();		
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
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();
		loadContentFragment(new TeamDriverEndDriverFrag());

		loadLeftNavFragment();
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();
	
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (TeamDriverEndDriverFrag)f;
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
		if(success)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	
	public void handleOKButtonClick()
	{
		if(_contentFrag.getDriverSpinner().getSelectedItemPosition() < 0)
		{
			this.ShowMessage(this, getString(R.string.msg_teamdriver_required));
		}
		else
		{
			TeamDriver teamDriver = _contentFrag.getDriverListAdapter().getItem(_contentFrag.getDriverSpinner().getSelectedItemPosition());
			_empCode = teamDriver.getEmployeeCode(); 
			_endTime = null;
			if (!_contentFrag.getEndCheckbox().isChecked())
			{
				_endTime = CreateExactTime();
			}
		
			String msg = this.getMyController().ValidateEnd(_empCode, _endTime);
			if (msg != null && msg.length() > 0)
			{
				this.ShowMessage(this, msg);
			}
			else
			{
				mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
				mSaveLocalDataTask.execute();
			}
		}
	}

	public void handleCancelButtonClick(){
		Return();
	}
	public void handleTeamAtEndChecked()
	{
		_contentFrag.getExactTimeButton().setEnabled(!_contentFrag.getEndCheckbox().isChecked());
	}
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
		this.getMyController().EndTeamDriver(_empCode, _endTime);
		isSuccessful = true;
		return isSuccessful;
	}

	private Date CreateExactTime()
	{
		String currentDate = DateUtility.getDateFormat().format(this.getController().getCurrentClockHomeTerminalTime());
		String strExactTime = _contentFrag.getExactTimeButton().getText().toString();
		Date exactTime = null;
		try {
				exactTime = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(currentDate + " " + strExactTime);
				} catch (ParseException e) {
				
				Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		return exactTime;
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
		return getString(R.string.tripinfo_actionitems_separatedevice);
	}


	public String getActivityMenuIconList()
	{
		return getString(R.string.tripinfo_actionitemsicons_separatedevice);
	}

}
