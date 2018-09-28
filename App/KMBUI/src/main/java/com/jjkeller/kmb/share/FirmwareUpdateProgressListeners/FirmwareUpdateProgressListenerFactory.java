package com.jjkeller.kmb.share.FirmwareUpdateProgressListeners;

import android.util.Log;

import com.jjkeller.kmb.EobrConfig;
import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.firmware.IFirmwareUpdateProgressListener;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbui.R;

public class FirmwareUpdateProgressListenerFactory {

	private FirmwareUpdateProgressListenerFactory() {

	}

	public static IFirmwareUpdateProgressListener getProgressListener(final BaseActivity activity) {
		IFirmwareUpdateProgressListener handler = null;
		if(activity instanceof RodsEntry){
			handler = getProgressListener(activity, RodsEntry.class, null, null);
		} else if(activity instanceof EobrConfig){
			final EobrConfig eobrConfig = (EobrConfig) activity;
			handler = getProgressListener(activity, null,
					//success action
					new Runnable() {
						@Override
						public void run() {

							EobrReader.getInstance().TransitionDeviceToNewState(eobrConfig, EobrReader.ConnectionState.ONLINE);
							eobrConfig.saveConfig();
						}
					},
					//failure action
					new Runnable() {
						@Override
						public void run() {
							eobrConfig.ShowMessage(eobrConfig, eobrConfig.getString(R.string.busTypeFirmwareFailure));
						}
					}
			);
		}
		return handler;
	}

	private static IFirmwareUpdateProgressListener getProgressListener(final BaseActivity activity, final Class activityToStart, final Runnable successAction, final Runnable failureAction)
	{
		IFirmwareUpdateProgressListener listener = null;
		EobrReader eobrReader = EobrReader.getInstance();
		if(eobrReader.getCurrentConnectionState() != EobrReader.ConnectionState.FIRMWAREUPDATE) {
			final boolean isJJK = eobrReader.Technician_GetEobrHardware(activity);
			final int generation = eobrReader.getEobrGeneration();

			if (generation == 2 && !isJJK) {
				// NWF BTE device
				if (GlobalState.getInstance().getAppSettings(activity).getUseBTEFirmwareDownload()) {
					// since we're using download over BT, we can use the standard progress handler
					listener = new FirmwareUpdateProgressHandler(activityToStart, 4, successAction, failureAction);
				} else {
					listener = new FirmwareUpdateProgressHandlerBTE(activity, activityToStart, successAction, failureAction);
				}
			} else {
				// JJK ELD
				listener = new FirmwareUpdateProgressHandler(activityToStart, 3, successAction, failureAction);
			}
		}
		return listener;
	}


}

