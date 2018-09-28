package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class PassengerCarrying60Hour extends PassengerCarrying {
	
	public PassengerCarrying60Hour(RulesetProperties properties)
	{
		super(properties);
	}
	
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
		
		this.WEEKLY_DUTY_TIME_ALLOWED = 60 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 7;
	}
}
