package com.jjkeller.kmbapi.eobrengine

import android.bluetooth.BluetoothDevice

interface IEobrEngineBluetooth {
    /// <summary>
    /// Clear the underlying array storing connected devices
    /// </summary>
    fun deleteBTAddress(): Int

    /// <summary>
    /// Return list of discovered devices that have been added
    /// to the connected devices array
    /// </summary>
    fun getDiscoveredDeviceList(): Array<EobrDeviceDescriptor>

    /// <summary>
    /// From the list of bluetooth devices, search for EOBRs that match the
    /// companyPasskey and the serialNumber if provided
    /// </summary>
    /// <param name="devices">list of bluetooth devices that have been discovered</param>
    /// <param name="companyPasskey">companyPasskey value to find matches for</param>
    /// <param name="serialNumber">if provided, serial number to find match for</param>
    /// <returns>Bundle containing return code value and list of matching eobrdevice names</returns>
    fun searchForEobrDevices(devices: List<BluetoothDevice>, companyPasskey: String, serialNumber: String): Int

    /// <summary>
    /// Validate the specific bluetooth device passed in is a valid EOBR.  This device
    /// was created from the macAddress.  Validate this is a valid EOBR by reading
    /// the serial number and validate the company passkey (Gen I)
    /// </summary>
    /// <param name="companyPasskey">Company passkey to check the eobr against</param>
    /// <param name="btDevice">Bluetooth Device to validate is an eobr</param>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>
    fun searchForEobrDevice(companyPasskey: String, btDevice: BluetoothDevice): Int

    /// <summary>
    /// Initialize list of discovered eobr devices
    /// </summary>
    fun initializeConnectedDevices()
}
