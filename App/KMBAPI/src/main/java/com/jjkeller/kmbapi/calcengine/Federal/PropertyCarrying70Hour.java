package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

public class PropertyCarrying70Hour extends PropertyCarrying{
		
	//Constructor
	public PropertyCarrying70Hour(RulesetProperties properties)
	{
		super(properties);
	}
	
	public PropertyCarrying70Hour(RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController)
	{
		super(properties, complianceDateController);
	}
	
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
		
		this.WEEKLY_DUTY_TIME_ALLOWED = 70 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 8;
	}
}
