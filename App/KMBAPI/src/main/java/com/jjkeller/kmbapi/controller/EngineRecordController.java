package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.EventLog;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EngineRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EventDataRecordFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.IWebAPIServiceHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.WebAPIServiceHelperFactory;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.EngineRecordTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EngineRecord;
import com.jjkeller.kmbapi.proxydata.EngineRecordList;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.EventDataRecord;
import com.jjkeller.kmbapi.proxydata.EventDataRecordList;
import com.jjkeller.kmbapi.proxydata.GpsLocation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//import com.jjkeller.kmbapi.proxydata.EventRecord;

public class EngineRecordController extends ControllerBase {

	private static final float KPH_TO_MPH = 0.6213712F;
	
	private static final AtomicBoolean _isSubmittingEngineRecords = new AtomicBoolean(false);
	private static final AtomicBoolean _isSubmittingEventDataRecords = new AtomicBoolean(false);

	private boolean _isEnabled = false;
	public boolean getIsEnabled() {
		return _isEnabled;
	}
	public void setIsEnabled(boolean isEnabled) {
		this._isEnabled = isEnabled;
	}

	private EobrConfiguration getCurrentEobrConfiguration(){
		return GlobalState.getInstance().getCurrentEobrConfiguration();
	}
	private void setCurrentEobrConfiguration(EobrConfiguration currentEobrConfiguration){
		GlobalState.getInstance().setCurrentEobrConfiguration(currentEobrConfiguration);
	}

	private StatusRecord getPreviousEobrStatusRecord(){
		return GlobalState.getInstance().getPreviousEobrStatusRecord();
	}
	private void setPreviousEobrStatusRecord(StatusRecord previousEobrStatusRecord){
		GlobalState.getInstance().setPreviousEobrStatusRecord(previousEobrStatusRecord);
	}

	private EventRecord getPreviousEobrEventRecord(){
		return GlobalState.getInstance().getPreviousEobrEventRecord();
	}
	private void setPreviousEobrEventRecord(EventRecord previousEobrEventRecord){
		GlobalState.getInstance().setPreviousEobrEventRecord(previousEobrEventRecord);
	}

	private Date getNextEngineRecordingTimestamp(){
		return GlobalState.getInstance().getNextEngineRecordingTimestamp();	
	}
	private void setNextEngineRecordingTimestamp(Date nextEngineRecordingTimestamp){
		GlobalState.getInstance().setNextEngineRecordingTimestamp(nextEngineRecordingTimestamp);	
	}
	
	private int _recordingInterval = -1;
	private int getRecordingInterval()
	{
		return _recordingInterval;
	}
	private void setRecordingInterval(int recordingInterval)
	{
		this._recordingInterval = recordingInterval;
	}

    public EngineRecordController(Context ctx, AppSettings appSettings)
    {
        super(ctx);

        int milliseconds = appSettings.getStatusRecordLogIntervalMS();
        if (milliseconds < 0)
        {
            this.setIsEnabled(false);
            this.setRecordingInterval(0);
        }
        else
        {
            this.setIsEnabled(true);
            this.setRecordingInterval(milliseconds);
        }
    }

    /// <summary>
    /// Submit all engine records that need to be sent up to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    public boolean SubmitEngineRecordsToDMO()
    {
        boolean isSuccesful = false; 

        if (this.getIsWebServicesAvailable())
        {
            try
            {                
                // first fetch unsubmitted engine records, but only fetch at most a limited number to avoid 
                // issues where large number of unsubmitted records exist
                EngineRecordFacade facade = new EngineRecordFacade(this.getContext(), this.getCurrentUser());                
                List<EngineRecord> unSubmittedRecords = facade.FetchUnsubmittedLimited(2000);

                WebAPIServiceHelperFactory apiServiceHelperFactory = new WebAPIServiceHelperFactory();
                IWebAPIServiceHelper apiHelper = apiServiceHelperFactory.getInstance(getContext());

                // are there any to send? 
                if (unSubmittedRecords != null && unSubmittedRecords.size() > 0)
                {
                    // build the EngineRecordList for each unique EOBR/Driver 
                    // combination found it the unsubmitted list
                    String lastSerialNumber = null;
                    String lastDriverId = null;
                    EngineRecordList listToSend = null;
                    ArrayList<EngineRecord> recordList = null;
                    int numToSend = 250;
 
                    for (EngineRecord rec : unSubmittedRecords)
                    {
                        if (!rec.getDriverEmployeeId().equalsIgnoreCase(lastDriverId) || !rec.getEobrSerialNumber().equalsIgnoreCase(lastSerialNumber))
                        {
                            // a record with either a new driver, or EOBR is discovered
                            if (listToSend != null && recordList != null && recordList.size() > 0)
                            {
                                // there is a previous list (from a different driver/eobr) to send to DMO
                       			listToSend.setEngineRecords(recordList.toArray(new EngineRecord[recordList.size()]));
                   	           	
                        		try
                        		{
                                    apiHelper.SubmitEngineRecords(listToSend);
                            		
                                    // mark the list just sent to DMO as submitted
                                    facade.MarkAsSubmitted(recordList);
            	            	}
            	            	catch (JsonSyntaxException e)
            	            	{
            	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));
            	            	}
            	            	catch (IOException e)
            	            	{
            	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));                    		
            	            	}
                            }

                            // start a new list to hold the new stuff for this driver/EOBR
                            listToSend = new EngineRecordList(null);
                            listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
                            listToSend.setEobrTractorNumber(rec.getEobrTractorNumber());
                            listToSend.setDriverEmployeeId(rec.getDriverEmployeeId());

                            recordList = new ArrayList<EngineRecord>();
                            lastDriverId = rec.getDriverEmployeeId();
                            lastSerialNumber = rec.getEobrSerialNumber();
                        }

                        recordList.add(rec);

                        // Have enough records been collected to send?
                        if (recordList.size() >= numToSend)
                        {
                            // yes, so send this batch of records to DMO
                			listToSend.setEngineRecords(recordList.toArray(new EngineRecord[recordList.size()]));                		                           
                        	
                    		try
                    		{
                                apiHelper.SubmitEngineRecords(listToSend);
                        		
                        		// mark the list just sent to DMO as submitted
                                facade.MarkAsSubmitted(recordList);
        	            	}
        	            	catch (JsonSyntaxException e)
        	            	{
        	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));
        	            	}
        	            	catch (IOException e)
        	            	{
        	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));                    		
        	            	}

                            // start a new list to hold the new stuff for this driver/EOBR
                            listToSend = new EngineRecordList(null);
                            listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
                            listToSend.setEobrTractorNumber(rec.getEobrTractorNumber());
                            listToSend.setDriverEmployeeId(rec.getDriverEmployeeId());

                            recordList = new ArrayList<EngineRecord>();

                        }
                    }

                    if (listToSend != null && recordList != null && recordList.size() > 0)
                    {
                        // there are records to send to DMO
               			listToSend.setEngineRecords(recordList.toArray(new EngineRecord[recordList.size()]));
               			
                		try
                		{
                            apiHelper.SubmitEngineRecords(listToSend);
                    		
                            // mark those just sent, as submitted
                            facade.MarkAsSubmitted(recordList);
    	            	}
    	            	catch (JsonSyntaxException e)
    	            	{
    	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));
    	            	}
    	            	catch (IOException e)
    	            	{
    	            		this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));                    		
    	            	}
                    }
                }

                isSuccesful = true;
            }
            catch (Exception excp)
            {
            	this.HandleException(excp, this.getContext().getString(R.string.submitenginerecordstodmo));
            }
        }

        return isSuccesful;
    }
    
    /**
     * Asynchronously submit all engine records that need to be sent up to DMO
     */
    public void SubmitEngineRecordsToDMOAsync()
    {
    	if (_isSubmittingEngineRecords.compareAndSet(false, true))
    	{
    		try
			{
				new SubmitEngineRecordsTask(getContext()).execute();
			}
			catch (Exception ex)
			{
	        	Log.e("UnhandledCatch", ex.getMessage(), ex);
				_isSubmittingEngineRecords.set(false);
			}
    	}
    }

    public void ProcessNewStatusRecord(String eobrSerialNbr, String eobrTractorNbr, 
    		User designatedDriver, EmployeeLog driversLog, EobrEventArgs e)
    {
    	// Status Records are not processed when Minimum data profile
    	DataProfileEnum dataProfile = GlobalState.getInstance().getCurrentDesignatedDriver().getDataProfile();
    	if(e.getStatusRecord() != null
                && dataProfile.getValue() != DataProfileEnum.MINIMUMHOS
                && dataProfile.getValue() != DataProfileEnum.MINIMUMHOSWITHFUELTAX)
    	{
    		ProcessNewStatusRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, 
    				driversLog, e.getStatusRecord(), e.getReturnCode());
    	}
    	else if(e.getEventRecord() != null)
    	{
    		ProcessNewEventRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, 
    				driversLog, e.getEventRecord(), e.getReturnCode());
    	}
    	else
    	{
    		// 10/12/12 JHM - Not sure if this is a possible scenario
    		Log.i("ProcessNewStatusRecord", "EobrEventArgs didn't contain StatusRecord(Gen1) or EventRecord(Gen2) for processing.");
    	}
    }
    
	private void ProcessNewStatusRecord(String eobrSerialNbr,
			String eobrTractorNbr, User designatedDriver,
			EmployeeLog driversLog, StatusRecord eobrStatusRecord, int returnCode) {

        if (this.getIsEnabled())
        {
            // first, verify that we have the correct config loaded for the EOBR
            EobrConfiguration config = this.getCurrentEobrConfiguration();
            if (config == null || eobrSerialNbr == null || config.getSerialNumber() == null || config.getSerialNumber().compareTo(eobrSerialNbr) != 0)
            {
                // not loaded, or not the config that matches the EOBR we're talking to now
                // read it in from the DB
            	EobrConfigurationFacade eobrConfigFacade = new EobrConfigurationFacade(this.getContext(), this.getCurrentUser());
            	if(eobrSerialNbr != null)
            	{	                
	                config = eobrConfigFacade.Fetch(eobrSerialNbr);
            	}
            	if (config == null)
            	{
                    // no config found in the db for this eobr.   
                    // In this case build an empty config and assign the serial number and tractor number to it
                    // the defaults for the performance thresholds will be just fine
            		// Note: Since this is now stored in the handheld database and will be submitted to DMO, create default
            		// values for all fields so submission to DMO will work
                    config = new EobrConfiguration();
                    config.setSerialNumber(eobrSerialNbr);
                    config.setTractorNumber(eobrTractorNbr);
                    config.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1708));
                    config.setDataCollectionRate(1);
                    config.setDiscoveryPasskey("undefined");
                    config.setFirmwareVersion("undefined");
                    config.setSleepModeMinutes(-1);
            		
                    // persist the new config into the database
                    if(eobrSerialNbr != null) 
                    	eobrConfigFacade.Save(eobrSerialNbr, config);
                }
                this.setCurrentEobrConfiguration(config);
            }

            int dataProfile = this.getCurrentDesignatedDriver().getDataProfile().getValue();
            
            // if previous record is more than 1 second prior to current record and the hard
            // brake threshold has been exceeded between the previous and current record, check for hard
            // brakes that occurred every second between the previous and current record
            // Do not perform hard brake check for HOSWITHFUELTAXANDMAPPING profile
            if (dataProfile != DataProfileEnum.HOSWITHFUELTAXANDMAPPING &&
            		getPreviousEobrStatusRecord() != null && eobrStatusRecord != null && 
            		getPreviousEobrStatusRecord().getTimestampUtc() != null &&
            		eobrStatusRecord.getTimestampUtc() != null &&
            		eobrStatusRecord.getTimestampUtc().getTime() - getPreviousEobrStatusRecord().getTimestampUtc().getTime() > 1000 &&
                    getPreviousEobrStatusRecord().getSpeedometerReading() - eobrStatusRecord.getSpeedometerReading() > config.getHardBrakeThreshold())
            {
            	this.CheckForPreviousHardBrakes(eobrStatusRecord, eobrSerialNbr, eobrTractorNbr, designatedDriver, config.getHardBrakeThreshold());
            }

            // the feature is enabled
            if (this.ShouldProcessEngineRecord(eobrStatusRecord, returnCode, dataProfile))
            {
                // this eobr status record should be processed
                EngineRecordTypeEnum recType = this.ProcessEngineRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, eobrStatusRecord, dataProfile);

                // determine the next timestamp that should be processed
                Date nextTimestamp = this.CalculateNextRecordingTime(eobrStatusRecord, returnCode, dataProfile);
                this.setNextEngineRecordingTimestamp(nextTimestamp);

                if (recType.getValue() == EngineRecordTypeEnum.STANDARD)
                {
                    // only send to DMO on the 'standard' engine records
                    // which get recorded every so often
                    // attempt to submit the engine records to DMO
                    this.SubmitEngineRecordsToDMOAsync();

                    // save the driver's log in case anything has changed
                    IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                    empLogCtrlr.SaveLocalEmployeeLog(designatedDriver, driversLog);
                }
            }

            // save this record for later, so that the next time we get
            // a record in we can compare the one just prior
            setPreviousEobrStatusRecord(eobrStatusRecord);
        }
	}

	private void CheckEobrConfiguration(String eobrSerialNbr, String eobrTractorNbr)
	{
        // first, verify that we have the correct config loaded for the EOBR
        EobrConfiguration config = this.getCurrentEobrConfiguration();
        if (config == null || eobrSerialNbr == null || config.getSerialNumber() == null || config.getSerialNumber().compareTo(eobrSerialNbr) != 0)
        {
            // not loaded, or not the config that matches the EOBR we're talking to now
            // read it in from the DB
        	EobrConfigurationFacade eobrConfigFacade = new EobrConfigurationFacade(this.getContext(), this.getCurrentUser());
        	if(eobrSerialNbr != null)
        	{	                
                config = eobrConfigFacade.Fetch(eobrSerialNbr);
        	}
        	if (config == null)
        	{
                // no config found in the db for this eobr.   
                // In this case build an empty config and assign the serial number and tractor number to it
                // the defaults for the performance thresholds will be just fine
        		// Note: Since this is now stored in the handheld database and will be submitted to DMO, create default
        		// values for all fields so submission to DMO will work
                config = new EobrConfiguration();
                config.setSerialNumber(eobrSerialNbr);
                config.setTractorNumber(eobrTractorNbr);
                config.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1708));
                config.setDataCollectionRate(1);
                config.setDiscoveryPasskey("undefined");
                config.setFirmwareVersion("undefined");
                config.setSleepModeMinutes(-1);
        		
                if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()) != null)
                {
                	config.setTachometerThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMaxAcceptableTach());
                	config.setSpeedometerThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMaxAcceptableSpeed());                	
                	config.setHardBrakeThreshold(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getHardBrakeDecelerationSpeed());
                }
                
                // persist the new config into the database
                if(eobrSerialNbr != null) 
                	eobrConfigFacade.Save(eobrSerialNbr, config);
            }
            this.setCurrentEobrConfiguration(config);
        }
	}
	
	private boolean ShouldProcessEngineRecord(StatusRecord currentStatusRecord, int returnCode, int dataProfile)
	{
        boolean shouldProcess = false;
        Date nextTimestamp = getNextEngineRecordingTimestamp();
        if (currentStatusRecord == null) return false;

        if (returnCode == EobrReturnCode.S_SUCCESS)
        {
            if (currentStatusRecord.getIsEngineTelemetryAvailable())
            {
                if (nextTimestamp == null)
                {
                    // this is the very first record seen definitely process this one
                    shouldProcess = true;
                }
                else
                {
                    if (nextTimestamp.compareTo(currentStatusRecord.getTimestampUtc()) < 0)
                    {
                        // this means that the standard recording interval has elapsed
                        shouldProcess = true;
                    }

                    // do not check for "special" engine records for HOSWITHFUELTAXANDMAPPING profile
                    if (dataProfile != DataProfileEnum.HOSWITHFUELTAXANDMAPPING && !shouldProcess && this.DetermineRecordType(currentStatusRecord).getValue() != EngineRecordTypeEnum.NULL)
                    {
                        // this means that one of the "special" engine record scenarios has been detected
                        shouldProcess = true;
                    }
                }
            }
        }
        else
        {
            if (nextTimestamp != null)
            {
                // this is the very first record seen with an invalid return code
                currentStatusRecord.setOverallStatus(returnCode * -1);
                shouldProcess = true;
            }
        }

        return shouldProcess;
	}
	
    /// <summary>
    /// Determine the record type of the current status record, when
    /// compared to the previous one saved.
    /// If there are not special conditions that have occurred then
    /// answer EngineRecordTypeEnum.Null
    /// </summary>
    /// <param name="currentStatusRecord"></param>
    /// <returns></returns>
    private EngineRecordTypeEnum DetermineRecordType(StatusRecord currentStatusRecord)
    {
        EngineRecordTypeEnum answer = new EngineRecordTypeEnum(EngineRecordTypeEnum.NULL);
        EobrConfiguration config = getCurrentEobrConfiguration();

        StatusRecord previousStatusRecord = getPreviousEobrStatusRecord();
        if (previousStatusRecord != null)
        {
            if (previousStatusRecord.getIsEngineRunning() != currentStatusRecord.getIsEngineRunning())
            {
                // this means the engine just turned on, or off
            	answer.setValue(EngineRecordTypeEnum.ENGINERUNNINGCHANGE);
            	return answer;
            }

            if (previousStatusRecord.getIsEngineTelemetryAvailable() && previousStatusRecord.getIsVehicleMoving() != currentStatusRecord.getIsVehicleMoving())
            {
                // this means the vehicle just stopped, or started
            	answer.setValue(EngineRecordTypeEnum.VEHICLEMOVEMENTCHANGE);
                return answer;
            }

            if (previousStatusRecord.getIsVehicleMoving() && previousStatusRecord.getSpeedometerReading() >= 0 && currentStatusRecord.getSpeedometerReading() >= 0 &&  
                previousStatusRecord.getSpeedometerReading() - currentStatusRecord.getSpeedometerReading() > config.getHardBrakeThreshold())
            {
                // this means that the vehicle has a hard braking event
                // the vehicle was moving, and the 
                // decrease in speed is more than the Hard Braking Threshold
            	answer.setValue(EngineRecordTypeEnum.HARDBRAKEEVENT);
                return answer;
            }

            if (previousStatusRecord.getIsVehicleMoving() &&
                currentStatusRecord.getIsVehicleMoving())
            {
                // the vehicle was moving before, and is still moving
                boolean isPreviousSpeedUnder = previousStatusRecord.getSpeedometerReading() <= config.getSpeedometerThreshold();
                boolean isCurrentSpeedUnder = currentStatusRecord.getSpeedometerReading() <= config.getSpeedometerThreshold();
                if (isPreviousSpeedUnder != isCurrentSpeedUnder)
                {
                    // this means that the vehicle has crossed the speed threshhold
                    // either be accelerating, or decellerating
                	answer.setValue(EngineRecordTypeEnum.THRESHOLDEXCEEDED);
                    return answer;
                }

                boolean isPreviousTachUnder = previousStatusRecord.getTachometer() <= config.getTachometerThreshold();
                boolean isCurrentTachUnder = currentStatusRecord.getTachometer() <= config.getTachometerThreshold();
                if (isPreviousTachUnder != isCurrentTachUnder)
                {
                    // this means that the engine just crossed the tachometer threshhold, 
                    // either going up, or coming down
                	answer.setValue(EngineRecordTypeEnum.THRESHOLDEXCEEDED);
                    return answer;
                }
            }
        }

        return answer;
    }

    /// <summary>
    /// Calculate the next timestamp that an engine record should be recorded.
    /// If the return code is not successful, then the next recording timestamp
    /// will be null (DateTime.MinValue).
    /// </summary>
    /// <param name="eobrStatusRecord"></param>
    /// <param name="returnCode"></param>
    /// <returns></returns>
    private Date CalculateNextRecordingTime(StatusRecord eobrStatusRecord, int returnCode, int dataProfile)
    {
        Date nextTimestamp;        
        
        if (returnCode == EobrReturnCode.S_SUCCESS)
        {
            // on a successful read of the EOBR, the next engine record
            // to be recorded will be at least the "recordingInterval" in the future
            int recordingInterval = this.getRecordingInterval();
            
            // if data profile is HOSWITHFUELTAXANDMAPPING, set recording interval to 15 minutes
            if (dataProfile == DataProfileEnum.HOSWITHFUELTAXANDMAPPING)
            	recordingInterval = 900000;
            
            nextTimestamp = DateUtility.AddMilliseconds(eobrStatusRecord.getTimestampUtc(), recordingInterval);
        }
        else
        {
            // if the eobr returncode is not successful, then disable recording until a good record comes through
            nextTimestamp = null;
        }

        return nextTimestamp;
    }
    
    /// <summary>
    /// Loop through times between current eobr record and previous eobr record and check for
    /// hard brakes between each record.
    /// </summary>
    /// <param name="eobrStatusRecord">current eobr status record that we are processing</param>
    /// <param name="eobrSerialNbr">serial number of the eobr</param>
    /// <param name="eobrTractorNbr">tractor number assigned in the eobr</param>
    /// <param name="designatedDriver">employeeid of current driver</param>
    /// <param name="hardBrakeThreshold">deceleration threshold that defines a hard brake</param>
    /// <returns></returns>
    private void CheckForPreviousHardBrakes(StatusRecord eobrStatusRecord, String eobrSerialNbr, String eobrTractorNbr, User designatedDriver, float hardBrakeThreshold)
    {
        StatusRecord previousStatusRecord = this.getPreviousEobrStatusRecord();
        if(previousStatusRecord == null) return; //null;
        
        Calendar calNextTimestamp = Calendar.getInstance();
        calNextTimestamp.setTime(previousStatusRecord.getTimestampUtc());
        calNextTimestamp.set(Calendar.MILLISECOND, 0);  // clear any milliseconds so comparison works down to second
        calNextTimestamp.add(Calendar.SECOND, 1);

        Calendar calEobrStatusRecord = Calendar.getInstance();
        calEobrStatusRecord.setTime(eobrStatusRecord.getTimestampUtc());
        calEobrStatusRecord.set(Calendar.MILLISECOND, 0); // clear any milliseconds so comparison works down to second
        while (calNextTimestamp.compareTo(calEobrStatusRecord) < 0)
        {
            StatusRecord nextStatusRecord = new StatusRecord();
            int rc = EobrReader.getInstance().Technician_GetHistoricalData(nextStatusRecord, calNextTimestamp.getTime());

             if (rc == 0 && !nextStatusRecord.IsEmpty() && !previousStatusRecord.IsEmpty())
            {
                if (previousStatusRecord.getSpeedometerReading() >= 0 && nextStatusRecord.getSpeedometerReading() >= 0 && previousStatusRecord.getSpeedometerReading() - nextStatusRecord.getSpeedometerReading() > hardBrakeThreshold)
                {                	
                	// AAZ 1/31/13 Save Previous Status Record as Hard Brake Surrounding
                	// Removing +/- 10 Seconds events
                	// Decelaration will be calculated using this and nextStatusRecord
                	this.SaveEngineRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, previousStatusRecord,new EngineRecordTypeEnum(EngineRecordTypeEnum.HARDBRAKESURROUNDING));
                	
                    // save this record as a hard brake event
                    this.SaveEngineRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, nextStatusRecord, new EngineRecordTypeEnum(EngineRecordTypeEnum.HARDBRAKEEVENT));
                }
            }

            previousStatusRecord = nextStatusRecord;
            calNextTimestamp.add(Calendar.SECOND, 1);
        }

        this.setPreviousEobrStatusRecord(previousStatusRecord);

    }
    
    /// <summary>
    /// Process the new engine record.
    /// Save it to the local database.
    /// In the event that this is a hard-brake event, read and save
    /// the engine record that occurred a few seconds before the hard-brake
    /// </summary>
    /// <param name="tractorNumber">Tractor number corresponding to the unit being driven</param>
    /// <param name="newLocation">GPS position info</param>
    private EngineRecordTypeEnum ProcessEngineRecord(String eobrSerialNbr, String eobrTractorNbr, User designatedDriver, StatusRecord eobrStatusRecord, int dataProfile)
    {
        // determine the record type
    	// for HOSWITHFUELTAXANDMAPPING profile, only process standard records, do not process 
    	// hard brakes, engine on/off, vehicle start/stop, speed/tach over threshold
    	EngineRecordTypeEnum recType = EngineRecordTypeEnum.valueOfDMOEnum("Null");
    	if (dataProfile != DataProfileEnum.HOSWITHFUELTAXANDMAPPING)
    		recType = this.DetermineRecordType(eobrStatusRecord);
    	
        if (recType.getValue() == EngineRecordTypeEnum.NULL)
        {
            // no specific conditions have occurred, so just mark it standard
            recType = new EngineRecordTypeEnum(EngineRecordTypeEnum.STANDARD);
        }

        if (recType.getValue() == EngineRecordTypeEnum.HARDBRAKEEVENT)
        {
        	
        	// 1/13/13 aaz
        	// a hard-brake was detected
        	// get previous status record and save as HardBrakeSurrounding
        	// will be used to calcualte deceleration
        	this.SaveEngineRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, getPreviousEobrStatusRecord(),new EngineRecordTypeEnum(EngineRecordTypeEnum.HARDBRAKESURROUNDING));
        }
       
        this.SaveEngineRecord(eobrSerialNbr, eobrTractorNbr, designatedDriver, eobrStatusRecord, recType);

        return recType;
    }
    
    /// <summary>
    /// Save the engine record info to the database
    /// </summary>
    /// <param name="eobrSerialNbr"></param>
    /// <param name="eobrTractorNbr"></param>
    /// <param name="designatedDriver"></param>
    /// <param name="eobrStatusRecord"></param>
    /// <param name="recordType"></param>
    private void SaveEngineRecord(String eobrSerialNbr, String eobrTractorNbr, User designatedDriver, StatusRecord eobrStatusRecord, EngineRecordTypeEnum recordType)
    {
        EngineRecord engineRec = new EngineRecord();

        engineRec.setEobrSerialNumber(eobrSerialNbr);
        engineRec.setEobrTractorNumber(eobrTractorNbr);
        engineRec.setDriverEmployeeId(designatedDriver.getCredentials().getEmployeeId());
        engineRec.setEobrOverallStatus(eobrStatusRecord.getOverallStatus());
        engineRec.setEobrTimestamp(eobrStatusRecord.getTimestampUtc());
        engineRec.setSpeedometer(eobrStatusRecord.getSpeedometerReading());
        engineRec.setOdometer(eobrStatusRecord.getOdometerReading());
        engineRec.setTachometer(eobrStatusRecord.getTachometer());

        engineRec.setFuelEconomyAverage(eobrStatusRecord.getAverageFuelEconomy());
        engineRec.setFuelEconomyInstant(eobrStatusRecord.getInstantFuelEconomy());
        engineRec.setFuelUseTotal(eobrStatusRecord.getTotalFuelUsed());
        engineRec.setCruiseControlSet(eobrStatusRecord.getCruiseControlStatus() == 1);
        engineRec.setBrakePressure(eobrStatusRecord.getBrakePressure());
        engineRec.setTransmissionAttained(eobrStatusRecord.getTransmissionRangeAttained() == null ? null : eobrStatusRecord.getTransmissionRangeAttained().ToString());
        engineRec.setTransmissionSelected(eobrStatusRecord.getTransmissionRangeSelected() == null ? null : eobrStatusRecord.getTransmissionRangeSelected().ToString());

        engineRec.setRecordType(recordType);
        if (eobrStatusRecord.IsGpsLocationValid())
        {
            GpsLocation gpsLoc = new GpsLocation(eobrStatusRecord.getTimestampUtc(), eobrStatusRecord.getGpsLatitude(), eobrStatusRecord.getNorthSouthInd(), eobrStatusRecord.getGpsLongitude(), eobrStatusRecord.getEastWestInd());
            engineRec.setGpsTimestamp(gpsLoc.getTimestampUtc());
            engineRec.setGpsLatitude(gpsLoc.getLatitudeDegrees());
            engineRec.setGpsLongitude(gpsLoc.getLongitudeDegrees());
        }

        // save it to the DB
        EngineRecordFacade facade = new EngineRecordFacade(this.getContext(), this.getCurrentUser());
        facade.Save(engineRec);
    }
    
    public EngineRecord GetLastEngineRecord()
    {
        // the EOBR is connected, initialized, and online
        EobrReader eobr = EobrReader.getInstance();

        // fetch last eobr odometer from db
        EngineRecordFacade facade = new EngineRecordFacade(getContext(), this.getCurrentUser());
        return facade.FetchMostRecent(eobr.getEobrSerialNumber());

    }

    private void ProcessNewEventRecord(String eobrSerialNbr, String eobrTractorNbr, User designatedDriver,
			EmployeeLog driversLog, EventRecord eobrEventRecord, int returnCode)
    {
        if (this.getIsEnabled())
        {
        	this.CheckEobrConfiguration(eobrSerialNbr, eobrTractorNbr);

            // Determine if this Event record should be processed
            if(this.ShouldProcessEventRecord(eobrEventRecord, returnCode)){
            	this.ProcessEventRecord(eobrSerialNbr, designatedDriver, eobrEventRecord);

                // 01/08/13 AMO: If the event is either a Move or Stop event then submit the events to DMO
                // 01/10/13 AAZ: Add  MapPosition Event to check as well.  Allows Auto Refresh Map to update
                //               If on a long Highway trip, with no Move or Stops
            	if (eobrEventRecord.getEventType() == EventTypeEnum.MOVE
                    || eobrEventRecord.getEventType() == EventTypeEnum.VEHICLESTOPPED
                    || eobrEventRecord.getEventType() == EventTypeEnum.MAPPOSITION) {
                    // attempt to submit the event records to DMO
                    this.SubmitEventDataRecordsToDMOAsync();

                    // save the driver's log in case anything has changed
                    IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                    empLogCtrlr.SaveLocalEmployeeLog(designatedDriver, driversLog);
                }
            }

            // TODO Gen1 determined the next timestamp that should be processed

            // TODO Gen1 did SubmitEngineRecordsToDMO here

            // save the driver's log in case anything has changed
            IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogCtrlr.SaveLocalEmployeeLog(designatedDriver, driversLog);

            // save this record for later, so that the next time we get
            // a record in we can compare the one just prior
            setPreviousEobrEventRecord(eobrEventRecord);
        }
    }
    
	private boolean ShouldProcessEventRecord(EventRecord currentEventRecord, int returnCode) {
        boolean shouldProcess = false;
        if (currentEventRecord == null) return false;
        if (returnCode == EobrReturnCode.S_SUCCESS) {
            if (currentEventRecord.getEventType() == EventTypeEnum.IGNITIONON ||
                    currentEventRecord.getEventType() == EventTypeEnum.IGNITIONOFF ||
                    currentEventRecord.getEventType() == EventTypeEnum.MOVE ||
                    currentEventRecord.getEventType() == EventTypeEnum.VEHICLESTOPPED ||
                    currentEventRecord.getEventType() == EventTypeEnum.DRIVESTART ||
                    currentEventRecord.getEventType() == EventTypeEnum.DRIVEEND ||
                    currentEventRecord.getEventType() == EventTypeEnum.RPMOVERTHRESHOLD ||
                    currentEventRecord.getEventType() == EventTypeEnum.RPMUNDERTHRESHOLD ||
                    currentEventRecord.getEventType() == EventTypeEnum.SPEEDOVERTHRESHOLD ||
                    currentEventRecord.getEventType() == EventTypeEnum.SPEEDUNDERTHRESHOLD ||
                    currentEventRecord.getEventType() == EventTypeEnum.HARDBRAKE ||
                    currentEventRecord.getEventType() == EventTypeEnum.MAPPOSITION ||
                    currentEventRecord.getEventType() == EventTypeEnum.ERROR) {
                shouldProcess = true;
            }
        }
        return shouldProcess;
	}

    private boolean ShouldSubmitEventRecord(EventRecord currentEventRecord, int dataProfile) {
        boolean shouldSubmit = false;
        if (currentEventRecord == null) return false;

        // Determine whether to submit the event record based on the dataProfile and the EventType (shouldSubmit = false by default above)
        // MINIMUMHOS & MINIMUMHOSWITHFUELTAX should never be submitted
        switch(dataProfile) {
            case DataProfileEnum.MINIMUMHOSWITHGPS:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAXANDGPS:
                if(currentEventRecord.getEventType() == EventTypeEnum.MAPPOSITION){
                    shouldSubmit = true;
                }
                break;
            case DataProfileEnum.HOSWITHFUELTAXANDMAPPING:
            case DataProfileEnum.FULL:
            case DataProfileEnum.FULLWITHGEOFENCE:
                if(currentEventRecord.getEventType() == EventTypeEnum.IGNITIONON ||
                        currentEventRecord.getEventType() == EventTypeEnum.IGNITIONOFF ||
                        currentEventRecord.getEventType() == EventTypeEnum.MOVE ||
                        currentEventRecord.getEventType() == EventTypeEnum.VEHICLESTOPPED ||
                        currentEventRecord.getEventType() == EventTypeEnum.DRIVESTART ||
                        currentEventRecord.getEventType() == EventTypeEnum.DRIVEEND ||
                        currentEventRecord.getEventType() == EventTypeEnum.RPMOVERTHRESHOLD ||
                        currentEventRecord.getEventType() == EventTypeEnum.RPMUNDERTHRESHOLD ||
                        currentEventRecord.getEventType() == EventTypeEnum.SPEEDOVERTHRESHOLD ||
                        currentEventRecord.getEventType() == EventTypeEnum.SPEEDUNDERTHRESHOLD ||
                        currentEventRecord.getEventType() == EventTypeEnum.HARDBRAKE ||
                        currentEventRecord.getEventType() == EventTypeEnum.MAPPOSITION) {
                    shouldSubmit = true;
                }
                break;
            default:
                break;
        }

        return shouldSubmit;
    }
	
    /// <summary>
    /// Process the new Event record.
    /// Save it to the local database.
    /// </summary>
    /// <param name="eobrSerialNbr">Serial Number for the current EOBR</param>
    /// <param name="designatedDriver">Current designated driver</param>
	/// <param name="eobrEventRecord">Event Record</param>
    private void ProcessEventRecord(String eobrSerialNbr, User designatedDriver, EventRecord eobrEventRecord)
    {
    	// 12/26/12 AMO: This is where Gen2 events get saved to the DB
        this.SaveEventRecord(eobrSerialNbr, designatedDriver, eobrEventRecord);
    }
    
    
	private void SaveEventRecord(String eobrSerialNbr, User designatedDriver, EventRecord eobrEventRecord) {
		// 12/26/12 AMO: Save event data to the local DB
		EventDataRecord eventDataRecord = new EventDataRecord();
        int dataProfile = this.getCurrentDesignatedDriver().getDataProfile().getValue();

		eventDataRecord.setDriverEmployeeId(designatedDriver.getCredentials().getEmployeeId());
		eventDataRecord.setEobrSerialNumber(eobrSerialNbr);
		eventDataRecord.setEobrTimestamp(eobrEventRecord.getTimecodeAsDate());
		eventDataRecord.setEventType(eobrEventRecord.getEventType());
		
		StatusRecord statusRecord = eobrEventRecord.getStatusRecordData();
		
		//only set the speedo and tach under certain circumstances
		if(ShouldCollectEngineData() && statusRecord != null)
		{
			eventDataRecord.setSpeedometer(statusRecord.getSpeedometerReading());
			eventDataRecord.setTachometer(statusRecord.getTachometer());
		} else
		{
			eventDataRecord.setSpeedometer(-1f);
			eventDataRecord.setTachometer(-1f);
		}

		// If the event is a hard brake the deceleration value should be converted to MPH instead of KPH
		if(eobrEventRecord.getEventType() == EventTypeEnum.HARDBRAKE) {
			float deceleration = ((eobrEventRecord.getEventData() / 100) * KPH_TO_MPH);
			eventDataRecord.setEventData(Math.round(deceleration));
		}
		else
			eventDataRecord.setEventData(eobrEventRecord.getEventData());
		
		// Trip Information will be available on DRIVE_START/END
		TripReport trip = eobrEventRecord.getTripReportData();
		if (trip != null) {
            eventDataRecord.setGpsLatitude(trip.getLatitude());
            eventDataRecord.setGpsLongitude(trip.getLongitude());
			eventDataRecord.setOdometer(trip.getOdometer());
		} else 
		{
			//all events should now have a status record on them
			if(statusRecord != null)
			{
                eventDataRecord.setGpsLatitude(statusRecord.getGpsLatitude());
                eventDataRecord.setGpsLongitude(statusRecord.getGpsLongitude());
				eventDataRecord.setOdometer(statusRecord.getOdometerReading());
			}
			else
				Log.w("eventSave", "Event did not have a status record.");		
		}
		
		// if both Lat/Long are Zero assume invalid, don't want to send them to DMO
		if (eventDataRecord.getGpsLatitude() == 0 && eventDataRecord.getGpsLongitude() == 0)
		{
			eventDataRecord.setGpsLatitude(Float.NaN);
			eventDataRecord.setGpsLongitude(Float.NaN);
		}

        // Per PBI 44939 - We do not want to trace odometer, speedometer, and tachometer
		if (dataProfile == DataProfileEnum.MINIMUMHOSWITHFUELTAXANDGPS || dataProfile == DataProfileEnum.MINIMUMHOSWITHGPS) {
            eventDataRecord.setOdometer(-1f);
            eventDataRecord.setSpeedometer(-1f);
            eventDataRecord.setTachometer(-1f);
        }
		
		EventDataRecordFacade eventDataRecordFacade = new EventDataRecordFacade(this.getContext(), this.getCurrentUser());
		eventDataRecordFacade.Save(eventDataRecord);

        // Only certain data profiles and event types should be submitted to Encompass (otherwise we will immediately mark the records submitted on the local device).
        // We are holding onto all if it locally in KMB to better facilitate troubleshooting issues in support using this data if needed.
        if (!ShouldSubmitEventRecord(eobrEventRecord, dataProfile)){
            ArrayList<EventDataRecord> list = new ArrayList<EventDataRecord>();
            list.add(eventDataRecord);
            eventDataRecordFacade.MarkAsSubmitted(list);
        }
	}
    
    //answers if we should collect speedo/tach data
    //given the EOBR's current bus type
    private boolean ShouldCollectEngineData()
    {
    	boolean answer = false;
    	
    	DatabusTypeEnum currentDatabusType = EobrReader.getInstance().getEobrDatabusType();
    	
    	switch(currentDatabusType.getValue())
    	{
	    	case DatabusTypeEnum.J1708:
	    	case DatabusTypeEnum.J1939:
	    	case DatabusTypeEnum.DUALMODEJ1708J1939:
            case DatabusTypeEnum.J1939F:
            case DatabusTypeEnum.DUALMODEJ1708J1939F:
	    		answer = true;
	    		break;
    		
    		default:
    			answer = false;
    			break;
    	}
    	
    	return answer;
    }
	
    private static class SubmitEngineRecordsTask extends AsyncTask<Void, Void, Boolean>
    {
    	private final WeakReference<Context> context;
    	
    	public SubmitEngineRecordsTask(final Context context)
    	{
    		this.context = new WeakReference<Context>(context);
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean isSuccesful = false;
			try
			{
				isSuccesful = new EngineRecordController(context.get(), GlobalState.getInstance().getAppSettings(context.get())).SubmitEngineRecordsToDMO();
			}
			finally
			{
				_isSubmittingEngineRecords.set(false);
			}
			return isSuccesful;
		}
    }


    
    /// <summary>
    /// Submit all engine records that need to be sent up to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    public boolean SubmitEventDataRecordsToDMO()
    {
        boolean isSuccesful = false; 

        if (this.getIsWebServicesAvailable())
        {
            try
            {
                WebAPIServiceHelperFactory apiServiceHelperFactory = new WebAPIServiceHelperFactory();
                IWebAPIServiceHelper apiHelper = apiServiceHelperFactory.getInstance(getContext());


                // first fetch unsubmitted engine records, but only fetch at most a limited number to avoid 
                // issues where large number of unsubmitted records exist
                EventDataRecordFacade facade = new EventDataRecordFacade(this.getContext(), this.getCurrentUser());                
                List<EventDataRecord> unSubmittedRecords = facade.FetchUnsubmittedLimited(2000);

                // are there any to send? 
                if (unSubmittedRecords != null && unSubmittedRecords.size() > 0)
                {
                    // build the EventDataRecordList for each unique EOBR/Driver combination found it the unsubmitted list
                    String lastSerialNumber = null;
                    String lastDriverId = null;
                    EventDataRecordList listToSend = null;
                    ArrayList<EventDataRecord> recordList = null;
                    int numToSend = 250;
 
					for (EventDataRecord rec : unSubmittedRecords) {
						if (!rec.getDriverEmployeeId().equalsIgnoreCase(lastDriverId) || !rec.getEobrSerialNumber().equalsIgnoreCase(lastSerialNumber)) {
							// a record with either a new driver, or EOBR is discovered
							if (listToSend != null && recordList != null && recordList.size() > 0) {
								// there is a previous list (from a different driver/eobr) to send to DMO
								listToSend.setEventRecords(recordList.toArray(new EventDataRecord[recordList.size()]));

								try {
									apiHelper.SubmitEventDataRecords(listToSend);

									// mark the list just sent to DMO as submitted
									facade.MarkAsSubmitted(recordList);
									
								} catch (JsonSyntaxException e) {
									this.HandleException(e, this.getContext().getString(R.string.submiteventdatarecordstodmo));
								} catch (IOException e) {
									this.HandleException(e, this.getContext().getString(R.string.submiteventdatarecordstodmo));
								}
							}

							// start a new list to hold the new stuff for this driver/EOBR
							listToSend = new EventDataRecordList(null);
							listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
							listToSend.setDriverEmployeeId(rec.getDriverEmployeeId());

							recordList = new ArrayList<EventDataRecord>();
							lastDriverId = rec.getDriverEmployeeId();
							lastSerialNumber = rec.getEobrSerialNumber();
						}
                        
						recordList.add(rec);

						// Have enough records been collected to send?
						if (recordList.size() >= numToSend) {
							// yes, so send this batch of records to DMO
							listToSend.setEventRecords(recordList.toArray(new EventDataRecord[recordList.size()]));

							try {
								apiHelper.SubmitEventDataRecords(listToSend);

								// mark the list just sent to DMO as submitted
								facade.MarkAsSubmitted(recordList);
							} catch (JsonSyntaxException e) {
								this.HandleException(e, this.getContext().getString(R.string.submiteventdatarecordstodmo));
							} catch (IOException e) {
								this.HandleException(e, this.getContext().getString(R.string.submiteventdatarecordstodmo));
							}

							// start a new list to hold the new stuff for this driver/EOBR
							listToSend = new EventDataRecordList(null);
							listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
							listToSend.setDriverEmployeeId(rec.getDriverEmployeeId());

							recordList = new ArrayList<EventDataRecord>();
						}
                    }

					if (listToSend != null && recordList != null && recordList.size() > 0) {
						// there are records to send to DMO
						listToSend.setEventRecords(recordList.toArray(new EventDataRecord[recordList.size()]));

						try {
							apiHelper.SubmitEventDataRecords(listToSend);

							// mark those just sent, as submitted
							facade.MarkAsSubmitted(recordList);
							
						} catch (JsonSyntaxException e) {
							this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));
						} catch (IOException e) {
							this.HandleException(e, this.getContext().getString(R.string.submitenginerecordstodmo));
						}
					}
				}

				isSuccesful = true;
			} catch (Exception excp) {
				this.HandleException(excp, this.getContext().getString(R.string.submitenginerecordstodmo));
			}
		}

		return isSuccesful;
	}

    /**
     * Asynchronously submit all engine records that need to be sent up to DMO
     */
    public void SubmitEventDataRecordsToDMOAsync()
    {
    	if (_isSubmittingEventDataRecords.compareAndSet(false, true))
    	{
    		try
			{
				new SubmitEventDataRecordsTask(getContext()).execute();
			}
			catch (Exception ex)
			{
	        	Log.e("UnhandledCatch", ex.getMessage(), ex);
				_isSubmittingEventDataRecords.set(false);
			}
    	}
    }
    
    private static class SubmitEventDataRecordsTask extends AsyncTask<Void, Void, Boolean>
    {
    	private final WeakReference<Context> context;
    	
    	public SubmitEventDataRecordsTask(final Context context)
    	{
    		this.context = new WeakReference<Context>(context);
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean isSuccesful = false;
			try
			{
				isSuccesful = new EngineRecordController(context.get(), GlobalState.getInstance().getAppSettings(context.get())).SubmitEventDataRecordsToDMO();
			}
			finally
			{
				_isSubmittingEventDataRecords.set(false);
			}
			return isSuccesful;
		}
    }


}
