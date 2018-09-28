package com.jjkeller.kmbapi.proxydata;



import com.jjkeller.kmbapi.enums.EobrCommunicationModeEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;

public class CompanyConfigSettings extends ProxyBase{
	
	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String dmoCompanyName;
	private String dmoCompanyId;
	private String dmoUsername;
	private String dmoPasswordEncrypt;
	
	private String carrierDOTNumber;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String stateAbbrev;
	private String zipCode;
	
	private String dailyLogStartTime;
	private Integer logPurgeDayCount = 10;
	private Boolean isActivated;
	private String eobrDiscoveryPasskey;
	private EobrCommunicationModeEnum eobrCommunicationMode = new EobrCommunicationModeEnum(EobrCommunicationModeEnum.USB_BT);;
	private Integer eobrSleepModeMinutes = -1;
	private Integer eobrDataCollectionRateSeconds = 1;
	private Boolean allowDriversCompleteDVIR = true;
	private Boolean generatePreTripDVIRWithDefectAlert = true;
	private String activationCode;
	private Float driverStartDistance = .5F;
	private Integer driverStopMinutes = 5;
	private Float maxAcceptableSpeed = 70F;
	private Integer maxAcceptableTach = 1800;
	private Float hardBrakeDecelerationSpeed = 7F;
	private Boolean useKmbWebApiServices = false;
	private Boolean multipleUsersAllowed = false;
	private Integer driveStartSpeed = 5;
	private Integer mandateDrivingStopTimeMinutes = 5;
	private Boolean isGeotabEnabled = false;
	private Boolean isMotionPictureEnabled = false;
	private boolean isAutoAssignUnIdentifiedEvents = true;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getDmoCompanyName(){
		return this.dmoCompanyName;
	}
	public void setDmoCompanyName(String dmoCompanyName){
		this.dmoCompanyName = dmoCompanyName;
	}
	
	public String getDmoCompanyId(){
		return dmoCompanyId;
	}
	public void setDmoCompanyId(String dmoCompanyId){
		this.dmoCompanyId = dmoCompanyId;
	}
	
	public String getDmoUsername(){
		return this.dmoUsername;
	}
	public void setDmoUsername(String dmoUsername){
		this.dmoUsername = dmoUsername;
	}

	public String getDmoPasswordEncrypt(){
		return this.dmoPasswordEncrypt;
	}
	public void setDmoPasswordEncrypt(String passwordEncrypt){
		this.dmoPasswordEncrypt = passwordEncrypt;
	}
	
	public String getCarrierDOTNumber(){
		return this.carrierDOTNumber;
	}
	public void setCarrierDOTNumber(String carrierDOTNumber){
		this.carrierDOTNumber = carrierDOTNumber;
	}
	
	public String getAddressLine1(){
		return this.addressLine1;
	}
	public void setAddressLine1(String addressLine1){
		this.addressLine1 = addressLine1;
	}
	
	public String getAddressLine2(){
		return this.addressLine2;
	}
	public void setAddressLine2(String addressLine2){
		this.addressLine2 = addressLine2;
	}
	
	public String getCity(){
		return this.city;
	}
	public void setCity(String city){
		this.city = city;
	}
	
	public String getStateAbbrev(){
		return this.stateAbbrev;
	}
	public void setStateAbbrev(String stateAbbrev){
		this.stateAbbrev = stateAbbrev;
	}
	
	public String getZipCode(){
		return this.zipCode;
	}
	public void setZipCode(String zipCode){
		this.zipCode = zipCode;
	}
	
	public String getDailyLogStartTime(){
		return this.dailyLogStartTime;
	}
	public void setDailyLogStartTime(String dailyLogStartTime){
		this.dailyLogStartTime = dailyLogStartTime;
	}

	public Integer getLogPurgeDayCount(){
		return logPurgeDayCount;
	}
	public void setLogPurgeDayCount(Integer logPurgeDayCount){
		this.logPurgeDayCount = logPurgeDayCount;
	}

	public Boolean getIsActivated(){
		return isActivated;
	}
	public void setIsActivated(Boolean isActivated){
		this.isActivated = isActivated;
	}

	public String getEobrDiscoveryPasskey(){
		return this.eobrDiscoveryPasskey;
	}
	public void setEobrDiscoveryPasskey(String eobrDiscoveryPasskey){
		this.eobrDiscoveryPasskey = eobrDiscoveryPasskey;
	}

	public EobrCommunicationModeEnum getEobrCommunicationMode(){
		return this.eobrCommunicationMode;
	}
	public void setEobrCommunicationMode(EobrCommunicationModeEnum eobrCommunicationMode){
		this.eobrCommunicationMode = eobrCommunicationMode;
	}

	public Integer getEobrSleepModeMinutes(){
		return eobrSleepModeMinutes;
	}
	public void setEobrSleepModeMinutes(Integer eobrSleepModeMinutes){
		this.eobrSleepModeMinutes = eobrSleepModeMinutes;
	}

	public Integer getEobrDataCollectionRateSeconds(){
		return eobrDataCollectionRateSeconds;
	}
	public void setEobrDataCollectionRateSeconds(Integer eobrDataCollectionRateSeconds){
		this.eobrDataCollectionRateSeconds = eobrDataCollectionRateSeconds;
	}
	
	public Boolean getAllowDriversCompleteDVIR(){
		return allowDriversCompleteDVIR;
	}
	public void setAllowDriversCompleteDVIR(Boolean value){
		this.allowDriversCompleteDVIR = value;
	}
	
	public Boolean getGeneratePreTripDVIRWithDefectAlert(){
		return generatePreTripDVIRWithDefectAlert;
	}
	public void setGeneratePreTripDVIRWithDefectAlert(Boolean value){
		this.generatePreTripDVIRWithDefectAlert = value;
	}
	
	public String getActivationCode(){
		return activationCode;
	}
	public void setActivationCode(String value){
		this.activationCode = value;
	}
	
	public Float getDriverStartDistance()
	{
		return this.driverStartDistance;
	}
	public void setDriverStartDistance(Float driverStartDistance)
	{
		this.driverStartDistance = driverStartDistance;
	}
	
	public Integer getDriverStopMinutes(){
		return driverStopMinutes;
	}
	public void setDriverStopMinutes(Integer driverStopMinutes){
		this.driverStopMinutes = driverStopMinutes;
	}
	
	public Float getMaxAcceptableSpeed()
	{
		return this.maxAcceptableSpeed;
	}
	public void setMaxAcceptableSpeed(Float maxAcceptableSpeed)
	{
		this.maxAcceptableSpeed = maxAcceptableSpeed;
	}	
	
	public Integer getMaxAcceptableTach()
	{
		return this.maxAcceptableTach;
	}
	public void setMaxAcceptableTach(Integer maxAcceptableTach)
	{
		this.maxAcceptableTach = maxAcceptableTach;
	}	
	
	public Float getHardBrakeDecelerationSpeed()
	{
		return this.hardBrakeDecelerationSpeed;
	}
	public void setHardBrakeDecelerationSpeed(Float hardBrakeDecelerationSpeed)
	{
		this.hardBrakeDecelerationSpeed = hardBrakeDecelerationSpeed;
	}	
	
	public Boolean getMultipleUsersAllowed(){
		return multipleUsersAllowed;
	}
	public void setMultipleUsersAllowed(Boolean value){
		this.multipleUsersAllowed = value;
	}

	public Boolean getUseKmbWebApiServices(){
		return useKmbWebApiServices;
	}
	public void setUseKmbWebApiServices(Boolean value){
		this.useKmbWebApiServices = value;
	}
	public int getDriveStartSpeed() {
		return driveStartSpeed;
	}
	public void setDriveStartSpeed(int driveStartSpeed) {
		this.driveStartSpeed = driveStartSpeed;
	}
	public int getMandateDrivingStopTimeMinutes() {
		return mandateDrivingStopTimeMinutes;
	}
	public void setMandateDrivingStopTimeMinutes(int mandateDrivingStopTimeMinutes) {
		this.mandateDrivingStopTimeMinutes = mandateDrivingStopTimeMinutes;
	}

	public boolean getIsGeotabEnabled() { return isGeotabEnabled; }
	public void setIsGeotabEnabled(boolean value) { this.isGeotabEnabled = value;}

	public boolean getIsMotionPictureEnabled() { return isMotionPictureEnabled; }
	public void setIsMotionPictureEnabled(boolean value) { this.isMotionPictureEnabled = value;}

	public boolean getIsAutoAssignUnIdentifiedEvents() { return isAutoAssignUnIdentifiedEvents; }
	public void setIsAutoAssignUnIdentifiedEvents(boolean value) { this.isAutoAssignUnIdentifiedEvents = value;}
	/**
	 * Turns the current CompanyConfigSettings into Thresholds
	 * NOTE: The driverEmployeeId will be an Empty String, and EventBlanking will be set to 10
	 * @param isMandateEnabled mandate toggle
	 * @return threasholds w/o driverEmployeeId
     */
	public Thresholds toThresholds(boolean isMandateEnabled) {
		int stopTime = isMandateEnabled ? getMandateDrivingStopTimeMinutes() : getDriverStopMinutes();

		Thresholds thresholds = new Thresholds(
				getMaxAcceptableTach(),
				getMaxAcceptableSpeed(),
				getHardBrakeDecelerationSpeed(),
				getDriverStartDistance(),
				getDriveStartSpeed(),
				stopTime,
				"", //Driver ID
				10 //event blanking
		);

		return thresholds;
	}
}
