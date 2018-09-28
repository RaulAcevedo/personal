package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.DatabusTypeEnum;

import org.joda.time.DateTime;

import java.util.Date;

public class EobrConfiguration extends ProxyBase
{
	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String serialNumber;
	private String tractorNumber;
	private DatabusTypeEnum databusType = new DatabusTypeEnum(DatabusTypeEnum.NULL);
	private int sleepModeMinutes = -1;
	private int dataCollectionRate = -1;
	private String firmwareVersion = "";
	private int majorFirmwareVersion = 0;
	private int minorFirmwareVersion = 0;
	private int patchFirmwareVersion = 0;
	private String discoveryPasskey = "";
	private Date odometerCalibrationDate;
	private Float eobrOdometer = 0F;
	private Float dashboardOdometer = 0F;
	private Float speedometerThreshold = 65F;
	private Integer tachometerThreshold = 1800;
	private Float hardBrakeThreshold = 7F;
	private int eobrGeneration = 0;
	private Date lastPowerCycleResetDate;
    private Long clockSyncOffset = null;
    private DateTime clockSyncDateUTC = null; // UTC
	private String vin;
	private Date lastEventReferenceTimestamp;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getSerialNumber()
	{
		return this.serialNumber;
	}
	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	public String getTractorNumber()
	{
		return this.tractorNumber;
	}
	public void setTractorNumber(String tractorNumber)
	{
		this.tractorNumber = tractorNumber;
	}
	
	public DatabusTypeEnum getDatabusType()
	{
		return this.databusType;
	}
	public void setDatabusType(DatabusTypeEnum databusType)
	{
		this.databusType = databusType;
	}
	
	public int getSleepModeMinutes()
	{
		return this.sleepModeMinutes;
	}
	public void setSleepModeMinutes(int sleepModeMinutes)
	{
		this.sleepModeMinutes = sleepModeMinutes;
	}

	public int getDataCollectionRate()
	{
		return this.dataCollectionRate;
	}
	public void setDataCollectionRate(int dataCollectionRate)
	{
		this.dataCollectionRate = dataCollectionRate;
	}

	public String getFirmwareVersion()
	{
		return this.firmwareVersion;
	}
	public void setFirmwareVersion(String firmwareVersion)
	{
		this.firmwareVersion = firmwareVersion;
	}

	public int getMajorFirmwareVersion() { return this.majorFirmwareVersion; }
	public void setMajorFirmwareVersion(int majorFirmwareVersion)
	{
		this.majorFirmwareVersion = majorFirmwareVersion;
	}

	public int getMinorFirmwareVersion() { return this.minorFirmwareVersion; }
	public void setMinorFirmwareVersion(int minorFirmwareVersion) {
		this.minorFirmwareVersion = minorFirmwareVersion;
	}

	public int getPatchFirmwareVersion() { return this.patchFirmwareVersion; }
	public void setPatchFirmwareVersion(int patchFirmwareVersion) {
		this.patchFirmwareVersion = patchFirmwareVersion;
	}

	public String getDiscoveryPasskey()
	{
		return this.discoveryPasskey;
	}
	public void setDiscoveryPasskey(String discoveryPasskey)
	{
		this.discoveryPasskey = discoveryPasskey;
	}
	
	public Date getOdometerCalibrationDate()
	{
		return this.odometerCalibrationDate;
	}
	public void setOdometerCalibrationDate(Date odometerCalibrationDate)
	{
		this.odometerCalibrationDate = odometerCalibrationDate;
	}

	public Float getEobrOdometer()
	{
		return this.eobrOdometer;
	}
	public void setEobrOdometer(Float eobrOdometer)
	{
		this.eobrOdometer = eobrOdometer;
	}

	public Float getDasboardOdometer()
	{
		return this.dashboardOdometer;
	}
	public void setDashboardOdometer(Float dashboardOdometer)
	{
		this.dashboardOdometer = dashboardOdometer;
	}

	public Float getSpeedometerThreshold()
	{
		return this.speedometerThreshold;
	}
	public void setSpeedometerThreshold(Float speedometerThreshold)
	{
		this.speedometerThreshold = speedometerThreshold;
	}

	public Integer getTachometerThreshold()
	{
		return this.tachometerThreshold;
	}
	public void setTachometerThreshold(Integer tachometerThreshold)
	{
		this.tachometerThreshold = tachometerThreshold;
	}

	public Float getHardBrakeThreshold()
	{
		return this.hardBrakeThreshold;
	}
	public void setHardBrakeThreshold(Float hardBrakeThreshold)
	{
		this.hardBrakeThreshold = hardBrakeThreshold;
	}

	public int getEobrGeneration()
	{
		return this.eobrGeneration;
	}
	public void setEobrGeneration(int eobrGeneration)
	{
		this.eobrGeneration = eobrGeneration;
	}
	
	public Date getLastPowerCycleResetDate()
	{
		return this.lastPowerCycleResetDate;
	}
	public void setLastPowerCycleResetDate(Date value)
	{
		this.lastPowerCycleResetDate = value;
	}


    public Long getClockSyncOffset() {
        return clockSyncOffset;
    }

    public void setClockSyncOffset(Long clockSyncOffset) {
        this.clockSyncOffset = clockSyncOffset;
    }

    public DateTime getClockSyncDateUTC() {
        return clockSyncDateUTC;
    }

    public void setClockSyncDateUTC(DateTime clockSyncDateUTC) {
        this.clockSyncDateUTC = clockSyncDateUTC;
    }

	public String getVIN() { return this.vin; }
	public void setVIN(String vin)
	{
		this.vin = vin;
	}

	public Date getLastEventReferenceTimestamp() { return this.lastEventReferenceTimestamp; }
	public void setLastEventReferenceTimestamp(Date lastEventReferenceTimestamp) { this.lastEventReferenceTimestamp = lastEventReferenceTimestamp; }

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

    // Generated from Generate -> ToString()

    @Override
    public String toString() {
        return this.getTractorNumber();
    }

}
