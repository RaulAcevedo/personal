package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

public class IDeviceDiscovery {
    public interface DeviceDiscoveryActions {
        public void handleDiscoverButtonClick();
        public void handleActivateButtonClick();
        public void handleReleaseButtonClick();
        public void handleCancelButtonClick();
        public void handleProvisionNewEobrClick();
        public void handleDefaultEobrClick();
    }
    public interface DeviceDiscoveryControllerMethods {
        public String getCurrentEobrIdentifier();
        public String getCurrentEobrMacAddress();
        public boolean IsEobrDeviceOnline() throws KmbApplicationException;    	
    }
}
