package com.jjkeller.kmbapi.controller.EOBR;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;

import java.io.InputStream;
import java.util.Locale;

public class FirmwareUpgraderBTE extends FirmwareUpgraderBase {
	
	private int generation = 2;
	private FirmwareUpdate firmwareUpdate = null;
	
	public static final int PROGRESS_INCOMPLETE = -1;
	public static final int PROGRESS_WAITING = -2;
	public static final int PROGRESS_CHECKING = 0;
	public static final int PROGRESS_DOWNLOAD_FINISHED = 99999;
	public static final int POLLING_INTERVAL = 1; //seconds
	
	protected FirmwareUpgraderBTE(IEobrReader eobrReader, IEobrService eobrService)
	{
		super(eobrReader, eobrService);
		
		//load the config right away
		getFirmwareUpdateConfig();
	}

	@Override
	protected InputStream getFirmwareImage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FirmwareUpdate getFirmwareUpdateConfig() {
		
		if(firmwareUpdate == null)
		{
			for(FirmwareUpdate update : GlobalState.getInstance().getAppSettings(context).getFirmwareUpdates())
			{
				if(update.getGeneration() == generation && update.getMaker().equalsIgnoreCase("Networkfleet"))
					firmwareUpdate = update;
			}
		}
		
		return firmwareUpdate;
	}
	
	@Override
	public boolean getIsApplicationUpgradeRequired()
	{
		FirmwareUpgradeStatusResult result = eobr.Technician_GetFirmwareUpdateStatus();

		if (result.getReturnCode() == EobrReturnCode.S_SUCCESS)
		{
			long installedVersion = result.getCurrentFirmwarePatchId();
			long requiredVersion = firmwareUpdate.getFirmwarePatchId();

			Log.v("ApplicationUpgrade", String.format("Checking if application upgrade is required. Installed firmware version %d, required firmware version %d", installedVersion, requiredVersion));

			if(installedVersion == 0)
				return true;
			else
			return installedVersion > requiredVersion;
		}

		return false;
	}
	
	@Override
	public boolean getIsFirmwareUpgradeRequired()
	{
		if(GlobalState.getInstance().getFeatureService().getIgnoreFirmwareUpdate())
			return false;
		
        FirmwareUpgradeStatusResult result = eobr.Technician_GetFirmwareUpdateStatus();
        
        if(result.getReturnCode() == EobrReturnCode.S_SUCCESS)
        {
        	long installedVersion = result.getCurrentFirmwarePatchId();
        	long requiredVersion = firmwareUpdate.getFirmwarePatchId();
        	
        	Log.v("FirmwareUpgrade", String.format("Checking if BTE firmware upgrade is required. Installed version %d, required version %d", installedVersion, requiredVersion));
        	
        	return installedVersion < requiredVersion || firmwareUpdate.getForceUpdate();
        }
        
        return false;
	}
	
	@Override
	protected boolean upgradeFirmware(FirmwareUpdate firmwareUpdateConfig)
	{
		boolean okToContinue = false;
		
        Log.v("FirmwareUpgrade", "update BTE firmware");        	

        // now, put the device into "firmware update" mode
    	eobr.TransitionDeviceToNewState(context, ConnectionState.FIRMWAREUPDATE, "BTE");

        // don't publish any status changes until the firmware update completes
        eobr.setOnStatusChangeEnabled(false);
        
        FirmwareUpgradeRequestResult result = eobr.Technician_RequestFirmwareUpdate(firmwareUpdateConfig.getFirmwarePatchId());
        
        if (result.getReturnCode() == EobrReturnCode.S_SUCCESS && result.getIsSuccessful())
        {
            // the update was successfully downloaded
            Log.v("FirmwareUpgrade", "BTE firmware upgrade requested");
            
        	//WORKAROUND: we need to know when the BTE has disconnected
    		IntentFilter filter = new IntentFilter();
    		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    		context.registerReceiver(disconnectReceiver, filter);
    		                        
            try {
				okToContinue = waitForCompletion();
			} catch (InterruptedException e1) {}
            
            if(okToContinue)
            {
            	ErrorLogHelper.RecordMessage(context, "Firmware download of BTE successfully completed, rediscovering eobr");
            	
	            // shutdown the EOBR here so that we can reload completely
	            try {
					eobr.Technician_ShutdownEobrDevice(context);
				} catch (KmbApplicationException e) {
					ErrorLogHelper.RecordException(context, e);
					okToContinue = false;
				}

	            // now, wait for the EOBR to be discovered again
				okToContinue = eobr.WaitForEobrDiscovery(context, currentEobrMacAddress, 10);
	            
				if (!okToContinue)
	            	ErrorLogHelper.RecordMessage(context, "Discovery of EOBR failed after firmware BTE update.");
	            else
	            	okToContinue = verifyFirmwareUpgrade(firmwareUpdate);
            }
        }
        
        return okToContinue;
	}
	
	protected boolean waitForCompletion() throws InterruptedException
	{
		boolean isComplete = false;
		boolean isSuccess = false;
			
		//allow things to settle
		Thread.sleep(10000);
		
		broadcaster.onDownloadFirmwareProgress(PROGRESS_WAITING);

		while(!isComplete)
		{
			Thread.sleep(POLLING_INTERVAL * 1000);
	
			//wait until the BTE has disconnected or the user
			//has chosen to abort the firmware upgrade
			if(_hasBteDisconnected)
			{
				isComplete = true; 
				isSuccess = true;

				broadcaster.onDownloadFirmwareProgress(PROGRESS_DOWNLOAD_FINISHED);
			}
			else
				//fail the upgrade if the user turned off Bluetooth or chose to stop waiting
				isComplete = _hasBtTurnedOff || GlobalState.getInstance().getAbortFirmwareUpgrade();
/*
 			WORKAROUND 2.0
			With the current state of the BTE firmware upgrade process in the 5500, there is no way
			for us to tell what the progress of the firmware upgrade is.  Theoretically, we should
			be able to tell that it failed, is in progress, or is complete - but at this point, once
			we issue the command to start the upgrade, we're blind - the only thing we can do is
			wait for the BTE to be reset (if it ever does reset).  The reset signifies that the
			BTE is in the final stage of the upgrade process.
	

			FirmwareUpgradeStatusResult result = null;
			
			try
			{
				result = eobr.Technician_GetFirmwareUpdateStatus();
			} catch(Exception e)
			{
				if(e instanceof TimeoutException)
				{
					//WORKAROUND 1.0
					//The communication timed out.  Unfortunately, at this point this is the only way to know that the BTE has
					//been reset by the 5500, and is a normal part of the firmware upgrade process.
					//
					//Once NWF has fixed the issue identified in WORKAROUND 2.0 above, this shouldn't be necessary anymore
					//since we should then be in charge of when the BTE resets.
					
					String message = "TimeoutException received while upgrading BTE firmware.  Assuming BTE has been reset...";
					Log.v("FirmwareUpgrade", message);
					ErrorLogHelper.RecordMessage(message);
									
					isSuccess = true;
					listener.onDownloadFirmwareProgress(PROGRESS_DOWNLOAD_FINISHED);
					
					
				}
				else
				{
					Log.e("FirmwareUpgrade", "Exception received checking BTE upgrade status.", e);
					ErrorLogHelper.RecordException(context,  e);
				}
				
				break;
			}
			
			if(result.getReturnCode() == EobrReturnCode.S_SUCCESS)
			{
				//NOTE: This is the way we're supposed to be able to do things
				
				Log.v("FirmwareUpgrade", String.format("Staged firmware patch ID: %1$d, Running firmware patch ID: %2$d", result.getStagedFirmwarePatchId(), firmwareUpdate.getFirmwarePatchId()));

				if(result.getIsUpgradeFailed())
				{
					String message = "Firmware update failed. Is there an invalid patch ID in the config?";
					Log.e("FirmwareUpgrade", message);
					ErrorLogHelper.RecordMessage(message);

					isComplete = true;
				}
				
				if(result.getIsUpgradeInProgress())
				{
					Log.v("FirmwareUpgrade", "Firmware update still in progress...");
					listener.onDownloadFirmwareProgress(PROGRESS_INCOMPLETE);
				}
				else
				{
					isComplete = true;
					
					if(result.getStagedFirmwarePatchId() == firmwareUpdate.getFirmwarePatchId())
						isSuccess = true;
						
					listener.onDownloadFirmwareProgress(PROGRESS_DOWNLOAD_FINISHED);
				}
			}
			else
			{
				String message = String.format(Locale.getDefault(), "BTE firmware upgrade status check failed with return code: %d", result.getReturnCode());
				
				Log.e("FirmwareUpgrade", message);
				ErrorLogHelper.RecordMessage(message);
				
				isComplete = true;
			}*/
		}
		
		return isSuccess;
	}
		
	
	
	@Override
	protected boolean verifyFirmwareUpgrade(FirmwareUpdate firmwareUpdateConfig)
	{
		boolean isSuccessful = false;
		
		FirmwareUpgradeStatusResult result = eobr.Technician_GetFirmwareUpdateStatus();

		String message = String.format(Locale.getDefault(), 
				"Intended firmware patch ID: %1$d, Actual firmware patch ID: %2$d", 
				firmwareUpdate.getFirmwarePatchId(), 
				result.getCurrentFirmwarePatchId());
		
		Log.v("FirmwareUpgrade", message);
		ErrorLogHelper.RecordMessage(context, message);
		
		if(result.getReturnCode() == EobrReturnCode.S_SUCCESS)
			isSuccessful = result.getCurrentFirmwarePatchId() == firmwareUpdateConfig.getFirmwarePatchId();
		
		return isSuccessful;
	}
	
	@Override
	protected boolean upgradeAppFirmware(FirmwareUpdate firmwareUpdateConfig, InputStream fwImage) { throw new UnsupportedOperationException(); }
	
	@Override
	protected boolean upgradeBootLoaderFirmware(FirmwareUpdate firmwareUpdateConfig, InputStream fwImage) { throw new UnsupportedOperationException(); }
	
	private boolean _hasBteDisconnected = false;
	private boolean _hasBtTurnedOff = false;
	private BroadcastReceiver disconnectReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			
			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceName = null;
				
				if (device != null)
				{
		            deviceName = device.getName();
		            
			        if(deviceName.equalsIgnoreCase(eobr.getEobrIdentifier()))
			        {
			        	_hasBteDisconnected = true;
			        	context.unregisterReceiver(this);
			        }
				}
				
				Log.v("FirmwareUpgrade", String.format("Received Bluetooth disconnection notification for device %s", deviceName == null ? "null" : deviceName));
			} else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
			{
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR); 
				
				Log.v("FirmwareUpgrade", String.format("Received Blueooth state change %d", state));
				
				if(state != BluetoothAdapter.STATE_ON)
				{
					ErrorLogHelper.RecordMessage("Bluetooth turned off during BTE firmware upgrade.");
					
					_hasBtTurnedOff = true;
					context.unregisterReceiver(this);
				}
					
			}
		}
	
	};
}
