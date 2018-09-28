package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.AdminController;

public interface IDailyPassword {
	public interface DailyPasswordFragActions{
		public void handleOkButton();
		public void handleCancelButton();
	}
	
	public interface DailyPasswordFragControllerMethods{
		public AdminController getMyController();
	}
}
