package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.EobrConfigController;

public interface IEobrConfig
{
	public interface EobrConfigFragControllerMethods{
		public EobrConfigController getMyController();
	}
	
	public interface EobrConfigFragActions
	{
		public void handleSaveButtonClick();
		public void handleCancelButtonClick();
		
		
	}
}
