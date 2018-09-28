package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;
import java.util.Hashtable;

public abstract class Exempt100MilePassengerCarrying extends PassengerCarrying {

	/**
	 * Amount of daily duty time allowed for the ruleset in milliseconds
	 */
	protected long DAILY_DUTY_TIME_ALLOWED_STANDARD;
	
	private Date _originalValidWeeklyResetDutyStartTimestamp;
	protected Date getOriginalValidWeeklyResetDutyStartTimestamp()
	{
		return _originalValidWeeklyResetDutyStartTimestamp;
	}
	protected void setOriginalValidWeeklyResetDutyStartTimestamp(Date firstValidWeeklyResetDutyStartTimestamp)
	{
		this._originalValidWeeklyResetDutyStartTimestamp = firstValidWeeklyResetDutyStartTimestamp;
	}
	
	protected Exempt100MilePassengerCarrying(RulesetProperties properties) {
		super(properties);
	}
	
	/**
	 * Initialize the calc engine.
	 * This must be called before any other methods are called on the calc engine.
	 */
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
		
		this.DAILY_DUTY_TIME_ALLOWED_STANDARD = this.DAILY_DUTY_TIME_ALLOWED;
		
		this.setDutyInfoByDate(new Hashtable<Date, DutyInfo>());
	}

	/**
	 * Process an on-duty status event.
	 * Accumulate the duty time accordingly.
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
    }
	
	/**
	 * Process the off-duty event.
	 */
	@Override
    protected void ProcessOffDuty(long length)
    {
		// when less than 10 hours, accumulate as a duty event
        if (DateUtility.ConvertMillisecondsToHours(length) < 10.0)
            this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        
        if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
        	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }
	
	/**
	 * Keep track of the fact that there is duty time on this log.
	 * This log is now considered a duty tour.
	 * This means that the use of the short haul exception may be allowed
	 * because there is duty time.
	 */
    protected void MarkAsDutyTour()
    {
        Date key = DateUtility.GetDateFromDateTime(this.Summary.getRecentDutyTimestamp());
        if (!this.getDutyInfoByDate().containsKey(key))
        {
            this.getDutyInfoByDate().put(key, new DutyInfo());
        }
        
        this.getDutyInfoByDate().get(key).setIsDutyTour(true);
    }
    
	private Hashtable<Date, DutyInfo> _dutyInfoByDate;    
    private Hashtable<Date, DutyInfo> getDutyInfoByDate()
    {
    	return this._dutyInfoByDate;
    }
    private void setDutyInfoByDate(Hashtable<Date, DutyInfo> dutyInfoByDate)
    {
    	this._dutyInfoByDate = dutyInfoByDate;
    }
}
