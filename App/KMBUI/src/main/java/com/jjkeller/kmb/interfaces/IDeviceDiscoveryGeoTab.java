package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

public class IDeviceDiscoveryGeoTab {
    public interface GeotabDeviceDiscoveryActions {
        public void handleActivateButtonClick();
        public void handleReleaseButtonClick();
        public void handleCancelButtonClick();

    }
    public interface DeviceDiscoveryControllerMethods {
        public String getCurrentGeoTabIdentifier();
        public String getCurrentGeoTabMacAddress();
        public boolean IsGeoTabDeviceOnline() throws KmbApplicationException;
    }
}
