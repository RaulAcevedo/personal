package com.jjkeller.kmb.share.FirmwareUpdateProgressListeners;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbui.R;

/**
 * Created by ief5781 on 8/18/16.
 */
public abstract class CancellableFirmwareUpdateProgressHandler extends FirmwareUpdateProgressHandlerBase
{
	protected AlertDialog cancelDialog;
	protected Handler cancelHandler = new Handler();
	protected Runnable cancelRunnable;

	protected CancellableFirmwareUpdateProgressHandler(Class activityToStart, Runnable successAction, Runnable failureAction) {
		super(activityToStart, successAction, failureAction);
	}

	protected abstract int getCancelDelayMs();

	@Override
	public void onFirmwareUpdateFinished(BaseActivity activity, boolean success)
	{
		stopCancelTimer();
		hideCancelDialog(activity);

		super.onFirmwareUpdateFinished(activity, success);
	}

	protected void onCancel()
	{

	}

	protected void startCancelTimer(final BaseActivity activity)
	{
		stopCancelTimer();
		if (cancelRunnable == null){
			cancelRunnable = new Runnable()
			{
				public void run()
				{
					showCancelDialog(activity);
				}
			};
		}
		cancelHandler.postDelayed(cancelRunnable, getCancelDelayMs());
	}

	protected void showCancelDialog(final BaseActivity activity)
	{
		if (cancelDialog == null || !cancelDialog.isShowing())
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
					dialogBuilder.setMessage(R.string.firmware_update_stop_waiting_message);
					dialogBuilder.setIcon(null);
					dialogBuilder.setPositiveButton(R.string.btnyes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							onCancel();
							onFirmwareUpdateFinished(activity, false);
						}
					});
					dialogBuilder.setNegativeButton(R.string.btnno, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							startCancelTimer(activity);
							hideCancelDialog(activity);
						}
					});
					cancelDialog = dialogBuilder.show();
				}
			});
		}
	}

	protected void hideCancelDialog(BaseActivity activity)
	{
		if (cancelDialog != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					cancelDialog.dismiss();
					cancelDialog = null;
				}
			});
		}
	}

	protected void stopCancelTimer()
	{
		if (cancelHandler != null && cancelRunnable != null)
		{
			cancelHandler.removeCallbacks(cancelRunnable);
		}
	}
}
