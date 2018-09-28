package com.jjkeller.kmbapi.controller.share;

import com.jjkeller.kmbapi.controller.interfaces.IEnforceMinimumLengthStrategy;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import java.util.Date;

/**
 * Created by jld5296 on 10/28/16.
 */
public class EldEnforceMinimumLengthStrategy implements IEnforceMinimumLengthStrategy {
    public boolean execute(UnassignedDrivingPeriod drivingPeriod) {
        EnforceMinimumLength(drivingPeriod);

        //for ELD mandate, we should always create the period regardless of duration.
        return true;
    }

    private void EnforceMinimumLength(UnassignedDrivingPeriod drivingPeriod) {
        if (drivingPeriod != null && drivingPeriod.getStartTime() != null && drivingPeriod.getStopTime() != null) {

            // Truncate Time to seconds
            Date start = drivingPeriod.getStartTime();
            Date stop = drivingPeriod.getStopTime();

            if (start.compareTo(stop) == 0) {
                // StartTime and StopTime are the same
                // Advance the EndTime by 1 second
                String debugMessage = String.format("StartTime and StopTime fall within the same second, advancing EndTime by 1 second. StartTime: '%1$s' StopTime: '%2$s'", start.toString(), stop.toString());
                ErrorLogHelper.RecordMessage(debugMessage);
                stop = DateUtility.AddSeconds(stop, 1);
            }

            drivingPeriod.setStartTime(start);
            drivingPeriod.setStopTime(stop);
        }
    }
}
