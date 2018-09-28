package com.jjkeller.kmbapi.configuration;

import android.text.format.DateFormat;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.proxydata.ProxyBase;

import java.util.Date;

public class LoginCredentials extends ProxyBase{

	private String username;
	private String password;
	private String employeeId;
	private String employeeCode;
	private String employeeFullName;
	private Date lastLoginTimestampUtc;
	private Date lastLogoutTimestampUtc = null;
	private Date lastSubmitTimestampUtc = null;
	private String homeTerminalDOTNumber;
	private String homeTerminalAddressLine1;
	private String homeTerminalAddressLine2;
	private String homeTerminalCity;
	private String homeTerminalStateAbbrev;
	private String homeTerminalZipCode;
	private int driverIdCrc;
	private String driverLicenseState;
	private String driverLicenseNumber;
	private String lastName;
	private String firstName;
	
	public LoginCredentials(){}
	
	public LoginCredentials(String username, String password, String employeeId, String employeeCode, String employeeFullName, String homeTerminalDOTNumber, 
			String homeTerminalAddressLine1, String homeTerminalAddressLine2, String homeTerminalCity, String homeTerminalStateAbbrev, String homeTerminalZipCode,
							String driverLicenseState, String driverLicenseNumber, String lastName, String firstName)
	{
		this.username = username;
		this.password = password;
		this.employeeId = employeeId;
		this.employeeCode = employeeCode;
		this.employeeFullName = employeeFullName;
		this.lastLoginTimestampUtc = (Date) DateFormat.format("MM/dd/yyyy hh:mm:ss", TimeKeeper.getInstance().now());
		this.homeTerminalDOTNumber = homeTerminalDOTNumber;
		this.homeTerminalAddressLine1 = homeTerminalAddressLine1;
		this.homeTerminalAddressLine2 = homeTerminalAddressLine2;
		this.homeTerminalCity = homeTerminalCity;
		this.homeTerminalStateAbbrev = homeTerminalStateAbbrev;
		this.homeTerminalZipCode = homeTerminalZipCode;
		this.driverIdCrc = -1;
		this.driverLicenseNumber = driverLicenseNumber;
		this.driverLicenseState = driverLicenseState;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getUsername(){
		return this.username == null ? null : this.username.toLowerCase();
	}
	public void setUsername(String username){
		this.username = username == null ? null : username.toLowerCase();
	}

	public String getPassword(){
		return this.password;
	}
	public void setPassword(String password){
		this.password = password;
	}

	public String getEmployeeId(){
		return this.employeeId;
	}
	public void setEmployeeId(String employeeId){
		this.employeeId = employeeId;
	}
	
	public String getEmployeeCode(){
		return this.employeeCode;
	}
	public void setEmployeeCode(String employeeCode){
		this.employeeCode = employeeCode;
	}

	public String getEmployeeFullName(){
		return this.employeeFullName;
	}
	public void setEmployeeFullName(String employeeFullName){
		this.employeeFullName = employeeFullName;
	}

	public Date getLastLoginTimestampUtc(){
		return this.lastLoginTimestampUtc;
	}
	public void setLastLoginTimestampUtc(Date lastLoginTimestampUtc){
		this.lastLoginTimestampUtc = lastLoginTimestampUtc;
	}

	public Date getLastLogoutTimestampUtc(){
		return this.lastLogoutTimestampUtc;
	}
	public void setLastLogoutTimestampUtc(Date lastLogoutTimestampUtc){
		this.lastLogoutTimestampUtc = lastLogoutTimestampUtc;
	}
	
	public Date getLastSubmitTimestampUtc(){
		return this.lastSubmitTimestampUtc;
	}
	public void setLastSubmitTimestampUtc(Date lastSubmitTimestampUtc){
		this.lastSubmitTimestampUtc = lastSubmitTimestampUtc;
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
	
	public int getDriverIdCrc(){
		return this.driverIdCrc;
	}
	public void setDriverIdCrc(int driverIdCrc){
		this.driverIdCrc = driverIdCrc;
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
	
	public String toString(){
		return this.employeeFullName;
	}
	
	// 10/01/12 AMO - remove all SOAP
    /*public Object getProperty(int index) {
        switch (index) {
        case 0: 
        	return username;
        case 1:
        	return password;
        case 2: 
        	return employeeId;
        case 3:
            return employeeCode;
        case 4:
            return employeeCode;
        case 5:
            return lastLoginTimestampUtc;
        case 6:
            return lastLogoutTimestampUtc;
        case 7:
            return lastSubmitTimestampUtc;            
        default:
            return null;
        }
	}
	
    public int getPropertyCount() {
		return 7;
	}
	
    @SuppressWarnings("rawtypes")
	public void getPropertyInfo(int index, Hashtable properties, PropertyInfo info) {
        switch (index)
        {
	        case 0:
	            info.type = PropertyInfo.STRING_CLASS;
	            info.name = "Username";
	            info.namespace = ELEMENTNS;
	            break;
	        case 1:
	            info.type = PropertyInfo.STRING_CLASS;
	            info.name = "Password";
	            break;
	        case 2:
	            info.type = PropertyInfo.STRING_CLASS;
	            info.name = "EmployeeId";
	            break;
	        case 3:
	            info.type = PropertyInfo.STRING_CLASS;
	            info.name = "EmployeeCode";
	            break;
	        case 4:
	            info.type = PropertyInfo.STRING_CLASS;
	            info.name = "EmployeeFullName";
	            break;
	       default:
	            break;
        }
    }

	public void setProperty(int index, Object value) {
		
	}*/
}
