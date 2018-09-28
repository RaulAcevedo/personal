package com.jjkeller.kmbapi.geotabengine;

/**
 * Created by jld5296 on 9/26/16.
 */

public final class GeotabConstants {
    public static final String ACTION_USB_PERMISSION_RESULT = "CommunicationManager.ActionUSBPermissionResult";
    public static final String ACTION_HANDSHAKE_ESTABLISHED = "CommunicationManager.ActionHandshakeEstablished";
    public static final String ACTION_CONNECTION_CLOSED = "CommunicationManager.ActionConnectionClosed";
    public static final String ACTION_GEOTAB_DETACHED = "CommunicationManager.ActionDeviceDetached";
    public static final String EXTRA_PERMISSION_GRANTED = "CommunicationManager.ExtraPermissionGranted";

    public static final String MANUFACTURER = "Geotab";
    public static final String MODEL = "IOX USB";
    public static final String ATTEMPTING_TO_RECONNECT_TO_GEOTAB = "com.jjkeller.kmbapi.GeotabEngine.Attemping_Reconnection";
    public static final String GEOTAB_RECONNECT_SUCCESSFUL = "com.jjkeller.kmbapi.GeotabEngine.Reconnection_Success";
    public static final String GEOTAB_RECONNECT_FAILED = "com.jjkeller.kmbapi.GeotabEngine.Reconnection_Failure";

    public static final int MOST_RECENT_RECORD_ID = 0xffffffff;
}
