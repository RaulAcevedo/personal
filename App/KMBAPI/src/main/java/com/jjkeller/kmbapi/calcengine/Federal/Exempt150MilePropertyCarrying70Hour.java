package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

public class Exempt150MilePropertyCarrying70Hour extends Exempt150MilePropertyCarrying {
	
	public Exempt150MilePropertyCarrying70Hour(com.jjkeller.kmbapi.calcengine.RulesetProperties properties) {
		super(properties);
	}
	
	public Exempt150MilePropertyCarrying70Hour(com.jjkeller.kmbapi.calcengine.RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController) {
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
		
		this.WEEKLY_DUTY_TIME_ALLOWED = 70 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 8;
		this.DAILY_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED_WITH_NON_CDL_EXEMPTION = this.DAILY_DUTY_TIME_ALLOWED + (2 * MILLISECONDS_PER_HOUR);
	}
}
