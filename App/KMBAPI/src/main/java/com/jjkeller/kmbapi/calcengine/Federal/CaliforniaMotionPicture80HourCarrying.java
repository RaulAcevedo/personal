package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;

/**
 * Created by T000726 on 9/21/2017.
 */

public class CaliforniaMotionPicture80HourCarrying extends PropertyCarrying {

    public CaliforniaMotionPicture80HourCarrying(com.jjkeller.kmbapi.calcengine.RulesetProperties properties) {
        super(properties);
    }

    @Override
    public void Initialize(RulesetProperties properties) {
        super.Initialize(properties);

        this.WEEKLY_DUTY_TIME_ALLOWED = 80 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_DUTY_PERIOD_DAYS = 8;

        this.DAILY_DRIVE_TIME_ALLOWED = 12 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 8 * MILLISECONDS_PER_HOUR;
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

}
