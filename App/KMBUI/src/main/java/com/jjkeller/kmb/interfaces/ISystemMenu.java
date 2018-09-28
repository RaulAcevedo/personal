package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface ISystemMenu {
	public interface SystemMenuFragControllerMethods{
		public LogEntryController getMyController();
		public boolean runMenuAction(int menuItemId, boolean isVehicleInMotion);
	}
}
