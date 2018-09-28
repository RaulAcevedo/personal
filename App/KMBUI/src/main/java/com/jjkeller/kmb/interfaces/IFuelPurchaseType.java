package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.FuelPurchaseController;

public interface IFuelPurchaseType {
	public interface FuelPurchaseTypeFragControllerMethods{
		public FuelPurchaseController getMyController();
		
	}
	
	public interface FuelPurchaseTypeFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
		public void setTimeDialogButton();
		public void setDateDialogButton();
	}
}
