package com.jjkeller.kmbapi.kmbeobr;

public class EobrReferenceTimestamps {
	public static final long REFERENCE_TIMESTAMP_DO_NOT_SET = 0xFFFFFFFFFFFFFFFFl;
	
	private long eobrReferenceTime;
	private long eventReferenceTime;
	private long tripReferenceTime;
	private long histogramReferenceTime;
	private long dtcReferenceTime;
	
	public long getEobrReferenceTime() {
		return eobrReferenceTime;
	}
	public void setEobrReferenceTime(long eobrReferenceTime) {
		this.eobrReferenceTime = eobrReferenceTime;
	}
	
	public long getEventReferenceTime() {
		return eventReferenceTime;
	}
	public void setEventReferenceTime(long eventReferenceTime) {
		this.eventReferenceTime = eventReferenceTime;
	}
	
	public long getTripReferenceTime() {
		return tripReferenceTime;
	}
	public void setTripReferenceTime(long tripReferenceTime) {
		this.tripReferenceTime = tripReferenceTime;
	}
	
	public long getHistogramReferenceTime() {
		return histogramReferenceTime;
	}
	public void setHistogramReferenceTime(long histogramReferenceTime) {
		this.histogramReferenceTime = histogramReferenceTime;
	}
	
	public long getDtcReferenceTime() {
		return dtcReferenceTime;
	}
	public void setDtcReferenceTime(long dtcReferenceTime) {
		this.dtcReferenceTime = dtcReferenceTime;
	}

	public void updateTimestampsFrom(EobrReferenceTimestamps timestamps) {
		this.setEobrReferenceTime(
				chooseTimestamp(
						timestamps.getEobrReferenceTime(),
						this.getEobrReferenceTime()
				)
		);

		this.setEventReferenceTime(
				chooseTimestamp(
						timestamps.getEventReferenceTime(),
						this.getEventReferenceTime()
				)
		);

		this.setTripReferenceTime(
				chooseTimestamp(
						timestamps.getTripReferenceTime(),
						this.getTripReferenceTime()
				)
		);

		this.setHistogramReferenceTime(
				chooseTimestamp(
						timestamps.getHistogramReferenceTime(),
						this.getHistogramReferenceTime()
				)
		);

		this.setDtcReferenceTime(
				chooseTimestamp(
						timestamps.getDtcReferenceTime(),
						this.getDtcReferenceTime()
				)
		);
	}

	private long chooseTimestamp(long newTimestamp, long currentTimestamp) {
		if(newTimestamp == REFERENCE_TIMESTAMP_DO_NOT_SET)
			return currentTimestamp;

		return newTimestamp;
	}

}
