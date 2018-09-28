package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ChangePasswordFrag;
import com.jjkeller.kmb.interfaces.IChangePassword.ChangePasswordFragActions;
import com.jjkeller.kmb.interfaces.IChangePassword.ChangePasswordFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

public class ChangePassword extends BaseActivity 
								implements ChangePasswordFragActions, ChangePasswordFragControllerMethods{
		
	ChangePasswordFrag _contentFrag;
	private String _validationMsg = "";
	private Intent nextActivity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepassword);
				
		Bundle extras = getIntent().getExtras();
		boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
		boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
		boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

		if(extras != null)
		{
			String nextActivityName = extras.getString(getString(R.string.extra_nextActivity));
			
			if(nextActivityName != null)
			{
				if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_discovery)))
				{
					if(GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled())
						nextActivity = new Intent(this, DeviceDiscoveryGeoTab.class);
					else
						nextActivity = new Intent(this, DeviceDiscovery.class);
				} else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_tripInfo)))
				{	
					// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else TripInfo screen
					if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled)) {
						nextActivity = new Intent(this, ExemptLogType.class);
					} else
						nextActivity = new Intent(this, TripInfo.class);
						
					nextActivity.putExtra(getString(R.string.extra_tripinfomsg), getString(R.string.extra_tripinfomsg));
					nextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				} else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_rptGridImage)))
				{
					nextActivity = new Intent(this, RptGridImage.class);
				} else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_eobrConfig)))
				{
					nextActivity = new Intent(this, EobrConfig.class);
					nextActivity.putExtra(getString(R.string.extra_displaytripinfo), true);
				} else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_teamDriverDeviceType)))
				{
					nextActivity = new Intent(this, TeamDriverDeviceType.class);
					nextActivity.putExtra(getString(R.string.extra_exemptlogtype), true);
				} else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_exemptlogtype))) {
					nextActivity = new Intent(this, ExemptLogType.class);
				}
				else if(nextActivityName.equalsIgnoreCase(getString(R.string.activity_selectDutyStatus)))
				{
					nextActivity = new Intent(this, SelectDutyStatus.class);
				} else {
					nextActivity = new Intent(this, RodsEntry.class);
					nextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
				
				//pass along some extras if we have them
				if(nextActivity != null && extras.containsKey(getString(R.string.extra_teamdriverlogin)))
					nextActivity.putExtra(getString(R.string.extra_teamdriverlogin), extras.getBoolean(getString(R.string.extra_teamdriverlogin)));			
				if(nextActivity != null && extras.containsKey(getString(R.string.extra_isloginprocess)))
					nextActivity.putExtra(getString(R.string.extra_isloginprocess), extras.getBoolean(getString(R.string.extra_isloginprocess)));		
			}
		}
		
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//if there's an activity we're supposed to go to
		//after this one, disable the back button
		if(nextActivity != null)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();		
		loadContentFragment(new ChangePasswordFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (ChangePasswordFrag)f;
	}
	
	@Override
	protected void InitController() {

		LoginController ctrlr = new LoginController(this);
		this.setController(ctrlr);	
	}
	
	@Override
	protected void Return()
	{
		this.finish();
		
		if(nextActivity != null)
			startActivity(nextActivity);
	}
	
	protected LoginController getMyController()
	{
		return (LoginController)this.getController();
	}
	
	public void handleOkButtonClick()
	{
		// first validate that the passwords are properly entered
		boolean isValid = this.Validate();
		if(isValid)
		{
			mChangePasswordTask = new ChangePasswordTask();
			mChangePasswordTask.execute();
		}
		else if(_validationMsg.length()>0)
		{
			this.ShowMessage(this, _validationMsg);
		}
	}
	
	public void handleCancelButtonClick()
	{
		this.Return();
	}
	
	private boolean Validate()
	{
		boolean isValid = false;
		
		String oldPassword = _contentFrag.getOldPasswordTextView().getText().toString();
		String newPassword = _contentFrag.getNewPasswordTextView().getText().toString();
		String confirmPassword = _contentFrag.getConfirmPasswordTextView().getText().toString();
		_validationMsg = "";
		
		if(!this.getMyController().getCurrentUser().getCredentials().getPassword().equalsIgnoreCase(oldPassword))
		{
			_validationMsg = this.getResources().getString(R.string.msgoldpasswordnotcorrect);
		}
		else
		{
			// old password is correct
			if(oldPassword.equalsIgnoreCase(newPassword))
			{
				_validationMsg = this.getResources().getString(R.string.msgnewpasswordmustbedifferent);
			}
			else if(newPassword.length() == 0)
			{
				_validationMsg = this.getResources().getString(R.string.msgenternewpassword);
			}
			else if(confirmPassword.length() == 0)
			{
				_validationMsg = this.getResources().getString(R.string.msgenterpasswordconfirmation);
			}
			else
			{
				// new password is different than before.
				if(!newPassword.equalsIgnoreCase(confirmPassword))
				{
					_validationMsg = this.getResources().getString(R.string.msgconfirmationpassworddoesnotmatch);	
				}
				else
				{
					// both new/confirm match
					isValid = true;
				}
			}
		}
		
		return isValid;
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	private static ChangePasswordTask mChangePasswordTask;
	private class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pd;
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}
		
		protected Boolean doInBackground(Void... params) {
			boolean isSuccessful = false;
			
			try
			{
				isSuccessful = getMyController().ChangePassword(_contentFrag.getNewPasswordTextView().getText().toString());
			}
			catch(Throwable e){
				ErrorLogHelper.RecordException(ChangePassword.this, e);
			}

			return isSuccessful;
		}

        protected void onPostExecute(Boolean isSuccessful) {
        	dismissProgressDialog();
			UnlockScreenRotation();

			if(isSuccessful){
				showMsg(getString(R.string.msgsuccessfullychangedpassword));
				Return();
			}
			else{
				//if(this.ContainingForm is App.login)
				if(getMyController().getIsNewUserBeingLoggedIn())
				{
					// at login time, show this error message, but continue to the next screen
	                // since the 'close' button is not available at login time, it makes sense 
	                // to automatically advance to the next screen	
					showMsg(getString(R.string.msgpasswordchangefailed));
	                Return();
				}
				else {
					showMsg(getString(R.string.msgpasswordchangefailed));
				}
			}
        }
        
		// Added public methods so that dialogs and context can be re-established 
		// after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	pd = CreateFetchDialog(getString(R.string.msgsaving));
        }
        
        public void dismissProgressDialog()
        {
        	DismissProgressDialog(ChangePassword.this, this.getClass(), pd);
        }
	}
}
