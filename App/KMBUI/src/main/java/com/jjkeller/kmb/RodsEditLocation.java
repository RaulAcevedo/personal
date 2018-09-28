package com.jjkeller.kmb;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RodsEditLocationFrag;
import com.jjkeller.kmb.interfaces.IRodsEditLocation.RodsEditLocationFragActions;
import com.jjkeller.kmb.interfaces.IRodsEditLocation.RodsEditLocationFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbui.R;

public class RodsEditLocation extends BaseActivity
		implements RodsEditLocationFragActions, RodsEditLocationFragControllerMethods {

	RodsEditLocationFrag _contentFrag;
	String _returnTo = "";
	Location _currentLocation;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(this.getIntent().getStringExtra(getString(R.string.calledfrom))!=null)
		{
			_returnTo = this.getIntent().getStringExtra(getString(R.string.calledfrom));			
		}
		
		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		//mFetchLocalDataTask.execute();
		
		// Somewhere around Android version 3.0, the behavior of the AsyncTask was changed from being a parallel
		// operation to a serialized operation.  Therefore, in order for newer versions of Android to operate as 
		// KMB expects (in a parallel fashion), we need to add the following code.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			mFetchLocalDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else 
		{
			mFetchLocalDataTask.execute();
		}
	}

	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls();
		loadContentFragment(new RodsEditLocationFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RodsEditLocationFrag)f;
	}
	
	@Override
	protected void InitController() {
		LogEntryController logEntryCtrl = new LogEntryController(this);
		
		this.setController(logEntryCtrl);	
	}

	public LogEntryController getMyController()
	{
		return (LogEntryController)this.getController();
	}
	
	public void handleCancelButtonClick()
	{
		this.Return();        
	}
	
	public void handleSaveButtonClick()
	{
    	if(_contentFrag.getMaintainDrivingCheckBox().getVisibility() == View.VISIBLE && _contentFrag.getMaintainDrivingCheckBox().isChecked())
    	{
			// display the wait cursor during this process
			// user has chosen to manually extend driving period
			this.getMyController().ManuallyExtendDrivingSegment();
    		this.Return(true);
    	}
    	else
    	{
    		// normal mode, not manual extending the driving segment
    		if(_contentFrag.getActualLocationEditText().length() == 0)
    		{
    			this.showMsg(this.getResources().getString(R.string.msgactuallocationrequired));
    		}
    		else if(!_contentFrag.getActualLocationEditText().isEnabled())
			{
    			// If the EditText field for Actual Location is Not Enabled, then it was a GPS decoded location
    			// don't save it, because if disconnected then the GPS will be overwritten
    			// Defect 16888 AAZ 7/16/13
				this.Return(true);
			}
    		else
    		{
    			// display the wait cursor during this process save the new location.
    			mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
    			//mSaveLocalDataTask.execute();
    			
    			// Somewhere around Android version 3.0, the behavior of the AsyncTask was changed from being a parallel
    			// operation to a serialized operation.  Therefore, in order for newer versions of Android to operate as 
    			// KMB expects (in a parallel fashion), we need to add the following code.
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    				mSaveLocalDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    			}
    			else {
    				mSaveLocalDataTask.execute();
    			}
    		}
    	}
	}
	
	@Override
	protected boolean saveData()
	{
		String location = _contentFrag.getActualLocationEditText().getText().toString();
		this.getMyController().UpdateCurrentEventLocation(location, true);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.Return();
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
    protected void Return(boolean success)
	{
		//this.finish();
		
		if(success)
		{
			this.getMyController().setLogEventForEdit(null);
			if(_returnTo.length()>0)
				this.startActivity(_returnTo, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			else
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		else Toast.makeText(this, this.getResources().getString(R.string.msgsavinglocationfailed), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		_currentLocation = this.getMyController().GetCurrentEventValues_Location();
		if (_currentLocation == null || _currentLocation.IsEmpty()) {
			return false;
		} else {
			this.CreateOptionsMenu(menu, false);
			return true;
		}
	}

	public void showMessage(String message) {
		ShowMessage(this, message);		
	}

	public boolean getLocalDataTask() {
		// return true if FetchLocalData is complete
		if(mFetchLocalDataTask == null)
			return true;
		else 
			return false;
	}

}
