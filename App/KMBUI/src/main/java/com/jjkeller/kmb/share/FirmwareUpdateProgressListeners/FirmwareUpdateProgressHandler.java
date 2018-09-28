package com.jjkeller.kmb.share.FirmwareUpdateProgressListeners;

import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.jjkeller.kmb.EobrService;
import com.jjkeller.kmb.firmware.EobrServiceShim;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpdateService;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpgraderFactory;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbui.R;


/**
 * Created by ief5781 on 8/18/16.
 */
public class FirmwareUpdateProgressHandler extends CancellableFirmwareUpdateProgressHandler {


    public FirmwareUpdateProgressHandler(Class activityToStart, int cancelDelayMinutes, Runnable successAction, Runnable failureAction) {
        super(activityToStart, successAction, failureAction);
        _cancelDelayMinutes = cancelDelayMinutes;
    }

    private int _cancelDelayMinutes = 3;

    @Override
    protected int getCancelDelayMs() {
        return _cancelDelayMinutes * MILLISECONDS_PER_MINUTE;
    }

    public void onDownloadFirmwareProgress(BaseActivity activity, final int progress) {
        if (progress < 100) {
            // Leave at 99% because there's more stuff after downloading the firmware
            updateProgress(activity, progress);
        } else {
            // Start a timer. If it takes too long to reconnect and finish, ask the user if they want to stop it.
            startCancelTimer(activity);
        }
    }

    @Override
    public void shouldDowngradeFirmware(final BaseActivity activity, final String tractorNumber) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                BaseActivity.ShowMessageClickListener onYesHandler = activity.new ShowMessageClickListener() {
                    @Override
                    public void onClick(DialogInterface downgradeDialog, int id) {
                        downgradeDialog.dismiss();
                        super.onClick(downgradeDialog, id);
                        IEobrService eobrService = (EobrService) GlobalState.getInstance().getEobrService();
                        if (eobrService == null) {
                            eobrService = new EobrServiceShim();
                        }
                        EobrReader eobrReader = EobrReader.getInstance();
                        FirmwareUpdateService.startFirmwareUpgrade(activity, FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService), true);

                    }
                };
                BaseActivity.ShowMessageClickListener onNoHandler = activity.new ShowMessageClickListener();
                activity.ShowConfirmationMessage(activity, R.string.lblDowngradeFirmwareTitle, activity.getString(R.string.lblDowngradeFirmwareAcknowledge, tractorNumber), R.string.btnDowngradeFirmwareAgree, onYesHandler, R.string.btnDowngradeFirmwareCancel, onNoHandler);
            }
        });
    }

    @Override
    protected void showDialog(final BaseActivity activity) {


        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (dialog == null || !dialog.isShowing()) {
                    dialog = new ProgressDialog(activity);
                    dialog.setTitle(R.string.firmware_update_progress_dialog_title);
                    if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                        dialog.setMessage(activity.getString(R.string.firmware_update_progress_dialog_message_eld));
                    } else {
                        dialog.setMessage(activity.getString(R.string.firmware_update_progress_dialog_message_eobr));
                    }
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setIcon(null);
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(false);
                    dialog.setMax(100);
                    dialog.setProgress(0);
                    dialog.show();
                }
            }
        });

    }

    private void updateProgress(BaseActivity activity, final int progress) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (dialog != null) {
                    dialog.setProgress(progress);
                }
            }
        });
    }

}
