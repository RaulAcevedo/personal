package com.jjkeller.kmbapi.controller.EOBR;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.VersionUtility;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.interfaces.IFacadeFactory;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.IRESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbapi.controller.utility.RestWebServiceHelperFactory;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.FirmwareVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FirmwareUpgraderGenII extends FirmwareUpgraderBase {

	protected int generation = 2;
	private FirmwareUpdate firmwareUpdateConfig;
	private String firmwareImageName = null;
	private boolean isDowngradingFirmware = false;
	private final IFirmwareImageDownloader firmwareImageDownloader;
	private final IFacadeFactory facadeFactory;

	private static String MIN_GEN_2_5_1_FIRMWARE = "6.88.113";
	public static String MIN_MANDATE_VERSION = "6.88.113";

	protected FirmwareUpgraderGenII(IEobrReader eobrReader, IEobrService eobrService, FirmwareUpdate fwUpdateConfig, IFirmwareImageDownloader fwImageDownloader, IFacadeFactory facadeFactory)
	{
		super(eobrReader, eobrService);

		firmwareUpdateConfig = fwUpdateConfig;
		firmwareImageDownloader = fwImageDownloader;
		this.facadeFactory = facadeFactory;
	}

	@Override
	protected InputStream getFirmwareImage() {
		if (firmwareImageName == null)
			return null;

		InputStream inputStream = null;
		File firmwareDirectory = new File(context.getFilesDir(), GlobalState.FIRMWARE_IMAGE_DIRECTORY);

		File path = new File(firmwareDirectory, firmwareImageName);
		try {
			inputStream = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			ErrorLogHelper.RecordException(context, e);
		}

		return inputStream;
	}

	private boolean hasValidReturnCode(Bundle b){
		return b.getInt(Constants.RETURNCODE) == Enums.EobrReturnCode.S_SUCCESS;
	}

	@Override
	public boolean getIsFirmwareUpgradeRequired()
	{
		if(GlobalState.getInstance().getFeatureService().getIgnoreFirmwareUpdate())
			return false;

		Bundle versionInfo = eobr.Technician_GetEOBRRevisions();
		if (!hasValidReturnCode(versionInfo)) {
			return false;
		}

		String installedVersion = versionInfo.getString(context.getString(R.string.mainfirmwarerevision));
		firmwareUpdateConfig.setInstalledVersion(installedVersion);

		//check to see if KMB has a config for the currently installed version -
		//if it does, check to see if the setting to prevent it from being overwritten is turned on
		//if it is, then we don't need to perform an update.
		FirmwareUpdate installedVersionConfig = FirmwareUpgraderFactory.GetFirmwareUpdateWithVersion(context, firmwareUpdateConfig.getGeneration(), firmwareUpdateConfig.getMaker(), installedVersion);
		if(installedVersionConfig != null && installedVersionConfig.getPreventOverwrite()) {
			return false;
		}

		if (firmwareUpdateConfig.getForceUpdate()) {
			firmwareImageName = firmwareUpdateConfig.getImageFileName();
			return true;
		}

		String eobrSerialNumber = eobr.getEobrSerialNumber();
		installedVersion = FirmwareUpgraderFactory.stripPatchId(installedVersion);
		String expectedVersionValue = null;
		ExpectedFirmwareVersionSource expectedFirmwareVersionSource = null;

		FirmwareVersion expectedNetworkVersion = getExpectedNetworkVersion(eobrSerialNumber);
		if (expectedNetworkVersion != null) {
			expectedVersionValue = expectedNetworkVersion.getVersionString();
			expectedFirmwareVersionSource = ExpectedFirmwareVersionSource.NetworkCall;
		}

		// you may have a valid network connection - but IIS may be down/stopped or
		// your not on the VPN. In those cases, treat it like no network connection.
		if (expectedVersionValue == null) {
			EobrConfigurationFacade facade = facadeFactory.getEobrConfigurationFacade(context);
			EobrConfiguration config = facade.Fetch(eobrSerialNumber);

			//checking major config number to avoid downgrading back to the default bundled version
			if (config != null && config.getMajorFirmwareVersion() > 0) {
				expectedVersionValue = VersionUtility.getVersionString(config.getMajorFirmwareVersion(), config.getMinorFirmwareVersion(), config.getPatchFirmwareVersion());

				firmwareImageName = VersionUtility.getImageFileName(generation, config.getMajorFirmwareVersion(),
						config.getMinorFirmwareVersion(), config.getPatchFirmwareVersion());
				expectedFirmwareVersionSource = ExpectedFirmwareVersionSource.LocalDatabase;
			}
			else {
				useMinimumMandateVersionIfNeeded(installedVersion);

				expectedVersionValue = firmwareUpdateConfig.getVersion();
				firmwareImageName = firmwareUpdateConfig.getImageFileName();
				expectedFirmwareVersionSource = ExpectedFirmwareVersionSource.BundledDefault;
			}
		}

		//Don't allow downgrading below mandate minimum
		if (isBelowMinimumMandateVersion(expectedVersionValue))
			return false;

		firmwareUpdateConfig.setVersion(expectedVersionValue);
		int installedVersionVsRequired = VersionUtility.compareVersions(installedVersion, expectedVersionValue);

		if (installedVersionVsRequired == 0)
			return false;
		else if (installedVersionVsRequired > 0) {
			isDowngradingFirmware = shouldDowngradeFirmware(installedVersion, expectedVersionValue, expectedFirmwareVersionSource);

			if(isGen2_5_1() && VersionUtility.compareVersions(expectedVersionValue, MIN_GEN_2_5_1_FIRMWARE) <= 0){
				Log.w("FirmwareUpgrade", "skip upgrade because gen251 requires gt 6.88.113");
				isDowngradingFirmware = false;
			}
			return isDowngradingFirmware;
		}
		else
			return true;
	}

	private FirmwareVersion getExpectedNetworkVersion(String eobrSerialNumber) {
		if (!NetworkUtilities.VerifyNetworkConnection(context))
			return null;

		FirmwareVersion firmwareVersion;
		VersionUtility.ParsedVersion parsedBundledVersion = VersionUtility.parseVersionString(firmwareUpdateConfig.getVersion());
		firmwareVersion = checkServerForFirmwareUpdate(parsedBundledVersion.getMajor(), parsedBundledVersion.getMinor(), eobrSerialNumber);

		if (firmwareVersion != null) {
			saveFirmwareVersion(eobrSerialNumber, firmwareVersion);
			firmwareImageDownloader.downloadImageIfNotOnDevice(context, firmwareVersion);
			firmwareImageName = firmwareVersion.getImageFileName();
		}

		return firmwareVersion;
	}

	private void useMinimumMandateVersionIfNeeded(String installedVersion) {
		if (isBelowMinimumMandateVersion(installedVersion)) {
			firmwareUpdateConfig = FirmwareUpgraderFactory.GetFirmwareUpdateWithVersion(context,
					generation, Constants.JJKA, MIN_MANDATE_VERSION);
		}
	}

	private boolean isBelowMinimumMandateVersion(String version) {
		boolean isBelowMinimumMandate = VersionUtility.compareVersions(version, MIN_MANDATE_VERSION) < 0;

		return GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()
				&& isBelowMinimumMandate;
	}

	private boolean shouldDowngradeFirmware(String installedVersion, String expectedVersionValue, ExpectedFirmwareVersionSource expectedFirmwareVersionSource) {
		boolean shouldDowngrade = false;

		switch (expectedFirmwareVersionSource) {
            case NetworkCall:
                shouldDowngrade = true;
				break;
            case LocalDatabase:
                boolean sameMajorMinorVersions = VersionUtility.compareMajorMinorVersions(installedVersion, expectedVersionValue) == 0;
				shouldDowngrade = !sameMajorMinorVersions;
				break;
            case BundledDefault:
				shouldDowngrade = false;
				break;
        }

        return shouldDowngrade;
	}

	private void saveFirmwareVersion(String eobrSerialNumber, FirmwareVersion firmwareVersion) {
		EobrConfigurationFacade facade = facadeFactory.getEobrConfigurationFacade(context);
		EobrConfiguration config = facade.Fetch(eobrSerialNumber);

		if (config == null) {
            config = new EobrConfiguration();
            config.setSerialNumber(eobrSerialNumber);
            String eobrId = EobrReader.getInstance().getEobrIdentifier();
            if (eobrId != null) {
				config.setTractorNumber(eobrId);
			} else {
            	config.setTractorNumber("");
			}
            config.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.UNKNOWN));
            config.setEobrGeneration(generation);
            config.setDiscoveryPasskey("-");
        }

		config.setMajorFirmwareVersion(firmwareVersion.getMajor());
		config.setMinorFirmwareVersion(firmwareVersion.getMinor());
		config.setPatchFirmwareVersion(firmwareVersion.getPatch());
		config.setFirmwareVersion(firmwareVersion.getVersionString());
		config.setSerialNumber(eobrSerialNumber);

		facade.Save(eobrSerialNumber, config);
	}

	private boolean isGen2_5_1() {
		Bundle hardwareInfo = eobr.getEobrEngine().GetEobrHardware();
		String hardwareVersion = hardwareInfo.getString(EobrEngineBase.EOBR_HARDWARE_VERSION);

		return hasValidReturnCode(hardwareInfo) && "2.5.1".equals(hardwareVersion);
	}

	@Override
	public FirmwareUpdate getFirmwareUpdateConfig() {
		return firmwareUpdateConfig;
	}

	private FirmwareVersion checkServerForFirmwareUpdate(int majorVersion, int minorVersion, String serialNumber)
	{
		FirmwareVersion firmwareVersion = null;
		IRESTWebServiceHelper rwsh = RestWebServiceHelperFactory.getInstance(context);

		try {
			firmwareVersion = rwsh.CheckForFirmwareUpdate(serialNumber, majorVersion, minorVersion);
		} catch (IOException e) {
			ErrorLogHelper.RecordException(context, e);
		}

		return firmwareVersion;
	}

	private enum ExpectedFirmwareVersionSource {
		NetworkCall,
		LocalDatabase,
		BundledDefault
	}

	@Override
	public void initiateFirmwareUpgrade(boolean downgradeConfirmed) {
		if (getIsFirmwareUpgradeRequired() || downgradeConfirmed) {
			if (!isDowngradingFirmware || downgradeConfirmed){
				performFirmwareUpgrade();
			} else {
				String tractorNumber = eobr.getEobrIdentifier();
				broadcaster.shouldDowngradeFirmware(tractorNumber);
			}
		}
	}
}