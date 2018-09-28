package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.eobrengine.Enums;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class FirmwareUpgraderBase implements IFirmwareUpgrader {
	
	protected Context context;
	protected IEobrService eobrService;
	protected IEobrReader eobr;
	
	protected String originalTractorNumber;
	protected float originalOdometerOffset;
	protected DatabusTypeEnum originalBusType;
	protected String currentEobrMacAddress;

	protected FirmwareUpdateBroadcaster broadcaster = new FirmwareUpdateBroadcaster();
	
	protected FirmwareUpgraderBase(IEobrReader eobrReader, IEobrService eobrService)
	{
		this.eobr = eobrReader;
		this.eobrService = eobrService;
		this.context = eobrService.getContext();
	}
	
	protected abstract InputStream getFirmwareImage();
	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.EOBR.IFirmwareUpgrader#getFirmwareUpdateConfig()
	 */
	public abstract FirmwareUpdate getFirmwareUpdateConfig();
	
	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.EOBR.IFirmwareUpgrader#getIsFirmwareUpgradeRequired()
	 */
	public boolean getIsFirmwareUpgradeRequired()
	{	
		if(GlobalState.getInstance().getFeatureService().getIgnoreFirmwareUpdate())
			return false;
		
        Bundle versionInfo = eobr.Technician_GetEOBRRevisions();
        int rc = versionInfo.getInt(context.getString(R.string.rc));
        
        if(rc == EobrReturnCode.S_SUCCESS)
        {
        	String installedVersion = versionInfo.getString(context.getString(R.string.mainfirmwarerevision));
        	
        	FirmwareUpdate firmwareUpdateConfig = getFirmwareUpdateConfig();
        	firmwareUpdateConfig.setInstalledVersion(installedVersion);

			//check to see if KMB has a config for the currently installed version -
			//if it does, check to see if the setting to prevent it from being overwritten is turned on
			//if it is, then we don't need to perform an update.
			FirmwareUpdate installedVersionConfig = FirmwareUpgraderFactory.GetFirmwareUpdateWithVersion(context, firmwareUpdateConfig.getGeneration(), firmwareUpdateConfig.getMaker(), installedVersion);
        	if(installedVersionConfig != null && installedVersionConfig.getPreventOverwrite())
				return false;

        	return !installedVersion.startsWith(firmwareUpdateConfig.getVersion()) || firmwareUpdateConfig.getForceUpdate();
        }
        
        return false;
	}
	
	public boolean getIsApplicationUpgradeRequired() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.EOBR.IFirmwareUpgrader#initiateFirmwareUpgrade()
	 */
	public void initiateFirmwareUpgrade(boolean downgradeConfirmed)
	{		
		if(!getIsFirmwareUpgradeRequired() && !downgradeConfirmed)
			return;

		performFirmwareUpgrade();
	}

	protected void performFirmwareUpgrade() {
		GlobalState.getInstance().setAbortFirmwareUpgrade(false);

		FirmwareUpdate firmwareUpdateConfig = getFirmwareUpdateConfig();

		Log.v("FirmwareUpgrade", String.format("Download firmware update to version: %s", firmwareUpdateConfig.getVersion()));
		ErrorLogHelper.RecordMessage(context, String.format("Firmware update to version '%s' is being performed", firmwareUpdateConfig.getVersion()));

		broadcaster.onFirmwareUpdateStart();

		boolean isSuccessful = false;
		boolean okToContinue = upgradePreparation(firmwareUpdateConfig);

		if(okToContinue)
            isSuccessful = upgradeFirmware(firmwareUpdateConfig);

		upgradeCleanup(isSuccessful);
	}

	protected boolean upgradeFirmware(FirmwareUpdate firmwareUpdateConfig)
	{
		boolean isSuccessful = false;
		String installedVersion = null;
		String[] versionPieces = null;

				InputStream fwImage = null;
		
		try
		{
			fwImage = getFirmwareImage();
		}
		catch(NotFoundException e)
		{
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		
		if(fwImage != null)
		{
			boolean okToContinue = upgradeBootLoaderFirmware(firmwareUpdateConfig, fwImage);

			if(okToContinue)
				okToContinue = upgradeAppFirmware(firmwareUpdateConfig, fwImage);

			if(okToContinue)
				isSuccessful = verifyFirmwareUpgrade(firmwareUpdateConfig);

			if(isSuccessful && firmwareUpdateConfig.getIsConditional())
			{
				RecordConditionalFirmwareUpgrade(firmwareUpdateConfig);
			}

			try
			{
				fwImage.close();
			}
			catch (IOException e)
			{
				Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
			}
		}
		else
		{
            // the firmware file does not exist
            ErrorLogHelper.RecordMessage(context, "Firmware update file does not exist.");
		}
		
		return isSuccessful;
	}

	private void RecordConditionalFirmwareUpgrade(FirmwareUpdate firmwareUpdateConfig) {
		String installedVersion;
		String[] versionPieces;

		Bundle versionInfo = eobr.Technician_GetEOBRRevisions();
		installedVersion = versionInfo.getString(context.getString(R.string.mainfirmwarerevision));
		String[] versionLeadingPart;;

		// remove trailing part of version (if it exists): 6.89.17 (1487706091)
		if (!TextUtils.isEmpty(installedVersion)) {
			versionLeadingPart = installedVersion.split(" ");
			installedVersion = versionLeadingPart[0];
		}

		versionPieces = installedVersion.split("\\.");
		int majorVersion = Integer.parseInt(versionPieces[0]);
		int minorVersion = Integer.parseInt(versionPieces[1]);
		int patchVersion = Integer.parseInt(versionPieces[2]);

		try {
            FirmwareUpgraderFactory.RecordConditionalFirmwareUpgrade(context, eobr.getEobrSerialNumber(), firmwareUpdateConfig.getGeneration(), majorVersion, minorVersion, patchVersion);
        } catch (KmbApplicationException e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
	}

	protected boolean upgradeAppFirmware(FirmwareUpdate firmwareUpdateConfig, InputStream fwImage)
	{
		boolean okToContinue = true;
	
        Log.v("FirmwareUpgrade", "update app firmware");

        // now, put the device into "firmware update" mode
    	eobr.TransitionDeviceToNewState(context, ConnectionState.FIRMWAREUPDATE, "APP");

        // don't publish any status changes until the firmware update completes
		eobr.setOnStatusChangeEnabled(false);

        // download the firmware file....this may take a while
        int rc = eobr.Technician_DownloadFirmwareUpdate(fwImage, Enums.FirmwareUpgradeTypeEnum.APP, firmwareUpdateConfig);
        if (rc == EobrReturnCode.S_SUCCESS)
        {
            // the update was successfully downloaded
            Log.v("FirmwareUpgrade", "app successfully updated");
            
            ErrorLogHelper.RecordMessage(context, "Firmware download of APP successfully completed, rediscovering eobr");

				// shutdown the EOBR here so that we can reload completely
				try {
					eobr.Technician_ShutdownEobrDevice(context);
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordException(context, e);
				}

	        // now, wait for the EOBR to be discovered again
			okToContinue = eobr.WaitForEobrDiscovery(context, currentEobrMacAddress, 10);
            if (!okToContinue)
            	ErrorLogHelper.RecordMessage(context, "Discovery of EOBR failed after firmware APP update.");
        }
        else
        {
            // the firmware download was not successful, what do we do now?
        	okToContinue = false;
            Log.v("FirmwareUpgrade", "Failed to update APP");        	                    	
            ErrorLogHelper.RecordMessage(context, String.format("Firmware download for APP failed, rc: '%s'", rc));
        }

        return okToContinue;
    }
	
	protected boolean upgradeBootLoaderFirmware(FirmwareUpdate firmwareUpdateConfig, InputStream fwImage)
	{
		boolean okToContinue = true;
		
        if (firmwareUpdateConfig.getUpdateBootLoader())
        {
            // yes it does, so download the bootloader update
            // now, put the device into "firmware update" mode
            Log.v("FirmwareUpgrade", "Update bootloader");        	
            eobr.TransitionDeviceToNewState(context, ConnectionState.FIRMWAREUPDATE, "BOOTLOADER");

            okToContinue = false;
            
            // download the firmware file to update the bootloader....this may take a while
            int rc = eobr.Technician_DownloadFirmwareUpdate(fwImage, Enums.FirmwareUpgradeTypeEnum.BOOTLOADER, firmwareUpdateConfig);
            if (rc == EobrReturnCode.S_SUCCESS)
            {
                // the bootloader update downloaded successfully
                Log.v("FirmwareUpgrade", "Bootloader successfully updated, shutting down the connection");        	
                ErrorLogHelper.RecordMessage(context, String.format("Firmware download of BOOTLOADER successfully completed, rediscovering eobr"));
                
                // shutdown the EOBR here so that we can reload completely
                // because there have been some failures rediscovering 
                // the EOBR after the bootloader update
                try {
					eobr.Technician_ShutdownEobrDevice(context);
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordException(context, e);
					return false;
				}

                // now, wait for the EOBR to be discovered again
                okToContinue = eobr.WaitForEobrDiscovery(context, currentEobrMacAddress, 10);
                if (!okToContinue) {
					ErrorLogHelper.RecordMessage(context, "Discovery of EOBR failed after firmware BOOTLOADER update.");
				}
            }
            else
            {
                // the firmware update was not successful, what do we do now?
            	okToContinue = false;
                Log.v("FirmwareUpgrade", "Failed to update bootloader");        	                    	
                ErrorLogHelper.RecordMessage(context, String.format("Firmware download for the BOOTLOADER failed, rc: '%s'", rc));
            }
        }
        
        return okToContinue;
	}
	
	protected boolean upgradePreparation(FirmwareUpdate firmwareUpdateConfig)
	{
		boolean okToContinue = true;
		
        currentEobrMacAddress = eobr.EobrMacAddress();

        // read the historical driving periods off the EOBR
        // before downloading firmware
        if (firmwareUpdateConfig.getReadHistoryFirst())
        {
            Log.v("FirmwareUpgrade", "Attempting to read history");        	
            ErrorLogHelper.RecordMessage(context, "FirmwareUpgrade, attempting to read history");

	        ArrayList<StatusRecord> historyList = eobrService.ReadHistoricalStatusRecords();
	        eobr.PublishEobrHistoricalRecords(historyList);
	        
	        // note: the history reading process is considered successful when either is true
	        //          1. there are records coming back in the historyList
	        //          2. OR the EobrReader's state is ONLINE
	        okToContinue = (historyList != null && historyList.size() > 0) || (eobr.getCurrentConnectionState() == ConnectionState.ONLINE);
	        
	        String msg = String.format("FirmwareUpgrade, after reading history found '%s' items, okToContinue '%s'", historyList == null ? "0" : historyList.size(), okToContinue );
            Log.v("FirmwareUpgrade", msg);        	
            ErrorLogHelper.RecordMessage(context, msg);        	

            if (!okToContinue) ErrorLogHelper.RecordMessage(context, "FirmwareUpgrade, EOBR history reading failed.");
        }
        
        // Prior to updating the firmware, get the Odometer Offset and hold onto it.
        // If we don't, the value will be wiped out by the update.
		Bundle bundle = eobr.Technician_ReadOdometerCalibrationValues();
		if (bundle != null && bundle.containsKey(context.getString(R.string.offsetparam)))
			originalOdometerOffset = bundle.getFloat(context.getString(R.string.offsetparam));
		
		// Also get the bus type in case it gets wiped out by the update
		originalBusType = eobr.Technician_GetBusType(context);
        
        // Prior to updating the firmware, get the unit id and hold onto it.
        // It may get erased as part of the firmware update and need to be reset after the firmware update
		originalTractorNumber = eobr.getEobrIdentifier();
        
        return okToContinue;
	}
	
	protected void upgradeCleanup(boolean isSuccessful)
	{
        // Start publishing status changes again
        eobr.setOnStatusChangeEnabled(true);
        
		if (isSuccessful)
		{
			// Update with the saved Odometer Offset (saved prior to firmware update).
        	eobr.Technician_SetOdometerCalibration(originalOdometerOffset, 0.0F);

        	// Also update the saved bus type
        	eobr.Technician_SetBusType(originalBusType.getValue());
			
        	// applying firmware update resets the TAB which clears the driver's
        	// thresholds and reverts to company default thresholds, reset driver's
        	// thresholds now that firmware update was successful
        	eobrService.UpdateDriverThresholds();

			// check if unit Id has been modified
        	String newTractorNumber = eobr.getEobrIdentifier();
        	if (!originalTractorNumber.equals("") && !newTractorNumber.equals("") && !originalTractorNumber.equals(newTractorNumber))
        		eobr.Technician_SetUniqueIdentifier(originalTractorNumber);
        	
            // since we're doing a firmware update...make the device go offline
            // temporarily so that the history records can be built.
        	eobr.TransitionDeviceToNewState(context, ConnectionState.OFFLINE, "DownloadFirmwareUpdate: go offline");
        	
        	// note: only resume the reading process if the firmware update was successful.
        	eobr.ResumeReading();
        	
        	eobrService.setIsFinishingFirmwareUpdate(true);
	    }
        else
        {
            // some aspsect of the update failed (either bootloader, or app)
        	eobr.TransitionDeviceToNewState(context, ConnectionState.DEVICEFAILURE, "EOBR Firmware Update failed.");
        	
        	// if the firmware does not install correctly then suspend to indicate the failure
        	// note: this suspend should be redundant because the start of the firmware download should have suspended everything
        	eobrService.SuspendReading();
        	
        	eobrService.setIsFinishingFirmwareUpdate(false);

			broadcaster.onFirmwareUpdateFinished(false);
        }
	}
	
	protected boolean verifyFirmwareUpgrade(FirmwareUpdate firmwareUpdateConfig)
	{
		boolean isSuccessful = false;
		
        Log.v("FirmwareUpgrade", "Verifying that the update was successful");        	
        ErrorLogHelper.RecordMessage(context, String.format("Verifying installed version matches the expected '%s'", firmwareUpdateConfig.getVersion()));
		
        // verify that the firmware just installed applied correctly
        // this is done by reading the version number off the EOBR
        // and comparing it to what we expect to be down there
        // If not correct, issue a failure
        Bundle versionInfo = eobr.Technician_GetEOBRRevisions();
        int rc = versionInfo.getInt(context.getString(R.string.rc));
        if (rc == EobrReturnCode.S_SUCCESS)
        {
        	String installedVersion = versionInfo.getString(context.getString(R.string.mainfirmwarerevision));
            String expectedVersion = firmwareUpdateConfig.getVersion();
            
            Log.v("FirmwareUpgrade", String.format("Expected version: %s installed version: %s", expectedVersion, installedVersion ));        	
            
            if (!installedVersion.startsWith(expectedVersion))
            {
                // something bad happened while applying the firmware update
                // the version number from the EOBR does not match what we thought it was going to be
                ErrorLogHelper.RecordMessage(context, String.format("Firmware update version in EOBR '%s' does not match expected version '%s'", installedVersion, expectedVersion));
			}
			else
			{
            	// this is the only way that the whole firmware update process is successful
            	// the installed version matches the expected version            	
            	isSuccessful = true;
            	ErrorLogHelper.RecordMessage(context, String.format("Firmware successfully updated in EOBR to '%s' ", installedVersion));
            }
        }
		else
		{
			ErrorLogHelper.RecordMessage(context, String.format("Failure reading firmware version rc: '%s'", rc));
		}
        
        return isSuccessful;
	}

	public void signalUpdateFailed(){
		broadcaster.onFirmwareUpdateFinished(false);
	}
}
