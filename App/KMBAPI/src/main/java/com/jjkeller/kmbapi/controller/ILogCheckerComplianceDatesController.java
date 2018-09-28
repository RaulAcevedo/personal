package com.jjkeller.kmbapi.controller;

import java.util.Date;

public interface ILogCheckerComplianceDatesController {

	public boolean IsLogCheckerComplianceDateActive(int complianceDatesType, Date dateToCheck, boolean dateRangeDefinesActivePeriod);
	
}
