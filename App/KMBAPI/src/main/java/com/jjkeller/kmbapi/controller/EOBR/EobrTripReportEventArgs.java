package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.kmbeobr.TripReport;

public class EobrTripReportEventArgs
{
	private TripReport tripReport;
	private float speedThreshold;
	private int rpmThreshold;
	
	public EobrTripReportEventArgs()
	{
		setTripReport(null);
	}
	
	public EobrTripReportEventArgs(TripReport tripReport, float speedThreshold, int rpmThreshold)
	{
		setTripReport(tripReport);
		setSpeedThreshold(speedThreshold);
		setRpmThreshold(rpmThreshold);
	}
	
	public TripReport getTripReport()
	{
		return tripReport;
	}
	
	public void setTripReport(TripReport tripReport)
	{
		this.tripReport = tripReport;
	}

	public float getSpeedThreshold()
	{
		return speedThreshold;
	}

	public void setSpeedThreshold(float speedThreshold)
	{
		this.speedThreshold = speedThreshold;
	}

	public int getRpmThreshold()
	{
		return rpmThreshold;
	}

	public void setRpmThreshold(int rpmThreshold)
	{
		this.rpmThreshold = rpmThreshold;
	}
}
