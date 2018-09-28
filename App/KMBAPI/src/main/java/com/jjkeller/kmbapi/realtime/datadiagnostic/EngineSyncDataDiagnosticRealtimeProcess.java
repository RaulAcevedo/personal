package com.jjkeller.kmbapi.realtime.datadiagnostic;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.realtime.RealtimeProcess;

import org.joda.time.DateTime;
import org.joda.time.Seconds;



import java.util.Date;

/**
 * For the data diagnostic event, an Engine Synchronization data diagnostic event should be generated when an engine synchronization failure
 * occurs.
 * If a data diagnostic event gets created and later an ERROR event is generated with a status of 0 (indicating the error state is over),
 * then a diagnostic event should be generated to clear the active diagnostic event.
 */
public class EngineSyncDataDiagnosticRealtimeProcess implements RealtimeProcess {

    private boolean _isInDataDiagnosticErrorState = false;
    EmployeeLogEldMandateController _eldMandateController;

    @Override
    public void setup() {
        _eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
    }

    // has EngineSync Error occurred within 5 seconds of an ELD Event needs?
    public boolean ErrorStopDateWithinFiveSecondsOfEventDate(DateTime errorStopDateTime, Date eventDate) {
        if (errorStopDateTime == null || eventDate == null) {
            return false;
        }

        DateTime eventDateTime = new DateTime(eventDate);
        if (eventDateTime == null) {
            return false;
        }

        // eventDateTime - errorStopDateTime = difference
        int seconds = Seconds.secondsBetween(errorStopDateTime, eventDateTime).getSeconds();
        return  seconds >= 0 && seconds <= 5;
    }

    /**
     * If a data diagnostic event gets created and later an ERROR event is generated with a status of 0 (indicating the error state is over),
     * then a diagnostic event should be generated to clear the active diagnostic event.
     */
    @Override
    public void processEvent(EventRecord eventRecord) {

        if (eventRecord.getEventType() != EventTypeEnum.ERROR)
            return;

        if(!_isInDataDiagnosticErrorState && (eventRecord.hasDataFlag(DataFlagEnums.ErrorEventFlags.VSS_FAULT) || eventRecord.hasDataFlag(DataFlagEnums.ErrorEventFlags.ODO_FAULT))){
            _isInDataDiagnosticErrorState = true;
            try {
                _eldMandateController.CreateDataDiagnosticEvent(GlobalState.getInstance().getCurrentEmployeeLog(), eventRecord.getTimecodeAsDate(), DataDiagnosticEnum.ENGINE_SYNCHRONIZATION, 0);
            } catch (Throwable throwable) {
                ErrorLogHelper.RecordException(throwable);
            }
            return;
        }

        if (_isInDataDiagnosticErrorState && (!eventRecord.hasDataFlag(DataFlagEnums.ErrorEventFlags.VSS_FAULT) && !eventRecord.hasDataFlag(DataFlagEnums.ErrorEventFlags.ODO_FAULT))) {

            _isInDataDiagnosticErrorState = false;

            try {
                _eldMandateController.CreateDataDiagnosticClearedEvent(GlobalState.getInstance().getCurrentEmployeeLog(), eventRecord.getTimecodeAsDate(), DataDiagnosticEnum.ENGINE_SYNCHRONIZATION, 0);
            } catch (Throwable throwable) {
                ErrorLogHelper.RecordException(throwable);
            }
        }

    }

    @Override
    public void post() {
    }
}
