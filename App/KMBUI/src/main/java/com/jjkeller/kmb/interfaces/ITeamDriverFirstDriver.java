package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ITeamDriverFirstDriver {
	public interface TeamDriverFirstDriverFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverFirstDriverFragActions{
		public void handleDriverButtonClick(String activeUserId);
	}
}
