package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.EmployeeRulesFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IEmployeeRules.EmployeeRulesFragActions;
import com.jjkeller.kmb.interfaces.IEmployeeRules.EmployeeRulesFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.MobileDeviceHandler;
import com.jjkeller.kmbapi.controller.EmployeeRuleController;
import com.jjkeller.kmbapi.controller.LogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbui.R;

public class EmployeeRules extends BaseActivity 
							implements EmployeeRulesFragActions, EmployeeRulesFragControllerMethods,
							LeftNavFrag.ActivityMenuItemsListener, LeftNavFrag.OnNavItemSelectedListener{
	EmployeeRulesFrag _contentFrag;
	private DownloadRulesTask _downloadRulesTask;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new EmployeeRulesFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (EmployeeRulesFrag)f;
	}
	
	@Override
	protected void InitController() {
		EmployeeRuleController ctrl = new EmployeeRuleController(this);

		this.setController(ctrl);
	}
	
	public EmployeeRuleController getMyController()
	{
		return (EmployeeRuleController) this.getController();
	}

	public void handleDownloadButtonClick()
	{
		_contentFrag.getDownloadButton().setEnabled(false);
		
		// Download the rules
		_downloadRulesTask = new DownloadRulesTask();
		_downloadRulesTask.execute();
		
		// Also upload the mobile device info, which will update the CoPilot activation state 
		new MobileDeviceHandler(this).UploadMobileDeviceInfo();
	}

	public void handleChangeRulesetButtonClick()
	{
		this.startActivity(ChangeRuleset.class);
	}

	@Override
    protected void Return()
    {
		this.finish();
		
		/* Display rodsentry activity */
        this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.clear();
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.btndone);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int item)
	{
		handleMenuItemSelected(item);
	}
	
	private boolean PerformDownloadRules(User currentUser) throws KmbApplicationException
	{
		boolean isSuccessful = true;
		
		if(this.getMyController().getIsWebServicesAvailable())
		{
			this.getMyController().DownloadEmployeeRules();
		}
		else
		{
			KmbApplicationException kae = new KmbApplicationException(this.getString(R.string.msgnetworknotavailable));
			throw kae;
		}

		return isSuccessful;
	}
	
	private boolean PerformDownloadLogCheckerComplianceDates(User currentUser) throws KmbApplicationException
	{
		boolean isSuccessful = true;
		LogCheckerComplianceDatesController ctrl = new LogCheckerComplianceDatesController(this); 
		
		
		if(ctrl.getIsWebServicesAvailable())
		{
			ctrl.DownloadLogCheckerComplianceDates();
		}
		else
		{
			KmbApplicationException kae = new KmbApplicationException(this.getString(R.string.msgnetworknotavailable));
			throw kae;
		}

		return isSuccessful;
	}

	/**
	 * @param input
	 * @param enumIndex
	 * @param textIndex
	 * @param rule
	 * @return
	 */
	protected String ConvertEnumToText(String input, int enumIndex, int textIndex) {
		String output = input;
		String [] enumList = this.getResources().getStringArray(enumIndex);
		String [] textList = this.getResources().getStringArray(textIndex);

		for(int i=0; i < enumList.length; i++)
		{
			if(enumList[i].equalsIgnoreCase(input))
			{
				output = textList[i];
				break;
			}
		}

		return output;
	}

	private void showConfigurationChangedSpecialDrivingCategoryMessage() {
		ConfigurationSpecialDrivingCategory configurationSpecialDrivingCategory = new ConfigurationSpecialDrivingCategory(this);
		String message = configurationSpecialDrivingCategory.getSpecialDrivingCategoryConfigurationMessage();

		if (message != null) this.ShowMessage(this, 0, message, null);
	}

	private class DownloadRulesTask extends AsyncTask<Void, String, Boolean>{
		ProgressDialog _pd;
		KmbApplicationException _ex = null;
		
		@Override
		protected void onPreExecute()
		{
			LockScreenRotation();
			_pd = ProgressDialog.show(EmployeeRules.this, "", getString(R.string.msgcontacting));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isDownloaded = false;

			try {
				isDownloaded = PerformDownloadRules(getMyController().getCurrentUser());
				isDownloaded = PerformDownloadLogCheckerComplianceDates(getMyController().getCurrentUser());
			} catch (KmbApplicationException e) {
				_ex = e;
			}

			if (isDownloaded) {
				// successfully submitted, so we're done
				publishProgress(getString(R.string.msgsuccessfullycompleted));
			} else if (_ex == null) {
				// some problem with submitting the logs
				publishProgress(getString(R.string.msgerrorsoccured));
			}

			return isDownloaded;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			_contentFrag.getMessageTextView().setText(values[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
        	if(_pd != null && _pd.isShowing()) _pd.dismiss();

        	if (_ex != null)
        		EmployeeRules.this.HandleException(_ex);
        	
        	else if(result)
			{
        		_contentFrag.onActivityCreated(null);
			}
        	
        	// Invalidate the menu to update the items
		    supportInvalidateOptionsMenu();
        	
        	UnlockScreenRotation();

			showConfigurationChangedSpecialDrivingCategoryMessage();
		}
	}
}
