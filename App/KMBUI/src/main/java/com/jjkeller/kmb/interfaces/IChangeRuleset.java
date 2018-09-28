package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

public interface IChangeRuleset {
	public interface ChangeRulesetFragControllerMethods{
		public IAPIController getMyController();
	}
	
	public interface ChangeRulesetFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
	}
}
