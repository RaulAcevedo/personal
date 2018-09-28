package com.jjkeller.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

import java.util.Date;

public abstract class USConstructionRulesetBase extends PropertyCarrying {

	protected USConstructionRulesetBase(RulesetProperties properties)
	{
		super(properties);
	}

	public USConstructionRulesetBase(RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController)
	{
		super(properties, complianceDateController);
	}

	/**
	 * Determine if a weekly hour reset has occurred.
	 * When the reset occurs, then the summary is completely reset to zero.
	 * Also, allow the short-haul exception to be used again.
	 */
	@Override
	protected void CalculateWeeklyReset(long length)
	{
		this.Summary.setWeeklyResetAmount(this.Summary.getWeeklyResetAmount() - length);

		long startTimestamp;
		if (this.getOriginalValidWeeklyResetDutyStartTimestamp() != null)
			startTimestamp = this.getOriginalValidWeeklyResetDutyStartTimestamp().getTime();
		else
			startTimestamp = this.Summary.getRecentDutyTimestamp().getTime();
		long endTimestamp = this.Summary.getRecentDutyTimestamp().getTime() + this.Summary.getRecentDutyLength();

		if (this.IsWeeklyResetValid(startTimestamp, endTimestamp))
		{
			DutyInfo dutyInfo = this.GetCurrentDutyInfo();
			if (!dutyInfo.getIsWeeklyResetUsedOverridden())
			{
				this.Summary.setDriveTimeAccumulated(0);
				this.Summary.setDutyTimeAccumulated(0);
				this.Summary.setWeeklyDutyTimeAccumulated(0);
				this.MarkSleeperCombinationReset();

				if (this.getRulesetProperties().getIs8HourDrivingRuleEnabled())
				{
					this.Summary.setDriveTimeAccumulated8HourRule(0);
				}

				this.ResetDailyDutyTotals();
				this.MarkWeeklyReset(new Date(startTimestamp));
				this.Summary.setConsecutiveOffDutyAccumulated(0);
			}
		}
	}
}
