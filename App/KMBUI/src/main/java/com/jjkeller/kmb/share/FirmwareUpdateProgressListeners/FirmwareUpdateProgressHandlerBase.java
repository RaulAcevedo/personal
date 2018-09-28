package com.jjkeller.kmb.share.FirmwareUpdateProgressListeners;

import android.app.ProgressDialog;
import android.content.Intent;

import com.jjkeller.kmb.firmware.IFirmwareUpdateProgressListener;
import com.jjkeller.kmb.share.BaseActivity;

/**
 * Created by ief5781 on 8/18/16.
 */
public abstract class FirmwareUpdateProgressHandlerBase implements IFirmwareUpdateProgressListener
{
	protected static final int MILLISECONDS_PER_MINUTE = 60000;

	protected ProgressDialog dialog;
	private Class activityToStart;
	protected Runnable successAction;
	private Runnable failureAction;

	public FirmwareUpdateProgressHandlerBase(Class activityToStart, Runnable successAction, Runnable failureAction) {
		this.activityToStart = activityToStart;
		this.successAction = successAction;
		this.failureAction = failureAction;
	}

	public void onFirmwareUpdateStart(BaseActivity activity)
	{
		if(activityToStart != null) {
			// Make sure the user is on RODS when the update starts

			activity.startActivity(activityToStart, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}

		activity.LockScreenRotation();
		showDialog(activity);
	}

	public void onFirmwareUpdateFinished(BaseActivity activity, boolean success)
	{
		hideDialog(activity);
		activity.UnlockScreenRotation();

		if(success) {
			if(successAction != null)
				activity.runOnUiThread(successAction);
		} else {
			if(failureAction != null)
				activity.runOnUiThread(failureAction);
		}
	}

	protected abstract void showDialog(BaseActivity activity);
	
	private void hideDialog(BaseActivity activity)
	{
		activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
	}
}
