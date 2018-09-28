package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface ISelectDutyStatus {
	 interface SelectDutyStatusFragActions {
		 void handleSubmitButtonClick();
		 void HandleDutyStatusChange(String dutyStatus);
		 void HandlePersonalConveyanceCheckBoxClick();
		 void HandleYardMoveCheckBoxClick();
		 void HandleHyrailCheckBoxClick();
		 void HandleNonRegDrivingCheckBoxClick();
		 boolean getLocalDataTask();
	}

	public interface SelectDutyStatusFragControllerMethods{
		LogEntryController getMyLogEntryController();
		boolean ShouldShowManualLocation();
		boolean isCurrentLogCreated();
		String getSuggestedInitialDutyStatus();
	}
}
