package com.jjkeller.kmbapi.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.interfaces.ISystemStartupProgress;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.eobrengine.EobrServiceBase;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;

import java.util.ArrayList;
import java.util.List;

public class SystemStartupController extends ControllerBase {

	private ISystemStartupProgress _systemStartupProgressListener = null;
	
	public SystemStartupController(Context ctx) {
		super(ctx);
	}

    /// <summary>
    /// Perform all actions required to get the system started up.
    /// Step 1. Startup up the database.
    /// Step 2. Discover EOBRs, and activate the first one
    /// </summary>
    public boolean PerformSystemStartup_CheckDatabase(ISystemStartupProgress systemStartupProgressListener)
    {
		this._systemStartupProgressListener = systemStartupProgressListener;
		
        this.PublishUpdateEvent(this.getContext().getString(R.string.checkingdatabase));
        return PerformDatabaseStartup();    	
    }
    
    public Bundle PerformSystemStartup_EobrDevice(String macAddress, BluetoothAdapter btAdapter)
    {  		        
       	this.PublishUpdateEvent(this.getContext().getString(R.string.devicediscovery));

       	Bundle bundle = null;
        String eobrDeviceName = "";
        int numEobrDevices = 0;

        try
        {
	        bundle = EobrReader.DiscoverAndActivateByMACAddress(this.getContext(), macAddress, btAdapter);
	        if (bundle != null)
	        {
	        	eobrDeviceName = bundle.getString(this.getContext().getString(R.string.returnvalue));
	
	        	if (eobrDeviceName == null) eobrDeviceName = "";
	        	
	        	if (eobrDeviceName.length() > 0)
	        		numEobrDevices = 1;

                if(macAddress == EobrReader.getInstance().getCurrentBtAddress())
	        	    this.ClearThresholds();

	        	initialize(EobrReader.getInstance());
	        }
	        else
	        {
	        	bundle = new Bundle();
	        }
	        
			bundle.putString(this.getContext().getString(R.string.devicename), eobrDeviceName);
			bundle.putInt(this.getContext().getString(R.string.numberdevices), numEobrDevices);
        }
        catch (KmbApplicationException kae)
        {
        	this.HandleException(kae);
        }
        
        catch (Exception excp)
        {
            // an error occcured initializing the reader
            // most common error here will that the device is not actually connected
            // what to do here?
            // don't throw an exception here, just report that no EOBR is connected
            this.HandleException(excp);
        }
        
        return bundle;
    }

    public List<EobrDeviceDescriptor> PerformSystemStartup_PairedDevices(List<BluetoothDevice> pairedDevices, BluetoothAdapter btAdapter)
    {
       	Bundle bundle = null;
        int GEN2_RVN_BTCLASS = 0x0704;
        List<EobrDeviceDescriptor> deviceList = null;
        
        for (BluetoothDevice btDevice: pairedDevices)
        {
    		BluetoothClass btClass = btDevice.getBluetoothClass();

    		// if bluetooth device class is same as a gen II device, attempt to connect
        	if (btClass.getDeviceClass() == GEN2_RVN_BTCLASS)
        	{
                try
                {
        	        bundle = EobrReader.DiscoverByMACAddress(this.getContext(), btDevice.getAddress(), btAdapter);
        	        if (bundle != null && bundle.containsKey(this.getContext().getString(R.string.returnvalue)))
        	        {
        	        	if (deviceList == null) deviceList = new ArrayList<EobrDeviceDescriptor>();
        	        	deviceList.add(new EobrDeviceDescriptor(bundle.getString(this.getContext().getString(R.string.returnvalue)), bundle.getString(this.getContext().getString(R.string.deviceaddress)), bundle.getInt(this.getContext().getString(R.string.devicegeneration)), bundle.getShort(this.getContext().getString(R.string.devicecrc))));        	
        	        }
        	        
        	        Thread.sleep(200);
                }
                catch (KmbApplicationException kae)
                {
                	this.HandleException(kae);
                }
                
                catch (InterruptedException iexcp)
                {
                	// an interrupted exception occurred when attempting to sleep the thread
                	// log exception to error log
                    this.HandleException(iexcp);                	
                }

                catch (Exception excp)
                {
                    // an error occcured initializing the reader
                    // most common error here will that the device is not actually connected
                    // what to do here?
                    // don't throw an exception here, just report that no EOBR is connected
                    this.HandleException(excp);
                }                
        	}
        }
        
        return deviceList;
    }
    
    /// <summary>
    /// Perform the device discovery on the configured comm bus.
    /// Answer the complete list of devices found.
    /// If no devices found, then return null.
    /// </summary>
    /// <returns></returns>
    public EobrDeviceDescriptor[] PerformFullDeviceDiscovery(List<BluetoothDevice> gen1Devices, List<BluetoothDevice> gen2Devices, boolean provisionNewDevice, String serialNumber)
    {
    	EobrDeviceDescriptor[] deviceNameList = EobrReader.PerformFullDeviceDiscovery(getContext(), gen1Devices, gen2Devices, provisionNewDevice, serialNumber);
        return deviceNameList;
    }
    
    
    // The Testharness does not have CompanyConfigSettings and therefore cannot "consume" any functions involving CompanyConfigSettings.  If it does, 
    // exceptions will be thrown and the application will not function properly.  In the "normal" PerformFullDeviceDiscovery function, a call is made to 
    // getCompanyConfigSettings.  We avoid that here.
    public EobrDeviceDescriptor[] PerformFullDeviceDiscoveryTestHarness(List<BluetoothDevice> gen1Devices, List<BluetoothDevice> gen2Devices, boolean provisionNewDevice, String serialNumber, String passkey)
    {
    	EobrDeviceDescriptor[] deviceNameList = EobrReader.PerformFullDeviceDiscoveryTestHarness(getContext(), gen1Devices, gen2Devices, provisionNewDevice, serialNumber, passkey);
        return deviceNameList;
    }
    
    
    /**
     * Clears all thresholds on the EOBR.
     */
    public void SetUnassignedDriverThreshold()
    {
        //set the unassigned driver thresholds will switch the driver
        //to be unassigned, until switched back. don't want the default
        //driver event to interfere with the rest of the app...
        EobrServiceBase serviceBase = GlobalState.getInstance().getEobrService();
        if(serviceBase != null)
            serviceBase.ignoreNextDefaultDriverEvent();

        // set driver thresholds to the company defaults
        int eventBlanking = 10;
        String defaultDriver = "";

        // set driver thresholds to the company defaults
        CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());
        EobrConfigController configController = new EobrConfigController(this.getContext());
        configController.SetThresholdValues(
                this.getContext(),
                companyConfigSettings.getMaxAcceptableTach(),
                companyConfigSettings.getMaxAcceptableSpeed(),
                companyConfigSettings.getHardBrakeDecelerationSpeed(),
                companyConfigSettings.getDriverStartDistance(),
                companyConfigSettings.getDriverStopMinutes(),
                eventBlanking,
                defaultDriver,
                companyConfigSettings.getMandateDrivingStopTimeMinutes(),
                companyConfigSettings.getDriveStartSpeed());
    }
        
    /**
     * This will shutdown the EOBR device so that a different handheld can discover and create a partnership with it.
     * The connection must be ONLINE in order to do this.
     * Returns true if the operation is successful and false otherwise.
     * @return true if the operation is successful and false otherwise
     * @throws KmbApplicationException
     */
    public boolean ShutdownEobrDevice() throws KmbApplicationException
    {
        boolean isShutdown = false;
        EobrReader eobr = EobrReader.getInstance();
        if (eobr != null)
        {
            if (eobr.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE)
            {
            	this.SetUnassignedDriverThreshold();
            	eobr.Technician_ShutdownEobrDevice(getContext());

                MalfunctionManager.getInstance().clearActiveMalfunctionsWhenDoneWithTab();
                MalfunctionManager.getInstance().clearActiveDataDiagnosticsWhenDoneWithTab();

                isShutdown = true;
            }
        }
        return isShutdown;
    }
    
    
    // The Testharness does not have CompanyConfigSettings and therefore cannot "consume" any functions involving CompanyConfigSettings.  If it does, 
    // exceptions will be thrown and the application will not function properly.  In the "normal" ShutdownEobrDevice function, a call is made to 
    // ClearThresholds which involves CompanyConfigSettings.  We avoid that here.
    public boolean ShutdownEobrDeviceTestHarness() throws KmbApplicationException {
    	
        boolean isShutdown = false;
        EobrReader eobr = EobrReader.getInstance();
        
        if (eobr != null) {
            if (eobr.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
            	eobr.Technician_ShutdownEobrDevice(getContext());
                isShutdown = true;
            }
        }
        
        return isShutdown;
    }    
    
    
    /// <summary>
    /// Perform all of the actions required to startup the database.
    /// Check to see if the database is current, otherwise migrate it to the
    /// new version.
    /// If the db is current, then attempt to compact it.
    /// Answer if the startup actions were successfully completed.
    /// </summary>
    /// <returns>true if successful, false otherwise</returns>
    private boolean PerformDatabaseStartup()
    {
        boolean isSuccessful = true;
        
        //TODO
        // check with the server to see if there is an update available
        //AppUpdateController appUpdateCtrlr = new AppUpdateController();
        //bool dbUpdateRequired = appUpdateCtrlr.IsDBUpdateRequired();

        //if (dbUpdateRequired)
        //{
        //    this.PublishUpdateEvent("Updating database...");
        //    isSuccessful = appUpdateCtrlr.PerformDBUpdate();
            // should be no need to compact the database in this case 
            // because we've started with an emptyDB and just inserted stuff
            // the db should be clean already
        //}
        //else
        //{
            // no db update required, so attempt to compact the database
        //    try
        //    {
        //        string dbFilePath = Configuration.Globals.DatabaseFilePath;
        //        SqlCeEngine engine = new SqlCeEngine("Data Source=" + dbFilePath);
        //        engine.Shrink();
        //        isSuccessful = true;
        //    }
        //    catch (Exception excp)
        //    {
                // log any error that may have occurred
        //        Utility.ErrorLog.RecordException(excp);
        //    }
        //}

        return isSuccessful;
    }

	private void PublishUpdateEvent(String message)
	{
    	if (this._systemStartupProgressListener != null)
    		this._systemStartupProgressListener.onProgressChanged(message);
	}
	
    /// <summary>
    /// Answer if the application has already started
    /// </summary>
    /// <returns></returns>
    public boolean IsAppStarted()
    {
        // when there is a logged in user, then the app is started
        return this.getCurrentUser() != null;
    }
    
    
    /// <summary>
    /// This method should ONLY be called from the Testharness application.  We have this specific method for the Testharness because
    /// several things do not need to execute for the Testharness to run.  Namely, the Testharness does not need to perform anything 
    /// related to current logs, reading historical records, setting odometer, etc.  Therefore, we have created this method specifically
    /// for the Testharness.
    ///
    /// Perform the device discovery on the configured comm bus.
    /// Answer the complete list of devices found.
    /// If no devices found, then return null.
    /// </summary>
    /// <returns></returns>
    public boolean TestHarness_ActivateEobrDevice(String deviceName) throws KmbApplicationException {
    	
    	boolean isSuccessful = false;
        EobrReader eobr = EobrReader.getInstance();
        
        if (eobr != null) {
        	eobr.TestHarness_ActivateEobrDevice(getContext(), deviceName);
            isSuccessful = true;
        }
        
        return isSuccessful;    	
    }
    
    
    /// <summary>
    /// Perform the device discovery on the configured comm bus.
    /// Answer the complete list of devices found.
    /// If no devices found, then return null.
    /// </summary>
    /// <returns></returns>
    public boolean ActivateEobrDevice(String deviceName) throws KmbApplicationException
    {
    	return ActivateEobrDevice(deviceName, null, (short)-1, -1);
    }

    /// <summary>
    /// Perform the device discovery on the configured comm bus.
    /// Answer the complete list of devices found.
    /// If no devices found, then return null.
    /// </summary>
    /// <returns></returns>
    public boolean ActivateEobrDevice(String deviceName, String deviceAddress, short deviceCrc, int deviceGeneration) throws KmbApplicationException
    {
    	boolean isSuccessful = false;
        EobrReader eobr = EobrReader.getInstance();
        if (eobr != null)
        {
        	if(deviceAddress != null)
        		eobr.ActivateEobrDevice(getContext(), deviceName, deviceAddress, deviceCrc, deviceGeneration);
        	else
        		eobr.ActivateEobrDevice(getContext(),  deviceName);
        	
            isSuccessful = true;

            initialize(eobr);
        }
        
        return isSuccessful;
    }

    public boolean ActivateEobrDevice(String deviceName, int deviceGeneration) throws KmbApplicationException {
        EobrReader eobr = EobrReader.getInstance();

        if(eobr != null) {
            eobr.ActivateEobrDevice(getContext(), deviceName, null, (short)0, deviceGeneration);

            initialize(eobr);

            return true;
        }

        return false;
    }

    private void initialize(EobrReader eobr) throws KmbApplicationException
    {
    	if(eobr == null)
    		return;
    	
    	LogEntryController logEntryCtrl = new LogEntryController(this.getContext());

    	// AMO - 12/11/12 Set the EOBR threshold values using the Unit Rules or Company Rules
    	User curDriver = this.getCurrentDesignatedDriver();
        int driverIdCrc = logEntryCtrl.SetThresholdValues(curDriver, true, false);

        // Apply CRC if designated driver isn't null
        if (curDriver != null)
        	this.getCurrentDesignatedDriver().getCredentials().setDriverIdCrc(driverIdCrc);
        
        // MLM - 9/29/14 Moved the setting of odometer above the reading of History.   
        // There was an issue when the Eobr had unassigned events and the reading history was performed before the Odometer has set
                
        // setup the last eobr odometer and last eobr odometer date when activating
        // to an eobr.  This solves issue where driver leaves one truck, releases eobr,
        // gets into another truck, discovers and activates without ever exiting
        // KMB.  In this scenario, the last eobr settings were still from the previous 
        // truck and were never updated.
        logEntryCtrl.SetLastEobrOdometer();
                    
        EobrConfigController eobrConfigController = new EobrConfigController(this.getContext());
        eobrConfigController.LoadOdometerOffsetFromEobr();
        
        //set a flag in the ELD to specify whether it should use the new mandate rules
        boolean isEldMandate = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        int rc = eobr.SetIsEldMandate(isEldMandate);
        if(rc != EobrReturnCode.S_SUCCESS && rc != EobrReturnCode.S_FUNC_NOT_IMPLEMENTED)
        	throw new KmbApplicationException("Unable to set ELD mandate compliance mode");
        
        // If activating from the Device Discovery screen from within KMB - not
        // during initial startup, then need to read history
        // Moved reading history from the ActivateEobrDevice method because
        // first needed to set thresholds to cause DRIVER event and associated
        // events
        if (this.IsAppStarted())
        	eobr.ReadAndPublishHistoricalStatusRecords(eobr.isEobrGen1(),this.getContext());
    }
    
    /// <summary>
    /// Answer the currently initialized EOBR
    /// </summary>
    /// <returns></returns>
    public String getCurrentEobrIdentifier()
    {
        String eobrId = null;
        EobrReader eobr = EobrReader.getInstance();
        if (eobr != null)
        {
            eobrId = eobr.getEobrIdentifier();
        }
        return eobrId;
    }
    
    /// <summary>
    /// Answer the mac address of the currently initialized EOBR
    /// </summary>
    /// <returns></returns>
    public String getCurrentEobrMacAddress()
    {
        String eobrMacAddress = null;
        EobrReader eobr = EobrReader.getInstance();
        if (eobr != null)
        {
        	eobrMacAddress = eobr.EobrMacAddress();
        }
        return eobrMacAddress;
    }
    
    public boolean IsEobrDeviceOnline() throws KmbApplicationException
    {
    	boolean isOnline = false;
        EobrReader eobr = EobrReader.getInstance();
        if (eobr != null)
        {
        	EobrReader.ConnectionState currentState = eobr.getCurrentConnectionState();
            if (currentState == EobrReader.ConnectionState.ONLINE || currentState == EobrReader.ConnectionState.READINGHISTORICAL)
            {
                isOnline = true;
            }
        }
        return isOnline;
    }

    /**
     * Clears all thresholds on the EOBR.
     */
    public void ClearThresholds()
    {
        int eventBlanking = 10;
        String defaultDriver = "";

        // set driver thresholds to the company defaults
        CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());
        EobrConfigController configController = new EobrConfigController(this.getContext());
        configController.SetThresholdValues(
                this.getContext(),
                companyConfigSettings.getMaxAcceptableTach(),
                companyConfigSettings.getMaxAcceptableSpeed(),
                companyConfigSettings.getHardBrakeDecelerationSpeed(),
                companyConfigSettings.getDriverStartDistance(),
                companyConfigSettings.getDriverStopMinutes(),
                eventBlanking,
                defaultDriver,
                companyConfigSettings.getMandateDrivingStopTimeMinutes(),
                companyConfigSettings.getDriveStartSpeed());
    }
}
