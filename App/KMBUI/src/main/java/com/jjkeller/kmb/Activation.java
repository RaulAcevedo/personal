package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ActivationFrag;
import com.jjkeller.kmb.interfaces.IActivation.ActivationFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbui.R;

public class Activation extends BaseActivity
				implements ActivationFragActions {

	TextView _tvActivationCode;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		loadControls();
		
/*  6/7/12 JHM - Removing unused code (problematic for TabUI redesign)
		// 9/29/11 JHM - Added handling for progress dialog of AuthenticateTask
        Object retained = getLastNonConfigurationInstance();
        if ( retained instanceof DownloadCompanyConfigSettingsTask )
        {
        	mDownloadCompanyConfigSettingsTask = (DownloadCompanyConfigSettingsTask)retained;
        	if(mDownloadCompanyConfigSettingsTask.getStatus() == Status.RUNNING)
        	{
        		mDownloadCompanyConfigSettingsTask.setContext(this);
        		mDownloadCompanyConfigSettingsTask.showProgressDialog();
        	}
        }
*/	
	}

	protected LoginController getMyController()
	{
		return (LoginController)this.getController();
	}

	protected void InitController()
	{
		LoginController loginCtrl = new LoginController(this);
	
		this.setController(loginCtrl);	
	}

/*	6/7/12 JHM - Removing unused code (problematic for TabUI redesign)

  	@Override
	public Object onRetainNonConfigurationInstance(){
		if(mDownloadCompanyConfigSettingsTask != null  && mDownloadCompanyConfigSettingsTask.getStatus() == Status.RUNNING)
		{
			mDownloadCompanyConfigSettingsTask.dismissProgressDialog();
			return mDownloadCompanyConfigSettingsTask;
		}
		else return super.onRetainNonConfigurationInstance();
	}
*/	
	@Override
	protected void loadControls()
	{
    	super.loadControls();
		loadContentFragment(new ActivationFrag());
	}
	
	public void handleActivateButtonClick()
	{
		if (getMyController().getIsNetworkAvailable())
		{
			ActivationFrag frag = (ActivationFrag)getSupportFragmentManager().findFragmentById(R.id.content_fragment);
			if (frag.getActivationCodeTextbox().getText().length() > 0)
			{
				mDownloadCompanyConfigSettingsTask = new DownloadCompanyConfigSettingsTask();
				mDownloadCompanyConfigSettingsTask.execute();
			}
			else
			{
				this.ShowMessage(this, this.getString(R.string.missing_activationcode));
			}
			
			frag = null;
		}
		else
		{
			ShowMessage(this, getString(R.string.no_network_connection));
		}
	}
	
	public void handleCancelButtonClick()
	{
		// Remove notifications when exiting app
		NotificationUtilities.CancelAllNotifications(Activation.this);
	
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){return true;}
	
	@Override
    protected void Return(boolean success)
	{
		if(success) {
			UnlockScreenRotation();
			this.finish();				
			
			/* Activation successful - display splash screen */
			Bundle extras = new Bundle();
			extras.putBoolean(SplashScreen.SKIP_SPLASH_ANIMATION_EXTRA_KEY, true);
			this.startActivity(SplashScreen.class, extras);
		}
		else
			ShowMessage(this, getString(R.string.invalid_activationcode));
	}
	
	private static DownloadCompanyConfigSettingsTask mDownloadCompanyConfigSettingsTask;
	private class DownloadCompanyConfigSettingsTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pd;
		Exception ex;
		
		public DownloadCompanyConfigSettingsTask()
		{
		}
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}
		
		protected Boolean doInBackground(Void... params) {
			publishProgress();

			try
			{
				ActivationFrag frag = (ActivationFrag)getSupportFragmentManager().findFragmentById(R.id.content_fragment);
				boolean success = getMyController().DownloadCompanyConfigSettings(frag.getActivationCodeTextbox().getText().toString().trim());
				if(success)
				{
					GlobalState.getInstance().setIsNewActivation(true); 					
				}
								
				frag = null;
				
				return true; 
			}
			catch (Exception ex)
			{
				this.ex = ex;
			}

			return false;
		}

        protected void onPostExecute(Boolean isSuccessful){
        	dismissProgressDialog();
        	
        	mDownloadCompanyConfigSettingsTask = null;
        	
        	if (ex != null)
        		if (ex instanceof KmbApplicationException)
        			HandleException((KmbApplicationException)ex);
        		else
        			ShowMessage(Activation.this, getString(R.string.no_network_connection));
        	else
        		Return(isSuccessful);
        }

		// Added public methods so that dialogs and context can be re-established 
		// after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	if(!Activation.this.isFinishing())
        		pd = ProgressDialog.show(Activation.this, "", Activation.this.getResources().getString(R.string.msgactivating));
        }
        
        public void dismissProgressDialog()
        {
        	try
        	{
        		if(pd != null && pd.isShowing()) pd.dismiss();
        	}
        	catch (Exception ex){
        		ErrorLogHelper.RecordMessage(Activation.this, String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, Activation.this.getClass().getSimpleName(), this.getClass().getSimpleName()));
        	}
        }
	}
}