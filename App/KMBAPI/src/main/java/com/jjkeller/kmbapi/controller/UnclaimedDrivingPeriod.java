package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

public class UnclaimedDrivingPeriod {

    private boolean isClaimed;
	private UnassignedDrivingPeriod unassignedDrivingPeriod;

	/// <summary>
    /// Is the driving period claimed
    /// </summary>
    public boolean getIsClaimed(){return isClaimed;}
    public void setIsClaimed(boolean value) { isClaimed = value; }

    public UnassignedDrivingPeriod getUnassignedDrivingPeriod() { return unassignedDrivingPeriod; }
    public void setUnassignedDrivingPeriod(UnassignedDrivingPeriod value) { unassignedDrivingPeriod = value; }
}
