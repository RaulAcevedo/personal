package com.jjkeller.kmb.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.VehicleInspectionController;

public interface IVehicleInspectionReview {
	public interface VehicleInspectionReviewFragControllerMethods{
		public VehicleInspectionController getMyController();
	}
	
	public interface VehicleInspectionReviewFragActions{
		public void DownloadAction();
		public int getCurrentSelectedInspection();
		public void handleViewPostButtonClick(Context ctx);	
		public void handleViewPreButtonClick(Context ctx);
		public void showSelectedInspection(int inspectionType);
	}
}
