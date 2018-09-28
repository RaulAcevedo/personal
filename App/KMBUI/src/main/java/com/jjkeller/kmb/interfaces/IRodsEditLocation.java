package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IRodsEditLocation {
	public interface RodsEditLocationFragActions{
		public void handleCancelButtonClick();
		public void handleSaveButtonClick();
		public void showMessage(String message);
		public boolean getLocalDataTask();
	}
	
	public interface RodsEditLocationFragControllerMethods{
		public LogEntryController getMyController();
	}
}
