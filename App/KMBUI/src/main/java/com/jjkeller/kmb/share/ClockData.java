package com.jjkeller.kmb.share;

import android.content.Context;

import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

/**
 * A helper class to keep track of data for a clock
 */
public class ClockData
{
	private long millisecondsAllowed = 0L;
	private long millisecondsAvailable = 0L;

    public ClockData(){}

    public ClockData(DutySummary dutySummary){
        if (dutySummary != null) {
            millisecondsAllowed = dutySummary.getAllowedHours() * DateUtility.MILLISECONDS_PER_HOUR;
            millisecondsAvailable = dutySummary.getAvailableMilliseconds();
        }
    }

	public double getPercentage()
	{
		if (millisecondsAllowed <= 0L)
			return 0.0;

		return (double) getMillisecondsAvailable() / (double) millisecondsAllowed;
	}

	public long getMillisecondsAvailable()
	{
		if (millisecondsAvailable < 0L)
			millisecondsAvailable = 0L;
		return millisecondsAvailable;
	}

	public void decreaseAvailable(long millis)
	{
		millisecondsAvailable = Math.max(0L, millisecondsAvailable - millis);
	}
}
