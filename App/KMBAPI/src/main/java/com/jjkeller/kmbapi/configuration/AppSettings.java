package com.jjkeller.kmbapi.configuration;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Scanner;

public class AppSettings {

	public static Context context;

	public void loadAppSettings(Context ctx, int resId)
	{
		String tagName = "";
		String text = "";

		InputStream inputStream = ctx == null ? context.getResources().openRawResource(resId) : ctx.getResources().openRawResource(resId);

		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(inputStream, null);

			int eventType = parser.getEventType();  
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				if (eventType == XmlPullParser.TEXT)
				{
					text = parser.getText(); //Pulling out node text
				}
				else if (eventType == XmlPullParser.END_TAG)
				{
					tagName = parser.getName();
					parseKeyValue(tagName, text);
					text = ""; //Reset text for the next node
				}
				eventType = parser.next();
			}

			_appSettingsLoaded = true;
		}
		catch (Throwable e)
		{
			ErrorLogHelper.RecordException(ctx, e);
			e.printStackTrace();
			_appSettingsLoaded = false;
		}
	}
	
	public void loadFirmwareUpdates(Context ctx, int resId)
	{
		Gson gson = new Gson();
		
		InputStream settings = ctx.getResources().openRawResource(resId);
		Scanner s = new Scanner(settings).useDelimiter("\\A");
		String json = s.hasNext() ? s.next() : null;
		
		if(json != null)
			_firmwareUpdates = gson.fromJson(json, FirmwareUpdate[].class);
	}
	
	private void parseKeyValue(String key, String value)
	{
		if(key == null || key.length() == 0)
			return;
		
		if(key.compareTo("EobrDeviceTypeName") == 0){
			TypeSettings ts = getEobrDevice();
			ts.setTypeName(value);
			setEobrDevice(ts);
		}
		else if(key.compareTo("EobrDeviceAssemblyName") == 0)
		{
			TypeSettings ts = getEobrDevice();
			ts.setAssemblyName(value);
			setEobrDevice(ts);
		}
		else if(key.compareTo("EobrIntervalMilliseconds") == 0)
		{
			setEobrIntervalMS(value);
		}
		else if(key.compareTo("EobrHibernateIntervalMilliseconds") == 0)
		{
			setEobrHibernateIntervalMS(value);
		}
		else if(key.compareTo("DownloadHistoryGeoTabTimeIntervalMilliseconds") == 0)
		{
			setDownloadHistoryGeoTabMS(value);
		}
		else if(key.compareTo("GeotabHistoryRepeatDownload") == 0)
		{
			setGeotabHistoryRepeatDownload(value);
		}
		else if(key.compareTo("StatusRecordLogIntervalMilliseconds") == 0)
		{
			setStatusRecordLogIntervalMS(value);
		}
		else if(key.compareTo("RoutePositionIntervalMinutes") == 0)
		{
			setRoutePositionIntervalMinutes(value);
		}
		else if(key.compareTo("HoursAvailableUpdateMinutes") == 0)
		{
			setHoursAvailableUpdateMinutes(value);
		}
		else if(key.compareTo("OverrideReferenceTimestamp") == 0)
		{
			setOverrideReferenceTimestamp(value);
		}
		else if(key.compareTo("ReferenceTimestampXS") == 0)
		{
			setReferenceTimestampXS(value);
		}
		else if(key.compareTo("KmbActivationUrl_REST") == 0)
		{
			setKmbActivationRESTUrl(value);
		}
		else if(key.compareTo("KmbWebServiceUrl_REST") == 0)
		{
			setKmbWebServiceRESTUrl(value);
		}
		else if(key.compareTo("ShowDebugFunctions") == 0)
		{
			setShowDebugFunctions(value);
		}
		else if(key.compareTo("KeepScreenOn") == 0)
		{
			setKeepScreenOn(value);
		}
		else if(key.compareTo("ReverseGeocodeFromLocalDB") == 0)
		{
			setReverseGeocodeFromLocalDB(value);
		}
		else if(key.compareTo("IgnoreServerTime") == 0)
		{
			setIgnoreServerTime(value); 
		}
		else if(key.compareTo("UseBTEFirmwareDownload") == 0)
		{
			setUseBTEFirmwareDownload(value); 
		}
		else if(key.compareTo("EldMandate") == 0)
		{
			setIsELDMandateEnabled(value);
		}
		else if(key.compareTo("DefaultTripInformation") == 0)
		{
			setDefaultTripInformation(value);
		}
		else if(key.compareTo("AlkCopilotActivation") == 0)
		{
			setAlkCopilotActivation(value);
		}
		else if(key.compareTo("AlkCopilotEnabled") == 0)
		{
			setAlkCopilotEnabled(value);
		}
		else if(key.compareTo("PersonalConveyanceEnabled") == 0)
		{
			setPersonalConveyanceEnabled(value);
		}
		else if(key.compareTo("IgnoreFirmwareUpdate") == 0)
		{
			setIgnoreFirmwareUpdate(value); 
		}
		else if (key.equals("NewTeamDriverWorkflowEnabled"))
		{
			setNewTeamDriverWorkflowEnabled(value);
		}
		else if (key.equals("UseCloudServices"))
		{
			setUseCloudServices(value);
		}
		else if(key.compareTo("ForceComplianceTabletMode") == 0)
		{
			setForceComplianceTabletMode(value); 
		}
		else if(key.compareTo("HyrailEnabled") == 0)
		{
			setHyrailEnabled(value);
		}
		else if(key.compareTo("NonRegDrivingEnabled") == 0)
		{
			setNonRegDrivingEnabled(value);
		}
		else if(key.compareTo("GeotabInjectDataStallsEnabled") == 0)
		{
			setGeoTabWatchDogTimerEnabled(value);
		}
		else if(key.compareTo("PositionMalfunctionMinutes") == 0)
		{
			setPositionMalfunctionMinutes(value);
		}
		else if(key.compareTo("EngineSyncMalfunctionMinutes") == 0)
		{
			setEngineSyncMalfunctionMinutes(value);
		}else if(key.compareTo("TimingMalfunctionClearMinutes") == 0) {
			setTimingMalfunctionClearMinutes(value);
		}
		else if(key.compareTo("DataTransferMechanismTimerValue") == 0)
		{
			setDataTransferMechanismTimerValue(value);
		}
		else if(key.compareTo("DataTransferMechanismSuccessDaysToNextTransfer") == 0)
		{
			setDataTransferMechanismSuccessDaysToNextTransfer(value);
		}
		else if(key.compareTo("DataTransferMechanismFailedDaysToNextTransfer") == 0)
		{
			setDataTransferMechanismFailedDaysToNextTransfer(value);
		}
		else if(key.compareTo("SelectiveFeatureTogglesEnabled") == 0)
        {
            setSelectiveFeatureTogglesEnabled(value);
        }
	}

	private String _kmbWebServiceRESTUrl = null;
    /// <summary>
    /// Url of the KMB LogChecker REST-based web service
    /// </summary>
    public String getKmbWebServiceRESTUrl()
    {
    	return _kmbWebServiceRESTUrl;
    }
    public void setKmbWebServiceRESTUrl(String value)
    {
    	_kmbWebServiceRESTUrl = value;
    }

	private String _kmbWebApiServiceUrl = null;
    /// <summary>
    /// Url of the KMB WebApi service
    /// </summary>
    public String getKmbWebApiServiceUrl()
    {
    	return _kmbWebApiServiceUrl;
    }
    public void setKmbWebApiServiceUrl(String value)
    {
    	_kmbWebApiServiceUrl = value;
    }
    
    private String _kmbActivationRESTUrl = null;
    /// <summary>
    /// Url of the KMB Activation REST-based web service
    /// </summary>
    //[XmlElement(ElementName = "KMBActivationUrl")]
    public String getKmbActivationRESTUrl()
    {
    	return _kmbActivationRESTUrl;
    }
    public void setKmbActivationRESTUrl(String value)
    {
    	_kmbActivationRESTUrl = value;
    }
    
    private TypeSettings _eobrDeviceType = new TypeSettings("PLXS.EOBR.EobrDevice, PLXS.EOBR");
    /// <summary>
    /// Type and assembly settings for the EOBR device interface object
    /// </summary>
    //[XmlElement(ElementName = "EobrDevice")]
    public TypeSettings getEobrDevice()
    {
        return _eobrDeviceType;
    }
    public void setEobrDevice(TypeSettings value)
    {
    	_eobrDeviceType = value;
    }

    private int _eobrIntervalMS = 5000;
    /// <summary>
    /// Polling interval for the EOBR device.   
    /// This is in milliseconds.
    /// </summary>
    //[XmlElement(ElementName = "EobrIntervalMilliseconds")]
    public int getEobrIntervalMS()
    {
        return _eobrIntervalMS;
    }
    public void setEobrIntervalMS(int value)
    {
    	_eobrIntervalMS = value;
    }
	private void setEobrIntervalMS(String value)
	{
		_eobrIntervalMS = Integer.parseInt(value);
	}

    private int _eobrHibernateIntervalMS = 60000;
    /// <summary>
    /// Polling interval for the EOBR device when in hiberation mode.
    /// Hibernation is when the app is not active on the device, and the EOBR
    /// is currently offline.
    /// This is in milliseconds.
    /// </summary>
    //[XmlElement(ElementName = "EobrHibernateIntervalMilliseconds")]
    public int getEobrHibernateIntervalMS()
    {
        return _eobrHibernateIntervalMS;
    }
    public void setEobrHibernateIntervalMS(int value)
    {
    	_eobrHibernateIntervalMS = value;
    }
	private void setEobrHibernateIntervalMS(String value)
	{
		_eobrHibernateIntervalMS = Integer.parseInt(value);
	}

	private int _downloadHistoryGeoTabMS = 600000;
	/// <summary>
	/// This is in milliseconds.
	/// </summary>
	//[XmlElement(ElementName = "DownloadUnassignedDrivingTimeGeoTab_TimeIntervalMilliseconds")]
	public int getDownloadHistoryGeoTabMS()
	{
		return _downloadHistoryGeoTabMS;
	}
	public void setDownloadHistoryGeoTabMS(int value)
	{
		_downloadHistoryGeoTabMS = value;
	}
	private void setDownloadHistoryGeoTabMS(String value)
	{
		_downloadHistoryGeoTabMS = Integer.parseInt(value);
	}

	private boolean _geotabHistoryRepeatDownload = false;
	/// <summary>
	/// Should the geotab engine repeat the download of history for the specified interval
	/// </summary>
	//[XmlElement(ElementName = "GeotabHistoryRepeatDownload")]
	public boolean getGeotabHistoryRepeatDownload()
		{
		return _geotabHistoryRepeatDownload;
	}
	public void setGeotabHistoryRepeatDownload(Boolean value)
	{
		_geotabHistoryRepeatDownload = value;
	}
	private void setGeotabHistoryRepeatDownload(String value)
	{
		_geotabHistoryRepeatDownload = Boolean.parseBoolean(value);
	}

    private int _statusRecordLogIntervalMS = 30000;
    /// <summary>
    /// Interval used for logging status records received from the EOBR device.   
    /// This is in milliseconds.
    /// </summary>
    //[XmlElement(ElementName = "StatusRecordLogIntervalMilliseconds")]
    public int getStatusRecordLogIntervalMS()
    {
        return _statusRecordLogIntervalMS;
    }
    public void setStatusRecordLogIntervalMS(int value)
    {
    	_statusRecordLogIntervalMS = value;
    }
	private void setStatusRecordLogIntervalMS(String value)
	{
		_statusRecordLogIntervalMS = Integer.parseInt(value);
	}

	private FirmwareUpdate[] _firmwareUpdates;
	public FirmwareUpdate[] getFirmwareUpdates()
	{
		return _firmwareUpdates;
	}
	
    private int _routePositionIntervalMinutes = 15;
    /// <summary>
    /// Interval used for recording valid GPS coordinates from the EOBR device.   
    /// This is in minutes.
    /// </summary>
    //[XmlElement(ElementName = "RoutePositionIntervalMinutes")]
    public int getRoutePositionIntervalMinutes()
    {
        return _routePositionIntervalMinutes;
    }
    public void setRoutePositionIntervalMinutes(int value)
    {
    	_routePositionIntervalMinutes = value;
    }
	private void setRoutePositionIntervalMinutes(String value)
	{
		_routePositionIntervalMinutes = Integer.parseInt(value);
	}

    private int _hoursAvailableUpdateMinutes = 60;
    /// <summary>
    /// Interval used for sending the HOS Hours Available summary to DMO. 
    /// This is in minutes.
    /// </summary>
    //[XmlElement(ElementName = "HoursAvailableUpdateMinutes")]
    public int getHoursAvailableUpdateMinutes()
    {
        return _hoursAvailableUpdateMinutes;
    }
    public void setHoursAvailableUpdateMinutes(int value)
    {
    	_hoursAvailableUpdateMinutes = value;
    }
	private void setHoursAvailableUpdateMinutes(String value)
	{
		_hoursAvailableUpdateMinutes = Integer.parseInt(value);
	}

    private boolean _overrideReferenceTimestamp = false;
    /// <summary>
    /// Indicates if there is an overriden reference timestamp to use when
    /// building the Unassigned Driving Periods.
    /// This is used in diagnostics of historical driving record processing.
    /// </summary>
    public boolean getOverrideReferenceTimestamp()
    {
        return _overrideReferenceTimestamp;
    }
    public void setOverrideReferenceTimestamp(boolean value)
    {
    	_overrideReferenceTimestamp = value;
    }
	private void setOverrideReferenceTimestamp(String value)
	{
		_overrideReferenceTimestamp = value.compareToIgnoreCase("true") == 0 ? true : false;
	}

    private String _referenceTimestampXS = null;
    /// <summary>
    /// Serializable form of the Date reference timestamp.
    /// </summary>
    public String getReferenceTimestampXS()
    {
        return _referenceTimestamp.toString();
    }
    public void setReferenceTimestampXS(String value) 
    {
        _referenceTimestampXS = value;
        if (_referenceTimestampXS != null || _referenceTimestampXS.trim().length() > 0)
        {
            try
            {
				_referenceTimestamp = DateUtility.getHomeTerminalReferenceTimestampFormat().parse(_referenceTimestampXS);
			}
            catch (ParseException e)
            {
            	
            	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
			}
        }
    }

    private Date _referenceTimestamp = null;
    /// <summary>
    /// Reference timestamp to use when building historical driving records,
    /// rather then the EOBR's reference timestamp.  
    /// This is used in diagnostics so that historical driving records 
    /// can be processed from any timestamp.
    /// The default value is Date.MinValue which causes all historical
    /// records stored in the EOBR to be read and processed.
    /// </summary>
    //[System.Xml.Serialization.XmlIgnore]
    public Date getReferenceTimestamp()
    {
        return _referenceTimestamp;
    }
    public void setReferenceTimestamp(Date value)
    {
    	_referenceTimestamp = value;
    }
        
    private boolean _showDebugFunctions = false;
    public boolean getShowDebugFunctions()
    {
    	// note: this method is intended only to turn on/off the Feature Toggle button on the login screen
    	//       The deprecated ShowDebugFunction is now a feature toggle and accessible like this:
    	//       GlobalState.getInstance().getFeatureService().getShowDebugFunctions()
    	return _showDebugFunctions;
    }
    public void setShowDebugFunctions(String value)
    {
    	_showDebugFunctions = value.compareToIgnoreCase("true") == 0 ? true : false;
    	// note: when the debug functions are set on through app_config, this means that the Feature Toggle button should be visible 
    	_showFeatureToggles = _showDebugFunctions;
    }
    
    private boolean _keepScreenOn = false;
    public boolean getKeepScreenOn()
    {
    	return _keepScreenOn;
    }
    public void setKeepScreenOn(String value)
    {
    	_keepScreenOn = value.compareToIgnoreCase("true") == 0 ? true : false;
    }

	private boolean _appSettingsLoaded = false;
	public boolean getAppSettingsLoaded() {
		return _appSettingsLoaded;
	}

    private boolean _reverseGeocodeFromLocalDB = false;
    public boolean getReverseGeocodeFromLocalDB()
    {
    	return _reverseGeocodeFromLocalDB;
    }
    public void setReverseGeocodeFromLocalDB(String value)
    {
    	_reverseGeocodeFromLocalDB = value.compareToIgnoreCase("true") == 0 ? true : false;
    }
    
    private boolean _ignoreServerTime = false; 
    public boolean getIgnoreServerTime()
    {
    	return _ignoreServerTime;     	
    }
    public void setIgnoreServerTime(String value)
    {
    	_ignoreServerTime = value.compareToIgnoreCase("true") == 0 ? true : false;    	
    }
    
    /*
     * The Use BTE Firmware Download setting is used for the BTE (Networkfleet) firmware update
     * This setting determines whether the KMB download over BT, or the NWF over-the-air 
     * update is used.
     * When set to TRUE, the KMB download over BT is used
     */
    private boolean _useBTEFirmwareDownload= false; 
    public boolean getUseBTEFirmwareDownload()
    {
    	return _useBTEFirmwareDownload;     	
    }
    public void setUseBTEFirmwareDownload(String value)
    {
    	_useBTEFirmwareDownload = value.compareToIgnoreCase("true") == 0 ? true : false;    	
    }
    
    private boolean _isELDMandateEnabled=false; 
    public boolean getIsELDMandateEnabled()
    {
    	return _isELDMandateEnabled;     	
    }
    public void setIsELDMandateEnabled(String value)
    {
    	_isELDMandateEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;    	
    }
    
    private boolean _defaultTripInformation=false;
    public boolean getDefaultTripInformation()
    {
    	return _defaultTripInformation;
    }
    public void setDefaultTripInformation(String value)
    {
    	_defaultTripInformation = value.compareToIgnoreCase("true") == 0 ? true : false; 
    }
    
    private boolean _alkCopilotActivation = true;
    public boolean getAlkCopilotActivation()
    {
    	return _alkCopilotActivation;
    }
    public void setAlkCopilotActivation(String value)
    {
    	_alkCopilotActivation = value.compareToIgnoreCase("true") == 0 ? true : false;
    }
    
    private boolean _alkCopilotEnabled = false;
    public boolean getAlkCopilotEnabled()
    {
    	return _alkCopilotEnabled;
    }
    public void setAlkCopilotEnabled(String value)
    {
    	_alkCopilotEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
    }
    
    private boolean _personalConveyanceEnabled = false;
    public boolean getPersonalConveyanceEnabled()
    {
    	return _personalConveyanceEnabled;
    }
    public void setPersonalConveyanceEnabled(String value)
    {
    	_personalConveyanceEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
    }
    
    private boolean _newTeamDriverWorkflowEnabled = false;
    public boolean getNewTeamDriverWorkflowEnabled()
    {
    	return _newTeamDriverWorkflowEnabled;
    }
    public void setNewTeamDriverWorkflowEnabled(String value)
    {
    	_newTeamDriverWorkflowEnabled = value.equalsIgnoreCase("true");
    }
    
    private boolean _useCloudServices = false;
    public boolean getUseCloudServices()
    {
    	return _useCloudServices;
    }
    public void setUseCloudServices(String value)
    {
    	_useCloudServices = value.equalsIgnoreCase("true");
    }
    
    private boolean _showFeatureToggles = false;
    public boolean getShowFeatureToggles()
    {
    	return _showFeatureToggles;
    }
    
    private String _kmbCloudDataServicesUrl = null; 
    public String getKmbCloudDataServicesUrl()
    {
    	return _kmbCloudDataServicesUrl;     	
    }
    public void setKmbCloudDataServicesUrl(String value)
    {
    	_kmbCloudDataServicesUrl = value;    	
    }
    
    private boolean _ignoreFirmwareUpdate = false; 
    public boolean getIgnoreFirmwareUpdate()
    {
    	return _ignoreFirmwareUpdate;     	
    }
    public void setIgnoreFirmwareUpdate(String value)
    {
    	_ignoreFirmwareUpdate = value.compareToIgnoreCase("true") == 0 ? true : false;    	
    }
    
    private boolean _forceComplainceTabletMode = false; 
    public boolean getForceComplianceTabletMode(){
    	return _forceComplainceTabletMode; 
    }
    public void setForceComplianceTabletMode(String value){
    	_forceComplainceTabletMode = value.compareToIgnoreCase("true") == 0 ? true : false;
    }

    private boolean _hyrailEnabled = false;
    public boolean getHyrailEnabled()
    {
        return _hyrailEnabled;
    }
    public void setHyrailEnabled(String value)
    {
        _hyrailEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
    }

	private boolean _nonRegDrivingEnabled = false;
	public boolean getNonRegDrivingEnabled()
	{
		return _nonRegDrivingEnabled;
	}
	public void setNonRegDrivingEnabled(String value)
	{
		_nonRegDrivingEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
	}

	private boolean _geoTabWatchDogTimerEnabled = false;
	public boolean getGeoTabWatchDogTimerEnabled() { return _geoTabWatchDogTimerEnabled; }
	public void setGeoTabWatchDogTimerEnabled(String value)
	{
		_geoTabWatchDogTimerEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
	}

	private boolean _selectiveFeatureTogglesEnabled;
	public boolean getSelectiveFeatureTogglesEnabled() { return _selectiveFeatureTogglesEnabled; }
	public void setSelectiveFeatureTogglesEnabled(String value) {
		_selectiveFeatureTogglesEnabled = value.compareToIgnoreCase("true") == 0 ? true : false;
	}

	/// <summary>
	/// Number of minutes before a GPS malfunction event occurs
	/// </summary>
	//[XmlElement(ElementName = "PositionMalfunctionMinutes")]
	private int _positionMalfunctionMinutes = 60;
	public int getPositionMalfunctionMinutes() { return _positionMalfunctionMinutes; }
	private void setPositionMalfunctionMinutes(String value) { _positionMalfunctionMinutes = Integer.parseInt(value); }

	private int timingMalffunctionClearMinutes = 30;
	public int getTimingMalfunctionClearMinutes() { return timingMalffunctionClearMinutes; }
	private void setTimingMalfunctionClearMinutes(String value) { timingMalffunctionClearMinutes = Integer.parseInt(value); }


	/// <summary>
	/// Number of minutes before an Engine Synchronization malfunction event occurs
	/// </summary>
	//[XmlElement(ElementName = "EngineSyncMalfunctionMinutes")]
	private int _engineSyncMalfunctionMinutes = 30;
	public int getEngineSyncMalfunctionMinutes() { return _engineSyncMalfunctionMinutes; }
	private void setEngineSyncMalfunctionMinutes(String value) { _engineSyncMalfunctionMinutes = Integer.parseInt(value); }

	/// <summary>
	/// Number of milliseconds to wait before executing the GetDataTransferMechanismStatus call
	/// </summary>
	//[XmlElement(ElementName = "DataTransferMechanismTimerValue")]
	private int _dataTransferMechanismTimerValue = 900000;  //default 15 minutes
	public int getDataTransferMechanismTimerValue() { return _dataTransferMechanismTimerValue; }
	private void setDataTransferMechanismTimerValue(String value) { _dataTransferMechanismTimerValue = Integer.parseInt(value); }

	/// <summary>
	/// Number of days to next transfer after successful transfer
	/// </summary>
	//[XmlElement(ElementName = "DataTransferMechanismSuccessDaysToNextTransfer")]
	private int _dataTransferMechanismSuccessDaysToNextTransfer = 7;  //default 7 days
	public int getDataTransferMechanismSuccessDaysToNextTransfer() { return _dataTransferMechanismSuccessDaysToNextTransfer; }
	private void setDataTransferMechanismSuccessDaysToNextTransfer(String value) { _dataTransferMechanismSuccessDaysToNextTransfer = Integer.parseInt(value); }

	/// <summary>
	/// Number of days to next transfer after failed transfer
	/// </summary>
	//[XmlElement(ElementName = "DataTransferMechanismFailedDaysToNextTransfer")]
	private int _dataTransferMechanismFailedDaysToNextTransfer = 1;  //default 1 day
	public int getDataTransferMechanismFailedDaysToNextTransfer() { return _dataTransferMechanismFailedDaysToNextTransfer; }
	private void setDataTransferMechanismFailedDaysToNextTransfer(String value) { _dataTransferMechanismFailedDaysToNextTransfer = Integer.parseInt(value); }

	public boolean getIsFeatureEnabled(String featureName)
	{
		boolean result = false;
		if(featureName.compareToIgnoreCase("SelectiveFeatureTogglesEnabled") == 0)
		{
			result = getSelectiveFeatureTogglesEnabled();
		}
		else if (featureName.compareToIgnoreCase("EldMandate") == 0)
		{
			result = getIsELDMandateEnabled();
		}
		else if (featureName.compareToIgnoreCase("IgnoreServerTime") == 0)
		{
			result = getIgnoreServerTime();
		}
		else if (featureName.compareToIgnoreCase("ShowDebugFunctions") == 0)
		{
			result = getShowDebugFunctions();
		}
		else if (featureName.compareToIgnoreCase("DefaultTripInformation") == 0)
		{
			result = getDefaultTripInformation();
		}
		else if (featureName.compareToIgnoreCase("AlkCopilotActivation") == 0)
		{
			result = getAlkCopilotActivation();
		}
		else if (featureName.compareToIgnoreCase("AlkCopilotEnabled") == 0)
		{
			result = getAlkCopilotEnabled();
		}
		else if (featureName.compareToIgnoreCase("PersonalConveyanceEnabled") == 0)
		{
			result = getPersonalConveyanceEnabled();
		}
		else if (featureName.compareToIgnoreCase("IgnoreFirmwareUpdate") == 0)
		{
			result = getIgnoreFirmwareUpdate();
		}
		else if (featureName.equalsIgnoreCase("NewTeamDriverWorkflowEnabled"))
		{
			result = getNewTeamDriverWorkflowEnabled();
		}
		else if (featureName.equalsIgnoreCase("UseCloudServices"))
		{
			result = getUseCloudServices();
		}
		else if (featureName.equalsIgnoreCase("ForceComplianceTabletMode"))
		{
			result = getForceComplianceTabletMode();
		}
        else if (featureName.compareToIgnoreCase("HyrailEnabled") == 0)
        {
            result = getHyrailEnabled();
        }

		else if (featureName.compareToIgnoreCase("NonRegDrivingEnabled") == 0)
		{
			result = getNonRegDrivingEnabled();
		}
		else if (featureName.compareToIgnoreCase("GeotabInjectDataStallsEnabled") == 0)
		{
			result = getGeoTabWatchDogTimerEnabled();
		}
		else
		{
			result = false;
		}

		return result;
	}

}
