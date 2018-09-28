package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.TeamDriverController;

public interface ITeamDriverNextStep {
	public interface TeamDriverNextStepFragControllerMethods{
		public TeamDriverController getMyController();
	}
	
	public interface TeamDriverNextStepFragActions{
		public void handleLoginButtonClick();
		public void handleDashboardButtonClick();
	}
}
