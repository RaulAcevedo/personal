package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;
import java.util.Stack;

public class SplitSleeperCombination implements Cloneable {

	double FULL_BREAK_HOURS = 0.0;
	double MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS = 0.0;
	double MIN_FULL_SLEEPER_PERIOD_HOURS = 0.0;
	double MAX_FULL_SLEEPER_PERIOD_HOURS = 0.0;
	boolean CONVERT_SHORT_OFFDUTY_TO_DUTY = false;
	boolean ALLOW_OFFDUTY_COMBINATIONS = false;
	boolean COUNT_UNCOMBINED_SLEEPERS_AS_DUTY = false;
	boolean CONVERT_ANY_OFFDUTY_TO_DUTY = false;
	
	private Stack<SplitSleeperPeriodAccumulator> _splitSleeperList;
	
	public static SplitSleeperCombination ForPassengerCarryingRules()
	{
		return new SplitSleeperCombination(2.0, 2.0, 8.0, 8.0, false, false, false, false);
	}
	
	public static SplitSleeperCombination ForPropertyCarryingRules()
	{
		return new SplitSleeperCombination(2.0, 8.0, 10.0, 10.0, true, true, false, false);
	}
	
	public static SplitSleeperCombination ForCanadianRules(boolean isTeamDriverPresent)
	{
		if (isTeamDriverPresent)
		{
			return new SplitSleeperCombination(4.0, 4.0, 8.0, 8.0, false, false, false, false);
		}
		else
		{
			return new SplitSleeperCombination(2.0, 2.0, 8.0, 10.0, false, false, false, false);
		}
	}
	
	public static SplitSleeperCombination ForUSOilField()
	{
		return new SplitSleeperCombination(2.0, 2.0, 8.0, 10.0, true, false, true, true);
	}
	
	public static SplitSleeperCombination ForTexasOilField()
	{
		return new SplitSleeperCombination(2.0, 2.0, 8.0, 8.0, false, false, false, false);
	}
	
	public static SplitSleeperCombination ForMotionPicture()
	{
		return new SplitSleeperCombination(2.0, 2.0, 8.0, 8.0, false, false, false, false);
	}
	
	private SplitSleeperCombination(double minOffDuty, double minSleeper, double maxSleeper, double fullBreak, boolean convertShortOffDutyPeriods, boolean allowOffDutyToCombine, boolean countUncombinedSleepersAsDuty, boolean convertAnyOffDutyToDuty)
	{
		this.MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS = minOffDuty;
		this.MIN_FULL_SLEEPER_PERIOD_HOURS = minSleeper;
		this.MAX_FULL_SLEEPER_PERIOD_HOURS = maxSleeper;
		this.FULL_BREAK_HOURS = fullBreak;
		this.CONVERT_SHORT_OFFDUTY_TO_DUTY = convertShortOffDutyPeriods;
		this.ALLOW_OFFDUTY_COMBINATIONS = allowOffDutyToCombine;
		this.COUNT_UNCOMBINED_SLEEPERS_AS_DUTY = countUncombinedSleepersAsDuty;
		this.CONVERT_ANY_OFFDUTY_TO_DUTY = convertAnyOffDutyToDuty;
		
		this._splitSleeperList = new Stack<SplitSleeperPeriodAccumulator>();		
	}
	
	private Stack<SplitSleeperPeriodAccumulator> getSplitSleeperList()
	{
		return this._splitSleeperList;
	}
	private void setSplitSleeperList(Stack<SplitSleeperPeriodAccumulator> splitSleeperList)
	{
		this._splitSleeperList = splitSleeperList;
	}
	
	public Object Clone()
	{
		SplitSleeperCombination val = new SplitSleeperCombination(this.MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS, this.MIN_FULL_SLEEPER_PERIOD_HOURS, this.MAX_FULL_SLEEPER_PERIOD_HOURS, this.FULL_BREAK_HOURS, this.CONVERT_SHORT_OFFDUTY_TO_DUTY, this.ALLOW_OFFDUTY_COMBINATIONS, this.COUNT_UNCOMBINED_SLEEPERS_AS_DUTY, this.CONVERT_ANY_OFFDUTY_TO_DUTY);
		
		for (int i = this.getSplitSleeperList().size()-1; i>=0; i--)
		{
			SplitSleeperPeriodAccumulator slpPeriod = this.getSplitSleeperList().elementAt(i);
			val.getSplitSleeperList().push((SplitSleeperPeriodAccumulator)slpPeriod.Clone());
		}
		
		return val;
	}
	
    /// <summary>
    /// Process the On-Duty period time by passing it through to each sleeper period
    /// being tracked.
    /// </summary>
	public void ProcessOnDutyTime(long length)
	{
		if (this.getSplitSleeperList().size() > 0)
		{
			for (SplitSleeperPeriodAccumulator slpPeriod : this.getSplitSleeperList())
			{
				slpPeriod.ProcessOnDutyTime(length);
			}
		}
	}
	
    /// <summary>
    /// Process the Drive time by passing is through to each sleeper period being 
    /// tracked
    /// </summary>
	public void ProcessDriveTime(long length)
	{
		if (this.getSplitSleeperList().size() > 0)
		{
			for (SplitSleeperPeriodAccumulator slpPeriod : this.getSplitSleeperList())
			{
				slpPeriod.ProcessDriveTime(length);
			}
		}
	}
	
    /// <summary>
    /// Process the Off-Duty time by attempting to combine with another period.
    /// </summary>
	public boolean ProcessOffDutyTime(long length, HoursOfServiceSummary summary)
	{
		boolean currentPeriodCombinable = false;
		if (this.ALLOW_OFFDUTY_COMBINATIONS)
		{
			boolean qualifiedPeriod = this.UpdateOffDutyTime(summary.getRecentDutyTimestamp(), length, false);
			
			if (this.getSplitSleeperList().size() > 0)
			{
                // the primary sleeper is the most recent one
				SplitSleeperPeriodAccumulator primarySleeperPeriod = this.getSplitSleeperList().peek();
				
				currentPeriodCombinable = this.AttemptToCombinePeriods(length, primarySleeperPeriod, summary);
				
				if (qualifiedPeriod)
				{
					primarySleeperPeriod.setDutyTimeAccumulated(0);
					primarySleeperPeriod.setDriveTimeAccumulated(0);
				}
			}
		}
		else
		{
            // off-duty periods are not allowed for combination in Canada
			if (this.getSplitSleeperList() != null && this.getSplitSleeperList().size() > 0)
			{
				for (SplitSleeperPeriodAccumulator slp : this.getSplitSleeperList())
				{
                    // accumlate the offduty time against all possible sleepers
					slp.ProcessOffDutyTime(length);
				}
			}
		}
		return currentPeriodCombinable;
	}
	
	/// Backward compatible method for rulesets other than US Oilfield
	public boolean ProcessSleeperTime(long length, HoursOfServiceSummary summary)
	{
		return this.ProcessSleeperTime(length, summary, false);
	}
	
    /// <summary>
    /// Process the Sleeper time by attempting to combine with one of the periods
    /// being tracked.
    /// </summary>
	public boolean ProcessSleeperTime(long length, HoursOfServiceSummary summary, boolean isOffDutywellsitePeriod)
	{
		boolean currentPeriodCombinable = false;
		boolean qualifiedPeriod = this.UpdateOffDutyTime(summary.getRecentDutyTimestamp(), length, isOffDutywellsitePeriod);
		
		if (this.getSplitSleeperList().size() > 0)
		{
			SplitSleeperPeriodAccumulator primarySleeperPeriod = this.getSplitSleeperList().peek();
			
			if (DateUtility.ConvertMillisecondsToHours(length) >= this.MIN_FULL_SLEEPER_PERIOD_HOURS &&
					DateUtility.ConvertMillisecondsToHours(length) < this.MAX_FULL_SLEEPER_PERIOD_HOURS)
			{
				primarySleeperPeriod.setIsFullSleeper(true);
			}
			
			currentPeriodCombinable = this.AttemptToCombinePeriods(length, primarySleeperPeriod, summary);
			
			if (qualifiedPeriod)
			{
				primarySleeperPeriod.setDutyTimeAccumulated(0);
				primarySleeperPeriod.setDriveTimeAccumulated(0);
			}
		}
		
		return currentPeriodCombinable;
	}

    /// <summary>
    /// Update the off-duty time in the most recently tracked sleeper period.
    /// Answer if the most recent sleeper period now has enough total consecutive
    /// time in it to be qualified as a combinable period.
    /// </summary>
	private boolean UpdateOffDutyTime(Date dutyTimestamp, long length, boolean isOffDutyWellsitePeriod)
	{
		boolean qualifiedOffDutyPeriod = false;
		boolean newOffDutyPeriod = true;
		if (this.getSplitSleeperList().size() != 0)
		{
			SplitSleeperPeriodAccumulator slpPeriod = this.getSplitSleeperList().peek();
			
            // attempt to determine if this off-duty period is a continuation of 
            // the most current on being tracked
			if (slpPeriod.getStartTimestamp().equals(dutyTimestamp))
			{
                // the start times exactly match
				slpPeriod.setLengthCombinablePeriod(length);
				newOffDutyPeriod = false;				
			}
			else if (new Date(slpPeriod.getStartTimestamp().getTime() + slpPeriod.getLengthCombinablePeriod()).equals(dutyTimestamp))
			{
                // the end of the slpPeriod matches the dutyTimestamp 
				slpPeriod.setLengthCombinablePeriod(slpPeriod.getLengthCombinablePeriod() + length);
				newOffDutyPeriod = false;
			}
			
            // determine if this period has now become qualified as combinable period
			long newLength = slpPeriod.getLengthCombinablePeriod();
			if (!newOffDutyPeriod && DateUtility.ConvertMillisecondsToHours(newLength) >= this.MIN_OFFDUTY_COMBINABLE_PERIOD_HOURS)
			{
				qualifiedOffDutyPeriod = true;
			}
		}
		
		if (newOffDutyPeriod)
		{
			SplitSleeperPeriodAccumulator newSlpPeriod = new SplitSleeperPeriodAccumulator(this);
			newSlpPeriod.setSTartTimestamp(dutyTimestamp);
			newSlpPeriod.setLengthCombinablePeriod(length);
			newSlpPeriod.setIsOffDutyWellsitePeriod(isOffDutyWellsitePeriod);
			this.getSplitSleeperList().push(newSlpPeriod);
		}

		return qualifiedOffDutyPeriod;
	}

    /// <summary>
    /// Attempt to combine each of the sleeper periods into the primary, or current,
    /// sleeper period.   If the two periods can be combined, then update the HOSSummary
    /// with the new drive/duty time values.
    /// The list of tracked sleeper periods will be reset and will only contain
    /// the primary sleeper as it may be used in a combination later on.
    /// </summary>
	private boolean AttemptToCombinePeriods(long length, SplitSleeperPeriodAccumulator primarySleeperPeriod, HoursOfServiceSummary summary)
	{
		boolean isAbleToCombine = false;
		
		for (int i=this.getSplitSleeperList().size()-1; i>=0; i--)
		{
			SplitSleeperPeriodAccumulator slpPeriod = this.getSplitSleeperList().elementAt(i);
			
			boolean canCombineForBreak = slpPeriod.CanCombineForFullBreak(primarySleeperPeriod);
			if (canCombineForBreak)
			{
				summary.setDriveTimeAccumulated(slpPeriod.getDriveTimeAccumulated());
				summary.setDutyTimeAccumulated(slpPeriod.getDutyTimeAccumulated());
				summary.setWorkShiftTimeAccumulated(slpPeriod.getWorkShiftTimeAccumulated());
								
                // since there's been a combination, all previous sleeper periods
                // can be dropped, except the most current one
				this.setSplitSleeperList(new Stack<SplitSleeperPeriodAccumulator>());

                // always need to keep the most current period because it might be 
                // a candidate to combine later on
                this.getSplitSleeperList().push(primarySleeperPeriod);
                
                isAbleToCombine = true;
                break;
			}
			
			// if the sleeper period is not an off duty wellsite period, check if need to add as duty time to last combined sleeper period
			if (!primarySleeperPeriod.getStartTimestamp().equals(slpPeriod.getStartTimestamp()) && !primarySleeperPeriod.getIsOffDutyWellsitePeriod())
				slpPeriod.ProcessSleeperTime(length, isAbleToCombine);
		}
		
		return isAbleToCombine;
	}
}
