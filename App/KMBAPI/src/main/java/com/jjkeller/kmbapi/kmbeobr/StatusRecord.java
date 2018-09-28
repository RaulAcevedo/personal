package com.jjkeller.kmbapi.kmbeobr;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.DeviceErrorFlags;

import java.util.Date;

public class StatusRecord {

    /// <summary>
    /// Conversion factor to translate between kilometers and miles
    /// </summary>
    private static final double KM_PER_MILE = 1.609344;


    private static final float MIN_GPS_LATITUDE = -90.0F;
    private static final float MAX_GPS_LATITUDE = 90.0F;
    private static final float MIN_GPS_LONGITUDE = -180.0F;
    private static final float MAX_GPS_LONGITUDE = 180.0F;

    private int recordId = 0;
    private Date timestampUtc = null;
    private int overallStatus = 0;
    private int activeBusType = DatabusTypeEnum.NULL;
    private boolean isEngineOn = false;
    private float speedometerReading = -1.0F;
    private float odometerReading = -1.0F;
    private float tachometer = -1.0F;
    private float instantFuelEconomy;
    private float averageFuelEconomy;
    private int cruiseControlStatus;
    
    private TransmissionRange transmissionRangeSelected;
    private TransmissionRange transmissionRangeAttained;
    
    private float totalFuelUsed;
    private float brakePressure;
    
    private Date gpsTimestampUtc = null;
    private float gpsLatitudeDeg = 0.0F;
    private float gpsLongitudeDeg = 0.0F;
    private char northSouthInd = '-';
    private char eastWestInd = '-';
    private char posFixIndicator = '-';
    
    private int diagnosticData_dwAddress = 0;
    private int diagnosticData_bReserved = 0;
    private int diagnosticData_bReserved1 = 0;
    
    private boolean ignored = false;
    private int block;
    private int page;
    private int entry;
    
    // Gen II additional fields - will not be populated for Gen I
    private int diagRecId;
    private int transmissionRange;
    private boolean ptoStatus = false;
    private int coolantPressure;
    private int coolantLevel;
    private int coolantTemp;
    private int transmissionOilLevel;
    private int transmissionOilPressure;
    private int throttlePos;
    private int engineLoad;
    private int engineOilPressure;
    private int engineOilTemp;
    private int engineOilLevel;
    private boolean parkBrakeState;
    private boolean milStatus;
    private int activeDTCs;
    private int pendingDTCs;
    private int evalSup;
    private int airFlow;
    private int manAbsPressure;
    private int intakeAirTemp;
    private float gpsDOP;
    private int gpsHeading;
    private float gpsSpeed;
    private float gpsAltitude;
    private float gpsUncertDistance;
	public int[] speedDetailMilli = new int[14];
	public float[] speedDetailSpeed = new float[14];
	private float tripOdometer;
	private float tripEngineSeconds;
    
	public int getRecordId() {
		return recordId;
	}
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
	
	public Date getTimestampUtc() {
		return timestampUtc;
	}
	public void setTimestampUtc(Date timestampUtc) {
		this.timestampUtc = timestampUtc;
	}
	
	public int getOverallStatus() {
		return overallStatus;
	}
	public void setOverallStatus(int overallStatus) {
		this.overallStatus = overallStatus;
	}
	
	public int getActiveBusType() {
		return activeBusType;
	}
	public void setActiveBusType(int activeBusType) {
		this.activeBusType = activeBusType;
	}
	
	public boolean getIsEngineOn() {
		return isEngineOn;
	}
	public void setIsEngineOn(boolean isEngineOn) {
		this.isEngineOn = isEngineOn;
	}
	
	public float getSpeedometerReading() {
		return speedometerReading;
	}
	public void setSpeedometerReading(float speedometerReading) {
		this.speedometerReading = speedometerReading;
	}
	
	public float getSpeedometerReadingMPH()
	{
		return this.speedometerReading;
	}
	
	public float getSpeedometerReadingKPH()
	{
		return this.ConvertToKilometers(this.speedometerReading);
	}
	
	public float getOdometerReading() {
		return odometerReading;
	}
	public void setOdometerReading(float odometerReading) {
		this.odometerReading = odometerReading;
	}
	
	public float getOdometerReadingMI()
	{
		return this.odometerReading;
	}
	
	public float getOdometerReadingKM()
	{
		return this.ConvertToKilometers(this.odometerReading);
	}
	
	public float getTachometer() {
		return tachometer;
	}
	public void setTachometer(float tachometer) {
		this.tachometer = tachometer;
	}
	
	public float getInstantFuelEconomy() {
		return instantFuelEconomy;
	}
	public void setInstantFuelEconomy(float instantFuelEconomy) {
		this.instantFuelEconomy = instantFuelEconomy;
	}
	
	public float getAverageFuelEconomy() {
		return averageFuelEconomy;
	}
	public void setAverageFuelEconomy(float averageFuelEconomy) {
		this.averageFuelEconomy = averageFuelEconomy;
	}
	
	public int getCruiseControlStatus() {
		return cruiseControlStatus;
	}
	public void setCruiseControlStatus(int cruiseControlStatus) {
		this.cruiseControlStatus = cruiseControlStatus;
	}
	
    public TransmissionRange getTransmissionRangeSelected()
    {
        return transmissionRangeSelected;
    }
	public void setTransmissionRangeSelected(TransmissionRange transmissionRange)
	{
		this.transmissionRangeSelected = transmissionRange;
	}

  public TransmissionRange getTransmissionRangeAttained()
  {
      return transmissionRangeAttained;
  }
	public void setTransmissionRangeAttained(TransmissionRange transmissionRange)
	{
		this.transmissionRangeAttained = transmissionRange;
	}
	
	public float getTotalFuelUsed() {
		return totalFuelUsed;
	}
	public void setTotalFuelUsed(float totalFuelUsed) {
		this.totalFuelUsed = totalFuelUsed;
	}
	
	public float getBrakePressure() {
		return brakePressure;
	}
	public void setBrakePressure(float brakePressure) {
		this.brakePressure = brakePressure;
	}
	
	public Date getGpsTimestampUtc() {
		return gpsTimestampUtc;
	}
	public void setGpsTimestampUtc(Date gpsTimestampUtc) {
		this.gpsTimestampUtc = gpsTimestampUtc;
	}
	
	public float getGpsLatitude() {
		return gpsLatitudeDeg;
	}
	public void setGpsLatitude(float gpsLatitudeDeg) {
		this.gpsLatitudeDeg = gpsLatitudeDeg;
	}
	
	public float getGpsLongitude() {
		return gpsLongitudeDeg;
	}
	public void setGpsLongitude(float gpsLongitudeDeg) {
		this.gpsLongitudeDeg = gpsLongitudeDeg;
	}
	
	public char getNorthSouthInd() {
		return northSouthInd;
	}
	public void setNorthSouthInd(char northSouthInd) {
		this.northSouthInd = northSouthInd;
	}
	
	public char getEastWestInd() {
		return eastWestInd;
	}
	public void setEastWestInd(char eastWestInd) {
		this.eastWestInd = eastWestInd;
	}
	
    /// <summary>
    /// GPS satellite Position Fix Indicator.  This indicator can be used
    /// as a confidence factor on the GPS location.
    /// Valid values are '0', '1', '2', and '6'.
    ///   0 - Invalid GPS (No satalite fix)
    ///   1 - Standard GPS Mode (valid satalite fix)
    ///   2 - Differential GPS Mode (valid satalite fix)
    ///   6 - Estimated Dead Reckoning  (valid satalite fix)
    /// </summary>
	public char getPosFixIndicator() {
		return posFixIndicator;
	}
	public void setPosFixIndicator(char posFixIndicator) {
		this.posFixIndicator = posFixIndicator;
	}
	
	public int getDiagnosticData_dwAddress() {
		return diagnosticData_dwAddress;
	}
	public void setDiagnosticData_dwAddress(int diagnosticData_dwAddress) {
		this.diagnosticData_dwAddress = diagnosticData_dwAddress;
	}
	
	public int getDiagnosticData_bReserved() {
		return diagnosticData_bReserved;
	}
	public void setDiagnosticData_bReserved(int diagnosticData_bReserved) {
		this.diagnosticData_bReserved = diagnosticData_bReserved;
	}
	
	public int getDiagnosticData_bReserved1() {
		return diagnosticData_bReserved1;
	}
	public void setDiagnosticData_bReserved1(int diagnosticData_bReserved1) {
		this.diagnosticData_bReserved1 = diagnosticData_bReserved1;
	}
	
	public boolean getIgnored() {
		return ignored;
	}
	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}
	
	public int getBlock() {
		return block;
	}
	public void setBlock(int block) {
		this.block = block;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getEntry() {
		return entry;
	}
	public void setEntry(int entry) {
		this.entry = entry;
	}

	/// GEN II Properties
	public int getDiagRecordId() {
		return diagRecId;
	}
	public void setDiagRecordId(int diagRecId) {
		this.diagRecId = diagRecId;
	}

	public int getTransmissionRange() {
		return transmissionRange;
	}
	public void setTransmissionRange(int transmissionRange) {
		this.transmissionRange = transmissionRange;
	}

	public boolean getPtoStatus() {
		return ptoStatus;
	}
	public void setPtoStatus(boolean ptoStatus) {
		this.ptoStatus = ptoStatus;
	}
    
	public int getCoolantPressure() {
		return coolantPressure;
	}
	public void setCoolantPressure(int coolantPressure) {
		this.coolantPressure = coolantPressure;
	}

	public int getCoolantLevel() {
		return coolantLevel;
	}
	public void setCoolantLevel(int coolantLevel) {
		this.coolantLevel = coolantLevel;
	}

	public int getCoolantTemp() {
		return coolantTemp;
	}
	public void setCoolantTemp(int coolantTemp) {
		this.coolantTemp = coolantTemp;
	}

	public int getTransmissionOilLevel() {
		return transmissionOilLevel;
	}
	public void setTransmissionOilLevel(int transmissionOilLevel) {
		this.transmissionOilLevel = transmissionOilLevel;
	}

	public int getTransmissionOilPressure() {
		return transmissionOilPressure;
	}
	public void setTransmissionOilPressure(int transmissionOilPressure) {
		this.transmissionOilPressure = transmissionOilPressure;
	}

	public int getThrottlePos() {
		return throttlePos;
	}
	public void setThrottlePos(int throttlePos) {
		this.throttlePos = throttlePos;
	}

	public int getEngineLoad() {
		return engineLoad;
	}
	public void setEngineLoad(int engineLoad) {
		this.engineLoad = engineLoad;
	}

	public int getEngineOilPressure() {
		return engineOilPressure;
	}
	public void setEngineOilPressure(int engineOilPressure) {
		this.engineOilPressure = engineOilPressure;
	}

	public int getEngineOilTemp() {
		return engineOilTemp;
	}
	public void setEngineOilTemp(int engineOilTemp) {
		this.engineOilTemp = engineOilTemp;
	}

	public int getEngineOilLevel() {
		return engineOilLevel;
	}
	public void setEngineOilLevel(int engineOilLevel) {
		this.engineOilLevel = engineOilLevel;
	}

	public boolean getParkBrakeState() {
		return parkBrakeState;
	}
	public void setParkBrakeState(boolean parkBrakeState) {
		this.parkBrakeState = parkBrakeState;
	}

	public boolean getMilStatus() {
		return milStatus;
	}
	public void setMilStatus(boolean milStatus) {
		this.milStatus = milStatus;
	}

	public int getActiveDTCs() {
		return activeDTCs;
	}
	public void setActiveDTCs(int activeDTCs) {
		this.activeDTCs = activeDTCs;
	}

	public int getPendingDTCs() {
		return pendingDTCs;
	}
	public void setPendingDTCs(int pendingDTCs) {
		this.pendingDTCs = pendingDTCs;
	}

	public int getEvalSup() {
		return evalSup;
	}
	public void setEvalSup(int evalSup) {
		this.evalSup = evalSup;
	}

	public int getAirFlow() {
		return airFlow;
	}
	public void setAirFlow(int airFlow) {
		this.airFlow = airFlow;
	}

	public int getManAbsPressure() {
		return manAbsPressure;
	}
	public void setManAbsPressure(int manAbsPressure) {
		this.manAbsPressure = manAbsPressure;
	}

	public int getIntakeAirTemp() {
		return intakeAirTemp;
	}
	public void setIntakeAirTemp(int intakeAirTemp) {
		this.intakeAirTemp = intakeAirTemp;
	}

	public float getGpsDOP() {
		return gpsDOP;
	}
	public void setGpsDOP(float gpsDOP) {
		this.gpsDOP = gpsDOP;
	}

	public int getGpsHeading() {
		return gpsHeading;
	}
	public void setGpsHeading(int gpsHeading) {
		this.gpsHeading = gpsHeading;
	}

	public float getGpsSpeed() {
		return gpsSpeed;
	}
	public void setGpsSpeed(float gpsSpeed) {
		this.gpsSpeed = gpsSpeed;
	}

	public float getGpsAltitude() {
		return gpsAltitude;
	}
	public void setGpsAltitude(float gpsAltitude) {
		this.gpsAltitude = gpsAltitude;
	}

    /**
     *
     * @return gpsUncertainty in meters.
     */
	public float getGpsUncertDistance() {
		return gpsUncertDistance;
	}
	public void setGpsUncertDistance(float gpsUncertDistance) {
		this.gpsUncertDistance = gpsUncertDistance;
	}

	public int[] getSpeedDetailMilli() {
		return this.speedDetailMilli;
	}
	public void setSpeedDetailMilli(int[] speeddetailmilli) {
		this.speedDetailMilli = speeddetailmilli;
	}
	
	public float[] getSpeedDetailSpeed() {
		return this.speedDetailSpeed;
	}
	public void setSpeedDetailSpeed(float[] speeddetailspeed) {
		this.speedDetailSpeed = speeddetailspeed;
	}

	public float getTripOdometer() {
		return tripOdometer;
	}

	public void setTripOdometer(float tripOdometer) {
		this.tripOdometer = tripOdometer;
	}

	public float getTripEngineSeconds() {
		return tripEngineSeconds;
	}

	public void setTripEngineSeconds(float tripEngineSeconds) {
		this.tripEngineSeconds = tripEngineSeconds;
	}

	/// <summary>
    /// Answer if the status record is empty
    /// </summary>
    /// <returns></returns>
    public boolean IsEmpty()
	{
        return timestampUtc == null;
    }

	/// <summary>
	/// This check builds on IsEmpty by also checking timestamp and odometer for some sentinal values which indicate we weren't able to successfully
	/// retrieve StatusRecord data from the ELD when calling EUCMD_GET_EOBR_DATA.
	/// </summary>
	/// <returns></returns>
    public boolean IsBlank()
	{
		return this.IsEmpty() || (timestampUtc.equals(new Date(0)) && odometerReading == 0);
	}

    /// <summary>
    /// Answer if any failures at all have been detected
    /// 9/24/2010 - TCH - do not identify device failure for JBus error (0x04), Internal 
    /// EOBR error (0x80) or combination of the two (0x84).  We see these errors in most
    /// every vehicle, yet all other data is valid (speed, odom, tach, gps).
    /// </summary>
    public boolean AnyDeviceFailuresDetected()
    {
        boolean isFailed = false;
        if (this.getOverallStatus() > 0)
        {
            switch (this.getOverallStatus())
            {
                case (int)DeviceErrorFlags.JBus:
                case (int)DeviceErrorFlags.InternalEOBR:
                case (int)DeviceErrorFlags.JBus + DeviceErrorFlags.InternalEOBR:
                case (int)DeviceErrorFlags.JbusDiagMemFull:
                    isFailed = false;
                    break;

                default:
                    isFailed = true;
                    break;
            }
        }
        return isFailed;
    }

    /// <summary>
    /// Answer if any failures have been detected that significantly effect
    /// the performance of the EOBR.
    /// Currently the only "optional" feature is the GPS.  If the GPS is the only
    /// device failure, then the system will not be adversely effected by this failure 
    /// and will return false from this method in that case.
    /// 9/24/2010 - TCH - do not identify device failure for JBus error (0x04), Internal 
    /// EOBR error (0x80) or combination of the two (0x84).  We see these errors in most
    /// every vehicle, yet all other data is valid (speed, odom, tach, gps).
    /// </summary>
    public boolean IsSignificantDeviceFailureDetected()
    {
        boolean isFailed = false;
        if (this.getOverallStatus() > 0)
        {
            switch (this.getOverallStatus())
            {
                case (int)DeviceErrorFlags.GPS:
                case (int)DeviceErrorFlags.JBus:
                case (int)DeviceErrorFlags.InternalEOBR:
                case (int)DeviceErrorFlags.JBus + (int)DeviceErrorFlags.InternalEOBR:
                case (int)DeviceErrorFlags.JbusDiagMemFull:
                    isFailed = false;
                    break;

                default:
                    isFailed = true;
                    break;
            }
        }
        return isFailed;
    }

    /// <summary>
    /// Answer if there has been a failure detected for a specific device.
    /// </summary>
    /// <param name="flag">specific device to check for</param>
    /// <returns>true if the specific device has failed, false otherwise</returns>
    public boolean IsFailureDetected(int flag)
    {
        //DeviceErrorFlags status = (DeviceErrorFlags)this.getOverallStatus();
        int status = this.getOverallStatus();

        boolean isFailed = false;

        if ((status & flag) > 0)
        {
            isFailed = true;
        }

        return isFailed;
    }
   
    /// <summary>
    /// Answer if the engine is running.
    /// This is a read-only property tied to a valid tachometer reading.
    /// If the tachometer has a non-zero value, then this means that the engine is running.
    /// </summary>
    public boolean getIsEngineRunning()
    {
        return this.getTachometer() > 0;
    }

    /// <summary>
    /// Answer if the vehicle is moving.
    /// This is a read-only property tied to a valid speedometer reading.
    /// If the speedometer has a non-zero value, then this means that the vehicle is moving.
    /// </summary>
    public boolean getIsVehicleMoving()
    {
        return this.getSpeedometerReading() > 0;
    }

    /// <summary>
    /// Answer if the GPS location info in the status record is valid.
    /// Valid GPS data means that there is no failure reported by the GPS module,
    /// a valid GPS timestamp and good satellite fix.
    /// </summary>
    /// <returns></returns>
    public boolean IsGpsLocationValid()
    {
        boolean isvalid = false;

        // note: SJN 08/18/08 Issue with the EOBR Firmware 0.4.A where
        //       the PosFixIndicator mistakely reads 0, so as a workaround
        //       remove ths PosFixIndicator check
        //if (!this.IsFailureDetected(DeviceErrorFlags.GPS) && 
        //    this.GpsTimeStampUtc != DateTime.MinValue && 
        //    this.PosFixIndicator != '0')

        if (!this.IsFailureDetected(DeviceErrorFlags.GPS) &&
            this.getGpsTimestampUtc() != null)
        {
            // validate that the LAT/LONG values are within the acceptable range
            float lat = this.getGpsLatitude();
            float lng = this.getGpsLongitude();
            if (lat > MIN_GPS_LATITUDE &&
                lat < MAX_GPS_LATITUDE &&
                lng > MIN_GPS_LONGITUDE &&
                lng < MAX_GPS_LONGITUDE)
            {
                isvalid = true;
            }
            else
            {
                isvalid = false;
            }
        }
        
        if(!isvalid)
        	GlobalState.getInstance().setLastGPSLocation(null);
        
        return isvalid;
    }

    /// <summary>
    /// Answer if the engine telemetry is available.
    /// This is a read-only property tied to valid J-Bus data coming through.
    /// If the speedomter/odometer/tachometer each have a value less than zero, 
    /// it means that the J-bus is not feeding data to the EOBR.   
    /// If the J-bus is not feeding data, then this implies that engine 
    /// telemetry is not available.
    /// </summary>
    public boolean getIsEngineTelemetryAvailable()
    {
        return this.getOdometerReading() >= 0 && 
               this.getSpeedometerReading() >= 0 && 
               this.getTachometer() >= 0; 
    }

    /// <summary>
    /// Outputs a string containing the formatted header information that 
    /// cooresponds to the data elements in the ToString method.
    /// </summary>
    /// <returns>String containing the header information.</returns>
    public static String StringHeader()
    {
        StringBuilder header = new StringBuilder();

        header.append("B:P:E\t");
        header.append("RecID\t");
        header.append("TimeStampUTC\t\t");
        header.append("Ignore\t");
        header.append("Status\t\t");
        header.append("EngOn\t");
        header.append("Speed\t");
        header.append("Odom\t");
        header.append("EngRPM\t");
        header.append("InstFuelEcon\t");
        header.append("AvgFuelEcon\t");
        header.append("Cruise\t");
        header.append("TransRangeS\t");
        header.append("TransRangeA\t");
        header.append("TotalFuelUsed\t");
        header.append("BrakePressure\t");
        header.append("GPSTimestamp\t\t");
        header.append("GPSLat ");
        header.append("GPSLong\t");
        header.append("GPSInd\t");
        header.append("Diag_dwAddress ");
        header.append("Diag_bReserved");

        return header.toString();
    }

    /// <summary>
    /// Convert the miles value into kilometers.
    /// </summary>
    /// <param name="miles">miles to convert</param>
    /// <returns>kilometers</returns>
    private float ConvertToKilometers(float miles)
    {
        float km = (float)Math.round(miles * KM_PER_MILE * 10000) / 10000;
        return km;
    }

    public class JbusDiagRecord
    {
	    private int jbusDiagnosticID = 0;
		public int getJbusDiagnosticID() {
			return jbusDiagnosticID;
		}
		public void setJbusDiagnosticID(int jbusDiagnosticID) {
			this.jbusDiagnosticID = jbusDiagnosticID;
		}
	    
		private Date timestampUtcInitial = null;
		public Date getTimestampUtcInitial() {
			return timestampUtcInitial;
		}
		public void setTimestampUtcInitial(Date timestampUtcInitial) {
			this.timestampUtcInitial = timestampUtcInitial;
		}
		
		private Date timestampUtcCurrent = null;
		public Date getTimestampUtcCurrent() {
			return timestampUtcCurrent;
		}
		public void setTimestampUtcCurrent(Date timestampUtcCurrent) {
			this.timestampUtcCurrent = timestampUtcCurrent;
		}
		
		private int activeBusType = (int)DatabusTypeEnum.NULL;
		public int getCurrentActiveBusType() {
			return activeBusType;
		}
		public void setCurrentActiveBusType(int activeBusType) {
			this.activeBusType = activeBusType;
		}
		
		private int diagErrorParam = 0;
		public int getDiagErrorParam() {
			return diagErrorParam;
		}
		public void setDiagErrorParam(int diagErrorParam) {
			this.diagErrorParam = diagErrorParam;
		}
		
		private byte fmi = 0;
		public byte getFmi() {
			return fmi;
		}
		public void setFmi(byte fmi) {
			this.fmi = fmi;
		}
		
		private byte occurenceCount = 0;
		public byte getOccurenceCount() {
			return occurenceCount;
		}
		public void setOccurenceCount(byte occurenceCount) {
			this.occurenceCount = occurenceCount;
		}
		
		private int lampStatus = 0;
		public int getLampStatus() {
			return lampStatus;
		}
		public void setLampStatus(int lampStatus) {
			this.lampStatus = lampStatus;
		}		
    }
    
    public class JbusDeviceInfo
    {
    	private byte activeBusType = DatabusTypeEnum.NULL;
		public byte getCurrentActiveBusType() {
			return activeBusType;
		}
		public void setCurrentActiveBusType(byte activeBusType) {
			this.activeBusType = activeBusType;
		}
    	
		private String make = null;
		public String getMake() {
			return make;
		}
		public void setMake(String make) {
			this.make = make;
		}
		
		private String model = null;
		public String getModel() {
			return model;
		}
		public void setModel(String model) {
			this.model = model;
		}
		
		private String serialNum = null;
		public String getSerialNumber() {
			return serialNum;
		}
		public void setSerialNumber(String serialNum) {
			this.serialNum = serialNum;
		}
		
		private short sourceDev = 0;
		public short getSourceDev() {
			return sourceDev;
		}
		public void setSourceDev(short sourceDev) {
			this.sourceDev = sourceDev;
		}		
    }
    
    public class TransmissionRange
    {
    	private static final char MIN_ASCII_VALUE = (char)0;
    	private static final char MAX_ASCII_VALUE = (char)127;
    	private static final char ASCII_SPACE = (char)32;
    	private static final char TRANS_ERROR_VALUE = (char)255;
    	private static final char ASCII_ERROR_VAL_1 = (char)69;
    	private static final char ASCII_ERROR_VAL_2 = (char)82;
    	
    	protected char firstChar;
    	protected char secondChar;
    	
    	public char getFirstChar()
    	{
    		return firstChar;
    	}
    	
    	public char getSecondChar()
    	{
    		return secondChar;
    	}
    	
        /// <summary>
        /// Construct the transmission range object with the specified first 
        /// and second characters.
        /// From J1587 Spec:
        /// Range selected by the operator. Characters may include P, R2, R1, R, N,
        /// D, D1, D2, L, L1, L2, 1, 2, 3, ... If only one displayable character is 
        /// required (ASCII 32 to 127), the second character shall be used and the 
        /// first character shall be either a space (ASCII 32) or a control character
        /// (ASCII 0 to 31). If the first character is a control character, refer to 
        /// the manufacturer's application document for definition
        /// </summary>
        /// <param name="first">First transmission character (ASCII 0 - 127)</param>
        /// <param name="second">Second transmission character (ASCII 32 - 127)</param>
        public TransmissionRange(char first, char second)
        {
            // check for error condition first
            if (first == TRANS_ERROR_VALUE && second == TRANS_ERROR_VALUE)
            {
                this.firstChar = ASCII_ERROR_VAL_1;
                this.secondChar = ASCII_ERROR_VAL_2;
            }
            else
            {
                if (first >= MIN_ASCII_VALUE && first <= MAX_ASCII_VALUE)
                    this.firstChar = first;
                else
                    this.firstChar = ASCII_SPACE;

                if (second >= ASCII_SPACE && second <= MAX_ASCII_VALUE)
                    this.secondChar = second;
                else
                    this.secondChar = ASCII_SPACE;
            }
        }
    	
        /// <summary>
        /// Constructor that takes the first and second character parameters as bytes
        /// to allow for passing ASCII characters from unmanaged code.
        /// </summary>
        /// <param name="first">byte representing the first character.</param>
        /// <param name="second">byte representing the second character.</param>
        public TransmissionRange(byte first, byte second)
        {
        	this((char)first, (char)second);
        }

        /// <summary>
        /// Returns a string representation of the transmission range.
        /// </summary>
        /// <returns>firstchar followed by second char.</returns>
        public String ToString()
        {
            if (firstChar < ASCII_SPACE)
                return String.valueOf(secondChar);
            else
                return String.valueOf(firstChar) + String.valueOf(secondChar);
        }        
    }        
}
