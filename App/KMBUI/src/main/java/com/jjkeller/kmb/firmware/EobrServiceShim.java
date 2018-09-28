package com.jjkeller.kmb.firmware;

import android.content.Context;

import com.jjkeller.kmb.EobrConfig;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.interfaces.IBluetoothDrivingManager;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateMessage;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.ArrayList;

/**
 * Created by T000684 on 10/16/2017.
 */

/**
 * this is used for firmware updates if the service has not been initialized.
 * Used if we hit the EOBRConfig activity during login process.
 */
public class EobrServiceShim implements IEobrService {
    private boolean _didNotifyFinished = false;

    @Override
    public void SuspendReading() {
    }

    @Override
    public void UpdateDriverThresholds() {
        LogEntryController logController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
        logController.SetThresholdValues(GlobalState.getInstance().getCurrentDesignatedDriver(), false, false);
    }

    @Override
    public ArrayList<StatusRecord> ReadHistoricalStatusRecords() {
        return null;
    }

    @Override
    public Context getContext() {
        return GlobalState.getInstance().getApplicationContext();
    }

    @Override
    public void setIsFinishingFirmwareUpdate(boolean isFinishingFirmwareUpdate) {
        if (!_didNotifyFinished) {
            _didNotifyFinished = true;
            FirmwareUpdateBroadcaster broadcaster = new FirmwareUpdateBroadcaster();
            broadcaster.onFirmwareUpdateFinished(isFinishingFirmwareUpdate);
        }
    }

    @Override
    public void ignoreNextDefaultDriverEvent() {
    }

    @Override
    public IBluetoothDrivingManager getBluetoothDrivingManager() {
        return null;
    }
}