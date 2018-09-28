package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.LogEntryController;

public interface IExemptLogType {
	public interface ExemptLogTypeFragControllerMethods{
		public IAPIController getMyController();
		public LoginController getMyLoginController();
	}
	
	public interface ExemptLogTypeFragActions{
		public void handleOKButtonClick();
	}
}
