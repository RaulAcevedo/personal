package com.jjkeller.kmb.share;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.Updater;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AppUpdateController;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.kmbeobr.IApplicationUpdateListener;
import com.jjkeller.kmbapi.kmbeobr.IApplicationUpdateListenerFactory;
import com.jjkeller.kmbui.R;

import java.util.concurrent.Semaphore;


public class ApplicationUpdateListenerFactory implements IApplicationUpdateListenerFactory {
	BaseActivity activity;
	
	private CheckForUpdatesTask _checkForUpdatesTask;
	boolean _isUpdateRequired;
	
	public ApplicationUpdateListenerFactory(BaseActivity activity)
	{
		this.activity = activity;
	}
	
	public IApplicationUpdateListener getUpdateListener()
	{			
		EobrReader eobrReader = EobrReader.getInstance();
		final boolean isJJK = eobrReader.Technician_GetEobrHardware(activity);
		final int generation = eobrReader.getEobrGeneration();
		
		final Semaphore semaphore = new Semaphore(0);
		
		class FactoryRunnable implements Runnable
		{
			public IApplicationUpdateListener listener = null;
			
			public void run()
			{
				if(generation == 2 && !isJJK)
					listener = new ApplicationUpdateHandler();
				
				semaphore.release();
			}
		};
		
		FactoryRunnable runnable = new FactoryRunnable();
		activity.runOnUiThread(runnable);
			
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {}
	
		return runnable.listener;
	}
	
	
	private class ApplicationUpdateHandler extends ApplicationUpdateMessageHandler
	{
		public void onApplicationUpdateRequired() {
			
			// Determine if there is an app update available
			_checkForUpdatesTask = new CheckForUpdatesTask();
			_checkForUpdatesTask.execute();
		}
	}
	
	private abstract class ApplicationUpdateMessageHandler implements IApplicationUpdateListener
	{
		protected AlertDialog updateDialog;
		protected Handler updateHandler = new Handler();
		protected Runnable updateRunnable;
		
		protected void setupDialog()
		{
			
			if (updateRunnable == null)
			{
				updateRunnable = new Runnable()
				{
					public void run()
					{
						EobrReader eobrReader = EobrReader.getInstance();
						eobrReader.Shutdown();
						
						// Make sure the user is on RODS when the dialog appears
						activity.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.LockScreenRotation();
						showApplicationUpdateDialog();
					}
				};
			}
			
			updateHandler.post(updateRunnable);
		}
		
		protected void showApplicationUpdateDialog()
		{
			if (updateDialog == null || !updateDialog.isShowing())
			{
				activity.runOnUiThread(new Runnable()
				{
					public void run()
					{
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
						dialogBuilder.setTitle(R.string.app_update_bte_title);
						dialogBuilder.setMessage(R.string.app_update_bte_message);
						dialogBuilder.setIcon(null);
						dialogBuilder.setPositiveButton(R.string.btndownload, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								// Navigate to Check for Update activity
								activity.startActivity(Updater.class,
										Intent.FLAG_ACTIVITY_SINGLE_TOP);
							}
						});
						dialogBuilder.setNegativeButton(R.string.btncancel, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								// The user has already been disconnected from the ELD
								// Pressing no will dismiss the dialog
							}
						});
						updateDialog = dialogBuilder.show();
					}
				});
			}
		}

	}
	
	private class CheckForUpdatesTask extends AsyncTask<Void, Void, Void>
	{
		AppUpdateController updateController;		

		@Override
		protected void onPreExecute()
		{
			updateController = new AppUpdateController(GlobalState.getInstance());
			_isUpdateRequired = false;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {			
			try {
				_isUpdateRequired = updateController.getAppUpdateCheck().isAppUpdateAvailable(false);
			} catch (KmbApplicationException e) {
				Log.e("ApplicationUpdate", e.getMessage() + ": " + Log.getStackTraceString(e));
			}								
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			ApplicationUpdateHandler updateHandler = new ApplicationUpdateHandler();
			
			// if no app update is available, continue reading history
			if (_isUpdateRequired)
				updateHandler.setupDialog();
			else
				EobrReader.getInstance().ResumeReading();
			
		}

	}

}
