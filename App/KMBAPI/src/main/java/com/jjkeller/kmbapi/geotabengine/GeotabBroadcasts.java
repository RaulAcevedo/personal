package com.jjkeller.kmbapi.geotabengine;

import android.hardware.usb.UsbManager;

/**
 * Created by ief5781 on 9/23/16.
 */

public class GeotabBroadcasts {
    public static final String ON_DEVICE_DETACHED = UsbManager.ACTION_USB_ACCESSORY_DETACHED;
    public static final String ON_PERMISSION_DENIED = "com.jjkeller.kmbapi.geotabengine.permissiondenied";
    public static final String ON_PERMISSION_GRANTED = "com.jjkeller.kmbapi.geotabengine.permissiongranted";
    public static final String ON_PERMISSION_TIMEOUT = "com.jjkeller.kmbapi.geotabengine.permissiontimeout";
}
