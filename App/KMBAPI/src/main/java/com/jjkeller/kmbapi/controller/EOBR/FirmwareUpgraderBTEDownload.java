package com.jjkeller.kmbapi.controller.EOBR;

import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;

import java.io.InputStream;
import java.util.Locale;

public class FirmwareUpgraderBTEDownload extends FirmwareUpgraderBase {
	
	private int generation = 2;
	private FirmwareUpdate firmwareUpdate = null;
	
	protected FirmwareUpgraderBTEDownload(IEobrReader eobrReader, IEobrService eobrService)
	{
		super(eobrReader, eobrService);
		
		//load the config right away
		getFirmwareUpdateConfig();
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
        	
        	// note: this is slightly different than the BTE firmware updater because we want to update firmware if the versions are different at all
        	return installedVersion != requiredVersion || firmwareUpdate.getForceUpdate();
        }
        
        return false;
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
	protected InputStream getFirmwareImage() {
		return context.getResources().openRawResource(R.raw.bte_eobr);
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
	

}
