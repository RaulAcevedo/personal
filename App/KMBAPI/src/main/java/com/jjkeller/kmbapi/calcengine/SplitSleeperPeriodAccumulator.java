package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

// internal class - only accessible within the package
class SplitSleeperPeriodAccumulator implements Cloneable {

	private SplitSleeperCombination _parent;
	public Date _startTimestamp;
	public long _lengthCombinablePeriod;
	public long _driveTimeAccumulated;
	public long _dutyTimeAccumulated;
	public long _workShiftTimeAccumulated;
	public boolean _isFullSleeper;
	public boolean _isOffDutyWellsitePeriod;
	
	SplitSleeperPeriodAccumulator(SplitSleeperCombination parent)
	{
		this.setParent(parent);
		this.setDriveTimeAccumulated(0);
		this.setDutyTimeAccumulated(0);
		this.setLengthCombinablePeriod(0);
		this.setSTartTimestamp(null);
	}
	
	/// NOTE:  GET/SET methods that do not have an accessibility level defined
	/// are accessible only within the package (corresponds to C# internal -
	/// which is only accessible within the assembly)

	SplitSleeperCombination getParent()
	{
		return this._parent;
	}
	void setParent(SplitSleeperCombination parent)
	{
		this._parent = parent;		
	}
	
    /// <summary>
    /// Timestamp when the combinable period first occurs.
    /// </summary>
	public Date getStartTimestamp()
	{
		return this._startTimestamp;
	}
	void setSTartTimestamp(Date startTimestamp)
	{
		this._startTimestamp = startTimestamp;
	}
	
    /// <summary>
    /// The is the total length of the combinable period.
    /// A couple of duty status events may be chronologically consecutive and 
    /// this length represents the complete length across all eligible events.
    /// </summary>
	public long getLengthCombinablePeriod()
	{
		return this._lengthCombinablePeriod;
	}
	void setLengthCombinablePeriod(long lengthCombinablePeriod)
	{
		this._lengthCombinablePeriod = lengthCombinablePeriod;
	}

    /// <summary>
    /// Accumulated drive time for the combinable period
    /// </summary>
	public long getDriveTimeAccumulated()
	{
		return this._driveTimeAccumulated;
	}
	void setDriveTimeAccumulated(long driveTimeAccumulated)
	{
		this._driveTimeAccumulated = driveTimeAccumulated;
	}

    /// <summary>
    /// Accumulated duty time (both On-Duty and Driving) for the combinable period
    /// </summary>
	public long getDutyTimeAccumulated()
	{
		return this._dutyTimeAccumulated;
	}
	void setDutyTimeAccumulated(long dutyTimeAccumulated)
	{
		this._dutyTimeAccumulated = dutyTimeAccumulated;
	}

    /// <summary>
    /// Accumulated all duty and off-duty time for the combinable period
    /// </summary>
	public long getWorkShiftTimeAccumulated()
	{
		return this._workShiftTimeAccumulated;
	}
	void setWorkShiftTimeAccumulated(long workShiftTimeAccumulated)
	{
		this._workShiftTimeAccumulated = workShiftTimeAccumulated;
	}
	
    /// <summary>
    /// Indicator whether this combinable period is considered a Full Sleeper.
    /// In order for two periods to be combined, one period must be a Full Sleeper.
    /// </summary>
	public boolean getIsFullSleeper()
	{
		return this._isFullSleeper;
	}
	void setIsFullSleeper(boolean isFullSleeper)
	{
		this._isFullSleeper = isFullSleeper;
	}

    /// <summary>
    /// Indicator whether this combinable period is considered a Full Sleeper.
    /// In order for two periods to be combined, one period must be a Full Sleeper.
    /// </summary>
	public boolean getIsOffDutyWellsitePeriod()
	{
		return this._isOffDutyWellsitePeriod;
	}
	void setIsOffDutyWellsitePeriod(boolean isOffDutyWellsitePeriod)
	{
		this._isOffDutyWellsitePeriod = isOffDutyWellsitePeriod;
	}

	public Object Clone()
	{
		SplitSleeperPeriodAccumulator val = new SplitSleeperPeriodAccumulator(this.getParent());
		
		val.setDriveTimeAccumulated(this.getDriveTimeAccumulated());
		val.setDutyTimeAccumulated(this.getDutyTimeAccumulated());
		val.setLengthCombinablePeriod(this.getLengthCombinablePeriod());
		val.setSTartTimestamp(this.getStartTimestamp());
		val.setIsFullSleeper(this.getIsFullSleeper());
		val.setWorkShiftTimeAccumulated(this.getWorkShiftTimeAccumulated());
		val.setIsOffDutyWellsitePeriod(this.getIsOffDutyWellsitePeriod());
		
		return val;
	}
	
    /// <summary>
    /// Accumulate the On-Duty period's time 
    /// </summary>
	public void ProcessOnDutyTime(long length)
	{
		if (this.getLengthCombinablePeriod() > 0)
		{
            // a combinable period is being monitored, so accumulate the drive time
			this.setDutyTimeAccumulated(this.getDutyTimeAccumulated() + length);
			
            // note: workshift time only accumulates as an On-Duty because 
            // drive time is also sent through here
			this.setWorkShiftTimeAccumulated(this.getWorkShiftTimeAccumulated() + length);
		}
	}
	
    /// <summary>
    /// Accumulate the Driving period's time
    /// </summary>
	public void ProcessDriveTime(long length)
	{
		if (this.getLengthCombinablePeriod() > 0)
		{
            // a combinable period is being monitored, so accumulate the drive time
			this.setDriveTimeAccumulated(this.getDriveTimeAccumulated() + length);
		}
	}
	
    /// <summary>
    /// Accumulate the Off-Duty time as On-Duty time so long as the period is too short 
    /// to be treated as a combinable period.  This conversion will only be performed
    /// if the parent allows it.
    /// </summary>
	public void ProcessOffDutyTime(long length)
	{
		if (DateUtility.ConvertMillisecondsToHours(length) < this.getParent().MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS)
		{
			if (this.getParent().CONVERT_SHORT_OFFDUTY_TO_DUTY)
			{
				this.setDutyTimeAccumulated(this.getDutyTimeAccumulated() + length);
			}
		}
		// if off duty time counts against duty limit and current off duty period is less than a full break,
		// add to duty time
		else if (this.getParent().CONVERT_ANY_OFFDUTY_TO_DUTY && DateUtility.ConvertMillisecondsToHours(length) < this.getParent().FULL_BREAK_HOURS)
		{
			this.setDutyTimeAccumulated(this.getDutyTimeAccumulated() + length);
		}
		
		this.setWorkShiftTimeAccumulated(this.getWorkShiftTimeAccumulated() + length);
	}

    /// <summary>
    /// Accumulate the Sleeper time as On-Duty time so long as the period is too short 
    /// to be treated as a combinable period.  This conversion will only be performed
    /// if the parent allows it.
    /// </summary>
	public void ProcessSleeperTime(long length, boolean combinedWithOtherSleeper)
	{
		if (DateUtility.ConvertMillisecondsToHours(length) < this.getParent().MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS)
		{
			if (this.getParent().CONVERT_SHORT_OFFDUTY_TO_DUTY)
			{
				this.setDutyTimeAccumulated(this.getDutyTimeAccumulated() + length);
			}
			
			this.setWorkShiftTimeAccumulated(this.getWorkShiftTimeAccumulated() + length);
		}
		else if (this.getParent().COUNT_UNCOMBINED_SLEEPERS_AS_DUTY && !combinedWithOtherSleeper)			
		{
			this.setDutyTimeAccumulated(this.getDutyTimeAccumulated() + length);
		}
	}
	
	public boolean CanCombineForFullBreak(SplitSleeperPeriodAccumulator sleepPeriod)
	{
		boolean canCombine = false;
		if (!this.getStartTimestamp().equals(sleepPeriod.getStartTimestamp())
				&& (this.getIsFullSleeper() || sleepPeriod.getIsFullSleeper())
				&& DateUtility.ConvertMillisecondsToHours(this.getLengthCombinablePeriod()) >= this.getParent().MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS
				&& DateUtility.ConvertMillisecondsToHours(sleepPeriod.getLengthCombinablePeriod()) >= this.getParent().MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS)
		{
            // the two periods are different and
            // one of them is a Full Sleeper and
            // the lengths of each period are long enough for combination

            // Now, verify the length of the combined period
			long combinedLength = this.getLengthCombinablePeriod() + sleepPeriod.getLengthCombinablePeriod();
			
            // if the combined lengths of the two periods is larger than a full break
            // then the two periods can be combined
			canCombine = (DateUtility.ConvertMillisecondsToHours(combinedLength) >= this.getParent().FULL_BREAK_HOURS);
		}
		
		return canCombine;
	}	
}
