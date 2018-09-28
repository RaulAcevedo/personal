package com.jjkeller.kmbapi.controller.EOBR;

import android.os.Bundle;

import com.jjkeller.kmbapi.controller.interfaces.IAutoAssignedELDCalls;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.DriverCountResponse;
import com.jjkeller.kmbapi.kmbeobr.Enums;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rab5795 on 7/25/18.
 */

public class AutoAssignedELDCalls implements IAutoAssignedELDCalls {
    protected EobrReader eobr = EobrReader.getInstance();

    public StatusRecord GetStatusRecordForEobrId(int eobrId){
        StatusRecord status = null;

        // if a valid eobrId specified, return eobr data for that id
        if (eobrId != 0 && eobrId != -1)
        {
            status = new StatusRecord();
            int rc = eobr.Technician_GetHistoricalData(status, eobrId);

            if (rc != Enums.EobrReturnCode.S_SUCCESS || status.getRecordId() == 0 || status.getRecordId() == -1) {
                status = null;
            }
        }

        return status;
    }

    public EventRecord GetLastDriverEvent(long startRefTimestamp, long endRefTimestamp, EventTypeEnum... eventTypes){
        EventRecord eventRecord = new EventRecord();
        int returnCode = eobr.Technician_GetDriverEvent(eventRecord, startRefTimestamp, endRefTimestamp, false, eventTypes);

        if (returnCode != Enums.EobrReturnCode.S_SUCCESS) {
            ErrorLogHelper.RecordMessage(String.format("GetLastDriveEvent failed to get last driver event with code %d", returnCode));
        }
        return eventRecord;
    }

    public EventRecord GetLastIgnitionEvent(long startRefTimestamp, long endRefTimestamp){
        List<EventTypeEnum> ignitionEventTypes = Arrays.asList(new EventTypeEnum(EventTypeEnum.IGNITIONOFF), new EventTypeEnum(EventTypeEnum.IGNITIONON));

        EventRecord eventRecord = new EventRecord();
        int returnCode = eobr.Technician_GetDriverEvent(eventRecord, startRefTimestamp, endRefTimestamp, true, ignitionEventTypes.toArray(new EventTypeEnum[ignitionEventTypes.size()]));

        if (returnCode != Enums.EobrReturnCode.S_SUCCESS) {
            ErrorLogHelper.RecordMessage(String.format("GetLastDriveEvent failed to get last ignition event with code %d", returnCode));
        }
        return eventRecord;
    }

    public EventRecord GetNextEvent(long startTimeCode, EventTypeEnum... eventTypes){
        EobrResponse<EventRecord> response = eobr.GetNextEvent(startTimeCode, eventTypes);
        return response.getData();
    }

    public DriverCountResponse GetDriverCount(long startRefTimestamp, long endRefTimestamp) {
        DriverCountResponse response = new DriverCountResponse();
        Bundle bundle = eobr.Technician_GetDriverCount(startRefTimestamp, endRefTimestamp);

        int returnCode = bundle.getInt(Constants.RETURNCODE);
        if (returnCode != Enums.EobrReturnCode.S_SUCCESS) {
            ErrorLogHelper.RecordMessage(String.format("GetLastDriveEvent failed to get last driver event with code %d", returnCode));
        } else {
            response.setDriverCount(bundle.getInt(Constants.RETURNVALUE));
            response.setDriverIds(bundle.getIntArray(Constants.DRIVERIDS));
        }

        return response;
    }
}
