package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.EmployeeRuleController;

public interface IEmployeeRules {
	public interface EmployeeRulesFragActions{
		public void handleDownloadButtonClick();
		public void handleChangeRulesetButtonClick();
	}
	
	public interface EmployeeRulesFragControllerMethods{
		public EmployeeRuleController getMyController();
	}
}
