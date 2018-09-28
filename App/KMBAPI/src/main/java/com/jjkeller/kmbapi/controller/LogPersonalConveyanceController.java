package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.abstracts.LogSpecialDrivingController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogPersonalConveyanceController extends LogSpecialDrivingController {
    public static AtomicBoolean vehicleRestartedDuringPc = new AtomicBoolean(false);

    public LogPersonalConveyanceController(Context ctx){
        super(ctx);
    }

    @Override
    public EmployeeLogProvisionTypeEnum getDrivingCategory() {
        return EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE;
    }

    @Override
    public boolean getIsFeatureToggleEnabled() {
        return GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled();
    }

    @Override
    public boolean getIsInSpecialDutyStatus() {
        return GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
    }

    @Override
    public void setIsInSpecialDutyStatus(boolean value) {
        GlobalState.getInstance().setIsInPersonalConveyanceDutyStatus(value);
        if (value) {
            vehicleRestartedDuringPc.set(false);
        }
    }

    @Override
    public boolean getIsInSpecialDrivingSegment() {
        return GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment();
    }

    @Override
    public void setIsInSpecialDrivingSegment(boolean value) {
        GlobalState.getInstance().setIsInPersonalConveyanceDrivingSegment(value);
    }

    @Override
	public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog) {
        StartSpecialDrivingStatus(startTime, location, empLog, false, null);
	}

    @Override
    public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog, boolean forMidnightTransition, EmployeeLog previousLog) {
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            // only create PC records is the ELD Mandate Feature toggle is turned off
            super.StartSpecialDrivingStatus(startTime, location, empLog);
        }
        setIsInSpecialDrivingSegment(true);

        if(forMidnightTransition) {
            String annotation = "failed";
            if(previousLog != null) {
                EmployeeLogEldEvent lastPcEvent = EmployeeLogUtilities.GetLastEventInLog(previousLog, Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
                if(lastPcEvent != null && lastPcEvent.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared) {
                    annotation = lastPcEvent.getEventComment();
                }
            }

            IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

            try {
                empLogController.CheckForAndCreatePersonalConveyanceChangedEvent(empLog, startTime, location, annotation);
            }
            catch (Throwable t) {
                HandleException(new Exception(String.format("Failed to create an ELD event to start PC for time %s", startTime), t), "UnhandledCatch");
            }
        }
    }

    @Override
    public void DialogPreprocessEndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog) {
        try {
            IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.CreateDutyStatusChangedEvent(empLog, endTime, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), location, true, empLog.getRuleset(), null, null, false, null, null);
        } catch (Throwable e) {
            ErrorLogHelper.RecordException(getContext(), e, "Failed to create Off-Duty status change event in response to choosing YES to the End PC Dialog after after an ignition cycle.");
        }
    }

    @Override
	public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog)
	{
        EndSpecialDrivingStatus(endTime, location, empLog, false);
	}

    @Override
    public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog, boolean forMidnightTransition) {
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && getIsInSpecialDrivingSegment()) {
            // only end PC records if the ELD Mandate Feature toggle is turned off
            super.EndSpecialDrivingStatus(endTime, location, empLog);
        }

        if(!forMidnightTransition) {
            // note: turn off PC mode in the app.
            //       This is done so that if the user forgets to acknowledge the prompt confirming PC mode, that the default action is to end PC
            setIsInSpecialDutyStatus(false);
            setIsInSpecialDrivingSegment(false);
        }

        try {
            Location endLocation = EmployeeLogUtilities.GetLastEventInLog(empLog).getLocation();
            GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
            IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.CheckForAndCreateEndOfPCYMWT_Event(empLog, endTime, endLocation, forMidnightTransition);
        } catch (Throwable t) {
            HandleException(new Exception(String.format("Failed to create an ELD event to end PC for time %s", endTime), t), "UnhandledCatch");
        }
    }

    @Override
    public void EndSpecialDrivingStatus(EventRecord eventRecord, Location location, EmployeeLog empLog) {
        Date endTime = EmployeeLogUtilities.GetLastEventInLog(empLog).getEventDateTime();

        EndSpecialDrivingStatus(endTime, location, empLog);
    }

    @Override
    public void ProcessNewDutyStatus(DutyStatusEnum dutyStatus, Date time, Location location, EmployeeLog empLog, EventRecord eventRecord) {
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && eventRecord.getEventType() == EventTypeEnum.DRIVESTART) {
            EobrReader.getInstance().PublishDismissSpecialDrivingDialog(getDrivingCategory());
            if (vehicleRestartedDuringPc.compareAndSet(true,false)) {
                EobrReader.getInstance().PublishDismissDrivingView();
                EndSpecialDrivingStatus(eventRecord, location, empLog);
                setIsInSpecialDutyStatus(false);
            }
        }
        if (getIsInSpecialDutyStatus()) {
            super.ProcessNewDutyStatus(dutyStatus, time, location, empLog, eventRecord);
        }
    }

    @Override
    public void ProcessEvent(EventRecord eventRecord, Location location, EmployeeLog empLog) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()
            && getIsInSpecialDutyStatus() ) {

            if (eventRecord.getEventType() == EventTypeEnum.MOVE && vehicleRestartedDuringPc.compareAndSet(true, false)) {
                try {
                    ErrorLogHelper.RecordMessage(String.format("LogPersonalConveyanceController.ProcessEvent - Mandate Duty Status Change in response to MOVE event - IsInDriveOnPeriod: %b", GlobalState.getInstance().getIsInDriveOnPeriod()));
                    IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

                    DutyStatusEnum newStatus = GlobalState.getInstance().getIsInDriveOnPeriod() ? new DutyStatusEnum(DutyStatusEnum.DRIVING) : new DutyStatusEnum(DutyStatusEnum.ONDUTY);
                    Date dutyStatusTime = GlobalState.getInstance().getIsInDriveOnPeriod() ? eventRecord.getTimecodeAsDate() : new Date(eventRecord.getTimecodeAsDate().getTime() - 1000);

                    empLogController.CreateDutyStatusChangedEvent(empLog, dutyStatusTime, newStatus, location, true, empLog.getRuleset(), null, null, false, null, null);

                    EmployeeLogProvisionTypeEnum drvCat = getDrivingCategory();
                    EobrReader.getInstance().PublishDismissSpecialDrivingDialog(drvCat);
                    EndSpecialDrivingStatus(dutyStatusTime, location, empLog);
                    setIsInSpecialDutyStatus(false);
                } catch (Throwable t) {
                    HandleException(new Exception("Failed to create duty status change event in response to a MOVE event after an ignition cycle when ending PC", t), "UnhandledCatch");
                }
            }
            else if (eventRecord.getEventType() == EventTypeEnum.IGNITIONON) {
                ErrorLogHelper.RecordMessage("LogPersonalConveyanceController.ProcessEvent - Mandate process IGNITIONON by setting vehicleRestartedDuringPc=true & showing End PC dialog");
                vehicleRestartedDuringPc.set(true);
            }
        }

        super.ProcessEvent(eventRecord, location, empLog);
    }
}
