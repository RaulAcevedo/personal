package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;

public interface IEditFuelPurchaseList {
	public interface EditFuelPurchaseListFragControllerMethods{
		public FuelPurchaseController getMyController();
	}
	
	public interface EditFuelPurchaseListFragActions{
		public void handleEditButtonClick(FuelPurchase fuelPurchase);
	}
}
