package com.jjkeller.kmb.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

public interface IViewTripInfo {
	public interface ViewTripInfoFragControllerMethods{
		public IAPIController getMyController();
		public void updateExemptLogStatus();
	}
	
	public interface ViewTripInfoFragActions{
		public void handleEditButtonClick(Context ctx);	
	}

}
