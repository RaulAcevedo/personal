package com.jjkeller.kmbapi.realtime.malfunction;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.IDataFlag;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.realtime.RealtimeProcess;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by psb6176 on 2/27/17.
 */

public class EngineSyncMalfunctionRealtimeProcess implements RealtimeProcess {

    private ErrorAccumulator engineSyncErrorAccumulator;
    private EmployeeLog employeeLog;
    private EmployeeLogEldMandateController eldMandateController;
    private ITimeKeeper timeKeeper;

    private int malfunctionMinutes;
    private boolean malfunctionRecordedIn24hourPeriod;

    protected EngineSyncMalfunctionRealtimeProcess(int timeToMalfunction){
        malfunctionMinutes = timeToMalfunction;
    }

    @Override
    public void setup() {
        eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        employeeLog = GlobalState.getInstance().getCurrentEmployeeLog();
        engineSyncErrorAccumulator = GlobalState.getInstance().getEngineSyncErrors();
        timeKeeper = TimeKeeper.getInstance();

        // see if we are in an active malfunction state
        int key = employeeLog == null ? -1 : (int)employeeLog.getPrimaryKey();
        malfunctionRecordedIn24hourPeriod = eldMandateController.isMalfunctioning(key);
    }

    @Override
    public void processEvent(EventRecord eventRecord) {

        if (eventRecord.getEventType() != EventTypeEnum.ERROR){
            return;
        }

        if (hasNullDependencies()) {
            return;
        }

        engineSyncErrorAccumulator.processEvent(eventRecord);

        post();
    }

    @Override
    public void post() {

        if (EobrReader.getIsEobrDeviceReadingHistory() == true){
            return;
        }

        // check if is mandate
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() == false){
            return;
        }

        if (hasNullDependencies()){
            return;
        }

        // compare current datetime with the daily log start time
        // if driver has been working more than 24 hours without re-logging in
        // reset the error accumulation
        if (timeKeeper.getCurrentDateTime().isAfter(engineSyncErrorAccumulator.getWatchWindowStart().plusDays(1))) {

            // if error accumulation is more than the threshold, clear the malfunction event
            if (engineSyncErrorAccumulator.getErrorStop() == null && // if errorstop is null then we did not do a clear yet
                    engineSyncErrorAccumulator.getAccumulation().getStandardMinutes() >= Duration.standardMinutes(malfunctionMinutes).getStandardMinutes()) {

                // clear malfunction at the end of previous day
                String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                Date logEnd = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, timeKeeper.getCurrentDateTime().minusDays(1).toDate(), timeZone);
                clearMalfunction(logEnd);
            }

            resetErrorAccumulator();

            // reset the flag
            malfunctionRecordedIn24hourPeriod = false;

            return; // short circuit here
        }

        // check if the daily malfunction has occurred.  If it has then create the malfunction event
        if (malfunctionRecordedIn24hourPeriod == false &&
                engineSyncErrorAccumulator.getAccumulation().getStandardMinutes() >= Duration.standardMinutes(malfunctionMinutes).getStandardMinutes()){

            try {
                malfunctionRecordedIn24hourPeriod = true;
                eldMandateController.CreateMalfunctionELDEvent(employeeLog, timeKeeper.getCurrentDateTime().toDate(), Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE);
            } catch (Throwable throwable) {
                ErrorLogHelper.RecordException(throwable);
            }
        }
    }

    private void resetErrorAccumulator() {
        ErrorAccumulator newErrorAccumulator = new ErrorAccumulator(new DateTime(engineSyncErrorAccumulator.getWatchWindowStart().plusDays(1)),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timeKeeper);

        // replay the last event so we set the state correctly.
        newErrorAccumulator.processEvent(engineSyncErrorAccumulator.getPreviousEvent());

        setEngineSyncErrorAccumulator(newErrorAccumulator);
    }

    private void clearMalfunction(Date eventTime) {
        try {
            eldMandateController.CreateMalfunctionELDClearedEvent(employeeLog, eventTime, Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE);
        } catch (Throwable throwable) {
            ErrorLogHelper.RecordException(throwable);
        }
    }

    protected boolean hasNullDependencies(){
        if (engineSyncErrorAccumulator == null){ // might be null if event processed before ReadingHistory completed
            engineSyncErrorAccumulator = GlobalState.getInstance().getEngineSyncErrors();
        }

        return engineSyncErrorAccumulator == null || eldMandateController == null || employeeLog == null || timeKeeper == null;
    }

    protected void setEngineSyncErrorAccumulator(ErrorAccumulator errorAccumulator){
        engineSyncErrorAccumulator = errorAccumulator;
        GlobalState.getInstance().setEngineSyncErrors(errorAccumulator);
    }
}
