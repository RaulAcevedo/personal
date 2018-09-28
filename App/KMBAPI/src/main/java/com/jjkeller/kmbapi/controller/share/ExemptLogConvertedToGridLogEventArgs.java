package com.jjkeller.kmbapi.controller.share;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;

public class ExemptLogConvertedToGridLogEventArgs {

	private EmployeeLog _log;
	private boolean _hasUserBeenNotified;
	
	public ExemptLogConvertedToGridLogEventArgs(EmployeeLog log, boolean hasUserBeenNotified)
	{
		_log = log;
		_hasUserBeenNotified = hasUserBeenNotified;
	}
	
	public EmployeeLog getLog() {
		return _log;
	}
	
	public boolean hasUserBeenNotified() {
		return _hasUserBeenNotified;
	}
	
}
