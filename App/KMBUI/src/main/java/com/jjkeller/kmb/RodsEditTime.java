package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RodsEditTimeFrag;
import com.jjkeller.kmb.interfaces.IRodsEditTime.RodsEditTimeFragActions;
import com.jjkeller.kmb.interfaces.IRodsEditTime.RodsEditTimeFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RodsEditTime extends BaseActivity
							implements RodsEditTimeFragActions, RodsEditTimeFragControllerMethods {

	RodsEditTimeFrag _contentFrag;
	private String verificationErrors = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	public LogEntryController getMyController()
	{
		return (LogEntryController)this.getController();
	}
	
	@Override
	protected void InitController() {

		LogEntryController ctrlr = new LogEntryController(this);
		this.setController(ctrlr);		
	}
		
	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new RodsEditTimeFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RodsEditTimeFrag)f;
	}
	
	public void HandleCancelButtonClick(){
		Return();
	}
	
	public void HandleOKButtonClick()
	{		
		try
		{
			// 11/19/12 JHM - Adding date to parse to address issue on Casio device where time off by 1 hour.
			// Example:  User would enter 6:30am and resulting event time would be 5:30am.
			String logDate = DateUtility.getHomeTerminalDateFormat().format(getMyController().getCurrentEmployeeLog().getLogDate());
			Date startTime = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(logDate + " " + _contentFrag.getEditTime().getText().toString());
			
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			
			_contentFrag.setCal("Hour", c);
			_contentFrag.setCal("Minute", c);
			_contentFrag.setCal("Second", c);

			verificationErrors = this.getMyController().VerifyTimestampForUpdate(_contentFrag.getCal().getTime());
			if (verificationErrors == null)
			{
				mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
    			mSaveLocalDataTask.execute();
			}
			else
				_contentFrag.setCalTimeZone(TimeZone.getDefault());
		}
		catch(ParseException pe)
		{
			pe.printStackTrace();
			Log.e("UnhandledCatch", pe.getMessage() + ": " + Log.getStackTraceString(pe));
		}
		
		if (verificationErrors != null)
		{
			this.ShowMessage(this, verificationErrors);
		}
	}
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
		this.getMyController().UpdateCurrentEventTimestamp(_contentFrag.getCal().getTime());
		
		isSuccessful = true;
		return isSuccessful;
	}

	@Override
    protected void Return(boolean success)
	{
		if(success)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		else Toast.makeText(this, this.getString(R.string.msgsavingtimefailed), Toast.LENGTH_SHORT).show();
	}
	
    // updates the time we display in the TextView
	@Override
	protected void updateTimeDisplay(Calendar c) {
		_contentFrag.setTimeDisplay(c);
    }

       
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}
}
