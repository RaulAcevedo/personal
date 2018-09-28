package com.jjkeller.kmbapi.controller.abstracts;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogWithProvisionsFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IReverseGeocodeLocationListener;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class LogSpecialDrivingController extends ControllerBase implements com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController {

    public LogSpecialDrivingController(Context ctx){
		super(ctx);
	}
    private boolean iSStartLocation = true;
	
	@Override
    public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog)
	{
        iSStartLocation = true;
        EmployeeLogWithProvisions provisionRecord = new EmployeeLogWithProvisions();
        if (location == null) {
            location = new Location();
            location.setGpsInfo(GlobalState.getInstance().getLastGPSLocation());
            location.setOdometerReading(GlobalState.getInstance().getLastValidOdometerReading());
        }
        if(location == null || location.getName() == null || location.getName().isEmpty()){
            location = getLocation(getCurrentGPSLocation());
        }

        provisionRecord.setProvisionTypeEnum(getDrivingCategory().getValue());
        provisionRecord.setStartTime(startTime);
        provisionRecord.setStartLocation(location);
        if (provisionRecord.getStartLocation().IsEmpty()){
            provisionRecord.getStartLocation().setName("-");
        }
        else if (provisionRecord.getStartLocation().getName() == null || provisionRecord.getStartLocation().getName().isEmpty()){
            provisionRecord.getStartLocation().setName(location.getGpsInfo().ToLocationString());
        }

        provisionRecord.setTractorNumber(EobrReader.getInstance().getEobrIdentifier());

        String debugMsg = String.format("%s driving period started at: %s odometer: %.1f tractorNumber: %s employee: %s",
                getDrivingCategory().getString(),
                DateUtility.getHomeTerminalDateTimeFormat12Hour().format(provisionRecord.getStartTime()),
                provisionRecord.getStartLocation().getOdometerReading(),
                provisionRecord.getTractorNumber(),
                empLog.getEmployeeId());
        Log.d("LogEvent", debugMsg);
        ErrorLogHelper.RecordMessage(debugMsg);

        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(this.getContext());
        facade.Save(provisionRecord, empLog);
        EmployeeLogEldEvent lastEventLog = EmployeeLogUtilities.GetLastEventInLog(empLog, Enums.EmployeeLogEldEventType.DutyStatusChange);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            AddMandateStartingLogRemark(empLog, lastEventLog);
        }
        LinkProvisionsToEldEvent(lastEventLog);
	}

    @Override
    public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog, boolean forMidnightTransition, EmployeeLog previousLog) {
        StartSpecialDrivingStatus(startTime, location, empLog);
    }

    @Override
    public void VerifySpecialDrivingEnd() {
        if(getIsInSpecialDutyStatus()) {
            if(getIsInSpecialDrivingSegment()) {
                EmployeeLog empLog = GlobalState.getInstance().getCurrentDriversLog();

                EndSpecialDrivingStatus(empLog, true);
            }
        }
    }

    @Override
    public void EndSpecialDrivingStatus(EventRecord eventRecord, Location location, EmployeeLog empLog)
    {
        EndSpecialDrivingStatus(empLog, false);
    }

    @Override
    public void EndSpecialDrivingStatus()
	{
        EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();

        EndSpecialDrivingStatus(empLog, false);
	}

    @Override
    public void EndSpecialDrivingStatus(EmployeeLog empLog, boolean setPotentialStopTime) {
	    Date endTime = this.getEndTime(empLog);

        if(setPotentialStopTime)
            GlobalState.getInstance().setPotentialDrivingStopTimestamp(endTime);

        this.EndSpecialDrivingStatus(endTime, null, empLog);
    }

    @Override
    public Date getEndTime(EmployeeLog log) {
        Date endTime;

	    String employeeId = log.getEmployeeId();
        User currentUser = GlobalState.getInstance().getCurrentUser();
        User currentDriver = GlobalState.getInstance().getCurrentDesignatedDriver();
        User user;

        if(currentUser != null && currentUser.getCredentials().getEmployeeId().equals(employeeId)) {
            user = currentUser;
        } else if(currentDriver != null && currentDriver.getCredentials().getEmployeeId().equals(employeeId)) {
            user = currentDriver;
        } else {
            user = GlobalState.getInstance().getLoggedInUser(employeeId);
        }

        if(user != null) {
            endTime = DateUtility.CurrentHomeTerminalTime(user);
        } else {
            endTime = DateUtility.getCurrentDateTimeWithSecondsUTC();
        }

        return endTime;
    }

    private Location getLocation(GpsLocation gpsLocation) {
        Location location = new Location();
        location.setGpsInfo(gpsLocation);
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            location.setName(gpsLocation.getDecodedInfo().LocationString());
        }
        location.setOdometerReading(GlobalState.getInstance().getLastValidOdometerReading());
        return location;
    }

    @Override
    public void DialogPreprocessEndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog) { }

    @Override
    public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog) {
        updateSpecialDrivingStatus(endTime, location, empLog);
    }

    @Override
    public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog, boolean forMidnightTransition) {
        EndSpecialDrivingStatus(endTime, location, empLog);
    }

    public void updateSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog) {
        iSStartLocation = false;
        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(this.getContext());
        EmployeeLogWithProvisions provisionRecord = facade.FetchMostRecentForLog(empLog, getDrivingCategory().getValue());
        if (provisionRecord == null) {
            // if we didn't find a record, something is wrong
            ErrorLogHelper.RecordMessage(String.format("Attempt to end a %s driving period, but no provision record found", getDrivingCategory().getString()));
            return;
        }
        if((location == null || location.getName() == null || location.getName().isEmpty()) && getCurrentGPSLocation() != null){
            location = getLocation(getCurrentGPSLocation());
        }
        provisionRecord.setProvisionTypeEnum(getDrivingCategory().getValue());
        provisionRecord.setEndTime(endTime);
        provisionRecord.setEndLocation(location);

        if (provisionRecord.getEndLocation().IsEmpty()){
            provisionRecord.getEndLocation().setName("-");
        }
        else if (provisionRecord.getEndLocation().getName() == null || provisionRecord.getEndLocation().getName().isEmpty()){
            provisionRecord.getEndLocation().setName(location.getName());
        }


        if (provisionRecord.getEndLocation().getEndOdometerReading() > provisionRecord.getEndLocation().getOdometerReading())
            // note: the GpsInfo supports both a starting and ending odometer.  Since we are *ending* something, try to transfer this value to the OdometerReading field.
            provisionRecord.getEndLocation().setOdometerReading(provisionRecord.getEndLocation().getEndOdometerReading());

        float startOdom = provisionRecord.getStartLocation().getOdometerReading();
        float endOdom = provisionRecord.getEndLocation().getOdometerReading();
        float totalDistance = endOdom - startOdom;
        provisionRecord.setTotalDistance(totalDistance);

        String debugMsg = String.format(Locale.US, "%s driving period ended startTime: %s endTime: %s startOdom: %.1f endOdom: %.1f dist: %.1f tractorNumber: %s employee: %s",
                getDrivingCategory().getString(),
                DateUtility.createHomeTerminalTimeString(provisionRecord.getStartTime(), false),
                DateUtility.createHomeTerminalTimeString(provisionRecord.getEndTime(), false),
                startOdom, endOdom, totalDistance, provisionRecord.getTractorNumber(), empLog.getEmployeeId());
        Log.d("LogEvent", debugMsg);
        ErrorLogHelper.RecordMessage(debugMsg);

        facade.Save(provisionRecord, empLog);
	}

    @Override
    public boolean SubmitAllSpecialDrivingItemsToDMO()
	{
        if(getIsFeatureToggleEnabled()) {
            boolean isSuccessful = false;

            if (this.getIsNetworkAvailable()) {
                try {
                    // first fetch all unsubmitted records
                    EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(this.getContext());
                    List<EmployeeLogWithProvisions> unsubmitted = facade.GetUnsubmitted(getDrivingCategory().getValue());

                    // are there any to send?
                    if (unsubmitted != null && unsubmitted.size() > 0) {
                        // second, attempt to send the entire list to DMO
                        EmployeeLogWithProvisions[] listToSend = unsubmitted.toArray(new EmployeeLogWithProvisions[unsubmitted.size()]);

                        RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                        rwsh.SubmitEmployeeLogWithProvisions(listToSend);

                        // third, mark all as submitted successfully
                        facade.MarkSubmitted(unsubmitted);
                    }

                    isSuccessful = true;
                } catch (JsonSyntaxException jse) {
                    this.HandleException(jse);
                } catch (IOException ioe) {
                    this.HandleException(ioe);
                }
            }

            return isSuccessful;
        }

        return true;
	}

    @Override
    public void ProcessNewDutyStatus(DutyStatusEnum dutyStatus, Date time, Location location, EmployeeLog empLog, EventRecord eventRecord) {
        if(getIsInSpecialDutyStatus()) {
            if(dutyStatus.getValue() == DutyStatusEnum.DRIVING) {
                StartSpecialDrivingStatus(time, location, empLog);
            } else {
                EndSpecialDrivingStatus(time, location, empLog);

                PublishSpecialDrivingEnd(eventRecord, location, empLog);
            }
        }
    }

    @Override
    public void ProcessEvent(EventRecord eventRecord, Location location, EmployeeLog empLog) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
            if (getIsInSpecialDutyStatus() && eventRecord.getEventType() == EventTypeEnum.IGNITIONON) {
                PublishSpecialDrivingEnd(eventRecord, location, empLog);
            }
        } else {
            if (getIsInSpecialDrivingSegment() && eventRecord.getEventType() == EventTypeEnum.DRIVEEND) {
                Date endTime = eventRecord.getTripReportData().getDataTimecodeAsDate();
                EndSpecialDrivingStatus(endTime, location, empLog);

                PublishSpecialDrivingEnd(eventRecord, location, empLog);
            }
        }
    }

    @Override
    public void PublishSpecialDrivingEnd(EventRecord eventRecord, Location location, EmployeeLog empLog) {
        EobrReader.getInstance().PublishVerifySpecialDrivingEnd(getDrivingCategory(), eventRecord, location, empLog);
    }

    @Override
    public void AddAobrdStartingLogRemark(EmployeeLog empLog, Date startTime, EmployeeLogEldEvent lastEventInLog) {
        IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		String logRemark = String.format("%s started at: %s%n", getDrivingCategory().getString(), DateUtility.getHomeTerminalTime12HourFormat().format(startTime));

		if(lastEventInLog.getLogRemark() != null && lastEventInLog.getLogRemark().length() > 0) {
            // there is already a log remark that exists, so add the new remark to the end
            lastEventInLog.setLogRemark(String.format("%s%s", lastEventInLog.getLogRemark(), logRemark));
        } else {
            lastEventInLog.setLogRemark(logRemark);
        }
		lastEventInLog.setLogRemarkDate(startTime);
        empLogCtrlr.SaveLocalEmployeeLog(empLog);
    }
    public void AddMandateStartingLogRemark(EmployeeLog empLog, EmployeeLogEldEvent lastEventInLog) {
        IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        lastEventInLog.setLogRemark(getDrivingCategory().getString());
        empLogCtrlr.SaveLocalEmployeeLog(empLog);
    }

    public void AddMandateEndingLogRemark(EmployeeLog empLog) {
        EmployeeLogEldEvent lastEventInLog = EmployeeLogUtilities.GetLastEventInLog(empLog, Enums.EmployeeLogEldEventType.DutyStatusChange);
        IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        lastEventInLog.setLogRemark("End of " + getDrivingCategory().getString());
        empLogCtrlr.SaveLocalEmployeeLog(empLog);
    }

    @Override
    public void LinkProvisionsToEldEvent(EmployeeLogEldEvent lastEventLog){
        EmployeeLogWithProvisionsFacade facadeLogWithProvisions = new EmployeeLogWithProvisionsFacade(getContext());
        EmployeeLogWithProvisions provisions = facadeLogWithProvisions.FetchLastLogWithProvisions();
        facadeLogWithProvisions.UpdateLogEldEventKey((int)provisions.getPrimaryKey(), (int)lastEventLog.getPrimaryKey());
    }

    private void UpdateCurrentProvisionRecord(Location location, EmployeeLog empLog, boolean iSStartLocation){
        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(this.getContext());
        EmployeeLogWithProvisions provisionRecord = facade.FetchMostRecentForLog(empLog, getDrivingCategory().getValue());
        if(iSStartLocation){
            provisionRecord.setStartLocation(location);
            provisionRecord.getStartLocation().setName(location.getGpsInfo().ToLocationString());
        }else{
            provisionRecord.setEndLocation(location);
            provisionRecord.getEndLocation().setName(location.getGpsInfo().ToLocationString());
        }
        facade.Save(provisionRecord, empLog);
    }

    private GpsLocation getCurrentGPSLocation() {
        EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
        GpsLocation gpsLocation = null;
        try {
            StatusRecord rec = new StatusRecord();
            // If we are connected to an EOBR then try and get the current data
            if (EobrReader.getInstance().getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
                EobrReader.getInstance().Technician_GetCurrentData(rec, false);

                // If there is a status record, then try and get the GPS information
                if (rec.IsGpsLocationValid() && rec.getGpsTimestampUtc().getTime() > 0) {
                    if (rec.getNorthSouthInd() == '-' || rec.getEastWestInd() == '-') {
                        // Gen2
                        gpsLocation = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getGpsLongitude());
                    } else {
                        // Gen1
                        gpsLocation = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getNorthSouthInd(), rec.getGpsLongitude(), rec.getEastWestInd());
                    }
                    IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                    empLogController.ReverseGeocodeLocationAsync(empLog, gpsLocation, new LogSpecialDrivingController.ReverseGeocodeAndUpdateCurrentProvisionRecord(getLocation(gpsLocation)));
                }
            }
        } catch (Exception ex) {
            Log.e("LogSpecialDrivingCont", "Error in getCurrentGPSLocation", ex);
        }
        return gpsLocation;
    }

    private class ReverseGeocodeAndUpdateCurrentProvisionRecord implements IReverseGeocodeLocationListener
    {
        private final Location location;
        public ReverseGeocodeAndUpdateCurrentProvisionRecord(final Location location)
        {
            this.location = location;
        }

        public void onResult(EmployeeLog log, GpsLocation location)
        {
            UpdateCurrentProvisionRecord(this.location, log, iSStartLocation);
        }
    }
}
