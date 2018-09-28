package com.jjkeller.kmbapi.controller.EOBR;

import android.util.Log;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;

/**
 * Created by ief5781 on 9/9/16.
 */
public class FirmwareUpgradeBlocker implements IFirmwareUpgrader {
    @Override
    public FirmwareUpdate getFirmwareUpdateConfig() {
        return null;
    }

    @Override
    public boolean getIsFirmwareUpgradeRequired() {
        return false;
    }

    @Override
    public boolean getIsApplicationUpgradeRequired() {
        return false;
    }

    @Override
    public void initiateFirmwareUpgrade(boolean confirmedDownload) {

    }

    @Override
    public void signalUpdateFailed() {

    }

}
