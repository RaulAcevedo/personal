package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import java.text.ParseException;
import java.util.Date;

public interface IExemptLogValidator {
	
	boolean IsExemptLogEligible(EmployeeLog employeeLog, EmployeeLogEldEventList currentLogEvents) throws ParseException;
	boolean IsExemptLogEligible(EmployeeLog employeeLog, EmployeeLogEldEventList currentLogEvents, Date endingTime) throws ParseException;
	Date DetermineValidationAuditStartDateFor(EmployeeLog employeeLog) throws ParseException; 
}
