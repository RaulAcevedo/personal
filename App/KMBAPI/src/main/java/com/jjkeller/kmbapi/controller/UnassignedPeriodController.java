package com.jjkeller.kmbapi.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrHistoryEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.RoutePositionFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedDrivingPeriodFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedEobrFailurePeriodFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEnforceMinimumLengthStrategy;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.RoutePosition;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.proxydata.UnassignedEobrFailurePeriod;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UnassignedPeriodController extends ControllerBase {

    private final IEnforceMinimumLengthStrategy minimumLengthStrategy;

    public UnassignedPeriodController(Context ctx, IEnforceMinimumLengthStrategy enforceMinimumLengthStrategy) {
		super(ctx);

        minimumLengthStrategy = enforceMinimumLengthStrategy;
	}

    private ArrayList<UnassignedEobrFailurePeriod> _failurePeriodList = null;
    private ArrayList<UnassignedEobrFailurePeriod> getFailurePeriodList()
    {
        return _failurePeriodList;
    }
    private void setFailurePeriodList(ArrayList<UnassignedEobrFailurePeriod> list)
    {
    	_failurePeriodList = list;
    }
        
    public UnassignedDrivingPeriodResult ProcessHistoricalDrivingData(List<VehicleLocation> vehicleLocations, List<UnassignedDrivingPeriod> periods)
    {
		String eobrId = EobrReader.getInstance().getEobrIdentifier();
    	String eobrSerNo = EobrReader.getInstance().getEobrSerialNumber();
    	UnassignedDrivingPeriodFacade udpFacade = new UnassignedDrivingPeriodFacade(super.getContext());
    	RoutePositionFacade routeFacade = new RoutePositionFacade(super.getContext());    	
    	UnassignedDrivingPeriodResult result = new UnassignedDrivingPeriodResult();
		IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		EmployeeLogEldEvent eldEventDriveStart = null;
		EmployeeLogEldEvent eldEventDriveEnd = null;

		UnassignedDrivingPeriod udp = new UnassignedDrivingPeriod();
    	udp.setEobrId(eobrId);
    	udp.setEobrSerialNumber(eobrSerNo);
    	
    	ArrayList<RoutePosition> routePositions = new ArrayList<RoutePosition>();

    	for(VehicleLocation location : vehicleLocations)
    	{
    		if(location.getEventType() == EventTypeEnum.DRIVESTART)
    		{
    			udp.setStartTime(location.getGpsFix().getTimecodeAsDate());
    			udp.setStartOdometer(location.getOdometer());
    			udp.setStartLocation(GpsLocation.FromGpsFix(location.getGpsFix()));

				eldEventDriveStart = empLogController.CreateDriveOnOrOffUnassignedEvent(location, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.DrivingEvent);
			}
    		else if(location.getEventType() == EventTypeEnum.DRIVEEND)
    		{
    			if(location.getRecordId() > 0)
    			{
    				udp.setStopTime(location.getGpsFix().getTimecodeAsDate());
    				udp.setStopOdometer(location.getOdometer());
    				udp.setStopLocation(GpsLocation.FromGpsFix(location.getGpsFix()));
    				udp.setDistance(udp.getStopOdometer() - udp.getStartOdometer());

					eldEventDriveEnd = empLogController.CreateDriveOnOrOffUnassignedEvent(location, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.OnDutyEvent);
    			}
    			else //this is an orphaned period... nothing to do anymore
    			{
    				Log.d("HistoricalDrivingData", String.format("Detected orphaned driving period starting %s", DateUtility.getHomeTerminalDateTimeFormat12Hour().format(udp.getStartTime())));
    				break;
    			}
    		}
    		//If the ELD was powered down during a driving period before a DRIVE_OFF was generated
    		//the firmware will see a RESET event before it finds a DRIVE_OFF.  In that case,
    		//the record in index 1, where the DRIVE_OFF normally is, will be a RESET event.
    		else if(location.getEventType() == EventTypeEnum.TABRESET)
    		{
    			ShowDiagnostics(vehicleLocations, null);
    			
    			ErrorLogHelper.RecordMessage(
    				String.format("Detected driving period starting %s with RESET before DRIVE_OFF.  Ignoring.", 
    						DateUtility.getHomeTerminalDateTimeFormat12Hour().format(udp.getStartTime())
						)
					);
    			
    			UnassignedDrivingPeriodResult resetResult = new UnassignedDrivingPeriodResult();
    			resetResult.setDetectedAnyDrivingPeriods(true); //We found one, just ignoring for now.  Need to to set to true or reading history will stop.
    			
    			return resetResult;
    		}
    		
    		RoutePosition route = new RoutePosition();
    		route.setEobrId(eobrId);
    		route.setEobrSerialNumber(eobrSerNo);
			route.setGpsLatitude(location.getGpsFix().getLatitude());
			route.setGpsLongitude(location.getGpsFix().getLongitude());
    		route.setGpsTimestamp(location.getGpsFix().getTimecodeAsDate());
    		route.setOdometer(location.getOdometer());
    		
    		routePositions.add(route);
    		
    		GlobalState gs = GlobalState.getInstance();
    		
    		if(gs.getLastValidOdometerReading() < location.getOdometer())
    		{
    			//this is a more recent reading - update
    			gs.setLastGPSLocation(GpsLocation.FromGpsFix(location.getGpsFix()));
    			gs.setLastValidOdometerReading(location.getOdometer());
    		}
    	}
    	
    	if(udp.getStartTime() != null && udp.getStopTime() != null)
    	{
			//NOTE: Technically we detected this unassigned driving period, even if it fails
			//the minimum length check (immediately below).  We need to set that we detected
			//a period regardless of its length, otherwise we'll prematurely end reading history
			//upon encountering a short unassigned driving period.
			result.setDetectedAnyDrivingPeriods(true);

    		if(this.minimumLengthStrategy.execute(udp))
    		{
				if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					try {
						eldEventDriveStart.setDistance((int) udp.getStopOdometer() - (int) udp.getStartOdometer());
						eldEventDriveStart.setEobrSerialNumber(udp.getEobrSerialNumber());

						// reverse geocode using local db
						GpsLocation gpsLocation = udp.getStartLocation();
						empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
						if (gpsLocation != null) {
							eldEventDriveStart.setGeolocation(gpsLocation.ToLocationString());
						}
						eldEventDriveStart.setOdometer(udp.getStartOdometer());
						eldEventDriveStart.setEndOdometer(udp.getStopOdometer());

						empLogController.SaveDriveOnOrOffUnassignedEvent(eldEventDriveStart);
					} catch (Throwable e) {
						// Log the error and then proceed with the rest of our reading history process
						String eventType = new EventTypeEnum(eldEventDriveStart.getEventType().getValue()).toStringDisplay(getContext());
						Log.e("UnhandledCatch", String.format("Failed to create an DutyStatus ELD event for event record type %s", eventType), e);
					}

					try {
						eldEventDriveEnd.setEobrSerialNumber(udp.getEobrSerialNumber());
						// reverse geocode using local db
						GpsLocation gpsLocation = udp.getStopLocation();
						empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
						if (gpsLocation != null) {
							eldEventDriveEnd.setGeolocation(gpsLocation.ToLocationString());
						}
						eldEventDriveEnd.setOdometer(udp.getStopOdometer());
						empLogController.SaveDriveOnOrOffUnassignedEvent(eldEventDriveEnd);
					} catch (Throwable e) {
						// Log the error and then proceed with the rest of our reading history process
						String eventType = new EventTypeEnum(eldEventDriveEnd.getEventType().getValue()).toStringDisplay(getContext());
						Log.e("UnhandledCatch", String.format("Failed to create an DutyStatus ELD event for event record type %s", eventType), e);
					}
				}
				else
				{
					result.setUnassignedPeriod(udp);

					udpFacade.Save(udp);
					periods.add(udp);
				}

				routeFacade.Save(routePositions);

				ShowDiagnostics(vehicleLocations, udp);
			}
    	}
    	else if(udp.getStopTime() == null)
    	{
			//orphaned driving period - user currently driving
			result.setOrphanedTripTime(udp.getStartTime());
			result.setOrphanedLatitude(udp.getStartLocation().getLatitudeDegrees());
			result.setOrphanedLongitude(udp.getStartLocation().getLongitudeDegrees());
			result.setOrphanedOdometer(udp.getStartOdometer());

			ShowDiagnostics(vehicleLocations, udp);
    	}

    	return result;
    }
    
    public UnassignedDrivingPeriodResult ProcessUnassignedDrivingPeriods(List<UnassignedDrivingPeriod> periods)
    {
		LogEntryController ctrlr = new LogEntryController(getContext());
        EmployeeLog currentLog = ctrlr.getCurrentEmployeeLog();
        
        ShowDiagnostics(periods);
        
        MergeUnassignedDrivingPeriods();
        
        if(periods != null && periods.size() > 0)
        {
        	UnassignedDrivingPeriod udp = periods.get(periods.size() - 1); //get the last period
        	
        	if(udp != null && udp.getStopTime() != null)
        	{
        		EobrReader eobrReader = EobrReader.getInstance();
        		
        		//read the most recent stop and move events
        		//to determine if the vehicle is currently moving
        		EventRecord move = new EventRecord();
        		EventRecord stop = new EventRecord();
        		
        		int returnCode = eobrReader.GetLastEventOfType(move, new EventTypeEnum(EventTypeEnum.MOVE));
        		if(returnCode == EobrReturnCode.S_SUCCESS)
        		{
        			returnCode = eobrReader.GetLastEventOfType(stop,  new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED));
        			if(returnCode == EobrReturnCode.S_SUCCESS)
        			{
        				List<EventRecord> events = new ArrayList<EventRecord>();
        				events.add(move);
        				
        				// if stop event is after move event, add to list
        				if (stop.getTimecode() > move.getTimecode())
        					events.add(stop);
        				
        				UnassignedDrivingPeriod claimedPeriod = AttemptAutoClaimPeriods(periods, currentLog, events, true);
        				
        				if(claimedPeriod != null)
        				{
        					UnassignedDrivingPeriodFacade udpFacade = new UnassignedDrivingPeriodFacade(super.getContext());
        					udpFacade.Delete(claimedPeriod);
        					
        					//remove the period from the list to property calculate the result
        					periods.remove(claimedPeriod);
        				}
        			}
        			else
        				ErrorLogHelper.RecordMessage(String.format(Locale.getDefault(), "Unable to read latest stop event with code %d.", returnCode));
        		}
        		else
        			ErrorLogHelper.RecordMessage(String.format(Locale.getDefault(), "Unable to read latest move event with code %d.", returnCode));
        	}
        }
        
        UnassignedDrivingPeriodResult result = new UnassignedDrivingPeriodResult();
        result.setDetectedAnyDrivingPeriods(periods != null && periods.size() > 0);
        result.setDetectedDrivingPeriodsForCurrentLog(AnyDrivingPeriodsForCurrentLog(periods));
        result.setDetectedPreloginDrivingPeriods(AnyUnassignedDrivingPeriodsPriorToLogin(periods));
        
        return result;
    }
    
    private void MergeUnassignedDrivingPeriods()
    {
    	UnassignedDrivingPeriodFacade udpFacade = new UnassignedDrivingPeriodFacade(super.getContext());
    	List<UnassignedDrivingPeriod> allPeriods = udpFacade.FetchUnsubmitted(); //get all unclaimed, unsubmitted periods
    	HashMap<String, List<UnassignedDrivingPeriod>> map = new HashMap<String, List<UnassignedDrivingPeriod>>();
    	
    	//since we're reading unclaimed/unsubmitted periods from the database, it's possible
    	//they're coming from multiple EOBRs so we need to separate them.  Don't want to
    	//merge consecutive periods from 2 different trucks...
    	for(UnassignedDrivingPeriod period : allPeriods)
    	{
    		List<UnassignedDrivingPeriod> list = map.get(period.getEobrId());
    		
    		if(list == null)
    		{
    			list = new ArrayList<UnassignedDrivingPeriod>();
    			map.put(period.getEobrId(), list);
    		}
    		
    		list.add(period);
    	}
    	
    	for(List<UnassignedDrivingPeriod> periods : map.values())
    		MergeUnassignedDrivingPeriods(periods);
    }
    
    private void MergeUnassignedDrivingPeriods(List<UnassignedDrivingPeriod> periods)
    {
    	UnassignedDrivingPeriodFacade udpFacade = new UnassignedDrivingPeriodFacade(super.getContext());
    	
    	List<UnassignedDrivingPeriod> mergedPeriods = new ArrayList<UnassignedDrivingPeriod>();
    	List<UnassignedDrivingPeriod> wereMerged = new ArrayList<UnassignedDrivingPeriod>();
    	
    	if(periods != null && periods.size() > 0)
    	{
            Collections.sort(periods, new Comparator<UnassignedDrivingPeriod>() {
    			public int compare(UnassignedDrivingPeriod first, UnassignedDrivingPeriod second)
    			{
    				return first.getStartTime().compareTo(second.getStartTime());
    			}
    		});
            
            int i = 0;
            UnassignedDrivingPeriod period;
            UnassignedDrivingPeriod nextPeriod;
            
            do
            {
            	period = periods.get(i);
            	
            	//if we're not at the end
            	if(i < periods.size() - 1)
            		nextPeriod = periods.get(i + 1);
            	else
            		break;
            	
            	//we want to merge if the period ends in the same minute as the next period starts,
            	//or if the next period starts a little before the first period ends.  This can happen
            	//due to the logic that advances the stop time 1 minute if the start and stop times
            	//both fall in the same minute
            	if(period.getStopTime().compareTo(nextPeriod.getStartTime()) >= 0)
            	{
            		UnassignedDrivingPeriod mergedPeriod = new UnassignedDrivingPeriod();
            		mergedPeriod.setEobrId(period.getEobrId());
            		mergedPeriod.setEobrSerialNumber(period.getEobrSerialNumber());
            		mergedPeriod.setStartTime(period.getStartTime());
            		mergedPeriod.setStartLocation(period.getStartLocation());
            		mergedPeriod.setStartOdometer(period.getStartOdometer());
            		mergedPeriod.setStopTime(nextPeriod.getStopTime());
            		mergedPeriod.setStopLocation(nextPeriod.getStopLocation());
            		mergedPeriod.setStopOdometer(nextPeriod.getStopOdometer());
            		mergedPeriod.setDistance(mergedPeriod.getStopOdometer() - mergedPeriod.getStartOdometer());
            		
            		mergedPeriods.add(mergedPeriod);
            		wereMerged.add(period);
            		wereMerged.add(nextPeriod);
            		
            		//sometimes, we may merge 3+ consecutive periods together
            		//in this case, a "synthetic merged" period will be merged with
            		//an actual period that was read from the ELD.  We need to update
            		//the mergedPeriods list when this happens.
            		//
            		//We don't need to actually check if mergedPeriods contains period
            		//since the remove won't do anything if it doesn't, and checking
            		//would be another O(n) operation
            		mergedPeriods.remove(period);
            		
            		//replace period with the new merged period at the same index
            		periods.remove(i);
            		periods.add(i, mergedPeriod);
            		periods.remove(i + 1);
            		
            		//don't update i - we want to compare the new merged period (at i)
            		//against the next period (that was i + 2 originally)
            	}
            	else
            		i++;
            	
            }while(i < periods.size());
    	}
    	
    	if(mergedPeriods.size() > 0)
    	{
    		udpFacade.Save(mergedPeriods);
    		
    		for(UnassignedDrivingPeriod original : wereMerged)
    			udpFacade.Delete(original);
    		
    		ShowDiagnostics(mergedPeriods, wereMerged);
    	}
    }
    
    private void ShowDiagnostics(List<VehicleLocation>locations, UnassignedDrivingPeriod period)
    {
    	StringBuilder sb = new StringBuilder();
    	SimpleDateFormat formatter = DateUtility.getHomeTerminalDateTimeFormat12Hour();
    	
    	sb.append(String.format("ProcessHistoricalDriveData: Processing %d historical drive data records\r", locations.size()));
    	
    	int index = 1;
    	for(VehicleLocation location : locations)
    	{
    		sb.append(
    			String.format("  Rcd %03d: type: %02d eventTime: %s odometer: %f lat: %f long: %f\r",
    				index,
    				location.getEventType(),
    				formatter.format(location.getGpsFix().getTimecodeAsDate()),
    				location.getOdometer(),
    				location.getGpsFix().getLatitude(),
    				location.getGpsFix().getLongitude()
				)
			);
    		
    		index++;
    	}
    	
    	if(period != null)
    	{
    		sb.append(
    			String.format("Built unassigned driving period with startTime: %s startOdom: %f stopTime: %s stopOdom: %f\r",
					formatter.format(period.getStartTime()),
					period.getStartOdometer(),
					period.getStopTime() != null ? formatter.format(period.getStopTime()) : "null",
					period.getStopOdometer()
				)
			);
    	}
    	else
    		sb.append("Did not build unassigned driving period.");
    	
    	ErrorLogHelper.RecordMessage(sb.toString());
    }
    
    private void ShowDiagnostics(List<UnassignedDrivingPeriod> periods)
    {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(String.format("Reading history detected %d unassigned driving periods\r", periods.size()));
    	sb.append(GenerateDiagnostics(periods));
    	
    	ErrorLogHelper.RecordMessage(sb.toString());
    }
    
    private void ShowDiagnostics(List<UnassignedDrivingPeriod> mergedPeriods, List<UnassignedDrivingPeriod> originalPeriods)
    {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(String.format("The following %d unassigned driving periods were merged for EOBR %s\r", originalPeriods.size(), originalPeriods.get(0).getEobrId()));
    	sb.append(GenerateDiagnostics(originalPeriods));
    	sb.append(String.format("\rResulting in the following %d merged periods:\r", mergedPeriods.size()));
    	sb.append(GenerateDiagnostics(mergedPeriods));

    	ErrorLogHelper.RecordMessage(sb.toString());
    }
    
    private StringBuilder GenerateDiagnostics(List<UnassignedDrivingPeriod> periods)
    {
    	StringBuilder sb = new StringBuilder();
    	
    	int index = 1;
    	for(UnassignedDrivingPeriod period : periods)
    	{
    		sb.append(String.format(Locale.getDefault(), "  UnassignedDrivingPeriod %03d: startTime: %s startOdom: %f stopTime: %s stopOdom: %f\r",
    				index,
    				DateUtility.getHomeTerminalDateTimeFormat12Hour().format(period.getStartTime()),
    				period.getStartOdometer(),
    				DateUtility.getHomeTerminalDateTimeFormat12Hour().format(period.getStopTime()),
    				period.getStopOdometer()));
    		
    		index++;
    	}
    	
    	return sb;
    }
    
    public UnassignedDrivingPeriodResult ProcessEobrReaderHistoryGenII(ArrayList<EventRecord> currentEvents, EmployeeLog empLog) {
    	
        ArrayList<UnassignedDrivingPeriod> drivingPeriodList = new ArrayList<UnassignedDrivingPeriod>();
        UnassignedDrivingPeriod drivingPeriod = null;
        UnassignedDrivingPeriod lastRemovedDrivingPeriod = null;
        
        TripReport currentTripReport = null;
        Date orphanedTripTime = null;
        float orphanedOdometer = 0F;
        float orphanedLatitude = 0F;
        float orphanedLongitude = 0F;
        
        boolean foundDriveOn = false;
        boolean detectedDrivingPeriods = false;
        boolean detectedPreloginDrivingPeriods = false;
        
        // Make sure the events are sorted
        Collections.sort(currentEvents, new Comparator<EventRecord>() {
			public int compare(EventRecord event1, EventRecord event2)
			{
				return event1.getTimecode() <= event2.getTimecode() ? -1 : 1;
			}
		});
        
        // NOTE:  This assumes we cannot have two DRIVE_ON events in a row before a DRIVE_OFF event.
    	for(EventRecord currentEvent : currentEvents)  {

            this.minimumLengthStrategy.execute(drivingPeriod);

    		// We are only concerned with the Driving Events DRIVE_ON, DRIVE_OFF, and HOURLYTRIPRECORD here.
    		if (currentEvent.getEventType() == EventTypeEnum.DRIVESTART) {
    			currentTripReport = currentEvent.getTripReportData();
    			
    			if (currentTripReport != null) {
    				
    				// 2013.12.17 sjn Verify that this event record is not a continuation of the current period
    				Date eventDate = currentTripReport.getDataTimecodeAsDate();
    				
    				if(drivingPeriod != null && drivingPeriod.getStopTime() != null && drivingPeriod.getStopTime().getTime() == eventDate.getTime() )
    				{
    					// the new event record has the same timestamp as the previous period's ending time
    					// this new event signals a continuation of the previous udp that has already been created
                        ErrorLogHelper.RecordMessage(this.getContext(), String.format("Ignoring DriveOn event with same timestamp as previous period: '%1$s'", drivingPeriod.getStopTime().toString()));
                        
                        // keep a copy of this driving period available in case we get all the way through the event list and need to add it back
                        lastRemovedDrivingPeriod = new UnassignedDrivingPeriod();
                        lastRemovedDrivingPeriod.setEobrId(drivingPeriod.getEobrId());
                        lastRemovedDrivingPeriod.setEobrSerialNumber(drivingPeriod.getEobrSerialNumber());
                        lastRemovedDrivingPeriod.setStartTime(drivingPeriod.getStartTime());
                        lastRemovedDrivingPeriod.setStopTime(drivingPeriod.getStopTime());
                        lastRemovedDrivingPeriod.setStartLocation(drivingPeriod.getStartLocation());
                        lastRemovedDrivingPeriod.setStopLocation(drivingPeriod.getStopLocation());
                        lastRemovedDrivingPeriod.setStartOdometer(drivingPeriod.getStartOdometer());
                        lastRemovedDrivingPeriod.setStopOdometer(drivingPeriod.getStopOdometer());
                        lastRemovedDrivingPeriod.setDistance(drivingPeriod.getDistance());
                        
                        // remove the driving period from the list and reset the stop fields so that they can be populated when
                        // the real DriveOff is detected
                        drivingPeriodList.remove(drivingPeriod);
                        
                        drivingPeriod.setStopTime(null);
                        drivingPeriod.setStopLocation(null);
                        drivingPeriod.setStopOdometer(0);
                        drivingPeriod.setDistance(0);
                        
                        
    				}
    				else{
	    				// DRIVE_ON event marks the beginning of a new unassigned driving period.
	    				drivingPeriod = new UnassignedDrivingPeriod();
	    				
	    				// Set the "start" conditions of the unassigned driving period.
	    				BuildUnassignedPeriodsGenII(currentEvent, currentTripReport, drivingPeriod, true);    				
    				}
    				
    				foundDriveOn = true;
    				
    				orphanedTripTime = currentTripReport.getDataTimecodeAsDate();
    				orphanedOdometer = currentTripReport.getOdometer();
    				
    				if (currentTripReport.IsGpsLocationValid())
    				{
    					orphanedLatitude = currentTripReport.getLatitude();
    					orphanedLongitude = currentTripReport.getLongitude();
    				}
    			}
    			else {
    				String debugMessage = String.format("Trip Report not set for DRIVE_ON Event: Event Timestamp '%1$s' Trip Report ID '%2$d' Event Record ID '%3$d'", currentEvent.getTimecodeAsDate().toString(), currentEvent.getEventData(), currentEvent.getRecordId());
                    ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);
    			}
    		}
    		else if (currentEvent.getEventType() == EventTypeEnum.HOURLYTRIPRECORD && foundDriveOn) {
    			if (currentEvent.getTripReportData() != null && HasCrossedLogStartTime(drivingPeriod, currentEvent)) {
    				currentTripReport = currentEvent.getTripReportData();
    				
    				// Stop the current unassigned driving period
    				StopUnassignedPeriodGenII(currentEvent, currentTripReport, drivingPeriod);
    				if(drivingPeriod.getDistance() > 0.0 || drivingPeriod.getStopTime().getTime() - drivingPeriod.getStartTime().getTime() > 60000)
    					// ensure that either the driving period has some distance, or is at least a minute long
    					drivingPeriodList.add(drivingPeriod);	
    				
    				// Start a new unassigned driving period
    				drivingPeriod = new UnassignedDrivingPeriod();
    				StartUnassignedPeriodGenII(currentEvent, currentTripReport, drivingPeriod);
    				
    				foundDriveOn = true;
    				orphanedTripTime = currentTripReport.getDataTimecodeAsDate();
    				orphanedOdometer = currentTripReport.getOdometer();
    				if (currentTripReport.IsGpsLocationValid())
    				{
    					orphanedLatitude = currentTripReport.getLatitude();
    					orphanedLongitude = currentTripReport.getLongitude();
    				}
    			}
    		}
    		else if (currentEvent.getEventType() == EventTypeEnum.DRIVEEND && foundDriveOn) {
    			currentTripReport = currentEvent.getTripReportData();
    			
    			if (currentTripReport != null) {
    				// Set the "end" conditions of the unassigned driving period.
    				BuildUnassignedPeriodsGenII(currentEvent, currentTripReport, drivingPeriod, false);
    			
    				// Once we have the DRIVE_OFF event accounted for, we can add the new unassigned driving period to the ArrayList.
    				if(drivingPeriod.getDistance() > 0.0 || drivingPeriod.getStopTime().getTime() - drivingPeriod.getStartTime().getTime() > 60000)
    					// ensure that either the driving period has some distance, or is at least a minute long
    					drivingPeriodList.add(drivingPeriod);
    				foundDriveOn = false;
    				
    				orphanedTripTime = null;
    				orphanedOdometer = 0F;
    				orphanedLatitude = 0F;
    				orphanedLongitude = 0F;
    			}
    			else {
    				String debugMessage = String.format("Trip Report not set for DRIVE_OFF Event: Event Timestamp '%1$s' Trip Report ID '%2$d' Event Record ID '%3$d'", currentEvent.getTimecodeAsDate().toString(), currentEvent.getEventData(), currentEvent.getRecordId());
                    ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);    				
    			}
    		}
    	}
    	    	
    	if(foundDriveOn && drivingPeriod != null && lastRemovedDrivingPeriod != null && drivingPeriod.getStartTime().getTime() == lastRemovedDrivingPeriod.getStartTime().getTime())
    	{
    		// we have an outstanding DriveOn event where an unassigned period was removed from the list (i.e., we anticipated merging additional driving into it)
            // this can happen if a user starts driving before logging into KMB, then we'll get a DRIVE_OFF of the unknown driver, a DRIVE_ON for the user, and no 
    		// subsequent DRIVE_OFF if the user is still driving
    		ErrorLogHelper.RecordMessage(this.getContext(), String.format("Adding previously ignored event back into the list with start '%1$s' and stop '%2$s'", lastRemovedDrivingPeriod.getStartTime().toString(), lastRemovedDrivingPeriod.getStopTime().toString()));
    		drivingPeriodList.add(lastRemovedDrivingPeriod);
    	}
    	
    	ErrorLogHelper.RecordMessage(this.getContext(), String.format("Reading history detected %1$d unassigned driving periods", drivingPeriodList.size()));
    	
    	if (drivingPeriodList.size() > 0) {
	    		        
	        UnassignedDrivingPeriod claimedPeriod = AttemptAutoClaimPeriods(drivingPeriodList, empLog, currentEvents, false);
			
            if(claimedPeriod != null)
            	drivingPeriodList.remove(claimedPeriod);
            
	        // Persist any unassigned driving periods that may be have been created.
	        this.SaveUnassignedDrivingPeriods(drivingPeriodList);			
	        
            if (empLog != null) {
                // Determine if there are any driving periods that effect the current log.
                detectedDrivingPeriods = this.AnyDrivingPeriodsForCurrentLog(drivingPeriodList);
                detectedPreloginDrivingPeriods = this.AnyUnassignedDrivingPeriodsPriorToLogin(drivingPeriodList);
            }	        
	        
            // Generate the Route Positions for Unassigned Driving Periods.
            String eobrSerialNumber = EobrReader.getInstance().getEobrSerialNumber();
            String eobrTractorNumber = EobrReader.getInstance().getEobrIdentifier();
            
            if(!GlobalState.getInstance().getAbortReadingHistory())
            	this.ProcessRoutePositionsForUnassignedDrivingPeriodsGenII(eobrSerialNumber, eobrTractorNumber, drivingPeriodList);

            // 2014.07.16 sjn - I'm not sure this needs to be set to false here.   
            //                  The flag is reset whenever history starts reading, so this may not be necessary
            GlobalState.getInstance().setAbortReadingHistory(false);
    	}
    	    	
    	UnassignedDrivingPeriodResult result = new UnassignedDrivingPeriodResult();
    	result.setDetectedDrivingPeriodsForCurrentLog(detectedDrivingPeriods);
    	result.setDetectedPreloginDrivingPeriods(detectedPreloginDrivingPeriods);
    	result.setDetectedAnyDrivingPeriods(drivingPeriodList.size() > 0);
    	        
    	// We are doing this for the case in which a driving period has started, but the driver is NOT logged into KMB yet.  Once the driver
    	// logs into KMB, the "pre-login" driving time is created as an unassigned driving period and a new DRIVE_ON is fired.  Typically, this
    	// DRIVE_ON will happen during the "read history".  In that case the DRIVE_ON is "orphaned".  We keep track of it and send back to the
        // LogEntryController ProcessEobrReaderHistoryEventGenII method to deal with.        
        if (orphanedTripTime != null) {		    
		    result.setOrphanedTripTime(orphanedTripTime);
		    result.setOrphanedOdometer(orphanedOdometer);
		    result.setOrphanedLatitude(orphanedLatitude);
		    result.setOrphanedLongitude(orphanedLongitude);
        }
        
        return result;    	
    	
    }
    
	private UnassignedDrivingPeriod AttemptAutoClaimPeriods(List<UnassignedDrivingPeriod> drivingPeriodList, EmployeeLog empLog, List<EventRecord> currentEvents, boolean processLogTransition) {
		UnassignedDrivingPeriod claimedPeriod = null;
		
		// Auto-claim the period if possible.
		if (GlobalState.getInstance().getCurrentDesignatedDriver() != null) {
			boolean isVehicleMoving = IsVehicleStillMoving(currentEvents);
		    claimedPeriod = this.PerformAutoClaimDrivingPeriod(drivingPeriodList, GlobalState.getInstance().getCurrentDesignatedDriver(), empLog, isVehicleMoving, processLogTransition);
		                    
		    if (isVehicleMoving) {
		    	GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);
		    }
		}
		
		return claimedPeriod;
	}


    private boolean HasCrossedLogStartTime(UnassignedDrivingPeriod drivingPeriod, EventRecord nextEvent) {
    	TimeZoneEnum homeTerminalTimeZone = getCurrentUser().getHomeTerminalTimeZone();
    	Date periodLogStart = EmployeeLogUtilities.CalculateLogStartTime(getContext(), drivingPeriod.getStartTime(), homeTerminalTimeZone);
    	Date nextEventLogStart = EmployeeLogUtilities.CalculateLogStartTime(getContext(), nextEvent.getTimecodeAsDate(), homeTerminalTimeZone);
		return periodLogStart.getYear() != nextEventLogStart.getYear() || periodLogStart.getMonth() != nextEventLogStart.getMonth() || periodLogStart.getDate() != nextEventLogStart.getDate();
	}
    
	private void BuildUnassignedPeriodsGenII(EventRecord currentEvent, TripReport currentTripReport, UnassignedDrivingPeriod drivingPeriod, boolean isDriveStart) {
    	if (isDriveStart) {
    		StartUnassignedPeriodGenII(currentEvent, currentTripReport, drivingPeriod);
    	}
    	else {
    		StopUnassignedPeriodGenII(currentEvent, currentTripReport, drivingPeriod);
    	}
    }
    
	private void StartUnassignedPeriodGenII(EventRecord currentEvent, TripReport currentTripReport, UnassignedDrivingPeriod drivingPeriod) {
		// Set the Eobr ID and Serial Number.
        String eobrSerialNumber = EobrReader.getInstance().getEobrSerialNumber();
        String eobrTractorNumber = EobrReader.getInstance().getEobrIdentifier();
        
		drivingPeriod.setEobrId(eobrTractorNumber);
		drivingPeriod.setEobrSerialNumber(eobrSerialNumber);
		
		// Set the Start Time and Start Odometer.
		drivingPeriod.setStartTime(currentTripReport.getDataTimecodeAsDate());
		drivingPeriod.setStartOdometer(currentTripReport.getOdometer());
		
		// Get the GPS Location so we can set the StartLocation.
		GpsLocation gpsLoc = new GpsLocation(currentTripReport.getDataTimecodeAsDate(), currentTripReport.getLatitude(), currentTripReport.getLongitude());
		drivingPeriod.setStartLocation(gpsLoc);
	}
    
	private void StopUnassignedPeriodGenII(EventRecord currentEvent, TripReport currentTripReport, UnassignedDrivingPeriod drivingPeriod) {
		// Set the End Time and Start Odometer.
		drivingPeriod.setStopTime(currentTripReport.getDataTimecodeAsDate());
		drivingPeriod.setStopOdometer(currentTripReport.getOdometer());
		
		// Get the GPS Location so we can set the EndLocation.
		GpsLocation gpsLoc = new GpsLocation(currentTripReport.getDataTimecodeAsDate(), currentTripReport.getLatitude(), currentTripReport.getLongitude());
		drivingPeriod.setStopLocation(gpsLoc);
		
		// Calculate and set the Distance.
		drivingPeriod.setDistance(drivingPeriod.getStopOdometer() - drivingPeriod.getStartOdometer());
	}
    
    private boolean IsVehicleStillMoving(List<EventRecord> currentEvents) {
    	
		boolean isVehicleMoving = false;
		
		// Iterate in reverse through the current events trying to determine if the vehicle is moving.  Once determined, exit loop.
		for (int idx = currentEvents.size() - 1; idx >= 0; idx--) {
			EventRecord evt = currentEvents.get(idx);
			
			if (evt.getEventType() == EventTypeEnum.DRIVEEND || evt.getEventType() == EventTypeEnum.VEHICLESTOPPED) {
				isVehicleMoving = false;
				break;
			}
			else if (evt.getEventType() == EventTypeEnum.MOVE || evt.getEventType() == EventTypeEnum.DRIVESTART) {
				isVehicleMoving = true;
				break;
			}			
		}
		
		return isVehicleMoving;
    }
           
       
    /// <summary>
    /// Process the EOBR history information to build a list of
    /// unassigned driving periods for the EOBR device.  
    /// Answer if there are any unassigned driving periods that might exist
    /// for the current log as a result of this.
    /// </summary>
    /// <param name="e"></param>
    public Bundle ProcessEobrReaderHistoryEvent(EobrHistoryEventArgs e, User currentDriver, EmployeeLog empLog) throws KmbApplicationException
    {
        boolean detectedDrivingPeriods = false;
        boolean detectedPreloginDrivingPeriods = false;
		String unableToProcessMessage;
        List<StatusRecord> historyList = e.getHistoryList();
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			unableToProcessMessage = getContext().getString(R.string.unable_to_process_eld);
		} else {
			unableToProcessMessage = getContext().getString(R.string.unable_to_process_eobr);
		}
        if (historyList != null && historyList.size() > 0)
        {
            try
            {
                // successful read of EOBR status reported, and at least one history record to process
                String eobrSerialNumber = EobrReader.getInstance().getEobrSerialNumber();
                String eobrTractorNumber = EobrReader.getInstance().getEobrIdentifier();

                // build the list of unassigned periods for the history list
                // this will populate the lists DrivingPeriodList and FailurePeriodList
                List<UnassignedDrivingPeriod> newDrivingPeriodList = this.BuildUnassignedPeriods(currentDriver, empLog, eobrSerialNumber, eobrTractorNumber, historyList);

                boolean isVehicleMoving = historyList.get(historyList.size() - 1).getIsVehicleMoving();
                UnassignedDrivingPeriod claimedPeriod = this.PerformAutoClaimDrivingPeriod(newDrivingPeriodList, currentDriver, empLog, isVehicleMoving, false);
                
                if(claimedPeriod != null)
                	newDrivingPeriodList.remove(claimedPeriod);

                // persist any unassigned driving periods that may be have been created
                this.SaveUnassignedDrivingPeriods(newDrivingPeriodList);

                // persist any unassigned driving periods that may be have been created
                List<UnassignedEobrFailurePeriod> newFailurePeriodList = this.getFailurePeriodList();
                this.SaveUnassignedFailurePeriods(newFailurePeriodList);

                if (empLog != null)
                {
                    // determine if there are any driving periods that effect the current log
                    detectedDrivingPeriods = this.AnyDrivingPeriodsForCurrentLog(newDrivingPeriodList);
                    detectedPreloginDrivingPeriods = this.AnyUnassignedDrivingPeriodsPriorToLogin(newDrivingPeriodList);
                }

                // TODO generate the historical route positions from the driving periods
                // 6/8/11 JHM - commented this out while we're using simulated Eobr records/dashboard.
                this.ProcessHistoricalRoutePositions(eobrSerialNumber, eobrTractorNumber, newDrivingPeriodList);
            }
            catch (Exception excp)
            {
                ErrorLogHelper.RecordException(getContext(), excp);
                throw new KmbApplicationException(unableToProcessMessage +excp.getMessage());
            }
        }

        Bundle retVal = new Bundle();
        retVal.putBoolean(EobrReader.DETECTEDDRIVINGPERIODS, detectedDrivingPeriods);
        retVal.putBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS, detectedPreloginDrivingPeriods);
        return retVal;
    }
    
    /// <summary>
    /// Submit all of the unassigned periods (both driving and failure) to DMO.
    /// If successfully submitted to DMO, then Mark them as submitted.
    /// Answer if both driving and failure info was submitted successfully to DMO.
    /// </summary>
	public boolean SubmitUnassignedPeriodsToDMO(boolean excludeTodaysLog)
	{
        boolean isSuccessful = true;

        // load and submit the unassigned driving periods
        List<UnassignedDrivingPeriod> drivingList = this.LoadUnassignedDrivingPeriods(excludeTodaysLog);
        if (drivingList != null && drivingList.size() > 0)
        {
            boolean done = false;
            while (!done)
            {
                // send to DMO in batches if there are too many to send at one time
                int numToSend = 50;
                if( drivingList.size() < numToSend ) numToSend = drivingList.size();

				List<UnassignedDrivingPeriod> listToSend = drivingList.subList(0, numToSend);

				try
				{
					RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
					rwsh.SubmitUnassignedDrivingPeriods(listToSend.toArray(new UnassignedDrivingPeriod[listToSend.size()]));
					this.MarkUnassignedDrivingPeriodsAsSubmitted(listToSend);
				}
				catch (JsonSyntaxException e)
				{
					isSuccessful = false;
					this.HandleException(e, this.getContext().getString(R.string.submitunassignedperiodstodmo));
				}
				catch (IOException e)
				{
					isSuccessful = false;
					this.HandleException(e, this.getContext().getString(R.string.submitunassignedperiodstodmo));
				}

				// remove the ones from the list that were just sent
				for (int i = numToSend - 1; i >= 0; i--)
					drivingList.remove(i);

                // the operation is successful when all have been sent
                if (drivingList.size() == 0) isSuccessful = true;
                done = !isSuccessful || drivingList.size() == 0;
            }
        }

        // load and submit the unassigned failure list
        List<UnassignedEobrFailurePeriod> failureList = this.LoadUnassignedFailurePeriods();
        if (failureList != null && failureList.size() > 0)
        {
        	boolean done = false;
            while (!done)
            {
                // send to DMO in batches if there are too many to send at one time
				int numToSend = 50;
				if (failureList.size() < numToSend)
					numToSend = failureList.size();

				List<UnassignedEobrFailurePeriod> listToSend = failureList.subList(0, numToSend);

				try
				{
					RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
					rwsh.SubmitUnassignedEobrFailurePeriods(listToSend.toArray(new UnassignedEobrFailurePeriod[listToSend.size()]));
					this.MarkUnassignedFailurePeriodsAsSubmitted(listToSend);
				}
				catch (JsonSyntaxException e)
				{
					isSuccessful = false;
					this.HandleException(e, this.getContext().getString(R.string.submitunassignedperiodstodmo));
				}
				catch (IOException e)
				{
					isSuccessful = false;
					this.HandleException(e, this.getContext().getString(R.string.submitunassignedperiodstodmo));
				}

				// remove the ones from the list that were just sent
				for (int i = numToSend - 1; i >= 0; i--)
					failureList.remove(i);

				// the operation is successful when all have been sent
				if (drivingList.size() == 0)
					isSuccessful = true;
				done = !isSuccessful || failureList.size() == 0;
            }
        }

        return isSuccessful;
	}

    /// <summary>
    /// Create the list of unassigned driving periods for the EOBR using
    /// the list of history records.    
    /// When done, the lists will be stored in the following properties: 
    /// this.DriverPeriodList and this.FailurePeriodList
    /// </summary>
    /// <param name="eobrId">identifier of the EOBR that the list is for</param>
    /// <param name="historyList">list of history records to create unaasigned driving periods with</param>
    /// <returns></returns>
    private List<UnassignedDrivingPeriod> BuildUnassignedPeriods(User currentDriver, EmployeeLog empLog, String eobrSerialNumber, String eobrTractorNumber, List<StatusRecord> historyList)
    {
        Date lastTimestamp = null;
        float drivingStartOdometer = 0F;
        float lastOdometer = 0F;
        StringBuilder diagMessageSet = new StringBuilder();
        String diagMessage = null;
        	
        diagMessage = String.format("---- Build unassigned periods for '%s' item count: %d time now: '%s'\n", eobrSerialNumber, historyList.size(), TimeKeeper.getInstance().now().toString());
        Log.v("Unassigned Driving", diagMessage);
        diagMessageSet.append(diagMessage);

        ArrayList<UnassignedDrivingPeriod> drivingPeriodList = new ArrayList<UnassignedDrivingPeriod>();
        UnassignedDrivingPeriod drivingPeriod = null;

        ArrayList<UnassignedEobrFailurePeriod> failurePeriodList = new ArrayList<UnassignedEobrFailurePeriod>();
        UnassignedEobrFailurePeriod failurePeriod = null;

        // grab the first record and see if it starts within a driving period
        StatusRecord firstRecord = historyList.get(0);
        
        diagMessage = String.format("Hist 0. ts:%s e:%s s:%f o:%f status:%d\n", firstRecord.getTimestampUtc().toString(), firstRecord.getIsEngineRunning(), firstRecord.getSpeedometerReadingMPH(), firstRecord.getOdometerReadingMI(), firstRecord.getOverallStatus());
        Log.v("Unassigned Driving", diagMessage);
        diagMessageSet.append(diagMessage);

        // process the first record through the VMD
        VehicleMotionDetector vmd = new VehicleMotionDetector(getContext());
        vmd.ProcessStatusRecord(currentDriver, firstRecord);
        
        // did the first record indicate that the vehicle was moving
        if (vmd.getIsVehicleInMotion())
        {
            // the first record in the period is in driving period.
            // don't bother using the MotionDetector to confirm the start
            // just start it right now
            // The assumption here is that if the history list starts with a
            // driving event, that this event has already been confirmed.
            // The rationale is that if the first record is a driving record, then
            // the EOBR must have been unplugged from KMB while the truck was being driven.
            drivingPeriod = new UnassignedDrivingPeriod();
            drivingPeriod.setEobrId(eobrTractorNumber);
            drivingPeriod.setEobrSerialNumber(eobrSerialNumber);
            drivingPeriod.setStartTime(firstRecord.getTimestampUtc());
            if (firstRecord.IsGpsLocationValid())
            {
                try
                {
                    // get the GPS position from the status record
                    // if the GPS data is valid, then keep track of it for use later
                    GpsLocation gpsLoc = new GpsLocation(firstRecord.getGpsTimestampUtc(), firstRecord.getGpsLatitude(), firstRecord.getNorthSouthInd(), firstRecord.getGpsLongitude(), firstRecord.getEastWestInd());
                    drivingPeriod.setStartLocation(gpsLoc);
                }
                catch (IllegalArgumentException excp)
                {
                    // some aspect of the GPS data is invalid, record the error
                    ErrorLogHelper.RecordException(getContext(), excp);
                }
            }
            drivingStartOdometer = firstRecord.getOdometerReadingMI();
            lastTimestamp = firstRecord.getTimestampUtc();
            if( firstRecord.getOdometerReadingMI() > 0F ) 
                lastOdometer = firstRecord.getOdometerReadingMI();

            diagMessage = String.format("\tDRV start: %s od: %f\n", drivingPeriod.getStartTime().toString(), drivingStartOdometer);
            Log.v("Unassigned Driving", diagMessage);
            diagMessageSet.append(diagMessage);
        }

        // does the first record start with a failure?
        if (firstRecord.AnyDeviceFailuresDetected())
        {
            // first record is in failure
            // create the failure period record
            failurePeriod = new UnassignedEobrFailurePeriod();
            failurePeriod.setEobrId(eobrTractorNumber);
            failurePeriod.setEobrSerialNumber(eobrSerialNumber);
            failurePeriod.setStartTime(firstRecord.getTimestampUtc());
            failurePeriod.setStopTime(firstRecord.getTimestampUtc());
            String msg = String.format("Device failed with status of '0x%x'", firstRecord.getOverallStatus());
            failurePeriod.setMessage(msg);
            failurePeriodList.add(failurePeriod);

            diagMessage = String.format("\tFAILURE start: %s\n", failurePeriod.getStartTime().toString());
            Log.v("Unassigned Driving", diagMessage);
            diagMessageSet.append(diagMessage);
        }

        // look at each status record in the history list and process
        for (int index = 1; index < historyList.size(); index++)
        {
            StatusRecord statusRec = historyList.get(index);

            if (!statusRec.IsEmpty())
            {
                diagMessage = String.format("Hist %d. ts:%s e:%s s:%f o:%f status:%d\n", index, statusRec.getTimestampUtc().toString(), statusRec.getIsEngineRunning(), statusRec.getSpeedometerReadingMPH(), statusRec.getOdometerReadingMI(), statusRec.getOverallStatus());
                Log.v("Unassigned Driving", diagMessage);
                diagMessageSet.append(diagMessage);

                lastTimestamp = statusRec.getTimestampUtc();
                if(statusRec.getOdometerReadingMI() > 0F)
                    lastOdometer = statusRec.getOdometerReadingMI();

                // first, process the record through the VMD to see if
                // there is a confirmed start, or end, of a driving segment
                vmd.ProcessStatusRecord(currentDriver, statusRec);
                if (vmd.getIsConfirmedDrivingPeriodStart() && drivingPeriod == null)
                {
                    // found the confirmed start of a driving period
                    // create a new unassigned period, and load the start time
                    drivingPeriod = new UnassignedDrivingPeriod();
                    drivingPeriod.setEobrId(eobrTractorNumber);
                    drivingPeriod.setEobrSerialNumber(eobrSerialNumber);
                    drivingPeriod.setStartTime(vmd.getConfirmedDrivingStartTimestamp());
                    drivingPeriod.setStartLocation(vmd.getDrivingPeriodStartLocation());
                    drivingStartOdometer = vmd.getDrivingPeriodStartOdometer();

                    diagMessage = String.format("\tDRV start: %s od: %f\n", drivingPeriod.getStartTime(), drivingStartOdometer);
                    Log.v("Unassigned Driving", diagMessage);
                    diagMessageSet.append(diagMessage);

                }
                else if (vmd.getIsConfirmedDrivingPeriodStop() && drivingPeriod != null)
                {
                    // found the confirmed end of a driving period
                    drivingPeriod.setStopTime(vmd.getConfirmedDrivingStopTimestamp());
                    drivingPeriod.setStopLocation(vmd.getDrivingPeriodStopLocation());

                    // determine how much driving distance occurred, and 
                    // assign to the udp
                    drivingPeriod.setStartOdometer(drivingStartOdometer);
                    
                    if (vmd.getDrivingPeriodStopOdometer() > 0F)
                        drivingPeriod.setStopOdometer(vmd.getDrivingPeriodStopOdometer());
                    else
                        drivingPeriod.setStopOdometer(lastOdometer);

                    drivingPeriod.setDistance(drivingPeriod.getStopOdometer() - drivingPeriod.getStartOdometer());

                    // validate that the driving period starts and ends in a 
                    // different log segment.
                    // NOTE: driving periods that start and end in the same log event time period
                    //       are considered insignificant and are ignored
                    Date startSegmentTime = DateUtility.ConvertToPreviousLogEventTime(drivingPeriod.getStartTime());
                    Date stopSegmentTime = DateUtility.ConvertToPreviousLogEventTime(drivingPeriod.getStopTime());
                    if (startSegmentTime.compareTo(stopSegmentTime) < 0 && drivingPeriod.getDistance() > 0)
                    {
                        // validated that the driving period starts and ends in different log segments
                        // add this driving period to the list
                        drivingPeriodList.add(drivingPeriod);

                        diagMessage = String.format("\tDRV stop: %s dist: %f\n", drivingPeriod.getStopTime().toString(), drivingPeriod.getDistance());
                        Log.v("Unassigned Driving", diagMessage);
                        diagMessageSet.append(diagMessage);
                    }
                    else
                    {
                        // this is a driving period that starts and ends in the same
                        // log event time period.
                        diagMessage = String.format("\tDRV period ignored: %s\n", drivingPeriod.getStopTime().toString());
                        Log.v("Unassigned Driving", diagMessage);
                        diagMessageSet.append(diagMessage);
                    }

                    // start a new driving period
                    drivingPeriod = null;
                }

                // second, process any failures that may be present in the EOBR 
                if (statusRec.AnyDeviceFailuresDetected())
                {
                    // failures were detected on the status record
                    if (failurePeriod == null)
                    {
                        // no failure is currently started yet, so create one
                        failurePeriod = new UnassignedEobrFailurePeriod();
                        failurePeriod.setEobrId(eobrTractorNumber);
                        failurePeriod.setEobrSerialNumber(eobrSerialNumber);
                        failurePeriod.setStartTime(statusRec.getTimestampUtc());
                        String msg = String.format("Device failed with status of '0x%x'", statusRec.getOverallStatus());
                        failurePeriod.setMessage(msg);
                        failurePeriodList.add(failurePeriod);
                    }
                    else
                    {
                        // if there is an open failure, update the stop time
                        failurePeriod.setStopTime(statusRec.getTimestampUtc());
                    }
                }
                else
                {
                    // remove any failures that might be in process right now 
                    // because this status record did not indicate a failure
                    if (failurePeriod != null)
                    {
                        // if there is an open failure, update the stop time
                        failurePeriod.setStopTime(statusRec.getTimestampUtc());
                        failurePeriod = null;
                    }
                }
            }
        }

        if (drivingPeriod != null)
        {
            // boundary condition where a driving period was started, but the stop
            // has not been confirmed yet by the VMD, so stop it now
            // try to use the VMD time, or if not available, use the last timestamp seen
            drivingPeriod.setStopTime(vmd.getPotentialDrivingStopTimestamp());
            drivingPeriod.setStopLocation(vmd.getDrivingPeriodStopLocation());
            if (drivingPeriod.getStopTime() == null)
            {
                // this shouldn't happen, but as a safe-guard use the most 
                // recent timestamp to make sure that something is assigned.
                drivingPeriod.setStopTime(lastTimestamp);
            }

            // determine how much driving distance occurred, and 
            // assign to the udp
            drivingPeriod.setStartOdometer(drivingStartOdometer);
            drivingPeriod.setStopOdometer(lastOdometer);
            drivingPeriod.setDistance(drivingPeriod.getStopOdometer() - drivingPeriod.getStartOdometer());

            // validate that the driving period starts and ends in a 
            // different log segment
            // NOTE: driving periods that start and end in the same log event time period
            //       are considered insignificant and are ignored
            Date startSegmentTime = DateUtility.ConvertToPreviousLogEventTime(drivingPeriod.getStartTime());
            Date stopSegmentTime = DateUtility.ConvertToPreviousLogEventTime(drivingPeriod.getStopTime());
            if (startSegmentTime.compareTo(stopSegmentTime) < 0 && drivingPeriod.getDistance() > 0F)
            {
                // validated that the driving period starts and ends in different log segments
                // add this driving period to the list
                drivingPeriodList.add(drivingPeriod);

                diagMessage = String.format("\tDRV stop: %s dist: %f\n", drivingPeriod.getStopTime().toString(), drivingPeriod.getDistance());
                Log.v("Unassigned Driving", diagMessage);
                diagMessageSet.append(diagMessage);
            }
            else
            {
                diagMessage = String.format("\tDRV period ignored: %s\n", drivingPeriod.getStopTime().toString());
                Log.v("Unassigned Driving", diagMessage);
                diagMessageSet.append(diagMessage);
            }
        }

        if (failurePeriod != null)
        {
            // if there is an open failure, update the stop time
            failurePeriod.setStopTime(lastTimestamp);
            failurePeriod = null;
        }

        diagMessage = String.format("---- done, time now: '%s' ----\n", TimeKeeper.getInstance().now().toString());
        Log.v("Unassigned Driving", diagMessage);
        diagMessageSet.append(diagMessage);

        if (drivingPeriodList.size() > 0 || failurePeriodList.size() > 0)
        {
            ErrorLogHelper.RecordMessage(getContext(), diagMessageSet.toString());
        }

        // save the lists
        this.setFailurePeriodList(this.RemoveInsignificantItems(failurePeriodList));
        
        return drivingPeriodList;
    }
    
    /// <summary>
    /// Persist the unassigned driving period list.
    /// </summary>
    /// <param name="unassignedDrivingPeriods"></param>
    private void SaveUnassignedDrivingPeriods(List<UnassignedDrivingPeriod> unassignedDrivingPeriods)
    {
        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(), this.getCurrentUser());
        facade.Save(unassignedDrivingPeriods);
    }
    
    /// <summary>
    /// Answer a list of existing unassigned driving periods for a day.
    /// If nothing already exists, then null will be returned.
    /// </summary>
    /// <returns></returns>
	private List<UnassignedDrivingPeriod> LoadUnassignedDrivingPeriods(Date date)
	{
        List<UnassignedDrivingPeriod> answer = null;

        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(), this.getCurrentUser());
        answer = facade.FetchUnsubmittedByDate(date);
        return answer;
	}

	/// <summary>
    /// Answer a list of all existing unassigned driving periods.
    /// If nothing already exists, then null will be returned.
    /// </summary>
    /// <returns></returns>
	private List<UnassignedDrivingPeriod> LoadUnassignedDrivingPeriods( boolean excludeTodaysLog)
	{
        List<UnassignedDrivingPeriod> answer = null;

        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(), this.getCurrentUser());
        answer = facade.Fetch();

        // 2/24/12 JHM - both timestamps in the unassigned period are in UTC.  No conversion needed.
        if (answer != null && answer.size() > 0)
        {
            // get current users home terminal time
            Date todaysDate;
            todaysDate = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
            todaysDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), todaysDate, this.getCurrentUser().getHomeTerminalTimeZone());

            for (UnassignedDrivingPeriod udp : answer)
            {
                if (excludeTodaysLog)
                {
                    Date startTime = udp.getStartTime();
                    Date stopTime = udp.getStopTime();

                    if (DateUtility.getHomeTerminalDateFormat().format(todaysDate).matches(DateUtility.getHomeTerminalDateFormat().format(startTime))
                    		|| DateUtility.getHomeTerminalDateFormat().format(todaysDate).matches(DateUtility.getHomeTerminalDateFormat().format(stopTime)))
                    {
                        // remove this unassigned event because either the start or end date matches
                        // today's date
                        answer.remove(udp);

                        // if removed last unassigned event from list, break out of loop
                        if (answer.size() == 0)
                            break;
                    }
                }
            }
        }

        return answer;
	}

	private void MarkUnassignedDrivingPeriodsAsSubmitted( List<UnassignedDrivingPeriod> unassignedDrivingPeriodList)
	{
        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(), this.getCurrentUser());
        facade.MarkAsSubmitted(unassignedDrivingPeriodList);
	}

	public void MarkUnassignedDrivingPeriodsAsClaimed(UnassignedDrivingPeriod period)
	{
        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(), this.getCurrentUser());
        facade.MarkAsClaimed(period);
	}

    /// <summary>
    /// Answer if there are any driving periods in the list that
    /// occur after the current log was initially created.
    /// </summary>
    /// <param name="list"></param>
    /// <returns></returns>
    private boolean AnyDrivingPeriodsForCurrentLog(List<UnassignedDrivingPeriod>list)
    {
        boolean answer = false;

        if (list != null)
        {
            // fetch the current log
            LogEntryController ctrlr = new LogEntryController(getContext());
            EmployeeLog empLog = ctrlr.getCurrentEmployeeLog();
            Date mobileLogStartTimestamp = empLog.getMobileStartTimestamp();

            // 11/18/14 JHM - Check for null timestamp before doing CompareTo
            if(mobileLogStartTimestamp == null) return false;
            
            for(UnassignedDrivingPeriod udp : list)
            {
                if (udp.getStartTime().compareTo(mobileLogStartTimestamp) >= 0)
                {
                    // 2014.12.02 sjn - Add checking against the end date of the period.
                    //                  This prevents the scenario where a midnight unassigned event may be claimable in KMB when the current log
                    //                  has not been transitioned across midnight yet
            
                    //period end time belongs on the current log (not on a future log)                	
                    if(ctrlr.BelongOnFutureLog(this.getCurrentUser(), empLog, udp.getStopTime()) == false)
                    {
                        answer = true;
                        break;
                    }
                }
            }
        }

        return answer;
    }
    
    /// <summary>
    /// Answer if there are any unassigned driving periods that occured for
    /// the current day's log, but prior to login
    /// </summary>
    /// <returns></returns>
    public boolean AnyUnassignedDrivingPeriodsPriorToLogin(List<UnassignedDrivingPeriod> list)
    {
        boolean answer = false;

        if (list != null)
        {
            // fetch the current log
            LogEntryController ctrlr = new LogEntryController(getContext());
            EmployeeLog empLog = ctrlr.getCurrentEmployeeLog();
            Date mobileLogStartTimestamp = empLog.getMobileStartTimestamp();
            Date logStartTimestamp = EmployeeLogUtilities.CalculateLogStartTime(getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone());

            // 11/18/14 JHM - Check for null timestamps before doing CompareTo
            if (mobileLogStartTimestamp == null || logStartTimestamp == null) return false;
            
            for (UnassignedDrivingPeriod udp : list)
            {
                if (udp.getStartTime().compareTo(logStartTimestamp) >= 0 &&
                    udp.getStartTime().compareTo(mobileLogStartTimestamp) <= 0)
                {
                    // 2014.12.02 sjn - Add checking against the end date of the period.
                    //                  This prevents the scenario where a midnight unassigned event may be claimable in KMB when the current log
                    //                  has not been transitioned across midnight yet
            
                    //period end time belongs on the current log (not on a future log)                	
                    if(ctrlr.BelongOnFutureLog(this.getCurrentUser(), empLog, udp.getStopTime()) == false)
                    {
                        answer = true;
                        break;
                    }                	
                }
            }
        }

        return answer;
    }
    
    /// <summary>
    /// Persist the unassigned Failure period list.
    /// </summary>
    /// <param name="unassignedFailurePeriods"></param>
    private void SaveUnassignedFailurePeriods(List<UnassignedEobrFailurePeriod> unassignedFailurePeriods)
    {
        UnassignedEobrFailurePeriodFacade facade = new UnassignedEobrFailurePeriodFacade(this.getContext(), this.getCurrentUser());
        facade.Save(unassignedFailurePeriods);
    }
    
    /// <summary>
    /// Answer a new list of the items that are greater
    /// than 1 minute in duration.
    /// </summary>
    /// <param name="list"></param>
    /// <returns></returns>
    private ArrayList<UnassignedEobrFailurePeriod> RemoveInsignificantItems(List<UnassignedEobrFailurePeriod> list)
    {
        ArrayList<UnassignedEobrFailurePeriod> newList = new ArrayList<UnassignedEobrFailurePeriod>();

        if (list != null && list.size() > 0)
        {
            for (UnassignedEobrFailurePeriod p : list)
            {
                // verify that the duration of the period is 
                // more than 1 minute
                float duration = (p.getStopTime().getTime() - p.getStartTime().getTime())/60000;
                //Date stopSegmentTime = DateUtility.ConvertToPreviousLogEventTime(p.getStopTime());
                if (duration > 1.0F)
                {
                    newList.add(p);
                }
            }
        }

        return newList;
    }
    
	private List<UnassignedEobrFailurePeriod> LoadUnassignedFailurePeriods()
	{
        List<UnassignedEobrFailurePeriod> answer = null;

        UnassignedEobrFailurePeriodFacade facade = new UnassignedEobrFailurePeriodFacade(this.getContext(), this.getCurrentUser());
        answer = facade.FetchUnsubmitted();

        // 2/24/12 JHM - both timestamps in the unassigned period are in UTC.  No conversion needed.
        return answer;
	}

	private void MarkUnassignedFailurePeriodsAsSubmitted(List<UnassignedEobrFailurePeriod> unassignedEobrFailurePeriodList)
	{
        UnassignedEobrFailurePeriodFacade facade = new UnassignedEobrFailurePeriodFacade(this.getContext(), this.getCurrentUser());
        facade.MarkAsSubmitted(unassignedEobrFailurePeriodList);
	}

    /// <summary>
    /// Use the time periods of unassigned driving periods to look 
    /// for GPS tracking records that should be sent to DMO.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <param name="eobrTractorNumber"></param>
    /// <param name="unassignedDrivingPeriods"></param>
    private void ProcessHistoricalRoutePositions(String eobrSerialNumber, String eobrTractorNumber, List<UnassignedDrivingPeriod> unassignedDrivingPeriods)
    {
        if (unassignedDrivingPeriods != null && unassignedDrivingPeriods.size() > 0)
        {
            RouteController rteCntrlr = new RouteController(getContext());
            for (UnassignedDrivingPeriod udp : unassignedDrivingPeriods)
            {
                rteCntrlr.ReadHistoricalRoutePositions(eobrSerialNumber, eobrTractorNumber, udp.getStartTime(), udp.getStopTime(), udp.getStartOdometer());
            }
        }
    }
    
    
    private void ProcessRoutePositionsForUnassignedDrivingPeriodsGenII(String eobrSerialNumber, String eobrTractorNumber, List<UnassignedDrivingPeriod> unassignedDrivingPeriods)
    {
    	RouteController rteCntrlr = new RouteController(getContext());
        for (UnassignedDrivingPeriod udp : unassignedDrivingPeriods)
        {
            rteCntrlr.ReadHistoricalRoutePositions(eobrSerialNumber, eobrTractorNumber, udp.getStartTime(), udp.getStopTime(), udp.getStartOdometer());
        }
    }
    
    
	/**
	 * Answer the list of unassigned driving periods that have occurred
	 * during the logging period for the current log.   
	 * These driving periods will only include
	 * those that have occurred after the starting time of the current log.
	 * @return
	 */
	public List<UnclaimedDrivingPeriod> GetUnclaimedDrivingPeriodsForCurrentLog() {
        List<UnclaimedDrivingPeriod> answer = new ArrayList<UnclaimedDrivingPeriod>();

        // fetch the starting time of the current log
        LogEntryController ctrlr = new LogEntryController(this.getContext());
        EmployeeLog empLog = ctrlr.getCurrentEmployeeLog();

		//Get date from start of day to fetch unassigned driving periods
		Date startTimestamp = new DateTime().withTimeAtStartOfDay().toDate();

        // fetch the current list of all unassigned driving periods 
        List<UnassignedDrivingPeriod> list = this.LoadUnassignedDrivingPeriods(startTimestamp);

        // look at each period in the list and determine if it falls 
        // into the time period of the current log
        if (list != null && list.size() > 0)
        {
            for(UnassignedDrivingPeriod udp : list)
            {
                // 2014.12.02 sjn - Add checking against the end date of the period.
                //                  This prevents the scenario where a midnight unassigned event may be claimable in KMB when the current log
                //                  has not been transitioned across midnight yet
        
                //period end time belongs on the current log (not on a future log)             	
            	if(ctrlr.BelongOnFutureLog(this.getCurrentUser(), empLog, udp.getStopTime()) == false){
            		// create the driving period to send back for 
            		// display with the start/stop times
            		// translated into the correct time zone
            		UnclaimedDrivingPeriod newPeriod = new UnclaimedDrivingPeriod();

            		Date startTime = udp.getStartTime();
            		Date stopTime = udp.getStopTime();
            		udp.setStartTime(startTime);
            		udp.setStopTime(stopTime);
            		newPeriod.setUnassignedDrivingPeriod(udp);

            		answer.add(newPeriod);
            	}
            }
        }

        return answer;
	}

	/**
	 * Process the unclaimed driving periods.   Those driving periods
	 * that are now marked as 'claimed' will be added to the current
	 * log as Driving events.  Afterwards, the claimed driving periods
	 * will be remove from the Unassigned Driving Period list.
	 * As part of merging the new driving period into the log, any
	 * events that may already be in the log during the time period 
	 * of the driving segment are removed.
	 * @param unclaimedDrivingPeriods
	 */
	public void ProcessClaimedDrivingPeriods(List<UnclaimedDrivingPeriod> unclaimedDrivingPeriods)
	{
        // fetch the current log
        LogEntryController logEntryController = new LogEntryController(this.getContext());
        IAPIController employeeLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = logEntryController.getCurrentEmployeeLog();

        for(UnclaimedDrivingPeriod drivingPeriod : unclaimedDrivingPeriods)
        {
        	if(drivingPeriod.getIsClaimed())
        	{
        		// found one
        		UnassignedDrivingPeriod udp = drivingPeriod.getUnassignedDrivingPeriod();
        		
        		// 1/25/13 JHM - Start/Stop times need to converted to LogEvent boundaries so they
        		// properly match up to any existing events for the log.
        		udp.setStartTime(DateUtility.ConvertToPreviousLogEventTime(udp.getStartTime()));
        		udp.setStopTime(DateUtility.ConvertToPreviousLogEventTime(udp.getStopTime()));
        		
        		empLog = logEntryController.CreateNewLogIfNecessary(getCurrentUser(), empLog, udp.getStartTime());
        		        		
                // first, remove any events that might already be in the range of the driving period
				EmployeeLogEldEvent lastEventInPeriod = EmployeeLogUtilities.RemoveAllEventsBetween(empLog, udp.getStartTime(), udp.getStopTime());

                //Location lastLocation = EmployeeLogUtilities.LocationAtTime(empLog, udp.StartTime);
                Location startLocation = new Location();
                startLocation.setGpsInfo(udp.getStartLocation());
                startLocation.setOdometerReading(udp.getStartOdometer());
                startLocation.setEndOdometerReading(udp.getStopOdometer());

                Location stopLocation = new Location();
                stopLocation.setGpsInfo(udp.getStopLocation());
                stopLocation.setOdometerReading(udp.getStopOdometer());

                // ensure that the start/stop of the unassigned driving is reverse geocoded
                List<GpsLocation> gpsListToDecode = new ArrayList<GpsLocation>();
                gpsListToDecode.add(startLocation.getGpsInfo());
                gpsListToDecode.add(stopLocation.getGpsInfo());
                employeeLogController.ReverseGeocodeLocation(gpsListToDecode);

                if (lastEventInPeriod != null && lastEventInPeriod.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
                {
                    // the event just after the unassigned is a driving period 
                    // the distance of the two periods needs to be combined
                    if (lastEventInPeriod.getLocation().getEndOdometerReading() > startLocation.getEndOdometerReading())
                    {
                        // update the end odometer because it is higher
                        startLocation.setEndOdometerReading(lastEventInPeriod.getLocation().getEndOdometerReading());
                    }
                }

                // add the start of the driving event, using the geocoded location
                EmployeeLogUtilities.AddEventToLog(empLog, udp.getStartTime(), new DutyStatusEnum(DutyStatusEnum.DRIVING), startLocation, true, this.getCurrentUser().getRulesetTypeEnum(), null, null,
						udp.getMotionPictureProductionId(), udp.getMotionPictureAuthorityId());

                // Update the start time and distance before possibly transitioning the log and adding the event
                if (drivingPeriod.getUnassignedDrivingPeriod().getStartTime().compareTo(empLog.getMobileStartTimestamp()) < 0)
                {
                    // the start of the unassigned period occurred before the user logged in
                    // adjust the user's start time for this log accordingly
                    empLog.setMobileStartTimestamp(udp.getStartTime());
                }

                employeeLogController.SaveLocalEmployeeLog(empLog);
                
                // if the claimed period overlaps with the start of a new event then take the
                // duty status from that event
                // otherwise take the status from the event immediately preceding the claimed period
                DutyStatusEnum endOfDrivingStatus;
                if (lastEventInPeriod != null)
                	endOfDrivingStatus = lastEventInPeriod.getDutyStatusEnum();
                else
                {
					EmployeeLogEldEvent event = EmployeeLogUtilities.GetEventPriorToTime(empLog,  udp.getStartTime());
                	endOfDrivingStatus = event.getDutyStatusEnum();
                }
                
                empLog = logEntryController.CreateNewLogIfNecessary(getCurrentUser(), empLog, udp.getStopTime());

				EmployeeLogUtilities.AddEventToLog(empLog, udp.getStopTime(), endOfDrivingStatus, stopLocation, true, this.getCurrentUser().getRulesetTypeEnum(), null, null,
						udp.getMotionPictureProductionId(), udp.getMotionPictureAuthorityId());

                String debugMessage = String.format("Claim unassigned driving period: DRV startTime '%1$s' DRV startOdom '%2$f' DRV endOdom '%3$f' DRV dist '%4$f' new status '%5$s' new status startTime '%6$s' mobileDerivedDist '%7$f' startLoc: '%8$s' endLoc: '%9$s'.", DateUtility.getHomeTerminalDateTimeFormat().format(udp.getStartTime()), startLocation.getOdometerReading(), startLocation.getEndOdometerReading(), drivingPeriod.getUnassignedDrivingPeriod().getDistance(), endOfDrivingStatus.toDMOEnum(), DateUtility.getHomeTerminalDateTimeFormat().format(udp.getStopTime()), empLog.getMobileDerivedDistance(), startLocation.ToLocationString(), stopLocation.ToLocationString());
                ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);

                // save the log
                employeeLogController.SaveLocalEmployeeLog(empLog);

            	//if this is an exempt log we need to make sure it can still qualify
            	//as an exempt log after claiming the period
                ExemptLogValidationController exemptController = new ExemptLogValidationController(this.getContext()); 
                exemptController.PerformCompleteValidationForCurrentLog(empLog, false);
                
                // need to remove the udp from the save list, because the driving period has been assigned
                this.MarkUnassignedDrivingPeriodsAsClaimed(udp);
        	}
        }
	}
		

    /// <summary>
    /// Attempt to automatically claim the first unassigned driving period.
    /// In order to auto-claim a period, it must have the following conditions:
    /// 1. There can one, and only, one item unassigned driving period in the list
    /// 2. The vehicle must currently be driving
    /// 3. Last event currently on the driver's log must be On-Duty
    /// 4. The On-Duty start must be within a minute of the unassigned driving period
    /// 5. There must a driving segment started immediately before the On-Duty
    /// 
    /// When all of these conditions exist, the unassigned period will be automatically 
    /// claimed into the drivers log.
    /// </summary>
    /// <param name="empLog"></param>
    @SuppressLint("LongLogTag")
    private UnassignedDrivingPeriod PerformAutoClaimDrivingPeriod(List<UnassignedDrivingPeriod> periods, User currentDriver, EmployeeLog empLog, boolean isVehicleMoving, boolean processLogTransition)
    {
    	IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        boolean isAutoClaim = false;
        
        if (periods.size() == 1 && isVehicleMoving && currentDriver != null && currentDriver.getIsAuthenticated() && empLog != null)
        {
            UnassignedDrivingPeriod udp = periods.get(0);
            Date udpStartTime = udp.getStartTime();
			EmployeeLogEldEvent lastEventOnLog = EmployeeLogUtilities.GetLastEventInLog(empLog);
            if (lastEventOnLog.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY)
            {
                // the last event on the log is OnDuty and
                // the udp occurs after the last log event
                long elapsed;
                if (lastEventOnLog.getStartTime().compareTo(udpStartTime) <= 0)
                {
                    // on-duty happens before the udp...this is the normal case
                    elapsed = udpStartTime.getTime() - lastEventOnLog.getStartTime().getTime();
                }
                else
                {
                    // it's possible to have the udp happens just before the on-duty
                    // this is rare, but it's possible around the top of the minute time period
                    elapsed = lastEventOnLog.getStartTime().getTime() - udpStartTime.getTime();
                }

                // Convert elapsed from milliseconds to minutes
                int elapsedTotalMinutes = (int)(elapsed / 60000);
                
                // 2/20/12 JHM - Added check for the app restart condition to handle
                // auto-claim when driving was continuous.
                if (elapsedTotalMinutes <= currentDriver.getDrivingStopTimeMinutes()
                		|| GlobalState.getInstance().getAppRestartFlag())
                {                	
                    // the udp occurs within the driver's stop time rule of the OnDuty event,
                    // now check to see if the On-Duty ends a Driving period 
					EmployeeLogEldEvent evtPriorToOnDuty = EmployeeLogUtilities.GetEventPriorToTime(empLog, lastEventOnLog.getStartTime());
                    if (evtPriorToOnDuty.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
                    {
                    	// Invalidate the start time of the on duty event (last event in the log) so
                    	// the RemoveAllEvents after will remove this event
                    	empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length - 1].setIsStartTimeValidated(false);

                    	// found a driving segment immediately before the unassigned period
                        // this udp should be ignored and rolled into the existing log
                        // clean up the driver's log by removing everything after this driving period, and save it
                        EmployeeLogUtilities.RemoveAllEventsAfter(empLog, evtPriorToOnDuty.getStartTime());
                        
                        Date nextLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(
                        		getContext(), 
                        		DateUtility.AddDays(empLog.getLogDate(),  1), 
                        		currentDriver.getHomeTerminalTimeZone());
                        
                        //if the end of the driving period occurs on the next log
                        //we need to split its mileage between the two logs
                        if(processLogTransition && udp.getStopTime().after(nextLogStartTime))
                        {
                        	long msOnFirstLog = nextLogStartTime.getTime() - udp.getStartTime().getTime();
                        	double percentFirst = (double)msOnFirstLog / (udp.getStopTime().getTime() - udp.getStartTime().getTime());
                        	
                        	//this assumes a constant rate of travel across the entire driving period
                        	float milesFirst = (float)(udp.getDistance() * percentFirst);
                        	
                        	//end odometer should be the odometer at time of the On Duty event, plus
                        	//the miles of the unassigned period that fall on yesterday's log
                        	evtPriorToOnDuty.getLocation().setEndOdometerReading(lastEventOnLog.getLocation().getOdometerReading() + milesFirst);
                        	empLogController.SaveLocalEmployeeLog(empLog);
                        	
                        	Location location = new Location();
                        	location.setOdometerReading(evtPriorToOnDuty.getLocation().getEndOdometerReading());
                        	
                        	EmployeeLog newLog = EmployeeLogUtilities.CreateNewLog(getContext(), currentDriver, nextLogStartTime, new DutyStatusEnum(DutyStatusEnum.DRIVING), location);
                        	
                        	//Copy values to new log
                        	newLog.setMobileStartTimestamp(nextLogStartTime);
                        	newLog.setTractorNumbers(empLog.getTractorNumbers());
                        	newLog.setTrailerNumbers(empLog.getTrailerNumbers());
							newLog.setTrailerPlate(empLog.getTrailerPlate());
                        	newLog.setShipmentInformation(empLog.getShipmentInformation());
							newLog.setVehiclePlate(empLog.getVehiclePlate());

                        	TeamDriverController teamDriverController = new TeamDriverController(getContext());
                        	teamDriverController.EndTeamDrivingOnTransition(currentDriver, empLog);
                        	teamDriverController.TransferTeamDrivers(currentDriver, newLog);
                        	
                        	empLogController.SaveLocalEmployeeLog(newLog);
                        	
                        	GlobalState.getInstance().setCurrentDriversLog(newLog);
                        	GlobalState.getInstance().setCurrentEmployeeLog(newLog);
                        }
                        else                        
                        	empLogController.SaveLocalEmployeeLog(empLog);

                        String msg = String.format("UnassignedPeriodController.PerformAutoClaimDrivingPeriod found a udp to claim DRV '%s' ON '%s' UDP '%s' udp.startOdom '%f' udp.stopOdom '%f' udp.distance '%f' mobileDerivedDist '%f'", evtPriorToOnDuty.getStartTime().toString(), lastEventOnLog.getStartTime().toString(), udpStartTime.toString(), udp.getStartOdometer(), udp.getStopOdometer(), udp.getDistance(), empLog.getMobileDerivedDistance());
                        Log.d("UnassignedPeriodController.PerformAutoClaimDrivingPeriod",  msg);
                        ErrorLogHelper.RecordMessage(getContext(), msg);

                        isAutoClaim = true;

                        // notify the audit controller so that the driver's alert
                        // can be recalculated
                        HosAuditController auditCtrlr = new HosAuditController(getContext());
                        auditCtrlr.MarkBeginningOfDrivingPeriod(evtPriorToOnDuty.getStartTime());
                        auditCtrlr.MarkBeginningOfRestBreakPeriod(evtPriorToOnDuty.getStartTime());
                    }
                }
            }
        }
        
        if(isAutoClaim)
        {
        	//if this is an exempt log we need to make sure it can still qualify
        	//as an exempt log after claiming the period
        	ExemptLogValidationController exemptController = new ExemptLogValidationController(this.getContext()); 
            exemptController.PerformCompleteValidationForCurrentLog(empLog, false);
        	
        	return periods.get(0);
        }
        
        return null;
    }
}
