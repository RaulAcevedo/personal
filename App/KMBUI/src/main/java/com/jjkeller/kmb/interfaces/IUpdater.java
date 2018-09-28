package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.AppUpdateController;

public interface IUpdater {
	public interface UpdaterFragControllerMethods{
		public AppUpdateController getMyController();
		public void ExecuteCheckForUpdatesTask();
	}
	
	public interface UpdaterFragActions{
		public void handleDownloadButtonClick();	
		public void handleDoneButtonClick();
	}

}
