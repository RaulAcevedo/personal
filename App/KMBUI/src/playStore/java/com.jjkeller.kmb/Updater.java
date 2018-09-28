package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.UpdaterFrag;
import com.jjkeller.kmb.interfaces.IUpdater.UpdaterFragActions;
import com.jjkeller.kmb.interfaces.IUpdater.UpdaterFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AppUpdateController;
import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbui.R;

public class Updater extends BaseActivity 
				implements UpdaterFragActions, UpdaterFragControllerMethods {

	UpdaterFrag _contentFrag;
	
	private CheckForUpdatesTask _checkForUpdatesTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mFetchLocalDataTask != null) mFetchLocalDataTask.cancel(true);
		
		if(_checkForUpdatesTask != null) _checkForUpdatesTask.cancel(true);
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState) {
		super.loadControls();	
		loadContentFragment(new UpdaterFrag());
	}

	@Override
	protected void InitController() {
		AppUpdateController ctrl = new AppUpdateController(this);
		this.setController(ctrl);
	}

	public AppUpdateController getMyController()
	{
		return (AppUpdateController)this.getController();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (UpdaterFrag)f;
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}

	@Override
	protected void Return(boolean success) {
		if (success)
		{			
			this.startActivity(Login.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.finish();
		}
		else
		{			
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.finish();
		}
	}

	public void handleDownloadButtonClick() {
		_contentFrag.getDownloadButton().setEnabled(false);

		final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
		try {
			startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://play.google.com/store/apps/details?id="+ appPackageName)));
		}
	}
	
	public void handleDoneButtonClick() {
        boolean okToClose = true;
        if (AppUpdateFactory.getInstance().areAppUpdatesEnabled() && this.getMyController().getAppUpdate().isDownloadInProgress())
        {
            okToClose = false;
        	this.ShowConfirmationMessage(this, getString(R.string.msg_download_in_progress), 
    				new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
			                Updater.this.getMyController().getAppUpdate().cancelDownload();
			                Updater.this.Return(GlobalState.getInstance().getIsAutoUpdate());
							super.onClick(dialog, id);
						}
        			}, 
    				new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
						}
        			});         	
        }

		if (okToClose)
			Return(GlobalState.getInstance().getIsAutoUpdate());
	}

	private class CheckForUpdatesTask extends AsyncTask<Void, Void, Void>
	{
		ProgressDialog pd;
		KmbApplicationException ex;
		Bundle savedInstanceState = new Bundle();

		@Override
		protected void onPreExecute()
		{
			//LockScreenRotation();
			if(!Updater.this.isFinishing())
				this.pd = ProgressDialog.show(Updater.this, "", getString(R.string.msg_checking_for_udpates));
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try
			{
				boolean isAvailable = getMyController().getAppUpdateCheck().isAppUpdateAvailable(false);
				String message = "";
				if (isAvailable)
				{
					message = String.format(getString(R.string.msg_update_available_version), getMyController().getNewUpdateVersion());
					
					if (getMyController().isFirmwareUpdateIncluded()) {
						if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
							message.concat(getString(R.string.msg_update_eld));
						} else {
							message.concat(getString(R.string.msg_update_eobr));
						}
					}
				}
				else
				{
					message = getString(R.string.msg_update_not_available);
				}
				
				savedInstanceState.putBoolean(UpdaterFrag.UPDATE_AVAILABLE, isAvailable);
				savedInstanceState.putString(UpdaterFrag.MESSAGE, message);
			}
			catch (KmbApplicationException kae)
			{
				this.ex = kae;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (pd != null && pd.isShowing())
				pd.dismiss();
			
			if (ex != null)
			{
				if (ex.getClass() == KmbApplicationException.class)
					HandleException((KmbApplicationException) ex);
			}
			else
			{
				_contentFrag.loadControlsFromBundle(savedInstanceState);
			}
		}
	}

	public void ExecuteCheckForUpdatesTask()
	{
	    if (!AppUpdateFactory.getInstance().areAppUpdateChecksEnabled())
	        return;

		_checkForUpdatesTask = new CheckForUpdatesTask();
		_checkForUpdatesTask.execute();
	}
}
