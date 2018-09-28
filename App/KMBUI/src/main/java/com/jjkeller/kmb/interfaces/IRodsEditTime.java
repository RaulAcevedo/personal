package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IRodsEditTime {
	public interface RodsEditTimeFragActions{
		public void HandleOKButtonClick();
		public void HandleCancelButtonClick();
	}
	
	public interface RodsEditTimeFragControllerMethods{
		public LogEntryController getMyController();

	}
}
