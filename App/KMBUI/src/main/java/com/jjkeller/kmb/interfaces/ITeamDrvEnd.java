package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ITeamDrvEnd {
	public interface TeamDriverEndFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverEndFragActions{
		public void handleOKButtonClick();
		public void handleTeamAtEndChecked();
		public void handleCancelButtonClick();
	}
}
