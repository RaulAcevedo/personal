package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

public class LogCheckerComplianceDates extends ProxyBase { 

	private Date complianceDate;
	private int itemEnum = 0;
	private String description;
	private Date complianceEndDate;
	
	public Date getComplianceDate() {
		return complianceDate;
	}

	public void setComplianceDate(Date complianceDate) {
		this.complianceDate = complianceDate;
	}

	public int getItemEnum() {
		return itemEnum;
	}

	public void setItemEnum(int itemEnum) {
		this.itemEnum = itemEnum;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getComplianceEndDate() {
		return complianceEndDate;
	}

	public void setComplianceEndDate(Date complianceEndDate) {
		this.complianceEndDate = complianceEndDate;
	}

}
