package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.EmployeeLogRevisionTypeEnum;

import java.util.Date;

public class EmployeeLogRevision extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String employeeCode;
	private Date employeeLogDate; 
	private EmployeeLogRevisionTypeEnum revisionType = new EmployeeLogRevisionTypeEnum(EmployeeLogRevisionTypeEnum.NONE);
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getEmployeeCode() {
		return employeeCode;
	}
	
	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}
	
	public Date getEmployeeLogDate() {
		return employeeLogDate;
	}
	
	public void setEmployeeLogDate(Date employeeLogDate) {
		this.employeeLogDate = employeeLogDate;
	}
	
	public EmployeeLogRevisionTypeEnum getRevisionType() {
		return revisionType;
	}
	public void setRevisionType(EmployeeLogRevisionTypeEnum revisionType) {
		this.revisionType = revisionType;
	}

}
