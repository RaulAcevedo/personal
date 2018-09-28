package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EobrSelfTestResult;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EobrConfigController extends ControllerBase {
	protected static final int HOURS_PER_DAY = 24;
	public static final String STARTRANGE = "startRange";
	public static final String ENDRANGE = "endRange";
	public static final String ISINVALIDRANGE = "isInValidRange";	

	/// <summary>
    /// This is the number of days until an odometer calibration "expires"
    /// and has to be performed again.
    /// </summary>
    private static final int ODOMETER_CALIBRATION_PERIOD_DAYS = 30;

    private static boolean hasEobrOdometerOffsetBeenSet = false;
    private static float EOBR_ODOMETER_OFFSET_MILES = 0.0f;
    
    /**
     * This is the number of hours that should elapse before the ELD needs to be power-cycled again
     */
    private static final int POWERCYCLE_RESET_PERIOD_HOURS = 336;  // 14 days

	private EobrReader eobrReader;

    public EobrConfigController(Context ctx) {
		super(ctx);

		eobrReader = EobrReader.getInstance();
	}

	public EobrConfigController(Context ctx, EobrReader eobrReader) {
		this(ctx);

		this.eobrReader = eobrReader;
	}

	public String GetTractorNumberFromDMO() {
		EobrConfiguration eobrConfig = null;
		String tractorNumber = null;

		if (this.getIsWebServicesAvailable() && this.IsEobrDeviceOnline()) {
			String serialNumber = eobrReader.getEobrSerialNumber();

			try {
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
				eobrConfig = rwsh.DownloadEobrConfiguration(serialNumber, changeTimestampUTC);

				if (eobrConfig != null) {
					tractorNumber = eobrConfig.getTractorNumber();
				}
			} catch (JsonSyntaxException jse) {
				this.HandleException(jse, this.getContext().getString(R.string.gettractornumberfromdmo));
			} catch (JsonParseException jpe) {
				// when connected to a network, but unable to get to webservice "e" is null at times
				if (jpe == null)
					jpe = new JsonParseException(JsonParseException.class.getName());
				this.HandleException(jpe, this.getContext().getString(R.string.gettractornumberfromdmo));
			} catch (IOException ioe) {
				this.HandleException(ioe, this.getContext().getString(R.string.gettractornumberfromdmo));
			}

		}
		return tractorNumber;
	}

	public String GetTruckVin() {

    	if (eobrReader != null) {
			// access VIN from ELD first
			if (this.IsEobrDeviceOnline() && !TextUtils.isEmpty(eobrReader.GetVin())) {
				return eobrReader.GetVin();
			} else if (!TextUtils.isEmpty(eobrReader.getEobrSerialNumber())) {
				// DownloadConfigFromDMO is called after ReadingHistory to populate EobrDevice table
				EobrConfiguration eobrConfigLocal = new EobrConfigurationFacade(this.getContext(), GlobalState.getInstance().getCurrentUser()).Fetch(eobrReader.getEobrSerialNumber());
				if (eobrConfigLocal != null && !TextUtils.isEmpty(eobrConfigLocal.getVIN())) {
					return eobrConfigLocal.getVIN();
				}
			}
		}

		return "";
	}
    
	@SuppressWarnings("unused")
	public EobrConfiguration DownloadConfigFromDMO() {

    	String serialNumber = null;

		if (this.IsEobrDeviceOnline()) {
			serialNumber = eobrReader.getEobrSerialNumber();
		}

		return DownloadConfigFromDMO(serialNumber);
	}

	public EobrConfiguration DownloadConfigFromDMO(String serialNumber)
	{
		EobrConfiguration eobrConfig = null;

		if (this.getIsWebServicesAvailable() && !TextUtils.isEmpty(serialNumber)) {

			try
			{
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
				eobrConfig = rwsh.DownloadEobrConfiguration(serialNumber, changeTimestampUTC);

				if(eobrConfig != null) {
					EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext(), this.getCurrentUser());
					EobrConfiguration eobrConfigLocal = facade.Fetch(serialNumber);
					if(eobrConfigLocal != null)
						copyExistingUnmappedProperties(eobrConfig, eobrConfigLocal);

					this.SaveConfig(eobrConfig);
				}
			}
			catch (JsonSyntaxException jse)
			{
				this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
			}
			catch (JsonParseException jpe)
			{
				// when connected to a network, but unable to get to webservice "e" is null at times
				if (jpe == null)
					jpe = new JsonParseException(JsonParseException.class.getName());
				this.HandleException(jpe, this.getContext().getString(R.string.downloadconfigfromdmo));
			}
			catch (IOException ioe)
			{
				this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
			}
		}
		
		return eobrConfig;
	}

	private void copyExistingUnmappedProperties(EobrConfiguration copyInto, EobrConfiguration copyFrom) {
		// note: These properties are not managed in DMO, so transfer from the local
		//       object to the one just downloaded.
		//       This is done so that they do not get overwritten in the local db
		copyInto.setLastPowerCycleResetDate(copyFrom.getLastPowerCycleResetDate());
		copyInto.setMajorFirmwareVersion(copyFrom.getMajorFirmwareVersion());
		copyInto.setMinorFirmwareVersion(copyFrom.getMinorFirmwareVersion());
		copyInto.setPatchFirmwareVersion(copyFrom.getPatchFirmwareVersion());
	}
	
	// Save the configuration to the local database
	private boolean SaveConfig(EobrConfiguration eobrConfig)
	{
		boolean isSuccessful = false;
		
		if (eobrConfig != null)
		{
			EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext(), this.getCurrentUser());
			facade.Save(eobrConfig.getSerialNumber(), eobrConfig);
		}
		isSuccessful = true;
		
		return isSuccessful;
	}

	@SuppressWarnings("unused")
	public boolean SubmitEobrConfigurationsToDMO()
	{
        boolean isSuccesful = false;
        if (!this.getIsNetworkAvailable()) {
            return false;
        }

        try
        {
            // first fetch all unsubmitted engine records
            EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext(), this.getCurrentUser());
            List<EobrConfiguration> unSubmittedRecords = facade.FetchAllUnsubmitted();

            // Add Geotab records that need to be submitted for clock sync
            List<EobrConfiguration> allRecords = facade.FetchAll();
            for (EobrConfiguration record : allRecords) {
                if(record.getClockSyncDateUTC() != null){
                    unSubmittedRecords.add(record);
                }
            }

            // are there any to send?
            if (unSubmittedRecords == null || unSubmittedRecords.size() <= 0) {
                return true;
            }

            // there are records to send to DMO
            for (EobrConfiguration eobrConfigLocal : unSubmittedRecords)
            {
                try
                {
                    RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                    EobrConfiguration eobrConfig = rwsh.SubmitEobrConfiguration(eobrConfigLocal);

                    copyExistingUnmappedProperties(eobrConfig, eobrConfigLocal);

                    // update the db, because the unit rules may have come back from DMO
                    facade.Save(eobrConfig.getSerialNumber(), eobrConfig);

                    // mark the one just sent, as submitted
                    facade.MarkAsSubmitted(eobrConfig);
                }
                catch (JsonSyntaxException e)
                {
                    this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
                }
                catch (JsonParseException e)
                {
                    // when connected to a network, but unable to get to webservice "e" is null
                    if(e == null)
                        e = new JsonParseException(JsonParseException.class.getName());
                    this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
                }
            }

            return true;

        }
        catch(IOException ioe)
        {
            this.HandleException(ioe, this.getContext().getString(R.string.submiteobrconfigurationstodmo));
        }
        catch (Exception excp)
        {
            this.HandleException(excp, this.getContext().getString(R.string.submiteobrconfigurationstodmo));
        }

        return isSuccesful;
	}
	
    /// <summary>
    /// Answer if the current EOBR instance is ONLINE.
    /// </summary>
    /// <returns></returns>
    public boolean IsEobrDeviceOnline()
    {
    	boolean isOnline = false;
        if (eobrReader != null)
        {
            if (eobrReader.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE)
            {
                isOnline = true;
            }
        }
        return isOnline;
    }
    
    /// <summary>
    /// Answer the TractorNumber from the currently connected EOBR
    /// </summary>
    /// <returns></returns>
    public String getCurrentTractorNumber()
    {
       	String eobrId = null;
        if (eobrReader != null)
        {
            eobrId = eobrReader.Technician_GetUniqueIdentifier(getContext());
        }
        return eobrId;
    }
    
    public String getSerialNumber()
    {
    	String eobrSerialNumber = null;
        if (eobrReader != null)
        {
        	eobrSerialNumber = eobrReader.getEobrSerialNumber();
        }
    	return eobrSerialNumber;
    }
    
    public DatabusTypeEnum getCurrentBusType()
    {
    	DatabusTypeEnum currentBusType = null;
    	if (EobrReader.getIsEobrDeviceAvailable())
    	{
    		currentBusType = eobrReader.Technician_GetBusType(getContext());
    	}
    	return currentBusType;
    }
	
    /**
     * Starts a self test on the EOBR. Returns whether or not the test was started successfully.
     */
	public boolean startSelfTest()
	{
		boolean result = false;
		try
		{
			result = eobrReader.Technician_SetSelfTest();
		}
		catch (Exception ex)
		{
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}
		return result;
	}

	/**
	 * Gets the result of the last self test
	 * @param result A test result object to fill in with the results
	 * @return The status code from the EOBR
	 */
	public int getSelfTest(EobrSelfTestResult result)
	{
		int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;

		// Set defaults
		result.setSuccessful(false);
		result.setErrorCode(-1);
		
		// Try to get the result
		try
		{
			returnCode = eobrReader.Technician_GetSelfTest(getContext(), result);
		}
		catch (Exception ex)
		{
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}
		
		return returnCode;
	}
    
	public boolean getIsEobrGenI()
	{
		boolean result = false;
		try
		{
			result = eobrReader.isEobrGen1();
		}
		catch(Exception ex)
		{
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}

		return result;
	}

	public boolean getIsGeotabDevice()
	{
		boolean result = false;
		try
		{
			result = eobrReader.getEobrGeneration() == Constants.GENERATION_GEOTAB;
		}
		catch(Exception ex)
		{
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}

		return result;
	}
	
	public boolean PerformEobrConfiguration(String eobrId, String busType) throws KmbApplicationException
	{
		boolean retVal = true;
		
		DatabusTypeEnum databusType = DatabusTypeEnum.valueOfDMOEnum(busType);
		
		switch (databusType.getValue())
		{
			case DatabusTypeEnum.GPSONLY: //gen I auto detect
				retVal = this.PerformAutoDetectEobrConfiguration(eobrId);
				break;
			case DatabusTypeEnum.UNKNOWN:	// gen II auto detect
				retVal = this.PerformAutoDetectTABConfiguration(eobrId);
				break;
			case DatabusTypeEnum.BIT11CANBUS250:
			case DatabusTypeEnum.BIT11CANBUS500:
			case DatabusTypeEnum.BIT29CANBUS250:
			case DatabusTypeEnum.BIT29CANBUS500:
			case DatabusTypeEnum.DUALMODEJ1708J1939:
			case DatabusTypeEnum.ISO91412:
			case DatabusTypeEnum.J1708:
			case DatabusTypeEnum.J1850PWM:
			case DatabusTypeEnum.J1850VPW:
			case DatabusTypeEnum.J1939:
			case DatabusTypeEnum.KWP2000:
			case DatabusTypeEnum.J1939F:
			case DatabusTypeEnum.DUALMODEJ1708J1939F:
				retVal = this.PerformEobrConfigurationForBus(eobrId, databusType);
				break;
		}
		return retVal;
	}

	public Bundle SetThresholdValues(Context ctx, int rpmThreshold, float speedThreshold, float hardBreakThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, int mandateDrivingStopMinutes, float driveStartSpeed)
	{
		boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		EobrReader eobr = eobrReader;
		if(isMandateEnabled){
			driveStopTime = mandateDrivingStopMinutes;
		}
		return eobr.Technician_SetThresholds(ctx, rpmThreshold, speedThreshold, hardBreakThreshold, driveStartDistance, driveStopTime, eventBlanking, driverId, driveStartSpeed);
	}
	
	
    /// <summary>
    /// Perform auto-detect EOBR configuration for Gen I.  The following EOBR config parms are set:
    ///    1. EOBR identifier, which is also considered the tractor number of the unit.
    ///    2. Sleep mode timeout
    ///    3. Data collection rate interval
    ///    4. Discovery Passkey
    /// The J-Bus will be auto-detected.  In order for auto-detect to work properly, 
    /// the truck engine needs to be running, or the ignition key needs to be 
    /// in the accessory position. 
    /// Answer if all EOBR parms are successfully set.
    /// </summary>
    /// <param name="eobrId">name to set the EOBR</param>
    /// <returns>true if successful, false otherwise</returns>
    private boolean PerformAutoDetectEobrConfiguration(String eobrId) throws KmbApplicationException
    {
        boolean isSuccessful = false;

        if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            int rc = -1;
            EobrReader eobr = eobrReader;

            try
            {
                eobr.SuspendReading();
                
	            isSuccessful = this.PerformGeneralEobrConfiguration(eobrId);
	            if (!isSuccessful) return false;
	
	            // test the current J-bus and determine if the data is being read properly
	            boolean isJ1708Active = false;
	            boolean isJ1939Active = false;
	
	            DatabusTypeEnum currentBusType = eobr.Technician_GetBusType(this.getContext());
	            boolean isJBusActive = eobr.TestJBusConfig();
	            // set the result of the JBus test and then switch to the other bus.
	            switch (currentBusType.getValue())
	            {
	                case DatabusTypeEnum.J1708: 
	                    isJ1708Active = isJBusActive;
	                    rc = eobr.Technician_SetBusType(2);
	                    break;
	                case DatabusTypeEnum.J1939:
	                    isJ1939Active = isJBusActive;
	                    rc = eobr.Technician_SetBusType(1);
	                    break;
	            }
	            if (rc != 0) return false;
	
	            // delay after the bus switch
	            try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					Log.e("UnhandledCatch", e1.getMessage() + ": " + Log.getStackTraceString(e1));
				}  // 5 seconds
	
	            // test the new bus
	            currentBusType = eobr.Technician_GetBusType(this.getContext());
	            isJBusActive = eobr.TestJBusConfig();
	
	            // set the result of the second JBus test
	            switch (currentBusType.getValue())
	            {
	                case DatabusTypeEnum.J1708:
	                    isJ1708Active = isJBusActive;
	                    break;
	                case DatabusTypeEnum.J1939:
	                    isJ1939Active = isJBusActive;
	                    break;
	            }
	
	            // If no active buses found...
	            if (!isJ1708Active && !isJ1939Active) return false;
	
	            // If multiple buses found active...
	            if (isJ1708Active && isJ1939Active)
	        	{
	        		KmbApplicationException kae = new KmbApplicationException();
	        		kae.setDisplayMessage(this.getContext().getString(R.string.msg_multipleenginedatabusesdetected));
	        		throw kae;
	        	}
	            
	            // Ensure that the bus with the data is active.
	            switch (currentBusType.getValue())
	            {
	                case DatabusTypeEnum.J1708:
	                    if (!isJ1708Active)
	                    {
	                        rc = eobr.Technician_SetBusType(2);
	                        // delay after the bus switch
	                        try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
								Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
							}  // 5 seconds
	                    }
	                    break;
	                case DatabusTypeEnum.J1939:
	                    if (!isJ1939Active)
	                    {
	                        rc = eobr.Technician_SetBusType(1);
	                        // delay after the bus switch
	                        try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
								Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
							}  // 5 seconds
	                    }
	                    break;
	            }
	            if (rc != 0) return false;
	
	            // attempt to submit the config to DMO
	            isSuccessful = this.ReadAndSendCurrentConfigToDMO();
            }
            finally
            {
                eobr.ResumeReading();
            }
        }

        // if it makes it here, it just be successful
        return isSuccessful;

    }

    /// <summary>
    /// Perform auto-detect TAB configuration for Gen II.  The following EOBR config parms are set:
    ///    1. EOBR identifier, which is also considered the tractor number of the unit.
    ///    2. Sleep mode timeout
    ///    3. Data collection rate interval
    ///    4. Discovery Passkey
    /// The J-Bus will be auto-detected and then set to the detected value.
    /// Answer if all EOBR parms are successfully set.
    /// </summary>
    /// <param name="eobrId">name to set the EOBR</param>
    /// <returns>true if successful, false otherwise</returns>
    private boolean PerformAutoDetectTABConfiguration(String eobrId) throws KmbApplicationException
    {
        boolean isSuccessful = false;

        if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            int rc = -1;
            EobrReader eobr = eobrReader;

            try
            {
                eobr.SuspendReading();
                
	            isSuccessful = this.PerformGeneralEobrConfiguration(eobrId);
	            if (!isSuccessful) return false;

	            // set bus type in TAB to have the tab detect the active bus type
	            rc = eobr.Technician_SetBusType(DatabusTypeEnum.UNKNOWN);

	            if (rc == EobrReturnCode.S_SUCCESS)
	            {
	            	// delay after setting auto detect to allow the TAB time to perform auto-detect
	            	try {
	            		Thread.sleep(45000); // 45 seconds
	            	} catch (InterruptedException e1) {
	            		e1.printStackTrace();
	            		Log.e("UnhandledCatch", e1.getMessage() + ": " + Log.getStackTraceString(e1));
	            	}

	            	DatabusTypeEnum databusType = eobr.Technician_GetBusType(this.getContext());
	            	if (databusType != null && databusType.getValue() != DatabusTypeEnum.UNKNOWN)
	            	{
	            		isSuccessful = true;
	            		
	    	            // attempt to submit the config to DMO
	    	            isSuccessful = this.ReadAndSendCurrentConfigToDMO();
	            	}
	            	else
	            	{
	            		isSuccessful = false;	            		
	            	}
	            	
	            	// BT times out waiting for autoscan of bus type for 90 seconds and thresholds
	            	// are set to default values.  So reset thresholds to driver's values
	        		LogEntryController leCtrlr = new LogEntryController(this.getContext());
	        		leCtrlr.SetThresholdValues(this.getCurrentDesignatedDriver());
	            }
	            else
	            	isSuccessful = false;
	        }
            finally
            {
                eobr.ResumeReading();
            }
        }

        // return if successful or not
        return isSuccessful;
    }

    /// <summary>
    /// Perform the general EOBR configuration.  The following EOBR config parms are set:
    ///    1. EOBR identifier, which is also considered the tractor number of the unit.
    ///    2. Sleep mode timeout
    ///    3. Data collection rate interval
    ///    4. Discovery Passkey
    /// Set the Engine Databus type (JBus) according to the specified value.
    /// Answer if all EOBR parms are successfully set.
    /// </summary>
    /// <param name="eobrId">name to set the EOBR</param>
    /// <returns>true if successful, false otherwise</returns>
    private boolean PerformEobrConfigurationForBus(String eobrId, DatabusTypeEnum busType)
    {
        boolean isSuccessful = false;
		if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            EobrReader eobr = eobrReader;

            try
            {
                eobr.SuspendReading();

                isSuccessful = this.PerformGeneralEobrConfiguration(eobrId);
                if (!isSuccessful) return false;

				// performing configuration creates some events that require reading history instead of just resuming
				eobr.ReadHistoryOnTimerPop();

                // set the J-bus for data
                if (this.getIsEobrGenI())
                {
                	// for gen I, the enum values for 1708 and 1939 don't
                	// correspond to the values that need to be sent to the eobr
                	// set the correct value for setting gen I bus type
                	int genIBusType = 0;
                	switch (busType.getValue())
                	{
	                	case DatabusTypeEnum.J1708:
	                		genIBusType = 1;
	                		break;
	                	case DatabusTypeEnum.J1939:
	                		genIBusType = 2;
	                		break;
                	}
                	
                	if (genIBusType > 0)
                	{
                        int rc = eobr.Technician_SetBusType(genIBusType);
                        if (rc != 0) return false;                	
                	}
                	else
                		return false;
                }
                else
                {
	                int rc = eobr.Technician_SetBusType(busType.getValue());
	                if (rc != 0) return false;
                }

                // attempt to submit the config to DMO
                isSuccessful = this.ReadAndSendCurrentConfigToDMO();
            }
            finally
            {
                eobr.ResumeReading();
            }
        }

        return isSuccessful;
    }
    
    /// <summary>
    /// Perform the general EOBR configuration.  The following EOBR config parms are set:
    ///    1. EOBR identifier, which is also considered the tractor number of the unit.
    ///    2. Sleep mode timeout
    ///    3. Data collection rate interval
    ///    4. Discovery Passkey
    /// The sleep mode timeout, data collection rate and discovery passkey values
    /// come from the CompanyConfig settings.
    /// Answer if all EOBR parms are successfully set.
    /// </summary>
    /// <param name="eobrId">name to set the EOBR (tractor number)</param>
    /// <returns>true if successful, false otherwise</returns>
    private boolean PerformGeneralEobrConfiguration(String eobrId)
    {
        int rc = -1;
        EobrReader eobr = eobrReader;

        // set the EOBR id
        String currentId = eobr.getEobrIdentifier();
        if (!currentId.equalsIgnoreCase(eobrId))
        {
            rc = eobr.Technician_SetUniqueIdentifier(eobrId);
            if (rc != 0) return false;
            else
            {
        		// 2014.06.25 sjn - added retry logic to attempt to read the tractor number a few times before quitting
            	boolean isSuccessful = this.PerformReadTractorNumberWithRetry(eobrId);
            	if(!isSuccessful) return false;
            }
        }

        // set the sleep mode 
        int currentTimeout = eobr.Technician_GetEngineOffCommsTimeoutDuration(this.getContext());
        if (currentTimeout != GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrSleepModeMinutes())
        {
        	if (this.getIsEobrGenI())
        		rc = eobr.Technician_SetEngineOffCommsTimeoutDuration(GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrSleepModeMinutes());
        	else
        		// for gen II always set EngineOffCommsTimeout value in TAB to 1, regardless of
        		// of value specified in CompanyConfigSettings
        		rc = eobr.Technician_SetEngineOffCommsTimeoutDuration(1);
        	
            if (rc != 0) return false;
        }

        // set the data collection rate
        int currentRate = eobr.Technician_GetDataCollectionRate(this.getContext());
        if (currentRate != GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrDataCollectionRateSeconds())
        {
            rc = eobr.Technician_SetDataCollectionRate(GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrDataCollectionRateSeconds());
            if (rc != 0) return false;
        }

        // set the discovery pass key
        String currentPasskey = eobr.Technician_GetCompanyPassKey(this.getContext());
        if (currentPasskey != null && !currentPasskey.equalsIgnoreCase(GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrDiscoveryPasskey()))
        {
            rc = eobr.Technician_SetCompanyPassKey(GlobalState.getInstance().getCompanyConfigSettings(getContext()).getEobrDiscoveryPasskey());
            if (rc != 0) return false;
        }

        
        // Set the remaining Company Configuration Settings that were downloaded from Encompass.
        CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());
        
        String driverId = "";
        if(this.getCurrentDesignatedDriver() != null && this.getCurrentDesignatedDriver().getCredentials() != null)
        	driverId = this.getCurrentDesignatedDriver().getCredentials().getEmployeeCode();


        Bundle bundle = SetThresholdValues(this.getContext(), companyConfigSettings.getMaxAcceptableTach(), companyConfigSettings.getMaxAcceptableSpeed(),
				companyConfigSettings.getHardBrakeDecelerationSpeed(), companyConfigSettings.getDriverStartDistance(), companyConfigSettings.getDriverStopMinutes(),
				10, driverId, companyConfigSettings.getMandateDrivingStopTimeMinutes(), companyConfigSettings.getDriveStartSpeed());

		// If the bundle returned is null or the return code is not 'S_SUCCESS', then return false indicating that the
    	if (bundle == null || bundle.getInt(getContext().getString(R.string.rc)) != EobrReturnCode.S_SUCCESS)
    	{
    		return false;
    	}
    	// need to set the driver's threshold settings back up.  The company defaults were downloaded to the TAB for when a driver
    	// is not connected.  However, currently a driver is connected, so set the driver's threshold settings up again
    	else
    	{
    		// AMO - 12/11/12 Set the EOBR threshold values using the Unit Rules or Company Rules
    		LogEntryController leCtrlr = new LogEntryController(this.getContext());
    		leCtrlr.SetThresholdValues(this.getCurrentDesignatedDriver());
            
    	}
        
        // determine if the reference timestamp needs to be reset
        try
        {
        	if (eobr.isEobrGen1())
        	{
	            Date referenceTimestamp = eobr.Technician_ReadReferenceTimestamp(this.getContext());
	            if (referenceTimestamp == null)
	            {
	                // the reference timestamp is not set yet
	                // This means that the EOBR is have never been used
	                // Update the reference timestamp to ignore any data records
	                // that may be on the EOBR already when reading history
	            	EobrEventArgs eobrEventArgs = new EobrEventArgs();
	            	StatusRecord statusRec = eobrEventArgs.getStatusRecord();
	                rc = eobr.Technician_GetCurrentData(statusRec,true);
	                if (rc != 0) return false;
	            }
        	}
        }
        catch (Exception ex)
        {
            return false;
        }

        // if it makes it here, it must be successful!
        return true;
    }
    
    /*
     * Attempt to read the tractor number from the ELD.
     * Try 3 times to read it before returning a failure.
     */
    private boolean PerformReadTractorNumberWithRetry(String eobrId)
    {
    	boolean isSuccessful = false;
        EobrReader eobr = eobrReader;

        boolean done = false;
        int checkCount = 1;

        // 2014.06.26 sjn - Add retry logic such that it will try to read the tractor number a couple times
        while(!done){
        	
	    	// 2/8/2012 JHM - Changing the UniqueId creates a period of time where the EOBR does   
	    	// not respond in a timely fashion.  Add a delay and then try to read the UniqueId.  
	        try {
				Thread.sleep(10000); // 10 seconds
				String readNewId = eobr.Technician_GetUniqueIdentifier(getContext());
				if(readNewId == null)
				{
					ErrorLogHelper.RecordMessage("PerformReadTractorNumberWithRetry - Retry obtaining new Unique Id");
					Log.i("Comm", "PerformReadTractorNumberWithRetry - Retry obtaining new Unique Id");
				}
				else{
					ErrorLogHelper.RecordMessage("New EOBR Unique Id: " + readNewId);
					Log.i("Comm", "New EOBR Unique Id: " + readNewId);			
					isSuccessful = true;
					done = true;
				}
			} catch (InterruptedException e) {
				Log.e("Comm", "Failed calling Technician_GetUniqueIdentifier (InterruptedException)");
				ErrorLogHelper.RecordException(e);
			} catch (Throwable e){
				Log.e("Comm", "Failed calling Technician_GetUniqueIdentifier (Throwable)");
				ErrorLogHelper.RecordException(e);
			}
	        
	        if(!done){
	        	// if not successfully read the UniqueId, then try again until we've tried 3 times
	        	done = checkCount++ >= 3;
	        }
        }
        
        return isSuccessful;
    }
    
    /// <summary>
    /// Read the current config from the EOBR, and send it to DMO.
    /// Success is when the config can be read, saved to the local database, 
    /// and submitted to DMO.
    /// </summary>
    /// <returns>true is successfully read and sent to DMO, false otherwise</returns>
    private boolean ReadAndSendCurrentConfigToDMO()
    {
        boolean isSuccessful = false;
        if (this.IsEobrDeviceOnline())
        {
            EobrConfiguration eobrConfig = this.ReadConfig();
            if (eobrConfig != null)
            {
                isSuccessful = this.SaveConfig(eobrConfig);
                if (isSuccessful)
                {
                    isSuccessful = this.SubmitEobrConfigurationsToDMO();
                }
            }
        }
        return isSuccessful;
    }
    
    /// <summary>
    /// Answer the current config read directly from the EOBR
    /// </summary>
    /// <returns></returns>
    private EobrConfiguration ReadConfig()
    {
        EobrConfiguration eobrConfig = null;
        if (this.IsEobrDeviceOnline())
        {
            EobrReader rdr = eobrReader;
			String serialNumber = null;
            try{
				serialNumber = rdr.Technician_GetSerialNumber(this.getContext());
            }
            catch(KmbApplicationException kex){
            	Log.e("UnhandledCatch", kex.getMessage() + ": " + Log.getStackTraceString(kex));
            }

            if (serialNumber != null) {
				eobrConfig = GetConfigFromDB(serialNumber);
			}

			if (eobrConfig == null) {
				eobrConfig = new EobrConfiguration();
			}

			eobrConfig.setSerialNumber(serialNumber);

            eobrConfig.setTractorNumber(rdr.getEobrIdentifier());
            eobrConfig.setSleepModeMinutes(rdr.Technician_GetEngineOffCommsTimeoutDuration(this.getContext()));
            eobrConfig.setDataCollectionRate(rdr.Technician_GetDataCollectionRate(this.getContext()));
            eobrConfig.setDiscoveryPasskey(rdr.Technician_GetCompanyPassKey(this.getContext()));
            switch (rdr.Technician_GetBusType(this.getContext()).getValue())
            {
            	case DatabusTypeEnum.UNKNOWN:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.UNKNOWN));
            		break;
    	    	case DatabusTypeEnum.J1850VPW:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1850VPW));
            		break;
    	    	case DatabusTypeEnum.J1850PWM:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1850PWM));
            		break;
    	    	case DatabusTypeEnum.ISO91412:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.ISO91412));
            		break;
    	    	case DatabusTypeEnum.KWP2000:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.KWP2000));
            		break;
    	    	case DatabusTypeEnum.BIT11CANBUS250:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.BIT11CANBUS250));
            		break;
    	    	case DatabusTypeEnum.BIT11CANBUS500:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.BIT11CANBUS500));
            		break;
    	    	case DatabusTypeEnum.BIT29CANBUS250:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.BIT29CANBUS250));
            		break;
    	    	case DatabusTypeEnum.BIT29CANBUS500:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.BIT29CANBUS500));
            		break;
    	    	case DatabusTypeEnum.DUALMODEJ1708J1939:
            		eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.DUALMODEJ1708J1939));
            		break;
                case DatabusTypeEnum.J1708:
                    eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1708));
                    break;
                case DatabusTypeEnum.J1939:
                    eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1939));
                    break;
				case DatabusTypeEnum.J1939F:
					eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1939F));
					break;
				case DatabusTypeEnum.DUALMODEJ1708J1939F:
					eobrConfig.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.DUALMODEJ1708J1939F));
					break;
            }
            Bundle bundle = rdr.Technician_GetEOBRRevisions();
            eobrConfig.setFirmwareVersion(bundle.getString(this.getContext().getString(R.string.mainfirmwarerevision)));
            eobrConfig.setEobrGeneration(rdr.getEobrGeneration());
            
            if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()) != null)
            {
            	eobrConfig.setTachometerThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMaxAcceptableTach());
            	eobrConfig.setSpeedometerThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMaxAcceptableSpeed());                	
            	eobrConfig.setHardBrakeThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getHardBrakeDecelerationSpeed());
            }
        }
        return eobrConfig;
    }
    
    /// <summary>
    /// Calculate the actual dashboard odometer using the EOBR calibration
    /// values.     
    /// If no EOBR is connected, then return -1.0
    /// If EOBR is connected, but cannot read offset, return 0
    /// </summary>
    /// <returns>dashboard odometer value</returns>
    public float CalculateDashboardOdometer()
    {
    	float dashOdom = -1.0F;
        
        if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            EobrReader eobr = eobrReader;
            eobr.SuspendReading();
            
            try{
	            // read the odometer directly from the EOBR
	            StatusRecord statusRec = new StatusRecord(); 
	            
	            int rc = eobr.Technician_GetCurrentData(statusRec, false);
	            if (rc == 0)
	            {
	                // able to read the current data from the eobr
	                // read the odometer offset from the EOBR
	
	                // if offset is successfully read from eobr, set dash based on offset
	                Bundle bundleOdometerCalibration = eobr.Technician_ReadOdometerCalibrationValues();
	                if (bundleOdometerCalibration != null)
	                {
	                    // the dashboard odometer is the eobr reading plus the offset
	                    // Note:  offset is stored in miles
	
	                	float offset = bundleOdometerCalibration.getFloat(this.getContext().getString(R.string.offsetparam));
	                	
	                	// if displaying distance as kilometers, convert statusrec odometer to kilometers
	                    if(this.getCurrentUser().getDistanceUnits().equalsIgnoreCase(this.getContext().getString(R.string.kilometers)))
	                    {
	                    	dashOdom = statusRec.getOdometerReadingKM();
	                    	offset = offset * GlobalState.MilesToKilometers;
	                    }
	                    else
	                    	dashOdom = statusRec.getOdometerReadingMI();
	
	                    dashOdom += offset;	                    
	                }
	                // if we cannot read the offset, set dashOdom to 0, we will not 
	                // prepopulate the dashboard odom - driver will have to enter
	                else
	                {
	                    dashOdom = 0;
	                }
	            }
            }
            finally{
            	eobr.ResumeReading();
            }

        }

        return dashOdom;
    }
	public float GetRawOdometer()
	{
		float odom = -1.0F;
		if (this.IsEobrDeviceOnline())
		{
			// the EOBR is connected, initialized, and online
			EobrReader eobr = eobrReader;
			eobr.SuspendReading();

			try {

				// read the odometer directly from the EOBR
				StatusRecord statusRec = new StatusRecord();

				int rc = eobr.Technician_GetCurrentData(statusRec, false);
				if (rc == 0) {
					odom = statusRec.getOdometerReading();
				}
			} finally {
				eobr.ResumeReading();
			}
		}
		return odom;
	}
    
    /// <summary>
    /// Answer if odometer calibaration is required.
    /// If there is no EOBR connected, then calibration is not required.
    /// Calibration is required if the last time it was calibrated is
    /// more than 30 days ago.
    /// </summary>
    /// <returns>true if calibration is required, false otherwise</returns>
    public boolean IsOdometerCalibrationRequired()
    {
        boolean isRequired = false;

        if (this.IsEobrDeviceOnline())
        {
            // if there's an EOBR connected, then is starts out required until we prove otherwise
            isRequired = true;

            EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext());
            EobrConfiguration config = facade.Fetch(eobrReader.getEobrSerialNumber());
            if (config == null)
            {
            	return isRequired;
            	// Newer versions of android do not allow network connections from the main ui thread
            	// therefore, if the current eobr is not setup on this phone, require odometer calibration
            	// to occur.
            	
                // no config in the db, attempt to get it from DMO
                // this must be the first time we've been connected to this EOBR
            	//config = this.DownloadConfigFromDMO();
            }

            if (config != null && config.getOdometerCalibrationDate() != null)
            {
    			long diff = this.getCurrentClockHomeTerminalTime().getTime() - config.getOdometerCalibrationDate().getTime();
    			double diffTotalDays = DateUtility.ConvertMillisecondsToHours(diff) / HOURS_PER_DAY;

                
            	if (diffTotalDays < ODOMETER_CALIBRATION_PERIOD_DAYS)
                {
                    // it's been less than 30 days since last calibrated
                    // so it's not required at this time
                    isRequired = false;
                }
            }
        }

        return isRequired;

    }
    
    /// <summary>
    /// Perform the odometer calibration using the simple offset technique.
    /// The concept is that the J-bus odometer is off by a constant number of miles.
    /// Answer if successfully performed the calculations, saved to the EOBR, and 
    /// sent the configuration info to DMO.
    /// </summary>
    /// <param name="actualDashboardOdometerValue">actual reading of the dashboard odometer</param>
    /// <returns> -1 - detected engine is not running
    ///			   0 - calibration failed
    ///			   1 - calibration was successful </returns>
    @SuppressWarnings("unused")
	public int PerformOdometerCalibrationOffset(float actualDashboardOdometerValue) throws Exception
    {
    	// TODO OdometerCalibration - Waiting for eobr API calls to be written
        int calibrationStatus = 0;

        if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            EobrReader eobr = eobrReader;
            eobr.SuspendReading();
            
            try{
	            StatusRecord statusRec = new StatusRecord();
	            int rc = eobr.Technician_GetCurrentData(statusRec, false);
	            if (rc == 0 && ((eobr.getEobrEngine().GetEobrGeneration() == 1 && statusRec.getIsEngineTelemetryAvailable()) || (eobr.getEobrEngine().GetEobrGeneration() == 2 && statusRec.getIsEngineOn()) || (eobr.getEobrEngine().GetEobrGeneration() == 7 && statusRec.getIsEngineOn())))
	            {
	                float currentEobrOdom = -1F;
	
	                Bundle bundleOdometerRange = eobr.OdometerInValidRange(statusRec.getTimestampUtc(), statusRec.getOdometerReading());
	                if(bundleOdometerRange.getBoolean(ISINVALIDRANGE))
	                	currentEobrOdom = statusRec.getOdometerReading();
	
	                float offset = 0.0F;
	                if (currentEobrOdom >= 0)
	                {
	                    // if displaying distance as kilometers (assumption is dashboard value is in KM), 
	                	// need to convert dashboard value which will be in kilometers, to miles
	                    if (this.getCurrentUser().getDistanceUnits().equalsIgnoreCase(this.getContext().getString(R.string.kilometers)))
	                        actualDashboardOdometerValue = actualDashboardOdometerValue / GlobalState.MilesToKilometers;
	
	                    if (actualDashboardOdometerValue != currentEobrOdom)
	                    {
	                        // if the actual dashboard odom, and the raw odom are different
	                        // then calc the offset
	                        offset = (float)Math.round((actualDashboardOdometerValue - currentEobrOdom) * 10f)/ 10f;
	                    }
	                    if (this.SaveOdometerCalibrations(offset, 0.0F))
	                    {
	                    	calibrationStatus = 1;
	                    	hasEobrOdometerOffsetBeenSet = true;
	                    	EOBR_ODOMETER_OFFSET_MILES = offset;	                    		
	                    }
	                }
	                else
	                {
	                    SimpleDateFormat dateFormat = DateUtility.getHomeTerminalDateTimeFormat24Hour();
	                    ErrorLogHelper.RecordMessage(getContext(), String.format(this.getContext().getString(R.string.msg_eobrperformcalibration), eobrReader.getEobrIdentifier(), statusRec.getOdometerReading(), dateFormat.format(statusRec.getTimestampUtc()), eobr.getLastEobrOdometer(), dateFormat.format(eobr.getLastEobrOdometerUTCTime())));
	                }
	                
	                if (calibrationStatus == 1)
	                {
	                    // so far so good, keep going
	                    // try to send odometer calibration info to DMO
	                    EobrConfiguration eobrConfig = this.ReadConfig();
	
	                    // add odometer calibration values to the current config of the EOBR
	                    Date now = this.getCurrentClockHomeTerminalTime();
	                    eobrConfig.setOdometerCalibrationDate(now);
	                    eobrConfig.setDashboardOdometer(actualDashboardOdometerValue);
	                    eobrConfig.setEobrOdometer(currentEobrOdom);
	
	                    // save the config to the database
	                    calibrationStatus = this.SaveConfig(eobrConfig) ? 1 : 0;
	
	                    // send the configs to DMO
	                    if (calibrationStatus == 1)
	                    {
	                        boolean submitSuccess = this.SubmitEobrConfigurationsToDMO();
	                    }
	                }
	            }
	            // if call to retrieve data is successful, means engine wasn't running
	            else if (rc == 0) 
	            	calibrationStatus = -1;
            	else
            		calibrationStatus = 0;	            		
            }
            finally{
            	eobr.ResumeReading();
            }
        }

        return calibrationStatus;
    }
    
    /// <summary>
    /// Save the odometer calibration values to the EOBR.
    /// Answer if succesfully saved the values.
    /// </summary>
    /// <param name="offset"></param>
    /// <param name="multiplier"></param>
    /// <returns></returns>
    private boolean SaveOdometerCalibrations(float offset, float multiplier)
    {
        boolean isSuccessful = false;

        // TODO - Implement SetOdometer Calibration
        EobrReader eobr = eobrReader;
        int rc = eobr.Technician_SetOdometerCalibration(offset, multiplier);
        isSuccessful = rc == 0;

        return isSuccessful;
    }
    
    /// <summary>
    /// Answer if current calculated odometer offset seems within valid 
    /// range of previous offset.
    /// </summary>
    /// <param name="curDashboardOdom">Current dashboard odometer entered by user</param>
    /// <param name="eobrOdometerValid">Is odometer read from eobr valid</param>
    /// <returns></returns>
//    public boolean IsOdometerOffSetValid(float curDashboardOdom, out bool eobrOdometerValid)
    public Bundle IsOdometerOffSetValid(float curDashboardOdom)
    {
    	// TODO OdometerCalibration
        boolean isValid = true;
        boolean isEobrOdometerValid = true;
        
        Bundle bundleOdomterOffSet = new Bundle();

        // the EOBR is connected, initialized, and online
        EobrReader eobr = eobrReader;
        eobr.SuspendReading();
        
        try{
	        EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext());
	        EobrConfiguration config = facade.Fetch(eobr.getEobrSerialNumber());
	
	        if (eobr.getLastEobrOdometerUTCTime() == null)
	        {
	            LogEntryController ctrlr = new LogEntryController(this.getContext());
	
	            Bundle bundleLastEobrOdometer = ctrlr.GetLastEobrOdometer();
	
	            eobr.setLastEobrOdometer(bundleLastEobrOdometer.getFloat(this.getContext().getString(R.string.lasteobrodometer)));
	    		if (bundleLastEobrOdometer.getString(this.getContext().getString(R.string.lasteobrodometertimestamp)) != null)
	    			eobr.setLastEobrOdometerUTCTime(new Date(bundleLastEobrOdometer.getString(this.getContext().getString(R.string.lasteobrodometertimestamp))));
	    		else
	    			eobr.setLastEobrOdometerUTCTime(null);
	        }
	
	        StatusRecord statusRec = new StatusRecord();
	        int rc = eobr.Technician_GetCurrentData(statusRec, false);
	        if (rc == 0)
	        {
	            float currentEobrOdom = -1F;
	
	            Bundle bundleOdomValidRange = eobr.OdometerInValidRange(statusRec.getTimestampUtc(), statusRec.getOdometerReading());
	            if (bundleOdomValidRange.getBoolean(ISINVALIDRANGE))
	            {
	                currentEobrOdom = statusRec.getOdometerReading();
	            }
	            else
	            {
	                isEobrOdometerValid = false;
	                ErrorLogHelper.RecordMessage(getContext(), String.format(this.getContext().getString(R.string.invalidodometer), eobrReader.getEobrIdentifier(), Float.toString(statusRec.getOdometerReading()), DateUtility.getHomeTerminalDateTimeFormat24Hour().format(statusRec.getTimestampUtc()), Float.toString(eobr.getLastEobrOdometer()), DateUtility.getHomeTerminalDateTimeFormat24Hour().format(eobr.getLastEobrOdometerUTCTime())));
	            }
	
	            // if we have previously calibrated, compare previous offset to current offset
	            if (config != null && config.getDasboardOdometer() > 0 && config.getEobrOdometer() > 0 && curDashboardOdom > 0 && currentEobrOdom > 0)
	            {
	                float prevOffSet = config.getDasboardOdometer() - config.getEobrOdometer();
	                float curOffSet = curDashboardOdom - currentEobrOdom;
	
	                if (Math.abs(prevOffSet - curOffSet) > 20.0F)
	                {
	                    isValid = false;
	                }
	            }
	        }
	        else if (rc == (int)EobrReturnCode.S_DEV_NOT_CONNECTED) {
	        	Bundle bundleNotConnected = new Bundle();
	        	bundleNotConnected.putInt(this.getContext().getString(R.string.isconnectedforodometercal), EobrReturnCode.S_DEV_NOT_CONNECTED);
	        	return bundleNotConnected;
	        }
        }
        finally {
        	eobr.ResumeReading();
        }
        
        bundleOdomterOffSet.putBoolean(this.getContext().getString(R.string.isodometeroffsetvalid), isValid);
        bundleOdomterOffSet.putBoolean(this.getContext().getString(R.string.iseobrodometervalid), isEobrOdometerValid);
        return bundleOdomterOffSet;
    }
    
    /// <summary>
    /// Get EobrConfiguration from database
    /// </summary>
    /// <returns></returns>
    public EobrConfiguration GetConfigFromDB()
    {
        // the EOBR is connected, initialized, and online
        EobrReader eobr = eobrReader;

        if (eobr.getCurrentConnectionState() == ConnectionState.ONLINE)
        {
        	EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext());
        	try{
        		return facade.Fetch(eobr.Technician_GetSerialNumber(getContext()));
        	}
        	catch(KmbApplicationException kmb){
        		return null;
        	}
        }
        else
        	return null;        		        
    }

    /// <summary>
    /// Get EobrConfiguration from database
    /// </summary>
    /// <returns></returns>
    public EobrConfiguration GetConfigFromDB(String serialNum)
    {
        EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext());
        return facade.Fetch(serialNum);        
    }

    public void LoadOdometerOffsetFromEobr(){
    	hasEobrOdometerOffsetBeenSet = false;
    	EOBR_ODOMETER_OFFSET_MILES = 0.0f;
        if (this.IsEobrDeviceOnline())
        {
            // the EOBR is connected, initialized, and online
            EobrReader eobr = eobrReader;
            
            // if offset is successfully read from eobr, set dash based on offset
            Bundle bundleOdometerCalibration = eobr.Technician_ReadOdometerCalibrationValues();
            if (bundleOdometerCalibration != null)
            {
                // the dashboard odometer is the eobr reading plus the offset
                // Note:  offset is stored in miles
            	float offset = bundleOdometerCalibration.getFloat(this.getContext().getString(R.string.offsetparam));

                hasEobrOdometerOffsetBeenSet = true;
                EOBR_ODOMETER_OFFSET_MILES = offset;	                    
            }            
        }
    }
    
    public float getCurrentOdometerOffsetForUser(User user)
    {
    	float offset = 0.0f;
    	
    	if(hasEobrOdometerOffsetBeenSet)
    	{
    		if(user != null)
    		{
            	// if displaying distance as kilometers, convert statusrec odometer to kilometers
                if(user.getDistanceUnits().equalsIgnoreCase(this.getContext().getString(R.string.kilometers)))
                	offset = EOBR_ODOMETER_OFFSET_MILES * GlobalState.MilesToKilometers;
                else
                	offset = EOBR_ODOMETER_OFFSET_MILES;
    		}
    		else
    			offset = EOBR_ODOMETER_OFFSET_MILES;
    	}
    	
    	return offset;
    }
    
    /**
     * Answer if the ELD should be power-cycled by determining if enough time has elapsed since the last one
     * @return
     */
    public boolean DetermineIfEobrShouldBePowerCycled(String serialNumber){
    	boolean shouldPowerCycleResetELD = false;
		EobrConfiguration eobrConfig = this.GetConfigFromDB(serialNumber);
		Date lastPowerCycleResetDate = null;
		if(eobrConfig != null)
		{
			lastPowerCycleResetDate = eobrConfig.getLastPowerCycleResetDate();
			if(lastPowerCycleResetDate == null)
				// there is no record of a power cycle, so should probably do it now
				shouldPowerCycleResetELD = true;
			else {
				Date now = DateUtility.getCurrentDateTimeUTC();
				Date cutoffDate = DateUtility.AddHours(now, POWERCYCLE_RESET_PERIOD_HOURS * -1);
				if(lastPowerCycleResetDate.compareTo(cutoffDate) < 0)
					// the ELD should be power cycled once the POWERCYCLE_RESET_PERIOD_HOURS have elapsed since the last reset
					shouldPowerCycleResetELD = true;
			}
		}
		
		ErrorLogHelper.RecordMessage(String.format("EobrConfigController.DetermineIfEobrShouldBePowerCycled: Last powercycle reset date: {%s} should be power-cycled now: {%s}", lastPowerCycleResetDate, shouldPowerCycleResetELD));

		return shouldPowerCycleResetELD;
    }
    
    /**
     * Update the last date that the specified ELD was power-cycled
     * @param serialNumber
     */
    public void UpdateEobrPowerCycleResetDate(String serialNumber){
    	EobrConfiguration eobrConfig = this.GetConfigFromDB(serialNumber);
		if(eobrConfig != null)
		{
			Date now = DateUtility.getCurrentDateTimeUTC();
			eobrConfig.setLastPowerCycleResetDate(now);
			this.SaveConfig(eobrConfig);
		}
    }
}
