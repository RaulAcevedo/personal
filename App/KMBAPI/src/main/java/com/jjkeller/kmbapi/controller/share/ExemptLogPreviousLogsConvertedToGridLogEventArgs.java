package com.jjkeller.kmbapi.controller.share;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.util.Date;
import java.util.List;

public class ExemptLogPreviousLogsConvertedToGridLogEventArgs {

	private EmployeeLog _log; 
	private List<Date> _logDateList;
	private boolean _hasConvertedCurrentLog;
	
	public ExemptLogPreviousLogsConvertedToGridLogEventArgs(EmployeeLog log, List<Date> logDateList, boolean hasConvertedCurrentLog)
	{
		_log = log; 
		_logDateList = logDateList;
		_hasConvertedCurrentLog = hasConvertedCurrentLog;
	}
	
	public EmployeeLog getLog() {
		return _log; 
	}
	
	public List<Date> getLogDateList() {
		return _logDateList;
	}
	
	public boolean hasConvertedCurrentLog() {
		return _hasConvertedCurrentLog;
	}
	
}
