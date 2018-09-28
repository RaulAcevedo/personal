package com.jjkeller.kmbapi.controller.EOBR;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FirmwareUpdateService extends IntentService {
    private static final String ACTION_UPDATE_FIRMWARE = "com.jjkeller.kmbapi.controller.EOBR.action.UPDATEFIRMWARE";

    private static IFirmwareUpgrader firmwareUpgrader;

    public FirmwareUpdateService() {
        super("FirmwareUpdateService");
    }


    private static String DOWNLOAD_CONFIRMED = "downloadConfirmed";
    /**
     * Starts this service to perform a firmware upgrade with the given parameters, but
     * only if one isn't running already.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startFirmwareUpgrade(Context context, IFirmwareUpgrader upgrader, boolean downgradeConfirmed) {
       if(firmwareUpgrader == null) {
            firmwareUpgrader = upgrader;

            Intent intent = new Intent(context, FirmwareUpdateService.class);
            intent.putExtra(DOWNLOAD_CONFIRMED, downgradeConfirmed);
            intent.setAction(ACTION_UPDATE_FIRMWARE);
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_FIRMWARE.equals(action)) {
                boolean confirmedDowngrade = intent.getBooleanExtra(DOWNLOAD_CONFIRMED, false);handleFirmwareUpgradeAction(confirmedDowngrade);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleFirmwareUpgradeAction(boolean downgradeConfirmed) {
        try {

            firmwareUpgrader.initiateFirmwareUpgrade(downgradeConfirmed);

        }catch(Throwable ex) {
            ErrorLogHelper.RecordException(this, ex);
            if(ex.getCause() != null)
            {
                ErrorLogHelper.RecordMessage(this, "Cause of unhandled error");
                ErrorLogHelper.RecordException(this, ex.getCause());
            }
            ex.printStackTrace();

            firmwareUpgrader.signalUpdateFailed();

            EobrReader.getInstance().TransitionDeviceToNewState(this, EobrReader.ConnectionState.DEVICEFAILURE, ex.getMessage());
        } finally {
            firmwareUpgrader = null;
        }
    }
}
