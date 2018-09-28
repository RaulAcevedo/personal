package com.jjkeller.kmb;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AutoAssignUnassignedEventsController;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.AutoAssignedELDCalls;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpgraderFactory;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.RouteController;
import com.jjkeller.kmbapi.controller.UnassignedPeriodController;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IBluetoothDrivingManager;
import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.eobrengine.EobrServiceBase;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.geotabengine.GeotabConstants;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.Enums.DeviceErrorFlags;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrServiceMessages;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.HistogramData;
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.IApplicationUpdateListener;
import com.jjkeller.kmbapi.kmbeobr.IDataFlag;
import com.jjkeller.kmbapi.kmbeobr.JbusDiagnosticData;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;
import com.jjkeller.kmbui.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EobrService extends EobrServiceBase {
	
    Messenger _replyToClient;
	private boolean _needToUpdateFirmwareOnNextTimerPop;
	private boolean _isFinishingFirmwareUpdate;
	private boolean _needToReadHistoryOnNextTimerPop;
	private volatile boolean _ignoreResume = false;
	private volatile boolean _timerSuspended = false; 
	private volatile boolean _timerCreated = false;
    private Handler _timerHandler = new Handler();
    private volatile boolean _isReadingHistory = false;
    private boolean _ignoreNextDefaultDriverEvent = false;
	private WakeLock _wl;
	private PowerManager _powerMgr;
	private IApplicationUpdateListener _applicationUpdateListener;
	private IBluetoothDrivingManager _bluetoothDrivingManager;

    /// <summary>
    /// This is the timestamp that device errors began to be reported from 
    /// the EOBR.    Any device error that gets reported, this timestamp will be
    /// the UTC timestamp of the status record which reported the failure.
    /// </summary>
    private Date _eobrFailureStartTimestamp = null;

    /// <summary>
    /// The amount of time that determines if short-term failures will be
    /// ignored from both history and current status processing. 
    /// When failures occur for shorter than this value, the failure will be ignored.
    /// This is in seconds.
    /// </summary>
    private static final int EOBR_FAILURE_IGNORE_DURATION_SECONDS = 60;
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
    	public EobrService getService() {
            return EobrService.this;
        }
        public IBinder getMessengerBinder()
        {
    		return mMessenger.getBinder();
        }
    }
    
    @Override
    public void onCreate() {
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
    	try
    	{
	    	// reset thresholds
	        CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());
            EobrConfigController configController = new EobrConfigController(this.getContext());
            int eventBlanking = 10;
            String defaultDriverId = "";
            configController.SetThresholdValues(
                    this.getContext(),
                    companyConfigSettings.getMaxAcceptableTach(),
                    companyConfigSettings.getMaxAcceptableSpeed(),
                    companyConfigSettings.getHardBrakeDecelerationSpeed(),
                    companyConfigSettings.getDriverStartDistance(),
                    companyConfigSettings.getDriverStopMinutes(),
                    eventBlanking,
                    defaultDriverId,
                    companyConfigSettings.getMandateDrivingStopTimeMinutes(),
                    companyConfigSettings.getDriveStartSpeed()
            );

            // shutdown eobr
            this.Shutdown();
    	}
    	finally
    	{
    		// stop service
    		this.stopForeground(true);
    	
    		GlobalState.getInstance().tearDown();
    	}
    	
    	super.onTaskRemoved(rootIntent);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("EobrService", "Received start id " + startId + ": " + intent);
        if(intent != null)
        {
			// initially we need to verify the EOBR before any other processing
	        _needToUpdateFirmwareOnNextTimerPop = true;
	
			_powerMgr = (PowerManager)GlobalState.getInstance().getSystemService(GlobalState.POWER_SERVICE);
			_wl = _powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EobrServiceWakeLock");
			if (!_wl.isHeld())
				_wl.acquire();

	        // 6/15/12 JHM - Added call to startForeground to increase priority of the application and therefore 
	        // less likely to get killed by OS for memory reasons.
			startForeground(NotificationUtilities.APPRUNNING_ID, NotificationUtilities.GetAppRunningNotification(getApplicationContext(), StartupActivity.class, "Application is running."));        
	
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_STICKY;
        }
        else
        {
        	// 9/19/12 JHM - Without an intent, this probably represent a failure we should ignore.
			stopSelf();
			return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
    	stopForeground(true);
		_timerHandler.removeCallbacksAndMessages(null);
		
		if (_wl != null && _wl.isHeld())
			_wl.release();
		
    	//_timerHandler.removeCallbacks(mMainTask);
    	//_timerHandler.removeCallbacks(mMainTask_Gen2);
    }

    @Override
    public IBinder onBind(Intent intent) {
    	try {
            Log.v("EobrService", "onBind");
			this.InitiatePeriodicReading();
            MalfunctionManager.getInstance().startScheduledProcessesForMalfunctions();
		} catch (KmbApplicationException kae) {
			ErrorLogHelper.RecordException(this, kae);
		}
        return mBinder;
    }

    private Runnable mMainTask = new Runnable()
    {
        public void run() 
        {
        	Log.v("EobrService", String.format("mMainTask.run() reply: %s timerSuspend: %s", _replyToClient, _timerSuspended));
        	if(_replyToClient != null && !_timerSuspended)
        	{        		
				_timerHandler.removeCallbacksAndMessages(null);
				
				ProcessTimerTask timerTask = new ProcessTimerTask();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					timerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					timerTask.execute();
        	}
        }
    };
    
    private class ProcessTimerTask extends AsyncTask<Void, Void, Void> {
    	protected Void doInBackground(Void... params) {
           ProcessTimerPop();
    		return null;
    	}

    	protected void onPostExecute(Void unused) {
            _timerHandler.postDelayed(mMainTask, 1000);
    	}
	}
        
    /// <summary>
    /// Process the EOBR reader time pop.
    /// 
    /// When the device is currently ONLINE, then read and process the
    /// current status record.    If the device is currently OFFLINE,
    /// test if the device is connected now.  If so, then read and 
    /// process the historical records off the device from the last 
    /// time a successful read occurred.
    /// </summary>
    /// <param name="state"></param>
    private void ProcessTimerPop()
    {    	
        try
        {
        	EobrReader eobrReader = EobrReader.getInstance();
        	Log.v("EobrService", String.format("ProcessTimerPop state: %s readHistory: %s", eobrReader.getCurrentConnectionState(), _needToReadHistoryOnNextTimerPop));

        	switch (eobrReader.getCurrentConnectionState())
            {
                case ONLINE:
                    if (_needToUpdateFirmwareOnNextTimerPop)
                    {
                   		// firmware needs to be updated
                   		_needToUpdateFirmwareOnNextTimerPop = false;
                   		this.DownloadFirmwareUpdate(eobrReader);
                    }
                    else
                    {
                        // currently the EOBR is ONLINE
                        // no firmware update needs to be applied, we should be good to go
                    	
                    	if (_isFinishingFirmwareUpdate)
                    		notifyFirmwareUpdateFinished(true);
                    	
                        if (_needToReadHistoryOnNextTimerPop)
                        {
                        	// 6/8/11 JHM - Prevent additional timer pops while we're processing history.
                        	// 10/20/11 SJN - moved inside ReadAndPublish...
                        	//this.SuspendReading();

                        	// the EOBR has become connected now
                            this.ReadAndPublishHistoricalStatusRecords();
                            _needToReadHistoryOnNextTimerPop = false;
                        }
                        else
                        { 
                        	//2013.09.30 sjn - added this to ensure that if network time has adjusted the clocks since the last time, the clock will be synced correctly
                        	eobrReader.VerifyClockConsistency(this);
                        	
                            this.ReadAndPublishCurrentStatusRecord(null);
                            // wait until the next timer pop to start reading again
                        }
                    }
                    break;

                case SHUTDOWN:
                    // ignore the shutdown EOBR, which comes from the user 'releasing' the connection
                    break;

                default:
                    // EOBR is currently OFFLINE
                	eobrReader.VerifyClockConsistency(this);

	                boolean checkForEobr = true;
	                if (eobrReader.getClockCheckTimestamp() != null && eobrReader.getEobrOfflineTimestamp() != null)
	                {
	                    // as long as the EOBR has been offline for approx 1 minute, 
	                    // then allow it to check for the presence of the EOBR                    	
	                    long elapsedTimeOffline = eobrReader.getClockCheckTimestamp().getTime() - eobrReader.getEobrOfflineTimestamp().getTime();
	                    checkForEobr = (elapsedTimeOffline/1000) > 50;
	                }
		
	                // see if the EOBR has become connected again
	                if (checkForEobr && eobrReader.IsDevicePhysicallyConnected(this, true))
	                {
                		StatusRecord currentStatus = this.ReadAndPublishHistoricalStatusRecords();
                		eobrReader.setEobrOfflineTimestamp(null);
                		if( eobrReader.getCurrentConnectionState() == ConnectionState.ONLINE )
                			this.ReadAndPublishCurrentStatusRecord(currentStatus);
	                }
	                
                    break;
            }

            // queue up another timer pop, as long as not in failure state, or shutdown
            // this design pattern was chosen because long running processing
            // of the timer pop (like reading historial records) will not
            // cause the timers to "backup"
            if (eobrReader.getCurrentConnectionState() != ConnectionState.DEVICEFAILURE && 
            		eobrReader.getCurrentConnectionState() != ConnectionState.SHUTDOWN)
            {
            	// 7/8/11 JHM - This seemed problematic because it was restarting an explicitly paused timer.
            	// Commenting out until such a point that we determine a definite need.
                //this.ResumeReading();
            }

            // save the current time so we can tell if we've timed out
        	GlobalState.getInstance().setLastTimerPop(DateUtility.getCurrentDateTimeWithSecondsUTC());
        }
        catch (Throwable excp)
        {
            ErrorLogHelper.RecordException(this, excp);
            if(excp.getCause() != null)
            {
            	ErrorLogHelper.RecordMessage(this, "Cause of unhandled error");
            	ErrorLogHelper.RecordException(this, excp.getCause());
            }
            excp.printStackTrace();
        	
        	if (_isFinishingFirmwareUpdate)
        		notifyFirmwareUpdateFinished(false);
        	
            EobrReader.getInstance().TransitionDeviceToNewState(this, ConnectionState.DEVICEFAILURE, excp.getMessage());
        }

    }

    /// <summary>
    /// Fetch and process the historical records that may be stored
    /// in the EOBR device from the last time a successful read from the device occurred.
    /// If something unexpected occurs, then the device will be marked as 'OFFLINE', 
    /// and null will be returned.
    /// If a list of history records is generated, they will be published to the UI.
    /// When everything works OK, the device will be marked as 'ONLINE' upon completion.
    /// </summary>
    private StatusRecord ReadAndPublishHistoricalStatusRecords()
    {
    	Log.v("EobrService", "About to read historical records");
    	// SJN 10/20/11 suspend processing while reading history
    	this.SuspendReading();
    	
        StatusRecord lastRecord = null;        
        try{
	        ArrayList<StatusRecord> historyList = this.ReadHistoricalStatusRecords();
	
	        // publish the list of historical records to the controller
	        EobrReader.getInstance().PublishEobrHistoricalRecords(historyList);
		
	        if (historyList != null && historyList.size() > 0)
	        {
		        // history was completely read and processed from the EOBR
		        // go back online with the connection
		     	EobrReader.getInstance().TransitionDeviceToNewState(this, ConnectionState.ONLINE, "ReadAndPublishHistoricalStatusRecords: back online");
		        Log.d("EobrService", String.format(getString(R.string.msgeobrservice_readhistorycomplete), historyList == null ? 0 : historyList.size()));

	            // grab the last record so that it can be returned to the caller
	            lastRecord = historyList.get(historyList.size() - 1);
	        }
	        else
	        	lastRecord = null;
	
	        // reset the local flag
	        _needToReadHistoryOnNextTimerPop = false;

    	}
        finally{
	    	// 6/8/11 JHM - Resume processing of records normally now that reading of history is done.
        	// 10/20/11 SJN - always make sure to resume as finally action, but only if we're not in a failure state
        	if(EobrReader.getInstance().getCurrentConnectionState() != ConnectionState.DEVICEFAILURE)
        	{
        		this.ResumeReading();

        		// 2/20/12 JHM - Clear app restart flag after we are finished read history.
        		GlobalState.getInstance().setAppRestartFlag(false);
        	}
        }
        
        return lastRecord;
    }
    
    /// <summary>
    /// Read the current status record from the EOBR and publish to the UI.
    /// If the current status record represents a device error, certain types
    /// of device errors are ignored.
    /// As part of this process, the clock on the KellerMobile device and
    /// the EOBR are compared.   The KellerMobile device clock may be changed as
    /// a result of this.
    /// </summary>
    private void ReadAndPublishCurrentStatusRecord(StatusRecord currentStatus)
    {
        int rc = EobrReturnCode.S_SUCCESS;
        EobrReader eobrReader = EobrReader.getInstance();
        if (currentStatus == null)
        {
            currentStatus = new StatusRecord();
            rc = eobrReader.Technician_GetCurrentData(currentStatus, true);            
        }

        if (rc == EobrReturnCode.S_SUCCESS)
        {
            // successful read from the EOBR
            if (!currentStatus.IsEmpty())
            {
            	// 2013.09.30 sjn - The EOBR clock is not used for time sync anymore.  The clock sync feature is tied to DMO time now.
            	//                  This call is being removed because it will calibrate the soft clock of DateUtilities to the eobr instead of DMO, which is not what we want anymore 
            	//eobrReader.VerifyClockConsistency(this, currentStatus.getTimestampUtc());

                // speedometer failure isn't getting returned from EOBR.  Speedometer value
                // of 127.5 (on 1708 bus) indicates a speedometer failure - if speed is 127.5
                // turn on speedometer failure device error flag
                if (currentStatus.getSpeedometerReading() == 127.5F)
                    currentStatus.setOverallStatus(DeviceErrorFlags.Speedometer);

                // verify that the current status record should be processed
                // specific short-term device errors are ignored
                // also verify that the odometer is valid compared to last odometer
                boolean ignoreCurrentStatus = this.ShouldIgnoreEobrStatusRecord(currentStatus, true);
                if (!ignoreCurrentStatus)
                {
                    eobrReader.PublishEobrStatusChange(rc, currentStatus);

                    // only update the last eobr odometer values if they have been previously set
                    // if they weren't previously set, we don't know for sure that the current
                    // odometer is valid - we don't want to set to an invalid value or we will ignore
                    // valid values
                    if (currentStatus.getOdometerReading() > 0 && eobrReader.getLastEobrOdometerUTCTime() != null)
                    {
                        eobrReader.setLastEobrOdometer(currentStatus.getOdometerReading());
                        eobrReader.setLastEobrOdometerUTCTime(currentStatus.getTimestampUtc());
                    }
                }
            }
        }
        else
        {
            // read failed, or some other problem, so go offline
        	eobrReader.TransitionDeviceToNewState(this, ConnectionState.OFFLINE, String.format("ReadAndPublishCurrentStatusRecord: GetCurrentData() bad return code '%s'", rc));

            // publish the failure, along with the timestamp of the failure
            //currentStatus.setTimestampUtc(this.ReadClockUniversalTime());
            eobrReader.PublishEobrStatusChange(rc, currentStatus);
        }
    }
    
    /// <summary>
    /// Answer if this status record should be ignored.
    /// Certain types of device error are being ignored, when they occur
    /// for a very short duration.
    /// The following device errors are recognized: JBus (0x4), Internal (0x80)
    /// and the combination of the two.  When these errors occur for a short
    /// duration (EOBR_FAILURE_IGNORE_DURATION_SECONDS), they will be ignored
    /// 9/24/2010 - TCH - the device statuses identified above will no longer
    /// cause a device failure.  Therefore, they no longer need to be ignored.
    /// This method now just needs to check if the odometer is valid
    /// </summary>
    /// <param name="statusRec"></param>
    /// <returns></returns>
    private boolean ShouldIgnoreEobrStatusRecord(StatusRecord statusRec, boolean checkOdometer)
    {
        boolean ignore = false;

        if (statusRec != null && !statusRec.IsEmpty())
        {
            // has any failure been detected
            if (statusRec.AnyDeviceFailuresDetected())
            {
                if (_eobrFailureStartTimestamp == null)
                {
                    // this is the first failure detected
                    _eobrFailureStartTimestamp = statusRec.getTimestampUtc();
                }

                switch (statusRec.getOverallStatus())
                {
                    case DeviceErrorFlags.Speedometer:
                        // 0x200 device status

                        // how long has the error been occurring?
                        // if the error has been occurring for less than the desired duration, then ignore this status report
                        long duration = statusRec.getTimestampUtc().getTime() - _eobrFailureStartTimestamp.getTime();
                        if (duration < EOBR_FAILURE_IGNORE_DURATION_SECONDS)
                        {
                            ignore = true;
                        }
                        break;

                    default:
                        // all other failures should be processed fully
                        ignore = false;
                        break;
                }
            }
            else
            {
                // no failures reported, so reset the failure start timestamp
                _eobrFailureStartTimestamp = null;
            }


            EobrReader eobrReader = EobrReader.getInstance();
            // if we aren't ignoring, verify the odometer is valid - greater than last
            // odometer reading and less than last odometer reading + number of days since
            // last reading times a maximum mph value.  
            if (!ignore && checkOdometer)
            {
                if (statusRec.getOdometerReading() > 0 && eobrReader.getLastEobrOdometer() > 0.0F && eobrReader.getLastEobrOdometerUTCTime() != null)
                {
                    // Calculate the milage that could have been traveled between the time of the last eobr odometer
                    // reading and the current status record (Note:  Use 85 MPH @ 24 hours).
                    // Since these records are processed every second the calculated odometer difference will not be 
                    // very large.  Set a minimum difference of 2 miles to handle cases when speed is greater
                    // than 85 MPH.
                	// Get difference between the timestamps and get days by dividing by 86400000 (1000*60*60*24)
                	float totalDays = (float)(statusRec.getTimestampUtc().getTime() - eobrReader.getLastEobrOdometerUTCTime().getTime()) / 86400000;
                    float diff = totalDays * 2040;

                    if (diff < 2.0F)
                        diff = 2.0F;

                    float startRange = eobrReader.getLastEobrOdometer();
                    float endRange = eobrReader.getLastEobrOdometer() + diff;

                    if (statusRec.getOdometerReading() < startRange || statusRec.getOdometerReading() > endRange)
                    {
                        ignore = true;
                        ErrorLogHelper.RecordMessage(this, String.format("ReadAndPublishCurrentStatusRecord:  Status record ignored because of invalid odometer.  Current odometer:  {%f},{%s};  Last Odometer:  {%f},{%s}", statusRec.getOdometerReading(), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(statusRec.getTimestampUtc()), eobrReader.getLastEobrOdometer(), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(eobrReader.getLastEobrOdometerUTCTime())));
                    }
                }
            }

            if (!ignore && statusRec.getSpeedometerReading() > 300)
            {
                ignore = true;
                ErrorLogHelper.RecordMessage(this, String.format("ReadAndPublishCurrentStatusRecord:  Status record ignored because of invalid speedometer ({%f}).", statusRec.getSpeedometerReading()));
            }
        }

        return ignore;
    }

    private void CreateTimer()
    {
        Log.v("EobrService", String.format("CreateTimer timerCreated: %s timerSuspended: %s",
                _timerCreated,
                _timerSuspended
        ));

        if (!_timerCreated || _timerSuspended)
		{
			if (EobrReader.getInstance() != null)
			{
				synchronized (_timerHandler)
				{
					_timerHandler.removeCallbacksAndMessages(null);
					if (EobrReader.getInstance().isEobrGen1())
					{
						// _timerHandler.removeCallbacks(mMainTask);
						_timerHandler.postDelayed(mMainTask, 1000);
					}
					else
					{
						// _timerHandler.removeCallbacks(mMainTask_Gen2);
						_timerHandler.postDelayed(mMainTask_Gen2, 1000);
                        Log.v("EobrService", "CreateTimer TimerCreated");
					}
				}

				_timerSuspended = false;
				_timerCreated = true;
			}
            else
            {
                Log.v("EobrService", "CreateTimer EobrReader instance not found");
            }
		}
        else{
            Log.v("EobrService", String.format("CreateTimer Skipped _timerCreated: %s _timerSuspended: %s", _timerCreated, _timerSuspended));
        }
    }

    private void InitiatePeriodicReading() throws KmbApplicationException
    {
        boolean isDevicePhysicallyConnected = EobrReader.getInstance().IsDevicePhysicallyConnected(this, false);
        Log.v("EobrService", String.format("InitiatePeriodicReading isDevicePhysicallyConnected: %s", isDevicePhysicallyConnected));

        // first, read and process historical records before going online
        if (isDevicePhysicallyConnected)
        {
            // the EOBR is already connected
            // pull the history off the EOBR and publish to the UI when
            // the timer starts popping
            _needToReadHistoryOnNextTimerPop = true;
        }

        // Start a timer that will periodically pop
    	CreateTimer();
	}
    
    
    private void ReadHistoryOnTimerPop()
    {
		try {
            boolean isDevicePhysicallyConnected = EobrReader.getInstance().IsDevicePhysicallyConnected(this, false);
            Log.v("EobrService", String.format("ReadHistoryOnTimerPop isDevicePhysicallyConnected: %s", isDevicePhysicallyConnected));

			// first, read and process historical records before going online
			if (isDevicePhysicallyConnected) {
				// the EOBR is already connected
				// pull the history off the EOBR and publish to the UI when
				// the timer starts popping
				_needToReadHistoryOnNextTimerPop = true;
			}
		} catch (KmbApplicationException kae) {
			ErrorLogHelper.RecordException(this, kae);
		}

	}
    
    /// <summary>
    /// Permanently suspend the periodic read timer.  
    /// This effectively disables the periodic reading of the EOBR.
    /// In order to initiate reading again, call ResumeReading().
    /// </summary>
    public void SuspendReading()
    {
        // suspend the timer, indefinitely
    	Log.v("EobrService", String.format("SuspendReading timerCreated: %s timerSuspended: %s", _timerCreated, _timerSuspended));
    	if(_timerCreated)
        {
    		_timerSuspended = true;
			_timerHandler.removeCallbacksAndMessages(null);
    		//_timerHandler.removeCallbacks(mMainTask);
    		// 10/11/12 JHM - Remove callbacks to Gen2
    		//_timerHandler.removeCallbacks(mMainTask_Gen2);
        }
    }

    /// <summary>
    /// Resume the periodic read timer to the original interval. 
    /// This enables the periodic reading of the EOBR.
    /// </summary>
	public void ResumeReading()
	{
		// don't resume reading if the ignore timer pop function is set
		if (this._ignoreResume)
			return;

		// resume the timer to the time interval
		Log.v("EobrService", String.format("ResumeReading timerCreated: %s", _timerCreated));

		if (_timerCreated)
		{
            Log.v("EobrService", String.format("ResumeReading _timerCreated: %s", _timerCreated));
			CreateTimer();
		}
		else
		{
			// if the read timer has not been created, then initiate reading
			try
			{
				this.InitiatePeriodicReading();
			}
			catch (KmbApplicationException kae)
			{
				ErrorLogHelper.RecordException(this, kae);
			}
		}
	}
    
    /**
     * Suspends periodic reading and forces an immediate read from the EOBR.
     * This will only be done for Gen II EOBR's.
     * <br /><br />
     * This is a blocking call that will won't finish until all outstanding EOBR records have been read and processed.
     */
    public void ForceImmediateRead()
    {
        Log.v("EobrService", String.format("ForceImmediateRead _timerCreated: %s", _timerCreated));
    	if (!EobrReader.getInstance().isEobrGen1())
    	{
	    	if (_timerCreated)
	    	{
	    		SuspendReading();
	    		_timerCreated = false;
	    	}
	    	
	    	_ignoreNextDefaultDriverEvent = true;
	    	ProcessTimerPop_Gen2();
	    	_ignoreNextDefaultDriverEvent = false;
    	}
    }
    
    /// <summary>
    /// Shutdown the reader and the EOBR device.
    /// </summary>
    public void Shutdown()
    {
        // turn off the timer and dispose of it
    	if(_timerCreated)
        {
    		SuspendReading();
    		
    		_timerCreated = false;
        }
    	
    	// remove all the delegates
    	EobrReader eobrReader = EobrReader.getInstance();
    	eobrReader.setEobrReaderChangeEventHandler(null);    	
    	eobrReader.setEobrHistoryChangeEventHandlerGenII(null);
    	
    	// gracefully shutdown the eobr device
    	try {
			eobrReader.Technician_ShutdownEobrDevice(this);

			// TODO eobr device was successfully shutdown, remove the instance
			// of both the device and the reader
			//eobrReader.setEobrDevice(null);
			//EobrReader.getInstance() == null;
		} catch (KmbApplicationException kae) {
			ErrorLogHelper.RecordException(this, kae);
		}
        MalfunctionManager.getInstance().stopScheduledProcessesForMalfunctions();
    }
    
    /// <summary>
    /// Fetch and process the historical records that may be stored
    /// in the EOBR device from the last time a successful read from the device occurred.
    /// If something unexpected occurs, then the device will be marked as 'OFFLINE', 
    /// and null will be returned.
    /// If a list of history records is generated, they will be published to the UI.
    /// When everything works OK, the device will be marked as 'ONLINE' upon completion.
    /// </summary>
    public ArrayList<StatusRecord> ReadHistoricalStatusRecords()
    {
    	EobrReader eobrReader = EobrReader.getInstance();
    	boolean done = false;
    	ArrayList<StatusRecord> historyList = new ArrayList<StatusRecord>();
    	float startRange = -1F;
    	float endRange = -1F;
    	
    	Log.d("EobrService", "ReadHistoricalStatusRecords EobrReader attempting to read driving history.");
    	eobrReader.TransitionDeviceToNewState(this, ConnectionState.READINGHISTORICAL);
    	
    	Date timestamp;
    	Bundle bundle = eobrReader.Technician_ReadReferenceTimestamp();
    	int rc = bundle.getInt(this.getString(R.string.rc));
    	long refTimestampMS = bundle.getLong(this.getString(R.string.returnvalue));
    		    	
    	if (rc != EobrReturnCode.S_SUCCESS)
    	{
            // an error occurred reading the reference timestamp, abort this method
            ConnectionState newConnectionState = rc == EobrReturnCode.S_DEV_NOT_CONNECTED ? ConnectionState.OFFLINE : ConnectionState.DEVICEFAILURE;
            eobrReader.TransitionDeviceToNewState(this, newConnectionState, String.format("ReadAndPublishHistoricalStatusRecords: ReadReferenceTimestamp() bad return code '%d'", rc));
            return null;
    	}
    	
        // verify that the EOBR SerialNumber has been read
        if (eobrReader.getEobrSerialNumber() == null || eobrReader.getEobrSerialNumber().equals(""))
        {
            // there have been some defects where the serial number 
            // was not read properly during Initialization
            // If this happens, try to read it again
            try
            {
            	eobrReader.setEobrSerialNumber(eobrReader.Technician_GetSerialNumber(this));
            } 
            catch (KmbApplicationException e) {
            	
            	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }

            // if it failed to read, then go offline and try again
            if (eobrReader.getEobrSerialNumber() == null || eobrReader.getEobrSerialNumber().equals(""))
            {
            	eobrReader.TransitionDeviceToNewState(this, ConnectionState.OFFLINE, "Failure while reading EOBR Serial Number.");
                return null;
            }
        }
 
    	if (refTimestampMS == -1)
    	{
    		timestamp = null;
    	}
    	else
    	{
       		timestamp = new Date(refTimestampMS);    			    		
    		Log.d("EobrService", "ReadHistoricalStatusRecords EobrReader reference timestamp: " + timestamp.toString());
    	}

        // the following setting allows the reference timestamp in the EOBR
        // to be overridden with a different value.
        // This allows the historical records to be read from any point.
        // NOTE: the config setting should be removed immediately when done
        //       debugging the history records, or else the next time debugging
        //       ths history will be read from the same spot causing duplicate UDPs.
    	if (GlobalState.getInstance().getAppSettings(this).getOverrideReferenceTimestamp())
        {
            Date referenceTimestampOverride = GlobalState.getInstance().getAppSettings(this).getReferenceTimestamp();
            Date current = DateUtility.getCurrentDateTimeUTC();
            if (current.compareTo(referenceTimestampOverride) < 0)
            {
                // if the override for the ref timestamp is in the future, 
                // reset it back to current
                // this has the effect of initially ignoring the history records
                referenceTimestampOverride = DateUtility.AddMinutes(current, -5);
                //done = true;
            }
            timestamp = referenceTimestampOverride;

    		Log.d("EobrService", String.format("EobrReader overriding the reference timestamp: %s", DateUtility.getHomeTerminalDateTimeFormat().format(timestamp)));
        }
        
    	StatusRecord statusRec = new StatusRecord();
    	rc = eobrReader.Technician_GetHistoricalData(statusRec, timestamp);
    	
        // if getting historical wasn't successful and the return code is a general
        // type error, try one more time
        if (rc != EobrReturnCode.S_SUCCESS &&
            ((rc == EobrReturnCode.S_DEV_INTERNAL_ERROR) ||
            (rc == EobrReturnCode.S_COMMS_BUSY) ||
            (rc == EobrReturnCode.S_GENERAL_ERROR)))
        {
        	rc = eobrReader.Technician_GetHistoricalData(statusRec, timestamp);
        }
           	
        if (rc == EobrReturnCode.S_SUCCESS)
        {
            // verify odometer is valid compared to LastEobrOdometer
            // if not, need to find a valid odometer to start
            if (statusRec.getOdometerReading() > 0)
            {
                // validate the odometer is valid based on the last eobr odometer that is setup
            	Bundle odometerValidBundle = eobrReader.OdometerInValidRange( statusRec.getTimestampUtc(), statusRec.getOdometerReading());
            	boolean isInValidRange = odometerValidBundle.getBoolean(EobrReader.ISINVALIDRANGE);
            	startRange = odometerValidBundle.getFloat(EobrReader.STARTRANGE);
            	endRange = odometerValidBundle.getFloat(EobrReader.ENDRANGE);
            	if (!isInValidRange)
                {
                    StatusRecord newStatusRec = eobrReader.SearchForValidOdometer(statusRec.getTimestampUtc(), statusRec.getSpeedometerReading(), statusRec.getOverallStatus(), startRange, endRange);

                    // if we didn't find a record with a valid odometer, set statusRec odometer reading to -1
                    if (newStatusRec == null)
                        statusRec.setOdometerReading(-1F);

                    // else if in searching for a valid odometer, we crossed a motion change,
                    // set previous status record odometer to -1
                    else if ((statusRec.getSpeedometerReading() <= 0 && newStatusRec.getSpeedometerReading() > 0) ||
                             (statusRec.getSpeedometerReading() > 0 && newStatusRec.getSpeedometerReading() <= 0) ||
                             (statusRec.getOverallStatus() != newStatusRec.getOverallStatus()))
                    {
                        statusRec.setOdometerReading(-1);
                    }
                    else
                    {
                        statusRec = newStatusRec;
                    }
                }

                // update last eobr odometer and timestamp - only update if it has previously been set
                if (statusRec.getOdometerReading() > 0 && eobrReader.getLastEobrOdometerUTCTime() != null)
                {
                	eobrReader.setLastEobrOdometer(statusRec.getOdometerReading());
                	eobrReader.setLastEobrOdometerUTCTime(statusRec.getTimestampUtc());
                }
            }

            // read from EOBR was successful
            if (!statusRec.IsEmpty())
            {
                // for first history record, if speed is greater than 0, need to have a valid 
                // odometer or mileage generated for unassigned event is incorrect,
                // continue searching future records for a valid odometer or until speed is 0
                // or we reach current data
                if (statusRec.getSpeedometerReading() > 0 && statusRec.getOdometerReading() < 0)
                {
                    StatusRecord newStatusRec = eobrReader.SearchFirstValidOdometer(statusRec.getTimestampUtc());
                    if (newStatusRec != null)
                    {
                        statusRec = newStatusRec;

                        // update last eobr odometer and timestamp - only update if it has previously been set
                        if (statusRec.getOdometerReading() > 0 && eobrReader.getLastEobrOdometerUTCTime() != null)
                        {
                            eobrReader.setLastEobrOdometer(statusRec.getOdometerReading());
                            eobrReader.setLastEobrOdometerUTCTime(statusRec.getTimestampUtc());
                        }
                    }
                    else
                    {
                        // add status rec to history list, so there is one record in the history
                        // list so we will go back online to the eobr.  Then stop processing.
                        historyList.add(statusRec);
                        done = true;
                        ErrorLogHelper.RecordMessage(this, String.format("ReadHistoricalStatusRecords:  Valid odometer not found for first entry - {%s}", DateUtility.getHomeTerminalDateTimeFormat24Hour().format(statusRec.getTimestampUtc()) ));

                        // in this boundary condition where current reference time has
                        // speed > 0, we couldn't find a record with a valid odometer,
                        // go back online with the connection and essentially "skip" 
                        // this historical period
                    }
                }

                if (!done)
                {
                    // convert the odometer to the dashboard odometer
                	// this wasn't doing anything because the offset and multiplier
                	// variables were never set.  We don't want to convert to dashboard
                	// odometer - we want to store the eobr odometer in the status record
                    //statusRec.setOdometerReading(eobrReader.ConvertToDashboardOdometer(statusRec.getOdometerReading()));

                    // save the history record for processing
                    //System.Diagnostics.Debug.WriteLine(string.Format("num:{0} id:{1} t:{2} o:{3} s:{4}", historyList.Count, statusRec.RecordID, statusRec.TimeStampUtc, statusRec.OverallStatus, statusRec.SpeedometerReading));
                    historyList.add(statusRec);

                    // move the timestamp to the next one to search from
                    timestamp = statusRec.getTimestampUtc();
                }
            }
            else
            {
                // stop processing
                done = true;
                ErrorLogHelper.RecordMessage(this, String.format("ReadHistoricalStatusRecords:  Current StatusRecord is empty: %d:%d:%d", statusRec.getBlock(), statusRec.getPage(), statusRec.getEntry()));
            	eobrReader.TransitionDeviceToNewState(this, ConnectionState.ONLINE, "ReadHistoricalStatusRecords: Skip this history period, because no records found.");
            	return null;
            	
                // in this boundary condition where the first record
                // in the period doesn't exist, go back online with the connection
                // and essentially "skip" this historical period because there
                // are no records to process
            }
        }
        else
        {
            // read was not successful, go offline and abort processing any further
            ConnectionState newConnectionState = rc == EobrReturnCode.S_DEV_NOT_CONNECTED ? ConnectionState.OFFLINE : ConnectionState.DEVICEFAILURE;
            eobrReader.TransitionDeviceToNewState(this, newConnectionState, String.format("ReadAndPublishHistoricalStatusRecords: GetHistoricalData() bad return code '{%d}' ts: '{%s}'", rc, timestamp));
            return null;
        }
        
    	while (!done)
    	{    		    	
            // starting from the timestamp, look for next record where a 
            // change in vehicle motion occurred
    		statusRec = new StatusRecord();
            Date previousRecordTimestamp = timestamp;

            rc = eobrReader.Technician_GetNextVehicleMotionChangeData(statusRec, timestamp);
            Log.d("EobrService", String.format("ReadHistoricalStatusRecords EobrReader NextMotionChange: ts: {%s} received: %s id: %s speed: %f odom: %f st: %d", timestamp, statusRec.getTimestampUtc(), statusRec.getRecordId(), statusRec.getSpeedometerReading(), statusRec.getOdometerReading(), statusRec.getOverallStatus()));

            // if getting next motion change wasn't successful and the return code 
            // is a general type error, try one more time
            if (rc != EobrReturnCode.S_SUCCESS &&
                ((rc == EobrReturnCode.S_DEV_INTERNAL_ERROR) ||
                (rc == EobrReturnCode.S_COMMS_BUSY) ||
                (rc == EobrReturnCode.S_GENERAL_ERROR)))
            {
                rc = eobrReader.Technician_GetNextVehicleMotionChangeData(statusRec, timestamp);
            }
                        
            if (rc == EobrReturnCode.S_SUCCESS)
    		{
    			if (statusRec.IsEmpty())
    			{
                    // no status record came back, so we're caught up current
                    // get the last record in the history period
    				statusRec = new StatusRecord();
    				rc = eobrReader.Technician_GetCurrentData(statusRec, true);
    				if (rc == EobrReturnCode.S_SUCCESS)
    				{
                        if (statusRec.getOdometerReading() > 0)
                        {
                        	Bundle b = eobrReader.OdometerInValidRange(statusRec.getTimestampUtc(), statusRec.getOdometerReading());
                            if (!b.getBoolean(EobrReader.ISINVALIDRANGE))
                                statusRec.setOdometerReading(-1);
                        }

                        //System.Diagnostics.Debug.WriteLine(string.Format("num:{0} id:{1} t:{2} o:{3} s:{4}", historyList.Count, statusRec.RecordID, statusRec.TimeStampUtc, statusRec.OverallStatus, statusRec.SpeedometerReading));
                        historyList.add(statusRec);

                        // update last eobr odometer and timestamp
                        if (statusRec.getOdometerReading() > 0 && eobrReader.getLastEobrOdometerUTCTime() != null)
                        {
                        	eobrReader.setLastEobrOdometer(statusRec.getOdometerReading());
                        	eobrReader.setLastEobrOdometerUTCTime(statusRec.getTimestampUtc());
                        }
                    }
                    else
                    {
                        // read was not successful, go offline and abort processing any further
                        ConnectionState newConnectionState = rc == EobrReturnCode.S_DEV_NOT_CONNECTED ? ConnectionState.OFFLINE : ConnectionState.DEVICEFAILURE;
                        eobrReader.TransitionDeviceToNewState(this, newConnectionState, String.format("ReadAndPublishHistoricalStatusRecords: GetCurrentData() bad return code '{%d}'", rc));

                        // note: this is a special case only when a failure occurs here should
                        // the method be aborted.   If there is a disconnect when
                        // reading current status, the method should continue as normal
                        if (newConnectionState == ConnectionState.DEVICEFAILURE)
                            return null;                           
                    }

                    // stop processing
    				done = true;
    			}
    			else
    			{
                    if (statusRec.getOdometerReading() > 0)
                    {
                    	Bundle b = eobrReader.OdometerInValidRange(statusRec.getTimestampUtc(), statusRec.getOdometerReading());
                    	startRange = b.getFloat(EobrReader.STARTRANGE);
                    	endRange = b.getFloat(EobrReader.ENDRANGE);
                        if (!b.getBoolean(EobrReader.ISINVALIDRANGE))
                        {
                            StatusRecord newStatusRec = eobrReader.SearchForValidOdometer(statusRec.getTimestampUtc(), statusRec.getSpeedometerReading(), statusRec.getOverallStatus(), startRange, endRange);

                            // if we didn't find a record with a valid odometer, set statusRec odometer reading to -1
                            if (newStatusRec == null)
                                statusRec.setOdometerReading(-1);

                            // else, if in searching for a valid odometer, we crossed a motion change
                            // set the odometer of the status record we started searching from to -1
                            else if ((statusRec.getSpeedometerReading() <= 0 && newStatusRec.getSpeedometerReading() > 0) ||
                                     (statusRec.getSpeedometerReading() > 0 && newStatusRec.getSpeedometerReading() <= 0) ||
                                     (statusRec.getOverallStatus() != newStatusRec.getOverallStatus()))
                            {
                                statusRec.setOdometerReading(-1);
                            }
                            // else, set status record to new status record to add that record to the history list
                            else
                                statusRec = newStatusRec;
                        }

                        // update last eobr odometer and timestamp
                        if (statusRec.getOdometerReading() > 0 && eobrReader.getLastEobrOdometerUTCTime() != null)
                        {
                        	eobrReader.setLastEobrOdometer(statusRec.getOdometerReading());
                        	eobrReader.setLastEobrOdometerUTCTime(statusRec.getTimestampUtc());
                        }
                    }

                    if (statusRec.getTimestampUtc().getTime() < previousRecordTimestamp.getTime() && 
                    		previousRecordTimestamp.getTime() - statusRec.getTimestampUtc().getTime() >= 1000)
                     {
                        // check to validate that we're moving chronologically forward through
                        // the history.    If the current record every goes chronlogically
                        // earlier then the previous one, then there's a problem.
                        // Set the EOBR in failure state, and abort

                        ConnectionState newConnectionState = rc == EobrReturnCode.S_DEV_NOT_CONNECTED ? ConnectionState.OFFLINE : ConnectionState.DEVICEFAILURE;
                        eobrReader.TransitionDeviceToNewState(this, newConnectionState, String.format("Reading History detected loopback, NextMotionChange() returned new record: '{%s}', earlier than previous record: '{%s}'", statusRec.getTimestampUtc(), previousRecordTimestamp));
                        return null;
                    }

                    // convert the odometer to the dashboard odometer
                	// this wasn't doing anything because the offset and multiplier
                	// variables were never set.  We don't want to convert to dashboard
                	// odometer - we want to store the eobr odometer in the status record
                    //statusRec.setOdometerReading(eobrReader.ConvertToDashboardOdometer(statusRec.getOdometerReading()));

                    // save the history record for processing
                    //System.Diagnostics.Debug.WriteLine(string.Format("num:{0} id:{1} t:{2} o:{3} s:{4}", historyList.Count, statusRec.RecordID, statusRec.TimeStampUtc, statusRec.OverallStatus, statusRec.SpeedometerReading));
                    eobrReader.AddToHistoryList(historyList, statusRec);

                    // move the timestamp to the next one to search from
                    timestamp = statusRec.getTimestampUtc();

                    // if the GPS location is not valid, then pull the very next record
                    // in order to attempt to get a statusRec with a valid GPS into the history list
                    // so that the VMD will be able to find valid GPS for starts and stops
                    if (!statusRec.IsGpsLocationValid())
                    {
                        // only get 1, and only 1, status record to try and fix this problem
                        // if the GPS is not valid, try to use only the next record
                        // it might be necessary to pull more than 1 record to fix this, 
                        // but for now this is good enough.
                        // NOTE: The GPS may not be connected, or functioning, so this code
                        // should never be performed in a loop.
                        Date nextTimestamp = DateUtility.AddSeconds(timestamp, GlobalState.getInstance().getCompanyConfigSettings(this).getEobrDataCollectionRateSeconds());
                        StatusRecord nextStatusRec = new StatusRecord();
                        rc = eobrReader.Technician_GetHistoricalData(nextStatusRec, nextTimestamp);
                        //rc = eobrDevice.GetHistoricalData(nextTimestamp, nextStatusRec);
                        if (rc == EobrReturnCode.S_SUCCESS)
                        {
                            // TCH - 2/17/10 - if nextStatusRec has valid gps, then add this
                            // record to the history list.  If gps still isn't valid, doesn't
                            // accomplish anything to add this to the history
                            if (nextStatusRec.IsGpsLocationValid())
                            {
                                // convert the odometer to the dashboard odometer
                            	// this wasn't doing anything because the offset and multiplier
                            	// variables were never set.  We don't want to convert to dashboard
                            	// odometer - we want to store the eobr odometer in the status record
                            	//nextStatusRec.setOdometerReading(eobrReader.ConvertToDashboardOdometer(nextStatusRec.getOdometerReading()));

                                historyList.add(nextStatusRec);
                            }
                        }
                    }
    			}
    		}
            else
            {
                // read was not successful, go offline and abort processing any further
                ConnectionState newConnectionState = rc == EobrReturnCode.S_DEV_NOT_CONNECTED ? ConnectionState.OFFLINE : ConnectionState.DEVICEFAILURE;
                eobrReader.TransitionDeviceToNewState(this, newConnectionState, String.format("ReadAndPublishHistoricalStatusRecords: NextMotionChange() bad return code '{%d}'", rc));
                return null;
            }           
    	}

        // diagnostics...if this is set, then turn if off so that the next time
        // through it does not override again.
        if (GlobalState.getInstance().getAppSettings(this).getOverrideReferenceTimestamp())
        {
            GlobalState.getInstance().getAppSettings(this).setOverrideReferenceTimestamp(false);
        }       
        
        return historyList; 
    }
        
    public ArrayList<StatusRecord> ReadHistoricalDrivingRecords()
    {
    	return this.ReadHistoricalStatusRecords();
    }
    
    // Target we publish for clients to send messages to IncomingHandler.
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // Handler of incoming messages from clients.
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EobrServiceMessages.MSG_REGISTER_CLIENT:
            	_replyToClient = msg.replyTo;
                break;
            case EobrServiceMessages.MSG_UNREGISTER_CLIENT:
            	if(_replyToClient == msg.replyTo) _replyToClient = null; 
            	else _replyToClient = null;
                break;
            case EobrServiceMessages.MSG_SUSPEND_READING:
            	EobrService.this.SuspendReading();
            	break;
            case EobrServiceMessages.MSG_RESUME_READING:
            	EobrService.this.ResumeReading();
            	break;
            case EobrServiceMessages.MSG_READHISTORYONTIMERPOP:
            	EobrService.this.ReadHistoryOnTimerPop();
            	break;
            case EobrServiceMessages.MSG_FORCE_IMMEDIATE_READ:
            	EobrService.this.ForceImmediateRead();
            	if (msg.obj != null)
            	{
            		synchronized (msg.obj)
            		{
            			msg.obj.notifyAll();
            		}
            	}
            	break;
            case EobrServiceMessages.MSG_SHUTDOWN:
            	EobrService.this.Shutdown();
            	break;
            case EobrServiceMessages.MSG_READANDPUBLISHHISTORICAL:
            	EobrService.this.ReadAndPublishHistoricalStatusRecords();
            	break;
            case EobrServiceMessages.MSG_READANDPUBLISHHISTORICAL_GEN2:
            	EobrService.this.ReadAndPublishHistoricalGen2Records();
            case EobrServiceMessages.MSG_HEARTBEAT:
            	Log.v("EobrService", "Heartbeat received from KMB");
            	
            	// Respond to all Heartbeat messages (these messages are coming from RodsEntry HeartBeatTimerTask every 60 seconds) so RodsEntry can 
            	// keep track of the service's status and restart it if it does not receive a reply.
            	boolean processMsg = true;
            	if (_replyToClient == null) {
            		if (msg.replyTo == null) {            			
            			processMsg = false;
            		}
            		else {
            			_replyToClient = msg.replyTo;
            		}
            	}
            	
            	if (processMsg) {
            		try {
	            		Message reply = Message.obtain(null, EobrServiceMessages.MSG_HEARTBEAT);
	            		_replyToClient.send(reply);
            		}
            		catch (RemoteException e) {
            			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            		}
            	}
            	
            	break;
            case EobrServiceMessages.MSG_IGNORERESUMEON:
            	EobrService.this._ignoreResume = true;
            	break;
            case EobrServiceMessages.MSG_IGNORERESUMEOFF:
            	EobrService.this._ignoreResume = false;
            	break;
            /*case EobrServiceMessages.MSG_SET_VALUE:
            	someValue =  msg.arg1;
            	break;*/
            default:
                super.handleMessage(msg);
            }
        }
    }

	//read and discard the most recent Histogram and DTC data so that 
    //the EOBR can properly perform its garbage collection
    private void ReadAndDiscardHistogramAndDtcData()
    {   	
    	Runnable runnable = new Runnable() {
			public void run() {
		    	EobrReader eobrReader = EobrReader.getInstance();

				HistogramData histogramData = new HistogramData();
				histogramData.setRecordId(0xFFFFFFFF);
				eobrReader.Technician_GetHistogramData(
						histogramData,
						new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), 
						new HistogramTypeEnum(HistogramTypeEnum.VEHICLESPEED), 
						0, 
						true);
				
				JbusDiagnosticData diagnosticData = new JbusDiagnosticData();
				diagnosticData.setRecordId(0xFFFFFFFF);
				eobrReader.Technician_GetJbusDiagnosticData(
						diagnosticData,
						new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID),  
						0, 
						true);
			}
    	};
    	
    	new Thread(runnable).start();
    }
    
	public void ApplicationUpdate(boolean readHistoryIfNoUpdate)
	{
        AppUpdateTask updateTask = new AppUpdateTask(readHistoryIfNoUpdate);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			updateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			updateTask.execute();
	}
    
	private class AppUpdateTask extends AsyncTask<Void, Void, Void> {
		
		private boolean _readHistoryIfNoUpdate = false;
		
		private AppUpdateTask(boolean readHistoryIfNoUpdate){
			_readHistoryIfNoUpdate = readHistoryIfNoUpdate;
		}
		
		protected Void doInBackground(Void... params)
		{
			// The following determines if we are connected to a BTE device;
			// if so we need to chack the FW version and determine if an appliction update is needed

			IFirmwareUpgrader upgrader;
			if (_applicationUpdateListener == null)
			{
			    EobrReader eobrReader = EobrReader.getInstance();
				upgrader = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, EobrService.this);
				if (upgrader != null && upgrader.getIsApplicationUpgradeRequired()) {
                  	_applicationUpdateListener = eobrReader.getApplicationUpdateHandler();
				}
			}
			if (_applicationUpdateListener != null)
			{
				_applicationUpdateListener.onApplicationUpdateRequired();
			}
			else {
				// no update needs to be installed.
				// We need to continue
				
				if(_readHistoryIfNoUpdate){

					int eobrGeneration = EobrReader.getInstance().getEobrGeneration();					
					if(eobrGeneration >= 2){						
						// 2014.09.16 sjn - I'm not wild about this design.  When activating a BTE, the service is suspended while the appUpdate is checked.
						//                  However, the service is only suspended for the BTE, and none of the others.  I wish this logic was easier to follow.
						//                  EobrReader.ReadAndPublishHistoricalStatusRecords is where the service is suspended for BTE
						// for a BTE, need to read history and resume processing. 

						boolean isJJK = EobrReader.getInstance().Technician_GetEobrHardware(GlobalState.getInstance().getApplicationContext());
						if(!isJJK){
							EobrService.this.ReadAndPublishHistoricalGen2Records();					
							EobrService.this.ResumeReading();
						}
					}
				}
			}
			
			return null;
		}
	
	}

	/// <summary>
	/// Verify we have access to VIN for the Roadside Inspection.
	/// </summary>
	private void VerifyAccessToRoadsideInspectionVin(EobrReader eobrReader, String serialNumber) {

    	Log.v("EobrService", "VerifyAccessToRoadsideInspectionVin");

		if (eobrReader == null || TextUtils.isEmpty(serialNumber)) {
			return;	// not enough data provided
		}

		// if the VIN field is populated in the EobrDevice table for the ELD we are connecting to, then we don't have to do either (read from ELD or download from ENC).
		EobrConfigurationFacade facade = new EobrConfigurationFacade(this.getContext(), GlobalState.getInstance().getCurrentUser());
		EobrConfiguration eobrConfigLocal = facade.Fetch(serialNumber);
		if(eobrConfigLocal != null && !TextUtils.isEmpty(eobrConfigLocal.getVIN())) {
			return;		// VIN found from EobrDevice table
		}

		// if the VIN field is not populated in the EobrDevice, attempt to read from the ELD
		String vin = eobrReader.GetVin();
		if (!TextUtils.isEmpty(vin)) {
			// if we successfully read a value from the ELD, store it in EobrConfig (EobrDevice table), so we don't have to continually go to the ELD to read the VIN each time
			if (eobrConfigLocal != null) {
				eobrConfigLocal.setVIN(vin);
				facade.Save(serialNumber, eobrConfigLocal);
			}
			return;	// VIN available from ELD
		}

		// last chance, attempt to download from Encompass
		// called from ProcessTimerPop_Gen2 in doInBackground of ProcessTimerTaskGen2 so we're already in a background thread
		EobrConfigController eobrConfigController = new EobrConfigController(getContext());
		EobrConfiguration eobrConfiguration = eobrConfigController.DownloadConfigFromDMO(serialNumber);
		if (eobrConfiguration != null) {
			GlobalState.getInstance().setCurrentEobrConfiguration(eobrConfiguration);
		}
	}

	private void DownloadFirmwareUpdate(EobrReader eobrReader)
    {
    	IFirmwareUpgrader upgrader = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, this);
    	upgrader.initiateFirmwareUpgrade(false);
    }
    
    private void notifyFirmwareUpdateFinished(boolean success) {
        FirmwareUpdateBroadcaster broadcaster = new FirmwareUpdateBroadcaster();
        broadcaster.onFirmwareUpdateFinished(success);
    	_isFinishingFirmwareUpdate = false;
    }

    /* BEGIN Gen2 Service logic */
    private Runnable mMainTask_Gen2 = new Runnable()
    {
        public void run() 
        {
        	Log.v("EobrService", String.format("mMainTask.run() reply: %s timerSuspend: %s", _replyToClient, _timerSuspended));
        	if(_replyToClient != null && !_timerSuspended)
        	{        		
				_timerHandler.removeCallbacksAndMessages(null);
				
        		ProcessTimerTaskGen2 timerTask = new ProcessTimerTaskGen2();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					timerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					timerTask.execute();
        	} else {
                //Run again if we fail.
                _timerHandler.postDelayed(this, 1000);
            }
        }
    };
    
    private class ProcessTimerTaskGen2 extends AsyncTask<Void, Void, ProcessTimerTaskResultGen2> {

        public ExceptionInTaskCallback exceptionCallback;

    	protected ProcessTimerTaskResultGen2 doInBackground(Void... params) {
            Log.v("ProcessTimerTaskGen2", String.format("ProcessTimerTaskGen2 isReadingHistory:%s", _isReadingHistory));
            ProcessTimerTaskResultGen2 result = new ProcessTimerTaskResultGen2();
            // reading history is done on a separate thread.  Don't fire timer pop
    		// if currently reading history
            try {
                if (!_isReadingHistory)
                    ProcessTimerPop_Gen2();
            } catch (Exception ex) {
                Log.e("ProcessTimerTaskGen2", String.format("Caught exception in ProcessTimerTaskGen2:doInBackground ex: %s", ex));
                result.setException(ex);
            }

    		return result;
    	}

    	protected void onPostExecute(ProcessTimerTaskResultGen2 result) {
            Log.v("ProcessTimerTaskGen2", "ProcessTimerTaskGen2 - onPostExecute/postDelayed");

            // if we need to read history yet, fire timer pop in 1 second
        	// otherwise fire in 30 seconds
        	if (_needToReadHistoryOnNextTimerPop)
        		_timerHandler.postDelayed(mMainTask_Gen2, 1000);
        	else
        	{
        		// if screen is off, fire timer pop every 45 seconds rather than 15
        		if (_powerMgr != null && !_powerMgr.isScreenOn())
        			_timerHandler.postDelayed(mMainTask_Gen2, 45000);
        		else
        			_timerHandler.postDelayed(mMainTask_Gen2, 15000);
        	}

            reportException(result);
    	}

        private void reportException(ProcessTimerTaskResultGen2 result) {
            if(result == null) return;

            boolean shouldReportException = result.getException() != null && exceptionCallback != null;
            if(shouldReportException)
                exceptionCallback.onException(result.getException());
        }
    }

    private class ProcessTimerTaskResultGen2 {
        private Exception _exception;
        public Exception getException(){
            return _exception;
        }

        public void setException(Exception ex){
            _exception = ex;
        }
    }

    private interface ExceptionInTaskCallback {
        void onException(Exception ex);
    }

    private int ReadRecordsToCurrent(EobrReader eobrReader , ArrayList<EventRecord> currentEvents, ArrayList<TripReport> currentTrips)
    {
    	return this.ReadRecordsToCurrent(eobrReader, currentEvents, currentTrips, null);
    }

    private int ReadRecordsToCurrent(EobrReader eobrReader , ArrayList<EventRecord> currentEvents, ArrayList<TripReport> currentTrips, StatusRecord currentStatus) {
    	int rc = EobrReturnCode.S_SUCCESS;
    	long refTimestamp;
    	boolean readingHistory = currentStatus == null;
    	boolean disconnectOccurred = false;
    	long disconnectedTimestamp = -1;
    	long prevTripRecordTimestamp = -1;
    	final boolean updateReferenceTimestamps = !readingHistory;
    	final StatusRecordQueryMethodEnum statusRecordQueryByTimestamp = new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP);

    	// Get current values for reference timestamps
        Bundle bundle = eobrReader.Technician_ReadReferenceTimestamp();
    	rc = bundle.getInt(this.getString(R.string.rc));

    	// Get all the events
    	EventRecord eventRecord = null;

    	// Check return code.  Only proceed on Success.
		if (rc == EobrReturnCode.S_SUCCESS) {
	    	refTimestamp = bundle.getLong(this.getString(R.string.eventreftime)) + 1;
	    	do {
	    		if(shouldAbortReadingHistory()) {
                    return EobrReturnCode.S_ABORTED;
                }

	        	eventRecord = new EventRecord();
				rc = eobrReader.Technician_GetEventData(eventRecord, statusRecordQueryByTimestamp, new EventTypeEnum(EventTypeEnum.ANYTYPE), refTimestamp, updateReferenceTimestamps);
				if (rc == EobrReturnCode.S_SUCCESS && eventRecord.getRecordId() != 0) {
                    // using the eventRecord's eobrid, find the status record that corresponds to this event
                    // so we can pull speedo, tach and other stuff from it
                    StatusRecord eventStatus = null;
                    if(eventRecord.getGeotabHOSDataKey() != 0){
                        eventStatus = GetStatusRecordForEobrId(eobrReader, eventRecord.getGeotabHOSDataKey());
                    }else{
                        eventStatus = GetStatusRecordForEobrId(eobrReader, eventRecord.getEobrId());
                    }

                    eventRecord.setStatusRecordData(eventStatus);
                    LogGetStatusRecordResult("ReadRecordsToCurrent", eventRecord);

		        	// if processing current data and we read and event record that is a DRIVER type and the data is -1 that
		        	// means the TAB has issued the remote comm lost message and switched to company default thresholds
					if (eventRecord.getEventType() == EventTypeEnum.DRIVER && eventRecord.getEventData() == -1) {
                        if(!_ignoreNextDefaultDriverEvent && !readingHistory) {
                            currentEvents.add(eventRecord);
                            disconnectedTimestamp = eventRecord.getTimecode();
                            disconnectOccurred = true;
                            break;
                        }
                        _ignoreNextDefaultDriverEvent = false;
					}
		        	currentEvents.add(eventRecord);

		        	// Increment refTimestamp to get next record
		        	refTimestamp = eventRecord.getTimecode() + 1;
		        }
				else if (rc != EobrReturnCode.S_SUCCESS) {
                    return rc;
                }
	    	} while (eventRecord != null && eventRecord.getRecordId() > 0);
    	} else {
            return rc;
        }

    	// If function is passed in a status object, read the most current EOBR record
		if (currentStatus != null) {
			rc = eobrReader.Technician_GetCurrentData(currentStatus, updateReferenceTimestamps);
			// Check return code. Only proceed on Success.
			if (rc != EobrReturnCode.S_SUCCESS) {
                return rc;
            }
		}

    	// Get all the trips
    	TripReport tripReport;
    	long originalTripRefTimestamp = bundle.getLong(this.getString(R.string.tripreftime));
    	refTimestamp = originalTripRefTimestamp + 1;

        if (eobrReader.getEobrGeneration() == Constants.GENERATION_GEOTAB) {
            tripReport = new TripReport();
            rc = eobrReader.Technician_GetTripData(tripReport, statusRecordQueryByTimestamp, refTimestamp, updateReferenceTimestamps);
            // ELDIntermediateEvent - if enabled
            IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            try {
                empLogController.CreateIntermediateEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum.IntermediateLog, tripReport);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            if (rc != EobrReturnCode.S_SUCCESS) {
                return rc;
            }
        } else {
            do {
                if(shouldAbortReadingHistory()) {
                    return EobrReturnCode.S_ABORTED;
                }

                //For Geotab, we obtain the id from last GeotabHOSData record
                tripReport = new TripReport();
                rc = eobrReader.Technician_GetTripData(tripReport, statusRecordQueryByTimestamp, refTimestamp, updateReferenceTimestamps);

                // make sure it's not an empty trip record
                if(rc == EobrReturnCode.S_SUCCESS && tripReport.getRecordId() > 0) {
                    if (disconnectOccurred && (tripReport.getTimecode() > disconnectedTimestamp)) {
                        // if previous trip reference timestamp is set, set reference timestamp back to previous record. otherwise set back to the original timestamp.
                        if (prevTripRecordTimestamp > 0) {
                            rc = eobrReader.Technician_GetTripData(tripReport, statusRecordQueryByTimestamp, prevTripRecordTimestamp, updateReferenceTimestamps);
                        } else {
                            rc = eobrReader.Technician_GetTripData(tripReport, statusRecordQueryByTimestamp, originalTripRefTimestamp, updateReferenceTimestamps);
                        }
                        break;
                    }

                    currentTrips.add(tripReport);

                    eventRecord = new EventRecord();
                    eventRecord.setEventType(EventTypeEnum.HOURLYTRIPRECORD);
                    eventRecord.setTimecode(tripReport.getDataTimecode()); //DataTimecode represents the time the data was actually gathered; timecode represents the time it was stored in NVRAM
                    eventRecord.setTripReportData(tripReport);
                    currentEvents.add(eventRecord);

                    prevTripRecordTimestamp = tripReport.getTimecode();
                } else if (rc != EobrReturnCode.S_SUCCESS) {
                    return rc;
                }

                // Increment refTimestamp to get next record
                refTimestamp = tripReport.getTimecode() + 1;
            } while (tripReport.getRecordId() > 0);
        }

    	if (disconnectOccurred) {
            eobrReader.TransitionDeviceToNewState(GlobalState.getInstance(), ConnectionState.OFFLINE, this.getString(R.string.eobr_offline_remotecommlost));
        }

    	// I'm not sure if this is necessary here, but it feels good
		if(shouldAbortReadingHistory()) {
            return EobrReturnCode.S_ABORTED;
        } else {
            return rc;
        }
    }

    private boolean shouldAbortReadingHistory() {
        return this._isReadingHistory && GlobalState.getInstance().getAbortReadingHistory();
    }

    // Search for a status record with the given eobrId.
    private StatusRecord GetStatusRecordForEobrId(EobrReader eobrReader, int eobrId)
    {
    	StatusRecord status = null;

    	// if a valid eobrId specified, return eobr data for that id 
    	if (eobrId != 0 && eobrId != -1)
    	{
        	status = new StatusRecord();
        	int rc = eobrReader.Technician_GetHistoricalData(status, eobrId);

    		if(rc != EobrReturnCode.S_SUCCESS || status.getRecordId() == 0 || status.getRecordId() == -1) {
    			status = null;
    		}
    	}

        return status;
    }

    // Search for a StatusRecord for the given EventRecord, starting with the EobrId, and using the timecode of the EventRecord as a backup
    private StatusRecord GetStatusRecordForEventRecord(EobrReader eobrReader, EventRecord eventRecord) {
        StatusRecord status = GetStatusRecordForEobrId(eobrReader, eventRecord.getEobrId());

        if (status == null) {
            Date timestamp = new Date(eventRecord.getTimecode() - 1);
            status = new StatusRecord();
            int rc = eobrReader.Technician_GetHistoricalData(status, timestamp);

            // If getting historical wasn't successful and the return code is a general type error, try one more time
            if (rc != EobrReturnCode.S_SUCCESS &&
                    ((rc == EobrReturnCode.S_DEV_INTERNAL_ERROR) ||
                            (rc == EobrReturnCode.S_COMMS_BUSY) ||
                            (rc == EobrReturnCode.S_GENERAL_ERROR)))
            {
                rc = eobrReader.Technician_GetHistoricalData(status, timestamp);
            }

            if(rc != EobrReturnCode.S_SUCCESS || status.getRecordId() == 0 || status.getRecordId() == -1) {
                status = null;
            }
        }

        return status;
    }

    private void LogGetStatusRecordResult(String logMethodName, EventRecord eventData) {
        StatusRecord status = eventData.getStatusRecordData();
        if (status != null) {
            Log.d("EobrService", String.format("{%s} referenceTimestamp: {%s} eventData.timecode: {%s} found status record with timestampUtc: {%s}", logMethodName, new Date(eventData.getTimecode()), eventData.getTimecodeAsDate(), status.getTimestampUtc()));
        }
        else {
            Log.d("EobrService", String.format("{%s} referenceTimestamp: {%s} eventData.timecode: {%s} couldn't find a matching status record", logMethodName, new Date(eventData.getTimecode()), eventData.getTimecodeAsDate()));
        }
    }
    
    /// <summary>
    /// Process the EOBR reader time pop for Gen2 devices.
    /// </summary>
    private void ProcessTimerPop_Gen2()
    {    	
        try
        {
        	EobrReader eobrReader = EobrReader.getInstance();
        	Log.v("EobrService", String.format("ProcessTimerPop_Gen2 state: %s readHistory: %s", eobrReader.getCurrentConnectionState(), _needToReadHistoryOnNextTimerPop));
           logState();

        	switch (eobrReader.getCurrentConnectionState())
            {
                case ONLINE:
                    if (_needToUpdateFirmwareOnNextTimerPop)
                    {
                    	// first time in the timer pop - check if device is physically
                    	// connected - this is to handle case where sit on Trip info
                    	// screen until bt connection timesout or bt turns off
                    	if (eobrReader.IsDevicePhysicallyConnected(this, false))
                    	{
                    		// update thresholds in the event we sat on the trip info
                    		// screen until bt comm was lost or bt turned off
                    		this.UpdateDriverThresholds();
                    	
                    		// firmware needs to be updated
                    		_needToUpdateFirmwareOnNextTimerPop = false;
                    		this.DownloadFirmwareUpdate(eobrReader);
                    	}
                    	else
                    	{
                    		eobrReader.TransitionDeviceToNewState(this, ConnectionState.OFFLINE);
                    	}
                    }
                    else
                    {
                        // currently the EOBR is ONLINE
                        // no firmware update needs to be applied, we should be good to go
                    	
                    	if (_isFinishingFirmwareUpdate)
                    		notifyFirmwareUpdateFinished(true);
                    	
                        if (_needToReadHistoryOnNextTimerPop)
                        {
                        	ReadAndPublishHistoricalGen2Records();

                            _needToReadHistoryOnNextTimerPop = false;

							// Verify we have access to VIN for the Roadside Inspection
                            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
								VerifyAccessToRoadsideInspectionVin(eobrReader, eobrReader.getEobrSerialNumber());
							}
                        }
                        else
                        {
                        	//2013.09.30 sjn - added this to ensure that if network time has adjusted the clocks since the last time, the clock will be synced correctly
                        	eobrReader.VerifyClockConsistency(this);
                        	
                        	ReadAndPublishCurrentGen2Records(eobrReader, null);
                            // wait until the next timer pop to start reading again
                        }
                    }
                    
                    break;

                case SHUTDOWN:
                    _needToReadHistoryOnNextTimerPop = true;
                    break;
                case FIRMWAREUPDATE:
                    // ignore the shutdown EOBR, which comes from the user 'releasing' the connection
                    break;

                case OFFLINE:
                default:
                    // EOBR is currently OFFLINE
                	eobrReader.VerifyClockConsistency(this);

	                boolean checkForEobr = true;
	                if (eobrReader.getClockCheckTimestamp() != null && eobrReader.getEobrOfflineTimestamp() != null)
	                {
	                    // as long as the EOBR has been offline for approx 1 minute, 
	                    // then allow it to check for the presence of the EOBR                    	
	                    long elapsedTimeOffline = eobrReader.getClockCheckTimestamp().getTime() - eobrReader.getEobrOfflineTimestamp().getTime();
	                    checkForEobr = (elapsedTimeOffline/1000) > 50;
	                    
	                    if(_isFinishingFirmwareUpdate)
	                    	checkForEobr = true;
	                }
		
	                // see if the EOBR has become connected again
	                if (checkForEobr && eobrReader.IsDevicePhysicallyConnected(this, true))
	                {
                    	if (_isFinishingFirmwareUpdate)
                    		notifyFirmwareUpdateFinished(true);

                        /*
                        If we are dealing with GeoTab there is the possibility to deny USB permissions,
                        so we need to make sure that the ActivateEobrDevice() method executes and sets the
                        DatabusType so we don't trigger a DeviceFailure later on.
                        */
                        if(eobrReader.getEobrIdentifier() == GeotabConstants.MANUFACTURER){
                            eobrReader.ActivateEobrDevice(getContext(), eobrReader.getEobrIdentifier(), null, (short)0, Constants.GENERATION_GEOTAB);
                        }

	                	// reconnected to TAB, update thresholds to driver's thresholds before reading history
	                	this.UpdateDriverThresholds();
	                	
                		this.ReadAndPublishHistoricalGen2Records();
                		eobrReader.setEobrOfflineTimestamp(null);
                		if( eobrReader.getCurrentConnectionState() == ConnectionState.ONLINE ) 
                		{
                			this.ReadAndPublishCurrentGen2Records(eobrReader, null);
                		}
	                }
	                
                    break;
            }

            // save the current time so we can tell if we've timed out
        	GlobalState.getInstance().setLastTimerPop(DateUtility.getCurrentDateTimeWithSecondsUTC());
            
        }
        catch (Throwable excp)
        {
            ErrorLogHelper.RecordException(this, excp);
            if(excp.getCause() != null)
            {
            	ErrorLogHelper.RecordMessage(this, "Cause of unhandled error");
            	ErrorLogHelper.RecordException(this, excp.getCause());
            }
            excp.printStackTrace();
            
        	if (_isFinishingFirmwareUpdate)
        		notifyFirmwareUpdateFinished(false);
        	
            EobrReader.getInstance().TransitionDeviceToNewState(this, ConnectionState.DEVICEFAILURE, excp.getMessage());
        }

    }

	private void ReadAndPublishCurrentGen2Records(EobrReader eobrReader, EventRecord eventRecord) {
        int rc = EobrReturnCode.S_SUCCESS;
        ArrayList<EventRecord> currentEvents = new ArrayList<EventRecord>();
        ArrayList<TripReport> currentTrips = new ArrayList<TripReport>();
        boolean needMapPosition = false;
        GlobalState globalState = GlobalState.getInstance();

        // read a status record every time to update last odometer and last gps position
        StatusRecord currentStatus = new StatusRecord();

        if (eventRecord == null) {
            // Need to Process MapPosition every xx minutes.
            // If not set yet, or enough time has passed, create Mapping Position
            if (globalState.getNextMapPositionTimeStamp() == null || (TimeKeeper.getInstance().now()).compareTo(globalState.getNextMapPositionTimeStamp()) >= 0)
                needMapPosition = true;

            rc = ReadRecordsToCurrent(eobrReader, currentEvents, currentTrips, currentStatus);
        } else
            currentEvents.add(eventRecord);

        if (rc == EobrReturnCode.S_SUCCESS) {

            /*
            Always create and publish a Route Position Event to update the gps location and odometer.
            We do this because there have been issues with the route position event was not being created due
            to the consistent presence of diagnostic trouble code (DTC) events. This resulted in out of date gps locations and odometer information.
            */
            if (currentStatus != null) {
                EventRecord routePositionEvent = new EventRecord();
                routePositionEvent.setEventType(EventTypeEnum.ROUTEPOSITION);
                routePositionEvent.setStatusRecordData(currentStatus);

                currentEvents.add(routePositionEvent);
            }

            // successful read from the EOBR
            if (currentEvents.size() > 0) {
                boolean publishDutyStatusChange = false;
                for (int i = 0; i < currentEvents.size(); i++) {
                    EventRecord event = currentEvents.get(i);

                    // TODO VerifyClockConsistency
                    //eobrReader.VerifyClockConsistency(this, currentStatus.getTimestampUtc());

                    // If we try to create an EventType that we do not have defined in the EventTypeEnum class, an "Enum Index Out of Bounds"
                    // exception is raised causing the application to abort.  To stop the application from aborting, we wrap this in a try...catch
                    // and log the event type so we can determine what event type we are missing from the enum class.
                    try {
                        Log.v("Events", String.format("Event: %s", new EventTypeEnum(event.getEventType()).toDMOEnum()));
                    } catch (Exception e) {
                        ErrorLogHelper.RecordMessage(String.format("Event type (%s) unknown.  Exception: %s", event.getEventType(), e.getLocalizedMessage()));
                        continue;
                    }

                    // process each event, but only publish to UI when processing the last event
                    // in the list
                    if (i == currentEvents.size() - 1)
                        publishDutyStatusChange = true;

                    eobrReader.PublishEobrStatusChange(rc, event, publishDutyStatusChange);

                    // only update the last eobr odometer values if they have been previously set
                    // if they weren't previously set, we don't know for sure that the current
                    // odometer is valid - we don't want to set to an invalid value or we will ignore
                    // valid values
                    if (eobrReader.getLastEobrOdometerUTCTime() != null &&
                            event.getTripReportData() != null &&
                            event.getTripReportData().getOdometer() > 0) {
                        eobrReader.setLastEobrOdometer(event.getTripReportData().getOdometer());
                        eobrReader.setLastEobrOdometerUTCTime(event.getTripReportData().getDataTimecodeAsDate());
                    }
                }
            }

            // Publish the trips
            if (currentTrips.size() > 0) {
                Bundle thresholds = eobrReader.Technician_GetThresholds(this, 0);
                float speedThreshold = thresholds.getFloat(getString(R.string.thresholdvalues_speed));
                int rpmThreshold = thresholds.getInt(getString(R.string.thresholdvalues_rpm));
                for (TripReport tripReport : currentTrips) {
                    eobrReader.PublishTripReport(tripReport, speedThreshold, rpmThreshold);
                }
            }

            // Need Mapping based on time and conditions from ShouldGenerateMapPosition
            if (needMapPosition && ShouldGenerateMapPosition(currentStatus)) {
                EventRecord mapPositionEvent = new EventRecord();
                mapPositionEvent.setEventType(EventTypeEnum.MAPPOSITION);
                mapPositionEvent.setTimecode(currentStatus.getTimestampUtc().getTime());
                mapPositionEvent.setStatusRecordData(currentStatus);
                eobrReader.PublishEobrStatusChange(rc, mapPositionEvent);
            }

            // Store the last read EOBR record in state.
            // This will serve to handle motion detection for Gen2.
            globalState.setPreviousEobrStatusRecord(currentStatus);
        } else {
            // read failed, or some other problem, so go offline
            eobrReader.TransitionDeviceToNewState(this, ConnectionState.OFFLINE, String.format("ReadAndPublishCurrentStatusRecord: GetCurrentData() bad return code '%s'", rc));

            // publish the failure, along with the timestamp of the failure
            if (currentEvents != null && currentEvents.size() > 0)
                eobrReader.PublishEobrStatusChange(rc, currentEvents.get(0));
            else {
                // 10/12/12 JHM - Not sure if this is a possible scenario,
                // but if the event collection is empty, set the time as now.
                EventRecord dummyEvent = new EventRecord();
                dummyEvent.setTimecode(TimeKeeper.getInstance().now().getTime());
                eobrReader.PublishEobrStatusChange(rc, dummyEvent);
            }
        }
    }
	
	private boolean ShouldGenerateMapPosition(StatusRecord currentStatus)
	{
		boolean shouldGenerate = false;
		StatusRecord previousStatus = GlobalState.getInstance().getPreviousEobrStatusRecord();
		
		//Have Current Status and current GPS Time AND
		// either previous status null OR not same recordid as current status
		if (currentStatus != null && currentStatus.IsGpsLocationValid() &&
			(previousStatus == null || previousStatus.getRecordId() != currentStatus.getRecordId()))
		{
			shouldGenerate = true;
		}
		
		return shouldGenerate;
	}

	private void ReadAndPublishHistoricalGen2Records() {
        Log.v("EobrService", String.format("ReadAndPublishHistoricalGen2Records isReadingHistory: %s timerCreated: %s timerSuspended: %s ignoreResume: %s", _isReadingHistory, _timerCreated, _timerSuspended, _ignoreResume));
        if (!_isReadingHistory) {
            _isReadingHistory = true;
            EobrReader eobrReader = EobrReader.getInstance();
            eobrReader.TransitionDeviceToNewState(this, ConnectionState.READINGHISTORICAL);
            BaseReadHistoryThread readHistoryThread = CreateReadHistoryThread(eobrReader);
            readHistoryThread.start();
        }
    }
	
	/*
	 * UpdateDriverThresholds for current designated driver stored in global state.
	 * This is needed after a successful firmware update and after reconnecting to
	 * a TAB that previously became disconnected.  In both scenarios, the current
	 * connection state will not be online, but we will be connected to the TAB, so
	 * we do not want to check if device is available before updating the thresholds.
	 */
	public void UpdateDriverThresholds()
	{
    	LogEntryController logController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
    	logController.SetThresholdValues(GlobalState.getInstance().getCurrentDesignatedDriver(), false, false);
	}

	public boolean IsReadingHistory()
	{
		return _isReadingHistory;
	}

    private BaseReadHistoryThread CreateReadHistoryThread(EobrReader eobrReader) {
        BaseReadHistoryThread result;
        if(eobrReader.IsGetDrivingPeriodsSupported()) {
            if (eobrReader.IsGetEventDataEventMaskSupported() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                result = new ReadMandateAndDriveDataThread(eobrReader);
            }
            else {
                result = new ReadDriveDataThread(eobrReader);
            }
        }
        else {
            result = new ReadHistoryThread(eobrReader);
        }
        return result;
    }

	private abstract class BaseReadHistoryThread extends Thread {
		EobrReader eobrReader;
		
		public BaseReadHistoryThread(EobrReader eobrReader)
		{
			this.eobrReader = eobrReader;
		}

        protected void prepareToRun() {
            // 2014.07.16 sjn - Since this is the start of the history reading process, ensure
            //                  that this flag is reset
            GlobalState.getInstance().setAbortReadingHistory(false);
        }

        protected void finishRun() {
            // History reading has finished now, so cleanup anything at the end
            EobrService.this._isReadingHistory = false;

            // 2014.07.16 sjn - Reset this flag when history reading completes so that any subsequent called to ReadCurrent
            //                  will work properly.  This is important to get reset
            GlobalState.getInstance().setAbortReadingHistory(false);
        }

        protected void execute() { }

		public void run() {
            prepareToRun();
            execute();
            finishRun();
        }
	}

    private class MandateHistoryErrorAccumulator {
        private static final String TAG = "MandateHistErrorAccum";
        private EobrReader eobrReader = EobrReader.getInstance();

        private DateTime periodStart24hr;

        private ErrorAccumulator engineSyncErrors;
        private ErrorAccumulator gpsErrors;

        public MandateHistoryErrorAccumulator(DateTime periodStart24hr) {
            this.periodStart24hr = periodStart24hr;

            //reset the error accumulators since we're recalculating them
            engineSyncErrors = new ErrorAccumulator(periodStart24hr, new EventTypeEnum(EventTypeEnum.ERROR), new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)));
            gpsErrors = new ErrorAccumulator(periodStart24hr, new EventTypeEnum(EventTypeEnum.GPS), DataFlagEnums.GpsEventFlags.GPS_FAULT);

            GlobalState.getInstance().setEngineSyncErrors(engineSyncErrors);
            GlobalState.getInstance().setGpsErrors(gpsErrors);
        }

        public long readErrorData() {
            long refTimestamp = gpsErrors.getErrorAccumulatorReferenceTimeStamp();
            return readErrorData(refTimestamp, null);
        }

        public long readErrorData(Long refTimestamp, Long endRefTimestamp) {
            long lastErrorTimeStamp;

            do {
                // We need to hold onto the last timestamp we processed so we can do one last check for events in finishRun()
                lastErrorTimeStamp = refTimestamp;

                refTimestamp = readNextErrorRecord(refTimestamp);
            } while (refTimestamp > 0 && (endRefTimestamp == null || refTimestamp <= endRefTimestamp));

            return lastErrorTimeStamp;
        }

        private long readNextErrorRecord(long timecode) {
            long result = 0;

            if(!GlobalState.getInstance().getAbortReadingHistory()) {

                EobrResponse<EventRecord> response  = eobrReader.GetNextEvent(
                        timecode,
                        new EventTypeEnum(EventTypeEnum.ERROR),
                        new EventTypeEnum(EventTypeEnum.GPS)
                );

                EventRecord event = response.getData();

                if (response.getReturnCode() == EobrReturnCode.S_SUCCESS && event.getRecordId() != 0) {
                    try {
                        Log.v(TAG, String.format("Found event %s at %s - data %d",
                                new EventTypeEnum(event.getEventType()).toStringDisplay(EobrService.this),
                                event.getTimecodeAsDate(),
                                event.getEventData()
                        ));

                        switch(event.getEventType()) {
                            case EventTypeEnum.ERROR:
                                engineSyncErrors.processEvent(event);
                                break;
                            case EventTypeEnum.GPS:
                                gpsErrors.processEvent(event);
                                break;
                            default:
                                Log.e(TAG, String.format("Encountered unexpected event %d", event.getEventType()));
                        }

                        result = event.getTimecode() + 1;
                    } catch (Throwable e) {
                        // Log the error and then proceed with the rest of our reading history process
                        String eventType = new EventTypeEnum(event.getEventType()).toStringDisplay(getContext());
                        Log.e("UnhandledCatch", String.format("Failed to accumulate error time for event record type %s", eventType), e);
                    }
                }
            } else {
                // When history is aborted, change the EOBR status to ONLINE.
                // This is because the history process didn't fail due to anything wrong with the ELD.
                // The app aborted history reading, so the ELD should still be online.
                eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "readIgnitionData: history read aborted");
            }

            return result;
        }
    }

    private class MandateIgnitionEventBuilder {
        private EobrReader eobrReader = EobrReader.getInstance();

        static final int IGNITIONSTATE_UNKNOWN = -1;
        static final int IGNITIONSTATE_OFF = 0;
        static final int IGNITIONSTATE_ON = 1;
        protected int engineSecondsOffset = 0;
        int currentIgnitionState = IGNITIONSTATE_UNKNOWN;
        IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        public long readIgnitionData(Long startTimestamp, Long endTimestamp) {
            long lastIgnitionTimeStamp;
            long refTimestamp = startTimestamp;

            engineSecondsOffset = empLogController.getEngineSecondsOffset();

            do {
                // We need to hold onto the last timestamp we processed so we can do one last check for ignition events in finishRun()
                lastIgnitionTimeStamp = refTimestamp;

                refTimestamp = readNextIgnitionRecord(refTimestamp);
            } while (refTimestamp > 0 && (endTimestamp == null || refTimestamp <= endTimestamp));

            return lastIgnitionTimeStamp;
        }

        // This returns the next timecode to read if needed, otherwise 0 if we are done
        private long readNextIgnitionRecord(long timecode) {
            long result = 0;
            if(!GlobalState.getInstance().getAbortReadingHistory()) {

                EobrResponse<EventRecord> response  = eobrReader.GetNextEvent(timecode,
                        new EventTypeEnum(EventTypeEnum.IGNITIONON),
                        new EventTypeEnum(EventTypeEnum.IGNITIONOFF),
                        new EventTypeEnum(EventTypeEnum.TABRESET)
                );

                EventRecord ignEvent = response.getData();

                if (response.getReturnCode() == EobrReturnCode.S_SUCCESS && ignEvent.getRecordId() != 0) {
                    try {

                        if(ignEvent.getEventType() == EventTypeEnum.TABRESET){
                            // need to make CMD call to retrieve status record in case of RESET
                            ignEvent.setStatusRecordData(GetStatusRecordForEventRecord(eobrReader, ignEvent));
                        }else{
                            // no additional CMD call needed to get status record info
                            StatusRecord statusRecord = new StatusRecord();
                            statusRecord.setGpsLatitude(ignEvent.getTripReportData().getLatitude());
                            statusRecord.setGpsLongitude(ignEvent.getTripReportData().getLongitude());
                            statusRecord.setGpsTimestampUtc(ignEvent.getTripReportData().getTimecodeAsDate());
                            statusRecord.setOdometerReading(ignEvent.getTripReportData().getOdometer());
                            statusRecord.setGpsUncertDistance(ignEvent.getTripReportData().getFixUncert());
                            if(ignEvent.getTripReportData().getIgnition() > 0)
                                statusRecord.setIsEngineOn(true);
                            ignEvent.setStatusRecordData(statusRecord);
                        }
                        LogGetStatusRecordResult("readIgnitionData", ignEvent);

                        switch (ignEvent.getEventType()) {
                            case EventTypeEnum.IGNITIONON:
                                currentIgnitionState = IGNITIONSTATE_ON;
                                createIgnitionEvent(ignEvent, CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent, engineSecondsOffset);
                                break;
                            case EventTypeEnum.TABRESET:
                                if (currentIgnitionState == IGNITIONSTATE_UNKNOWN) {
                                    currentIgnitionState = ignEvent.getStatusRecordData() != null && ignEvent.getStatusRecordData().getIsEngineOn() ? IGNITIONSTATE_ON : IGNITIONSTATE_OFF;
                                }

                                if (currentIgnitionState == IGNITIONSTATE_ON) {
                                    currentIgnitionState = IGNITIONSTATE_OFF;
                                    createIgnitionEvent(ignEvent, CompositeEmployeeLogEldEventTypeEventCodeEnum.EngineShutDownEvent, engineSecondsOffset);
                                }
                                break;
                            case EventTypeEnum.IGNITIONOFF:
                                currentIgnitionState = IGNITIONSTATE_OFF;
                                createIgnitionEvent(ignEvent, CompositeEmployeeLogEldEventTypeEventCodeEnum.EngineShutDownEvent, engineSecondsOffset);
                                break;
                        }
                        result = ignEvent.getTimecode() + 1;
                    } catch (Throwable e) {
                        // Log the error and then proceed with the rest of our reading history process
                        String eventType = new EventTypeEnum(ignEvent.getEventType()).toStringDisplay(getContext());
                        Log.e("UnhandledCatch", String.format("Failed to create an Ignition ELD event for event record type %s", eventType), e);
                    }
                }
            } else {
                // When history is aborted, change the EOBR status to ONLINE.
                // This is because the history process didn't fail due to anything wrong with the ELD.
                // The app aborted history reading, so the ELD should still be online.
                eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "readIgnitionData: history read aborted");
            }
            return result;
        }

        // We're checking for duplicates before actually creating events, just in case reading history had previously failed part way through
        private void createIgnitionEvent(EventRecord event, CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode, int offset) throws Throwable {
            if (!empLogController.IsDuplicateEnginePowerUpOrShutDownUnassignedEvent(event, eventTypeAndCode)) {
                EmployeeLog empLog =  GlobalState.getInstance().getCurrentDriversLog();
                if(event.getDriverId() > 0){
                    // IGN event occurred after login, during reading history
                    empLogController.CreateEnginePowerUpOrShutDownEvent(empLog, event, eventTypeAndCode);
                }else{
                    empLogController.CreateEnginePowerUpOrShutDownUnassignedEvent(event, eventTypeAndCode, offset);
                }
            }
        }
    }

    private class ReadMandateAndDriveDataThread extends ReadDriveDataThread {
        protected Long lastReadIgnitionRefTimestamp = null;
        protected Long lastReadErrorRefTimestamp = null;

        MandateHistoryErrorAccumulator acc;
        MandateIgnitionEventBuilder ign;


        public ReadMandateAndDriveDataThread(EobrReader eobrReader) {
            super(eobrReader);

            ign = new MandateIgnitionEventBuilder();

            Date logDate = GlobalState.getInstance().getCurrentEmployeeLog().getLogDate();
            String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(EobrService.this).getDailyLogStartTime();
            TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
            Date logStart = EmployeeLogUtilities.CalculateLogStartTime(dailyLogStart, logDate, timeZone);

            acc = new MandateHistoryErrorAccumulator(new DateTime(logStart));
        }

        @Override
        protected void execute() {
            Bundle timestamps = eobrReader.Technician_ReadReferenceTimestamp();
            long refTimestamp = timestamps.getLong(EobrService.this.getString(R.string.eventreftime)) + 1;
            lastReadIgnitionRefTimestamp = ign.readIgnitionData(refTimestamp, null);

            lastReadErrorRefTimestamp = acc.readErrorData();

            super.execute();
        }

        @Override
        protected void finishRun() {
            if(!GlobalState.getInstance().getAbortReadingHistory() && lastReadIgnitionRefTimestamp < endRefTimestamp) {
                ign.readIgnitionData(lastReadIgnitionRefTimestamp, endRefTimestamp);
            }

            //see if any error events were logged while reading drive data
            if(!GlobalState.getInstance().getAbortReadingHistory() && lastReadErrorRefTimestamp < endRefTimestamp) {
                acc.readErrorData(lastReadErrorRefTimestamp, endRefTimestamp);
            }

            Duration engineSyncErrors = GlobalState.getInstance().getEngineSyncErrors().getAccumulation();
            Duration gpsErrors = GlobalState.getInstance().getGpsErrors().getAccumulation();

            String message = String.format(Locale.getDefault(),
                    "Detected %d seconds accumulated engine sync error time and %d seconds accumulated GPS error time.",
                    engineSyncErrors.getStandardSeconds(),
                    gpsErrors.getStandardSeconds()
            );

            Log.d("EobrService", message);
            ErrorLogHelper.RecordMessage(message);

            super.finishRun();
        }
    }

	private class ReadDriveDataThread extends BaseReadHistoryThread {
		RouteController routeController = new RouteController(EobrService.this);
		UnassignedPeriodController udpController = ControllerFactory.getInstance().getUnassignedPeriodController();
		AutoAssignUnassignedEventsController aaueController = new AutoAssignUnassignedEventsController(EobrService.this, new AutoAssignedELDCalls());
		
		List<UnassignedDrivingPeriod> periods = new ArrayList<UnassignedDrivingPeriod>();
		UnassignedDrivingPeriodResult periodResult = new UnassignedDrivingPeriodResult();

        protected Long startRefTimestamp = null;
        protected Long endRefTimestamp = null;
		
		public ReadDriveDataThread(EobrReader eobrReader)
		{
			super(eobrReader);
		}
		
		@Override
		protected void execute() {
			Bundle timestamps = eobrReader.Technician_ReadReferenceTimestamp();
			long refTimestamp = timestamps.getLong(EobrService.this.getString(R.string.eventreftime)) + 1;
			startRefTimestamp = refTimestamp;

			do {
                endRefTimestamp = refTimestamp;

				refTimestamp = readDriveData(refTimestamp);
			} while(refTimestamp != 0);
		}

		// This used to be recursive, but ran into StackOverflow errors due to the volume
		// of periods we're reading.  Now it returns the next timecode to read.
        private long readDriveData(long timecode) {

            boolean isGeotab = GlobalState.getInstance().getCompanyConfigSettings(getContext()).getIsGeotabEnabled();

            if (GlobalState.getInstance().getAbortReadingHistory() || isGeotab) {
                // When history is aborted or we are using a Geotab device, change the EOBR status to ONLINE.
                // This is because the history process didn't fail due to anything wrong with the ELD.
                // The app aborted history reading, so the ELD should still be online.

                // trigger unassignedDrivingEvent handler
                if (isGeotab) eobrReader.PublishUnassignedDrivingPeriods(periods, periodResult);

                eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "ReadDriveData: history read aborted");
                return 0;
            }

            //route position interval argument is in seconds, getRecordingInterval returns minutes
            EobrResponse<DriveData> result = eobrReader.GetDrivingPeriod(timecode, (short) (routeController.getRecordingInterval() * 60), Constants.KM_TO_METERS);

            if (result.getReturnCode() == EobrReturnCode.S_SUCCESS) {
                boolean done = false;
                DriveData driveData = result.getData();
                List<VehicleLocation> locations = driveData.getVehicleLocations();

                /* There are several different ways we'll update the reference timestamp.
                 * 1) If we know we have more history to read, we'll set it to the time of
                 * 		the DRIVE_OFF we just read.
                 * 2) If we just read an orphaned driving period, we'll set it to the time
                 * 		of the DRIVE_ON.
                 * 3) If we just read a full period but we know we're done reading history,
                 * 		we'll get the time of the last DRIVER event.  We'll set the timestamp
                 * 		to the most recent of either the DRIVER event or the DRIVE_OFF.
                 * 		Details: If the last driving period ended several days ago, we don't
                 * 		want all of the events that have occurred since then to be read by
                 * 		the current driver (could be idle time, etc).  If the TAB hasn't
                 * 		been driven in several weeks it could take time to get current.
                 */
                long refTimestamp = 0;

                if (locations != null && locations.size() > 0) {
                    UnassignedDrivingPeriodResult udpResult = udpController.ProcessHistoricalDrivingData(locations, periods);

                    periodResult.setDetectedAnyDrivingPeriods(periodResult.getDetectedAnyDrivingPeriods() || udpResult.getDetectedAnyDrivingPeriods());
                    periodResult.setUnassignedPeriod(udpResult.getUnassignedPeriod());

                    if (periodResult.getOrphanedTripTime() != null) {
                        refTimestamp = udpResult.getOrphanedTripTime().getTime();

                        periodResult.setOrphanedTripTime(udpResult.getOrphanedTripTime());
                        periodResult.setOrphanedOdometer(udpResult.getOrphanedOdometer());
                        periodResult.setOrphanedLatitude(udpResult.getOrphanedLatitude());
                        periodResult.setOrphanedLongitude(udpResult.getOrphanedLongitude());
                    } else
                        refTimestamp = driveData.getDriveOffTimecode();

                    //if we detected any full driving periods, search for more
                    //an orphaned period indicates the user is currently driving,
                    //so we're done
                    if (udpResult.getDetectedAnyDrivingPeriods())
                        timecode = driveData.getDriveOffTimecode() + 1;
                    else
                        done = true;
                } else
                    done = true;

                if (done) {
                    EventRecord driverEvent = new EventRecord();

                    int returnCode = eobrReader.GetLastEventOfType(driverEvent, new EventTypeEnum(EventTypeEnum.DRIVER));
                    if (returnCode == EobrReturnCode.S_SUCCESS) {
                        Bundle referenceTimeStamps = eobrReader.Technician_ReadReferenceTimestamp();

                        long eobrReferenceTime = referenceTimeStamps.getLong(EobrService.this.getString(R.string.eobrreftime));
                        long eventReferenceTime = referenceTimeStamps.getLong(EobrService.this.getString(R.string.eventreftime));
                        long tripReferenceTime = referenceTimeStamps.getLong(EobrService.this.getString(R.string.tripreftime));

                        //If the app were to crash this check was added to check the ELD times
                        //against the device log and compare times.  This check is to prevent times
                        //from going back in time and over writing log events.
                        if (eobrReferenceTime < driverEvent.getTimecode() &&
                                eventReferenceTime < driverEvent.getTimecode() &&
                                tripReferenceTime < driverEvent.getTimecode()) {
                            //if the driver event is more recent than the reference timestamp
                            //from the DRIVE_OFF then set the timestamp to the drive event
                            //(if the refTimestamp comes from an orphaned period, this will
                            //always evaluate to false
                            if (driverEvent.getTimecode() > refTimestamp)
                                refTimestamp = driverEvent.getTimecode();
                        } else {
                            //Since the ELD time(s) are less than the driverEvent.timecode
                            //we want to set the refTimestamp to the ELD last event time
                            refTimestamp = eventReferenceTime;
                        }
                    } else
                        ErrorLogHelper.RecordMessage(String.format("ReadDriveData failed to get last driver event with code %d", returnCode));

                    setReferenceTimestamps(refTimestamp);
                    //aaueController.AutoAssignUnassignedEvents();

                    setReferenceTimestamps(refTimestamp);

                    if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && GlobalState.getInstance().getCompanyConfigSettings(EobrService.this).getIsAutoAssignUnIdentifiedEvents()){
                        aaueController.AutoAssignUnassignedEvents();
                    }

                    //merge unassigned periods, check for periods that could impact today's log, autoclaim, etc
                    UnassignedDrivingPeriodResult processedResult = udpController.ProcessUnassignedDrivingPeriods(periods);
                    periodResult.setDetectedDrivingPeriodsForCurrentLog(processedResult.getDetectedDrivingPeriodsForCurrentLog());
                    periodResult.setDetectedPreloginDrivingPeriods(processedResult.getDetectedPreloginDrivingPeriods());

                    eobrReader.PublishUnassignedDrivingPeriods(periods, periodResult);

                    eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "ReadAndPublishHistoricalGen2Records: back online");

                    ReadAndDiscardHistogramAndDtcData();

                    return 0;
                } else {
                    setReferenceTimestamps(refTimestamp);

                    return timecode;
                }
            } else {
                // Read failed, or some other problem, so go offline.
                eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.OFFLINE, String.format("ReadDriveData: GetDrivingData failed with return code '%s'", result.getReturnCode()));

                //TODO: Is this needed?
                EventRecord dummyEvent = new EventRecord();
                dummyEvent.setTimecode(TimeKeeper.getInstance().now().getTime());
                eobrReader.PublishEobrStatusChange(result.getReturnCode(), dummyEvent);

                return 0;
            }
        }

        private void setReferenceTimestamps(long refTimestamp) {
			if(refTimestamp != 0)
			{
				Log.d("ReadDriveData", String.format("Reading history setting timestamps to %d", refTimestamp));
				
				EobrReferenceTimestamps timestamps = new EobrReferenceTimestamps();
				timestamps.setEobrReferenceTime(refTimestamp);
				timestamps.setEventReferenceTime(refTimestamp);
				timestamps.setTripReferenceTime(refTimestamp);
				timestamps.setDtcReferenceTime(EobrReferenceTimestamps.REFERENCE_TIMESTAMP_DO_NOT_SET);
				timestamps.setHistogramReferenceTime(EobrReferenceTimestamps.REFERENCE_TIMESTAMP_DO_NOT_SET);

				int returnCode = eobrReader.SetReferenceTimestamps(timestamps);

				if(returnCode != EobrReturnCode.S_SUCCESS) {
					ErrorLogHelper.RecordMessage(String.format("ReadDriveData failed to set reference timestamps to %d with code %d", refTimestamp, returnCode));
				}
			}
		}
	}

	private class ReadHistoryThread extends BaseReadHistoryThread {
		public ReadHistoryThread(EobrReader eobrReader)
		{
			super(eobrReader);
		}

        @Override
		protected void execute() {
			try {
				ArrayList<EventRecord> currentEvents = new ArrayList<EventRecord>();
				ArrayList<TripReport> currentTrips = new ArrayList<TripReport>();

				// This will get any data that exists since the last reference timestamp.
				int rc = ReadRecordsToCurrent(eobrReader, currentEvents, currentTrips);
				if (rc == EobrReturnCode.S_SUCCESS)
				{
					// Successful read from the EOBR. In order to process Unassigned Driving Periods, the Events ArrayList must be populated.
					if (currentEvents.size() > 0)
					{
						eobrReader.PublishEobrHistoricalRecordsGenII(currentEvents);
					}

					// Now that the events have been processed, update the reference timestamps
					updateEventAndTripReferenceTimestamps(currentEvents, currentTrips);

					eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "ReadAndPublishHistoricalGen2Records: back online");

					ReadAndDiscardHistogramAndDtcData();
				}
				else if(rc == EobrReturnCode.S_ABORTED)
				{
					// 2014.07.16 sjn - When history is aborted, change the EOBR status to ONLINE.
					//                  This is because the history process didn't fail due to anything wrong with the ELD.
					//                  The app aborted history reading, so the ELD should still be online.
					eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.ONLINE, "ReadAndPublishHistoricalGen2Records: history read aborted");
				}
				else
				{
					// Read failed, or some other problem, so go offline.
					eobrReader.TransitionDeviceToNewState(EobrService.this, ConnectionState.OFFLINE, String.format("ReadAndPublishHistoricalGen2Records: GetCurrentData() bad return code '%s'", rc));

					// Publish the failure, along with the timestamp of the failure.
					if (currentEvents != null && currentEvents.size() > 0)
						eobrReader.PublishEobrStatusChange(rc, currentEvents.get(0));
					else
					{
						// 10/12/12 JHM - Not sure if this is a possible scenario, but if the event collection is empty, set the time as now.
						EventRecord dummyEvent = new EventRecord();
						dummyEvent.setTimecode(TimeKeeper.getInstance().now().getTime());
						eobrReader.PublishEobrStatusChange(rc, dummyEvent);
					}
				}
			}
			catch (Exception e)
			{
				Log.e("ReadHistoryThread", "ReadHistoryThread failed", e);
				EobrService.this._isReadingHistory = false;
			}
		}

		private void updateEventAndTripReferenceTimestamps(ArrayList<EventRecord> currentEvents, ArrayList<TripReport> currentTrips) {
			final StatusRecordQueryMethodEnum statusQueryMethodByRecordId = new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID);
			
			// Update the event reference timestamp
			if (!currentEvents.isEmpty())
			{
				// Find the latest record out of the events. There could be an hourly trip record at the end, which we want to ignore.
				int latestRecordId = 0;
				for (int i = currentEvents.size() - 1; i >= 0; i--)
				{
					EventRecord record = currentEvents.get(i);
					if (record.getRecordId() > 0 && record.getEventType() != EventTypeEnum.HOURLYTRIPRECORD)
					{
						latestRecordId = record.getRecordId();
						break;
					}
				}
				
				if (latestRecordId > 0)
				{
					EventRecord eventData = new EventRecord();
					eventData.setRecordId(latestRecordId);
					eobrReader.Technician_GetEventData(eventData, statusQueryMethodByRecordId, new EventTypeEnum(EventTypeEnum.ANYTYPE), 0, true);
				}
			}
			
			// Update the trip reference timestamp
			if (!currentTrips.isEmpty())
			{
				int latestRecordId = currentTrips.get(currentTrips.size() - 1).getRecordId();
				TripReport tripData = new TripReport();
				tripData.setRecordId(latestRecordId);
				eobrReader.Technician_GetTripData(tripData, statusQueryMethodByRecordId, 0, true);
			}
		}
	}

	/**
	 * Lazy load BT disconnect manager for Mandate.
	 *
	 * Management of the bluetooth connection between the KMB app and the EOBR device.
	 */
	public IBluetoothDrivingManager getBluetoothDrivingManager() {
		if (_bluetoothDrivingManager == null) {
			_bluetoothDrivingManager = new BluetoothDrivingManagerMandate();
		}

		return _bluetoothDrivingManager;
	}

    public void logState(){
        Log.v("EobrService", String.format("" +
                "State:\n" +
                "timerCreated: %s\n" +
                "timerSuspended: %s\n" +
                "isReadingHistory: %s\n" +
                "isFinishingFirmwareUpdate: %s\n" +
                "ignoreResume: %s\n" +
                "needToUpdateFirmwareOnNextTimerPop: %s\n" +
                "needToReadHistoryOnNextTimerPop: %s\n" +
                "currentConnectionState: %s",
                _timerCreated,
                _timerSuspended,
                _isReadingHistory,
                _isFinishingFirmwareUpdate,
                _ignoreResume,
                _needToUpdateFirmwareOnNextTimerPop,
                _needToReadHistoryOnNextTimerPop,
                EobrReader.getInstance().getCurrentConnectionState()
                ));
    }

	/*================================*/
	/* Extra methods for IEobrService */
	/*================================*/
	
	public Context getContext()
	{
		return this;
	}

	public void setIsFinishingFirmwareUpdate(boolean isFinishingFirmwareUpdate) {
		_isFinishingFirmwareUpdate = isFinishingFirmwareUpdate;
	}

    public void ignoreNextDefaultDriverEvent() {
        _ignoreNextDefaultDriverEvent = true;
    }
}
