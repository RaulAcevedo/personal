package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public class ITeamDriverEndDriver {
	public interface TeamDriverEndDriverFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverEndDriverFragActions{
		public void handleOKButtonClick();
		public void handleTeamAtEndChecked();
		public void handleCancelButtonClick();
	}
}
