package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.UnassignedPeriodController;

public interface IUnassignedDrivingPeriods {
	public interface UnassignedDrivingPeriodsFragActions{
		public void handleClaimButtonClick();
	}
	
	public interface UnassignedDrivingPeriodsFragControllerMethods{
		public UnassignedPeriodController getMyController();
	}
}
