package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;

import java.util.Date;

public class HoursOfServiceSummary implements Cloneable{

	private Date _validityTimestamp;
	private Date _logStartTimestamp;
	private Date _recentDutyTimestamp;
	private DutyStatusEnum _recentDutyStatus;
	private long _recentDutyLength;
	private RuleSetTypeEnum _recentDutyRuleset;
	private long _driveTimeAccumulated;
	private long _dutyTimeAccumulated; 
	private long _logDateDriveTimeAccumulated;
	private long _logDateDutyTimeAccumulated;
	private long _workShiftTimeAccumulated;
	private long _weeklyDutyTimeAccumulated;
	private long _dailyResetAmount;
	private long _weeklyResetAmount;
	private boolean _isShortHaulExceptionAvailable;
	private boolean _is34HourResetAllowed;
	private SplitSleeperCombination _splitSleeperCombination;
	private long _cycle2OffDutyResetAmount;
	private Date _recent24HourOffDutyPeriod;
	private CanadaDeferralTypeEnum _canadaDeferralType;
	private long _driveTimeAccumulated8HourRule;
	private long _consecutiveOffDutyAccumulated;
	private boolean _hasDrivingOccurredAfterReset;
	private long _oilfieldshorthaulreset;
	
	public HoursOfServiceSummary()
	{
		this.setDailyResetAmount(0);
		this.setDriveTimeAccumulated(0);
		this.setDriveTimeAccumulated8HourRule(0);
		this.setConsecutiveOffDutyAccumulated(0);
		this.setDutyTimeAccumulated(0);
		this.setIsShortHaulExceptionAvailable(false);
		this.setIs34HourResetAllowed(false);
		this.setRecentDutyLength(0);
		this.setRecentDutyTimestamp(null);
		this.setRecentDutyRuleset(RuleSetTypeEnum.Null);
		this.setValidityTimestamp(null);
		this.setWeeklyDutyTimeAccumulated(0);
		this.setWeeklyResetAmount(0);
		this.setLogDateDriveTimeAccumulated(0);
		this.setLogDateDutyTimeAccumulated(0);
		this.setLogStartTimestamp(null);
		this.setWorkShiftTimeAccumulated(0);
		this.setRecent24HourOffDutyPeriod(null);
		this.setCycle2OffDutyResetAmount(0);
		this.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
		this.setHasDrivingOccurredAfterDailyReset(false);
		this.setOilFieldShortHaulReset(0);
	}

	public Object Clone()
	{
		HoursOfServiceSummary val = new HoursOfServiceSummary();
		
		val.setDailyResetAmount(this.getDailyResetAmount());
		val.setDriveTimeAccumulated(this.getDriveTimeAccumulated());
		val.setDriveTimeAccumulated8HourRule(this.getDriveTimeAccumulated8HourRule());
		val.setConsecutiveOffDutyAccumulated(this.getConsecutiveOffDutyAccumulated());
		val.setDutyTimeAccumulated(this.getDutyTimeAccumulated());
		val.setIsShortHaulExceptionAvailable(this.getIsShortHaulExceptionAvailable());
		val.setIs34HourResetAllowed(this.getIs34HourResetAllowed());
		val.setRecentDutyLength(this.getRecentDutyLength());
		val.setRecentDutyStatus(this.getRecentDutyStatus());
		val.setRecentDutyTimestamp(this.getRecentDutyTimestamp());
		val.setRecentDutyRuleset(this.getRecentDutyRuleset());
		val.setValidityTimestamp(this.getValidityTimestamp());
		val.setWeeklyDutyTimeAccumulated(this.getWeeklyDutyTimeAccumulated());
		val.setWeeklyResetAmount(this.getWeeklyResetAmount());
		val.setLogDateDriveTimeAccumulated(this.getLogDateDriveTimeAccumulated());
		val.setLogDateDutyTimeAccumulated(this.getLogDateDutyTimeAccumulated());
		val.setLogStartTimestamp(this.getLogStartTimestamp());
		val.setWorkShiftTimeAccumulated(this.getWorkShiftTimeAccumulated());
		val.setRecent24HourOffDutyPeriod(this.getRecent24HourOffDutyPeriod());
		val.setCycle2OffDutyResetAmount(this.getCycle2OffDutyResetAmount());
		val.setCanadaDeferralType(this.getCanadaDeferralType());
		val.setHasDrivingOccurredAfterDailyReset(this.getHasDrivingOccurredAfterDailyReset());
		val.setOilFieldShortHaulReset(this.getOilFieldShortHaulReset());
		
		if (this.getCombinableOffDutyPeriod() != null)
		{
			val.setCombinableOffDutyPeriod((SplitSleeperCombination) this.getCombinableOffDutyPeriod().Clone());
		}
		
		return val;
	}
	
	/// NOTE:  SET methods that do not have an accessibility level defined
	/// are accessible only within the package (corresponds to C# internal -
	/// which is only accessible within the assembly)

    /// <summary>
    /// Timestamp that the summary has been calculated through.
    /// This will be the ending time of the very last event that the engine
    /// has performed calcs on.
    /// </summary>
	public Date getValidityTimestamp()
	{
		return this._validityTimestamp;
	}
	public void setValidityTimestamp(Date validityTimestamp)
	{
		this._validityTimestamp = validityTimestamp;
	}

    /// <summary>
    /// Timestamp of the start of the log that the summary has been 
    /// calculated through.
    /// This will be the starting time of the very last log that the engine
    /// has performed calcs on.
	public Date getLogStartTimestamp()
	{
		return this._logStartTimestamp;
	}
	public void setLogStartTimestamp(Date logStartTimestamp)
	{
		this._logStartTimestamp = logStartTimestamp;
	}

    /// <summary>
    /// Timestamp of the start of the event processed by the engine
    /// </summary>
	public Date getRecentDutyTimestamp()
	{
		return this._recentDutyTimestamp;
	}
	public void setRecentDutyTimestamp(Date recentDutyTimestamp)
	{
		this._recentDutyTimestamp = recentDutyTimestamp;
	}
	
    /// <summary>
    /// Duty status of the event processed by the engine
    /// </summary>
	public DutyStatusEnum getRecentDutyStatus()
	{
		return this._recentDutyStatus;
	}
	public void setRecentDutyStatus(DutyStatusEnum dutyStatusEnum)
	{
		this._recentDutyStatus = dutyStatusEnum;
	}
	
    /// <summary>
    /// Length of the duty status event processed by the engine
    /// </summary>
	public long getRecentDutyLength()
	{
		return this._recentDutyLength;
	}
	public void setRecentDutyLength(long recentDutyLength)
	{
		this._recentDutyLength = recentDutyLength;
	}
	
    /// <summary>
    /// Ruleset of the duty status event processed by the engine
    /// </summary>
	public RuleSetTypeEnum getRecentDutyRuleset()
	{
		return this._recentDutyRuleset;
	}
	public void setRecentDutyRuleset(RuleSetTypeEnum recentDutyRuleset)
	{
		this._recentDutyRuleset = recentDutyRuleset;
	}
	
    /// <summary>
    /// Drive time accumulated since the start of the work shift.
    /// The start of the work shift begins after the previous daily reset.
    /// </summary>
	public long getDriveTimeAccumulated()
	{
		return this._driveTimeAccumulated;
	}
	public void setDriveTimeAccumulated(long driveTimeAccumulated)
	{
		this._driveTimeAccumulated = driveTimeAccumulated;
	}
	
    /// <summary>
    /// Drive time accumulated since the start of the work shift, or since the last 30 minute break (offduty or sleeper). 
	/// This accumulated tracks the proposed 8 hour driving rule which tentatively goes into effect 7/1/2013.
    /// The start of the work shift begins after the previous daily reset.
    /// </summary>
	public long getDriveTimeAccumulated8HourRule()
	{
		return this._driveTimeAccumulated8HourRule;
	}
	public void setDriveTimeAccumulated8HourRule(long driveTimeAccumulated)
	{
		this._driveTimeAccumulated8HourRule = driveTimeAccumulated;
	}
	
    /// <summary>
    /// Amount of consecutive off duty time, used for the 8 Hour Driving rule reset
    /// </summary>	
	public long getConsecutiveOffDutyAccumulated()
	{
		return this._consecutiveOffDutyAccumulated;
	}
	public void setConsecutiveOffDutyAccumulated(long time)
	{
		this._consecutiveOffDutyAccumulated = time;
	}
	
    /// <summary>
    /// Duty time accumulated since the start of the work shift.
    /// The start of the work shift begins after the previous daily reset.
    /// </summary>
	public long getDutyTimeAccumulated()
	{
		return this._dutyTimeAccumulated;
	}
	public void setDutyTimeAccumulated(long dutyTimeAccumulated)
	{
		this._dutyTimeAccumulated = dutyTimeAccumulated;
	}
	
    /// <summary>
    /// Amount of drive time accumulated since the start of the log day
    /// </summary>
	public long getLogDateDriveTimeAccumulated()
	{
		return this._logDateDriveTimeAccumulated;
	}
	public void setLogDateDriveTimeAccumulated(long logDateDriveTimeAccumulated)
	{
		this._logDateDriveTimeAccumulated = logDateDriveTimeAccumulated;
	}

    /// <summary>
    /// Amount of duty time accumulated since the start of the log day
    /// </summary>
	public long getLogDateDutyTimeAccumulated()
	{
		return this._logDateDutyTimeAccumulated;
	}
	public void setLogDateDutyTimeAccumulated(long logDateDutyTimeAccumulated)
	{
		this._logDateDutyTimeAccumulated = logDateDutyTimeAccumulated;
	}

    /// <summary>
    /// Amount of time accumulated since the start of the workshift
    /// </summary>
	public long getWorkShiftTimeAccumulated()
	{
		return this._workShiftTimeAccumulated;
	}
	public void setWorkShiftTimeAccumulated(long workShiftTimeAccumulated)
	{
		this._workShiftTimeAccumulated = workShiftTimeAccumulated;
	}

    /// <summary>
    /// Duty time accumulated since the start of the work week.
    /// The start of the work week begins after the previous weekly reset.
    /// </summary>
	public long getWeeklyDutyTimeAccumulated()
	{
		return this._weeklyDutyTimeAccumulated;
	}
	public void setWeeklyDutyTimeAccumulated(long weeklyDutyTimeAccumulated)
	{
		this._weeklyDutyTimeAccumulated = weeklyDutyTimeAccumulated;
	}
	
    /// <summary>
    /// Amount of off duty time required to reset the work shift
    /// </summary>
	public long getDailyResetAmount()
	{
		return this._dailyResetAmount;
	}
	public void setDailyResetAmount(long dailyResetAmount)
	{
		this._dailyResetAmount = dailyResetAmount;
	}

    @Override
    public String toString() {
        return "HoursOfServiceSummary{" +
                "_validityTimestamp=" + _validityTimestamp +
                ", _logStartTimestamp=" + _logStartTimestamp +
                ", _recentDutyTimestamp=" + _recentDutyTimestamp +
                ", _recentDutyStatus=" + _recentDutyStatus +
                ", _recentDutyLength=" + _recentDutyLength +
                ", _recentDutyRuleset=" + _recentDutyRuleset +
                ", _driveTimeAccumulated=" + _driveTimeAccumulated +
                ", _dutyTimeAccumulated=" + _dutyTimeAccumulated +
                ", _logDateDriveTimeAccumulated=" + _logDateDriveTimeAccumulated +
                ", _logDateDutyTimeAccumulated=" + _logDateDutyTimeAccumulated +
                ", _workShiftTimeAccumulated=" + _workShiftTimeAccumulated +
                ", _weeklyDutyTimeAccumulated=" + _weeklyDutyTimeAccumulated +
                ", _dailyResetAmount=" + _dailyResetAmount +
                ", _weeklyResetAmount=" + _weeklyResetAmount +
                ", _isShortHaulExceptionAvailable=" + _isShortHaulExceptionAvailable +
                ", _is34HourResetAllowed=" + _is34HourResetAllowed +
                ", _splitSleeperCombination=" + _splitSleeperCombination +
                ", _cycle2OffDutyResetAmount=" + _cycle2OffDutyResetAmount +
                ", _recent24HourOffDutyPeriod=" + _recent24HourOffDutyPeriod +
                ", _canadaDeferralType=" + _canadaDeferralType +
                ", _driveTimeAccumulated8HourRule=" + _driveTimeAccumulated8HourRule +
                ", _consecutiveOffDutyAccumulated=" + _consecutiveOffDutyAccumulated +
                ", _hasDrivingOccurredAfterReset=" + _hasDrivingOccurredAfterReset +
                ", _oilfieldshorthaulreset=" + _oilfieldshorthaulreset +
                '}';
    }

    /// <summary>
    /// Amount of off duty time required to reset the work week
    /// </summary>
	public long getWeeklyResetAmount()
	{
		return this._weeklyResetAmount;
	}
	public void setWeeklyResetAmount(long weeklyResetAmount)
	{
		this._weeklyResetAmount = weeklyResetAmount;
	}
	
    /// <summary>
    /// Whether the short haul exception is available.
    /// This is a US Federal concept.
    /// </summary>
	public boolean getIsShortHaulExceptionAvailable()
	{
		return this._isShortHaulExceptionAvailable;
	}
	public void setIsShortHaulExceptionAvailable(boolean isShortHaulExceptionAvailable)
	{
		this._isShortHaulExceptionAvailable = isShortHaulExceptionAvailable;
	}

    /// <summary>
    /// Whether the 34 hour reset is available.
    /// This is a US Federal property carrying concept.
    /// </summary>
	public boolean getIs34HourResetAllowed()
	{
		return this._is34HourResetAllowed;
	}
	public void setIs34HourResetAllowed(boolean is34HourResetAllowed)
	{
		this._is34HourResetAllowed = is34HourResetAllowed;
	}

    /// <summary>
    /// The combinable split sleeper info
    /// </summary>
	public SplitSleeperCombination getCombinableOffDutyPeriod()
	{
		return this._splitSleeperCombination;
	}
	public void setCombinableOffDutyPeriod(SplitSleeperCombination splitSleeperCombination)
	{
		this._splitSleeperCombination = splitSleeperCombination;
	}
	
    /// <summary>
    /// Amount of Canadian Cycle 2 duty time that is allowed 
    /// before requiring a mandatory 24 hour break
    /// </summary>
	public long getCycle2OffDutyResetAmount()
	{
		return this._cycle2OffDutyResetAmount;
	}
	public void setCycle2OffDutyResetAmount(long cycle2OffDutyResetAmount)
	{
		this._cycle2OffDutyResetAmount = cycle2OffDutyResetAmount;
	}

    /// <summary>
    /// Date of the last full 24 hour offduty period
    /// </summary>
	public Date getRecent24HourOffDutyPeriod()
	{
		return this._recent24HourOffDutyPeriod;
	}
	public void setRecent24HourOffDutyPeriod(Date recent24HourOffDutyPeriod)
	{
		this._recent24HourOffDutyPeriod = recent24HourOffDutyPeriod;
	}
	
    /// <summary>
    /// Specific canadian offduty deferral in place for the log
    /// </summary>
	public CanadaDeferralTypeEnum getCanadaDeferralType()
	{
		return this._canadaDeferralType;
	}
	public void setCanadaDeferralType(CanadaDeferralTypeEnum canadaDeferralType)
	{
		this._canadaDeferralType = canadaDeferralType;
	}
	
    /// <summary>
    /// Answer if there has been a driving period after a daily reset
    /// </summary>
	public boolean getHasDrivingOccurredAfterDailyReset()
	{
		return this._hasDrivingOccurredAfterReset;
	}
	public void setHasDrivingOccurredAfterDailyReset(boolean value)
	{
		this._hasDrivingOccurredAfterReset = value;
	}

    /// <summary>
    /// Amount of off duty time required to allow shorthaul exception to be used again 
	/// for oil field log
    /// </summary>
	public long getOilFieldShortHaulReset()
	{
		return this._oilfieldshorthaulreset;
	}
	public void setOilFieldShortHaulReset(long oilFieldShortHaulReset)
	{
		this._oilfieldshorthaulreset = oilFieldShortHaulReset;
	}
}
