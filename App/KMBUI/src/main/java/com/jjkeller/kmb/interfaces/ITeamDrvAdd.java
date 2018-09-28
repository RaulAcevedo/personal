package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ITeamDrvAdd {
	public interface TeamDriverAddFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverAddFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
		public void handleDownloadButtonClick();
		public void handleTeamFromStartChecked();
	}
}
