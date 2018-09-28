package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class Exempt100MilePassengerCarrying60Hour extends Exempt100MilePassengerCarrying {

	public Exempt100MilePassengerCarrying60Hour(com.jjkeller.kmbapi.calcengine.RulesetProperties properties) {
		super(properties);
	}

	/// <summary>
    /// Initialize the calc engine.
    /// This must be called before any other methods are called on the calc engine.
    /// </summary>
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
		
		this.WEEKLY_DUTY_TIME_ALLOWED = 60 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 7;
		this.DAILY_DUTY_TIME_ALLOWED = 12 * MILLISECONDS_PER_HOUR;
	}
}
