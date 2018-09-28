package com.jjkeller.kmbapi.controller.share;

import com.jjkeller.kmbapi.controller.interfaces.IEnforceMinimumLengthStrategy;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jld5296 on 10/28/16.
 */
public class AobrdEnforceMinimumLengthStrategy implements IEnforceMinimumLengthStrategy {
    @Override
    public boolean execute(UnassignedDrivingPeriod udp) {

        //this check may seem redundant with the enforceMinimumLength method
        //but this allows short driving periods if there's at least some distance
        //(or a longer period even if there isn't distance)
        if(udp.getDistance() > 0F || (udp.getStopTime().getTime() - udp.getStartTime().getTime()) > 60000) {
            EnforceMinimumLength(udp);

            return true;
        }

        String debugMessage = String.format(Locale.getDefault(),
                "Unassigned driving period failed minimum length check. StartTime: '%1$s' StopTime: '%2$s' Distance: '%3$.2f'",
                udp.getStartTime(), udp.getStopTime(), udp.getDistance());
        ErrorLogHelper.RecordMessage(debugMessage);

        return false;
    }

    private void EnforceMinimumLength(UnassignedDrivingPeriod drivingPeriod) {
        if (drivingPeriod != null && drivingPeriod.getStartTime() != null && drivingPeriod.getStopTime() != null) {

            // Truncate Time
            Date start = getDateTimeWithoutSecondsFromDate(drivingPeriod.getStartTime());
            Date stop = getDateTimeWithoutSecondsFromDate(drivingPeriod.getStopTime());

            if (start.compareTo(stop) == 0) {
                // StartTime and StopTime fall within the same minute.
                // Advance the EndTime by 1 minute
                String debugMessage = String.format("StartTime and StopTime fall within the same minute, advancing EndTime by 1 minute. StartTime: '%1$s' StopTime: '%2$s'", start.toString(), stop.toString());
                ErrorLogHelper.RecordMessage(debugMessage);
                stop = DateUtility.AddMinutes(stop, 1);
            }

            drivingPeriod.setStartTime(start);
            drivingPeriod.setStopTime(stop);
        }
    }

    private Date getDateTimeWithoutSecondsFromDate(Date date) {
        Date returnDate = null;

        try {
            // Truncate seconds
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            returnDate = cal.getTime();
        }
        catch (Exception e) {
            returnDate = null;
        }

        return returnDate;
    }

}
