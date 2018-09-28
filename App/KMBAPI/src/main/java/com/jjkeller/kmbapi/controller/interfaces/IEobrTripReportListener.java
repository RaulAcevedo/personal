package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.EOBR.EobrTripReportEventArgs;

public interface IEobrTripReportListener
{
	public void onTripReport(EobrTripReportEventArgs e);
}
