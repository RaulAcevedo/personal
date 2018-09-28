package com.jjkeller.kmbapi.proxydata;


import java.util.Date;

public class AuthenticationInfo extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private boolean isAuthenticated;
	private String employeeId;
	private String employeeFullName;
	private String employeeCode;
	private boolean requiredToChangePassword;
	private boolean isClientAppVersionCurrent;
	private EmployeeRule employeeRule;
	private Date clockSyncTimestamp;
	private String homeTerminalDOTNumber;
	private String homeTerminalAddressLine1;
	private String homeTerminalAddressLine2;
	private String homeTerminalCity;
	private String homeTerminalStateAbbrev;
	private String homeTerminalZipCode;
	private String driverLicenseState;
	private String driverLicenseNumber;
	private String lastName;
	private String firstName;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public boolean getIsAuthenticated(){
		return this.isAuthenticated;
	}
	public void setIsAuthenticated(boolean isAuthenticated){
		this.isAuthenticated = isAuthenticated;
	}
	
	public String getEmployeeId(){
		return this.employeeId;
	}
	public void setEmployeeId(String employeeId){
		this.employeeId = employeeId;
	}

	public String getEmployeeFullName(){
		return this.employeeFullName;
	}
	public void setEmployeeFullName(String employeeFullName){
		this.employeeFullName = employeeFullName;
	}

	public String getEmployeeCode(){
		return this.employeeCode;
	}
	public void setEmployeeCode(String employeeCode){
		this.employeeCode = employeeCode;
	}

	public boolean getRequiredToChangePassword(){
		return this.requiredToChangePassword;
	}
	public void setRequiredToChangePassword(boolean requiredToChangePassword){
		this.requiredToChangePassword = requiredToChangePassword;
	}

	public boolean getIsClientAppVersionCurrent(){
		return this.isClientAppVersionCurrent;
	}
	public void setIsClientAppVersionCurrent(boolean isClientAppVersionCurrent){
		this.isClientAppVersionCurrent = isClientAppVersionCurrent;
	}

	public EmployeeRule getEmployeeRule(){
		return this.employeeRule;
	}
	public void setEmployeeRule(EmployeeRule employeeRule){
		this.employeeRule = employeeRule;
	}
	
	public Date getClockSyncTimestamp(){
		return this.clockSyncTimestamp;
	}
	public void setClockSyncTimestamp(Date clockSyncTimestamp){
		this.clockSyncTimestamp = clockSyncTimestamp;
	}
	
	public String getHomeTerminalDOTNumber(){
		return this.homeTerminalDOTNumber;
	}
	public void setHomeTerminalDOTNumber(String homeTerminalDOTNumber){
		this.homeTerminalDOTNumber = homeTerminalDOTNumber;
	}
	
	public String getHomeTerminalAddressLine1(){
		return this.homeTerminalAddressLine1;
	}
	public void setHomeTerminalAddressLine1(String homeTerminalAddressLine1){
		this.homeTerminalAddressLine1 = homeTerminalAddressLine1;
	}
	
	public String getHomeTerminalAddressLine2(){
		return this.homeTerminalAddressLine2;
	}
	public void setHomeTerminalAddressLine2(String homeTerminalAddressLine2){
		this.homeTerminalAddressLine2 = homeTerminalAddressLine2;
	}
	
	public String getHomeTerminalCity(){
		return this.homeTerminalCity;
	}
	public void setHomeTerminalCity(String homeTerminalCity){
		this.homeTerminalCity = homeTerminalCity;
	}
	
	public String getHomeTerminalStateAbbrev(){
		return this.homeTerminalStateAbbrev;
	}
	public void setHomeTerminalStateAbbrev(String homeTerminalStateAbbrev){
		this.homeTerminalStateAbbrev = homeTerminalStateAbbrev;
	}
	
	public String getHomeTerminalZipCode(){
		return this.homeTerminalZipCode;
	}
	public void setHomeTerminalZipCode(String homeTerminalZipCode){
		this.homeTerminalZipCode = homeTerminalZipCode;
	}

	public String getDriverLicenseState()
	{
		return this.driverLicenseState;
	}
	public void setDriverLicenseState(String driverLicenseState)
	{
		this.driverLicenseState = driverLicenseState;
	}

	public String getDriverLicenseNumber()
	{
		return this.driverLicenseNumber;
	}
	public void setDriverLicenseNumber(String driverLicenseNumber)
	{
		this.driverLicenseNumber = driverLicenseNumber;
	}

	public String getLastName()
	{
		return this.lastName;
	}
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFirstName()
	{
		return this.firstName;
	}
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
