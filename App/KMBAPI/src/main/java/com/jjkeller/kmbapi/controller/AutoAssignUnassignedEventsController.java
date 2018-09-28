package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IAutoAssignedELDCalls;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.DriverCountResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by rab5795 on 7/18/18.
 */

public class AutoAssignUnassignedEventsController extends ControllerBase {
    private EobrReader eobrReader;
    private EmployeeLogEldEventFacade eldEventFacade;
    private int currentDriverId;
    private String currentEmployeeId;
    protected static final float THRESHOLD_MOVE_MPH = 1.0f;
    private IAutoAssignedELDCalls aaEldCalls;
    private List<UnidentifiedPairedEvents> unidentifiedPairedEvents = null;

    private EventRecord lastIdentifiedEventRecord = null;

    private static List<EventTypeEnum> eventTypeEnumListToDetermineLastIdentifiedEvent = Arrays.asList(new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED), new EventTypeEnum(EventTypeEnum.IGNITIONOFF),
            new EventTypeEnum(EventTypeEnum.IGNITIONON), new EventTypeEnum(EventTypeEnum.RPMOVERTHRESHOLD), new EventTypeEnum(EventTypeEnum.RPMUNDERTHRESHOLD), new EventTypeEnum(EventTypeEnum.SPEEDOVERTHRESHOLD),
            new EventTypeEnum(EventTypeEnum.SPEEDUNDERTHRESHOLD), new EventTypeEnum(EventTypeEnum.HARDBRAKE), new EventTypeEnum(EventTypeEnum.DRIVESTART), new EventTypeEnum(EventTypeEnum.DRIVEEND),
            new EventTypeEnum(EventTypeEnum.DRIVER), new EventTypeEnum(EventTypeEnum.MOVE));

    public AutoAssignUnassignedEventsController(Context ctx){
        super(ctx);
        eobrReader = EobrReader.getInstance();
        eldEventFacade = new EmployeeLogEldEventFacade(getContext(), GlobalState.getInstance().getCurrentUser());
    }

    public AutoAssignUnassignedEventsController(Context ctx, IAutoAssignedELDCalls aaEldCalls){
        super(ctx);
        eldEventFacade = new EmployeeLogEldEventFacade(getContext(), GlobalState.getInstance().getCurrentUser());
        currentDriverId = GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getDriverIdCrc();
        currentEmployeeId = GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getEmployeeId();
        this.aaEldCalls = aaEldCalls;
    }

    public void AutoAssignUnassignedEvents(){
        IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        unidentifiedPairedEvents = empLogController.LoadUnreviewedWithoutConfidenceLevelEvents();
        lastIdentifiedEventRecord = GetLastEventWithDriverId(unidentifiedPairedEvents);

        if (lastIdentifiedEventRecord == null || lastIdentifiedEventRecord.getDriverId() != currentDriverId) {
            UpdateUnidentifiedEventsToZeroConfidence();
            return;
        }

        // Process events in chronological order
        for (int i = unidentifiedPairedEvents.size() - 1; i >= 0; i--) {
            ProcessUnidentifiedEventPair(unidentifiedPairedEvents.get(i).startEvent, unidentifiedPairedEvents.get(i).endEvent);
        }
    }

    protected void ProcessUnidentifiedEventPair(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent) {
        boolean processed = false;

        processed = ProcessContinuousMovingScenario(startEvent, endEvent, lastIdentifiedEventRecord); // Scenario 1

        if (!processed) {
            DriverCountResponse driverCountResponse = aaEldCalls.GetDriverCount(subtractDaysFromEvent(startEvent, 7), startEvent.getEventDateTime().getTime());
            if (driverCountResponse == null || driverCountResponse.getDriverCount() == 0) {
                UpdateUnidentifiedEventsToZeroConfidence();
                return;
            }

            if (driverCountResponse.getDriverCount() == 1) {
                ProcessOneDriverIdScenario(startEvent, endEvent, driverCountResponse); // Scenario 2
            } else {
                // Check ignition status at time of last identified event
                EventRecord lastIgnitionEvent = aaEldCalls.GetLastIgnitionEvent(DateUtility.AddDays(lastIdentifiedEventRecord.getTimecodeAsDate(), -1).getTime(), lastIdentifiedEventRecord.getTimecode());
                if (lastIgnitionEvent == null || lastIgnitionEvent.getRecordId() <= 0 || lastIgnitionEvent.getEventType() == EventTypeEnum.IGNITIONOFF) {
                    UpdateUnidentifiedEventsToZeroConfidence();
                    return;
                }

                boolean ignitionOffEventExists = CheckForEvent(lastIdentifiedEventRecord.getTimecode(), startEvent.getEventDateTime().getTime(), new EventTypeEnum(EventTypeEnum.IGNITIONOFF));
                if (!ignitionOffEventExists) {
                    ProcessNoIgnitionOffSinceStop(startEvent, endEvent); // Scenario 3
                } else {
                    ProcessIgnitionOffSinceLastEventWithDriverId(startEvent, endEvent, lastIdentifiedEventRecord); // Scenario 4
                }
            }
        }
    }

    // Scenario 1: Continuous Vehicle Movement Since Last Driver Id Identified (100% confidence)
    protected boolean ProcessContinuousMovingScenario(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent, EventRecord lastIdentifiedEventRecord) {
        boolean processed = false;

        StatusRecord lastIdentifiedEventStatusRecord = aaEldCalls.GetStatusRecordForEobrId(lastIdentifiedEventRecord.getEobrId());
        lastIdentifiedEventRecord.setStatusRecordData(lastIdentifiedEventStatusRecord != null ? lastIdentifiedEventStatusRecord : null);

        // Verify that car is moving at time of last identified event
        if (lastIdentifiedEventStatusRecord != null && lastIdentifiedEventStatusRecord.getSpeedometerReadingMPH() > THRESHOLD_MOVE_MPH) {
            // If there hasn't been a stop event between the last identified event and start of unidentified event, 100% confident
            if (!CheckForEvent(lastIdentifiedEventRecord.getTimecode(), startEvent.getEventDateTime().getTime(), new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED))) {
                SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(startEvent, endEvent, 100, currentEmployeeId);
                processed = true;
            }
        }

        return processed;
    }

    // Scenario 2: Only one driver ID in last 8 days (90% confidence)
    protected boolean ProcessOneDriverIdScenario(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent, DriverCountResponse driverCountResponse) {
        boolean processed = false;

        if (driverCountResponse.getDriverIds()[0] == currentDriverId) {
            SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(startEvent, endEvent, 90, currentEmployeeId);
            processed = true;
        }

        return processed;
    }

    // Scenario 3: No Ignition Off Since Last Identified Event
    private void ProcessNoIgnitionOffSinceStop(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent) {
        if (lastIdentifiedEventRecord != null && lastIdentifiedEventRecord.getRecordId() > 0) {
            long differenceMillis = startEvent.getEventDateTime().getTime() - lastIdentifiedEventRecord.getTimecode();
            double timeSinceLastIdentifiedEvent = DateUtility.ConvertMillisecondsToMinutes(differenceMillis);
            int confidenceLevel = 0;

            if (timeSinceLastIdentifiedEvent < 15) {
                confidenceLevel = 80;
            } else if (timeSinceLastIdentifiedEvent >= 15 && timeSinceLastIdentifiedEvent < 30) {
                confidenceLevel = 70;
            } else if (timeSinceLastIdentifiedEvent >= 30 && timeSinceLastIdentifiedEvent < 60) {
                confidenceLevel = 60;
            } else if (timeSinceLastIdentifiedEvent >= 60 && timeSinceLastIdentifiedEvent < 120) {
                confidenceLevel = 50;
            } else if (timeSinceLastIdentifiedEvent >= 120 && timeSinceLastIdentifiedEvent < 240) {
                confidenceLevel = 30;
            } else if (timeSinceLastIdentifiedEvent >= 240 && timeSinceLastIdentifiedEvent < 480) {
                confidenceLevel = 10;
            }

            SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(startEvent, endEvent, confidenceLevel, currentEmployeeId);
        }
    }
    // Scenario 4: Ignition event found after the last event with driver id.
    // Calculate confidence value based on the amount of time since the last event with a driver id specified that matches the currently connected driver id.
    private void ProcessIgnitionOffSinceLastEventWithDriverId(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent, EventRecord lastIdentifiedEventRecord) {
        long differenceMillis = startEvent.getEventDateTime().getTime() - lastIdentifiedEventRecord.getTimecode();
        double timeSinceLastIdentifiedEvent = DateUtility.ConvertMillisecondsToMinutes(differenceMillis);

        int confidenceLevel = 0;

        if (timeSinceLastIdentifiedEvent < 15) {
            confidenceLevel = 70;
        } else if (timeSinceLastIdentifiedEvent >= 15 && timeSinceLastIdentifiedEvent < 30) {
            confidenceLevel = 60;
        } else if (timeSinceLastIdentifiedEvent >= 30 && timeSinceLastIdentifiedEvent < 60) {
            confidenceLevel = 50;
        } else if (timeSinceLastIdentifiedEvent >= 60 && timeSinceLastIdentifiedEvent < 120) {
            confidenceLevel = 30;
        } else if (timeSinceLastIdentifiedEvent >= 120 && timeSinceLastIdentifiedEvent < 240) {
            confidenceLevel = 10;
        } else if (timeSinceLastIdentifiedEvent >= 240 && timeSinceLastIdentifiedEvent < 480) {
            confidenceLevel = 5;
        }

        SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(startEvent, endEvent, confidenceLevel, currentEmployeeId);
    }

    public long subtractDaysFromEvent(EmployeeLogEldEvent event, int daysToSubtract){
        return DateUtility.AddDays(event.getEventDateTime(), -daysToSubtract).getTime();
    }

    // Check ELD for event between last identified event and start of unidentified event
    public boolean CheckForEvent(long startTimeCode, long endTimeCode, EventTypeEnum eventType) {
        boolean eventExists = false;

        EventRecord nextEvent = aaEldCalls.GetNextEvent(startTimeCode, eventType);

        if (nextEvent != null && nextEvent.getRecordId() > 0 && nextEvent.getTimecode() < endTimeCode) {
            eventExists = true;
        }

        return eventExists;
    }

    private void SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent, int confidenceLevel, String driverId){
        //Remove driver id when confidence level is 0
        if (confidenceLevel == 0) {
            driverId = null;
        }
        SaveConfidenceLevelAndSuggestedDriver(startEvent, confidenceLevel, driverId);
        SaveConfidenceLevelAndSuggestedDriver(endEvent, confidenceLevel, driverId);
    }

    private void SaveConfidenceLevelAndSuggestedDriver(EmployeeLogEldEvent event, int confidenceLevel, String driverId) {
        event.setUnidentifiedEventConfidenceLevel(confidenceLevel);
        event.setUnidentifiedEventSuggestedDriver(driverId);
        eldEventFacade.Save(event);
    }

    private EventRecord GetLastEventWithDriverId(List<UnidentifiedPairedEvents> unidentifiedPairedEvents) {
        EventRecord lastEventWithDriverId = null;
        if (!unidentifiedPairedEvents.isEmpty()) {
            UnidentifiedPairedEvents firstUnidentifiedPair = unidentifiedPairedEvents.get(unidentifiedPairedEvents.size() - 1);
            long startTimeForCalculation = subtractDaysFromEvent(firstUnidentifiedPair.startEvent, 7);
            lastEventWithDriverId = aaEldCalls.GetLastDriverEvent(startTimeForCalculation, firstUnidentifiedPair.startEvent.getEventDateTime().getTime(), eventTypeEnumListToDetermineLastIdentifiedEvent.toArray(new EventTypeEnum[eventTypeEnumListToDetermineLastIdentifiedEvent.size()]));
        }
        return lastEventWithDriverId;
    }

    private void UpdateUnidentifiedEventsToZeroConfidence(){
        if (unidentifiedPairedEvents != null) {
            for (int i = unidentifiedPairedEvents.size() - 1; i >= 0; i--) {
                EmployeeLogEldEvent start = unidentifiedPairedEvents.get(i).startEvent;
                EmployeeLogEldEvent end = unidentifiedPairedEvents.get(i).endEvent;
                if(start.getUnidentifiedEventConfidenceLevel() == null)
                    SaveConfidenceLevelAndSuggestedDriverForUnidentifiedPair(start, end, 0, null);
            }
        }
    }
}