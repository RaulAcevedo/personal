package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public class ITeamDriverAddDriver {
	public interface TeamDriverAddDriverFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverAddDriverFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
		public void handleCheckNameButtonClick();
		public void handleTeamFromStartChecked();
	}

}
