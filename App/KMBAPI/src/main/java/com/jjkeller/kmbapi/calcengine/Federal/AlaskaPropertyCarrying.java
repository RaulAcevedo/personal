package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public class AlaskaPropertyCarrying extends PropertyCarrying
{
	protected AlaskaPropertyCarrying(RulesetProperties properties)
	{
		super(properties);
	}
	
    @Override
    public void Initialize(RulesetProperties properties)
    {
        super.Initialize(properties);

        this.DAILY_DRIVE_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 20 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED_STANDARD = this.DAILY_DUTY_TIME_ALLOWED;
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 10 * MILLISECONDS_PER_HOUR;
    }
    
    /**
     * Override default property carrying ruleset to not reset off duty accululated because we're using non-consecutive time
     */
    @Override
    protected void ProcessOnDuty(long length)
    {
		this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
		this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
		this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
		this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
		this.setOriginalValidWeeklyResetDutyStartTimestamp(null);

		this.MarkAsDutyTour();
		
		this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);

		if (!this.getProcessFirstOnDutyEvent())
			this.setProcessFirstOnDutyEvent(true);
    }
    
    /**
     * Override default property carrying ruleset to process as non-consecutive time instead
     */
    @Override
	protected void ProcessOffDuty(long length)
	{
    	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessOffDutyTime(length, this.Summary);            
        if(hasCombinedSleeper)
        	this.Summary.setHasDrivingOccurredAfterDailyReset(false);
    	
		if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
		{
			this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
		}

		this.CalculateDailyReset(length);
		this.CalculateWeeklyReset(length);
	}
    
    /**
     * Override default property carrying ruleset to process as non-consecutive time instead
     */
    @Override
	protected void ProcessSleepDuty(long length)
    {
    	if (DateUtility.ConvertMillisecondsToHours(length) < 8.0)
        {
            // when the length of the sleeper is less than 8 hours, process as an off-duty
            this.ProcessOffDuty(length);
        }
        else
        {
        	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);            
            if(hasCombinedSleeper)
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);

            if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
            {
            	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
            }
            
            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);
        }
    }
    
    /**
     * Override default property carrying to not allow short haul exception
     */
    @Override
    protected boolean CanShortHaulExceptionBeUsed()
    {
    	return false;
    }
}
