package com.jjkeller.kmb.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IEditLogLocations {	
	public interface EditLogLocationsFragControllerMethods{
		public LogEntryController getMyController();
	}
	public interface EditLogLocationsFragActions{
		public void handleEditLogLocationsClick(Context ctx);
	}
}
