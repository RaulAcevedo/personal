package com.jjkeller.kmbapi.realtime.malfunction;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.realtime.RealtimeProcess;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Position Malfunction Realtime Process
 *
 * Handles generating and clearing Position Malfunction Enum.
 * This is part of the ELD Mandate 4.6.1.4 Positioning Compliance Monitoring.
 *
 * Created by Charles Stebbins on 2/24/2017.
 */

public class PositionMalfunctionRealtimeProcess implements RealtimeProcess {

    private ErrorAccumulator gpsErrorAccumulator;
    private EmployeeLog employeeLog;
    private EmployeeLogEldMandateController eldMandateController;
    private ITimeKeeper timeKeeper;

    private int malfunctionMinutes;
    private boolean malfunctionRecordedIn24hourPeriod;

    protected PositionMalfunctionRealtimeProcess(int timeToMalfunction){
        malfunctionMinutes = timeToMalfunction;

    }

    @Override
    public void setup() {
        eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        employeeLog = GlobalState.getInstance().getCurrentEmployeeLog();
        gpsErrorAccumulator = GlobalState.getInstance().getGpsErrors();
        timeKeeper = TimeKeeper.getInstance();

        malfunctionRecordedIn24hourPeriod = eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE);
    }

    @Override
    public void processEvent(EventRecord eventRecord) {

        //This malfunction only cares about GPS errors.
        if (eventRecord.getEventType() != EventTypeEnum.GPS){
            return;
        }

        //We want to double check gpsErrorAccumulator so we will process the event if it's not null.
        if (hasNullDependencies() && gpsErrorAccumulator == null){
            return;
        }

        gpsErrorAccumulator.processEvent(eventRecord);

        post();
    }

    @Override
    public void post() {
        if (hasNullDependencies()){
            return;
        }

        //Check if we've gone past the 24 hour mark.
        //If we have, restart the ErrorAccumulator.
        if (timeKeeper.getCurrentDateTime().isAfter(gpsErrorAccumulator.getWatchWindowStart().plusDays(1))){
            ErrorAccumulator newGpsErrorAccumulator = new ErrorAccumulator(new DateTime(gpsErrorAccumulator.getWatchWindowStart().plusDays(1)),
                    new EventTypeEnum(EventTypeEnum.GPS),
                    DataFlagEnums.GpsEventFlags.GPS_FAULT,
                    timeKeeper);

            //Check the previous log, see if we need to put a cleared event on it.
            EmployeeLog previousLog = eldMandateController.GetLocalEmployeeLog(GlobalState.getInstance().getCurrentUser(), gpsErrorAccumulator.getWatchWindowStart().toDate());
            if (previousLog != null && eldMandateController.isMalfunctioning(previousLog, Malfunction.POSITIONING_COMPLIANCE)){
                try {
                    eldMandateController.clearMalfunctionForLoggedInUsers(gpsErrorAccumulator.getWatchWindowStart().toDate(), Malfunction.POSITIONING_COMPLIANCE);
                } catch (Throwable throwable) {
                    ErrorLogHelper.RecordException(throwable);
                }
            }

            //Replay the last event so we set the state correctly.
            newGpsErrorAccumulator.processEvent(gpsErrorAccumulator.getPreviousEvent());

            setGpsErrorAccumulator(newGpsErrorAccumulator);
        }

        //Checks if the daily malfunction has occurred.  If it has then raise the event.
        if (! malfunctionRecordedIn24hourPeriod &&
                gpsErrorAccumulator.getAccumulation().getStandardMinutes() >= Duration.standardMinutes(malfunctionMinutes).getStandardMinutes()){

            try {
                malfunctionRecordedIn24hourPeriod = true;
                eldMandateController.createMalfunctionForLoggedInUsers(timeKeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
            } catch (Throwable throwable) {
                //Create malfunction branch will keep being hit if it fails.
                ErrorLogHelper.RecordException(throwable);
            }
        }

        //Checks if the daily malfunction hasn't occurred, ensure that it's not logged.
        if (malfunctionRecordedIn24hourPeriod &&
                gpsErrorAccumulator.getAccumulation().getStandardMinutes() < Duration.standardMinutes(malfunctionMinutes).getStandardMinutes()){
            try {
                eldMandateController.clearMalfunctionForLoggedInUsers(timeKeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
            } catch (Throwable throwable) {
                //Clear malfunction branch will keep being hit if it fails to update the malfunction.
                ErrorLogHelper.RecordException(throwable);
            }
        }


    }

    /**
     * Check for null dependencies so we don't have a null pointer exception
     *
     * Exposed for testing.
     * @return true if there are null dependencies.
     */
    protected boolean hasNullDependencies(){
        if (eldMandateController == null) {
            eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        }
        employeeLog = GlobalState.getInstance().getCurrentEmployeeLog();
        gpsErrorAccumulator = GlobalState.getInstance().getGpsErrors();
        timeKeeper = TimeKeeper.getInstance();

        return gpsErrorAccumulator == null || eldMandateController == null || employeeLog == null || timeKeeper == null;
    }

    /**
     * Wrapping global state stuff in here so I can mock it.
     * @param errorAccumulator
     */
    protected void setGpsErrorAccumulator(ErrorAccumulator errorAccumulator){
        gpsErrorAccumulator = errorAccumulator;
        GlobalState.getInstance().setGpsErrors(errorAccumulator);
    }

}
