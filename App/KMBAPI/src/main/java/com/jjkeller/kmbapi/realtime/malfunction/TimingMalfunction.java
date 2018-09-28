package com.jjkeller.kmbapi.realtime.malfunction;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;

import java.util.Date;


public class TimingMalfunction {

    private EmployeeLogEldMandateController eldMandateController;
    private EobrReader eobrReader;

    public TimingMalfunction(final EobrReader aEobrReaderInstance,
                             final EmployeeLogEldMandateController anEmployeeLogEldMandateController) {
        eobrReader = aEobrReaderInstance;
        eldMandateController = anEmployeeLogEldMandateController;
    }


    private static final long MANDATE_CLOCK_SYNC_FAILURE_THRESHOLD = 10 * 60 * 1000;	// 10 minutes, as a millisecond value
    protected boolean isWithinEldMandateThreshold(long clockDifference){
        return  Math.abs(clockDifference) <= MANDATE_CLOCK_SYNC_FAILURE_THRESHOLD;
    }

    private static final String TAB_DMO_MSG = "Timing Malfunction detected - TAB clock and DMO time too far apart";
    private static final String TAB_GPS_MSG = "Timing Malfunction detected - TAB clock and GPS time too far apart ";
    private static final String ANDROID_GPS_MSG = "Timing Malfunction detected - AndroidClock and GPS time too far apart.";
    private static final String LOG_TAG = "TimeSync";

    /**
     *
     * @param sourceOfTruthTime - GPS or DMO time from TAB
     * @param tabRtc - RTC time from TAB
     * @return Null if we are unable to determine a source of truth, True if we are compliant, False if we are not
     */
    protected Bundle isTimingInCompliance(Date sourceOfTruthTime, Date tabRtc){
        boolean compliant;
        Bundle answer = new Bundle();
        long clockDiff;
        long tabTime = tabRtc != null ? tabRtc.getTime() : 0;
        boolean requiresSynchronization;
        if(sourceOfTruthTime != null){
            //Added this line in order to always set the ELD clock to sourceOfTruthTime. 54873
            eobrReader.Technician_SetClockUniversalTime(GlobalState.getInstance(), sourceOfTruthTime);
            clockDiff = sourceOfTruthTime.getTime() - tabTime;
            compliant = isWithinEldMandateThreshold(clockDiff);
            if(!compliant){
                Log.e(LOG_TAG, TAB_DMO_MSG);
                ErrorLogHelper.RecordMessage(TAB_DMO_MSG);
                DateUtility.SetSystemTime(sourceOfTruthTime);   //Added this line here based on Tom Harter's comment. 54873
                requiresSynchronization = true;
            }else{
                requiresSynchronization = false;
            }
        } else {
            clockDiff = tabTime - DateUtility.getCurrentDateTimeUTC().getTime();
            if(Math.abs(clockDiff) > MANDATE_CLOCK_SYNC_FAILURE_THRESHOLD){
                requiresSynchronization = false;
                compliant = false;
            }else{
                compliant = true;
                requiresSynchronization = false;
            }
        }
        answer.putLong("clockDifference", clockDiff);
        answer.putBoolean("isCompliant", compliant);
        answer.putBoolean("requiresSynchronization", requiresSynchronization);
        return answer;
    }

    public Bundle validateTimingMalfunction(Date sourceOfTruthTime, Date tabRtc) {
        Bundle answer = isTimingInCompliance(sourceOfTruthTime, tabRtc);
        Boolean isCompliant = answer.getBoolean("isCompliant", false);
        if (!isCompliant) {
            Log.i(LOG_TAG, " ELD Mandate Clock Sync Malfunction Detected");
            try {
                eldMandateController.createMalfunctionForLoggedInUsers(sourceOfTruthTime == null ? tabRtc : sourceOfTruthTime, Malfunction.TIMING_COMPLIANCE);
            } catch (Throwable e) {
                Log.e(LOG_TAG, "Error saving log for timing malfunction", e);
            }
        }
        return answer;
    }

    protected EmployeeLogEldMandateController getEldMandateController() {
        return eldMandateController;
    }

    protected EobrReader getEobrReader() {
        return eobrReader;
    }
}
