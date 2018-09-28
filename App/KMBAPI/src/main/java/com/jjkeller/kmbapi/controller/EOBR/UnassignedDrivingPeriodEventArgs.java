package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import java.util.List;

public class UnassignedDrivingPeriodEventArgs {
	private List<UnassignedDrivingPeriod> periods;
	private UnassignedDrivingPeriodResult result;
	
	public UnassignedDrivingPeriodEventArgs(List<UnassignedDrivingPeriod> periods, UnassignedDrivingPeriodResult result)
	{
		this.periods = periods;
		this.result = result;
	}

	public List<UnassignedDrivingPeriod> getPeriods() {
		return periods;
	}
	public void setPeriods(List<UnassignedDrivingPeriod> periods) {
		this.periods = periods;
	}

	public UnassignedDrivingPeriodResult getResult() {
		return result;
	}
	public void setResult(UnassignedDrivingPeriodResult result) {
		this.result = result;
	}

}
