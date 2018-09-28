package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class Eobr_Data {

    public int recordID;
    public EobrTimestamp timestamp;
    public byte engineUseIndicator;
    public int overallStatus;
    public byte activeBusType;
    public ODOMETER odometer;
    public float speedometer;
    public float instFuelEconomy;
    public float avgFuelEconomy;
    public char cruiseControlStatus;

/*
Bit 8: cruise mode 1=active/0=not active
Bit 7: clutch switch 1=on/0=off
Bit 6: brake switch 1=on/0=off
Bit 5: accel switch 1=on/0=off
Bit 4: resume switch 1=on/0=off
Bit 3: coast switch 1=on/0=off
Bit 2: set switch 1=on/0=off
Bit 1: cruise control switch 1=on/0=off
*/

    public TRANSMISSION_RANGE transmissionRangeSelected;
    public TRANSMISSION_RANGE transmissionRangeAttained;
    public float totalFuelUsed;
    public float brakePressure;
    public GPS_POSITION gpsPosition;
    public int diagnosticData_dwAddress;
    public byte diagnosticData_bReserved;
    public byte diagnosticData_bReserved1;


	public int getRecordID() {
		return recordID;
	}
	public void setRecordID(int recordID) {
		this.recordID = recordID;
	}

	public EobrTimestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(EobrTimestamp timestamp) {
		this.timestamp = timestamp;
	}

	public byte getEngineUseIndicator() {
		return engineUseIndicator;
	}
	public void setEngineUseIndicator(byte engineUseIndicator) {
		this.engineUseIndicator = engineUseIndicator;
	}

	public int getOverallStatus() {
		return overallStatus;
	}
	public void setOverallStatus(int overallStatus) {
		this.overallStatus = overallStatus;
	}

	public byte getActiveBusType() {
		return activeBusType;
	}
	public void setActiveBusType(byte activeBusType) {
		this.activeBusType = activeBusType;
	}

	public ODOMETER getOdometer() {
		return odometer;
	}
	public void setOdometer(ODOMETER odometer) {
		this.odometer = odometer;
	}

	public float getSpeedometer() {
		return speedometer;
	}
	public void setSpeedometer(float speedometer) {
		this.speedometer = speedometer;
	}

	public float getInstFuelEconomy() {
		return instFuelEconomy;
	}
	public void setInstFuelEconomy(float instFuelEconomy) {
		this.instFuelEconomy = instFuelEconomy;
	}

	public float getAvgFuelEconomy() {
		return avgFuelEconomy;
	}
	public void setAvgFuelEconomy(float avgFuelEconomy) {
		this.avgFuelEconomy = avgFuelEconomy;
	}

	public char getCruiseControlStatus() {
		return cruiseControlStatus;
	}
	public void setCruiseControlStatus(char cruiseControlStatus) {
		this.cruiseControlStatus = cruiseControlStatus;
	}

	public TRANSMISSION_RANGE getTransmissionRangeSelected() {
		return transmissionRangeSelected;
	}
	public void setTransmissionRangeSelected(
			TRANSMISSION_RANGE transmissionRangeSelected) {
		this.transmissionRangeSelected = transmissionRangeSelected;
	}

	public TRANSMISSION_RANGE getTransmissionRangeAttained() {
		return transmissionRangeAttained;
	}
	public void setTransmissionRangeAttained(
			TRANSMISSION_RANGE transmissionRangeAttained) {
		this.transmissionRangeAttained = transmissionRangeAttained;
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

	public GPS_POSITION getGpsPosition() {
		return gpsPosition;
	}
	public void setGpsPosition(GPS_POSITION gpsPosition) {
		this.gpsPosition = gpsPosition;
	}

	public int getDiagnosticData_dwAddress() {
		return diagnosticData_dwAddress;
	}
	public void setDiagnosticData_dwAddress(int diagnosticData_dwAddress) {
		this.diagnosticData_dwAddress = diagnosticData_dwAddress;
	}

	public byte getDiagnosticData_bReserved() {
		return diagnosticData_bReserved;
	}
	public void setDiagnosticData_bReserved(byte diagnosticData_bReserved) {
		this.diagnosticData_bReserved = diagnosticData_bReserved;
	}

	public byte getDiagnosticData_bReserved1() {
		return diagnosticData_bReserved1;
	}
	public void setDiagnosticData_bReserved1(byte diagnosticData_bReserved1) {
		this.diagnosticData_bReserved1 = diagnosticData_bReserved1;
	}

	public class ODOMETER
	{
		public float tachometer;
		public float totalTripDistance;

		public float getTachometer() {
			return tachometer;
		}
		public void setTachometer(float tachometer) {
			this.tachometer = tachometer;
		}
		
		public float getTotalTripDistance() {
			return totalTripDistance;
		}
		public void setTotalTripDistance(float totalTripDistance) {
			this.totalTripDistance = totalTripDistance;
		}
	}
	
	public class TRANSMISSION_RANGE
	{
		public char firstChar;
		public char secondChar;
		
		public char getFirstChar() {
			return firstChar;
		}
		public void setFirstChar(char firstChar) {
			this.firstChar = firstChar;
		}
		
		public char getSecondChar() {
			return secondChar;
		}
		public void setSecondChar(char secondChar) {
			this.secondChar = secondChar;
		}
	}
	
	public class GPS_POSITION
	{
		public EobrTimestamp gpsTimestamp;
		public float latitude;
		public char northSouthInd;
		public float longitude;
		public char eastWestInd;
		public char posFixIndicator;
		
		public EobrTimestamp getGpsTimestamp() {
			return gpsTimestamp;
		}
		public void setGpsTimestamp(EobrTimestamp gpsTimestamp) {
			this.gpsTimestamp = gpsTimestamp;
		}
		
		public float getLatitude() {
			return latitude;
		}
		public void setLatitude(float latitude) {
			this.latitude = latitude;
		}
		
		public char getNorthSouthInd() {
			return northSouthInd;
		}
		public void setNorthSouthInd(char northSouthInd) {
			this.northSouthInd = northSouthInd;
		}
		
		public float getLongitude() {
			return longitude;
		}
		public void setLongitude(float longitude) {
			this.longitude = longitude;
		}
		
		public char getEastWestInd() {
			return eastWestInd;
		}
		public void setEastWestInd(char eastWestInd) {
			this.eastWestInd = eastWestInd;
		}
		
		public char getPosFixIndicator() {
			return posFixIndicator;
		}
		public void setPosFixIndicator(char posFixIndicator) {
			this.posFixIndicator = posFixIndicator;
		}
	}
}

