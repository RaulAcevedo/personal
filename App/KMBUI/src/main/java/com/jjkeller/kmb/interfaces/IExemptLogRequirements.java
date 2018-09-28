package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LoginController;

public interface IExemptLogRequirements {
	public interface ExemptLogRequirementsFragControllerMethods{
		public LoginController getMyController();
	}
	
	public interface ExemptLogRequirementsFragActions{
		public void handleYesButtonClick();
		public void handleNoButtonClick();
	}
}
