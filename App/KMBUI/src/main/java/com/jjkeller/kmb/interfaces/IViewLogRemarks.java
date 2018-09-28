package com.jjkeller.kmb.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IViewLogRemarks {	
	public interface EditLogRemarksFragControllerMethods{
		public LogEntryController getMyLogEntryController();
	}
	public interface EditLogRemarksFragActions{
		public void handleEditLogRemarksClick(Context ctx);
	}
	public interface DeleteLogRemarksFragControllerMethods{
		public LogEntryController getMyLogEntryController();
	}
	public interface DeleteLogRemarksFragActions{
		public void handleDeleteLogRemarksClick(Context ctx);
	}
}
