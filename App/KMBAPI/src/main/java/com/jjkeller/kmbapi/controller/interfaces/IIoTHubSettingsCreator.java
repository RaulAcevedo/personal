package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.share.IoTHubSettings;
import com.jjkeller.kmbapi.proxydata.MobileDevice;

import java.io.IOException;

public interface IIoTHubSettingsCreator {
    /**
     * Creates IoT Hub settings for the given device.
     * Returns null if there was a failure that didn't result from an exception.
     *
     * @param mobileDevice The mobile device to create settings for (typically the current device)
     * @return {@link IoTHubSettings} if successful and null otherwise
     * @throws IOException If there was a communication error
     */
    IoTHubSettings CreateIoTHubSettings(MobileDevice mobileDevice) throws IOException;
}
