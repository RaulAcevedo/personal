package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.DashboardController;

public interface IDashBoard {
	public interface DashBoardFragControllerMethods{
		public DashboardController getMyController();
		public boolean CheckEngine();
	}
	
	public interface DashBoardFragActions{
		public void HandleReturnCode(int rc);
		public void ChangeSpeed();
		public void printException(String ex);
		public void startTimer();
	}
}
