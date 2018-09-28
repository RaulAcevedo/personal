package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.eobrengine.Enums;
import com.jjkeller.kmbapi.eobrengine.IEobrEngine;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.io.InputStream;
import java.util.List;

public interface IEobrReader {
    IEobrEngine getEobrEngine();

    EobrReader.ConnectionState getCurrentConnectionState();

    /// <summary>
    /// This is the EOBR identifier (tractor number).
    /// This is loaded during Initialization
    /// </summary>
    String getEobrIdentifier();

    /// <summary>
    /// This is the EOBR serial number.
    /// This is loaded during Initialization
    /// </summary>
    String getEobrSerialNumber();

    boolean getOnStatusChangeEnabled();

    void setOnStatusChangeEnabled(boolean val);

    /// <summary>
    /// Return Generation of Device
    /// Set in EobrConfiguration to send to Encompass
    /// </summary>
    int getEobrGeneration();

    void TransitionDeviceToNewState(Context ctx, EobrReader.ConnectionState newState,
                                    String message);

    /// <summary>
    /// Read the odometer calibration values
    /// </summary>
    Bundle Technician_ReadOdometerCalibrationValues();

    /// <summary>
    /// Sets the odometer calibration values
    /// </summary>
    /// <param name="collectionRateSeconds"></param>
    /// <returns></returns>
    int Technician_SetOdometerCalibration(float offset, float multiplier);

    int Technician_SetUniqueIdentifier(String uniqueId);

    DatabusTypeEnum Technician_GetBusType(Context ctx);

    int Technician_SetBusType(int busType);

    Bundle Technician_GetEOBRRevisions();

    int Technician_DownloadFirmwareUpdate(InputStream firmwareUpdateFile,
                                          Enums.FirmwareUpgradeTypeEnum firmwareUpgradeType,
                                          FirmwareUpdate firmwareUpdateConfig);

    boolean Technician_GetEobrHardware(Context ctx);

    FirmwareUpgradeRequestResult Technician_RequestFirmwareUpdate(long firmwarePatchId);

    FirmwareUpgradeStatusResult Technician_GetFirmwareUpdateStatus();

    void Technician_ShutdownEobrDevice(Context ctx) throws KmbApplicationException;

    String EobrMacAddress();

    /// <summary>
    /// Publish the EOBR history to the registered delegates.
    /// </summary>
    /// <param name="historyList"></param>
    void PublishEobrHistoricalRecords(List<StatusRecord> historyList);

    void ResumeReading();

    /// <summary>
    /// Perform a device discovery and look for a specific EOBR on the
    /// last bus (usb/bt) that was used.   If the discovery is not successful in
    /// the duration, then stop the discovery.
    /// This will continuously perform a discovery for the specific EOBR until
    /// either the device is found, or the duration has elapsed.
    /// Answer if the specific EOBR is discovered within the duration.
    /// </summary>
    /// <param name="eobrId">Specific EOBR device to look for</param>
    /// <param name="duration">Amount of time to look, so that it doesn't try forever</param>
    /// <returns>true if succesfully discovered, false otherwise</returns>
    boolean WaitForEobrDiscovery(Context ctx, String eobrMacAddress, int durationMinutes);
}
