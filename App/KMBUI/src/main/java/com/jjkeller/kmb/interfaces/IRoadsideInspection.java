package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IRoadsideInspection {
	public interface RoadsideInspectionFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
	}
	
	public interface RoadsideInspectionFragControllerMethods {
		public LogEntryController getMyController();
	}

}
