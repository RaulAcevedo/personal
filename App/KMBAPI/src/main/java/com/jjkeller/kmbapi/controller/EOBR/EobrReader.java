package com.jjkeller.kmbapi.controller.EOBR;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.dataaccess.FacadeFactory;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEventGenII;
import com.jjkeller.kmbapi.controller.interfaces.IEobrReaderChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrTripReportListener;
import com.jjkeller.kmbapi.controller.interfaces.IEventHandler;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.eobrengine.Enums;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.eobrengine.EobrEngineFactory;
import com.jjkeller.kmbapi.eobrengine.IEobrEngine;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.DeviceNotConnectedEobrException;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.EobrException;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.CommunicationsMode;
import com.jjkeller.kmbapi.kmbeobr.Enums.DeviceErrorFlags;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrServiceMessages;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EobrSelfTestResult;
import com.jjkeller.kmbapi.kmbeobr.EventMaskBuilder;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;
import com.jjkeller.kmbapi.kmbeobr.HistogramData;
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.IApplicationUpdateListener;
import com.jjkeller.kmbapi.kmbeobr.IApplicationUpdateListenerFactory;
import com.jjkeller.kmbapi.kmbeobr.JbusDiagnosticData;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordMotionOptionEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.jjkeller.kmbapi.configuration.GlobalState.getContext;
import static com.jjkeller.kmbapi.kmbeobr.Constants.EVENTREFTIME;

public class EobrReader implements IEobrReader {
	// Debug Toggles
	protected FirmwareUpdateBroadcaster broadcaster = new FirmwareUpdateBroadcaster();

	private static final boolean TIMER_POP_DEBUG = false;

	private static EobrReader singleton;
	
	private static EobrDeviceDescriptor[] _deviceList = null;
	private static IEobrEngine _eobrEngine = null;
	private String _previousBtAddress = null;

	public static EobrReader getInstance(){
		if (singleton == null)
			singleton = new EobrReader();
		
		return singleton;
	}

	/* OdometerInValidRange bundle keys */
	public static final String ISINVALIDRANGE = "isInValidRange";
	public static final String STARTRANGE = "startRange";
	public static final String ENDRANGE = "endRange";
	
	/* ProcessEobrReaderHistoryEvent bundle keys */
	public static final String DETECTEDDRIVINGPERIODS = "detectedDrivingPeriods";
	public static final String DETECTEDPRELOGINDRIVINGPERIODS = "detectedPreloginDrivingPeriods";
	
	// Eobr bundle return code key
	private static final String RETURNCODE = "rc";
	private static final String RETURNVALUE = "ReturnValue";
	
    private static final int GEN1_BTCLASS = 0x1f00;
    private static final int GEN2_PAN_BTCLASS = 0x0000;
    private static final int GEN2_RVN_BTCLASS = 0x0704;

    private static Object _clockVerificationLock = new Object();
    private String _unitLicensePlateNumber = null;

    // the amount of time that must expire between clock consistency check
    private static long CLOCK_VERIFICATION_FREQUENCY_MILLIS = 60 * 1000;  // 1 minute

    private IApplicationUpdateListenerFactory _applicationUpdateListenerFactory;
	public IApplicationUpdateListenerFactory getApplicationUpdateListenerFactory()
	{
		return _applicationUpdateListenerFactory;
	}

	public void setApplicationUpdateListenerFactory(IApplicationUpdateListenerFactory applicationUpdateListenerFactory)
	{
		_applicationUpdateListenerFactory = applicationUpdateListenerFactory;
	}

	private IEobrReaderChangeEvent _eobrReaderChangeEventHandler;
	public void setEobrReaderChangeEventHandler(IEobrReaderChangeEvent handler)
	{
		this._eobrReaderChangeEventHandler = handler;
	}

	private IEobrHistoryChangeEvent _eobrHistoryChangeEventHandler;
	public void setEobrHistoryChangeEventHandler(IEobrHistoryChangeEvent handler)
	{
		this._eobrHistoryChangeEventHandler = handler;
	}
	
	private IEobrHistoryChangeEventGenII _eobrHistoryChangeEventHandlerGenII;
	public void setEobrHistoryChangeEventHandlerGenII(IEobrHistoryChangeEventGenII handler)
	{
		this._eobrHistoryChangeEventHandlerGenII = handler;
	}
	
	private IEventHandler<UnassignedDrivingPeriodEventArgs> _unassignedDrivingPeriodHandler;
	public void setUnassignedDrivingPeriodEventHandler(IEventHandler<UnassignedDrivingPeriodEventArgs> handler)
	{
		_unassignedDrivingPeriodHandler = handler;
	}
	
	private IEobrTripReportListener _eobrTripReportHandler;
	public void setEobrTripReportHandler(IEobrTripReportListener handler)
	{
		_eobrTripReportHandler = handler;
	}

	private IApplicationUpdateListener _applicationUpdateListener;
	public IApplicationUpdateListener getApplicationUpdateHandler()
	{
		if(_applicationUpdateListener == null)
			_applicationUpdateListener = _applicationUpdateListenerFactory.getUpdateListener();
		
		return _applicationUpdateListener;
	}

    @Override
	public IEobrEngine getEobrEngine(){
    	return _eobrEngine;
    }
    private void setEobrEngine(IEobrEngine eobrEngine)
    {
    	_eobrEngine = eobrEngine;
    }
    
    
	// Do not use the following 4 methods for anything else except sharing the bluetooth connection information with the 
	// Testharness.  These are only being exposed here so that the TestHarness application can share the Bluetooth information.
	// Currently, the Testharness application communicates with the EOBR via KMBAPI methods and via its own "internal" methods.
	// Because of this, we need to be able to share bluetooth connection information.  For example, the Testharness initially 
	// establishes EOBR communications using the KMBAPI methods.  Although some of the test cases within the Testharness will 
	// continue to use the KMBAPI communication methods to communicate with the EOBR, there are several test cases within the 
	// Testharness that will use its own "internal" methods to communicate with the EOBR.  Because of this, we need to "share", 
	// or continue to use the bluetooth connection information originally established.  If we don't, we run into several errors 
	// and issues. This solution was discussed with Jim M. in June, 2013 and was accepted as the best alternative at this point.
    public BluetoothAdapter getBluetoothAdapter() {
    	return EobrReader.getInstance().getEobrEngine().getBluetoothAdapter();
    }
    
    public BluetoothSocket getBlueToothSocket() {
    	return EobrReader.getInstance().getEobrEngine().getBlueToothSocket();
    }
    
    public String getCurrentBtAddress() {
    	return EobrReader.getInstance().getEobrEngine().getCurrentBtAddress();
    }
    
    public boolean getIsSocketConnected() {
    	boolean socketConnected = false;
    	
    	IEobrEngine engine = EobrReader.getInstance().getEobrEngine();
    	if (engine != null)
    		socketConnected = engine.getIsSocketConnected();
    	
    	return socketConnected;
    }

	// EobrService messenger that the Reader can use to interact with service.
    private Messenger _eobrServiceMessenger = null;
    public void setEobrServiceMessenger(Messenger messenger)
    {
    	_eobrServiceMessenger = messenger;
    }
    
    /// <summary>
    /// The current state of the connection to the physical EOBR device
    /// </summary>
	private ConnectionState _connectionState = ConnectionState.OFFLINE;
    @Override
	public ConnectionState getCurrentConnectionState()
    {
        return _connectionState;
    }
    public void setCurrentConnectionState(ConnectionState value)
    {
        _connectionState = value;

        if (value != ConnectionState.ONLINE && _hibernateOnNextOffline)
        {
            // the connection just when OFFLINE, and the "hiberate when offline" flag is set
            // so hibernate it
        	// TODO set the EOBR to Hibernate
            //this.Hibernate();
        }
    }

    private CommunicationsMode _currentCommMode = CommunicationsMode.Unknown;
    public CommunicationsMode getCurrentCommunicationMode()
    {
    	return _currentCommMode;
    }
    public void setCurrentCommunicationMode(CommunicationsMode communicationsMode)
    {
    	_currentCommMode = communicationsMode;
    }
    
    private String _eobrId = null;
    /// <summary>
    /// This is the EOBR identifier (tractor number).   
    /// This is loaded during Initialization
    /// </summary>
    @Override
	public String getEobrIdentifier()
    {
        return _eobrId;
    }

    private void setEobrIdentifier(String value)
    {
    	_eobrId = value;
    }
    
    private String _eobrSerialNumber = null;
    /// <summary>
    /// This is the EOBR serial number.   
    /// This is loaded during Initialization
    /// </summary>
    @Override
	public String getEobrSerialNumber()
    {
        return _eobrSerialNumber;
    }
    
	public void setEobrSerialNumber(String value)
    { 
    	_eobrSerialNumber = value;
    }
    
	private DatabusTypeEnum _eobrDatabusType;
    /// <summary>
    /// This is the current EOBR databus type.   
    /// This is loaded during Initialization
    /// </summary>
	public DatabusTypeEnum getEobrDatabusType()
	{
		return _eobrDatabusType;
	}
	
	public void setEobrDatabusType(DatabusTypeEnum databusType)
	{
		_eobrDatabusType = databusType;
	}
	
    private Date _clockCheckTimestamp = null;
    /// <summary>
    /// This timestamp is used to verify that the device clock has not
    /// been changed to a time that has past since we last read data from
    /// the EOBR.
    /// </summary>
    public Date getClockCheckTimestamp()
    {
        return _clockCheckTimestamp;
    }
    public void setClockCheckTimestamp(Date clockCheckTimestamp)
    {
        this._clockCheckTimestamp = clockCheckTimestamp;
    }
    
    private long _clockCheckElapsedTime = -1;
    /// <summary>
    /// This value is used to identify the elapsed time since the phone has
    /// been booted, including sleep time.  This is used to determine if the
    /// devices clock has been manipulated since we last read data from the EOBR.
    /// </summary>
    public long getClockCheckElapsedTime()
    {
        return _clockCheckElapsedTime;
    }
    public void setClockCheckElapsedTime(long clockCheckElapsedTime)
    {
        this._clockCheckElapsedTime = clockCheckElapsedTime;
    }
    
    /// <summary>
    /// This value is used to identify if the device clock is being set by the AutoTime feature.
    /// This feature is set as a preference in the Settings/DateTime and controls whether the network sets the device clock.
    /// This is important to know because when clock changes come through in AutoTime mode, then we can trust their accuracy.
    /// </summary>
    private long _clockUpdatedViaAutomaticTimeCount = 0;    

    /// <summary>
    /// This value is used to identify if the device clock has been updated recently.
    /// If we receive an device clock change event, this may get set depending on other factors.
    /// As soon as Verify Clock Consistency gets a chance to run, this will be turned back off again
    /// </summary>
    private boolean _clockRecentUpdateNeedsEvaluation = false;  
   
    public boolean IsClockUpdatePending()
    {
    	return _clockRecentUpdateNeedsEvaluation;
    }
    
    private Date _eobrOfflineTimestamp = null;
    /// <summary>
    /// This is the timestamp that the EOBR went offline
    /// </summary>
    public Date getEobrOfflineTimestamp()
    { 
    	return _eobrOfflineTimestamp;
    }

    public void setEobrOfflineTimestamp(Date eobrOfflineTimestamp)
    {
    	this._eobrOfflineTimestamp = eobrOfflineTimestamp;
    }
    
    private float _odometerOffset = 0.0F;
    /// <summary>
    /// This is the odometer calibration value for OFFSET.   
    /// This is loaded during Initialization.
    /// The formula for determining the dashboard odometer value uses this property.
    /// </summary>
    public float getOdometerOffset()
    {
    	return _odometerOffset; 
	}
    public void setOdometerOffset(float odometerOffset)
    {
        _odometerOffset = odometerOffset;
    }
    
    private float _odometerMultiplier = 0.0F;
    /// <summary>
    /// This is the odometer calibration value for MULTIPLIER.   
    /// This is loaded during Initialization.
    /// The formula for determining the dashboard odometer value uses this property.
    /// </summary>
    public float getOdometerMultiplier()
    {
        return _odometerMultiplier;
	}
    public void setOdometerMultiplier(float odometerMultiplier)
    {
    	_odometerMultiplier = odometerMultiplier; 
    }
    
    private float _lastEobrOdometer = -1.0F;
    /// <summary>
    /// Last eobr odometer read from the eobr either when reading
    /// current data or when reading historical data.
    /// Used to eliminate invalid odometers coming from the eobr.
    /// </summary>
    public float getLastEobrOdometer()
    {
        return _lastEobrOdometer;
    }
    public void setLastEobrOdometer(float lastEobrOdometer)
    {
        _lastEobrOdometer = lastEobrOdometer;
    }
    
    private Date _lastEobrOdometerUTCTime = null;
    /// <summary>
    /// UTC date/time of last eobr odometer reading
    /// </summary>
    public Date getLastEobrOdometerUTCTime()
    {
        return _lastEobrOdometerUTCTime;
    }
    public void setLastEobrOdometerUTCTime(Date lastEobrOdometerUTCTime)
    {
    	_lastEobrOdometerUTCTime = lastEobrOdometerUTCTime;
    }
    
    private boolean _onStatusChangeEnabled = true;
    /**
     * Answer if the publish of DutyStatus changes is enabled
     * @return
     */
    @Override
	public boolean getOnStatusChangeEnabled(){
    	return _onStatusChangeEnabled;
    }
    @Override
	public void setOnStatusChangeEnabled(boolean val){
    	_onStatusChangeEnabled = val;
    }
    
    public boolean isEobrGen1()
    {
    	boolean retVal = false;
    	
    	// TEMPORARY - I don't believe this method is needed once all Gen II functionality
    	// is added - currently used to skip some functions for Gen II
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		EobrDeviceDescriptor[] deviceDes = eobrEngine.getDiscoveredDeviceList();
    		if (deviceDes != null && deviceDes.length == 1)
    			retVal = deviceDes[0].getEobrGen() == 1;
    	}
    	
    	return retVal;
    }
    
    /// <summary>
    /// Return Generation of Device 
    /// Set in EobrConfiguration to send to Encompass
    /// </summary>
    @Override
	public int getEobrGeneration()
    {
    	int retVal = 0;

    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	if (eobrEngine != null)
    	{
    		EobrDeviceDescriptor[] deviceDes = eobrEngine.getDiscoveredDeviceList();
    		if (deviceDes != null && deviceDes.length == 1)
    			retVal = deviceDes[0].getEobrGen();
    		else{
    			// if there is no device description, then rely on the factory to determine based on types
    			retVal = EobrEngineFactory.DetermineGenerationFor(eobrEngine);
    		}
    	}
    	
    	return retVal;
    }
    
    /// <summary>
    /// Number of milliseconds that the two clocks (mobile device and eobr)
    /// are allowed to be out of sync before automatically synchronizing
    /// them together.
    /// </summary>
    private int _clockVerificationConsistencyToleranceMillis = 60 * 1000;
    
    /// <summary>
    /// This flag is used to hibernate the reader if it ever goes offline
    /// </summary>
    private boolean _hibernateOnNextOffline = false;

	public String getPreviousBtAddress() {
		return _previousBtAddress;
	}

	/// <summary>
    /// Possible states of the connection to the physical EOBR device
    /// </summary>
    public enum ConnectionState { ONLINE, OFFLINE, READINGHISTORICAL, DEVICEFAILURE, FIRMWAREUPDATE, SHUTDOWN }
    
    /// <summary>
    /// Answer the unique Id assigned to the last active EOBR device
    /// </summary>
    /// <returns></returns>
//    public String GetUniqueIdentifier()
//    {
//        return this.getEobrIdentifier();
//    }
    
    public static boolean getIsEobrDeviceAvailable()
    {
    	boolean isDeviceOnline = false;
    	if (singleton != null)
    	{
    		isDeviceOnline = singleton.getCurrentConnectionState() == ConnectionState.ONLINE;
    	}
    	return isDeviceOnline;
    }
    
    public static boolean getIsEobrDeviceReadingHistory()
    {
    	boolean isReadingHistory = false;
    	if (singleton != null)
    	{
    		isReadingHistory = singleton.getCurrentConnectionState() == ConnectionState.READINGHISTORICAL;
    	}
    	return isReadingHistory;
    }
    
	public static boolean getIsEobrDeviceOnlineOrReadingHistory()
	{
		boolean isDeviceOnlineOrReadingHistory = false;
		if (singleton != null)
		{
			isDeviceOnlineOrReadingHistory =
					singleton.getCurrentConnectionState() == ConnectionState.ONLINE
					|| singleton.getCurrentConnectionState() == ConnectionState.READINGHISTORICAL;
		}
		return isDeviceOnlineOrReadingHistory;
	}

    public static boolean getIsEobrDevicePhysicallyConnected()
    {
    	boolean isDeviceConnected = false;
    	if (singleton != null)
    	{
    		isDeviceConnected = singleton.getIsSocketConnected();
    	}
    	return isDeviceConnected;
    }
    
//********************************************************************************
// EOBR Discovery/Connectivity - Start
//********************************************************************************
    
    /// <summary>
    /// Perform discovery on the list of gen 1 bt devices and list of gen 2 bt devices
    /// to determine which are eobrs setup with the correct passkey.
    /// <param name="ctx">Context calling this method</param>
    /// <param name="gen1Devices">List of gen 1 bluetooth devices (BT Class = Uncategorized)</param>
    /// <param name="gen2Devices">List of gen 2 bluetooth devices (BT Class = MISC)</param>
    /// <param name="provisionNewDevice">Flag indicating whether provisioning a new device</param>
    /// <param name="serialNumber">Serial number of eobr to provision when provisioning a new device</param>
    /// <returns>Merged EobrDeviceDescriptor list containing any discovered gen I and gen II eobrs</returns>
    public static EobrDeviceDescriptor[] PerformFullDeviceDiscovery(Context ctx, List<BluetoothDevice> gen1Devices, List<BluetoothDevice> gen2Devices, boolean provisionNewDevice, String serialNumber)
    {
        // make the device go offline initially, and suspend any updates
    	EobrReader eobrRdr = EobrReader.getInstance();
    	eobrRdr.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, "PerformFullDeviceDiscovery");
    	eobrRdr.SuspendReading();

    	try{	
	        // discover every device on the comm stack
	        String passkey = "";
	        if(!provisionNewDevice)
	        	passkey =  GlobalState.getInstance().getCompanyConfigSettings(ctx).getEobrDiscoveryPasskey();
	        else
	        	passkey = "All Devices";
	        	
	        // get gen 1 device list
	        IEobrEngine eobrEngine = EobrEngineFactory.ForBTGenI();
	        eobrEngine.initializeConnectedDevices();
	        @SuppressWarnings("unused")
			int rc = eobrEngine.searchForEobrDevices(gen1Devices, passkey, serialNumber);
        	EobrDeviceDescriptor[] gen1DeviceList = eobrEngine.getDiscoveredDeviceList();
	        
	        // get gen 2 device list
	        eobrEngine = EobrEngineFactory.ForBTGenII();
	        eobrEngine.initializeConnectedDevices();
	        rc = eobrEngine.searchForEobrDevices(gen2Devices, passkey, serialNumber);
	        EobrDeviceDescriptor[] gen2DeviceList = eobrEngine.getDiscoveredDeviceList();

	        _deviceList = MergeDiscoveredDevices(gen1DeviceList, gen2DeviceList);	        	        
    	}
    	finally{
	        // resume reading when the discovery is complete
	        eobrRdr.ResumeReading();
    	}

        return _deviceList;
    }

    // The Testharness does not have CompanyConfigSettings and therefore cannot "consume" any functions involving CompanyConfigSettings.  If it does, 
    // exceptions will be thrown and the application will not function properly.  In the "normal" PerformFullDeviceDiscovery function, a call is made to 
    // getCompanyConfigSettings.  We avoid that here.
    public static EobrDeviceDescriptor[] PerformFullDeviceDiscoveryTestHarness(Context ctx, List<BluetoothDevice> gen1Devices, List<BluetoothDevice> gen2Devices, boolean provisionNewDevice, String serialNumber, String passkey)
    {
        // make the device go offline initially, and suspend any updates
    	EobrReader eobrRdr = EobrReader.getInstance();
    	eobrRdr.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, "PerformFullDeviceDiscovery");
    	eobrRdr.SuspendReading();

    	try {	
            if(passkey.compareTo("")== 0)
            	passkey = "All Devices";
    		
	        // get gen 1 device list
	        IEobrEngine eobrEngine = EobrEngineFactory.ForBTGenI();
	        eobrEngine.initializeConnectedDevices();
	        @SuppressWarnings("unused")
			int rc = eobrEngine.searchForEobrDevices(gen1Devices, passkey, serialNumber);
        	EobrDeviceDescriptor[] gen1DeviceList = eobrEngine.getDiscoveredDeviceList();
	        
	        // get gen 2 device list
	        eobrEngine = EobrEngineFactory.ForBTGenII();
	        eobrEngine.initializeConnectedDevices();
	        rc = eobrEngine.searchForEobrDevices(gen2Devices, passkey, serialNumber);
	        EobrDeviceDescriptor[] gen2DeviceList = eobrEngine.getDiscoveredDeviceList();

	        _deviceList = MergeDiscoveredDevices(gen1DeviceList, gen2DeviceList);	        	        
    	}
    	finally{
	        // resume reading when the discovery is complete
	        eobrRdr.ResumeReading();
    	}

        return _deviceList;
    }    
   
    private static EobrDeviceDescriptor[] MergeDiscoveredDevices(EobrDeviceDescriptor[] gen1List, EobrDeviceDescriptor[] gen2List)
    {
    	EobrDeviceDescriptor[] deviceList = null;
    	
        int numGen1Devices = 0;
        if (gen1List != null && gen1List.length > 0)
        	numGen1Devices = gen1List.length;
        
        int numGen2Devices = 0;
        if (gen2List != null && gen2List.length > 0)
        	numGen2Devices = gen2List.length;
        	        
        // if any devices discovered, merge the two lists into one
        if (numGen1Devices + numGen2Devices > 0)
        {	
        	deviceList = new EobrDeviceDescriptor[numGen1Devices + numGen2Devices];
        	
        	if (numGen1Devices > 0)
        		System.arraycopy(gen1List, 0, deviceList, 0, numGen1Devices);
        	
        	if (numGen2Devices > 0)
        		System.arraycopy(gen2List, 0, deviceList, numGen1Devices, numGen2Devices);
        }	

        return deviceList;
    }
        
    public static Bundle DiscoverAndActivateByMACAddress(Context ctx, String macAddress, BluetoothAdapter btAdapter) throws KmbApplicationException
    {
    	EobrReader eobrRdr = null;
    	
    	try
    	{
    		eobrRdr = EobrReader.getInstance();
    	}
    	catch(Exception ex)
    	{
    		throw new KmbApplicationException(String.format("%s %s", ctx.getString(R.string.exception_loadeobrdevice), ex.toString()));
    	}
    	
    	Bundle bundle = eobrRdr.PerformDeviceDiscoveryAndActivationFromMACAddress(ctx, macAddress, true, btAdapter);
 
    	return bundle;
    	
    }
    
    public static Bundle DiscoverByMACAddress(Context ctx, String macAddress, BluetoothAdapter btAdapter) throws KmbApplicationException
    {
    	EobrReader eobrRdr = null;
    	
    	try
    	{
    		eobrRdr = EobrReader.getInstance();
    	}
    	catch(Exception ex)
    	{
    		throw new KmbApplicationException(String.format("%s %s", ctx.getString(R.string.exception_loadeobrdevice), ex.toString()));
    	}
    	
    	Bundle bundle = eobrRdr.PerformDeviceDiscoveryFromMACAddress(ctx, macAddress, true, btAdapter);
 
    	return bundle;    	
    }

	private Bundle PerformDeviceDiscoveryAndActivationFromMACAddressForFirmwareUpdate(Context ctx, String deviceMacAddress, boolean performTransitions, BluetoothAdapter btAdapter) throws KmbApplicationException {
		return PerformDeviceDiscoveryAndActivationFromMACAddress(ctx, deviceMacAddress, performTransitions, btAdapter, false);
	}

	private Bundle PerformDeviceDiscoveryAndActivationFromMACAddress(Context ctx, String deviceMacAddress, boolean performTransitions, BluetoothAdapter btAdapter) throws KmbApplicationException {
		return PerformDeviceDiscoveryAndActivationFromMACAddress(ctx, deviceMacAddress, performTransitions, btAdapter, true);
	}

    private Bundle PerformDeviceDiscoveryAndActivationFromMACAddress(Context ctx, String deviceMacAddress, boolean performTransitions, BluetoothAdapter btAdapter, boolean shouldResumeReading) throws KmbApplicationException
    {
    	Bundle bundle = this.PerformDeviceDiscoveryFromMACAddress(ctx, deviceMacAddress, performTransitions, btAdapter);
    	int rc;
    	
    	if (bundle.containsKey(ctx.getString(R.string.rc)))
		{
    		rc = bundle.getInt(ctx.getString(R.string.rc));
    		
    		if (rc == EobrReturnCode.S_SUCCESS)
    		{
    			String deviceName = null;
    			if (bundle.containsKey(ctx.getString(R.string.returnvalue)))
    				deviceName = bundle.getString(ctx.getString(R.string.returnvalue));

    			try
    			{	
    				if (deviceName != null && deviceName.trim().length() > 0)
    				{
    					Thread.sleep(5000);
    					deviceName = deviceName.trim();

    					this.ActivateEobrDevice(ctx, deviceName, shouldResumeReading);
				
    					bundle.putInt(ctx.getString(R.string.rc), rc);
    					bundle.putString(ctx.getString(R.string.returnvalue), deviceName);
    				}
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
    			}
    		}
		}
    	
    	return bundle;
    }
    
    /*
     * Attempt a bluetooth socket connect to the bt device identified by the maAddress.
     * If successful, return the device name in the bundle
     */
    private Bundle PerformDeviceDiscoveryFromMACAddress(Context ctx, String deviceMacAddress, boolean performTransitions, BluetoothAdapter btAdapter) throws KmbApplicationException
    {
    	String deviceName = null;
    	String deviceAddress = null;
    	short deviceCrc = -1;
    	int deviceGeneration = -1;
    	Bundle bundle = new Bundle();
		bundle.putInt(ctx.getString(R.string.rc), EobrReturnCode.S_DEV_NOT_CONNECTED);
    	
    	if(performTransitions)
    		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, ctx.getString(R.string.performdevicediscovery));
    	
    	if(performTransitions)
    		this.SuspendReading();
    	
    	try{
	    	this.setEobrIdentifier(null);
	    	this.setEobrSerialNumber(null);
	    	this.setEobrDatabusType(null);
	    	
	        String passkey = GlobalState.getInstance().getCompanyConfigSettings(ctx).getEobrDiscoveryPasskey();

	    	if (btAdapter == null)
	    		btAdapter = BluetoothAdapter.getDefaultAdapter();
	    	
	    	BluetoothDevice btDevice = btAdapter.getRemoteDevice(deviceMacAddress);
	    	if (btDevice != null)
	    	{
	    		BluetoothClass btClass = btDevice.getBluetoothClass();
	    		IEobrEngine eobrEngine = null;
	    		
	    		// on LG phone, after turning bluetooth off and back on, the btClass
	    		// is not defined on the remote device until a discovery is performed
	    		if (btClass != null)
	    		{
	    			if (btClass.getDeviceClass() == GEN1_BTCLASS)
	    				eobrEngine = EobrEngineFactory.ForBTGenI();
	    			else if (btClass.getDeviceClass() == GEN2_PAN_BTCLASS || btClass.getDeviceClass() == GEN2_RVN_BTCLASS)
	    				eobrEngine = EobrEngineFactory.ForBTGenII();
	    		}
	    		
	    		// if eobr engine not initialized, try gen I and then gen II
    			if (eobrEngine == null)
    			{
    				IEobrEngine engine = EobrEngineFactory.ForBTGenI();
    				engine.initializeConnectedDevices();
    		        @SuppressWarnings("unused")
    				int rc = engine.searchForEobrDevice(passkey, btDevice);
    				_deviceList = engine.getDiscoveredDeviceList();
    				
    				if (_deviceList != null && _deviceList.length == 1)
    					eobrEngine = EobrEngineFactory.ForBTGenI();
    				else
    				{
        				engine = EobrEngineFactory.ForBTGenII();
        				engine.initializeConnectedDevices();
        				rc = engine.searchForEobrDevice(passkey, btDevice);
        				_deviceList = engine.getDiscoveredDeviceList();
        				
        				if (_deviceList != null && _deviceList.length == 1)
        					eobrEngine = EobrEngineFactory.ForBTGenII();
    				}
    			}
	    		
				EobrReader.getInstance().setEobrEngine(eobrEngine);
				eobrEngine.initializeConnectedDevices();

				int rc = eobrEngine.searchForEobrDevice(passkey, btDevice);
				_deviceList = eobrEngine.getDiscoveredDeviceList();
				
				if (rc != EobrReturnCode.S_SUCCESS && rc != EobrReturnCode.S_DEV_NOT_CONNECTED)
				{
					ErrorLogHelper.RecordMessage(ctx, String.format(Locale.getDefault(), ctx.getString(R.string.errorlog_searchforeobrmacaddressfailed), Integer.toString(rc), deviceMacAddress, passkey));
				}
	    	
				if (rc == EobrReturnCode.S_SUCCESS)
				{					
					// Get the device name from _deviceList (only one device should be found).  In some cases (new Roving 
					// Network emulation) where Set Eobr Config is used to change the unit id, the name of the bt device 
					// has not been updated.  Therefore Activation/Initialization fails because the device name identified 
					// by the bt device doesn't match the name in the discovered list
					if (_deviceList.length == 1)
					{
						deviceName = _deviceList[0].getName();
						deviceAddress = _deviceList[0].getAddress();
						deviceCrc = _deviceList[0].getCrc();
						deviceGeneration = _deviceList[0].getEobrGen();
					}
				}
				
				bundle.putInt(ctx.getString(R.string.rc), rc);
				if (deviceName != null)
					bundle.putString(ctx.getString(R.string.returnvalue), deviceName);
				if (deviceAddress != null)
					bundle.putString(ctx.getString(R.string.deviceaddress), deviceAddress);
				
				bundle.putShort(ctx.getString(R.string.devicecrc), deviceCrc);
				bundle.putInt(ctx.getString(R.string.devicegeneration), deviceGeneration);
	    	}
    	} 
    	finally{
    		// resume reading after discovery
    		if(performTransitions)
    			this.ResumeReading();
    	}

    	return bundle;
    }
        
    /// <summary>
    /// Answer if the EOBR device is connected and functional.
    /// If the comm to an EOBR has never been established, then perform
    /// a device discovery to detect if one is available now.
    /// </summary>
    /// <returns>true if EOBR device is connected and functional, false otherwise</returns>
    public boolean IsDevicePhysicallyConnected(Context ctx, boolean initializeFirst) throws KmbApplicationException
    {
    	boolean answer = false;

        // has the comm been established to this particular EOBR yet?
        if (this.getEobrIdentifier() != null && this.getEobrIdentifier().compareTo("") != 0)
        {
        	int rc = 0;
        	boolean testConnection = true;
        	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
        	
        	// if in bluetooth mode, try initializing device first - recreate the socket first
        	// before attempting calls to the eobr
        	// TCH - 1/19/2012 - always in Bluetooth mode, don't need to check the mode
        	if (initializeFirst && eobrEngine != null)
        	{
        		rc = eobrEngine.OpenDevice(this.getEobrIdentifier());
        		if (rc != EobrReturnCode.S_SUCCESS)
        		{
        			testConnection = false;
        			eobrEngine.CloseDevice();
        		}
        	}
        	
        	if (testConnection)
        	{
        		// This eobr device has already been discovered and initialized
        		// perform the connection test on the EOBR device   
        		rc = EobrReader.getInstance().Technician_TestConnection();
        		if (rc == EobrReturnCode.S_SUCCESS)
        		{
        			answer = true;
        		}
        	}
        }
        else 
        {
            // no EOBR has discovered yet because the EobrIdentifier is empty
            // need to perform discovery, and initialization of the EOBR
        	// NOTE:  In bluetooth mode, list of devices is null, so no device
        	// will be discovered.  Need to perform manual discovery from the 
        	// menu option
        	// 10/19/11 JHM - Skip discovery if bluetooth because of null list of devices resets
        	// 1/19/2012 - TCH - always in Bluetooth mode for Android - can't rediscover
        	// from here because device list is null - return false
        	answer = false;
        }

        return answer;
    }
    
    /// <summary>
    /// This method should ONLY be called from the Testharness application.  We have this specific method for the Testharness because
    /// several things do not need to execute for the Testharness to run.  Namely, the Testharness does not need to perform anything 
    /// related to current logs, reading historical records, setting odometer, etc.  Therefore, we have created this method specifically
    /// for the Testharness.
    ///
    /// Activate and initialize the specified EOBR device, by name.
    /// This will cause communication to be established to the new EOBR.
    /// If activation fails, then an exception will be thrown.
    /// </summary>
    /// <param name="deviceName">name of the device to select</param>
    public void TestHarness_ActivateEobrDevice(Context ctx, String deviceName) throws KmbApplicationException {
    	
        try {        	
	        // Activate the selected device.
	        this.InitializeEobrDevice(ctx, deviceName, true, true);	
        }
        finally { }
    }    

   	/**
	 * Activate and initialize the specified EOBR device, by name.
	 * This will cause communication to be established to the new EOBR.
	 * If activation fails, then an exception will be thrown.
	 *
	 * @param ctx Activity context
	 * @param deviceName name of the device to select
	 * @throws KmbApplicationException
	 */
	public void ActivateEobrDevice(Context ctx, String deviceName) throws KmbApplicationException
    {
        this.ActivateEobrDevice(ctx, deviceName, true);
    }

	/**
	 * Activates and Eobr via device name
	 *
	 * @param ctx Context from activity
	 * @param deviceName Name of device to activate
	 * @param resumeReading Determine if reading/timerpop
	 *                      should continue to function. This is used only for firmware.
	*/
    public void ActivateEobrDevice(Context ctx, String deviceName, boolean resumeReading) throws KmbApplicationException
    {
        // make the device go offline initially, and suspend any updates
        this.SuspendReading();
        this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format("ActivateEobrDevice: deviceName '%s'", deviceName));

        try{
            // activate the selected device
            this.InitializeEobrDevice(ctx, deviceName, true);
        }
        finally{

            // resume reading when the activation is complete
            if(resumeReading) {
				// Resume timer pops
				this.ResumeReading();
            }else{
				// Firmware Only
				ErrorLogHelper.RecordMessage(String.format("ActivateEobrDevice Skipped Resume\n%s", Log.getStackTraceString(new Exception())));
			}
        }
    }

    /// <summary>
    /// Activate and initialize the specified EOBR device, by name.
    /// This will cause communication to be established to the new EOBR.
    /// If activation fails, then an exception will be thrown.
    /// </summary>
    /// <param name="deviceName">name of the device to select</param>
    public void ActivateEobrDevice(Context ctx, String deviceName, String deviceAddress, short deviceCrc, int deviceGeneration) throws KmbApplicationException
    {
        // make the device go offline initially, and suspend any updates
        this.SuspendReading();
        this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format("ActivateEobrDevice: deviceName '%s'", deviceName));

        try{        	
	        // activate the selected device
	        this.InitializeEobrDevice(ctx, deviceName, deviceAddress, deviceCrc, deviceGeneration, true, false);	
        }
        finally{
        	// resume reading when the activation is complete
        	this.ResumeReading();
        }
    }

    // Overloaded so we can continue to use the KMBAPI code for the Testharness. 
    private void InitializeEobrDevice(Context ctx, String deviceName, boolean performTransitions) throws KmbApplicationException {
    	InitializeEobrDevice(ctx, deviceName, performTransitions, false);
    }

    /// <summary>
    /// Initialize the reader.
    /// ApplicationExceptions will be thrown if not sucessfully initialized.
    /// </summary>
    private void InitializeEobrDevice(Context ctx, String deviceName, boolean performTransitions, boolean fromTestHarness) throws KmbApplicationException
    {
        String clockNotSetCorrectlyMessage;
		String initializeLogginDeviceFailureExceptionMessage;
		String intializeLoggingDeviceFailureMessage;
		String initializingLoggingDeviceFailureMessage2;

		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            clockNotSetCorrectlyMessage = ctx.getString(R.string.eld_clock_not_set_correctly);
			initializeLogginDeviceFailureExceptionMessage = ctx.getString(R.string.errorlog_initializeeldexception);
			initializingLoggingDeviceFailureMessage2 = ctx.getString(R.string.errorlog_initializeeldfailedmessage);
			intializeLoggingDeviceFailureMessage = ctx.getString(R.string.errorlog_intializeeldfailed);
        } else {
			clockNotSetCorrectlyMessage = ctx.getString(R.string.eobr_clock_not_set_correctly);
			initializeLogginDeviceFailureExceptionMessage = ctx.getString(R.string.errorlog_initializeeobrexception);
			initializingLoggingDeviceFailureMessage2 = ctx.getString(R.string.errorlog_initializeeobrfailedmessage);
			intializeLoggingDeviceFailureMessage = ctx.getString(R.string.errorlog_intializeeobrfailed);

		}
		IEobrEngine eobrEngine = null;

    	// attempt to initialize the device
        this.setEobrIdentifier(deviceName);
        
        // need to determine if selected device is gen 1 or gen 2 and set
        // up the eobr engine.  Then need to initialize the eobr device
        if (_deviceList != null)
        {
        	for (EobrDeviceDescriptor desc : _deviceList)
        	{
        		if (desc.getName().equals(deviceName))
        		{
					eobrEngine = EobrEngineFactory.ForGeneration(desc.getEobrGen(), ctx);

    				EobrReader.getInstance().setEobrEngine(eobrEngine);
    				EobrReader.getInstance().getEobrEngine().initializeConnectedDevices();
    				EobrReader.getInstance().getEobrEngine().SetupActiveDevice(desc.getName(), desc.getAddress(), desc.getEobrGen(), desc.getCrc());
    				
    				break;
        		}
        	}
        }
        
        // Verify the specified device was found in the list and the eobrEngine created
        if (eobrEngine != null)
        {
	        int rc = EobrReader.getInstance().getEobrEngine().OpenDevice(deviceName);
	        if (rc == EobrReturnCode.S_SUCCESS)
	        {
	            // successfully initialized
	            // assume that the device is online, and functional at this point
	        	if(performTransitions)
	        		this.TransitionDeviceToNewState(ctx, ConnectionState.ONLINE, String.format(ctx.getString(R.string.errorlog_initializeeobrmessage), deviceName, this.getCurrentCommunicationMode()));
	
	            try
	            {	
	                // synchronize both the system clock and the EOBR clock
	            	boolean isInFailure = false;
	            	if (!fromTestHarness)
	            		isInFailure = this.PerformClockSynchronization(ctx);
	            	else
	            		isInFailure = this.TestHarness_PerformClockSynchronization(ctx);
	
	                // if the device were hiberating, then terminate the hiberation
	            	// TODO - terminate hibernation
	                //this.TerminateHiberation();
	
	            	if(!isInFailure){
		                // read and save the serial number from the EOBR
		                this.setEobrSerialNumber(this.Technician_GetSerialNumber(ctx));
		                this.setEobrDatabusType(this.Technician_GetBusType(ctx));
	            	}
	            	else{
	            		// if the clock is in failure, go offline
		            	throw new KmbApplicationException(clockNotSetCorrectlyMessage);
	            	}
	            	
	            }
	            catch (KmbApplicationException kae)
	            {
	            	if(performTransitions)
	            		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format(initializeLogginDeviceFailureExceptionMessage, deviceName, kae.toString()));
	            	throw kae;
	            }
	            catch (Throwable excp)
	            {
	                // if not successfull, then set the device OFFLINE
	            	if(performTransitions)
	            		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format(initializeLogginDeviceFailureExceptionMessage, deviceName, excp.toString()));
	                KmbApplicationException kae = new KmbApplicationException(excp.toString());
	                throw kae;
	            }
	        }
	        else
	        {
	            // errors occurred during initialization of the EOBR device
	            String msg = String.format(initializingLoggingDeviceFailureMessage2, rc);
	        	if(performTransitions)
	        		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, msg);
	            throw new KmbApplicationException(msg);
	       }
        }
        else
        {
            // errors occurred during initialization of the EOBR device
            String msg = String.format(intializeLoggingDeviceFailureMessage, deviceName);
        	if(performTransitions)
        		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, msg);
            throw new KmbApplicationException(msg);        	
        }
    }

    /// <summary>
    /// Initialize the reader.
    /// ApplicationExceptions will be thrown if not sucessfully initialized.
    /// </summary>
    private void InitializeEobrDevice(Context ctx, String deviceName, String deviceAddress, short deviceCrc, int deviceGeneration, boolean performTransitions, boolean fromTestHarness) throws KmbApplicationException
    {
		IEobrEngine eobrEngine = null;

    	// attempt to initialize the device
        this.setEobrIdentifier(deviceName);
		String clockNotSetCorrectlyMessage;
		String initializingLoggingDeviceFailed;
		String initializingLoggingDeviceFailedException;
		String initializingLoggingDeviceFailed2;
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			clockNotSetCorrectlyMessage = ctx.getString(R.string.eld_clock_not_set_correctly);
			initializingLoggingDeviceFailed = ctx.getString(R.string.errorlog_intializeeldfailed);
			initializingLoggingDeviceFailedException = ctx.getString(R.string.errorlog_initializeeldexception);
			initializingLoggingDeviceFailed2 = ctx.getString(R.string.errorlog_initializeeldfailedmessage);
		} else {
			clockNotSetCorrectlyMessage = ctx.getString(R.string.eobr_clock_not_set_correctly);
			initializingLoggingDeviceFailed = ctx.getString(R.string.errorlog_intializeeobrfailed);
			initializingLoggingDeviceFailedException = ctx.getString(R.string.errorlog_initializeeobrexception);
			initializingLoggingDeviceFailed2 = ctx.getString(R.string.errorlog_initializeeobrfailedmessage);
		}
		eobrEngine = EobrEngineFactory.ForGeneration(deviceGeneration, ctx);
        			
    	EobrReader.getInstance().setEobrEngine(eobrEngine);
    	EobrReader.getInstance().getEobrEngine().initializeConnectedDevices();
    	EobrReader.getInstance().getEobrEngine().SetupActiveDevice(deviceName, deviceAddress, deviceGeneration, deviceCrc);
    				        
        // Verify the specified device was found in the list and the eobrEngine created
        if (eobrEngine != null)
        {
	        int rc = EobrReader.getInstance().getEobrEngine().OpenDevice(deviceName);
			LogCat.getInstance().v("EobrReader", "RC:" + String.valueOf(rc));
	        if (rc == EobrReturnCode.S_SUCCESS)
	        {
	            // successfully initialized
	            // assume that the device is online, and functional at this point
	        	if(performTransitions)
	        		this.TransitionDeviceToNewState(ctx, ConnectionState.ONLINE, String.format(ctx.getString(R.string.errorlog_initializeeobrmessage), deviceName, this.getCurrentCommunicationMode()));
	
	            try
	            {	
	                // synchronize both the system clock and the EOBR clock
	            	boolean isInFailure = false;
	            	if (!fromTestHarness)
	            		isInFailure = this.PerformClockSynchronization(ctx);
	            	else
	            		isInFailure = this.TestHarness_PerformClockSynchronization(ctx);
	
	                // if the device were hiberating, then terminate the hiberation
	            	// TODO - terminate hibernation
	                //this.TerminateHiberation();
	
	            	
	            	if(!isInFailure){
		                // read and save the serial number from the EOBR
		                this.setEobrSerialNumber(this.Technician_GetSerialNumber(ctx));
		                this.setEobrDatabusType(this.Technician_GetBusType(ctx));
	            	}
	            	else{
	            		// if the clock is in failure, go offline
		            	throw new KmbApplicationException(clockNotSetCorrectlyMessage);
	            	}
	            }
	            catch (KmbApplicationException kae)
	            {
	            	if(performTransitions)
	            		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format(initializingLoggingDeviceFailedException, deviceName, kae.toString()));
	            	throw kae;
	            }
	            catch (Throwable excp)
	            {
	                // if not successfull, then set the device OFFLINE
	            	if(performTransitions)
	            		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format(initializingLoggingDeviceFailedException, deviceName, excp.toString()));
	                KmbApplicationException kae = new KmbApplicationException(excp.toString());
	                throw kae;
	            }
	        }
	        else
	        {
	            // errors occurred during initialization of the EOBR device
	            String msg = String.format(initializingLoggingDeviceFailed2, rc);
	        	if(performTransitions)
	        		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, msg);
	            throw new KmbApplicationException(msg);
	       }
        }
        else
        {
            // errors occurred during initialization of the EOBR device
            String msg = String.format(initializingLoggingDeviceFailed, deviceName);
        	if(performTransitions)
        		this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, msg);
            throw new KmbApplicationException(msg);        	
        }
    }

//********************************************************************************
// EOBR Discovery/Connectivity - END
//********************************************************************************
    
    public void TransitionDeviceToNewState(Context ctx, ConnectionState newState)
    {
    	this.TransitionDeviceToNewState(ctx, newState, "");
    }
    
    @Override
	public void TransitionDeviceToNewState(Context ctx, ConnectionState newState, String message)
    {
    	ConnectionState previousState = getCurrentConnectionState();
    	this.setCurrentConnectionState(newState);
    	String eobrId = this.getEobrIdentifier();
    	if (eobrId == null) eobrId = "";

		Log.i("EobrReader", String.format("TransitionDeviceToNewState from: %s to: %s", previousState, newState));
		switch (newState)
    	{
    		case ONLINE:
    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobronline), eobrId, message));
    			break;
    		
    		case OFFLINE:
    			this.setEobrOfflineTimestamp(DateUtility.getCurrentDateTimeWithSecondsUTC());

				if(previousState == ConnectionState.ONLINE)
					this._previousBtAddress = this.getCurrentBtAddress();

    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobroffline), eobrId, message));
    			break;
    		
    		case READINGHISTORICAL:
    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobrreadinghistory), eobrId, message));
    	    	break;
    	    	
    		case FIRMWAREUPDATE:
    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobrfirmwareupdate), eobrId, message));
    			break;
    	    	
    		case DEVICEFAILURE:
    			// permanently disable reading when a failure occurs
    			this.SuspendReading();
    			
    			// 2014.06.23 sjn - Create a failure to record on the current log
        		LogEntryController logEntryController = new LogEntryController(ctx);
        		EmployeeLog empLog = logEntryController.getCurrentEmployeeLog();
    			FailureController ctrl = new FailureController(ctx);
				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					ctrl.ReportEobrFailure(empLog, DateUtility.getCurrentDateTimeWithSecondsUTC(), ctx.getString(R.string.msgelddevicefailure));
				} else {
					ctrl.ReportEobrFailure(empLog, DateUtility.getCurrentDateTimeWithSecondsUTC(), ctx.getString(R.string.msgeobrdevicefailure));
				}

				PublishVerifySpecialDrivingEnd(previousState, newState);

    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobrdevicefailure), eobrId, message));
    	    	break;
    	    
    		case SHUTDOWN:
				PublishVerifySpecialDrivingEnd(previousState, newState);

    			ErrorLogHelper.RecordMessage(ctx, String.format(ctx.getString(R.string.errorlog_eobrshutdown), eobrId, message));
    	    	break;
    	}
    	
    	if (EobrReader.getInstance().isEobrGen1())
    		this.PublishEobrStatusChange(newState);
    	else
    		this.PublishEobrStatusChange(0, new EventRecord(), newState, true);
    }

	public boolean IsEobrConfigurationNeeded()
    {
    	return this.getEobrSerialNumber().equalsIgnoreCase(this.getEobrIdentifier());
    }
    
//********************************************************************************
// EOBR Interface Methods - Start
//********************************************************************************

    public Date Technician_ReadClockUniversalTime(Context ctx)
    {
    	Date utcTime = null;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	
    	if (eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.GetClockUTC();
    		
    		if (bundle != null)
    		{
    			int status = bundle.getInt(ctx.getString(R.string.rc));
    			if (status == EobrReturnCode.S_SUCCESS)
        			utcTime = new Date(bundle.getLong(ctx.getString(R.string.returnvalue)));
    			else
    				this.Technician_CheckBTReturnStatus(status);
    		}
    	}
    	
    	return utcTime;
    }

	public Date Technician_ReadGPSTime()
	{
		Date gpsTime = null;
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

		if (eobrEngine != null)
		{
			EobrResponse<Date> result = eobrEngine.GetGPSTimestamp();

			if(result != null && result.getReturnCode() == EobrReturnCode.S_SUCCESS)
			{
				Date gpsDate = result.getData();
				if(gpsDate != null)
					gpsTime = gpsDate;
			}
		}

		return gpsTime;
	}
    
    public void Technician_SetClockUniversalTime(Context ctx, Date newTimestampUtc)
    {
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		int rc = eobrEngine.SetClockUTC(newTimestampUtc);
    		if (rc != EobrReturnCode.S_SUCCESS)
    		{
    			this.Technician_CheckBTReturnStatus(rc);
    			this.TransitionDeviceToNewState(ctx, ConnectionState.OFFLINE, String.format(ctx.getString(R.string.errorlog_setclockbadreturncode), Integer.toString(rc)));
    		}
    	}
    }
        
    /// <summary>
    /// Read the odometer calibration values
    /// </summary>
    @Override
	public Bundle Technician_ReadOdometerCalibrationValues()
    {	
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	Bundle bundle = null;
    	
    	if (eobrEngine != null)
    	{
    		bundle = eobrEngine.GetOdometerCalibration();
    		if (bundle != null)
    		{
    			int status = bundle.getInt(RETURNCODE);
    			this.Technician_CheckBTReturnStatus(status);
    		}
    	}
    	else
    	{
    		bundle = new Bundle();
    		bundle.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
    	}
    	
    	return bundle;
    }

    /// <summary>
    /// Sets the odometer calibration values
    /// </summary>
    /// <param name="collectionRateSeconds"></param>
    /// <returns></returns>
    @Override
	public int Technician_SetOdometerCalibration(float offset, float multiplier)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.SetOdometerCalibration(offset, multiplier);
    		this.Technician_CheckBTReturnStatus(rc);
    	}

    	return rc;
    }    
    
    public int Technician_GetCurrentData(StatusRecord statusRec, boolean updateRefTimestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.GetEobrData(statusRec, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), 0xffffffff, null, new StatusRecordMotionOptionEnum(StatusRecordMotionOptionEnum.NEXTRECORD), updateRefTimestamp);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }    
    
    public int Technician_GetHistoricalData(StatusRecord statusRec, Date timestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{    	
    		rc = eobrEngine.GetEobrData(statusRec, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP), 0, timestamp, new StatusRecordMotionOptionEnum(StatusRecordMotionOptionEnum.NEXTRECORD), false);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }

	public int Technician_GetHistoricalData(StatusRecord statusRec, int recordId)
	{
		return Technician_GetHistoricalData(statusRec, recordId, true);
	}

    public int Technician_GetHistoricalData(StatusRecord statusRec, int recordId, boolean performDataRecordingMalfunctionCheck)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{    	
    		rc = eobrEngine.GetEobrData(statusRec, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), recordId, null, new StatusRecordMotionOptionEnum(StatusRecordMotionOptionEnum.NEXTRECORD), false);

    		if (performDataRecordingMalfunctionCheck) {
				this.PrepareForDataRecordingMalfunctionCheck(recordId, statusRec.getRecordId());
			}

    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }
    
    public int Technician_GetNextVehicleMotionChangeData(StatusRecord statusRec, Date timestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.GetEobrData(statusRec, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP), 0, timestamp, new StatusRecordMotionOptionEnum(StatusRecordMotionOptionEnum.NEXTMOTIONCHANGE), false);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
		return rc;
    }

    public String Technician_GetUniqueIdentifier(Context ctx)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	String uniqueId = null;
    	
    	if (eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.GetUnitId();

    		if (bundle != null)
    		{
    			int status = bundle.getInt(ctx.getString(R.string.rc));
    			this.Technician_CheckBTReturnStatus(status);

    			uniqueId = bundle.getString(ctx.getString(R.string.returnvalue));
    		}
    	}
    	
    	return uniqueId;
    }
    
    @Override
	public int Technician_SetUniqueIdentifier(String uniqueId)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		// remember the macAddress of the current TAB
    		String macAddress = eobrEngine.GetActiveDeviceAddress();

    		// Note:  this call should fail - setting the unitId resets bluetooth
    		// in the TAB.  Therefore, after writing the command, when we attempt
    		// to read the response we no longer have a connection to the TAB.
    		rc = eobrEngine.SetUnitId(uniqueId);    	
    		if (rc == EobrReturnCode.S_SUCCESS)
    			this.setEobrIdentifier(uniqueId);
    	
	    	// for gen II, setting the unit Id resets Bluetooth in the TAB which
	    	// causes the connection to be lost - need to rediscover/reactivate to the TAB
	    	if (!this.isEobrGen1() && macAddress != null)
	    	{	 	    		
	    		// shutdown the eobr
	    		try {
					Technician_ShutdownEobrDevice(GlobalState.getInstance());
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordMessage(String.format("%s %s", GlobalState.getInstance().getString(R.string.eobrerror_setunitid), e.getDisplayMessage()));
				}
	
				// rediscover and activate the eobr for specific mac address
	    		try {
	    			// unpair from the device as the bluetooth "friendly" name has now changed - the device
	    			// retains the friendly name from when pairing occurred
	    			this.UnpairFromDevice(macAddress);

					Bundle bundle = PerformDeviceDiscoveryAndActivationFromMACAddressForFirmwareUpdate(GlobalState.getInstance(), macAddress, false, null);
					if (bundle.containsKey(GlobalState.getInstance().getString(R.string.rc)))
					{
						rc = bundle.getInt(GlobalState.getInstance().getString(R.string.rc));
					}
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordMessage(String.format("%s %s", GlobalState.getInstance().getString(R.string.eobrerror_setunitid), e.getDisplayMessage()));
					rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
				}
	    	}
    	}
    			
    	return rc;
    }
    
    /// <summary>
    /// Answer the Serial Number assigned to the last active EOBR device
    /// </summary>
    /// <returns></returns>
    public String Technician_GetSerialNumber(Context ctx) throws KmbApplicationException
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
        String serialNumber = null;
        int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
        // if eobr engine is defined
        if (eobrEngine != null)
        {
	        Bundle bundle = eobrEngine.GetEobrSerialNumber();
	        rc = bundle.getInt(ctx.getString(R.string.rc));
	        serialNumber = bundle.getString(ctx.getString(R.string.returnvalue));
	        
	        this.Technician_CheckBTReturnStatus(rc);
	        if (rc != EobrReturnCode.S_SUCCESS || serialNumber == null || serialNumber.equals(""))
	        {
				String readSerialNumberFailedMessage;
				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					readSerialNumberFailedMessage = ctx.getString(R.string.errorlog_readserialnumberfailed_eld);
				} else {
					readSerialNumberFailedMessage = ctx.getString(R.string.errorlog_readserialnumberfailed_eobr);

				}
	            // read of property failed
	            throw new KmbApplicationException(String.format(readSerialNumberFailedMessage, rc));
	        }
            else
            {
                //Only spoof EOBR if able to successfully read the real one
                //TODO: Add EOBR Spoof Boolean check and Serial value from app.Config
            }
        }
        
        return serialNumber;
    }

    public int Technician_GetEngineOffCommsTimeoutDuration(Context ctx)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int timeout = 0;
    	
    	if (eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.GetEngineOffCommsTimeout();
    		
    		int rc = bundle.getInt(ctx.getString(R.string.rc));
    		this.Technician_CheckBTReturnStatus(rc);
    		
    		timeout = bundle.getInt(ctx.getString(R.string.returnvalue));
    	}
    	
    	return timeout;
    }
    
    public int Technician_SetEngineOffCommsTimeoutDuration(int timeoutMinutes)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.SetEngineOffCommsTimeout(timeoutMinutes);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
    	return rc;
    }
    
    public int Technician_GetDataCollectionRate(Context ctx)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int dataInterval = 1;
    	
    	if (eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.ReadDataCollectionRate();

    		if (bundle != null)
    		{
    			int status = bundle.getInt(ctx.getString(R.string.rc));
    			this.Technician_CheckBTReturnStatus(status);

    			if (bundle.containsKey(ctx.getString(R.string.returnvalue)))
    				dataInterval = bundle.getInt(ctx.getString(R.string.returnvalue));
    		}
    	}
    	
    	return dataInterval;
    }
    
    public int Technician_SetDataCollectionRate(int collectionRateSeconds)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.ChangeDataCollectionRate(collectionRateSeconds);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
    	return rc;
    }
        
    public String Technician_GetCompanyPassKey(Context ctx)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	String companyPassKey = null;
    	
    	if (eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.GetCompanyPasskey();
    		
    		if (bundle != null)
    		{
    			int status = bundle.getInt(ctx.getString(R.string.rc));
    			
    			if (status == EobrReturnCode.S_SUCCESS)
    				companyPassKey = bundle.getString(ctx.getString(R.string.returnvalue));
    			else
    				this.Technician_CheckBTReturnStatus(status);
    		}
    	}
    	
    	return companyPassKey;
    }
    
    public int Technician_SetCompanyPassKey(String passkey)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.SetCompanyPasskey(passkey);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
    	return rc;
    }

	public Bundle Technician_GetDistHours(long timecode)
	{
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		Bundle bundle = null;

		if (eobrEngine != null)
		{
			bundle = eobrEngine.GetDistHours(timecode);

			if (bundle != null)
			{
				int status = bundle.getInt(RETURNCODE);
				this.Technician_CheckBTReturnStatus(status);
			}
		}
		else
		{
			bundle = new Bundle();
			bundle.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
		}

		return bundle;
	}

    public Bundle Technician_ReadReferenceTimestamp()
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	Bundle bundle = null;
    	
    	if (eobrEngine != null)
    	{
    		bundle = eobrEngine.GetReferenceTimestamp();
    		
    		if (bundle != null)
    		{
    			int status = bundle.getInt(RETURNCODE);
    			this.Technician_CheckBTReturnStatus(status);

				if (status == EobrReturnCode.S_SUCCESS) {
					long refTimestamp = bundle.getLong(EVENTREFTIME);

					// determine if the EOBR event reference timestamp is before the expected timestamp (the last reference timestamp stored in the database)
					if (!isReferenceTimestampCurrent(refTimestamp)) {
						long lastReferenceTimestampFromDB = getLastReferenceTimestampFromDB();

						bundle.putLong(EVENTREFTIME, lastReferenceTimestampFromDB);

						String message = String.format("EobrReader::Technician_ReadReferenceTimestamp  Using database LastEventReferenceTimestamp because the ELD reference timestamp ('%s') is before the expected timestamp stored in the database ('%s')\r",
								DateFormat.format("EEE, MMM d, yyyy hh:mm:ss a", new Date(refTimestamp)).toString(),
								DateFormat.format("EEE, MMM d, yyyy hh:mm:ss a", new Date(lastReferenceTimestampFromDB)).toString());
						ErrorLogHelper.RecordMessage(message);
					}
				}
    		}
    	}
    	else
    	{
    		bundle = new Bundle();
    		bundle.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
    	}
    	
		return bundle;
    }
    
	public Date Technician_ReadReferenceTimestamp(Context ctx)
	{
		Date utcTime = null;

		Bundle bundle = this.Technician_ReadReferenceTimestamp();

		if (bundle != null)
		{
			int status = bundle.getInt(ctx.getString(R.string.rc));

			if (status == EobrReturnCode.S_SUCCESS)
				utcTime = new Date(bundle.getLong(ctx.getString(R.string.returnvalue)));
			else
				this.Technician_CheckBTReturnStatus(status);
		}
		
		return utcTime;
	}

	public String GetVin() {
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		String vin = "";
		if (eobrEngine != null) {
			Bundle vinBundle = eobrEngine.GetVin();

			if (vinBundle != null) {
				int status = vinBundle.getInt("rc");

				if (status == EobrReturnCode.S_SUCCESS) {
					if (vinBundle.containsKey("ReturnValue")) {
						vin = vinBundle.getString("ReturnValue");
					}
				}
			}
		}
		return vin;
	}

	@Override
	public DatabusTypeEnum Technician_GetBusType(Context ctx)
	{
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		DatabusTypeEnum busTypeEnum = new DatabusTypeEnum(DatabusTypeEnum.NULL);
		
		if (eobrEngine != null)
		{
			Bundle bundle = eobrEngine.GetActiveBusType();

			if (bundle != null)
			{
				int status = bundle.getInt(ctx.getString(R.string.rc));

				if (status == EobrReturnCode.S_SUCCESS)
				{
					if (bundle.containsKey(ctx.getString(R.string.returnvalue)))
					{
						int busTypeValue = bundle.getInt(ctx.getString(R.string.returnvalue));
						
						// if working with Gen I device, need to map values of 1 and 2 returned
						// by Gen I eobr to the correct values in the databusType enum
						if (this.isEobrGen1())
						{
							switch(busTypeValue)
							{
							case 1: // J1708
								busTypeEnum.setValue(DatabusTypeEnum.J1708);
								break;
							case 2: // J1939
								busTypeEnum.setValue(DatabusTypeEnum.J1939);
								break;
							}
						}
						else
							busTypeEnum.setValue(bundle.getInt(ctx.getString(R.string.returnvalue)));
					}
				}
				else
					this.Technician_CheckBTReturnStatus(status);
			}
		}
		
		return busTypeEnum;
	}
	
	@Override
	public int Technician_SetBusType(int busType)
	{
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.ChangeActiveBusType(busType);
    		this.Technician_CheckBTReturnStatus(rc);
    	}

    	//save the new databus type
    	if(rc == EobrReturnCode.S_SUCCESS)
    	{
    		DatabusTypeEnum databus;
    		
    		if(this.isEobrGen1())
    		{
    			if(busType == 1)
    				databus = new DatabusTypeEnum(DatabusTypeEnum.J1708);
    			else
    				databus = new DatabusTypeEnum(DatabusTypeEnum.J1939);
    		} else
    			databus = new DatabusTypeEnum(busType);
    		
    		this.setEobrDatabusType(databus);
    	}
    	
		return rc;
	}

	@Override
	public Bundle Technician_GetEOBRRevisions()
	{
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	Bundle bundle = null;
    	
    	if (eobrEngine != null)
    	{
    		bundle = EobrReader.getInstance().getEobrEngine().GetEOBRDllRevisions();
    		
    		if (bundle != null)
    		{
    			int status = bundle.getInt(RETURNCODE);
    			this.Technician_CheckBTReturnStatus(status);
    		}
    	}
    	else
    	{
    		bundle = new Bundle();
    		bundle.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
    	}
    	
		return bundle;
	}
	
    public int Technician_SetDebugFlag(int flag)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.SetDebugFlags(flag);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }
    
	public Bundle SendConsoleCommand(String command) {
		
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		Bundle answer = new Bundle();
		answer.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
		
		if (eobrEngine != null) {
			answer = eobrEngine.SendConsoleCommandToDevice(command);
			this.Technician_CheckBTReturnStatus(answer.getInt(RETURNCODE));
		}
		
    	return answer;
	}
    
    /// <summary>
    /// Clear all record data from the EOBR
    /// </summary>
    /// <returns></returns>
    public int Technician_ClearAllRecordData(String password, int clearFlags)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.ClearAllRecordData(clearFlags);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }
    
    /// <summary>
    /// Clear all record data from the EOBR
    /// </summary>
    /// <returns></returns>
    public void Technician_ClearAllEobrData(String password)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	
    	if(eobrEngine == null)
    		throw new DeviceNotConnectedEobrException();
    	    
    	try {
    		eobrEngine.ClearAllEobrData();
		} catch (EobrException e) {
			this.Technician_CheckBTReturnStatus(e.getReturnCode());
			throw e;
		}
    }
    
    @Override
	public int Technician_DownloadFirmwareUpdate(InputStream firmwareUpdateFile,
												 Enums.FirmwareUpgradeTypeEnum firmwareUpgradeType,
												 FirmwareUpdate firmwareUpdateConfig)
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{    		
    		rc = eobrEngine.DownloadFirmwareUpdate(firmwareUpdateFile, firmwareUpgradeType, broadcaster, firmwareUpdateConfig);
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }
    
    @Override
	public boolean Technician_GetEobrHardware(Context ctx)
    {
    	boolean isJJK = true;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

		if (eobrEngine != null)
		{
			isJJK =  eobrEngine.IsJJK(ctx);
		}
			
		return isJJK;
	}
        
    @Override
	public FirmwareUpgradeRequestResult Technician_RequestFirmwareUpdate(long firmwarePatchId)
    {
    	FirmwareUpgradeRequestResult result = null;
    	IEobrEngine engine = getEobrEngine();
    	
    	if(engine != null)
    		result = engine.RequestFirmwareUpgrade(firmwarePatchId);
    	
    	return result;
    }
    
    @Override
	public FirmwareUpgradeStatusResult Technician_GetFirmwareUpdateStatus()
    {
    	FirmwareUpgradeStatusResult result = null;
    	IEobrEngine engine = getEobrEngine();
    	
    	if(engine != null)
    		result = engine.GetFirmwareUpgradeStatus();
    	
    	return result;
    }
    
    @Override
	public void Technician_ShutdownEobrDevice(Context ctx) throws KmbApplicationException
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	// gracefully shutdown the eobr device
        if (eobrEngine != null)
        {
        	// 11/9/11 JHM - If the user is null (logging out), don't perform transition because a NullRefException
        	// occurs attempting to do some work with the log which is irrelevant at this point.
        	if(GlobalState.getInstance().getCurrentUser() != null) {
				this.TransitionDeviceToNewState(ctx, ConnectionState.SHUTDOWN, "ShutdownEobrDevice");
			}
            this.setEobrIdentifier(null);
            this.setEobrSerialNumber(null);
            this.setEobrDatabusType(null);

            int rc = eobrEngine.CloseDevice();
            eobrEngine.deleteBTAddress();
            if (rc != EobrReturnCode.S_SUCCESS)
            {
				String shutdownFailedMessage;
				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					shutdownFailedMessage = ctx.getString(R.string.msg_shutdowneldfailed);
				} else {
					shutdownFailedMessage = ctx.getString(R.string.msg_shutdowneobrfailed);

				}
                // what to do if the shutdown fails, should an exception be thrown?
                throw new KmbApplicationException(String.format(shutdownFailedMessage, rc));
            }
        }
    }

    /// <summary>
    /// This function performs a test of the EOBR connection. This function will be used to test
    /// the connectivity and the basic functionality of the EOBR device.
    /// </summary>
    /// <returns>return code: S_SUCCESS / S_DEV_NOT_CONNECTED / S_GENERAL_ERROR / S_COMMS_BUSY</returns>
    public int Technician_TestConnection()
    {
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if (eobrEngine != null)
    	{
    		rc = eobrEngine.PingEobrDevice();
    		this.Technician_CheckBTReturnStatus(rc);
    	}
    	
        return rc;
    }     

    /// <summary>
    /// Check the BT return status, if "Dev not Connected" happen, close socket. 
    /// </summary>
    private void Technician_CheckBTReturnStatus(int errorCode)
    {    	
        // "Dev not Connected" is return when sendBulkData/receiveBulkData fail(Out of range).
        // "S_WRONG_COMMAND" is return when the receive reply data is wrong(packet out of sync).
        if (errorCode == (int)EobrReturnCode.S_DEV_NOT_CONNECTED || errorCode == (int)EobrReturnCode.S_GENERAL_ERROR)
        {
            //close socket, because maybe in the middle of sending, receiving data
            //user go out of range. This will cause receiveBulkData function in
            //cpp layer to return "Dev Not Connected".
        	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
        	if (eobrEngine != null)
        		eobrEngine.CloseDevice();
        }
    }

    /**
     * Starts a self test on the EOBR. Returns whether or not the test was started successfully.
     */
	public boolean Technician_SetSelfTest()
	{
		IEobrEngine eobrEngine = getEobrEngine();
		return (eobrEngine == null) ? false : eobrEngine.SetSelfTest();
	}

	/**
	 * Gets the result of the last self test
	 * @param testResult A test result object to fill in with the results
	 * @return The status code from the EOBR
	 */
	public int Technician_GetSelfTest(Context context, EobrSelfTestResult testResult)
	{
		int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
		IEobrEngine eobrEngine = getEobrEngine();
		
		if (eobrEngine != null)
		{
			Bundle response = getEobrEngine().GetSelfTest();
			returnCode = response.getInt(context.getString(R.string.rc));
			if (returnCode == EobrReturnCode.S_SUCCESS)
			{
				int errorCode = response.getInt(context.getString(R.string.returnvalue));
				testResult.setSuccessful(errorCode == 0);
				testResult.setErrorCode(errorCode);
			}
		}
		
		return returnCode;
	}

	public Bundle Technician_GetThresholds(Context ctx, int thresholdType)
	{
		Bundle response = new Bundle();
		response.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);

		IEobrEngine eobrEngine = getEobrEngine();
		if (eobrEngine != null)
		{
			response = getEobrEngine().GetThresholdValues(thresholdType);
			
			int returnCode = response.getInt(ctx.getString(R.string.rc));
    		this.Technician_CheckBTReturnStatus(returnCode);
		}

		return response;
	}

	public Bundle Technician_SetThresholds(Context ctx, int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, float driveStartSpeed)
	{
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	Bundle bundle = new Bundle();
    	bundle.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
    	
    	if (eobrEngine != null)
    	{
    		bundle = eobrEngine.SetThresholdValues(rpmThreshold, speedThreshold, hardBrakeThreshold, driveStartDistance, driveStopTime, eventBlanking, driverId, driveStartSpeed);
    		this.Technician_CheckBTReturnStatus(bundle.getInt(RETURNCODE));
    	}

		return bundle;
	}
	
	public int Technician_GetEventData(EventRecord eventRec, StatusRecordQueryMethodEnum queryMethodEnum, EventTypeEnum eventTypeEnum, long refTimestamp, boolean updateRefTimestamp)
	{
		int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

		if (eobrEngine != null)
		{
			if (queryMethodEnum.getValue() == StatusRecordQueryMethodEnum.TIMESTAMP) {
				rc = eobrEngine.GetEventData(eventRec, queryMethodEnum, 0, refTimestamp, eventTypeEnum, updateRefTimestamp);
			}
			else {
				int lookupRecordId = eventRec.getRecordId();
				rc = eobrEngine.GetEventData(eventRec, queryMethodEnum, lookupRecordId, -1, eventTypeEnum, updateRefTimestamp);
				this.PrepareForDataRecordingMalfunctionCheck(lookupRecordId, eventRec.getRecordId());
			}

			this.Technician_CheckBTReturnStatus(rc);
		}

		return rc;
	}

    public int Technician_GetTripData(TripReport tripRec, StatusRecordQueryMethodEnum queryMethodEnum, long refTimestamp, boolean updateRefTimestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		if(queryMethodEnum.getValue() == StatusRecordQueryMethodEnum.TIMESTAMP) {
				rc = eobrEngine.GetTripData(tripRec, queryMethodEnum, tripRec.getRecordId(), refTimestamp, updateRefTimestamp);
			}
    		else {
    			int lookupRecordId = tripRec.getRecordId();
				rc = eobrEngine.GetTripData(tripRec, queryMethodEnum, tripRec.getRecordId(), -1, updateRefTimestamp);
				this.PrepareForDataRecordingMalfunctionCheck(lookupRecordId, tripRec.getRecordId());
			}

	    	this.Technician_CheckBTReturnStatus(rc);	    	
    	}
    	
        return rc;
    }
    
    public int Technician_GetHistogramData(HistogramData histogramData, StatusRecordQueryMethodEnum queryMethodEnum, HistogramTypeEnum histogramType, long refTimestamp, boolean updateRefTimestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		if(queryMethodEnum.getValue() == StatusRecordQueryMethodEnum.TIMESTAMP)
    			rc = eobrEngine.GetHistogramData(histogramData, queryMethodEnum, histogramData.getRecordId(), refTimestamp, histogramType, updateRefTimestamp);
    		else
    			rc = eobrEngine.GetHistogramData(histogramData, queryMethodEnum, histogramData.getRecordId(), -1, histogramType, updateRefTimestamp);    	

	    	this.Technician_CheckBTReturnStatus(rc);	    	
    	}
    	
        return rc;    	
    }
    
    public int Technician_GetJbusDiagnosticData(JbusDiagnosticData diagnosticData, StatusRecordQueryMethodEnum queryMethodEnum, long refTimestamp, boolean updateRefTimestamp)
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null)
    	{
    		if(queryMethodEnum.getValue() == StatusRecordQueryMethodEnum.TIMESTAMP)
    			rc = eobrEngine.GetJBusDiagnosticDataFromDevice(diagnosticData, queryMethodEnum, diagnosticData.getRecordId(), refTimestamp, updateRefTimestamp);
    		else
    			rc = eobrEngine.GetJBusDiagnosticDataFromDevice(diagnosticData, queryMethodEnum, diagnosticData.getRecordId(), -1, updateRefTimestamp);    	

	    	this.Technician_CheckBTReturnStatus(rc);	    	
    	}
    	
        return rc;    	
    }
    
    /**
     * @param startDate
     * @param endDate
     * @return The console log between startDate and endDate
     * @throws KmbApplicationException 
     */
    public String Technician_GetConsoleLog(Context ctx, Date startDate, Date endDate) throws KmbApplicationException
    {
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	String log = null;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if(eobrEngine != null)
    	{
    		Bundle bundle = eobrEngine.GetConsoleLog(startDate, endDate);
    		
    		if(bundle != null)
    		{
    			rc = bundle.getInt(RETURNCODE);
    			
    			this.Technician_CheckBTReturnStatus(rc);
    			
    			if(rc == EobrReturnCode.S_SUCCESS) {
					log = bundle.getString(RETURNVALUE);
				} else {
					String readConsoleLogFailedMessage;
					if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
						readConsoleLogFailedMessage = ctx.getString(R.string.erorrLog_readconsolelogfailed_eld);
					} else {
						readConsoleLogFailedMessage = ctx.getString(R.string.erorrLog_readconsolelogfailed_eobr);

					}
					throw new KmbApplicationException(String.format(readConsoleLogFailedMessage, rc));
				}
    				
    		}
    	}
    	
    	return log;
    }
    
    public Bundle Technician_GetConsoleLogAsBundle(Context ctx, Date startDate, Date endDate) throws KmbApplicationException {
    	
    	int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;    	
    	Bundle bundle = null;
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();

    	if (eobrEngine != null) {
    		bundle = eobrEngine.GetConsoleLog(startDate, endDate);
    		
    		if (bundle != null) {
    			rc = bundle.getInt(RETURNCODE);    			
    			this.Technician_CheckBTReturnStatus(rc);
    			
    			if (rc != EobrReturnCode.S_SUCCESS) {
					String readConsoleLogFailedMessage;

					if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
						readConsoleLogFailedMessage = ctx.getString(R.string.erorrLog_readconsolelogfailed_eld);
					} else {
						readConsoleLogFailedMessage = ctx.getString(R.string.erorrLog_readconsolelogfailed_eobr);

					}
					throw new KmbApplicationException(String.format(readConsoleLogFailedMessage, rc));
				}
    		}
    	}
    	
    	return bundle;
    }

	public int Technician_GetDriverEvent(EventRecord eventRecord, long startRefTimestamp, long endRefTimestamp, boolean includeEventsWithoutDriverId, EventTypeEnum... eventTypes)
	{
		int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		if (eobrEngine != null)
		{
			int eventMask = new EventMaskBuilder().withEventTypes(eventTypes).build();
			rc = eobrEngine.GetDriverEvent(eventRecord, startRefTimestamp, endRefTimestamp, eventMask, includeEventsWithoutDriverId);
			this.Technician_CheckBTReturnStatus(rc);
		}

		return rc;
	}

	public Bundle Technician_GetDriverCount(long startRefTimestamp, long endRefTimestamp)
	{
		int rc = EobrReturnCode.S_DEV_NOT_CONNECTED;
		Bundle bundle = null;
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		if (eobrEngine != null)
		{
			bundle = eobrEngine.GetDriverCount(startRefTimestamp, endRefTimestamp);

			if(bundle != null)
			{
				rc = bundle.getInt(RETURNCODE);
				this.Technician_CheckBTReturnStatus(rc);
			}
		}

		return bundle;
	}
        
    
//********************************************************************************
// EOBR Interface Methods - End
//********************************************************************************

    /// <summary>
    /// Add the status record to the history list.
    /// Verify that the status record being added does not repeat the 
    /// info from the last entry in the list.
    /// The new status must be different from the last one in the list.  
    /// The new status should represent a transition to stopped, or a 
    /// transition to moving (when compared to the last entry in the list).
    /// </summary>
    /// <param name="list"></param>
    /// <param name="statusRec"></param>
    public void AddToHistoryList(ArrayList<StatusRecord> list, StatusRecord statusRecToAdd)
    {
        boolean addToList = true;
        if (list.size() > 0)
        {
            StatusRecord lastRecord = list.get(list.size()-1);
            if ((statusRecToAdd.getOverallStatus() == 0 && lastRecord.getOverallStatus() == 0) || 
                (statusRecToAdd.getOverallStatus() != 0 && lastRecord.getOverallStatus() != 0))
            {
                // either both records have good status, or both have bad status
                // now, verify that the new record either transitions to moving, 
                // or transitions to a stop
                if (statusRecToAdd.getSpeedometerReading() > 0 && lastRecord.getSpeedometerReading() > 0)
                {
                    // both records are moving, so ignore the new status
                    addToList = false;
                }
                else if (statusRecToAdd.getSpeedometerReading() <= 0 && lastRecord.getSpeedometerReading() <= 0)
                {
                    // both records are stopped, so ignore the new status
                    addToList = false;
                }
            }
        }

        if (addToList) list.add(statusRecToAdd);
    }

    public int ResetHistoryData()
    {
    	int rc = EobrReader.getInstance().Technician_GetCurrentData(new StatusRecord(), true);
    	if (rc == EobrReturnCode.S_SUCCESS && !EobrReader.getInstance().isEobrGen1())
    	{
    		final int latestRecordId = 0xffffffff;
    		final StatusRecordQueryMethodEnum recordIdQueryType = new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID);
    		
    		EventRecord event = new EventRecord();
    		event.setRecordId(latestRecordId);
    		rc = EobrReader.getInstance().Technician_GetEventData(event, recordIdQueryType, new EventTypeEnum(EventTypeEnum.ANYTYPE), 0, true);
    		
    		if (rc == EobrReturnCode.S_SUCCESS)
    		{
	    		TripReport trip = new TripReport();
	    		trip.setRecordId(latestRecordId);
	    		rc = EobrReader.getInstance().Technician_GetTripData(trip, recordIdQueryType, 0, true);
    		}

    		if (rc == EobrReturnCode.S_SUCCESS)
    		{
	    		HistogramData histogram = new HistogramData();
	    		histogram.setRecordId(latestRecordId);
	    		rc = EobrReader.getInstance().Technician_GetHistogramData(histogram, recordIdQueryType, new HistogramTypeEnum(HistogramTypeEnum.VEHICLESPEED), 0, true);
    		}

    		if (rc == EobrReturnCode.S_SUCCESS)
    		{
				JbusDiagnosticData diagnosticData = new JbusDiagnosticData();
				diagnosticData.setRecordId(latestRecordId);
				rc = EobrReader.getInstance().Technician_GetJbusDiagnosticData(diagnosticData, recordIdQueryType, 0, true);
    		}
    	}
        return rc;
    }

    /// <summary>
    /// Answer if all data from the J-bus is being received correctly.
    /// </summary>
    /// <returns></returns>
    public boolean TestJBusConfig()
    {
        boolean isJbusDataConfigured = false;
        StatusRecord statusRec = new StatusRecord();
        int rc = EobrReader.getInstance().Technician_GetCurrentData(statusRec, false);
        if (rc == 0)
        {
            if (statusRec.getSpeedometerReading() >= 0 && statusRec.getOdometerReading() >= 0 && statusRec.getTachometer() >= 0)
            {
                // the J-Bus is reading data correctly as soon as each value is at least 0
                // note: when the j-bus is not responding, then each value will be -1.0M
                isJbusDataConfigured = true;
            }
        }
        
        return isJbusDataConfigured;
    }

    /// <summary>
    /// Validate the odometer is valid based on the last eobr odometer setting and the
    /// date of the last eobr odometer setting
    /// </summary>
    /// <param name="odometerTime">Timestamp of odometer we are checking</param>
    /// <param name="odometer">Odometer value to determine if valid</param>
    /// <returns>true if valid, false if not</returns>
    public Bundle OdometerInValidRange(Date odometerTime, float odometer)
    {
    	Bundle retVal = new Bundle();
        boolean isInValidRange = false;
        float startRange = -1F;
        float endRange = -1F;

        // if last eobr odometer time isn't defined, can't validate the odometer
        if (this.getLastEobrOdometerUTCTime() == null)
        {
        	retVal.putBoolean(ISINVALIDRANGE, true);
        	retVal.putFloat(STARTRANGE, startRange);
        	retVal.putFloat(ENDRANGE, endRange);
            return retVal;
        }

        float dayDiff = (float)(odometerTime.getTime() - this.getLastEobrOdometerUTCTime().getTime())/86400000;
        float lastEobrOdometer = EobrReader.getInstance().getLastEobrOdometer();
        
        // if last eobr odometer date is more recent than odometer date we are checking, 
        // set start range to last eobr odometer minus the number of days times a value
        // representing traveling at 85 MPH for 24 hours, and set end range to last
        // eobr odometer
        if (dayDiff < 0)
        {
            if (lastEobrOdometer + (dayDiff * 2040) < 0)
                startRange = 0;
            else
                startRange = lastEobrOdometer + (dayDiff * 2040);

            endRange = lastEobrOdometer;
        }
        // else last eobr odometer is prior to date of odometer we are checking, set start
        // range to the last eobr odometer and the end range to the last odometer plus
        // a value representing traveling at 85 MPH for 24 hours
        else
        {
            startRange = lastEobrOdometer;
            endRange = lastEobrOdometer + (dayDiff * 2040);
        }

        // the minimum the range should be is 1/10 of a mile
        if (endRange - startRange < 2F)
            endRange = startRange + 2F;

        if (odometer >= startRange && odometer <= endRange)
        {
        	isInValidRange = true;
        }

    	retVal.putBoolean(ISINVALIDRANGE, isInValidRange);
    	retVal.putFloat(STARTRANGE, startRange);
    	retVal.putFloat(ENDRANGE, endRange);
        return retVal;
    }
    
    /// <summary>
    /// Search for a valid odometer within the start and end range specified from the 
    /// start time specified.  Search for a maximum of 30 seconds from the specified
    /// start time.  If a motion change is encountered (speed or status), stop searching.
    /// </summary>
    /// <param name="startTime">Start time to search from</param>
    /// <param name="originalSpeedometer">Speedometer value of initial record</param>
    /// <param name="originalStatus">Overall status of initial record</param>
    /// <param name="startOdometerRange">Start range of valid odometer</param>
    /// <param name="endOdometerRange">End range of valid odometer</param>
    /// <returns>StatusRecord</returns>
    public StatusRecord SearchForValidOdometer(Date startTime, float originalSpeedometer, int originalStatus, float startOdometerRange, float endOdometerRange)
    {
        boolean done = false;
        Date currentTimestamp = DateUtility.AddSeconds(startTime, 1);
        Date endTimestamp = DateUtility.AddSeconds(currentTimestamp, 30);
        StatusRecord newStatusRec = null;

        while (!done)
        {
            newStatusRec = new StatusRecord();
            int rc = EobrReader.getInstance().Technician_GetHistoricalData(newStatusRec, currentTimestamp);
            if (rc == EobrReturnCode.S_SUCCESS)
            {
                if (newStatusRec.getOdometerReading() > 0 &&
                    !newStatusRec.IsFailureDetected(DeviceErrorFlags.Odometer))
                {
                    // found a good historical record, with the j-bus enabled and no odometer failure detected
                    // check if the odometer is within range
                    if (newStatusRec.getOdometerReading() >= startOdometerRange && newStatusRec.getOdometerReading() <= endOdometerRange)
                    {
                        return newStatusRec;
                    }
                    // 
                    else if ((originalSpeedometer <= 0 && newStatusRec.getSpeedometerReading() > 0) || (originalSpeedometer > 0 && newStatusRec.getSpeedometerReading() <= 0))
                    {
                        newStatusRec.setOdometerReading(-1);
                        return newStatusRec;
                    }
                    else if (originalStatus != newStatusRec.getOverallStatus())
                    {
                        newStatusRec.setOdometerReading(-1);
                        return newStatusRec;
                    }
                }
                else
                {
                    return null;
                }
            }

            // if we get to current data when reading records in the future,
            // set odometer reading to -1 and return
            if (rc == EobrReturnCode.S_NO_HISTORICAL_DATA)
            {
                newStatusRec.setOdometerReading(-1);
                return newStatusRec;
            }

            // search forward one second at a time looking for a valid odometer
            currentTimestamp = DateUtility.AddSeconds(currentTimestamp, 1);
            if (currentTimestamp.compareTo(endTimestamp) > 0)
                done = true;
        }

        return null;
    }
    
    /// <summary>
    /// Search for a valid odometer within the start and end range specified.  Need to
    /// continue searching until:
    ///     1. A valid odometer is founc
    ///     2. We reach next motion change (speed = 0)
    ///     3. We reach a significant failure
    /// </summary>
    /// <param name="startTime">Start time to search from</param>
    /// <returns>StatusRecord</returns>
    public StatusRecord SearchFirstValidOdometer(Date startTime)
    {
        boolean done = false;
        Date currentTimestamp = DateUtility.AddSeconds(startTime, 5);
        StatusRecord newStatusRec = null;
        boolean invalidOdometer = false;

        while (!done)
        {
            newStatusRec = new StatusRecord();
            int rc = EobrReader.getInstance().Technician_GetHistoricalData(newStatusRec, currentTimestamp);
            if (rc == EobrReturnCode.S_SUCCESS)
            {
                invalidOdometer = false;

                if (newStatusRec.getOdometerReading() > 0)
                {
                    // if valid odometer range is detected, verify odometer is valid
                	Bundle b = this.OdometerInValidRange(newStatusRec.getTimestampUtc(), newStatusRec.getOdometerReading());

                    if (b.getBoolean(ISINVALIDRANGE))
                    {
                        return newStatusRec;
                    }
                    else
                    {
                        invalidOdometer = true;
                    }
                }

                // if a significant failure is detected, return this record
                if (newStatusRec.IsSignificantDeviceFailureDetected())
                {
                    if (invalidOdometer)
                        newStatusRec.setOdometerReading(-1);

                    return newStatusRec;
                }
                
                // if speedometer is now 0, return this record
                if (newStatusRec.getSpeedometerReading() <= 0)
                {
                    if (invalidOdometer)
                        newStatusRec.setOdometerReading(-1);

                    return newStatusRec;
                }
            }

            // if we get to current data when reading records in the future
            // return current data
            if (rc == EobrReturnCode.S_NO_HISTORICAL_DATA)
            {
                rc = EobrReader.getInstance().Technician_GetCurrentData(newStatusRec, true);
                if (rc == EobrReturnCode.S_SUCCESS)
                {
                    // verify current odometer is valid
                	Bundle b = this.OdometerInValidRange(newStatusRec.getTimestampUtc(), newStatusRec.getOdometerReading()); 
                    if (!b.getBoolean(ISINVALIDRANGE))
                        return null;
                    else
                        return newStatusRec;
                }
                else
                    return null;
            }
            // if GetHistoricalData failed with return code other than S_NO_HISTORICAL_DATA,
            // continue trying until current time stamp is in the future
            else if (rc != EobrReturnCode.S_SUCCESS)
            {
                Date now = DateUtility.getCurrentDateTimeUTC();

                if (currentTimestamp.compareTo(now) > 0)
                    return null;
            }

            // search forward five seconds at a time looking for a valid odometer
            currentTimestamp = DateUtility.AddSeconds(currentTimestamp, 5);
        }

        return null;
    }
    
    /// <summary>
    /// Convert the odometer value to the dashboard odometer value.
    /// This uses the EOBR odometer calibration values for OFFSET and MULTIPLIER.
    /// The formula is the following:
    ///    dashboardOdom = (odom * multiplier) + offset
    /// NOTE: this routine has no effect if the calibration values have not been set
    /// </summary>
    /// <param name="odometerValue">odometer value to convert</param>
    /// <returns>dashboard converted value</returns>
    public float ConvertToDashboardOdometer(Context ctx, float odometerValue)
    {
    	Bundle bundle = this.Technician_ReadOdometerCalibrationValues();
    	int rc = bundle.getInt(ctx.getString(R.string.rc));
        
    	if (rc == EobrReturnCode.S_SUCCESS)
    	{
    		float multiplier = bundle.getFloat(ctx.getString(R.string.multiplierparam));
    		if (multiplier > 0.0)
    		{
    			odometerValue = odometerValue * multiplier;
    		}

    		// apply the offset, if there is one
    		float offset = bundle.getFloat(ctx.getString(R.string.offsetparam));
    		if (offset != 0.0)
    		{
    			odometerValue = odometerValue + offset;
    		}
    	}
    	
        return odometerValue;
    }
    
    /// <summary>
    /// Verify that the local system clock has not been changed significantly
    /// since the last time it was checked.
    /// If the local system clock is not sychronized within to the allowed
    /// tolerance, then the local clock will be set to the expected value.
    /// </summary>
    public void VerifyClockConsistency(Context ctx)
    {        
    	//we only want one clock correction to happen at a time
    	synchronized(_clockVerificationLock)
    	{
			// ignoreServerTime
			if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime())
			{
				return;			
			}		
	    		
	        Date clockCheckTimestamp = this.getClockCheckTimestamp();
	        long clockCheckElapsedTime = this.getClockCheckElapsedTime();
	        if(clockCheckTimestamp == null || clockCheckElapsedTime == -1) {
		        this.UpdateClockConsistencyState();
	        	return;
	        }
	
	        if(this.HasReceivedMultipleDeviceClockUpdatesViaAutoTime()){
	        	// We've received multiple broadcast messages that the clock is been updated by the cellular netwrok
	        	// Let's trust that the device clock value has been verified, and synchronized, via the network.
	        	// Furthermore, because of this the current timekeeper (DateUtility) should still be good
	        	// simply update the consisistency state and return so that the next time we verify we can evaluate again
	        	
	        	// Important note: The very first clock change detected when AutoTime is set 
	        	//                 should go through full clock verification
	        	//                 This is because the device clock may be been wrong, 
	        	//                 and the autotime update will sync the device clock to the correct network time
	        	this.UpdateClockConsistencyState();
	        	
	        	if(_clockUpdatedViaAutomaticTimeCount < 3)
	        		Log.d("EobrReader", String.format("EobrReader.VerifyClockConsistency ignoring due to AutoTime update updateViaAutoTimeCount: {%s}", _clockUpdatedViaAutomaticTimeCount));
	        	
	        	return;
	        }
	        
	        long currentElapsedTime = SystemClock.elapsedRealtime();

	        int currentElapsedTimeMilliseconds = (int)(currentElapsedTime - clockCheckElapsedTime);	        
	        if(!_clockRecentUpdateNeedsEvaluation && currentElapsedTimeMilliseconds < CLOCK_VERIFICATION_FREQUENCY_MILLIS)
	        {
	        	// ignore this verification check until we've waited long enough to perform this algorithm,
	        	// unless the user has recently changed the clock value.  In that case we want to run the algorithm right now.
	        	// This will prevent this algorithm from running too frequently, when it doesn't really need to       	
	        	return;
	        }
	        
	        // Determine the elapsed time since the last clock check.
	        // Adjust the clock check by this elapsed time.
	        // Doing this will programmatically determine what the clock should be now.
	        // The adjustedClockCheckTimestamp represents what the current clock should be, if the system is behaving normally
	        Date adjustedClockCheckTimestamp = DateUtility.AddMilliseconds(clockCheckTimestamp, currentElapsedTimeMilliseconds);
	        	        
	        // note: get time 'now' *after* calculating the adjustedClock because these should really be the *same* value if eveything is working properly
	        Date now = DateUtility.getCurrentDateTimeWithSecondsUTC();	        	        
	        
	        // note: when now is chronologically earlier than adjustedClock the timeDifference will be a positive numbers
	        //       the normal case will be that the timeDifference is negative (and a very small number)
	        long timeDifference = adjustedClockCheckTimestamp.getTime() - now.getTime();	
	        
	        Log.d("EobrReader", String.format("EobrReader.VerifyClockConsistency clockCheckTimestamp: {%s} clockCheckElapsedTime {%s} now: {%s} currentElapsedTime: {%s} currentElapsedTimeMilliseconds: {%s} adjustedClockCheckTimestamp: {%s} timeDifference: {%s}",
					clockCheckTimestamp, clockCheckElapsedTime, now, currentElapsedTime, currentElapsedTimeMilliseconds, adjustedClockCheckTimestamp, timeDifference));
	        
	        // add a little buffer room here for error, so only consider the timeDifference if it's more than a minute off
	        if (timeDifference > _clockVerificationConsistencyToleranceMillis)
	        {	        
	            // detected that the device clock has been changed 
	            // to a time in the past.
	            // Reset the system clock to what it was before.
	            DateUtility.SetSystemTime(adjustedClockCheckTimestamp);
	            
	            String msg = String.format("EOBR Clock inconsistency past set clock to newTime: {%s} timeNow: {%s} clockCheckTimestamp: {%s} clockCheckElapsedTime {%s} currentElapsedTime: {%s} currentElapsedMillis: {%s}", 
	            		DateUtility.getHomeTerminalDateTimeFormat24Hour().format(adjustedClockCheckTimestamp), 
	            		DateUtility.getHomeTerminalDateTimeFormat24Hour().format(now), 
	            		clockCheckTimestamp, 
	            		clockCheckElapsedTime, currentElapsedTime, currentElapsedTimeMilliseconds);
	            Log.d("EobrReader", msg);
	            ErrorLogHelper.RecordMessage(ctx, msg);
	        		           
	        }
	        else
	        {
	            // has the clock moved to the future by any unexpected amount	        	
	            Date futureTime = DateUtility.AddMilliseconds(adjustedClockCheckTimestamp ,_clockVerificationConsistencyToleranceMillis);
	            if (now.compareTo(futureTime) > 0)
	            {
	                // detected that the device clock has been changed
	                // time now is in the future by an unacceptable amount.
	                // Reset the system clock to what it was before.
	                DateUtility.SetSystemTime(adjustedClockCheckTimestamp);
	
		            String msg = String.format("EOBR Clock inconsistency future set clock to newTime: {%s} timeNow: {%s} clockCheckTimestamp: {%s} clockCheckElapsedTime {%s} currentElapsedTime: {%s} currentElapsedMillis: {%s}", 
		            		DateUtility.getHomeTerminalDateTimeFormat24Hour().format(adjustedClockCheckTimestamp), 
		            		DateUtility.getHomeTerminalDateTimeFormat24Hour().format(now), 
		            		clockCheckTimestamp, 
		            		clockCheckElapsedTime, currentElapsedTime, currentElapsedTimeMilliseconds);	                
	                Log.d(this.getClass().getSimpleName(), msg);
	                ErrorLogHelper.RecordMessage(ctx, msg);
	            }
	        }
	        
	        // save the current clock check values for the the next time we verify
	        this.UpdateClockConsistencyState();

    	}
    }
    
    /**
     * Handle a detected change to the device clock time.
     * This is reported by a BroadcastReceiver handling Intent.ACTION_TIME_CHANGED.
     */
    public void ReportDeviceClockTimeChange(Context ctx){
    		
    	if(DateUtility.IsAutoDateTimePreferenceSet(ctx.getContentResolver())){
    		// if the auto-time setting is turned on, this means that the network may have set the clock
    		// keep track of how many times we see autotime update the clock
    		
    		// it's important to note that this does not mean the time of the clock has actually been changed to anything different
    		// It been observed that every time there is a time broadcast from the network, there is a report of a device time change.
    		// It seems like the device broadcasts this event every time it receives a network time message, even though the clock is good
    	
    		if(_clockUpdatedViaAutomaticTimeCount <= 0){
    			// in this case, this the first autoTime update.  
    			// We can't assume that the clock time is correct, so the Verify Clock sync needs to process this update
    			_clockUpdatedViaAutomaticTimeCount = 1;
    			_clockRecentUpdateNeedsEvaluation = true;
    			
    			ErrorLogHelper.RecordMessage(ctx, "Device Clock Time change detected, AutoTime is set -- first update.");
    		}
    		else{
    			// in this case, we've been receiving continual updates while AutoTime is set
    			_clockUpdatedViaAutomaticTimeCount++;   
    		}
    	}
    	else{
    		// if a clock change is detected, and autoTime is not set, then reset this value back to zero
    		// this means that the clock change was done manually by the user
    		_clockUpdatedViaAutomaticTimeCount = 0;	
    		_clockRecentUpdateNeedsEvaluation = true;
    		
    		ErrorLogHelper.RecordMessage(ctx, "Device Clock Time change detected, AutoTime is not set.");
    	}
    }

    /**
     * Answer if more than one device clock change has been reported while in AutoTime mode
     * @return
     */
    private boolean HasReceivedMultipleDeviceClockUpdatesViaAutoTime(){
    	return _clockUpdatedViaAutomaticTimeCount > 1;    	
    }
    
    /**
     * Update the state data saved in support of the clock consistency verification process
     */
    private void UpdateClockConsistencyState(){
		this.setClockCheckTimestamp(DateUtility.getCurrentDateTimeWithSecondsUTC());
		this.setClockCheckElapsedTime(SystemClock.elapsedRealtime());
		_clockRecentUpdateNeedsEvaluation = false;
    }

    /// <summary>
    /// This method should ONLY be called when using the Testharness application.  We have this specific method for the Testharness because
    /// several things do not need to execute for the Testharness to run.  Namely, the Testharness does not need to perform anything 
    /// related to current logs, reading historical records, setting odometer, etc.  Therefore, we have created this method specifically
    /// for the Testharness.
    ///
    /// Synchronize both the EOBR clock and the local system clock.
    /// Generally this involves setting the local system clock to the
    /// EOBR time.  
    /// </summary>
    private boolean TestHarness_PerformClockSynchronization(Context ctx) {
    	boolean isInFailure = false;
    	Date eobrTimestampUtc = this.Technician_ReadClockUniversalTime(ctx);
		Date eobrGpsTimestampUtc = GlobalState.getInstance().getForceInvalidGPSDate() ? SetReturnedGPSDateToInvalidDate() : this.Technician_ReadGPSTime();
    	
    	if (eobrTimestampUtc != null) {
            // perform the synchronization with the failure controller
            FailureController ctrlr = new FailureController(ctx);
            Bundle clockSyncBundle = ctrlr.DetermineIfEOBRClockSyncIsNecessary(eobrTimestampUtc, eobrGpsTimestampUtc);
            
            isInFailure = clockSyncBundle.getBoolean("isInFailure");
            boolean requiresSynchronization = clockSyncBundle.getBoolean("requiresSynchronization");
            long clockDifference = clockSyncBundle.getLong("clockDifference");
            
            if(isInFailure){
            	// if clock sync has reported the eld in failure, then release the connection to the eobr
            	ErrorLogHelper.RecordMessage(ctx, "Detected EOBR in clock sync failure, forcing release the connection to the eobr");
            	SystemStartupController startupCtlr = new SystemStartupController(ctx);
            	try {
					startupCtlr.ShutdownEobrDevice();
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordException(ctx, e);
				}
            	return isInFailure;
            }
            
            if (requiresSynchronization && clockDifference != 0) {
            	// the failure controller determined that the clock needs to be set
            	// determine what the clock should be set to by reading the current time, and then adding the difference reported by the failureController
            	eobrTimestampUtc = this.Technician_ReadClockUniversalTime(ctx);
            	Date newClockTimeUtc = DateUtility.AddMilliseconds(eobrTimestampUtc, (int)clockDifference);
            	
            	if (eobrTimestampUtc.compareTo(newClockTimeUtc) < 0) {
            		// NOTE: for now only set the clock when it will change the time to the future
            		String msg = String.format("EobrReader.PerformClockSynchronization set eobr time from: {%s} to: {%s}", eobrTimestampUtc, newClockTimeUtc);
                    Log.d("TimeSync", msg);
                    ErrorLogHelper.RecordMessage(ctx, msg);
                    
            		this.Technician_SetClockUniversalTime(ctx, newClockTimeUtc);
            	}            	
            } else {
            	// the clocks either don't need to be synchronized, or the failure controller was not able to connect to DMO
            	// check to see if the clock has been set yet
            	if (DateUtility.HasClockBeenSet() == false) {
            		// the system clock has not been set yet
            		
            		// 1. make sure to set the current time to the EOBR timestamp
            		DateUtility.SetSystemTime(eobrTimestampUtc);            		
            	}
            }

        }    	
    	
    	return isInFailure;
    }

	/// <summary>
    /// Synchronize both the EOBR clock and the local system clock.
    /// Generally this involves setting the local system clock to the
    /// EOBR time.  
    /// </summary>
    public boolean PerformClockSynchronization(Context ctx)
    {
    	boolean isInFailure = false;
    	Date eobrTimestampUtc = this.Technician_ReadClockUniversalTime(ctx);
		Date eobrGpsTimestampUtc = GlobalState.getInstance().getForceInvalidGPSDate() ? SetReturnedGPSDateToInvalidDate() : this.Technician_ReadGPSTime();

		if (eobrTimestampUtc != null)
    	{
            // perform the synchronization with the failure controller
            FailureController ctrlr = new FailureController(ctx);
            Bundle clockSyncBundle = ctrlr.DetermineIfEOBRClockSyncIsNecessary(eobrTimestampUtc, eobrGpsTimestampUtc);
            
            isInFailure = clockSyncBundle.getBoolean("isInFailure");
            boolean requiresSynchronization = clockSyncBundle.getBoolean("requiresSynchronization");
            long clockDifference = clockSyncBundle.getLong("clockDifference");

			if (isInFailure && !requiresSynchronization) {	//We are unable to get DMO and GPS
				// if clock sync has reported the eld in failure, then release the connection to the eobr
				ErrorLogHelper.RecordMessage(ctx, "Detected EOBR in clock sync failure, forcing release the connection to the eobr");
				SystemStartupController startupCtlr = new SystemStartupController(ctx);
				try {
					startupCtlr.ShutdownEobrDevice();
				} catch (KmbApplicationException e) {
					// ignore any exceptions here
				}
				return true;
			} else if (requiresSynchronization){
				// the failure controller determined that the clock needs to be set
            	// determine what the clock should be set to by reading the current time, and then adding the difference reported by the failureController
            	Date newClockTimeUtc = DateUtility.addMilliseconds(eobrTimestampUtc, clockDifference);

            	// tch - 10/10/2013 - set clock in either direction, forward or back
           		String msg = String.format("EobrReader.PerformClockSynchronization set eobr time from: {%s} to: {%s}", eobrTimestampUtc, newClockTimeUtc);
                Log.d("TimeSync", msg);
                ErrorLogHelper.RecordMessage(ctx, msg);

          		this.Technician_SetClockUniversalTime(ctx, newClockTimeUtc);
				isInFailure = false;
            }

			// the clocks either don't need to be synchronized, or the failure controller was not able to connect to DMO
			// check to see if the clock has been set yet
			if(!DateUtility.HasClockBeenSet()){
				// the system clock has not been set yet

				// 1. make sure to set the current time to the EOBR timestamp
				DateUtility.SetSystemTime(eobrTimestampUtc);

				// 2. check to see if the mobileStartTimestamp of the current log needs to be adjusted
				//    this is important because if a driving event gets created but is before the mobile log start it might not be submitted to DMO
				IAPIController logController = MandateObjectFactory.getInstance(ctx, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

				User user = GlobalState.getInstance().getCurrentUser();
				Date logDate = EmployeeLogUtilities.CalculateLogStartTime(ctx, eobrTimestampUtc, user.getHomeTerminalTimeZone());

				//only retrieve a local log - don't create one if one doesn't exist.
				EmployeeLog empLog = logController.GetLocalEmployeeLog(user, logDate);

				if(empLog != null) {
					LogEntryController logEntryController = new LogEntryController(ctx);
					Date currentClockHomeTerminalTime = logEntryController.getCurrentClockHomeTerminalTime();
					if (currentClockHomeTerminalTime.getTime() < empLog.getMobileStartTimestamp().getTime()) {
						// the current clock is earlier than the mobile log start time so adjust it so that
						empLog.setMobileStartTimestamp(currentClockHomeTerminalTime);
					}

					// 3. Delete any log events on the log that might be lingering in the future
					//    This can happen if the user sign into KMB without a DMO connection at the login time and creates any log events (such as the OnDuty from the login)
					EmployeeLogUtilities.RemoveAllEventsAfter(empLog, currentClockHomeTerminalTime);
				}
			}
            	
        }
    	
    	return isInFailure;
    }
    
    @Override
	public String EobrMacAddress()
    {
    	String retVal = "";
    	
    	IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
    	
    	if (eobrEngine != null)
    	{
    		EobrDeviceDescriptor[] devices = eobrEngine.getDiscoveredDeviceList();
    		if (devices != null && devices.length == 1)
    		{
    			retVal = devices[0].getAddress();
    		}
    	}
    	
    	return retVal;
    }

    /// <summary>
    /// Publish the new EOBR info to the registered delegates.   
    /// This method should indicate to the UI layer that there is some 
    /// type of status change to be notified about it.
    /// <param name="connectionState">Current connection state</param>
    /// </summary>
	public void PublishEobrStatusChange(ConnectionState connectionState)
    {
        this.PublishEobrStatusChange(0, (StatusRecord)null, connectionState);
    }

    public void PublishEobrStatusChange(int returnCode, StatusRecord currentStatus)
    {
        this.PublishEobrStatusChange(returnCode, currentStatus, ConnectionState.OFFLINE);
    }

    /// <summary>
    /// Publish the EOBR status to the registered delegates
    /// </summary>
    /// <param name="returnCode"></param>
    /// <param name="currentStatus">current status record to send out</param>
    /// <param name="connectionState">current connection state</param>
    public void PublishEobrStatusChange(int returnCode, StatusRecord currentStatus, ConnectionState connectionState)
    {
        // is there any registered event handlers?
        if (_eobrReaderChangeEventHandler != null && this.getOnStatusChangeEnabled())
        {
            // create the event args
            EobrEventArgs eventArgs = new EobrEventArgs( currentStatus );

            eventArgs.setReturnCode(returnCode);
            eventArgs.setConnectionState(connectionState);

            // Invokes the delegates. 
            _eobrReaderChangeEventHandler.onEventChange(eventArgs);
        }
    }

    public void PublishEobrStatusChange(int returnCode, EventRecord currentEvent, boolean publishDutyStatus)
    {
    	this.PublishEobrStatusChange(returnCode, currentEvent, ConnectionState.OFFLINE, publishDutyStatus);
    }
    
    /* Gen2 versions of EobrStatusChange delegates */
    public void PublishEobrStatusChange(int returnCode, EventRecord currentEvent)
    {
        this.PublishEobrStatusChange(returnCode, currentEvent, ConnectionState.OFFLINE, true);
    }
    
    /// <summary>
    /// Publish the EOBR status to the registered delegates
    /// </summary>
    /// <param name="returnCode"></param>
    /// <param name="currentStatus">current status record to send out</param>
    /// <param name="connectionState">current connection state</param>
    private void PublishEobrStatusChange(int returnCode, EventRecord currentStatus, ConnectionState connectionState, boolean publishDutyStatusChange)
    {
        // is there any registered event handlers?
        if (_eobrReaderChangeEventHandler != null && this.getOnStatusChangeEnabled())
        {
            // create the event args
            EobrEventArgs eventArgs = new EobrEventArgs( currentStatus );

            eventArgs.setReturnCode(returnCode);
            eventArgs.setConnectionState(connectionState);
            eventArgs.setPublishDutyStatusChange(publishDutyStatusChange);
            
            // Invokes the delegates. 
            _eobrReaderChangeEventHandler.onEventChange(eventArgs);
        }
    }
    
    /// <summary>
    /// Publish the EOBR history to the registered delegates.
    /// </summary>
    /// <param name="historyList"></param>
    @Override
	public void PublishEobrHistoricalRecords(List<StatusRecord> historyList)
    {
        // is there any registered event handlers?
        if (_eobrHistoryChangeEventHandler != null)
        {
            // create the event args
            EobrHistoryEventArgs eventArgs = new EobrHistoryEventArgs(historyList);
            eventArgs.setHasFailureOccurred(this.getCurrentConnectionState() == ConnectionState.DEVICEFAILURE);

            // Invokes the delegates. 
            _eobrHistoryChangeEventHandler.onEventChange(eventArgs);
        }
    }
    
    /// <summary>
    /// Publish the Gen II EOBR history to the registered delegates.
    /// </summary>
    /// <param name="historyList"></param>
    public void PublishEobrHistoricalRecordsGenII(ArrayList<EventRecord> eventRecords)
    {
        // Is there any registered event handlers?
        if (_eobrHistoryChangeEventHandlerGenII != null)
        {
            // Create the event args.
        	EobrGenIIHistoryEventArgs eventArgs = new EobrGenIIHistoryEventArgs(eventRecords);        	
            eventArgs.setHasFailureOccurred(this.getCurrentConnectionState() == ConnectionState.DEVICEFAILURE);

            // Invokes the delegates. 
            _eobrHistoryChangeEventHandlerGenII.onEventChange(eventArgs);
        }
    }
    
    public void PublishUnassignedDrivingPeriods(List<UnassignedDrivingPeriod> periods, UnassignedDrivingPeriodResult result)
    {
    	if(_unassignedDrivingPeriodHandler != null)
    	{
    		UnassignedDrivingPeriodEventArgs args = new UnassignedDrivingPeriodEventArgs(periods, result);
    		_unassignedDrivingPeriodHandler.onEventChange(args);
    	}
    }
    
    public void PublishTripReport(TripReport tripReport, float speedThreshold, int rpmThreshold)
    {
    	if (_eobrTripReportHandler != null)
    	{
    		EobrTripReportEventArgs args = new EobrTripReportEventArgs(tripReport, speedThreshold, rpmThreshold);
    		_eobrTripReportHandler.onTripReport(args);
    	}
    }

    /**
     * Publishes an event to verify ending of a Driving Period
     */
	public void PublishVerifyDrivingEnd()
    {
        if (_eobrReaderChangeEventHandler != null)
        {
            _eobrReaderChangeEventHandler.onVerifyDriveEnd();
        }
    }

	public void PublishVerifySpecialDrivingEnd(final EmployeeLogProvisionTypeEnum drivingCategory, EventRecord eventRecord, Location location, EmployeeLog empLog) {
		if (_eobrReaderChangeEventHandler != null)
		{
			_eobrReaderChangeEventHandler.onVerifySpecialDrivingEnd(drivingCategory, eventRecord, location, empLog);
		}
	}

	public void PublishDismissSpecialDrivingDialog(EmployeeLogProvisionTypeEnum drivingCategory) {
		if (_eobrReaderChangeEventHandler != null)
		{
			_eobrReaderChangeEventHandler.onDismissSpecialDrivingDialog(drivingCategory);
		}
	}

	public void PublishDismissDrivingView() {
		if (_eobrReaderChangeEventHandler != null)
		{
			_eobrReaderChangeEventHandler.onDismissDrivingView();
		}
	}

	public void PublishVerifySpecialDrivingEnd(ConnectionState previousState, ConnectionState newState) {
		PublishVerifySpecialDrivingEnd(EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE, previousState, newState);
		PublishVerifySpecialDrivingEnd(EmployeeLogProvisionTypeEnum.HYRAIL, previousState, newState);
		PublishVerifySpecialDrivingEnd(EmployeeLogProvisionTypeEnum.NONREGULATED, previousState, newState);
	}

	/**
	 * Will publish an event to verify ending non-regulated driving if the EOBR
	 * state has changed and the previous state wasn't also an offline state.
	 *
	 * @param previousState The previous connection state of the ELD
	 * @param newState The new connection state of the ELD
	 */
	public void PublishVerifySpecialDrivingEnd(final EmployeeLogProvisionTypeEnum drivingCategory, ConnectionState previousState, ConnectionState newState)
	{
		if (_eobrReaderChangeEventHandler != null)
		{
			if (previousState != newState)
			{
				boolean previousStateWasOffline = false;
				switch (previousState)
				{
					case OFFLINE:
					case DEVICEFAILURE:
					case SHUTDOWN:
						previousStateWasOffline = true;
						break;
					default:
						break;
				}

				if (!previousStateWasOffline)
				{
					switch (newState)
					{
						case OFFLINE:
						case DEVICEFAILURE:
						case SHUTDOWN:

							ISpecialDrivingController controller = SpecialDrivingFactory.getControllerForDrivingCategory(drivingCategory);
							controller.VerifySpecialDrivingEnd();

							break;
						default:
							break;
					}
				}
			}
		}
	}
    
	public void SuspendReading() {
		if(TIMER_POP_DEBUG) {
			Log.i("EobrReader", String.format("SuspendReading\n%s", Log.getStackTraceString(new Exception("DEBUG"))));
		}
		try {
			if(_eobrServiceMessenger != null)
				_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_SUSPEND_READING));
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}
	
	public void ReadHistoryOnTimerPop(){
		try {
			if(_eobrServiceMessenger != null)
				_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_READHISTORYONTIMERPOP));
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}
	
	@Override
	public void ResumeReading(){
		if(TIMER_POP_DEBUG) {
			Log.i("EobrReader", String.format("ResumeReading\n%s", Log.getStackTraceString(new Exception("DEBUG"))));
		}
		try {
			if(_eobrServiceMessenger != null)
				_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_RESUME_READING));
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}
	
	/**
     * Suspends periodic reading and forces an immediate read from the EOBR.
     * This will only be done for Gen II EOBR's.
     * If a lock is passed in, notifyAll() will be called when reading is complete.
	 * @param lock An optional lock to notify when reading is complete.
	 */
	public void ForceImmediateRead(Object lock) {
		try {
			if(_eobrServiceMessenger != null)
			{
				_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_FORCE_IMMEDIATE_READ, lock));
			}
		} catch (RemoteException e) {
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		} 
	}
	
	public void Shutdown(){
		try {
			if(_eobrServiceMessenger != null)
				_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_SHUTDOWN));
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}
	
	public void IgnoreResume(boolean ignoreResume){
		try {
			if(_eobrServiceMessenger != null)
			{
				if (ignoreResume)
					_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_IGNORERESUMEON));
				else
					_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_IGNORERESUMEOFF));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}	
	    
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
    @Override
	public boolean WaitForEobrDiscovery(Context ctx, String eobrMacAddress, int durationMinutes)
    {
    	boolean isSuccessful = false;
        Calendar endingTimestamp = Calendar.getInstance();
        endingTimestamp.add(Calendar.MINUTE, durationMinutes);
        long endingTime = endingTimestamp.getTime().getTime();
        
        boolean done = false;
        while (!done)
        {
            // try to discover the device on the bus
        	Bundle bundle = null;
	    	try {
        		Log.d("EobrReader", String.format("Discovery attempt for macaddress: {%s}", eobrMacAddress));
	    		bundle = this.PerformDeviceDiscoveryAndActivationFromMACAddressForFirmwareUpdate(ctx, eobrMacAddress, false, null);
			} catch (KmbApplicationException e1) {
				e1.printStackTrace();
				Log.e("UnhandledCatch", e1.getMessage() + ": " + Log.getStackTraceString(e1));
			}
            if (bundle != null && bundle.getInt(ctx.getString(R.string.rc)) == 0)
            {
                // found it, so initialize it					
                done = true;
                isSuccessful = true;
            }
            else
            {
                // see how long this has been taking so far
            	long now = TimeKeeper.getInstance().now().getTime();
                if (now >= endingTime)
                {
                    // we've waited long enough, abort the discovery
                    done = true;
                    isSuccessful = false;
                }

                // wait a little bit before trying again
                try {
                	Log.d("EobrReader", String.format("Waiting during discovery of macaddress: {%s}", eobrMacAddress));
					Thread.sleep(30000); // 30 seconds
				} catch (InterruptedException e) {
					// skip any exception and keep going
					Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}  
            }
        }
        return isSuccessful;
    }
    
    public void ReadAndPublishHistoricalStatusRecords(boolean isGen1Eobr, Context ctx){
		try {
			if(_eobrServiceMessenger != null)
			{
	    		final boolean isJJK = this.Technician_GetEobrHardware(ctx);
				if (isGen1Eobr)
					_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_READANDPUBLISHHISTORICAL));
				else if(isJJK)
					_eobrServiceMessenger.send(Message.obtain(null, EobrServiceMessages.MSG_READANDPUBLISHHISTORICAL_GEN2));
				else
					// note: on a BTE need to suspend here because this is called after Activation.
					//       The history reading for BTE will be done after checking for application updates.
		    		this.SuspendReading();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}	
    }
    
    public void UnpairFromDevice(String macAddress)
    {
    	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	if (btAdapter.isEnabled())
    	{
    		BluetoothDevice btDevice = btAdapter.getRemoteDevice(macAddress);

    		try {
    			Method m = btDevice.getClass().getMethod("removeBond", (Class[]) null);
    			m.invoke(btDevice, (Object[]) null);
    		} catch (Exception e) {
    			Log.e("Comm", e.getMessage());
    		}

    		// After unpairing the device, need to wait before attempting to connect
    		// to the socket again - otherwise get a Device or resource is busy when
    		// attempt to connect to the socket.  The wait period seems to be different 
    		// on different devices.  5 seconds is fine on Motorola Droid Razr, however 
    		// 5 seconds results in Device or resource is busy on a Motorola MB860. 
    		// Therefore, sleep for 15 seconds.
    		try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
			}
   		}
    }

    /**
     * Perform a power-cycle reset of the ELD.
     * It's important to note that when this is done, the BT connection to the ELD will be lost.
     * This means that any subsequent messages sent to the ELD may not be successful, until the connection is restored.
     * 
     */
    public void PerformPowerCycleReset(Context ctx){		
		IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
		Bundle answer = new Bundle();
		answer.putInt(RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED);
		
		if (eobrEngine != null) {
			// NOTE: This command will cause the ELD to be power-cycled, and as such the BT connection will be lost
			//       during the power cycle.
			//       There is no reason to retry this command since the response will always be DEV_NOT_CONNECTED
			answer = eobrEngine.SendConsoleCommandToDeviceWithNoRetry("reset");
		}
				
		ErrorLogHelper.RecordMessage(ctx, "EobrReader.PerformPowerCycleReset - Sent command to power-cycle reset the ELD.");    	
    }
    
    public boolean IsGetDrivingPeriodsSupported()
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	
    	if(eobrEngine != null)
    		return eobrEngine.IsGetDriveDataSupported();
    	else
    		return false;
    }

	public boolean IsGetEventDataEventMaskSupported() {
		boolean result = false;

		IEobrEngine eobrEngine = getEobrEngine();
		if (eobrEngine != null) {
			result = eobrEngine.IsGetEventDataEventMaskSupported();
		}

		return result;
	}
    
    public EobrResponse<DriveData> GetDrivingPeriod(long timecode, short routePositionInterval, short maxGpsUncertainty)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	DriveData driveData = new DriveData();
    	List<VehicleLocation> locations = new ArrayList<VehicleLocation>();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	Log.d("EobrReader", String.format("Loading driving periods from %d", timecode));
    	
    	if(eobrEngine != null)
    	{
    		//the first call needs to specify a type of DrivePeriod - this will cause the first 2 VehicleLocations to be a DRIVE_ON and a DRIVE_OFF,
    		//respectively, with the next 4 elements being standard route positions
    		EobrResponse<DriveData> driveResult = eobrEngine.GetDriveData(DriveDataTypeEnum.DRIVEPERIOD, timecode, routePositionInterval, maxGpsUncertainty);
    		returnCode = driveResult.getReturnCode();
    		
    		if(driveResult.getReturnCode() == EobrReturnCode.S_SUCCESS && driveResult.getData().getVehicleLocations().size() > 0)
    		{
    			driveData = driveResult.getData();
    			boolean haveExceededDriveOff = false;
    			
    			for(VehicleLocation location : driveData.getVehicleLocations())
    			{
    				//route positions that occur after the drive stop should be ignored (or if there's no drive stop... the timecode will be 0, so everything will be greater)
    				if(location.getEventType() == EventTypeEnum.ANYTYPE)
    				{
    					if(location.getGpsFix().getTimeCode() > driveData.getDriveOffTimecode())
    					{
    						haveExceededDriveOff = true;
    						break;
    					}
    				}
    				
    				locations.add(location);
    			}
    			
    			//the packets are large enough to return several route positions
    			//if this packet is full and we haven't exceeded the drive stop time yet
    			//then look for more.  If it's not full, then this is all we get.
    			if(!haveExceededDriveOff && locations.size() == DriveData.MaxVehicleLocationCount())
    			{
    				//the DriveData that we got from the TAB contains the timecode for the next batch of route positions to request
    				returnCode = LoadRoutePositions(locations, driveData.getNextTimecode(), driveData.getDriveOffTimecode(), routePositionInterval, maxGpsUncertainty);
    			}
    		}
    	}
    	
    	if(driveData != null)
    		driveData.setVehicleLocations(locations);
    	
    	EobrResponse<DriveData> response = new EobrResponse<DriveData>(returnCode);
    	response.setData(driveData);
    	
    	return response;
    }
    
    public int SetReferenceTimestamps(EobrReferenceTimestamps timestamps)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if(eobrEngine != null) {
			returnCode = eobrEngine.SetReferenceTimestamps(timestamps);

			if (returnCode == EobrReturnCode.S_SUCCESS) {
				try {
					// Update the reference timestamp stored on the EOBRDevice record
					saveLastReferenceTimestampToDB(timestamps.getEventReferenceTime());
				} catch (Exception e) {
					// do nothing -- problem's shouldn't stop normal code execution -- just use refTimestamp
				}
			}
		}

    	return returnCode;
    }
    
    public int GetLastEventOfType(EventRecord eventRecord, EventTypeEnum eventType)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if(eobrEngine != null)
    		returnCode = eobrEngine.GetEventData(eventRecord, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), 0xFFFFFFFF, 0, eventType, false);
    	
    	Technician_CheckBTReturnStatus(returnCode);
    	
    	return returnCode;
    }
	
	public EobrResponse<EventRecord> GetNextEvent(long refTimestamp, EventTypeEnum... eventTypes) {
		IEobrEngine eobrEngine = getEobrEngine();
		int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
		EventRecord eventRecord = new EventRecord();

		if(eobrEngine != null) {
			int eventMask = new EventMaskBuilder().withEventTypes(eventTypes).build();

			returnCode = eobrEngine.GetEventData(eventRecord, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP), 0, refTimestamp, new EventTypeEnum(EventTypeEnum.ANYTYPE), false, eventMask);
		}

		Technician_CheckBTReturnStatus(returnCode);

		return new EobrResponse<>(returnCode, eventRecord);
	}

    public int SetIsEldMandate(boolean isEldMandate)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if(eobrEngine != null)
    		returnCode = eobrEngine.SetIsEldMandate(isEldMandate);
    	
    	return returnCode;
    }
    
    public int SetDisableReadEldVin(boolean isEldReadingVin)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if(eobrEngine != null)
    		returnCode = eobrEngine.SetDisableReadEldVin(isEldReadingVin);
    	
    	return returnCode;
    }
    
    public boolean GetDisableReadEldVin()
    {
    	Bundle bundle = null;
    	boolean isReadingVin = true;
    	int returnCode = 0;
		
    	IEobrEngine eobrEngine = getEobrEngine();
    	
    	if(eobrEngine != null)
    	{
    		bundle = eobrEngine.GetDisableReadEldVin();
    		
    		if (bundle != null)
    		{
    			returnCode = bundle.getInt(RETURNCODE);
        		
        		if (returnCode == EobrReturnCode.S_SUCCESS)
        			isReadingVin = bundle.getBoolean(RETURNVALUE);
    		}
    		
    	}   	
    	
    	return isReadingVin;
    }
    
    private int LoadRoutePositions(List<VehicleLocation> locations, long fromTimecode, long toTimecode, short routePositionInterval, short maxGpsUncertainty)
    {
    	IEobrEngine eobrEngine = getEobrEngine();
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	if(eobrEngine != null)
    	{
    		//specify a type of TimeSteps to only go after route positions - not driving periods
    		EobrResponse<DriveData> response = eobrEngine.GetDriveData(DriveDataTypeEnum.TIMESTEPS, fromTimecode, routePositionInterval, maxGpsUncertainty);
    		returnCode = response.getReturnCode();
    		
    		if(response.getReturnCode() == EobrReturnCode.S_SUCCESS && response.getData().getVehicleLocations().size() > 0)
    		{
    			DriveData driveData = response.getData();
    			boolean readMore = true;
    			
    			for(VehicleLocation location : driveData.getVehicleLocations())
    			{
    				//route positions that occur after the toTimecode
    				if(location.getGpsFix().getTimeCode() > toTimecode)
    				{
    					readMore = false;
    					break;
    				}
    				
    				locations.add(location);
    			}
    			
    			if(readMore)
    				returnCode = LoadRoutePositions(locations, driveData.getNextTimecode(), toTimecode, routePositionInterval, maxGpsUncertainty);
    		}
    		else if(response.getReturnCode() != EobrReturnCode.S_SUCCESS)
    		{
    			ErrorLogHelper.RecordMessage(String.format("LoadRoutePositions - failed to read positions at time '%d' returnCode '%d'", fromTimecode, response.getReturnCode()));
    		}
    	}
    	
    	return returnCode;
    }

    /**
     * Get the Encompass License Plate Number for the Unit currently associated to the EOBR.
     */
    public String getUnitLicensePlateNumber() { return _unitLicensePlateNumber; };
    public void setUnitLicensePlateNumber(String licensePlateNumber) {
        if (licensePlateNumber != null)
            _unitLicensePlateNumber = licensePlateNumber.trim();
        else
            _unitLicensePlateNumber = null;
    }


	/**
	 * Gets the current status buffer.
	 * If there is an error, the response data will be null.
	 * @return An {@link EobrResponse} with the response
	 */
	public EobrResponse<StatusBuffer> GetStatusBuffer() {
		IEobrEngine eobrEngine = getEobrEngine();
		if (eobrEngine != null) {
			return eobrEngine.GetStatusBuffer();
		}
		return new EobrResponse<>(EobrReturnCode.S_DEV_NOT_CONNECTED);
	}

	/**
	 * Determine if the EOBR event reference timestamp is before the expected timestamp
	 */
	private boolean isReferenceTimestampCurrent(long currentReferenceTimestamp) {
		try {
			return currentReferenceTimestamp >= getLastReferenceTimestampFromDB();
		} catch (Exception e) {
			// do nothing -- problem's shouldn't stop normal code execution -- just use refTimestamp
		}

		return true;
	}

	/**
	 * Get the last ReferencedTimestamp saved in the local database
	 */
	private long getLastReferenceTimestampFromDB() {
		String eobrSerialNumber = getEobrSerialNumber();

		if (!TextUtils.isEmpty(eobrSerialNumber)) {
			EobrConfigurationFacade eobrConfigurationFacade = FacadeFactory.GetInstance().getEobrConfigurationFacade(getContext());
			if (eobrConfigurationFacade != null) {
				EobrConfiguration eobrConfig = eobrConfigurationFacade.Fetch(eobrSerialNumber);
				if (eobrConfig != null) {
					Date lastEventReferenceTimestamp = eobrConfig.getLastEventReferenceTimestamp();
					if (lastEventReferenceTimestamp != null)
						return lastEventReferenceTimestamp.getTime();
				}
			}
		}

		return 0;
	}

	/**
	 * Update the ref timestamp stored on the EOBRDevice record
	 */
	private void saveLastReferenceTimestampToDB(long eldRefTimestamp) {
		String eobrSerialNumber = getEobrSerialNumber();

		if (!TextUtils.isEmpty(eobrSerialNumber) && eldRefTimestamp != 0) {

			EobrConfigurationFacade eobrConfigurationFacade = FacadeFactory.GetInstance().getEobrConfigurationFacade(getContext());
			if (eobrConfigurationFacade != null) {
				EobrConfiguration eobrConfig = eobrConfigurationFacade.Fetch(eobrSerialNumber);
				if (eobrConfig != null) {
					eobrConfig.setLastEventReferenceTimestamp(new Date(eldRefTimestamp));

					eobrConfigurationFacade.Save(eobrSerialNumber, eobrConfig);
				}
			}
		}
	}

	private void PrepareForDataRecordingMalfunctionCheck(int queryRecordId, int resultRecordId){
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			Integer statusBufferNumberOfTrips = null;

			// If the lookup failed for a valid queryRecordId, then we need to go get the number of trips from the status buffer to help determine if a malfunction should be created or not
			if (queryRecordId != 0 && resultRecordId <= 0) {
				StatusBuffer sb = this.GetStatusBuffer().getData();
				statusBufferNumberOfTrips = sb.getNumberOfTrips();
			}
			MalfunctionManager.getInstance().checkDataRecordingMalfunction(queryRecordId, resultRecordId, statusBufferNumberOfTrips);
		}
	}

	private Date SetReturnedGPSDateToInvalidDate(){
		Date invalidGPSDate = DateUtility.getDateTimeFromString("12/30/201523:59:00", new SimpleDateFormat("MM/dd/yyyyHH:mm:ss"), TimeZone.getTimeZone("UTC"));
		return invalidGPSDate;
	}
}
