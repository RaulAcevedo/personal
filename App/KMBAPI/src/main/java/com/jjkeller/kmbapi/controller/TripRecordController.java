package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.TripRecordFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEobrTripReportListener;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.IWebAPIServiceHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.WebAPIServiceHelperFactory;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.TripRecord;
import com.jjkeller.kmbapi.proxydata.TripRecordList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TripRecordController extends ControllerBase
{
	private static final AtomicBoolean _isSubmittingTripRecords = new AtomicBoolean(false);
	
	public TripRecordController(Context ctx)
	{
		super(ctx);
	}
	
	public void RegisterForUpdates(Context context, IEobrTripReportListener eobrTripReportHandler)
	{
		EobrReader.getInstance().setEobrTripReportHandler(eobrTripReportHandler);
	}
	
	public void ProcessTripReport(TripReport tripReport, float speedThreshold, int rpmThreshold)
	{
		ArrayList<User> loggedInUsers = getLoggedInUserList();

		// Find the user this trip is for
		User employeeForTrip = null;
		for (User user : loggedInUsers)
		{
			if (user.getCredentials().getDriverIdCrc() == tripReport.getDriverId())
			{
				employeeForTrip = user;
				break;
			}
		}
		
		int dataProfile = this.getCurrentDesignatedDriver().getDataProfile().getValue();
		boolean shouldSubmitToDMO = dataProfile != DataProfileEnum.MINIMUMHOS
									&& dataProfile != DataProfileEnum.MINIMUMHOSWITHGPS
									&& dataProfile != DataProfileEnum.MINIMUMHOSWITHFUELTAX
									&& dataProfile != DataProfileEnum.MINIMUMHOSWITHFUELTAXANDGPS;
		
		// If the user was found, save the trip
		if (employeeForTrip != null)
		{
			TripRecord saveReport = new TripRecord();
			saveReport.setEobrSerialNumber(EobrReader.getInstance().getEobrSerialNumber());
			saveReport.setEobrTractorNumber(EobrReader.getInstance().getEobrIdentifier());
			saveReport.setEmployeeId(employeeForTrip.getCredentials().getEmployeeId());
			saveReport.setTripNumber(tripReport.getTripNum());
			saveReport.setIgnitionState(tripReport.getIgnition());
			saveReport.setOdometer(tripReport.getOdometer());
			saveReport.setTripSecs(tripReport.getTripSecs());
			saveReport.setTripDist(tripReport.getTripDist());
			saveReport.setIdleSecs(tripReport.getIdleSecs());
			
			if (tripReport.getLatitude() == 0 && tripReport.getLongitude() == 0)
			{
				saveReport.setGpsLatitude(Float.NaN);
				saveReport.setGpsLongitude(Float.NaN);
			}
			else
			{
				saveReport.setGpsLatitude(tripReport.getLatitude());
				saveReport.setGpsLongitude(tripReport.getLongitude());
			}
			
			saveReport.setMaxSpeed(tripReport.getMaxSpeed());
			saveReport.setTripFuel(tripReport.getTripFuel());
			saveReport.setTimestamp(tripReport.getDataTimecodeAsDate());
			saveReport.setAllowedSpeed(speedThreshold);
			saveReport.setAllowedTach(rpmThreshold);
			saveReport.setIsSubmitted(!shouldSubmitToDMO);
			saveReport.setMaxEngRPM(tripReport.getMaxEngRPM());
			saveReport.setAvgEngRPM(tripReport.getAvgEngRPM());
			SaveTripRecord(saveReport);
		}
				
		if (shouldSubmitToDMO)
		{
			SubmitTripRecordsToDMOAsync();
		}
	}

	public void SaveTripRecord(TripRecord tripRecord)
	{
		TripRecordFacade facade = new TripRecordFacade(getContext(), getCurrentUser());
		facade.Save(tripRecord);
	}
	
    /**
     * Asynchronously submit all engine records that need to be sent up to DMO
     */
	public void SubmitTripRecordsToDMOAsync()
	{
		if(_isSubmittingTripRecords.compareAndSet(false, true))
		{
    		try
			{
				new SubmitTripRecordsTask(getContext()).execute();
			}
			catch (Exception ex)
			{
	        	Log.e("UnhandledCatch", ex.getMessage(), ex);
	        	_isSubmittingTripRecords.set(false);
			}
		}
	}

	
    /// <summary>
    /// Submit all engine records that need to be sent up to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    public boolean SubmitTripRecordsToDMO()
    {
        boolean isSuccesful = false; 

        if (this.getIsWebServicesAvailable())
        {       	
            try
            {                
                // first fetch unsubmitted trip records, but only fetch at most a limited number to avoid 
                // issues where large number of unsubmitted records exist
                TripRecordFacade facade = new TripRecordFacade(this.getContext(), this.getCurrentUser());                
                List<TripRecord> unsubmittedRecords = facade.FetchUnsubmittedLimited(2000);

				WebAPIServiceHelperFactory apiServiceHelperFactory = new WebAPIServiceHelperFactory();
				IWebAPIServiceHelper apiHelper = apiServiceHelperFactory.getInstance(getContext());

                // are there any to send? 
                if (unsubmittedRecords != null && unsubmittedRecords.size() > 0)
                {
                    // build the EngineRecordList for each unique EOBR/Driver 
                    // combination found it the unsubmitted list
                    String lastSerialNumber = null;
                    String lastEmployeeId = null;
                    TripRecordList listToSend = null;
                    ArrayList<TripRecord> recordList = null;
                    int numToSend = 250;
 
                    for (TripRecord rec : unsubmittedRecords)
                    {
                        if (!rec.getEmployeeId().equalsIgnoreCase(lastEmployeeId) || !rec.getEobrSerialNumber().equalsIgnoreCase(lastSerialNumber))
                        {                       	
                            // a record with either a new driver, or EOBR is discovered
                            if (listToSend != null && recordList != null && recordList.size() > 0)
                            {
                                // there is a previous list (from a different employee/eobr) to send to DMO
                       			listToSend.setTripRecords(recordList.toArray(new TripRecord[recordList.size()]));
                   	           	
                        		try
                        		{
									apiHelper.SubmitTripRecords(listToSend);
                            		
                                    // mark the list just sent to DMO as submitted
                                    facade.MarkAsSubmitted(recordList);
            	            	}
            	            	catch (JsonSyntaxException e)
            	            	{
            	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));
            	            	}
            	            	catch (IOException e)
            	            	{
            	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));                    		
            	            	}
                            }

                            // start a new list to hold the new stuff for this driver/EOBR
                            listToSend = new TripRecordList(null);
                            listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
                            listToSend.setEobrTractorNumber(rec.getEobrTractorNumber());
                            listToSend.setDriverEmployeeId(rec.getEmployeeId());

                            recordList = new ArrayList<TripRecord>();
                            lastEmployeeId = rec.getEmployeeId();
                            lastSerialNumber = rec.getEobrSerialNumber();
                        }

                        recordList.add(rec);

                        // Have enough records been collected to send?
                        if (recordList.size() >= numToSend)
                        {
                            // yes, so send this batch of records to DMO
                   			listToSend.setTripRecords(recordList.toArray(new TripRecord[recordList.size()]));
               	           	
                    		try
                    		{
								apiHelper.SubmitTripRecords(listToSend);
                        		
                                // mark the list just sent to DMO as submitted
                                facade.MarkAsSubmitted(recordList);
        	            	}
        	            	catch (JsonSyntaxException e)
        	            	{
        	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));
        	            	}
        	            	catch (IOException e)
        	            	{
        	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));                    		
        	            	}

	                        // start a new list to hold the new stuff for this driver/EOBR
	                        listToSend = new TripRecordList(null);
	                        listToSend.setEobrSerialNumber(rec.getEobrSerialNumber());
	                        listToSend.setEobrTractorNumber(rec.getEobrTractorNumber());
	                        listToSend.setDriverEmployeeId(rec.getEmployeeId());
	
	                        recordList = new ArrayList<TripRecord>();
                        }
                    }

                    if (listToSend != null && recordList != null && recordList.size() > 0)
                    {
                        // there are records to send to DMO
               			listToSend.setTripRecords(recordList.toArray(new TripRecord[recordList.size()]));
           	           	
                		try
                		{
							apiHelper.SubmitTripRecords(listToSend);
                    		
                            // mark the list just sent to DMO as submitted
                            facade.MarkAsSubmitted(recordList);
    	            	}
    	            	catch (JsonSyntaxException e)
    	            	{
    	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));
    	            	}
    	            	catch (IOException e)
    	            	{
    	            		this.HandleException(e, this.getContext().getString(R.string.submittriprecordstodmo));                    		
    	            	}
                    }
                }

                isSuccesful = true;
            }
            catch (Exception excp)
            {
            	this.HandleException(excp, this.getContext().getString(R.string.submitenginerecordstodmo));
            }
        }

        return isSuccesful;
    }

    private static class SubmitTripRecordsTask extends AsyncTask<Void, Void, Boolean>
    {
    	private final WeakReference<Context> context;
    	
    	public SubmitTripRecordsTask(final Context context)
    	{
    		this.context = new WeakReference<Context>(context);
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean isSuccesful = false;
			try
			{
				isSuccesful = new TripRecordController(context.get()).SubmitTripRecordsToDMO();
			}
			finally
			{
				_isSubmittingTripRecords.set(false);
			}
			return isSuccesful;
		}
    }
	
}
