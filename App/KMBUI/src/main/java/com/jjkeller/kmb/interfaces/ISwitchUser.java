package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ISwitchUser {
	public interface SwitchUserFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface SwitchUserFragActions{
		public void HandleOKButtonClick();
		public void HandleCancelButtonClick();
		public void handleUserSelect();
	}
}
