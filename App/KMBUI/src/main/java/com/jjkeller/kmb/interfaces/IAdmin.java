package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.AdminController;

public interface IAdmin {
	interface AdminFragControllerMethods{
		AdminController getMyController();
	}
	
	interface AdminFragActions{
		void handleResetHistory();
		void handleDashboard();
		void handleResetDataUsage();
		void handleDeleteDB();
		void handleClearHomeSetting();
		void handleResetEobrButtonClick();
		void handlePowerCycleReset();
		void handleDisableReadEldVin(boolean isChecked, boolean showMessage);
		void handleAdminMalfunctionAndDataDiagnosticClick();
		void handleAdminDataTransferStatusClick();
		void handleAdminStorageFillerClick();
		void handleAdminClearActiveDeviceCrcClick();
		void handleAdminSaveDuplicateDutyStatusClick();
		void handleForceGeotabInvalidGpsLatch(boolean isChecked);
		void handleForceGeotabOdoError(boolean isChecked);
		void handleForceGeotabVssError(boolean isChecked);
		void handleForceInvalidGPSDate(boolean isChecked);
	}
}
