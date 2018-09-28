package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.DownloadLogsFrag;
import com.jjkeller.kmb.interfaces.IDownloadLogs.DownloadLogsFragActions;
import com.jjkeller.kmb.interfaces.IDownloadLogs.DownloadLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ExemptLogValidationController;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.text.ParseException;


public class DownloadLogs extends BaseActivity
							implements DownloadLogsFragActions, DownloadLogsFragControllerMethods{
	DownloadLogsFrag _contentFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (DownloadLogsFrag)f;
	}
	
	@Override
	protected void loadControls()
	{
		super.loadControls();		
		loadContentFragment(new DownloadLogsFrag());
	}

	public void HandleDownloadClick(View v){
		new DownloadLogDataTask().execute();
	}
	
	public void HandleDoneClick()
	{
		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
		mSaveLocalDataTask.execute();
	}

	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
		if (_contentFrag._offDutyLogList != null)
		{
			for (String logDate : _contentFrag._offDutyLogList)
			{
				try {
					getMyController().CreateOffDutyLog(DateUtility.getDateFormat().parse(logDate));
				} catch (ParseException e) {
					
		        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}
			}
		}
		isSuccessful = true;
		return isSuccessful;
	}


	@Override
	protected void Return(boolean success)
	{
        startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	
	public IAPIController getMyController()
	{
		return this.getController(APIControllerBase.class);
	}

	@Override
	protected void InitController()
	{
		IAPIController empLogCtrl = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
	
		this.setController(empLogCtrl);	
	}
	
	private class DownloadLogDataTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		Exception ex;
		
		
		public DownloadLogDataTask() { }		
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			if(!DownloadLogs.this.isFinishing())
				pd = ProgressDialog.show(DownloadLogs.this, "", getString(R.string.msgcontacting));
		}
		
		protected Void doInBackground(Void... params) {
			try
			{
				getMyController().DownloadRecordsForCompliance(((APIControllerBase)getMyController()).getCurrentUser(), false);
			}
			catch (KmbApplicationException kae)
			{
				this.ex = kae;
			}

			return null;
		}

		protected void onProgressUpdate(Void... unused) {
		}

        protected void onPostExecute(Void unused) {
        	if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		if (ex.getClass() == KmbApplicationException.class)
        			HandleException((KmbApplicationException)ex);
        	}
        	else
			{
				_contentFrag.getMessageTextView().setText(getString(R.string.msgsuccessfullydownloaded));				
				_contentFrag.Reload();
				if( GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed()) { 
					// validate that all the exempt logs are still valid					
					ExemptLogValidationController ctrlr = new ExemptLogValidationController(DownloadLogs.this);
					ctrlr.PerformCompleteValidationForCurrentLog(GlobalState.getInstance().getCurrentEmployeeLog(), false);
				}
			}
			
        	UnlockScreenRotation();
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}	
}
