package com.jjkeller.kmbapi.configuration;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.VehicleMotionDetector;
import com.jjkeller.kmbapi.controller.dataaccess.CompanyFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.eobrengine.EobrServiceBase;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GlobalState extends Application {

	public static final float MilesToKilometers = 1.609344F;
	public static final String MSDecimalSerializationMinValue = "-79228162514264337593543950335";
    /// <summary>
    /// Name of the directory where the old error logs are copied
    /// </summary>
	public static final String TEMP_DATA_DIRECTORY = "Temp";
	public static final String FIRMWARE_IMAGE_DIRECTORY = "fwImages";

	protected static final String TAG = "BTDisconnect";
	private WeakReference<Activity> mActiveActivity = null;
	private int mActiveActivitiesRefCount = 0;

	protected static GlobalState singleton;
	private static boolean isApplicationRunning;

    public static GlobalState getInstance(){
		if (singleton == null)
			singleton = new GlobalState();
		
		return singleton;
	}

	private static Context kmbContext;
	
	@Override
	public final void onCreate(){
		super.onCreate();		
		singleton = this;
		kmbContext = this;

		// use ActivityLifecycleCallbacks to determine if KMB Application is in Background or Foreground
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

			//	The key is in understanding how activities coordinate with each other. When switching between activities A and B, their methods are called in this order:
			//	A.onPause();
			//	B.onCreate();
			//	B.onStart();
			//	B.onResume(); (Activity B now has user focus)
			//	A.onStop(); (if Activity A is no longer visible on screen)

			@Override
			public void onActivityCreated(Activity activity, Bundle bundle) { }

			@Override
			public void onActivityStarted(Activity activity) { }

			@Override
			public void onActivityResumed(Activity activity) {
				++mActiveActivitiesRefCount;

				if (mActiveActivity != null) {
					mActiveActivity.clear();
				}
				//At least one activity is alive, our application is running
				if (!isApplicationRunning){
					isApplicationRunning = true;
				}

				mActiveActivity = new WeakReference<Activity>(activity);

				//Log.d(TAG, "onActivityResumed " + String.valueOf(mActiveActivitiesRefCount) + "  " + activity.getLocalClassName());
			}

			@Override
			public void onActivityPaused(Activity activity) { }

			@Override
			public void onActivityStopped(Activity activity) {
				--mActiveActivitiesRefCount;
				//No more activities are alive, it means our application has been closed or destroyed
				if (mActiveActivitiesRefCount == 0){
					isApplicationRunning = false;
				}
				//Log.d(TAG, "onActivityStopped " + String.valueOf(mActiveActivitiesRefCount) + "  " + activity.getLocalClassName());
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle bundle) { }

			@Override
			public void onActivityDestroyed(Activity activity) { }
		});
	}

	public static Context getContext(){
		return kmbContext;
	}
	
	@Override
	public final void onTerminate(){
		super.onTerminate();

		if (mActiveActivity != null) {
			mActiveActivity.clear();
			mActiveActivity = null;
		}

		singleton = null;
	}
	
	public void tearDown()
	{
		if (mActiveActivity != null) {
			mActiveActivity.clear();
			mActiveActivity = null;
		}

		singleton = null;
	}

	public static boolean isApplicationRunning(){
		return isApplicationRunning;
	}

	private AppSettings _appSettings = null;
	public AppSettings getAppSettings(Context ctx)
	{
		if(_appSettings == null)
		{
			_appSettings = new AppSettings();
			AppSettings.context = this;
			_appSettings.loadAppSettings(ctx, R.raw.app_config);
			_appSettings.loadFirmwareUpdates(ctx, R.raw.firmware_updates);
		}
		return _appSettings;
	}

	/**
	 * Used for providing mocked AppSettings for Unit Testing
	 * @param appSettings Mocked up AppSettings
	 */
	public void setAppSettings(AppSettings appSettings) {
		_appSettings = appSettings;
	}

	protected CompanyConfigSettings _companyConfigSettings = null;
	public CompanyConfigSettings getCompanyConfigSettings(Context ctx)
	{
		if (_companyConfigSettings == null)
		{
			CompanyFacade companyFacade = new CompanyFacade(ctx);
			_companyConfigSettings = companyFacade.Fetch();
		}
		return _companyConfigSettings;
	}
	
	public void setCompanyConfigSettings(Context ctx, CompanyConfigSettings config)
	{
		_companyConfigSettings = config;
		if (_companyConfigSettings != null)
		{
			CompanyFacade companyFacade = new CompanyFacade(ctx);
			companyFacade.Save(_companyConfigSettings);			
		}
	}

	private IFeatureToggleService _featureToggleService = null;
    public IFeatureToggleService getFeatureService()
    {
        if(_featureToggleService == null)
        {
            _featureToggleService = new FeatureToggleService(_appSettings);
        }

        return _featureToggleService;
    }

    /**
     * Used for providing mocked FeatureToggle service for Unit Testing
     * @param service Mocked up FeatureToggleService
     */
    public void setfeatureToggleService(IFeatureToggleService service){
        _featureToggleService = service;
    }

	// State data - currently storing/accessing state data from this global
	// application object - this is maintained across different activities
	private User _user = null;
	public User getCurrentUser()
	{
		return this._user;
	}
	public void setCurrentUser(User user)
	{
		this._user = user;
	}
	
    private User _currentDesignatedDriver = null;
	public User getCurrentDesignatedDriver()
	{
		return this._currentDesignatedDriver;
	}
	public void setCurrentDesignatedDriver(User user)
	{
		this._currentDesignatedDriver = user;
	}

	private ArrayList<User> loggedInUserList = new ArrayList<User>();
	public ArrayList<User> getLoggedInUserList(){
		return this.loggedInUserList;
	}
	public void setLoggedInUserList(ArrayList<User> loggedInUserList){
		this.loggedInUserList = loggedInUserList;
	}
	
	public User getLoggedInUser(String employeeId)
	{
		User user = null;
		
		for(User loggedInUser : loggedInUserList)
		{
			if(loggedInUser.getCredentials().getEmployeeId().equalsIgnoreCase(employeeId))
			{
				user = loggedInUser;
				break;
			}
		}
		
		return user;
	}
	
	private EmployeeLog _currentEmployeeLog = null;
	public EmployeeLog getCurrentEmployeeLog()
	{
		return this._currentEmployeeLog;
	}
	
	public void setCurrentEmployeeLog(EmployeeLog log)
	{
		this._currentEmployeeLog = log;
	}
	
	private EmployeeLog _currentDriversLog = null;
	public EmployeeLog getCurrentDriversLog()
	{
		return this._currentDriversLog;
	}


	public void setCurrentDriversLog(EmployeeLog log)
	{
		this._currentDriversLog = log;
	}

    private String _currentEobrSerialNumber;
    public String getCurrentEobrSerialNumber(){
        return _currentEobrSerialNumber;
    }

    public void setCurrentEobrSerialNumber(String serialNumber){
        _currentEobrSerialNumber=serialNumber;
    }

	private EmployeeLog _selectedLogForReport = null;
	public EmployeeLog getSelectedLogForReport()
	{
		return _selectedLogForReport;
	}
	public void setSelectedLogForReport(EmployeeLog log)
	{
		this._selectedLogForReport = log;
	}
	
	private GpsLocation _lastGPSLocation = null;
	public GpsLocation getLastGPSLocation()
	{
		return this._lastGPSLocation;
	}
	
	public void setLastGPSLocation(GpsLocation gpsLocation)
	{
		this._lastGPSLocation = gpsLocation;
	}

	private Location _lastLocation = null;
	public Location getLastLocation() { return this._lastLocation; }
	public void setLastLocation(Location location) { this._lastLocation = location; }

	private float _lastValidOdometerReading = 0;
    public float getLastValidOdometerReading()
    {
        // read from state
        return _lastValidOdometerReading;
    }
    public void setLastValidOdometerReading(float value)
    {
        // save to state, as long as it's real
        if (value > 0)
        {
        	_lastValidOdometerReading = value;
        }
    }

	private boolean _offDutyMsgCloseBtnPressed = false;
	public boolean getOffDutyMsgCloseBtnPressed() { return _offDutyMsgCloseBtnPressed; }

	public void setOffDutyMsgCloseBtnPressed(boolean offDutyMsgCloseBtnPressed){ _offDutyMsgCloseBtnPressed=offDutyMsgCloseBtnPressed; }

	private boolean _hasCertifyLogsDialogBeenDisplayedOnceOnRODS = false;
    public boolean getHasCertifyLogsDialogBeenDisplayedOnceOnRODS() { return _hasCertifyLogsDialogBeenDisplayedOnceOnRODS; }
    public void setHasCertifyLogsDialogBeenDisplayedOnceOnRODS(boolean hasBeenDisplayedOnceOnRODS) { _hasCertifyLogsDialogBeenDisplayedOnceOnRODS = hasBeenDisplayedOnceOnRODS; }

    private boolean _hasReviewLogEditsDialogBeenDisplayedOnceOnRODS = false;
    public boolean getReviewLogEditsDialogBeenDisplayedOnceOnRODS() { return _hasReviewLogEditsDialogBeenDisplayedOnceOnRODS; }
    public void setReviewLogEditsDialogBeenDisplayedOnceOnRODS(boolean hasBeenDisplayedOnceOnRODS) { _hasReviewLogEditsDialogBeenDisplayedOnceOnRODS = hasBeenDisplayedOnceOnRODS; }

	private boolean _hasReviewUDPDialogBeenDisplayedOnceOnRODS = false;
	public boolean getReviewUDPDialogBeenDisplayedOnceOnRODS() { return _hasReviewUDPDialogBeenDisplayedOnceOnRODS ; }
	public void setReviewUDPDialogBeenDisplayedOnceOnRODS(boolean hasBeenDisplayedOnceOnRODS) { _hasReviewUDPDialogBeenDisplayedOnceOnRODS  = hasBeenDisplayedOnceOnRODS; }


    private boolean _isNewUserLogin = false;
    public boolean getIsNewUserLogin()
    {
    	return _isNewUserLogin;
    }
    public void setIsNewUserLogin(boolean isNewUserLogin)
    {
    	_isNewUserLogin = isNewUserLogin;
    }
    
    private KmbUserInfo _kmbUserInfo = null;
    public KmbUserInfo getKmbUserInfo()
    {
    	return this._kmbUserInfo;
    }
    public void setKmbUserInfo(KmbUserInfo kmbUserInfo)
    {
    	this._kmbUserInfo = kmbUserInfo;
    }
    
    private boolean _roadsideInspectionMode;
    public boolean getRoadsideInspectionMode()
    {
    	return _roadsideInspectionMode;
    }
    public void setRoadsideInspectionMode(boolean roadsideInspectionMode)
    {
    	this._roadsideInspectionMode = roadsideInspectionMode;
    }
    
    private EmployeeLogEldEvent _logEventForEdit;
    public EmployeeLogEldEvent getLogEventForEdit()
    {
    	return _logEventForEdit;
    }
    public void setLogEventForEdit(EmployeeLogEldEvent event)
    {
    	this._logEventForEdit = event;
    }
    
    private boolean _isExtendedDrivingSegmentEnabled;
    public boolean isExtendDrivingSegmentEnabled()
    {
    	return _isExtendedDrivingSegmentEnabled;
    }
    public void isExtendDrivingSegmentEnabled(boolean isExtendedDrivingSegmentEnabled)
    {
    	_isExtendedDrivingSegmentEnabled = isExtendedDrivingSegmentEnabled;
    }
    
    
    private LocationCodeDictionary _locationCodeDictionary = null;
    public LocationCodeDictionary getLocationCodeDictionary()
    {
    	return _locationCodeDictionary;
    }
    
    public void setLocationCodeDictionary(LocationCodeDictionary locationCodeDictionary)
    {
    	this._locationCodeDictionary = locationCodeDictionary;
    }

    private boolean _isExtendDrivingSegmentEnabled = false;
    public boolean getIsExtendDrivingSegmentEnabled()
    {
    	return this._isExtendDrivingSegmentEnabled;
    }
    public void setIsExtendDrivingSegmentEnabled(boolean isExtendDrivingSegmentEnabled)
    {
    	this._isExtendDrivingSegmentEnabled = isExtendDrivingSegmentEnabled;
    }

    private boolean _hasExtendedDrivingSegment = false;
    public boolean getHasExtendedDrivingSegment()
    {
    	return this._hasExtendedDrivingSegment;
    }
    public void setHasExtendedDrivingSegment(boolean hasExtendedDrivingSegment)
    {
    	this._hasExtendedDrivingSegment = hasExtendedDrivingSegment;
    }
    
    private boolean _isMobileClockSynchronized = false;
    public boolean getIsMobileClockSynchronized()
    {
    	return this._isMobileClockSynchronized;
    }
    public void setIsMobileClockSynchronized(boolean isMobileClockSynchronized)
    {
    	this._isMobileClockSynchronized = isMobileClockSynchronized;
    }

    private EobrConfiguration _currentEobrConfiguration = null;
    public EobrConfiguration getCurrentEobrConfiguration() {
		return _currentEobrConfiguration;
	}
	public void setCurrentEobrConfiguration(EobrConfiguration currentEobrConfiguration) {
		this._currentEobrConfiguration = currentEobrConfiguration;
	}

	private StatusRecord _previousEobrStatusRecord = null;
	public StatusRecord getPreviousEobrStatusRecord() {
		return _previousEobrStatusRecord;
	}
	public void setPreviousEobrStatusRecord(StatusRecord previousEobrStatusRecord) {
		this._previousEobrStatusRecord = previousEobrStatusRecord;
	}

	private EventRecord _previousEobrEventRecord = null;
	public EventRecord getPreviousEobrEventRecord() {
		return _previousEobrEventRecord;
	}
	public void setPreviousEobrEventRecord(EventRecord previousEobrEventRecord) {
		this._previousEobrEventRecord = previousEobrEventRecord;
	}
	
	private Date _nextEngineRecordingTimestamp = null;
	public Date getNextEngineRecordingTimestamp() {
		return this._nextEngineRecordingTimestamp;
	}
	
	public void setNextEngineRecordingTimestamp(Date nextEngineRecordingTimestamp) {
		this._nextEngineRecordingTimestamp = nextEngineRecordingTimestamp;
	}

	private Date _nextEobrReaderEventProcessingTimestamp = null;
	public Date getNextEobrReaderEventProcessingTimestamp() {
		return this._nextEobrReaderEventProcessingTimestamp;
	}
	
	public void setNextEobrReaderEventProcessingTimestamp(Date nextEobrReaderEventProcessingTimestamp) {
		this._nextEobrReaderEventProcessingTimestamp = nextEobrReaderEventProcessingTimestamp;
	}
	
	private Date _lastTimerPop = null;
	public Date getLastTimerPop() {
		return this._lastTimerPop;
	}
	public void setLastTimerPop(Date lastTimerPop) {
		this._lastTimerPop = lastTimerPop;
	}
	
	private Date _daylightSavingPeriodStart = null;
    private Date _daylightSavingPeriodEnd = null;
    public Date getDaylightSavingPeriodStart()
    {
    	if(_daylightSavingPeriodStart == null)
    		initializeDaylightSavingPeriod();
    	return _daylightSavingPeriodStart;
    }
    public Date getDaylightSavingPeriodEnd()
    {
    	if(_daylightSavingPeriodEnd == null)
    		initializeDaylightSavingPeriod();
    	return _daylightSavingPeriodEnd;
    }
    public int getDaylightSavingsPeriodDelta()
    {
    	return 1;
    }
    /// <summary>
    /// Answer the U.S. Daylight Saving time period
    /// </summary>
    private void initializeDaylightSavingPeriod()

    {
            // fetch the daylight saving period from the OS
            Calendar cal = Calendar.getInstance();

            // if the current timezone configured for the OS clock
            // is not set to a timezone that observes DST, then calc
            // the DST period manually
            // note: this will occur if configured to use "GMT-7 Arizona"

            // calc the start date of DST
            Calendar startDate = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) < 2007)
            {
                // 1st Sunday in April                        
                startDate.set(cal.get(Calendar.YEAR), Calendar.APRIL, 1, 2, 0);
            }
            else
            {
                // 2nd Sunday in March starting in 2007                        
                startDate.set(cal.get(Calendar.YEAR), Calendar.MARCH, 8, 2, 0);
            }

            while (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            {
                // move forward to find the next Sunday
            	startDate.add(Calendar.DATE, 1);
            }

            // end date is 1st Sunday in November starting in 2007
            Calendar endDate = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) < 2007)
            {
                // last Sunday in October                        
                endDate.set(cal.get(Calendar.YEAR), Calendar.OCTOBER, 25, 2, 0);
            }
            else
            {
                // 1st Sunday in November starting in 2007                        
                endDate.set(cal.get(Calendar.YEAR), Calendar.NOVEMBER, 1, 2, 0);
            }

            while (endDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            {
                // move forward to find the next Sunday
            	endDate.add(Calendar.DATE, 1);
            }

            _daylightSavingPeriodStart = startDate.getTime();
            _daylightSavingPeriodEnd = endDate.getTime();
    }

    private List<FailureReport> _recentFailureReportList = null;
    public List<FailureReport> getRecentFailureReportList()
    {
    	return _recentFailureReportList;
    }
    public void setRecentFailureReportList(List<FailureReport> list)
    {
    	_recentFailureReportList = list;
    }

    private Date _nextRoutingTimestamp = null;
    public Date getNextRoutingTimestamp()
    {
    	return this._nextRoutingTimestamp;
    }
    public void setNextRoutingTimestamp(Date nextRoutingTimestamp)
    {
    	this._nextRoutingTimestamp = nextRoutingTimestamp;
    }
    
    private Date _nextMapPositionTimestamp = null;
    public Date getNextMapPositionTimeStamp()
    {
    	return this._nextMapPositionTimestamp;
    }
    public void setNextMapPositionTimestamp(Date nextMapPostionTimestamp)
    {
    	this._nextMapPositionTimestamp = nextMapPostionTimestamp;
    }
    
    private FailureReport _delayedFailureReport = null;
    public FailureReport getDelayedFailureReport()
    {
        // try to read from state
        return _delayedFailureReport;
    }
    public void setDelayedFailureReport(FailureReport delayedFailureReport)
    {
        // save it to state
    	_delayedFailureReport = delayedFailureReport;
    }
    
    private VehicleMotionDetector _vehicleMotionDetector = null;
    public VehicleMotionDetector getVehicleMotionDetector(Context ctx)
    {
	    // try to read from state
	    VehicleMotionDetector detector = _vehicleMotionDetector;
	
	    if (detector == null)
	    {
	        // not found in state 
	        detector = new VehicleMotionDetector(ctx);
	
	        // add it to state
	        _vehicleMotionDetector = detector;
	    }
	    return detector;
    }
    public void setVehicleMotionDetector(VehicleMotionDetector detector)
    {
    	_vehicleMotionDetector = detector;
    }
    
    private Date _potentialDrivingStopTimestamp = TimeKeeper.getInstance().now();
    public Date getPotentialDrivingStopTimestamp()
    {
    	return this._potentialDrivingStopTimestamp;
    }
    public void setPotentialDrivingStopTimestamp(Date timestamp)
    {
        if (timestamp != null) {
            EventBus.getDefault().post(new DutyStatusEnum(DutyStatusEnum.DRIVING));
        }
        else {
            EventBus.getDefault().post(new DutyStatusEnum(DutyStatusEnum.NULL));
        }
    	this._potentialDrivingStopTimestamp = timestamp;
    }
    
    private FuelPurchase _fuelPurchase = null;
    public FuelPurchase getFuelPurchase()
    {
    	return _fuelPurchase;
    }
    public void setFuelPurchase(FuelPurchase fuelPurchase)
    {
    	this._fuelPurchase = fuelPurchase;
    }
    
    private String _packageVersionName = null;
    public String getPackageVersionName()
    {
    	if (_packageVersionName == null)
    	{
			PackageManager pm = getPackageManager();
			PackageInfo pi = null;
			try {
				String packageName = getPackageName();
				pi = pm.getPackageInfo(packageName, 0);
			} catch (NameNotFoundException e) {
				// Ignore NameNotFoundException.
				pi = null;
			}

			if (pi != null)
				_packageVersionName = pi.versionName;
		}
    	
		return _packageVersionName;
	}

	public String getHardwarePlatform() {
		// TODO Remove hardcoded value for HardwarePlatform
		return "Android";
	}

	public String getCPUArchitecture() {
		// TODO Remove hardcoded value for CPUArchitecture
		return "ARM";
	}

    private boolean isAutoUpdate = false;
	public boolean getIsAutoUpdate() {
		return this.isAutoUpdate;
	}
	public void setIsAutoUpdate(boolean isAutoUpdate)
	{
		this.isAutoUpdate = isAutoUpdate;
	}
	
	private Object ihosRulesetCalcEngine = null;
	public Object getRulesetCalcEngine()
	{
		return ihosRulesetCalcEngine;
	}
	public void setRulesetCalcEngine(Object ihosRulesetCalcEngine)
	{
		this.ihosRulesetCalcEngine = ihosRulesetCalcEngine;
	}
	
    private boolean isPassedRods = false;
	public boolean getPassedRods() {
		return this.isPassedRods;
	}
	public void setIsPassedRods(boolean isPassedRods)
	{
		this.isPassedRods = isPassedRods;
	}
	
	private Object hoursOfServiceSummary = null;
	public Object getCalcEngineSummary()
	{
		return hoursOfServiceSummary;
	}
	public void setCalcEngineSummary(Object summary)
	{
		this.hoursOfServiceSummary = summary;
	}
	
	private Object nextServerUpdateTimestamp = null;
	public Object getNextServerUpdateTimestamp()
	{
		return nextServerUpdateTimestamp;
	}
	public void setNextServerUpdateTimestamp(Object timestamp)
	{
		this.nextServerUpdateTimestamp = timestamp;
	}
	
	private Class<?> notificationHoursAvailableClass = null;
	public Class<?> getNotificationHoursAvailableClass()
	{
		return this.notificationHoursAvailableClass;
	}
	public void setNotificationHoursAvailableClass(Class<?> notificationClass)
	{
		this.notificationHoursAvailableClass = notificationClass;
	}
	
	private VehicleInspection vehicleInspection = null;
	public VehicleInspection getCurrentVehicleInspection(){
		return this.vehicleInspection;
	}
	public void setCurrentVehicleInspection(VehicleInspection vehicleInspection)
	{
		this.vehicleInspection = vehicleInspection;
	}
	
	private VehicleInspection vehiclePreInspection = null;
	public VehicleInspection getCurrentVehiclePreInspection()
	{
		return this.vehiclePreInspection;
	}
	public void setCurrentVehiclePreInspection(VehicleInspection vehiclePreInspection)
	{
		this.vehiclePreInspection = vehiclePreInspection;
	}

	private boolean appRestartFlag = false;
    public boolean getAppRestartFlag()
    {
    	return this.appRestartFlag;
    }
    public void setAppRestartFlag(boolean appRestart)
    {
    	this.appRestartFlag = appRestart;
    }
    

    // This is set on the RodsEntry page to let the Login page know we logged out.
    // When app is initially run this will be false so the additional startup "tasks" will not be executed.
    private boolean runLockedDownAppStartup = false;
    public boolean getRunLockedDownAppStartup()
    {
    	return runLockedDownAppStartup;
    }
    public void setRunLockedDownAppStartup(boolean value)
    {
    	runLockedDownAppStartup = value;
    }
        
    private boolean _isViewOnlyMode;
    public boolean getIsViewOnlyMode()
    {
    	return _isViewOnlyMode;
    }
    public void setIsViewOnlyMode(boolean isViewOnlyMode)
    {
    	_isViewOnlyMode = isViewOnlyMode;
    }
    
    // 9/19/12 JHM - Added eobrService and isCrashDetected methods to support clean termination 
    // of app, service, and notifications
    private EobrServiceBase eobrService;
    public EobrServiceBase getEobrService()
    {
    	return this.eobrService;
    }
    public void setEobrService(EobrServiceBase eobrService)
    {
    	this.eobrService = eobrService;
    }
    
    private boolean isCrashDetected = false;
    public boolean getIsCrashDetected()
    {
    	return this.isCrashDetected;
    }
    public void setIsCrashDetected(boolean isCrashing)
    {
    	this.isCrashDetected = isCrashing;
    }

    private boolean isReviewEldEvent = false;
	public boolean isReviewEldEvent() {
		return isReviewEldEvent;
	}
	public void setIsReviewEldEvent(boolean value) {
		isReviewEldEvent = value;
	}


    private Integer _reviewEldEventLogKey;
    public Integer  getReviewEldEventLogKey() {
        return _reviewEldEventLogKey;
    }
    public void setReviewEldEventLogKey(Integer value) {
        _reviewEldEventLogKey = value;
    }

    private Date _reviewEldEventDate;
    public Date  getReviewEldEventDate() {
        return _reviewEldEventDate;
    }
    public void setReviewEldEventDate(Date value) {
        _reviewEldEventDate = value;
    }

    private boolean isNewActivation = false;
    public boolean isNewActivation() {
        return isNewActivation;
    }

    public void setIsNewActivation(boolean isNewActivation) {
        this.isNewActivation = isNewActivation;
    }

    /*
	 * 2014.04.18 sjn Added the timestamp that a driving status was manually ended
	 */
    private Date _savedManualDrivingStopTimestamp = null;
    public Date getSavedManualDrivingStopTimestamp()
    {
    	return this._savedManualDrivingStopTimestamp;
    }
    public void setSavedManualDrivingStopTimestamp(Date timestamp)
    {
    	this._savedManualDrivingStopTimestamp = timestamp;
    }
    
    private boolean _abortFirmwareUpgrade = false;
    public boolean getAbortFirmwareUpgrade()
    {
    	return _abortFirmwareUpgrade;
    }
    public void setAbortFirmwareUpgrade(boolean abort)
    {
    	_abortFirmwareUpgrade = abort;
    }
    
    /**
     * 2014.07.16 sjn Added this flag such that the EOBR history reading process can be aborted while it is running
     * 				  This flag will be continually monitored during history reading actions.
     *  			  When this flag gets set to TRUE, it will abort history reading.
     */
    private boolean _abortReadingHistory = false;
    public boolean getAbortReadingHistory()
    {
    	return _abortReadingHistory;
    }
    public void setAbortReadingHistory(boolean abort)
    {
    	_abortReadingHistory = abort;
    }
    
    public boolean isAlkCoPilotActivated()
    {
    	SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpreferencefile), MODE_PRIVATE);
    	return preferences.getBoolean(getString(R.string.pref_is_alk_copilot_activated), false);
    }
    public void setAlkCoPilotActivated(boolean isAlkCoPilotActivated)
    {
    	SharedPreferences.Editor preferences = getSharedPreferences(getString(R.string.sharedpreferencefile), MODE_PRIVATE).edit();
    	preferences.putBoolean(getString(R.string.pref_is_alk_copilot_activated), isAlkCoPilotActivated);
    	preferences.commit();
    }

	public boolean getIsUsingDrivingOverride()
	{
		return getIsInHyrailDutyStatus() || getIsInPersonalConveyanceDutyStatus() || getIsInNonRegDrivingDutyStatus();
	}

    public boolean getIsInPersonalConveyanceDutyStatus()
    { 
    	return getCurrentUser().getUserState().getIsInPersonalConveyanceDutyStatus();
    }
    public void setIsInPersonalConveyanceDutyStatus(boolean value)
    {
    	getCurrentUser().getUserState().setIsInPersonalConveyanceDutyStatus(value);
    }

	public boolean getIsInYardMoveDutyStatus()
	{
		return getCurrentUser().getUserState().getIsInYardMoveDutyStatus();
	}

	public void setIsInYardMoveDutyStatus(boolean value)
	{
		getCurrentUser().getUserState().setIsInYardMoveDutyStatus(value);
	}

	public boolean getIsInHyrailDutyStatus()
	{
		return getCurrentUser().getUserState().getIsInHyrailDutyStatus();
	}
	public void setIsInHyrailDutyStatus(boolean value)
	{
		getCurrentUser().getUserState().setIsInHyrailDutyStatus(value);
	}

    public boolean getIsInNonRegDrivingDutyStatus()
    {
        return getCurrentUser().getUserState().getIsInNonRegDrivingDutyStatus();
    }
    public void setIsInNonRegDrivingDutyStatus(boolean value)
    {
        getCurrentUser().getUserState().setIsInNonRegDrivingDutyStatus(value);
    }


    public enum TeamDriverModeEnum { NONE, SHAREDDEVICE, SEPARATEDEVICE }
    private TeamDriverModeEnum _teamDriverMode = TeamDriverModeEnum.NONE; 
    public TeamDriverModeEnum getTeamDriverMode()
    { 
    	return _teamDriverMode; 
    }
    public void setTeamDriverMode(TeamDriverModeEnum value)
    {
    	_teamDriverMode = value; 
    }

	private boolean _isInYardMoveDrivingSegment = false;
	public boolean getIsInYardMoveDrivingSegment() {
		return _isInYardMoveDrivingSegment;
	}

	public void setIsInYardMoveDrivingSegment(boolean value) {
		_isInYardMoveDrivingSegment = value;
	}

	private boolean _isInPersonalConveyanceDrivingSegment = false;
    public boolean getIsInPersonalConveyanceDrivingSegment()
    { 
    	return _isInPersonalConveyanceDrivingSegment; 
    }
    public void setIsInPersonalConveyanceDrivingSegment(boolean value)
    {
    	_isInPersonalConveyanceDrivingSegment = value; 
    }

	private boolean _isInHyrailDrivingSegment = false;
	public boolean getIsInHyrailDrivingSegment()
	{
		return _isInHyrailDrivingSegment;
	}
	
	public void setIsInHyrailDrivingSegment(boolean value)
	{
		_isInHyrailDrivingSegment = value;
	}

	private boolean _isInNonRegDrivingSegment = false;

	public boolean getIsInNonRegDrivingSegment(){
		return _isInNonRegDrivingSegment;
	}

	public void setIsInNonRegDrivingSegment(boolean value) {_isInNonRegDrivingSegment = value; }

	private boolean isInDriveOnPeriod = false;
	public boolean getIsInDriveOnPeriod() {return isInDriveOnPeriod;}
	public void setIsInDriveOnPeriod(boolean value) {isInDriveOnPeriod = value;}

	private boolean _isUserLoggingOut = false;
    public boolean getIsUserLoggingOut()
    {
    	return _isUserLoggingOut;
    }
    public void setIsUserLoggingOut(boolean value)
    {
    	_isUserLoggingOut = value;
    }

    private boolean _isTeamLogin= false;
    public boolean getIsTeamLogin(){return _isTeamLogin;}
    public void setIsTeamLogin(boolean value){_isTeamLogin=value;}

	private boolean _isMultipleUsersLogin = false;
	public boolean getIsMultipleUsersLogin() { return _isMultipleUsersLogin; }
	public void setIsMultipleUsersLogin(boolean value) { _isMultipleUsersLogin = value; }

	private String _currentVehiclePlate;
	public String get_currentVehiclePlate() { return _currentVehiclePlate;}
	public void set_currentVehiclePlate(String value){_currentVehiclePlate = value;}

	private String _currentTrailerPlate;
	public String get_currentTrailerPlate() { return _currentTrailerPlate;}
	public void set_currentTrailerPlate(String value){_currentTrailerPlate = value;}

	public String get_currentTrailerNumbers() { return getCurrentUser().getUserState().get_currentTrailerNumbers();}
	public void set_currentTrailerNumbers(String value){
		getCurrentUser().getUserState().set_currentTrailerNumbers(value);}

	public String get_currentShipmentInfo() { return getCurrentUser().getUserState().get_currentShipmentInfo();}
	public void  set_currentShipmentInfo(String value){getCurrentUser().getUserState().set_currentShipmentInfo(value);}

	public String get_currentMotionPictureAuthorityId() {return getCurrentUser().getUserState().getMotionPictureAuthorityId();}
	public void set_currentMotionPictureAuthorityId(String value) {getCurrentUser().getUserState().setMotionPictureAuthorityId(value);}

	public String get_currentMotionPictureProductionId() {return getCurrentUser().getUserState().getMotionPictureProductionId();}
	public void set_currentMotionPictureProductionId(String value) {getCurrentUser().getUserState().setMotionPictureProductionId(value);}

	private String _currentTractorNumbers;
	public String get_currentTractorNumbers() { return _currentTractorNumbers;}
	public void  set_currentTractorNumbers(String value){_currentTractorNumbers = value;}

	private boolean _isEndingActivePCYMWT_Status = false;
	public boolean getIsEndingActivePCYMWT_Status(){ return _isEndingActivePCYMWT_Status;}
	public void setIsEndingActivePCYMWT_Status(boolean value){
		_isEndingActivePCYMWT_Status = value;
	}

	public boolean getIsCurrentUserTheDesignatedDriver() {
		User user = GlobalState.getInstance().getCurrentUser();
		User dd = GlobalState.getInstance().getCurrentDesignatedDriver();

		boolean isDriver = false;
		if (dd != null && user != null && user.getCredentials() != null && user.getCredentials().getEmployeeId().compareTo(dd.getCredentials().getEmployeeId())==0)
		{
			isDriver = true;
		}
		return isDriver;
	}

	private ErrorAccumulator engineSyncErrors;
	public ErrorAccumulator getEngineSyncErrors() {
		return engineSyncErrors;
	}
	public void setEngineSyncErrors(ErrorAccumulator engineSyncErrors) {
		this.engineSyncErrors = engineSyncErrors;
	}
	public DataTransferMechanismStatus _dataTransferMechanismStatus;
	public DataTransferMechanismStatus getDataTransferMechanismStatus() {return _dataTransferMechanismStatus;}
	public void setDataTransferMechanismStatus(DataTransferMechanismStatus value) {_dataTransferMechanismStatus = value;}

	private ErrorAccumulator gpsErrors;
	public ErrorAccumulator getGpsErrors() {
		return gpsErrors;
	}
	public void setGpsErrors(ErrorAccumulator gpsErrors) {
		this.gpsErrors = gpsErrors;
	}
	public boolean _isDataTransferMechanismStatusInProgress = false;
	public boolean getisDataTransferMechanismStatusInProgress(){ return _isDataTransferMechanismStatusInProgress;}
	public void setisDataTransferMechanismStatusInProgress(Boolean value){ _isDataTransferMechanismStatusInProgress = value;}

	private boolean _forceGeotabInvalidOdo = false;
	public boolean getForceGeotabInvalidOdo(){ return _forceGeotabInvalidOdo;}
	public void setForceGeotabInvalidOdo(boolean value){
		_forceGeotabInvalidOdo = value;
	}

	private boolean _forceGeotabInvalidVss = false;
	public boolean getForceGeotabInvalidVss(){ return _forceGeotabInvalidVss;}
	public void setForceGeotabInvalidVss(boolean value){
		_forceGeotabInvalidVss = value;
	}

	private boolean _forceInvalidGPSDate = false;
	public boolean getForceInvalidGPSDate(){ return  _forceInvalidGPSDate;}
	public void setForceInvalidGPSDate(boolean value){
		_forceInvalidGPSDate = value;
	}

	private boolean _forceGeotabInvalidGPS = false;
	public boolean getForceGeotabInvalidGPS(){ return _forceGeotabInvalidGPS;}
	public void setForceGeotabInvalidGPS(boolean value){
		_forceGeotabInvalidGPS = value;
	}

	/**
	 * Use ActivityLifecycleCallbacks to determine if KMB Application is in Background or Foreground
	 */
	public boolean isApplicationInForeground() {
		return mActiveActivitiesRefCount > 0;
	}

	public Activity getCurrentActivity() {
		return mActiveActivity.get();
	}
}
