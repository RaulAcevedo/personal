package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.FuelPurchaseController;

public interface IFuelPurchaseEdit {
	public interface FuelPurchaseEditFragControllerMethods{
		public FuelPurchaseController getMyController();		
	}
	
	public interface FuelPurchaseEditFragActions{
		public void handleOKButtonClick();
		public void handleDoneButtonClick();
	}

}
