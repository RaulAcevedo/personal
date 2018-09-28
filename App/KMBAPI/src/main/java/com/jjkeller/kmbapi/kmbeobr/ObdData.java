package com.jjkeller.kmbapi.kmbeobr;

public class ObdData {
	public long timeCode;
	public byte ignition;		// ignition state
	public float vehicleSpeed;
	public short engineSpeed;	// RPM
	public float odometer;
	public float avgFuelRate;
	public byte cruiseConStat;
	public short transRange;	// transmission range
	public float totalFuelUsed;
	public short brakePress;
	public float instFuelRate;
	public byte ptoStatus;
	public short coolantPress;
	public byte coolantLevel;
	public byte coolantTemp;
	public byte transOilLevel;
	public short transOilPress;
	public byte throttlePos;
	public byte engineLoad;
	public short engineOilPress;
	public short engineOilTemp;
	public byte engineOilLevel;
	public byte parkBrakeState;
	public byte mil;			// Malfunction Indicator Light status
	public byte activeDTCs;		// number of active DTCs
	public byte pendingDTCs;	// number of pending/inactive DTCs
	public int evalSup;
	public short airFlow; // Mass air flow (gm/s*100)
	public byte manAbsPress; // Manifold abs pressure (kPa)
	public byte IntakeAirTemp; // Intake air temperature (degC + 40)
	public int[] speedDetailMilli = new int[14];
	public float[] speedDetailSpeed = new float[14];
	
	public long getTimeCode() {
		return this.timeCode;
	}
	public void setTimeCode(long timecode) {
		this.timeCode = timecode;
	}
	
	public byte getIgnition() {
		return this.ignition;
	}
	public void setIgnition(byte ignition) {
		this.ignition = ignition;
	}
	
	public float getVehicleSpeed() {
		return this.vehicleSpeed;
	}
	public void setVehicleSpeed(float vehiclespeed) {
		this.vehicleSpeed = vehiclespeed;
	}
	
	public short getEngineSpeed() {
		return this.engineSpeed;
	}
	public void setEngineSpeed(short enginespeed) {
		this.engineSpeed = enginespeed;
	}
	
	public float getOdometer() {
		return this.odometer;
	}
	public void setOdometer(float odometer) {
		this.odometer = odometer;
	}
	
	public float getAvgFuelRate() {
		return this.avgFuelRate;
	}
	public void setAvgFuelRate(float avgfuelrate) {
		this.avgFuelRate = avgfuelrate;
	}
	
	public byte getCruiseConStat() {
		return this.cruiseConStat;
	}
	public void setCruiseConStat(byte cruisecontrolstatus) {
		this.cruiseConStat = cruisecontrolstatus;
	}
	
	public short getTransRange() {
		return this.transRange;
	}
	public void setTransRange(short transrange) {
		this.transRange = transrange;
	}
	
	public float getTotalFueldUsed() {
		return this.totalFuelUsed;
	}
	public void setTotalFuelused(float totalfuelused) {
		this.totalFuelUsed = totalfuelused;
	}
	
	public short getBrakePress() {
		return this.brakePress;
	}
	public void setBrakePress(short brakepress) {
		this.brakePress = brakepress;
	}
	
	public float getInstFuelRate() {
		return this.instFuelRate;
	}
	public void setInstFuelRate(float instfuelrate) {
		this.instFuelRate = instfuelrate;
	}
	
	public byte getPTOStatus() {
		return this.ptoStatus;
	}
	public void setPTOStatus(byte ptostatus) {
		this.ptoStatus = ptostatus;
	}
	
	public short getCoolantPress() {
		return this.coolantPress;
	}
	public void setCoolanPress(short coolantpress) {
		this.coolantPress = coolantpress;
	}
	
	public byte getCoolantLevel() {
		return this.coolantLevel;
	}
	public void setCoolantLevel(byte coolantlevel) {
		this.coolantLevel = coolantlevel;
	}
	
	public byte getCoolantTemp() {
		return this.coolantTemp;
	}
	public void setCoolantTemp(byte coolanttemp) {
		this.coolantTemp = coolanttemp;
	}
	
	public byte getTransOilLevel() {
		return this.transOilLevel;
	}
	public void setTransOilLevel(byte transoillevel) {
		this.transOilLevel = transoillevel;
	}
	
	public short getTransOilPress() {
		return this.transOilPress;
	}
	public void setTransOilPress(short transoilpress) {
		this.transOilPress = transoilpress;
	}
	
	public byte getThrottlePos() {
		return this.throttlePos;
	}
	public void setThrottlePos(byte throttlepos) {
		this.throttlePos = throttlepos;
	}
	
	public byte getEngineLoad() {
		return this.engineLoad;
	}
	public void setEngineLoad(byte engineload) {
		this.engineLoad = engineload;
	}
	  
	public short getEngineOilPress() {
		return this.engineOilPress;
	}
	public void setEngineOilPress(short engineoilpress) {
		this.engineOilPress = engineoilpress;
	}
	
	public short getEngineOilTemp() {
		return this.engineOilTemp;
	}
	public void setEngineOilTemp(short engineoiltemp) {
		this.engineOilTemp = engineoiltemp;
	}
	
	public byte getEngineOilLevel() {
		return this.engineOilLevel;
	}
	public void setEngineOilLevel(byte engineoillevel) {
		this.engineOilLevel = engineoillevel;
	}
	
	public byte getParkBrakeState() {
		return this.parkBrakeState;
	}
	public void setParkBrakeStat(byte parkbrakestate) {
		this.parkBrakeState = parkbrakestate;
	}
	
	public byte getMIL() {
		return this.mil;
	}
	public void setMIL(byte mil) {
		this.mil = mil;
	}
	
	public byte getActiveDTCs() {
		return this.activeDTCs;
	}
	public void setActiveDTCs(byte activedtcs) {
		this.activeDTCs = activedtcs;
	}
	
	public byte getPendingDTCs() {
		return this.pendingDTCs;
	}
	public void setPendingDTCs(byte pendingdtcs) {
		this.pendingDTCs = pendingdtcs;
	}
	
	public int getEvalSup() {
		return this.evalSup;
	}
	public void setEvalSup(int evalsup) {
		this.evalSup = evalsup;
	}
	
	public short getAirFlow() {
		return this.airFlow;
	}
	public void setAirFlow(short airFlow) {
		this.airFlow = airFlow;
	}
	
	public byte getManAbsPress() {
		return this.manAbsPress;
	}
	public void setManAbsPress(byte manAbsPress) {
		this.manAbsPress = manAbsPress;
	}
	
	public byte getIntakeAirTemp() {
		return this.IntakeAirTemp;
	}
	public void setIntakeAirTemp(byte intakeAirTemp) {
		this.IntakeAirTemp = intakeAirTemp;
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
}