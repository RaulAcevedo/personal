package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.FirmwareVersion;

import java.io.IOException;

public interface IRESTWebServiceHelper {
    FirmwareVersion CheckForFirmwareUpdate(String eobrSerialNumber, int majorVersion,
                                           int minorVersion) throws IOException;
}
