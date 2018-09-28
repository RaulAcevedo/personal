package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

public class ExemptLogValidatorFactory {

	public static IExemptLogValidator GetExemptLogValidator(EmployeeLog log) {
		if(log == null) return null;
		
		switch(log.getExemptLogType().getValue()) {
			case ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE:
				return new Exempt100AirMileLogValidator();
			case ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL:
				return new Exempt150AirMileLogValidator();
		}
		
		return null;
	}
	
	public static IExemptLogValidator GetValidatorOrDefault(EmployeeLog log) { 
		if(log == null) return null;
		
		switch(log.getExemptLogType().getValue()) {
			case ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE:
				return new Exempt100AirMileLogValidator();
			case ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL:
				return new Exempt150AirMileLogValidator();
			default: 
				return new Exempt150AirMileLogValidator();
		}
	}
	
	public static IExemptLogValidator GetExemptLogValidator(ExemptLogTypeEnum exemptLogType) { 
		switch(exemptLogType.getValue()) {
		case ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE:
			return new Exempt100AirMileLogValidator();
		case ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL:
			return new Exempt150AirMileLogValidator();
	}
	
	return null;
	}
}
