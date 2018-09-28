package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.jjkeller.kmbapi.controller.AppUpdateEventArgs;
import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheck;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateEvent;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.wifi.WifiStatus;
import com.jjkeller.kmbui.R;

public class Updater extends BaseActivity 
				implements UpdaterFragActions, UpdaterFragControllerMethods {

	UpdaterFrag _contentFrag;
	
	private CheckForUpdatesTask _checkForUpdatesTask;
	private IAppUpdateEvent _onAppUpdateEvent;

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

		if(mDownloadUpdateTask != null) mDownloadUpdateTask.cancel(true);
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState) {
		super.loadControls();	
		loadContentFragment(new UpdaterFrag());
		
		OnAppUpdateEvent onAppUpdateEvent = new OnAppUpdateEvent();
		this.setOnAppUpdateEvent(onAppUpdateEvent);
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

		if (DeviceInfo.IsComplianceTablet() && !WifiStatus.isConnected(this) && getMyController().isWifiRequired()) {
			showUpdateRequiresWifiPrompt();
			_contentFrag.getDownloadButton().setEnabled(true);
		} else if (DeviceInfo.IsAppSideloaded(this.getPackageManager()) || DeviceInfo.IsComplianceTablet()) {
			mDownloadUpdateTask = new DownloadUpdateTask();
			mDownloadUpdateTask.execute();
		}
	}
	
	public void handleDoneButtonClick() {
        boolean okToClose = true;
        if (this.getMyController().getAppUpdate().isDownloadInProgress())
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

	private void showUpdateRequiresWifiPrompt()
	{
		ShowConfirmationMessage(this, R.string.update_requires_wifi_title,
				getString(R.string.update_requires_wifi_message),
				R.string.wifi_settings,
				new ShowMessageClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						super.onClick(dialog, id);
						startActivity(WifiSettings.class);
					}
				},
				R.string.cancellabel,
				new ShowMessageClickListener());
	}
	
	protected void ShowCloseApplicationDialog()
	{
		AlertDialog alert = null;
		AlertDialog.Builder builder = null;
		builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_close_application);
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.oklabel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				Install();
			}
		});
		alert = builder.create();
		alert.show();
	}
	
	protected void Install()
	{
		try
		{
			this.getMyController().getAppUpdate().performInstall();
		}
		catch (KmbApplicationException e)
		{
			HandleException(e);
			_contentFrag.getDoneButton().setEnabled(true);
		}
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
	
	private DownloadUpdateTask mDownloadUpdateTask;
	private class DownloadUpdateTask extends AsyncTask<Void, Void, Void>
	{
		ProgressDialog pd;
		Exception ex;


		@Override
		protected void onPreExecute()
		{
			//LockScreenRotation();
			if(!Updater.this.isFinishing())
				pd = ProgressDialog.show(Updater.this, "", getString(R.string.msg_update_downloading));
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try
			{
				publishProgress();
				getMyController().getAppUpdate().downloadUpdates(_onAppUpdateEvent);
			}
			catch (KmbApplicationException kae)
			{
				this.ex = kae;
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			_contentFrag.getMessageLabel().setText(getString(R.string.msg_update_downloading));
		}

		@Override
		protected void onCancelled() {
			Updater.this.getMyController().getAppUpdate().cancelDownload();
		}

		@Override
		protected void onPostExecute(Void result) {
			if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		if (ex.getClass() == KmbApplicationException.class)
        			HandleException((KmbApplicationException)ex);
        		
				_contentFrag.getMessageLabel().setText(getString(R.string.msg_update_download_errors));
			}
        	
			_contentFrag.getDoneButton().setEnabled(true);
		}
	}

	protected void DownloadCompleted(AppUpdateEventArgs e) {
		if (mDownloadUpdateTask.isCancelled()) {
			Updater.this.getMyController().getAppUpdate().cancelDownload();
		}
		else if (e.getHasDownloadCompleted()) {
			_contentFrag.getDownloadButton().setEnabled(false);

			if (e.getWasDownloadSuccessful())
			{
				_contentFrag.getMessageLabel().setText(getString(R.string.msg_update_download_success));
				ShowCloseApplicationDialog();
			}
			else
			{
				_contentFrag.getMessageLabel().setText(getString(R.string.msg_update_download_errors));
				_contentFrag.getDoneButton().setEnabled(true);
			}
		}
	}

	private void setOnAppUpdateEvent(OnAppUpdateEvent onAppUpdateEvent) {
		this._onAppUpdateEvent = onAppUpdateEvent;
	}

	public class OnAppUpdateEvent implements IAppUpdateEvent{
		
		public void onAppUpdateDownload(final AppUpdateEventArgs e) {
            runOnUiThread( new Runnable() {
				public void run() {
					Updater.this.DownloadCompleted(e);
				}
			});
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
