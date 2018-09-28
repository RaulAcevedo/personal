package com.jjkeller.kmbapi.calcengine;

import java.util.Date;

public class OffDuty implements Cloneable {

    private Date _lastOffDutyTime;
    private boolean _isOffDuty;



    public Date getLastOffDutyTime()
    {
        return this._lastOffDutyTime;
    }
    public void setLastOffDutyTime(Date lastOffDutyTime)
    {
        this._lastOffDutyTime = lastOffDutyTime;
    }

    public boolean getIsOffDuty()
    {
        return this._isOffDuty;
    }
    public void setIsOffDuty(boolean isOffDuty)
    {
        this._isOffDuty = isOffDuty;
    }

}
