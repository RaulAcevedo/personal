package com.jjkeller.kmbapi.kmbeobr;

import java.util.Date;

public class HistogramData {
	private int recordId;
	private long timecode;
	private int histogramType;
	private int runTime;
	private int trips;
	private int onTime;
	private float odometer;
	private int driverid;
	private int lowLimit;
	private int highLimit;
	private int period;
	private int binSize;
	private int numBins;
	private int totalCounts;
	private int[] histogramData = new int[50];

	public int getRecordId() {
		return this.recordId;
	}
	public void setRecordId(int recordid) {
		this.recordId = recordid;
	}

	public long getTimecode() {
		return this.timecode;
	}
	public void setTimecode(long timecode) {
		this.timecode = timecode;
	}
	
	public Date getTimestamp() {
		return new Date(this.timecode);
	}
	public int getHistogramType() {
		return this.histogramType;
	}
	public void setHistogramType(int histogramType) {
		this.histogramType  = histogramType;
	}
	
	public int getRunTime() {
		return this.runTime;
	}
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	public int getTrips() {
		return this.trips;
	}
	public void setTrips(int trips) {
		this.trips = trips;
	}

	public int getOnTime() {
		return this.onTime;
	}
	public void setOnTime(int onTime) {
		this.onTime = onTime;
	}

	public float getOdometer() {
		return this.odometer;
	}
	public void setOdometer(float odometer) {
		this.odometer = odometer;
	}
	
	public int getDriverId() {
		return this.driverid;
	}
	public void setDriverId(int value) {
		this.driverid = value;
	}

	public int getLowLimit() {
		return this.lowLimit;
	}
	public void setLowLimit(int lowLimit) {
		this.lowLimit = lowLimit;
	}

	public int getHighLimit() {
		return this.highLimit;
	}
	public void setHighLimit(int highLimit) {
		this.highLimit = highLimit;
	}

	public int getPeriod() {
		return this.period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}

	public int getBinSize() {
		return this.binSize;
	}
	public void setBinSize(int binSize) {
		this.binSize = binSize;
	}

	public int getNumBins() {
		return this.numBins;
	}
	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}

	public int getTotalCounts() {
		return this.totalCounts;
	}
	public void setTotalCounts(int totalCounts) {
		this.totalCounts = totalCounts;
	}

	public int[] getHistogramData() {
		return this.histogramData;
	}
	public void setHistogramData(int[] histogramData) {
		this.histogramData = histogramData;
	}
}
