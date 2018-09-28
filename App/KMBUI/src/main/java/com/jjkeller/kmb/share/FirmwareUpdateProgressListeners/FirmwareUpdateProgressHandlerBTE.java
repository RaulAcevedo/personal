package com.jjkeller.kmb.share.FirmwareUpdateProgressListeners;

import android.app.ProgressDialog;
import android.os.Handler;
import android.view.View;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.CustomProgressDialog;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpgraderBTE;
import com.jjkeller.kmbui.R;

/**
 * Created by ief5781 on 8/18/16.
 */
public class FirmwareUpdateProgressHandlerBTE extends CancellableFirmwareUpdateProgressHandler {
	private int timeRemaining;
    private Handler updateHandler = new Handler();
    private boolean firstMessage = true;
    private String firmwareUpdateProgressDialogDeviceMessage;

    //TODO remove this.. this will cause a leak.  I left it in because BTE is being discontinued. This files has a few TODOs
    //it appears if we reopen the BTE partnership we will need to adjust this at that time.
    private BaseActivity baseActivity;

	private Runnable updateRunnable = new Runnable() {
        public void run() {
            if (timeRemaining > 0)
                timeRemaining--;

			updateProgress(baseActivity, timeRemaining);

			startUpdateTimer();
        }
    };

    public FirmwareUpdateProgressHandlerBTE(BaseActivity launchingActivity, Class activityToStart, Runnable successAction, Runnable failureAction) {
        super(activityToStart, successAction, failureAction);
        baseActivity = launchingActivity;
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            firmwareUpdateProgressDialogDeviceMessage = baseActivity.getString(R.string.firmware_update_progress_dialog_message_eld);
        } else {
            firmwareUpdateProgressDialogDeviceMessage = baseActivity.getString(R.string.firmware_update_progress_dialog_message_eobr);
        }
    }

    @Override
    protected int getCancelDelayMs() {
        return 30 * MILLISECONDS_PER_MINUTE;
    }

	public void onDownloadFirmwareProgress(BaseActivity activity, final int progress) {
        updateProgress(activity, progress);
    }

    public void onFirmwareUpdateFinished(BaseActivity activity, boolean success) {
        stopUpdateTimer();

		super.onFirmwareUpdateFinished(activity, success);
    }

    @Override
    public void shouldDowngradeFirmware(BaseActivity activity, String tractorNumber) { throw new UnsupportedOperationException(); }

    @Override
    protected void onCancel() {
        GlobalState.getInstance().setAbortFirmwareUpgrade(true);
    }

	@Override
    protected void showDialog(final BaseActivity activity) {
        if (dialog == null || !dialog.isShowing()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    String message = firmwareUpdateProgressDialogDeviceMessage + activity.getString(R.string.firmware_update_start);
					dialog = new CustomProgressDialog(activity, View.GONE, View.GONE);
                    dialog.setTitle(R.string.firmware_update_progress_dialog_title);
                    dialog.setMessage(message);
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setIndeterminate(true);
                    dialog.setIcon(null);
                    dialog.setCancelable(false);

					dialog.show();
                }
            });
        }
    }

    private void updateProgress(BaseActivity activity, final int progress) {
        String message = null;

		switch (progress) {
            case FirmwareUpgraderBTE.PROGRESS_WAITING:
                resetUpdateTimer();
                startCancelTimer(activity);
                //message = getTimeRemainingMessage(timeRemaining);
                message = getWaitMessage(activity);
                break;
// These cases aren't valid until we can do the firmware upgrade the right way...
// See comments in FirmwareUpgraderBTE.java
//				case FirmwareUpgraderBTE.PROGRESS_INCOMPLETE:
//					resetUpdateTimer();
//					message = getString(R.string.firmware_update_incomplete);
//					break;
//				case FirmwareUpgraderBTE.PROGRESS_CHECKING:
//					message = getString(R.string.firmware_update_check);
//					break;
            case FirmwareUpgraderBTE.PROGRESS_DOWNLOAD_FINISHED:
                message = firmwareUpdateProgressDialogDeviceMessage + activity.getString(R.string.firmware_update_wrapup);
                stopUpdateTimer();
                break;
            default:
                //message = getTimeRemainingMessage(progress);
                message = getWaitMessage(activity);

				break;
        }

		final String dialogMessage = message;

		if (dialog != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dialog.setMessage(dialogMessage);
                }
            });
        }
    }

	private String getWaitMessage(BaseActivity activity) {
        String message = firmwareUpdateProgressDialogDeviceMessage;

		int resId = firstMessage ? R.string.firmware_update_wait1 : R.string.firmware_update_wait2;
        firstMessage = !firstMessage;

		message += activity.getString(resId);

		return message;
    }

//		private String getTimeRemainingMessage(int progress)
//		{
//			int minutes = (int)progress / 60;
//			int seconds = progress % 60;
//
//			return String.format(activity.getString(R.string.firmware_update_timer, minutes, seconds));
//		}

	private void resetUpdateTimer() {
        timeRemaining = FirmwareUpgraderBTE.POLLING_INTERVAL;
        startUpdateTimer();
    }

	private void startUpdateTimer() {
        stopUpdateTimer();

		//TODO: Once BTE firmware upgrade is fixed, change this back to 1 second
        updateHandler.postDelayed(updateRunnable, 10000);
    }

	private void stopUpdateTimer() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}
