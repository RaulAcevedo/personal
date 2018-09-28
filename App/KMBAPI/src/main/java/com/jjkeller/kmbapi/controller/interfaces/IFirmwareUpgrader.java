package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;

public interface IFirmwareUpgrader {

	 FirmwareUpdate getFirmwareUpdateConfig();
	 boolean getIsFirmwareUpgradeRequired();
	 boolean getIsApplicationUpgradeRequired();
	 void initiateFirmwareUpgrade(boolean downgradeConfirmed);
	 void signalUpdateFailed();

}