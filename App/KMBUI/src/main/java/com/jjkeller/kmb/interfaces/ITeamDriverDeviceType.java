package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ITeamDriverDeviceType {
	public interface TeamDriverDeviceTypeFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverDeviceTypeFragActions{
		public void handleSharedDeviceButtonClick();
		public void handleSeparateDeviceButtonClick();
	}
}
