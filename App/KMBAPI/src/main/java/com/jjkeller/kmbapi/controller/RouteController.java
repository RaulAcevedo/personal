package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.RoutePositionFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.IWebAPIServiceHelper;
import com.jjkeller.kmbapi.controller.utility.WebAPIServiceHelperFactory;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.RoutePosition;
import com.jjkeller.kmbapi.proxydata.RoutePositionList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouteController extends ControllerBase {

    private static final AtomicBoolean _isSubmittingRoutePositions = new AtomicBoolean(false);

    private boolean _isEnabled = false;
	private void setIsEnabled(boolean isEnabled) {
		this._isEnabled = isEnabled;
	}
	private boolean getIsEnabled() {
		return _isEnabled;
	}

    private static int defaultRecordingInterval = 15;
    public int getRecordingInterval()
    {
    	int recordingInterval = 0;

    	switch (GlobalState.getInstance().getCurrentDesignatedDriver().getDataProfile().getValue()) {
    		case DataProfileEnum.FULLWITHGEOFENCE:
                // The geofence data profile is reduced to 5 minutes
    			recordingInterval = 5;
    			break;

			case DataProfileEnum.MINIMUMHOS:
	            // If the data profile is set to minimumhos, we want to limit the processing
	            // of the new location to once per hour instead of every 15 minutes to reduce
	            // the amount of data we send to dmo.
				recordingInterval = 60;
				break;

			default:
				recordingInterval = defaultRecordingInterval;
				break;
		}

    	return recordingInterval;
    }
	private void setDefaultRecordingInterval(int value)
    {
        defaultRecordingInterval = value;
    }

    private float _lastValidOdometer;
    private float getLastValidOdometer()
    {
    	return _lastValidOdometer;
    }
    private void setLastValidOdometer(float value)
    {
    	_lastValidOdometer = value;
	}

    private Date _lastValidOdometerTimestamp;
    private Date getLastValidOdometerTimestamp()
    {
    	return _lastValidOdometerTimestamp;
	}
    private void setLastValidOdometerTimestamp(Date value)
    {
    	_lastValidOdometerTimestamp = value;
	}

    /// <summary>
    /// Answer the timestamp when the next routing location needs to be saved
    /// </summary>
    private Date getNextRoutingTimestamp()
    {
        // try to read from state
        Date timestamp;
        Object val = GlobalState.getInstance().getNextRoutingTimestamp();
        if (val == null)
        {
        	timestamp = null;
        }
        else
        {
            timestamp = (Date)val;
        }

        return timestamp;
    }
    private void setNextRoutingTimestamp(Date value)
    {
    	GlobalState.getInstance().setNextRoutingTimestamp(value);
    }

	public RouteController(Context ctx) {
		super(ctx);

		int minutes = GlobalState.getInstance().getAppSettings(ctx).getRoutePositionIntervalMinutes();
		if (minutes < 0)
		{
			this.setIsEnabled(false);
			this.setDefaultRecordingInterval(0);
		}
		else
		{
			this.setIsEnabled(true);
			this.setDefaultRecordingInterval(minutes);
		}
	}

    /// <summary>
    /// Submit all route positions that need to be sent up to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    public boolean SubmitRoutePositionsToDMO()
    {
        boolean isSuccessful = false;
        if (this.getIsWebServicesAvailable())
        {
            RoutePositionFacade facade = new RoutePositionFacade(this.getContext(), this.getCurrentUser());
            List<RoutePosition> unSubmittedPositions = null;

            // first, fetch all unsubmitted route positions
            unSubmittedPositions = facade.FetchAllUnsubmitted();

            WebAPIServiceHelperFactory apiServiceHelperFactory = new WebAPIServiceHelperFactory();
            IWebAPIServiceHelper apiHelper = apiServiceHelperFactory.getInstance(getContext());





            // did they fetch succesfully and are there any to send?
            if (unSubmittedPositions != null && unSubmittedPositions.size() > 0)
            {
                String lastSerialNumber = null;
                RoutePositionList listToSend = null;
                ArrayList<RoutePosition> recordList = null;
                int numToSend = 50;

                for(RoutePosition rec : unSubmittedPositions){
                    if(! rec.getEobrSerialNumber().equalsIgnoreCase(lastSerialNumber)){
                        if(listToSend != null && recordList != null && recordList.size() > 0){
                            listToSend.setRoutePositions(recordList.toArray(new RoutePosition[recordList.size()]));

                            try{
                                apiHelper.SubmitRoutePositions(listToSend);

                                facade.MarkAsSubmitted(recordList);
                            }
                            catch(JsonSyntaxException e){
                                this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                            }
                            catch(IOException e){
                                this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                            }


                        }
                        listToSend = new RoutePositionList();
                        listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
                        recordList = new ArrayList<RoutePosition>();
                        lastSerialNumber = rec.getEobrSerialNumber();
                    }
                    recordList.add(rec);

                    if(recordList.size() >= numToSend){
                        listToSend.setRoutePositions(recordList.toArray(new RoutePosition[recordList.size()]));

                        try{
                            apiHelper.SubmitRoutePositions(listToSend);

                            facade.MarkAsSubmitted(recordList);
                        }
                        catch(JsonSyntaxException e){
                            this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                        }
                        catch(IOException e){
                            this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                        }

                        listToSend = new RoutePositionList();
                        listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
                        recordList = new ArrayList<RoutePosition>();

                    }
                }

                if(listToSend != null && recordList != null && recordList.size() > 0){
                    listToSend.setRoutePositions(recordList.toArray(new RoutePosition[recordList.size()]));
                    try{
                        apiHelper.SubmitRoutePositions(listToSend);

                        facade.MarkAsSubmitted(recordList);
                    }
                    catch(JsonSyntaxException e){
                        this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                    }
                    catch(IOException e){
                        this.HandleException(e, this.getContext().getString(R.string.submitroutepositionstodmo));
                    }
                }
                isSuccessful = true;
            }
            else
            {
                // nothing to send
                isSuccessful = true;
            }
        }

        return isSuccessful;
    }

    /**
     * Asynchronously submit all route positions that need to be sent up to DMO
     */
    public void SubmitRoutePositionsToDMOAsync()
    {
    	if (_isSubmittingRoutePositions.compareAndSet(false, true))
    	{
    		try
    		{
    			new SubmitRoutePositionsTask(getContext()).execute();
    		}
    		catch (Exception ex)
    		{
	        	Log.e("UnhandledCatch", ex.getMessage(), ex);
    			_isSubmittingRoutePositions.set(false);
    		}
    	}
    }

    /// <summary>
    /// Process the new GPS location.
    /// This should be a valid GPS location coming from the source.
    /// Currently, the only source supported is the EOBR.
    /// </summary>
    public void ProcessNewLocation(String eobrSerialNbr, String eobrTractorNbr, GpsLocation gpsLocation, float odometerReading, boolean forceEntry)
    {
        if (this.getIsEnabled() && gpsLocation != null && !gpsLocation.IsEmpty() && eobrSerialNbr != null && !eobrSerialNbr.equals("") && eobrTractorNbr != null && !eobrTractorNbr.equals(""))
        {
            // as long as the feature is enabled, and there is a valid location
            Date currentTimestamp = gpsLocation.getTimestampUtc();
            if (forceEntry || this.ShouldProcessLocation(currentTimestamp))
            {
                // this gps location should be processed
                this.ProcessLocation(eobrSerialNbr, eobrTractorNbr, gpsLocation, odometerReading);

                int recordingInterval = getRecordingInterval();

                // determine the next timestamp that should be processed
                // this is done by taking the current timestamp and adding the duration.
                Date nextTimestamp = DateUtility.AddMinutes(currentTimestamp, recordingInterval);
                this.setNextRoutingTimestamp(nextTimestamp);

                // attempt to submit the positions to DMO
                this.SubmitRoutePositionsToDMOAsync();
            }
        }
    }

    /// <summary>
    /// Process the new location.
    /// Save it to the local database.
    /// </summary>
    private void ProcessLocation(String eobrSerialNbr, String eobrTractorNbr, GpsLocation newLocation, float odometerReading)
    {
        RoutePosition pos = new RoutePosition();

        pos.setEobrId(eobrTractorNbr);
        pos.setEobrSerialNumber(eobrSerialNbr);
        pos.setGpsTimestamp(newLocation.getTimestampUtc());
        // Was if (pos.GpsTimestamp == DateTime.MinValue)
        if (pos.getGpsTimestamp() == null)
        {
            pos.setGpsTimestamp(DateUtility.getCurrentDateTimeUTC());
        }

        pos.setGpsLatitude( newLocation.getLatitudeDegrees());
        pos.setGpsLongitude(newLocation.getLongitudeDegrees());

        pos.setOdometer(odometerReading);

        // save it to the DB
 		RoutePositionFacade facade = new RoutePositionFacade(getContext(), getCurrentUser());
        facade.Save(pos);
    }

    /// <summary>
    /// Attempt to read the GPS info stored in the EOBR at the specified
    /// timestamp.  If the status record at that timestamp either can not 
    /// be read, or there are no valid GPS coord on that enty, then 
    /// keep reading records from the EOBR until a one is 
    /// found that contains valid GPS info.  Once a valid GPS tracking 
    /// record is found, create a RoutePosition that will eventually 
    /// be sent to DMO.
    /// Answer if a record was successfully found and processed.
    /// </summary>
    /// <param name="eobrSerialNbr">serial number of the EOBR</param>
    /// <param name="eobrTractorNbr">tractor number of the EOBR</param>
    /// <param name="timestamp">timestamp to look for a record</param>
    private boolean ProcessHistoricalRecord(String eobrSerialNbr, String eobrTractorNbr, Date startTimestamp, Date stopTimestamp)
    {
        boolean isSuccessful = false;

        StatusRecord statusRecWithGpsInfo = null;
        EobrReader eobrRdr = EobrReader.getInstance();
        Date timestampToRead = startTimestamp;

        // determine the boundary timestamp, in the future, that we will not
        // try to read past
        Date maxTimeToAttempt = DateUtility.AddMinutes(timestampToRead, this.getRecordingInterval());
        boolean done = false;
        while (!done && timestampToRead.compareTo(maxTimeToAttempt) < 0)
        {
        	// 2014.07.16 sjn - If the history reading process should be aborted, then stop right here
    		if(GlobalState.getInstance().getAbortReadingHistory())
    			return false;

            // not done yet, and the timestamp has not exceeded the maximum
            // that we're going to try and read
            StatusRecord statusRec = new StatusRecord();
            int rc = eobrRdr.Technician_GetHistoricalData(statusRec, timestampToRead);
            if (rc == 0)
            {
                // the record was read correctly
                if (!statusRec.IsEmpty() && !statusRec.IsSignificantDeviceFailureDetected() && statusRec.IsGpsLocationValid())
                {
                    // if there is an odometer value, validate it against last odometer
                    if (statusRec.getOdometerReading() > -1F)
                    {
                        // check if odometer is valid - if not search for a valid odometer
                        if (this.IsOdometerValid(statusRec.getTimestampUtc(), statusRec.getOdometerReading()))
                        {
                            statusRecWithGpsInfo = statusRec;
                            this.setLastValidOdometer(statusRec.getOdometerReading());
                            this.setLastValidOdometerTimestamp(statusRec.getTimestampUtc());

                            done = true;
                        }
                    }
                }
            }
            else if (rc != EobrReturnCode.S_SUCCESS)
            {
                done = true;
            }

            // move the time ahead by a few seconds and try to read the next record 
            if (!done) timestampToRead = DateUtility.AddSeconds(timestampToRead, 15);
        }

        if (statusRecWithGpsInfo != null)
        {
            //System.Diagnostics.Debug.WriteLine(string.Format("RouteController.ProcessHistoricalRecord ts: {0} found: {1}", timestamp, statusRecWithGpsInfo.TimeStampUtc));

            // Found a status record to process get the GPS info from the status record.

        	// We are not setting NorthSouth and EastWest indicators with Gen II.  Therefore, use the overloaded constructor if the indicators are not present.
        	GpsLocation gpsLoc = null;

        	char latitudeDirection = statusRecWithGpsInfo.getNorthSouthInd();
        	char longitudeDirection = statusRecWithGpsInfo.getEastWestInd();

        	if (latitudeDirection == '-' || longitudeDirection == '-')
        	{
        		gpsLoc = new GpsLocation(statusRecWithGpsInfo.getGpsTimestampUtc(), statusRecWithGpsInfo.getGpsLatitude(), statusRecWithGpsInfo.getGpsLongitude());
        	}
        	else
        	{
        		gpsLoc = new GpsLocation(statusRecWithGpsInfo.getGpsTimestampUtc(), statusRecWithGpsInfo.getGpsLatitude(), statusRecWithGpsInfo.getNorthSouthInd(), statusRecWithGpsInfo.getGpsLongitude(), statusRecWithGpsInfo.getEastWestInd());
        	}

            //GpsLocation gpsLoc = new GpsLocation(statusRecWithGpsInfo.getGpsTimestampUtc(), statusRecWithGpsInfo.getGpsLatitude(), statusRecWithGpsInfo.getNorthSouthInd(), statusRecWithGpsInfo.getGpsLongitude(), statusRecWithGpsInfo.getEastWestInd());

            // save the GPS info as a RoutePosition that should be sent to DMO
            this.ProcessLocation(eobrSerialNbr, eobrTractorNbr, gpsLoc, statusRecWithGpsInfo.getOdometerReading());

            isSuccessful = true;
        }

        return isSuccessful;
    }

    /// <summary>
    /// Answer if the timestamp from the GPS location indicates that this
    /// GPS location should be processed.
    /// This is done by comparing the timestamp to the next routing timestamp, 
    /// saved to state.   When the timestamp exceeds the next one, then this
    /// GPS location should be processed.
    /// </summary>
    private boolean ShouldProcessLocation(Date timestamp)
    {
        boolean shouldProcess = false;
        Date nextTimestamp = this.getNextRoutingTimestamp();
        // Was if (nextTimestamp == DateTime.MinValue) 
        if (nextTimestamp == null)
        {
            shouldProcess = true;
        }
        else
        {
            shouldProcess = nextTimestamp.compareTo(timestamp) < 0;
        }
        return shouldProcess;
    }

    /// <summary>
    /// Look for Route Positions in the EOBR between the time period
    /// specified by the start/stop timestamp.   Beginning with the start 
    /// time, route position records are read from the EOBR. 
    /// A record will be read from the EOBR with the frequency of the 
    /// local recording interval.
    /// A record for the stopTimestamp will also be generated.
    /// </summary>
    /// <param name="eobrSerialNbr"></param>
    /// <param name="eobrTractorNbr"></param>
    /// <param name="startTimestamp"></param>
    /// <param name="stopTimestamp"></param>
    public void ReadHistoricalRoutePositions(String eobrSerialNbr, String eobrTractorNbr, Date startTimestamp, Date stopTimestamp, float startOdometer)
    {
        boolean done = false;
        Date currentTimestamp = startTimestamp;
        int routePositionCount = 0;
        int totalCount = 0;
        this.setLastValidOdometer(startOdometer);
        this.setLastValidOdometerTimestamp(startTimestamp);

        while (!done)
        {
        	// 2014.07.16 sjn - If the history reading process should be aborted, then stop right here
    		if(GlobalState.getInstance().getAbortReadingHistory())
    			return;

            //System.Diagnostics.Debug.WriteLine(string.Format("RouteController.ReadHistoricalRoutePositions cur: {0} from: {1} to: {2}", currentTimestamp, startTimestamp, stopTimestamp));
            boolean foundOne = this.ProcessHistoricalRecord(eobrSerialNbr, eobrTractorNbr, currentTimestamp, stopTimestamp);

            if (foundOne) routePositionCount++;    // keep track of how many good ones were found
            totalCount++;

            // determine the timestamp of the next record to read
            if (currentTimestamp.compareTo(stopTimestamp) < 0)
            {
                int recordingInterval = getRecordingInterval();

                currentTimestamp = DateUtility.AddMinutes(currentTimestamp , recordingInterval);

                // if after adding the RecordingInterval we've moved past the stop,
                // then set the timestamp to the stop
                if (currentTimestamp.compareTo(stopTimestamp) > 0)
                    currentTimestamp = stopTimestamp;
            }
            else
            {
                // once we move up to, or past, the stop, then we're done
                done = true;
            }
        }

        // log tracing info to the error log
        String msg = String.format("---- Built historical route positions for '%s' start: '%s' stop: '%s' attempted: '%d' found: '%d'\n", eobrSerialNbr, startTimestamp.toString(), stopTimestamp.toString(), totalCount, routePositionCount);
        ErrorLogHelper.RecordMessage(getContext(), msg);
        Log.d("RouteController.ReadHistoricalRoutePositions", msg);
    }

    /// <summary>
    /// Validate the odometer is valid based on the last eobr odometer and the timestamp
    /// of the last eobr odometer compared to the odometer and timestamp of the record we are checking
    /// </summary>
    /// <param name="odometerTime">Timestamp of odometer we are checking</param>
    /// <param name="odometer">Odometer value to determine if valid</param>
    /// <param name="lastOdometerTime">Timestamp of last valid odometer</param>
    /// <returns>true if valid, false if not</returns>
    private boolean IsOdometerValid(Date odometerTime, float odometer)
    {
        float startRange = -1F;
        float endRange = -1F;

        boolean retVal = false;
        // Convert the difference between the values to days
        float dayDiff = (float)(odometerTime.getTime() - this.getLastValidOdometerTimestamp().getTime())/86400000;

        // if last eobr odometer timestamp is more recent than odometer date we are checking, 
        // set start range to last eobr odometer minus the number of days times a value
        // representing traveling at 85 MPH for 24 hours, and set end range to last
        // eobr odometer
        if (dayDiff < 0)
        {
            if ((this.getLastValidOdometer() + (dayDiff * 2040)) < 0)
                startRange = 0;
            else
                startRange = this.getLastValidOdometer() + (dayDiff * 2040);

            endRange = this.getLastValidOdometer();
        }
        // else last eobr odometer is prior to date of odometer we are checking, set start
        // range to the last eobr odometer and the end range to the last odometer plus
        // a value representing traveling at 65 MPH for 24 hours
        else
        {
            startRange = this.getLastValidOdometer();
            endRange = this.getLastValidOdometer() + (dayDiff * 2040);
        }

        // set the minimum range at 2 miles
        if (endRange - startRange < 2.0F)
            endRange = startRange + 2.0F;

        if (odometer >= startRange && odometer <= endRange)
        {
            retVal = true;
        }

        return retVal;
    }

    private static class SubmitRoutePositionsTask extends AsyncTask<Void, Void, Boolean>
    {
		private final WeakReference<Context> context;

    	public SubmitRoutePositionsTask(final Context context)
    	{
    		this.context = new WeakReference<Context>(context);
    	}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean isSuccessful = false;
			try
			{
                //09/25/2017 carlos.briseno: This async task can be executed right away the GlobalState application is being destroyed (on log out(, so let's way 1 second to
                //verify if it is still alive. The best solution for it is to move our web service requests to an Android service but our app
                //has a lot of dependencies to the GlobalState and to modify it would require a lot of effort while hugely impacting the code.
                Thread.sleep(1000);
				if (GlobalState.isApplicationRunning()){
                    isSuccessful = new RouteController(context.get()).SubmitRoutePositionsToDMO();
                }
			} catch (InterruptedException e) {
                e.printStackTrace();
            } finally
			{
				_isSubmittingRoutePositions.set(false);
			}
			return isSuccessful;
		}
    }
}
