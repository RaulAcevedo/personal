package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RodsBorderFrag;
import com.jjkeller.kmb.interfaces.IRodsBorder.RodsBorderFragActions;
import com.jjkeller.kmb.interfaces.IRodsBorder.RodsBorderFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ExemptLogValidationController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;

public class RodsBorder extends BaseActivity 
									implements RodsBorderFragActions, RodsBorderFragControllerMethods{
	RodsBorderFrag _contentFrag;	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
	
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	public LogEntryController getMyController()
	{
		return (LogEntryController)this.getController();
	}
	
	@Override
	protected void InitController() {
		LogEntryController logEntryCtrl = new LogEntryController(this);
	
		this.setController(logEntryCtrl);	
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();
		loadContentFragment(new RodsBorderFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RodsBorderFrag)f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
	@Override
    protected void Return(boolean success)
	{
		// Dismiss screen to return to RodsEntry
		finish();
	}
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
        this.getMyController().PerformBorderCrossingChange(CreateExactTime());
        
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        ExemptLogValidationController elc = new ExemptLogValidationController(this);
		elc.PerformCompleteValidationForCurrentLog(currentLog, true);
		
		isSuccessful = true;
		return isSuccessful;
	}

	
	public void handleCancelButtonClick()
	{
		Return();
	}
	public void handleOKButtonClick()
	{
		String verificationErrors = null;
		
        // save the new time
        Date startTime = CreateExactTime();

        // verify that the requested time is OK to update
        verificationErrors = this.getMyController().VerifyTimestampForNewEvent(startTime);
        if (verificationErrors == null || verificationErrors.compareTo("") == 0)
        {
        	EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        	IAPIController elc = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			if (elc.IsLogExemptEligible(currentLog))
			{
				this.ShowConfirmationMessage(this, R.string.lblexemptchangerulesettitle,
						getString(R.string.lblexemptchangerulesetmessage), R.string.oklabel,
						new ShowMessageClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								super.onClick(dialog, id);
								dialog.dismiss();
								mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
								mSaveLocalDataTask.execute();
							}
						}, R.string.cancellabel, new ShowMessageClickListener());
			}
			else
			{
				mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
				mSaveLocalDataTask.execute();
			}
        }
        
        // display the verification messages, if any
        if( verificationErrors != null && verificationErrors.length() > 0 ) 
        {
        	this.ShowMessage(this, verificationErrors);
        }
	}
	
    private Date CreateExactTime()
    {
    	String currentDate = DateUtility.getDateFormat().format(this.getController().getCurrentClockHomeTerminalTime());
        String strExactTime = _contentFrag.getTimeButton().getText().toString();
        Date exactTime = null;
        try {
        	exactTime = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(currentDate + " " + strExactTime);
		} catch (ParseException e) {
			
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		return exactTime;
    }

}
