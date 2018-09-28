package com.jjkeller.kmbapi.eobrengine;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;
import com.jjkeller.kmbapi.kmbeobr.HistogramData;
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.JbusDiagnosticData;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordMotionOptionEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

import java.io.InputStream;
import java.util.Date;

public interface IEobrEngine extends ITestHarnessBluetoothCommunication, IEobrEngineBluetooth {
	float MINUTE_PER_DEG = 60;
	int EOBR_MIN_YEAR = 2000; // Min year allowed in EOBR RTC
	int EOBR_MAX_YEAR = 2099; // Max year allowed in EOBR RTC
	int EOBR_MIN_YEAR_OFFSET = 0;
	int EOBR_MAX_YEAR_OFFSET = 99;

	void ClearActiveDeviceCrc();  		// This is for debugging/admin testing only

	/// <summary>
    /// Setup the active device that was selected from discovered list
    /// </summary>
    /// <param name="deviceName">name of active device</param>
    /// <param name="btAddress">bt address of active device</param>
    /// <param name="eobrGen">eobr generation of active device</param>
	/// <param name="crc">crc of active device</param>
	void SetupActiveDevice(String deviceName, String btAddress, int eobrGen, short crc);
	
	/**
	 * Get MacAddress of active eobr
	 */
	String GetActiveDeviceAddress();

	/// <summary>
    /// Open bluetooth socket for specified device and initialize the eobr
    /// </summary>
    /// <param name="deviceName">name of device open/initialize</param>
    /// <returns>S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR</returns>
	int OpenDevice(String deviceName);
	
    /// <summary>
    /// Close the bluetooth socket 
    /// </summary>
    /// <returns>S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR</returns>
	int CloseDevice();
	
	/// <summary>
	/// Check if Eobr is connected and available
	/// </summary>
	int PingEobrDevice();
	
    /// <summary>
    /// Get the serial number from the connected/activated EOBR
    /// </summary>
    /// <returns>Bundle containing return code value and serial number value</returns>	
	Bundle GetEobrSerialNumber();
	
	/// <summary>
	/// Get the current clock value from the eobr.
	/// </summary>
	/// <returns>Return code returned from EOBR - S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR
	///			and long value representing number of milliseconds since Jan. 1, 1970</returns>
	Bundle GetClockUTC();

	/// <summary>
	/// Get the current GPS timestamp value from the eobr.
	/// </summary>
	/// <returns>Return code returned from EOBR - S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR
	///			and long value representing number of milliseconds since Jan. 1, 1970</returns>
	EobrResponse<Date> GetGPSTimestamp();

	/// <summary>
	/// Set the clock in the eobr to the new clock value.
	/// </summary>
	/// <param name="newClock">New UTC clock value to set Eobr clock to</param>
	/// <returns>Return code returned from EOBR - S_SUCCESS, S_DEV_NOT_CONNECTED, S_INVALID_DATE_TIME</returns>
	int SetClockUTC(Date newClock);
	
    /// <summary>
    /// Get the company passkey value from the connected/activated EOBR
    /// </summary>
    /// <returns>Bundle containing return code value and passkey value from the EOBR</returns>	
	Bundle GetCompanyPasskey();
	
    /// <summary>
    /// Set the company passkey value in the eobr to specified value
    /// </summary>
    /// <returns>Return code from eobr - S_SUCCESS, S_DEV_NOT_CONNECTED, S_FUNC_NOT_IMPLEMENTED (for Gen II)</returns>	
	int SetCompanyPasskey(String passkey);
	
	/// <summary>
	/// Get custom parameter from the EOBR for specified parameter index.
	/// </summary>
	/// <param name="customParameterIndex">Index of custom parameter to retrieve</param>
	/// <returns>Bundle containing return code (S_SUCCESS, S_DEV_NOT_CONNECTED, S_UNKNOWN_ERROR)
	///			and custom parameter value for specified index</returns>
    Bundle GetCustomParameter(int customParameterIndex);

	/// <summary>
	/// Set custom parameter value specified in the eobr for the specified parameter index.
	/// </summary>
    /// <param name="customParameter">Custom parameter value to set</param>
	/// <param name="customParameterIndex">Index of custom parameter to set</param>
	/// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED, S_UNKNOWN_ERROR</returns>
    int SetCustomParameter(int customParameter, int customParameterIndex);
    
	/// <summary>
	/// Get eobr odometer offset from the EOBR.
	/// </summary>
	/// <returns>Bundle containing return code (S_SUCCESS, S_DEV_NOT_CONNECTED, S_UNKNOWN_ERROR)
	///			and custom parameter value for specified index</returns>
    Bundle GetEobrOdometerOffset();
    
	/// <summary>
	/// Set eobr odometer offset in the eobr.  Note:  this is only applicable for Gen II.
    /// Gen I will return S_FUNC_NOT_IMPLEMENTED
	/// </summary>
    /// <param name="offset">Offset value to set in eobr</param>
	/// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED, S_FUNC_NOT_IMPLEMENTED (GEN I)</returns>
    int SetEobrOdometerOffset(float offset);
    
    /// <summary>
    /// Get the unit id value from the connected/activated EOBR
    /// </summary>
    /// <returns>Bundle containing return code value and unit id value from the EOBR</returns>	
	Bundle GetUnitId();

    /// <summary>
    /// Set the unit id in the eobr to the specified value
    /// </summary>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>		
	int SetUnitId(String unitId);
	
	/// <summary>
	/// Get odometer calibration value(s) from the eobr
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_UNKNOWN_ERROR, S_DEV_INTERNAL_ERROR)
	///			and odometer calibration value(s) (OFFSET and MULTIPLIER for GEN I, OFFSET for GEN II)</returns>
	Bundle GetOdometerCalibration();

	/// <summary>
	/// Set odometer calibration value(s) in the eobr
	/// </summary>
	/// <returns>return code value - S_SUCCESS, S_DEV_NOT_CONNECTED, S_FUNC_NOT_IMPLEMENTED</returns>
	int SetOdometerCalibration(float offset, float multiplier);
	
	/// <summary>
	/// Retrieve data from the eobr - based on parameter value specified, this will return current
	/// data, historical data or next motion change data.
	/// </summary>
	/// <param name="statusRec">Return value - populated StatusRecord</param>
	/// <param name="queryMethod">Identifies how to retrieve data - by RECORDID or TIMESTAMP</param>
	/// <param name="recordId">RecordId value to retrieve when retrieving data by recordId</param>
	/// <param name="timeCode">Timestamp to retrieve when retrieving data by timestamp</param>
	/// <param name="motionOption">Identify if retrieving NEXTRECORD or NEXTMOTIONCHANGE</param>
	/// <param name="resetReferenceTimestampToCurrent">Flag identifying if should advance reference timestamp</param>
	/// <returns>Return code returned from EOBR - S_SUCCESS, S_DEV_NOT_CONNECTED
	///		Also returns a StatusRecord object populated with data retrieved from the eobr</returns>
	int GetEobrData(StatusRecord statusRec, StatusRecordQueryMethodEnum queryMethod, int recordId, Date timeCode, StatusRecordMotionOptionEnum motionOption, boolean resetReferenceTimestampToCurrent);
    
	//Bundle GetReferenceTimestamp();
	//int GetEobrRecordIdGivenTimestamp(EobrTimestamp timestamp);
	
	/// <summary>
	/// Get sleep mode minutes value (engine off comms timeout) from eobr
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR)
	///			and engine off comms timeout value</returns>
	Bundle GetEngineOffCommsTimeout();
	
    /// <summary>
    /// Set the sleep mode minutes (engine off comms timeout) value in the eobr to the specified value
    /// </summary>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR</returns>		
	int SetEngineOffCommsTimeout(int timeoutInMinutes);

	/// <summary>
	/// Get data collection rate from eobr
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR)
	///			and data collection rate value in number of seconds</returns>	
	Bundle ReadDataCollectionRate();

    /// <summary>
    /// Set the data collection rate in the eobr to the specified value
    /// </summary>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>		
	int ChangeDataCollectionRate(int newDataRate);
	
	/// <summary>
	/// Get the reference timestamp from the eobr - if reference timestamp is not set
	/// no reference timestamp is returned in the bundle.
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR)
	///			and reference timestamp value</returns>		
	Bundle GetReferenceTimestamp();

	/// <summary>
	/// Get the number of hours since last good distance
	/// </summary>
	/// <returns> Bundle containing return code value ()</returns>
	Bundle GetDistHours(long timecode);

	/// <summary>
	/// Get active bus type from eobr
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR)
	///			and active bus type</returns>	
	Bundle GetActiveBusType();

	Bundle GetVin();
	
    /// <summary>
    /// Set the active bus type
    /// </summary>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>		
	int ChangeActiveBusType(int newBusType);

	/// <summary>
	/// Get revision info from the eobr - includes the following version info:
	///		- main firmware version
	///		- usb firmware version
	///		- record version
	///		- boot loader version
	///		- eobr dll version
	/// </summary>
	/// <returns>Bundle containing return code value (S_SUCCESS, S_DEV_NOT_CONNECTED, S_UNKNOWN_ERROR)
	///			and a version number for each version type identified above</returns>	
	Bundle GetEOBRDllRevisions();

    /// <summary>
    /// Set debug flags in eobr
    /// </summary>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>		
	int SetDebugFlags(int debugFlags);
	
	
	
	Bundle SendConsoleCommandToDevice(String command);
	Bundle SendConsoleCommandToDeviceWithNoRetry(String command);
	

    /**
     * Starts a self test on the EOBR.
     * @return <code>true</code> if the self test was started successfully, and <code>false</code> otherwise
     */
	boolean SetSelfTest();

	/**
	 * Gets the result of the last self test.
	 * @return A Bundle containing the return code and a return value set to the result of the self test.
	 */
	Bundle GetSelfTest();
	
    /// <summary>
    /// Wipe data from flash in eobr
    /// </summary>
	/// <param name="clearFlags">For Gen I identifies which data to clear
	///			- historical data
	///			- configuration data
	///			- error logs
	///			- JBus diagnostic data
	///		Unused for Gen II</param>
    /// <returns>Return code - S_SUCCESS, S_DEV_NOT_CONNECTED</returns>			
	int ClearAllRecordData(int clearFlags);


	/**
	 * Download firmware update to the eobr
	 * @param firmwareUpdateFile Stream containing the firmware update
	 * @param firmwareUpgradeType Type of firmware update - bootloader or app
	 * @param broadcaster An optional broadcaster that can broadcast to the progress of the download
	 * @return Return code - S_SUCCESS, S_DEV_NOT_CONNECTED, S_DEV_INTERNAL_ERROR, S_UNKNOWN_ERROR
	 */
	int DownloadFirmwareUpdate(InputStream firmwareUpdateFile, Enums.FirmwareUpgradeTypeEnum firmwareUpgradeType, FirmwareUpdateBroadcaster broadcaster, FirmwareUpdate firmwareUpdateConfig);

	/**
	 * Get the threshold values set in the TAB.
	 * @param thresholdType	- 0 to get driver threshold settings
	 * 						- -1 (0xffffffff) to get default threshold settings
	 * @return A Bundle containing the return code and the threshold values:
	 * 		- rpm threshold
	 * 		- speed threshold
	 * 		- hardbrake threshold
	 * 		- drive start distance threshold
	 * 		- drive stop time threshold
	 * 		- event blanking threshold
	 * 		- driver id
	 */
    Bundle GetThresholdValues(int thresholdType);

	/**
	 * Set the threshold values set in the TAB.
	 * @param rpmThreshold - rpm threshold value
	 * @param speedThreshold - speed threshold value
	 * @param hardBrakeThreshold - hard brake threshold value
	 * @param driveStartDistance - drive start distance setting
	 * @param driveStopTime - drive stop time setting
	 * @param eventBlanking - event blanking threshold setting
	 * @param driverId - CRC value correpsonding to driver's employee code
	 * @return Bundle containing return code and calculated CRC value for driver employee code
	 */
    //Bundle SetThresholdValues(int rpmThreshold, int speedThreshold, int hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId);
    Bundle SetThresholdValues(int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, float driveStartSpeed);
	
    // 10/11/12 JHM - Gen2 methods
	int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent);
	int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent, int eventMask);
	int GetTripData(TripReport eventData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, boolean resetReferenceTimestampToCurrent);
	int GetHistogramData(HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, HistogramTypeEnum histogramType, boolean setRefTime);
	int GetJBusDiagnosticDataFromDevice(JbusDiagnosticData diagnosticData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime);
	
	Bundle GetConsoleLog(Date startDate, Date endDate);
	
	int GetEobrGeneration();	
	void ClearAllEobrData();
	boolean IsJJK(Context ctx);
	FirmwareUpgradeRequestResult RequestFirmwareUpgrade(long firmwarePatchId);
	FirmwareUpgradeStatusResult GetFirmwareUpgradeStatus();
	
	EobrResponse<DriveData> GetDriveData(DriveDataTypeEnum typeEnum, long timeCode, short timeStep, short maxUncertainty);
	int SetReferenceTimestamps(EobrReferenceTimestamps timestamps);
	boolean IsGetDriveDataSupported();
	boolean IsGetEventDataEventMaskSupported();
	int SetIsEldMandate(boolean isEldMandate);
	
	int SetDisableReadEldVin(boolean isEldReadingVin);
	Bundle GetDisableReadEldVin();

	/**
	 * Gets the current status buffer.
	 * If there is an error, the response data will be null.
	 * @return An {@link EobrResponse} with the response
     */
	EobrResponse<StatusBuffer> GetStatusBuffer();


	Bundle GetEobrHardware();

	int GetDriverEvent(EventRecord eventData, long startTimeCode, long endTimeCode, int eventMask, boolean includeEventsWithoutDriverId);
	Bundle GetDriverCount(long startTimeCode, long endTimeCode);
}
