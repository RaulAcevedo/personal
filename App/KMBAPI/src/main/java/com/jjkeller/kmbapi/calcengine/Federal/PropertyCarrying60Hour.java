package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

public class PropertyCarrying60Hour extends PropertyCarrying {
	
	//Constructor
	public PropertyCarrying60Hour(RulesetProperties properties)
	{
		super(properties);
	}

	public PropertyCarrying60Hour(RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController)
	{
		super(properties, complianceDateController);
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
	}
}
