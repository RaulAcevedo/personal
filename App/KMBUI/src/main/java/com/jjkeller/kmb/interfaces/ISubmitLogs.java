package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

public interface ISubmitLogs {
	public interface SubmitLogsFragControllerMethods{
		public IAPIController getMyController();
	}

	public interface SubmitLogsFragActions{
		public void handleSubmitButtonClick();
		public void handleDoneButtonClick();
	}
}
