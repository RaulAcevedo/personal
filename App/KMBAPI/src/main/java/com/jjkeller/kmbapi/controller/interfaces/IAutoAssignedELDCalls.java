package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.kmbeobr.DriverCountResponse;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

/**
 * Created by rab5795 on 7/25/18.
 */

public interface IAutoAssignedELDCalls {

    public abstract StatusRecord GetStatusRecordForEobrId(int eobrId);
    public abstract EventRecord GetLastDriverEvent(long startRefTimestamp, long endRefTimestamp, EventTypeEnum... eventTypes);
    public abstract EventRecord GetLastIgnitionEvent(long startRefTimestamp, long endRefTimestamp);
    public abstract EventRecord GetNextEvent(long startTimeCode, EventTypeEnum... eventTypes);
    public abstract DriverCountResponse GetDriverCount(long startRefTimestamp, long endRefTimestamp);
}
