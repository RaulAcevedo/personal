package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;

public interface IRodsNewStatus {
	public interface RodsNewStatusFragActions{
		public void HandleCancelButtonClick();
		public void HandleOKButtonClick();
        public void HandlePersonalConveyanceCheckBoxClick();
		public void HandleYardMoveCheckBoxClick();
		public void HandleNonRegDrivingCheckBoxClick();
        public void HandleHyrailCheckBoxClick();
		public void HandleDutyStatusChange();
		public void loadDutyStatus(DutyStatusEnum _currentDutyStatus);
		public void handleMotionPictureProductionSelect();
		public void loadMotionPicture();
		public boolean getLocalDataTask();
	}
	
	public interface RodsNewStatusFragControllerMethods{
		public LogEntryController getMyController();
		public boolean ShouldShowManualLocation();
	}
}
