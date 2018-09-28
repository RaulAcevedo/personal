package com.jjkeller.kmb.firmware;

import com.jjkeller.kmb.share.BaseActivity;

public interface IFirmwareUpdateProgressListener
{
	void onFirmwareUpdateStart(BaseActivity activity);
	
	/**
	 * Called periodically while the firmware download progresses.
	 * @param progress the approximate percentage of the firmware download that has been completed, ranging from 0 to 100 (inclusive)
	 */
	void onDownloadFirmwareProgress(BaseActivity activity, int progress);
	
	void onFirmwareUpdateFinished(BaseActivity activity, boolean success);

	void shouldDowngradeFirmware(BaseActivity activity, String tractorNumber);
}
