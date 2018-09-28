package com.jjkeller.kmb.interfaces;


public interface IRodsEntry {
    public interface RodsEntryFragActions {
        public void handleViewLogClick();
        public void handleVehicleInspectionClick();
        public void handleEobrConnectionClick();
        public void handleLogoffClick();
        public void handleRoadsideInspectionClick();
    }
    
    public interface RodsEntryFragControllerMethods {
		public void ShowCurrentStatus(boolean autoDisplayEditLocation);
	}
}
