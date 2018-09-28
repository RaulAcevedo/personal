package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RoadsideInspectionFrag;
import com.jjkeller.kmb.interfaces.IRoadsideInspection.RoadsideInspectionFragActions;
import com.jjkeller.kmb.interfaces.IRoadsideInspection.RoadsideInspectionFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

public class RoadsideInspection extends BaseActivity
									implements RoadsideInspectionFragActions, RoadsideInspectionFragControllerMethods{

	RoadsideInspectionFrag _contentFrag;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
	
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();		
		loadContentFragment(new RoadsideInspectionFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RoadsideInspectionFrag)f;
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
    protected void Return(boolean success)
    {
		if(success)
		{
			this.finish();
						
        	// Clear the menu in the process of returning to the RodsEntry activity.
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}        
    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// 12/29/16 JMoen - Menu navigation removed.  User must enter password & OK or select Cancel.
		return true;
	}
    
	public void handleOKButtonClick()
	{
		mEnableRSIModeTask = new EnableRSIModeTask( _contentFrag.getPasswordTextView().getText().toString());
		mEnableRSIModeTask.execute();
	}
	
	public void handleCancelButtonClick()
	{
		// Under Mandate & RSI, return to RSI mandate page on Cancel
		if(getMyController().getRoadsideInspectionMode() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
			finish();
		else
			Return();
	}

	private static EnableRSIModeTask mEnableRSIModeTask;
	private class EnableRSIModeTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pd;
		String _password;

		public EnableRSIModeTask(String password)
		{
			_password = password;
		}

		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}
		
		protected Boolean doInBackground(Void... params) {
			boolean isSuccessful = false;
			
			try
			{
		        // display the wait cursor during this process
				isSuccessful = getMyController().ToggleRoadsideInspectionMode(_password);
			}
			catch(Throwable e){
				ErrorLogHelper.RecordException(RoadsideInspection.this, e);
			}

			return isSuccessful;
		}

        protected void onPostExecute(Boolean isSuccessful) {
        	dismissProgressDialog();
			UnlockScreenRotation();

			if (isSuccessful)
			{
			    final boolean enableRoadsideInspectionMode = getMyController().getRoadsideInspectionMode();
			    if (enableRoadsideInspectionMode && !getMyController().getIsNetworkAvailable() && !getMyController().IsLocationGeocodingComplete())
			    {
			        // going into roadside inspection, but no network is available and some GPS locations need to be decoded
			        ShowMessage(RoadsideInspection.this, null, 
			        		getString(R.string.msgnetworkunavailabledecodefailed), 
			        		new ShowMessageClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									super.onClick(dialog, id);
									PerformReturnNavigation(enableRoadsideInspectionMode);
								}
							});
			    }
			    else
					PerformReturnNavigation(enableRoadsideInspectionMode);
			}
			else
			{
				ShowMessage(RoadsideInspection.this, getString(R.string.msgconfirmationpassworddoesnotmatch));
			}
        }
        
		// Added public methods so that dialogs and context can be re-established 
		// after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	pd = CreateFetchDialog(getString(R.string.msgswitchinginspectionmode));
        }
        
        public void dismissProgressDialog()
        {
        	DismissProgressDialog(RoadsideInspection.this, this.getClass(), pd);
        }

		private void PerformReturnNavigation(boolean enableRoadsideInspectionMode)
		{
			// If enabling RSI and in Mandate mode, navigate to the RSI Mandate screen.
			// Otherwise navigate back/home
			if(enableRoadsideInspectionMode && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
				startActivity(RoadsideInspectionMandate.class);
			else
				Return();
		}
	}
}
