package com.jjkeller.kmbapi.controller.interfaces;

import java.util.Date;

/**
 * Management of the bluetooth disconnection and reconnection between the
 * KMB app and the EOBR device while automatic driving in Mandate mode.
 */
public interface IBluetoothDrivingManager {
	void setBluetoothDisconnectedDuringDriving(Date timestamp, long drivingStopTimeMillis, boolean isInYardMoveDrivingSegment, boolean isInPersonalConveyanceDrivingSegment, boolean isInHyrailDrivingSegment, boolean isInNonRegDrivingSegment);
	void onUnidentifiedEldEventsClaimed(boolean isInYardMoveDrivingSegment, boolean isInPersonalConveyanceDrivingSegment);
	void setManualDutyStatusChange();
	void setVehicleDriving();
	boolean isThresholdTimerRunning();
	Date getPotentialDrivingStopTimestamp();
}