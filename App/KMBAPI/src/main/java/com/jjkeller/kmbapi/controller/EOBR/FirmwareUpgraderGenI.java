package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;

import java.io.InputStream;

public class FirmwareUpgraderGenI extends FirmwareUpgraderBase {

	private int generation = 1;
	private FirmwareUpdate firmwareUpdate = null;
	
	protected FirmwareUpgraderGenI(IEobrReader eobrReader, IEobrService eobrService)
	{
		super(eobrReader, eobrService);
	}

	@Override
	protected InputStream getFirmwareImage() {
		return context.getResources().openRawResource(R.raw.eobr_gen1);
	}

	@Override
	public FirmwareUpdate getFirmwareUpdateConfig() {
		
		if(firmwareUpdate == null)
		{
			for(FirmwareUpdate update : GlobalState.getInstance().getAppSettings(context).getFirmwareUpdates())
			{
				if(update.getGeneration() == generation)
					firmwareUpdate = update;
			}
		}
		
		return firmwareUpdate;
	}

}
