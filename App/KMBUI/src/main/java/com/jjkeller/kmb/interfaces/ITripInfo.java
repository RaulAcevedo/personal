package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

public interface ITripInfo {
	public interface TripInfoFragControllerMethods{
		public IAPIController getMyController();
	}
	
	public interface TripInfoFragActions{
		public void handleOKButtonClick();	
		public void handleCancelButtonClick();
		public void handleSetShipmentInformationClick(); 
		public void handleSetTrailerInformationClick();
		public void handleMotionPictureProductionSelect();
		public void loadMotionPicture();
	}

}
