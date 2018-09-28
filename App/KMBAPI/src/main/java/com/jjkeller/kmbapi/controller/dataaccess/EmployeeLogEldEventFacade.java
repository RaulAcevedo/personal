package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class EmployeeLogEldEventFacade extends FacadeBase {

    private EventSequenceIdGenerator eventSequenceIdGenerator;

    public EmployeeLogEldEventFacade(Context ctx, User user) {
        this(ctx, user, new EventSequenceIdGenerator(new ApplicationStateFacade(ctx)));
    }

    public EmployeeLogEldEventFacade(Context ctx, User user, EventSequenceIdGenerator eventSequenceIdGenerator) {
        super(ctx, user);
        this.eventSequenceIdGenerator = eventSequenceIdGenerator;
    }

    public void Save(EmployeeLogEldEvent eldEvent) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        persist.Persist(eldEvent);
    }

    public void PurgeUnidentifiedEvents(Date cutoffDate)
    {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        persist.PurgeUnidentifiedEvents(cutoffDate);
    }

    /**
     * Save multiple records at once so they are wrapped in the same transaction. All will succeed or fail together.
     */
    public void SaveListInSingleTransaction(EmployeeLogEldEvent[] eldEventList) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        persist.PersistListInSingleTransaction(eldEventList);
    }

    public int GetCertificationCountByLogKey(int logKey) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchCertificationCountByLogKey(logKey);
    }

    public List<EmployeeLogEldEvent> GetLoginDutyStatusEvents(String currentUserId) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchLoginDutyStatusEvents(currentUserId);
    }

    public EmployeeLogEldEvent[] GetByEventTypes(int employeeLogKey, List<Integer> eventTypes, List<Integer> eventRecordStatuses) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchByEventTypes(employeeLogKey, eventTypes, eventRecordStatuses);
    }

    /**
     * Gets all active malfunction or data diagnostic ELD events with a diagnostic code before a timestamp for the current user.
     * @param diagnosticCode The diagnostic code to search for
     * @param timestamp The exclusive timestamp to search before
     * @return A {@link List} of the ELD events
     */
    public List<EmployeeLogEldEvent> GetActiveByUserAndDiagnosticCode(String diagnosticCode, Date timestamp) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchActiveByUserAndDiagnosticCode(diagnosticCode, timestamp);
    }

    public EmployeeLogEldEvent FetchByKey(Integer uniqueKey) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchByKey(uniqueKey);
    }

    public EmployeeLogEldEvent[] FetchEldPCRecords(Integer uniqueKey) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchEldPCRecords(uniqueKey);
    }

    public EmployeeLogEldEvent FetchLastByEventType(int eventType) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchLastByEventType(eventType);
    }

    public EmployeeLogEldEvent FetchActiveByNaturalKey(Integer logKey, EmployeeLogEldEventType eventType, EmployeeLogEldEventCode eventCode, Date eventDateTime) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchActiveByNaturalKey(logKey, eventType.getValue(), eventCode.getValue(), eventDateTime);
    }

    public EmployeeLogEldEvent getMostRecentDriverIndicationEventByDriverID(String driverId) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchMostRecentDriverIndicationEventByDriverID(driverId);
    }

    public Date GetMostRecentDateforIgnitionOffbyDriverId(String driverId) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist(EmployeeLogEldEventPersist.class, this.getContext());
        return persist.FetchMostRecentDateforIgnitionOffByDriverId(driverId);
    }

    public Date GetMostRecentDateforIgnitionOnbyDriverId(String driverId) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist(EmployeeLogEldEventPersist.class, this.getContext());
        return persist.FetchMostRecentDateforIgnitionOnByDriverId(driverId);
    }

    public EmployeeLogEldEvent[] getPreviousWeekUnidentifiedEvents(String serialNumber) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchPreviousWeekUnidentifiedEvents(serialNumber);
    }

	public List<UnclaimedEventDTO> getUnidentifiedDrivingEventsByELD(String serialNumber, Date LogRangeStartTime){
		EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
		return persist.FetchUnidentifiedDrivingEventsByELD(serialNumber, LogRangeStartTime);
	}

	public Boolean isUnidentifiedDrivingDiagnosticEventByELDActive(String serialNumber, Date LogRangeStartTime){
		EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
		return persist.isUnidentifiedDrivingDiagnosticEventByELDActive(serialNumber, LogRangeStartTime);
	}

	public List<EmployeeLogEldEvent> GetUnreviewedUnidentifiedEvents() {
		EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
		return persist.FetchUnreviewedUnidentifiedEvents();
	}

    public List<EmployeeLogEldEvent> GetUnreviewedWithoutConfidenceLevelEvents(){
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchUnreviewedWithoutConfidenceLevelEvents();
    }

    public List<EmployeeLogEldEvent> GetUnsubmittedUnidentifiedEvents() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchUnsubmittedUnidentifiedEvents();
    }

    public List<EmployeeLogEldEvent> GetUnsubmittedUnidentifiedEventsToSubmitLogs() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchUnsubmittedUnidentifiedEventsToSubmitLogs();
    }

    public List<EmployeeLogEldEvent> GetUnsynchronizedUnidentifiedEvents() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchUnsynchronizedUnidentifiedEvents();
    }

    public List<EmployeeLogEldEvent> GetSubmittedUnidentifiedEvents() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.FetchSubmittedUnidentifiedEvents();
    }

    public void Update(EmployeeLogEldEvent empLogEldEvent) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        persist.UpdateEmployeeLogEldEvent(empLogEldEvent);
    }

    /**
     * Returns a List<EmployeeLogEldEvent> reconciled based on requested Enum
     * Enum.Original         - returns all Active Eld Events
     * Enum.Accept_Preview   - reconciles date overlaps to return a preview of what the Eld Events would look like if you Accepted all Change Requests
     * Enum.Accept_Database  - Accepts all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     * Enum.Reject_Database  - Rejects all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     */
    public List<EmployeeLogEldEvent> getReconcileChangeRequestedEldEvents(int employeeLogKey, com.jjkeller.kmbapi.calcengine.Enums.ReconcileChangeRequestedEldEventsEnum reconcileEnum) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext(), getCurrentUser(), Long.valueOf(employeeLogKey));
        List<EmployeeLogEldEvent> allLogEvents = persist.FetchList();

        if (reconcileEnum == Enums.ReconcileChangeRequestedEldEventsEnum.ORIGINAL)
            return getOriginalEldEvents(allLogEvents);
        else if (reconcileEnum == Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_PREVIEW)
            return previewChangeRequests(allLogEvents);
        else if (reconcileEnum == Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_DATABASE)
            return acceptChangeRequests(allLogEvents);
        else if (reconcileEnum == Enums.ReconcileChangeRequestedEldEventsEnum.REJECT_DATABASE)
            return rejectChangeRequests(allLogEvents);
        else
            return allLogEvents;
    }

    /**
     * Return all Active Eld Events
     */
    public List<EmployeeLogEldEvent> getOriginalEldEvents(List<EmployeeLogEldEvent> allEldEvents) {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        for (EmployeeLogEldEvent eldEvent : allEldEvents) {
            if (!isAValidEventTypeToEdit(eldEvent.getEventType()))
                continue;

            if (eldEvent.getEventRecordStatus() == 1 /*Active*/)
                results.add(eldEvent);
        }

        return results;
    }

    /**
     * Reconcile date overlaps to return a preview of what the Eld Events would look like if you Accepted all Change Requests
     */
    private List<EmployeeLogEldEvent> previewChangeRequests(List<EmployeeLogEldEvent> allEldEvents) {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        DutyStatusLists dutyStatusLists = prepareDutyStatusLists(allEldEvents);

        // Reverse iterate through active events to see if it's StartTime is between a ChangeRequest and it will become Inactive
        for (int i = dutyStatusLists.getActiveList().size() - 1; i >= 0; i--) {
            ActiveDutyStatusInfo active = dutyStatusLists.getActiveList().get(i);
            for (ActiveDutyStatusInfo changeRequest : dutyStatusLists.getChangeRequestsList()) {
                if (this.isEventOverlapping(active, changeRequest)) {
                    dutyStatusLists.getActiveList().remove(i);
                    break;
                }
            }
        }

        // Chronologically insert all ChangeRequests into the Active list
        for (ActiveDutyStatusInfo changeRequest : dutyStatusLists.getChangeRequestsList()) {
            dutyStatusLists.insertChronological(changeRequest, dutyStatusLists.getActiveList());
        }

        // The remaining events in our active list now are what we should provide as the "preview"
        for (ActiveDutyStatusInfo active : dutyStatusLists.getActiveList()) {
            results.add(active.getEldEvent());
        }

        return results;
    }

    private List<EmployeeLogEldEvent> acceptChangeRequests(List<EmployeeLogEldEvent> allEldEvents) {
        return eldEventsToUpdateInDatabase(handleChangeRequestsThatOverlapActiveEvents(allEldEvents, null));
    }

    public List<EmployeeLogEldEvent> eldEventsToUpdateInDatabase(List<EmployeeLogEldEvent> allEldEvents) {
        return eldEventsToUpdateInDatabase(allEldEvents, true, true);
    }

    public List<EmployeeLogEldEvent> eldEventsToUpdateInDatabase(List<EmployeeLogEldEvent> allEldEvents, boolean isFinalReconcile) {
        return eldEventsToUpdateInDatabase(allEldEvents, isFinalReconcile, true);
    }

    public List<EmployeeLogEldEvent> eldEventsToUpdateInDatabase(List<EmployeeLogEldEvent> allEldEvents, boolean isFinalReconcile, boolean isManuallyEdited) {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        // return only those records that have changes that need to be committed to the database
        for (EmployeeLogEldEvent changeRequest : allEldEvents) {
            if (changeRequest.getEventRecordStatus() == 1 /* Active */)
                continue;

            if (changeRequest.getEventRecordStatus() == 3 /* Inactive - Change Requests */) {
                changeRequest.setEventRecordStatus(1 /*Active*/);

                // Encompass edits may be Merged with existing edits so Sequence number will remain -2, in this case generate next sequence number
                if (changeRequest.getEventSequenceIDNumber() == EmployeeLogEldEnum.DEFAULT) {
                    changeRequest.setEventSequenceIDNumber(eventSequenceIdGenerator.GetNextSequenceNumber());
                }
            }

            if (isFinalReconcile) {
                changeRequest.setEditDuration(null);
            }
            changeRequest.setIsManuallyEditedByKMBUser(isManuallyEdited);
            results.add(changeRequest);
        }

        if (results.size() > 1) {
            // attempt to get the sequence number in sequential order by ordering the changed events chronological
            Collections.sort(results, new Comparator<EmployeeLogEldEvent>() {
                @Override
                public int compare(EmployeeLogEldEvent left, EmployeeLogEldEvent right) {
                    return left.getEventDateTime().compareTo(right.getEventDateTime());
                }
            });
        }

        return results;
    }

    public Boolean IsDataTransferMalfunctionEventActive() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.IsDataTransferMalfunctionEventActive();
    }

    public Boolean IsMalfunctionDataTransferComplianceActive(){
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, getContext());
        return persist.IsMalfunctionDataTransferComplianceActive();
    }

    /**
     * Inactive ELD Events due to new Change Requests
     * @param allEldEvents
     * @param eventStartTime
     * @return List<EmployeeLogEldEvent>
     */
    public List<EmployeeLogEldEvent> handleChangeRequestsThatOverlapActiveEvents(List<EmployeeLogEldEvent> allEldEvents, Date eventStartTime) {
        DutyStatusLists dutyStatusLists = prepareDutyStatusLists(allEldEvents);

        // iterate through Active events to see if it's StartTime is between a ChangeRequest so it should become Inactive
        for (ActiveDutyStatusInfo active : dutyStatusLists.getActiveList()) {
            if (eventStartTime != null) {
                if (active.getStartDateTime().compareTo(eventStartTime) >= 0) {
                    active.getEldEvent().setEventRecordStatus(com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                }
            } else {
                for (ActiveDutyStatusInfo changeRequest : dutyStatusLists.getChangeRequestsList()) {
                    // compareTo 0 = Date is equal; > 0 if Date is after argument
                    if (active._startDateTime.compareTo(changeRequest.getStartDateTime()) >= 0 && active._startDateTime.compareTo(changeRequest.getEndDateTime()) < 0) {
                        active.getEldEvent().setEventRecordStatus(2 /*Inactive - Changed*/);
                        break;
                    }

                }
            }

        }

        return allEldEvents;
    }

    private boolean isEventOverlapping(ActiveDutyStatusInfo active, ActiveDutyStatusInfo changeRequest) {
        // For active change in driver indication clear events, we need to ensure we inactivate when the end time equals the event time
        if (active.getEldEvent().getEventType() == EmployeeLogEldEventType.ChangeInDriversIndication
                && active.getEldEvent().getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared) {
            return active.getStartDateTime().compareTo(changeRequest.getStartDateTime()) >= 0 && active.getStartDateTime().compareTo(changeRequest.getEndDateTime()) <= 0;
        }
        else {
            return active.getStartDateTime().compareTo(changeRequest.getStartDateTime()) >= 0 && active.getStartDateTime().compareTo(changeRequest.getEndDateTime()) < 0;
        }
    }

    /**
     * Reject all Change Requests and return a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     */
    private List<EmployeeLogEldEvent> rejectChangeRequests(List<EmployeeLogEldEvent> allEldEvents) {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        for (EmployeeLogEldEvent eldEvent : allEldEvents) {
            if (!isAValidEventTypeToEdit(eldEvent.getEventType()))
                continue;

            if (eldEvent.getEventRecordStatus() == 3 /* Inactive – Change Requested */) {
                eldEvent.setEventRecordStatus(4 /*Inactive – Change Rejected*/);

                // Encompass edits may be Merged with existing edits so Sequence number will remain -2, in this case generate next sequence number
                if (eldEvent.getEventSequenceIDNumber() == EmployeeLogEldEnum.DEFAULT) {
                    eldEvent.setEventSequenceIDNumber(eventSequenceIdGenerator.GetNextSequenceNumber());
                }

                results.add(eldEvent);
            }
        }

        return results;
    }

    private boolean isAValidEventTypeToEdit(EmployeeLogEldEventType eventType) {
        //4.3.2.8.2. Driver Edit Limitations
        //	(a) An ELD must not allow or require the editing or manual entry of records with
        //		the following event types, as described in section 7.25 of this appendix:
        //
        //		Event Type Description
        //			2 An intermediate log,
        //			5 A driver’s login/logout activity,
        //			6 CMV’s engine power up / shut down, or
        //			7 ELD malfunctions and data diagnostic events
        return eventType == EmployeeLogEldEventType.DutyStatusChange
                || eventType == EmployeeLogEldEventType.ChangeInDriversIndication;
    }

    /**
     * Returns a list of Active and ChangeRequests Eld Events sorted in chronological order and calculated EndTimes.
     */
    private DutyStatusLists prepareDutyStatusLists(List<EmployeeLogEldEvent> allEldEvents) {
        DutyStatusLists results = new DutyStatusLists();

        for (EmployeeLogEldEvent eldEvent : allEldEvents) {
            if (!isAValidEventTypeToEdit(eldEvent.getEventType()))
                continue;

            if (eldEvent.getEventRecordStatus() != 1 /*Active*/ && eldEvent.getEventRecordStatus() != 3 /* Inactive – Change Requested */)
                continue;

            results.add(eldEvent);
        }

        results.calculateEndTimes();

        return results;
    }

    /**
     * Helper method that separates EldEvents into an Active and ChangeRequest list
     */
    public class DutyStatusLists {
        List<ActiveDutyStatusInfo> _active = new ArrayList<>();
        List<ActiveDutyStatusInfo> _changeRequests = new ArrayList<>();

        public List<ActiveDutyStatusInfo> getActiveList() {
            return _active;
        }

        public List<ActiveDutyStatusInfo> getChangeRequestsList() {
            return _changeRequests;
        }

        public void add(EmployeeLogEldEvent eldEvent) {
            insertChronological(new ActiveDutyStatusInfo(eldEvent));
        }

        /**
         * Calculate EndTime for each Eld Event so comparing overlap is easier.
         */
        public void calculateEndTimes() {
            String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(getContext()).getDailyLogStartTime();
            TimeZoneEnum homeTerminalTimeZoneEnum = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();

            int activeSize = _active.size();
            for (int i = 0; i < activeSize; i++) {
                // set End as the Start of the next Eld Event
                if (i < activeSize - 1)
                    _active.get(i)._endDateTime = _active.get(i + 1).getStartDateTime();
            }

            // force last event to go until end of log (23:59:59 if midnight is the log start time)
            if (activeSize > 0) {
                ActiveDutyStatusInfo lastDutyStatusInfo = _active.get(activeSize - 1);
                Date endDate = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStartTime, lastDutyStatusInfo.getStartDateTime(), homeTerminalTimeZoneEnum);
                lastDutyStatusInfo._endDateTime = endDate;
            }

            // ChangeRequest
            int changeRequestSize = _changeRequests.size();
            for (int i = 0; i < changeRequestSize; i++) {
                ActiveDutyStatusInfo dutyStatusInfo = _changeRequests.get(i);

                if (dutyStatusInfo.getEldEvent().getEditDuration() != null && dutyStatusInfo.getEldEvent().getEditDuration() > 0) {
                    // get end of log (23:59:59 for midnight start time)
                    Calendar calLogEnd = Calendar.getInstance();
                    calLogEnd.setTimeZone(homeTerminalTimeZoneEnum.toTimeZone());
                    calLogEnd.setTime(EmployeeLogUtilities.CalculateLogEndTime(dailyLogStartTime, dutyStatusInfo.getStartDateTime(), homeTerminalTimeZoneEnum));

                    // calculate end of event
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeZone(homeTerminalTimeZoneEnum.toTimeZone());
                    calendar.setTime(dutyStatusInfo.getStartDateTime());
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + dutyStatusInfo.getEldEvent().getEditDuration());

                    // EditDuration stores Minutes, force last event to go until end of log
                    if (calendar.get(Calendar.HOUR_OF_DAY) == calLogEnd.get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.MINUTE) == calLogEnd.get(Calendar.MINUTE)) {
                        calendar.set(Calendar.SECOND, calLogEnd.get(Calendar.SECOND));
                    }

                    DateTime logStartDate = new DateTime(EmployeeLogUtilities.CalculateLogStartTime(dailyLogStartTime, dutyStatusInfo.getStartDateTime(), homeTerminalTimeZoneEnum));
                    DateTime calendarTime = new DateTime(calendar.getTime());
                    if (calendarTime.isAfter(logStartDate.plusDays(1).minusSeconds(1))) {
                        dutyStatusInfo._endDateTime = logStartDate.plusDays(1).minusSeconds(1).toDate();
                    } else {
                        dutyStatusInfo._endDateTime = calendar.getTime();
                    }



                } else {
                    // the duration is 0, but we still need to have an end time, as not having one causes an error
                    dutyStatusInfo._endDateTime = dutyStatusInfo._startDateTime;
                }
            }
        }

        protected void insertChronological(ActiveDutyStatusInfo dutyStatusInfo) {
            insertChronological(dutyStatusInfo, dutyStatusInfo.getEldEvent().getEventRecordStatus() == 1 /*Active*/ ? _active : _changeRequests);
        }

        protected void insertChronological(ActiveDutyStatusInfo dutyStatusInfo, List<ActiveDutyStatusInfo> list) {
            // loop through all elements
            for (int i = 0; i < list.size(); i++) {
                // if the element you are looking at is smaller than go to the next element
                if (list.get(i).getEldEvent().getEventDateTime().before(dutyStatusInfo.getStartDateTime()))
                    continue;

                // otherwise, we have found the location to add
                list.add(i, dutyStatusInfo);
                return;
            }

            // we looked through all of the elements, and they were all smaller, so add to the end of the list
            list.add(dutyStatusInfo);
        }
    }

    /**
     * Helper class to hold StartTime and EndTime for a EmployeeLogEldEvent
     */
    public class ActiveDutyStatusInfo {
        private Date _startDateTime;
        private Date _endDateTime;
        private EmployeeLogEldEvent _eldEvent;

        public ActiveDutyStatusInfo(EmployeeLogEldEvent eldEvent) {
            _eldEvent = eldEvent;
            _startDateTime = eldEvent.getEventDateTime();
        }

        public Date getStartDateTime() {
            return _startDateTime;
        }

        public Date getEndDateTime() {
            return _endDateTime;
        }

        public EmployeeLogEldEvent getEldEvent() {
            return _eldEvent;
        }
    }

}
