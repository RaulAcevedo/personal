package com.jjkeller.kmb.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IViewLogRemarksEdit {	
	public interface SaveLogRemarksFragControllerMethods{
		public LogEntryController getMyLogEntryController();
	}
	public interface SaveLogRemarksFragActions{
		public void handleSaveLogRemarksClick(Context ctx);
	}
	
	public interface CancelLogRemarksFragControllerMethods{
		public LogEntryController getMyLogEntryController();
	}
	public interface CancelLogRemarksFragActions{
		public void handleCancelLogRemarksClick(Context ctx);
	}
	
	public interface SelectLogRemarksFragActions{
		public void handleRemarkSelect();
	}
	public interface SelectLogRemarksFragControllerMethods{
		public LogEntryController getMyLogEntryController();
	}
	
}
