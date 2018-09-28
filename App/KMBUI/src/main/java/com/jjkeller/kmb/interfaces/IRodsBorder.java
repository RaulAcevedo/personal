package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IRodsBorder {
	public interface RodsBorderFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
	}
	
	public interface RodsBorderFragControllerMethods{
		public LogEntryController getMyController();
	}
}
