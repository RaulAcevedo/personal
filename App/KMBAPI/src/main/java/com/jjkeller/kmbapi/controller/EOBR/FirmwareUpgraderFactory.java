package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;
import android.os.Bundle;

import com.android.internal.util.Predicate;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.VersionUtility;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.FirmwareUpgradeController;
import com.jjkeller.kmbapi.controller.dataaccess.FacadeFactory;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.StringUtility;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums;
import com.jjkeller.kmbapi.proxydata.ConditionalFirmwareUpgrade;

public class FirmwareUpgraderFactory {

	public static IFirmwareUpgrader GetFirmwareUpgrader(IEobrReader eobrReader, IEobrService eobrService)
	{
		return FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService, new DatabusTypeEnum(DatabusTypeEnum.NULL));
	}

	public static String MIN_FAST_COMPLIANT_VERSION = "6.88.100";
	public static String MIN_MANDATE_FAST_COMPLIANT_VERSION = "6.88.113";
	private static String FIRMWARE_LOG_TAG = "FirmwareUpdater";

	public static IFirmwareUpgrader GetFirmwareUpgrader(IEobrReader eobrReader, IEobrService eobrService, DatabusTypeEnum databusTypeEnum)
	{
		Context context = eobrService.getContext();

		IFirmwareUpgrader upgrader = null;

		int generation = eobrReader.getEobrGeneration();
		switch(generation)
		{
			case Constants.GENERATION_GEN_I:
			{
				upgrader = new FirmwareUpgraderGenI(eobrReader, eobrService);
				break;
			}
			case Constants.GENERATION_GEN_II:
			{
				String maker;
				if(eobrReader.Technician_GetEobrHardware(context)) {

					LogCat.getInstance().i(FIRMWARE_LOG_TAG, "Firmware upgrader type JJKA Gen2 " );
					// if the above call worked this is a JJKA Gen2 device
					maker = Constants.JJKA;
					upgrader = getFirmwareUpgrader(eobrReader, eobrService, databusTypeEnum, eobrReader, context, upgrader, generation, maker);
				}
				else {
					// for the NWF BTE, decide which updater to use
					if (GlobalState.getInstance().getAppSettings(context).getUseBTEFirmwareDownload()) {
						LogCat.getInstance().i(FIRMWARE_LOG_TAG, "use the KMB direct firmware downloader over BT " );
						// use the KMB direct firmware downloader over BT
						upgrader = new FirmwareUpgraderBTEDownload(eobrReader, eobrService);
					}else {
						LogCat.getInstance().i(FIRMWARE_LOG_TAG, " use the Networkfleet over-the-air updater" );
						// use the Networkfleet over-the-air updater
						upgrader = new FirmwareUpgraderBTE(eobrReader, eobrService);
					}
				}
				break;
			}
			case Constants.GENERATION_GEOTAB: {
				LogCat.getInstance().i(FIRMWARE_LOG_TAG, " GEOTAB - user firmware blocker" );
				upgrader = new FirmwareUpgradeBlocker();

				break;
			}
		}

		return upgrader;
	}

	private static IFirmwareUpgrader getFirmwareUpgrader(IEobrReader eobrReader, IEobrService eobrService, DatabusTypeEnum databusTypeEnum, IEobrReader reader, Context context, IFirmwareUpgrader upgrader, int generation, String maker) {
		//find the appropriate update configuration based on the specified bus type
		FirmwareUpdate updateConfig;

		Bundle versionInfo = reader.Technician_GetEOBRRevisions();
		int rc = versionInfo.getInt(context.getString(R.string.rc));
		boolean firmwareIsJfastCompliant;
		boolean shouldForceUpdate;
		boolean	hasDualModeOrJfast = (databusTypeEnum.getValue() == DatabusTypeEnum.DUALMODEJ1708J1939F || databusTypeEnum.getValue() == DatabusTypeEnum.J1939F);

		String installedVersion = versionInfo.getString(context.getString(R.string.mainfirmwarerevision));
		String minFastBusVersion = getMinimumFastBusCompliantFirmwareVersion();

		installedVersion = stripPatchId(installedVersion);
		if(rc == Enums.EobrReturnCode.S_SUCCESS && !installedVersion.isEmpty()) {

			firmwareIsJfastCompliant = VersionUtility.compareVersions(installedVersion, minFastBusVersion) >= 0;

			//if user is on 6.89.0, specifically, then set them to 6.88.100 PBI 45183
			shouldForceUpdate = !firmwareIsJfastCompliant && hasDualModeOrJfast || VersionUtility.compareVersions(installedVersion, "6.89.00") == 0;

            if(shouldForceUpdate) {
				updateConfig = FirmwareUpgraderFactory.GetFirmwareUpdateWithVersion(context, generation, maker, minFastBusVersion);
				//causes successful firmware upgrade to be recorded as conditional: RecordConditionalFirmwareUpgrade is called in FirmwareUpgraderBase
				updateConfig.setIsConditional(true);
				updateConfig.setForceUpdate(true);
			}
            else {
                updateConfig = FirmwareUpgraderFactory.GetDefaultFirmwareUpdate(context, generation, maker);
			}

			upgrader = new FirmwareUpgraderGenII(eobrReader, eobrService, updateConfig,
					new FirmwareImageDownloader(), FacadeFactory.GetInstance());
		}

		return upgrader;
	}

	private static String getMinimumFastBusCompliantFirmwareVersion(){
		if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			return MIN_MANDATE_FAST_COMPLIANT_VERSION;
		} else{
			return MIN_FAST_COMPLIANT_VERSION;
		}
	}

	public static void RecordConditionalFirmwareUpgrade(Context context, String eldSerialNumber, int generation, int majorVersion, int minorVersion, int patchVersion) throws KmbApplicationException {
		FirmwareUpgradeController fwController = new FirmwareUpgradeController(context);
		ConditionalFirmwareUpgrade cfu = new ConditionalFirmwareUpgrade(eldSerialNumber, generation, majorVersion, minorVersion, patchVersion);
		fwController.RecordConditionalFirmwareUpgrade(cfu);
	}

	public static FirmwareUpdate GetDefaultFirmwareUpdate(Context context, int generation, String maker) {
		return FirmwareUpgraderFactory.GetFirmwareUpdate(context, generation, maker,
				new Predicate<FirmwareUpdate>() {
					@Override
					public boolean apply(FirmwareUpdate firmwareUpdate) {
						return firmwareUpdate.getDefaultVersion();
					}
				});
	}

	public static FirmwareUpdate GetFirmwareUpdateWithVersion(Context context, int generation, String maker, final String version) {
		return FirmwareUpgraderFactory.GetFirmwareUpdate(context, generation, maker,
				new Predicate<FirmwareUpdate>() {
					@Override
					public boolean apply(FirmwareUpdate firmwareUpdate) {
						return version.startsWith(firmwareUpdate.getVersion());
					}
				});
	}

	private static FirmwareUpdate GetFirmwareUpdate(Context context, int generation, String maker, Predicate<FirmwareUpdate> predicate) {
		for(FirmwareUpdate update : GlobalState.getInstance().getAppSettings(context).getFirmwareUpdates()) {
			if(update.getGeneration() == generation && update.getMaker().equalsIgnoreCase(maker)) {
				if(predicate.apply(update))
					return update;
			}
		}

		return null;
	}

	// remove trailing part of version (if it exists): 6.89.17 (1487706091)
	protected static String stripPatchId(String installedVersion) {
		if (StringUtility.notNullOrEmpty(installedVersion)) {
			String[] versionPieces = installedVersion.split(" ");
			installedVersion = versionPieces[0];
		}
		return installedVersion;
	}
}
