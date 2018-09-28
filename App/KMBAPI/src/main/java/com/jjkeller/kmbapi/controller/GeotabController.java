package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.GeotabEventRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.GeotabHOSDataFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedDrivingPeriodFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.ReverseGeocodeUtilities;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;
import com.jjkeller.kmbapi.geotabengine.GeotabEngine;
import com.jjkeller.kmbapi.geotabengine.IGeotabListener;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.EventRecordToGeotabEventRecordAdapter;
import com.jjkeller.kmbapi.proxydata.GeotabDriver;
import com.jjkeller.kmbapi.proxydata.GeotabEventRecord;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;
import com.jjkeller.kmbapi.proxydata.HosMessageToGeoTabHosDataAdapter;
import com.jjkeller.kmbapi.proxydata.KMBUnassignedPeriodIsClaimable;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by jld5296 on 9/22/16.
 */

public class GeotabController extends ControllerBase {

    private static final String TAG = GeotabController.class.getSimpleName();

    private GeotabEngine _engine = null;

    private final GeotabEventRecordFacade _geotabEventRecordFacade;
    private final GeotabHOSDataFacade _geotabHOSDataFacade;

    private static final int TIMEFRAME_FOR_TRIPENGINESECONDS = 1;
    private static final String GEOTAB_API_UNASSIGN_DRIVER_ID = "-1";

    private static int _cachedDriverId = -1;

    public GeotabController(Context ctx) {
        super(ctx);

        _geotabEventRecordFacade = new GeotabEventRecordFacade(super.getContext());
        _geotabHOSDataFacade = new GeotabHOSDataFacade(super.getContext());

        if(IsCurrentDeviceGeotab())
            _engine = (GeotabEngine) EobrReader.getInstance().getEobrEngine();

        CacheCurrentDriver();
    }

    public boolean IsCurrentDeviceGeotab() {
        return EobrReader.getInstance().getEobrEngine() instanceof GeotabEngine;
    }

    public boolean IsDeviceAttached() {
        return GeotabEngine.IsDeviceAttached(getContext());
    }

    public boolean IsDeviceConnected() {
        return _engine != null && _engine.getIsSocketConnected();
    }

    public void addListener(IGeotabListener listener) {
        if(_engine != null)
            _engine.addListener(listener);
    }

    public void removeListener(IGeotabListener listener) {
        if(_engine != null)
            _engine.removeListener(listener);
    }

    public void SaveEventsAndHosDataForDriver(String vehicleId, List<EventRecord> events,
            IHOSMessage hosMessage) {
        int driverCrc;

        // When this method is called during the logout process, the current designated driver from
        // global state is sometimes null.  To prevent the application crash and save this data load
        // the credentials from a cached static member variable if the global state value is null
        if (GlobalState.getInstance().getCurrentDesignatedDriver() == null) {
            driverCrc = _cachedDriverId;
        } else {
            driverCrc = GlobalState.getInstance().getCurrentDesignatedDriver()
                    .getCredentials().getDriverIdCrc();
        }

        GeotabHOSData geotabHOSData = new HosMessageToGeoTabHosDataAdapter(hosMessage, driverCrc,
                vehicleId);
        _geotabHOSDataFacade.Save(geotabHOSData);
        Long recordKey = geotabHOSData.getPrimaryKey();

        for (EventRecord eventRecord : events) {
            if (shouldSaveEventRecord(eventRecord, geotabHOSData, vehicleId)) {
                _geotabEventRecordFacade.Save(
                        new EventRecordToGeotabEventRecordAdapter(eventRecord, driverCrc, vehicleId,
                                recordKey));
            }
        }
    }

    private void CacheCurrentDriver() {

        if (GlobalState.getInstance().getCurrentDesignatedDriver() != null &&
                GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getDriverIdCrc() != -1) {
            _cachedDriverId = GlobalState.getInstance()
                    .getCurrentDesignatedDriver().getCredentials().getDriverIdCrc();
        }
    }

    private boolean shouldSaveEventRecord(EventRecord eventRecord,
            GeotabHOSData geotabHOSData, String vehicleId) {
        // If the eventRecord is type ignition on and occurred prior to the login or has already
        // been saved, do not save it
        if (eventRecord.getEventType() != EventTypeEnum.IGNITIONON) {
            return true;
        }

        boolean isEventPriorToLogin = isEventPriorToLogin(geotabHOSData);
        boolean isDuplicateToIgnRecord = isDuplicateToIgnitionRecord(
                eventRecord.getTimecodeAsDate(),
                vehicleId, geotabHOSData.getTripEngineSeconds());

        return !isEventPriorToLogin && !isDuplicateToIgnRecord;
    }

    private boolean isEventPriorToLogin(GeotabHOSData geotabHOSData) {
        final int GeotabHosRecordFrequency = 2;

        //Compare to the frequency we receive GeotabHOSData records
        return geotabHOSData.getTripEngineSeconds() >= GeotabHosRecordFrequency;
    }

    private Date getCalculatedIgnTimestamp(Date currentDate, float tripEngineSeconds ) {
        // We need to subtract the TripEngineSeconds from the current date to get the real
        // ignition on timeStamp
        if(currentDate != null && tripEngineSeconds > 0 ){
            DateTime eventDateTime = new DateTime(currentDate);
            return eventDateTime.minusSeconds((int)tripEngineSeconds).toDate();
        }
        return currentDate;
    }

    private boolean isDuplicateToIgnitionRecord(Date eventRecordDate, String vehicleId,
            float tripEngineSeconds) {
        GeotabEventRecord existingEventRecord;

        // We need to calculate the real IgnTimestamp to find duplicate records when
        // disconnecting the geotab cord.
        DateTime eventDateTime = new DateTime(
                getCalculatedIgnTimestamp(eventRecordDate, tripEngineSeconds));
        // Since we are loosing milliseconds precision on the calculation of the ignOnEvent
        // TimeStamp we need to look for the event in a time frame of +1 to -1
        // seconds only for GeoTab
        Date initialEventDateTime = eventDateTime.minusSeconds(
                TIMEFRAME_FOR_TRIPENGINESECONDS).toDate();
        Date finalEventDateTime = eventDateTime.plusSeconds(
                TIMEFRAME_FOR_TRIPENGINESECONDS).toDate();

        existingEventRecord = _geotabEventRecordFacade.FetchByTimestampFrame(vehicleId,
                EventTypeEnum.IGNITIONON, initialEventDateTime, finalEventDateTime);

        return existingEventRecord != null && existingEventRecord.getVehicleId().equalsIgnoreCase(
                GlobalState.getInstance().getCurrentEobrSerialNumber());
    }

	/**
	 * Created by Alex Gonzalez on 1/26/2017
	 * IMPORTANT : If more geotab tables are added, the purge should be implemented 
	*/
    public void PurgeGeotabTables()
	{
        _geotabEventRecordFacade.PurgeTable();
        _geotabHOSDataFacade.PurgeTable();
    }

    public EmployeeLogEldEventList DownloadUnidentifiedELDEvents(String eobrSerialNumber) throws KmbApplicationException {

        EmployeeLogEldEventList unidentifiedELDEvents = null;

        try {
            RESTWebServiceHelper restWebServiceHelper = new RESTWebServiceHelper(this.getContext());
            unidentifiedELDEvents = restWebServiceHelper.GetGeotabUnidentfiedELDEvents(eobrSerialNumber);
            ErrorLogHelper.RecordMessage(String.format("Downloaded %s Unidentified Geotab Eld Events", unidentifiedELDEvents.getEldEventList().length));
        }
        catch (Exception exception){
            this.HandleExceptionAndThrow(exception, this.getContext().getString(R.string.downloadUnidentifiedELDEvents), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }

        return unidentifiedELDEvents;
    }

    public List<UnassignedDrivingPeriod> DownloadUnassignedDrivingPeriods(String eobrSerialNumber, String startTimeUTC) throws KmbApplicationException {
        List<UnassignedDrivingPeriod> unassignedDrivingPeriods = new ArrayList<>();

        //TODO Temporary prevention of downloading unassigned driver events PBI 54487
        /*try {
            RESTWebServiceHelper restWebServiceHelper = new RESTWebServiceHelper(this.getContext());
            unassignedDrivingPeriods = restWebServiceHelper.GetDisconnectedDrivingPeriods(eobrSerialNumber, startTimeUTC);

            if(unassignedDrivingPeriods != null && !unassignedDrivingPeriods.isEmpty()){
                SetRecordsToSubmitted(unassignedDrivingPeriods);
            }

            SaveUnassignedDrivingPeriods(unassignedDrivingPeriods, startTimeUTC);

        }
        catch (Exception exception){
            this.HandleExceptionAndThrow(exception, this.getContext().getString(R.string.downloadUnassignedDrivingPeriods), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }*/
        Log.w(TAG, "UnassignedDrivingPeriods prevented to be downloaded for GeoTab");

       return unassignedDrivingPeriods;
    }

    public List<KMBUnassignedPeriodIsClaimable> ClaimUnassignedDrivingPeriods(Collection<UnclaimedDrivingPeriod> unclaimedDrivingPeriods) throws KmbApplicationException {
        try {
            RESTWebServiceHelper restWebServiceHelper = new RESTWebServiceHelper(this.getContext());

            List<String> ids = new ArrayList<>();
            for(UnclaimedDrivingPeriod period : unclaimedDrivingPeriods) {
                if(period.getIsClaimed()) {
                    ids.add(period.getUnassignedDrivingPeriod().getEncompassId());
                }
            }

            KMBUnassignedPeriodIsClaimable[] result = restWebServiceHelper.ClaimDisconnectedDrivingPeriods(ids.toArray(new String[ids.size()]));

            return Arrays.asList(result);
        }
        catch (Exception exception){
            this.HandleExceptionAndThrow(exception, this.getContext().getString(R.string.downloadUnassignedDrivingPeriods), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }

        return null;
    }

    private void SaveUnassignedDrivingPeriods(List<UnassignedDrivingPeriod> unassignedDrivingPeriods, String startTimeUTC)
    {
	    // Go trough the list of existing UDP in the local DB to prevent duplicates
        List<UnassignedDrivingPeriod> FinalList = new ArrayList<>();
        UnassignedDrivingPeriodFacade facade = new UnassignedDrivingPeriodFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        List<UnassignedDrivingPeriod> CurrentUDPList = facade.FetchAllByDate(startTimeUTC);
        for (UnassignedDrivingPeriod udp: unassignedDrivingPeriods){
            boolean addNew = true;
            for (UnassignedDrivingPeriod existUdp: CurrentUDPList){
                if (udp.getEncompassId().equals(existUdp.getEncompassId())) {
                    if (udp.getIsClaimed()  && !existUdp.getIsClaimed())
                        facade.MarkAsClaimed(existUdp);
                    addNew = false;
                    break;
                }
            }
            if (addNew && !udp.getIsClaimed())
                FinalList.add(udp);
        }
        if (FinalList.size() > 0){
            GlobalState.getInstance().setReviewUDPDialogBeenDisplayedOnceOnRODS(false);
            facade.Save(FinalList);
        }
    }

    private void  SaveUnidentifiedEldEvents(EmployeeLogEldEventList unidentifiedELDEvents, boolean isReviewed)
    {
        EmployeeLogEldEvent[] events = unidentifiedELDEvents.getEldEventList();
        EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(),getCurrentUser());

        for (EmployeeLogEldEvent event: events) {
            event.setIsReviewed(isReviewed ? isReviewed : event.getIsReviewed());
            event.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.LOCAL);

            // If possible, Geotab Unidentified Events should have their Geolocations persisted
            if(ReverseGeocodeUtilities.eventIsMissingGeolocation(event)) {
                String geolocation = ReverseGeocodeUtilities
                        .getGeolocationFromEventData(event.getEventDateTime(), event.getLatitude(), event.getLongitude());
                if(!geolocation.isEmpty()) { event.setGeolocation(geolocation); }
            }
        }

        facade.SaveListInSingleTransaction(unidentifiedELDEvents.getEldEventList());
    }

    public void ClearEngineState()
    {
        if(_engine != null) _engine.ClearState();
    }

    public void DownloadUnidentifiedELDEventsForCurrentDevice(final String eobrSerialNumber, boolean isReviewed) throws KmbApplicationException {
        SaveUnidentifiedEldEvents(DownloadUnidentifiedELDEvents(eobrSerialNumber), isReviewed);
    }

    public void DownloadUnassignedDrivingPeriodsForCurrentLog(final String eobrSerialNumber) throws KmbApplicationException {
        String dateString =  getLogStartDate();
        SaveUnassignedDrivingPeriods(DownloadUnassignedDrivingPeriods(eobrSerialNumber, dateString), dateString);
    }

    public boolean CheckForUnassignedDrivingPeriods(){
        List<UnclaimedDrivingPeriod> periods = ControllerFactory.getInstance().getUnassignedPeriodController().GetUnclaimedDrivingPeriodsForCurrentLog();

        return periods != null && !periods.isEmpty();
    }

    public String getLogStartDate() {
        DateTime currentDateTime = DateUtility.getCurrentDateTime();
        DateTime logStartTime = new DateTime(EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentDateTime.toDate(), this.getCurrentUser().getHomeTerminalTimeZone()));

        SimpleDateFormat dateFormat = DateUtility.getUnassignedDrivingPeriodDateFormat();
        String dateString;

        DateTimeComparator dateTimeComparator = DateTimeComparator.getTimeOnlyInstance();

        int timeCompare = dateTimeComparator.compare(currentDateTime, logStartTime);

        if (timeCompare >= 0){
            dateString = dateFormat.format(logStartTime.toDate());
        }
        else{
            Date dayBeforeDate = DateUtility.AddDays(logStartTime.toDate(), -1);

            dateString = dateFormat.format(dayBeforeDate);
        }

        return dateString;
    }

    public void AutoPurgeOldGeotabRecords() {
        int logPurgeDayCount = GlobalState.getInstance().getCompanyConfigSettings(getContext()).getLogPurgeDayCount();

        DateTime purgeDate = new DateTime();
        purgeDate = purgeDate.minusDays(logPurgeDayCount);

        _geotabEventRecordFacade.PurgeOldRecords(purgeDate.toDate());
        _geotabHOSDataFacade.PurgeOldRecords(purgeDate.toDate(), true);
    }

    /**
     * General method to submit a DriverChange request to KMB REST Services and update
     * Geotab's API with the current driver. It is safe to call this from anywhere as
     * it will decide where to execute the call based on the current thread environment.
     * @param unassignDriver
     */
    public void SubmitGeotabApiDriverChange(boolean unassignDriver) {
        // If we're on the main thread, spin up a background task,
        // otherwise hit the REST Services immediately from current (background) thread
        if(Looper.myLooper() == Looper.getMainLooper()) {
            SubmitGeotabDriverChangeTask(unassignDriver);
        } else {
            SubmitGeotabDriverChangeRequest(unassignDriver);
        }
    }

    /**
     * General method to submit an asynchronous background task that updates
     * Geotab assigned driver info via KMB REST Services. Use from the UI thread only!
     * @param unassignDriver
     */
    private void SubmitGeotabDriverChangeTask(boolean unassignDriver) {
        String eobrSerialNumber = GlobalState.getInstance().getCurrentEobrSerialNumber();
        String employeeCode = unassignDriver ? GEOTAB_API_UNASSIGN_DRIVER_ID :
            GlobalState.getInstance()
                    .getCurrentDesignatedDriver()
                    .getCredentials()
                    .getEmployeeCode();

        SubmitGeotabDriverChangeTaskRun(eobrSerialNumber, employeeCode);
    }

    /**
     * General method to submit an HTTP request to KMB REST Services that updates
     * Geotab assigned driver info. Use from a background thread only!
     * @param unassignDriver
     * @return
     */
    private boolean SubmitGeotabDriverChangeRequest(boolean unassignDriver) {
        String eobrSerialNumber = GlobalState.getInstance().getCurrentEobrSerialNumber();
        String employeeCode = unassignDriver ? GEOTAB_API_UNASSIGN_DRIVER_ID :
                GlobalState.getInstance()
                        .getCurrentDesignatedDriver()
                        .getCredentials()
                        .getEmployeeCode();
        GeotabDriver driver = new GeotabDriver();
        driver.setEobrSerialNumber(eobrSerialNumber);
        driver.setDriverId(employeeCode);
        return SubmitGeotabDriverChange(driver);
    }

    private boolean SubmitGeotabDriverChange(GeotabDriver geotabDriver) {
        boolean isSubmitted = false;
        RESTWebServiceHelper restWebServiceHelper = new RESTWebServiceHelper(this.getContext());
        try{
            isSubmitted = restWebServiceHelper.SubmitGeotabDriverChange(geotabDriver);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse);
        } catch (IOException ioe) {
            this.HandleException(ioe);
        }
        return isSubmitted;
    }

    private static SubmitGeotabDriverTask submitGeotabDriverTask;
    private class SubmitGeotabDriverTask extends AsyncTask<Void, Void, Boolean> {

        private String eobrSerialNumber;
        private String employeeCode;
        public SubmitGeotabDriverTask(String eobrSerialNumber, String employeeCode){
            super();
            this.eobrSerialNumber = eobrSerialNumber;
            this.employeeCode = employeeCode;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            GeotabDriver geotabDriver = new GeotabDriver();
            geotabDriver.setEobrSerialNumber(eobrSerialNumber);
            geotabDriver.setDriverId(employeeCode);

            return SubmitGeotabDriverChange(geotabDriver);
        }

    }

    public void SubmitGeotabDriverChangeTaskRun(String eobrSerialNumber, String employeeCode){
        submitGeotabDriverTask = new SubmitGeotabDriverTask(eobrSerialNumber, employeeCode);
        submitGeotabDriverTask.execute();
    }

}
