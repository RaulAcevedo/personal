package com.jjkeller.kmbapi.controller.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.ArrayList;

public interface IEobrService {

	void SuspendReading();
	void UpdateDriverThresholds();
	ArrayList<StatusRecord> ReadHistoricalStatusRecords();
	Context getContext();

	void setIsFinishingFirmwareUpdate(boolean isFinishingFirmwareUpdate);
	void ignoreNextDefaultDriverEvent();

	IBluetoothDrivingManager getBluetoothDrivingManager();
}
