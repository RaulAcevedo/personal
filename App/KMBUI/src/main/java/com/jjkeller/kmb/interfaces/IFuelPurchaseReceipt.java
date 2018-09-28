package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.FuelPurchaseController;

public interface IFuelPurchaseReceipt {
	public interface FuelPurchaseReceiptFragControllerMethods{
		public FuelPurchaseController getMyController(); 
	}
	
	public interface FuelPurchaseReceiptFragActions{
		public void handleOKButtonClick();
		public void handleCancelButtonClick();
	}
}
