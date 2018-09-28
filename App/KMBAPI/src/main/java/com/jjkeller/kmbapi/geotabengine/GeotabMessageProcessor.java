package com.jjkeller.kmbapi.geotabengine;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.HosMessageProcessor.HosMessageProcessor;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IVehicleStateMachine;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.VehicleStateMachine;
import com.jjkeller.kmbapi.HosMessageProcessor.utility.GeotabEventRecordToEventRecordUtility;
import com.jjkeller.kmbapi.HosMessageProcessor.utility.GeotabHosDataToStatusRecordUtility;
import com.jjkeller.kmbapi.HosMessageProcessor.utility.GeotabHosDataToTripRecordUtility;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.dataaccess.GeotabEventRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.GeotabHOSDataFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.interfaces.IGeotabMessageProcessor;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.GeotabEventRecord;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jhm2586 on 10/3/2016.
 */

public class GeotabMessageProcessor extends HosMessageProcessor implements IGeotabMessageProcessor {
    private String vehicleId;


    public GeotabMessageProcessor(IFeatureToggleService featureToggleService, Thresholds thresholds) {
        this(featureToggleService, new VehicleStateMachine(featureToggleService, thresholds));
    }

    public GeotabMessageProcessor(IFeatureToggleService featureToggleService, IVehicleStateMachine vehicleStateMachine) {
        super(featureToggleService, vehicleStateMachine);

        //at this point we're not persisting the reference timestamps since history
        //is facilitated through a different process so just set them to now
        DateTime now = TimeKeeper.getInstance().getCurrentDateTime();
        super.getReferenceTimestamp().setDtcReferenceTime(now.getMillis());
        super.getReferenceTimestamp().setEobrReferenceTime(now.getMillis());
        super.getReferenceTimestamp().setEventReferenceTime(now.getMillis());
        super.getReferenceTimestamp().setHistogramReferenceTime(now.getMillis());
        super.getReferenceTimestamp().setTripReferenceTime(now.getMillis());

    }

    private CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>> _geotabDataProcessed;

    @Override
    public ArrayList<EventRecord> processHosMessage(IHOSMessage hosMessage) {
        ArrayList<EventRecord> events = super.processHosMessage(hosMessage);
        if(getGeotabDataProcessed() != null)
            getGeotabDataProcessed().execute(hosMessage, events);
        return events;
    }

    @Override
    public void getEobrData(Context ctx, StatusRecord statusRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime) {
        GeotabHOSDataFacade facade = new GeotabHOSDataFacade(ctx, null);
        GeotabHOSData record = null;

        switch(queryMethod.getValue())
        {
            case StatusRecordQueryMethodEnum.TIMESTAMP:
                 record = facade.FetchByTimestamp(this.vehicleId, new Date(timestamp));

                if(setRefTime && record != null && record.getTimestampUtc() != null)
                    this.referenceTimestamps.setEobrReferenceTime(record.getTimestampUtc().getMillis());

                break;
            case StatusRecordQueryMethodEnum.RECORDID:
                if(recordId == GeotabConstants.MOST_RECENT_RECORD_ID) {
                    record = facade.FetchLatestByVehicle(this.vehicleId);
                }else{
                    record = facade.FetchByVehicleAndKey(this.vehicleId, recordId);
                }
                if (setRefTime && record != null && record.getTimestampUtc() != null)
                    this.referenceTimestamps.setEobrReferenceTime(record.getTimestampUtc().getMillis());

                break;
            default:
                break;
        }

        if(record != null) {
            GeotabHosDataToStatusRecordUtility.UpdateStatusRecordFromGeotabHosData(statusRecord, record);
        }
    }

    @Override
    public void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime) {
        GeotabEventRecordFacade facade = new GeotabEventRecordFacade(ctx, null);
        GeotabEventRecord record = null;

        switch (queryMethod.getValue()) {
            case StatusRecordQueryMethodEnum.TIMESTAMP:
                record = facade.FetchByTimestamp(this.vehicleId, new Date(timestamp));
                if (setRefTime && record != null && record.getTimestampUtc() != null)
                    this.referenceTimestamps.setEventReferenceTime(record.getTimestampUtc().getTime());
                break;
            case StatusRecordQueryMethodEnum.RECORDID:
                if (recordId == GeotabConstants.MOST_RECENT_RECORD_ID)
                    record = facade.FetchLatestByVehicle(this.vehicleId);

                break;
            default:
                break;
        }

        if (record != null) {
            this.UpdateEventRecordFromGeotabEventRecord(ctx, eventRecord, record, timestamp);
        }
    }

    public void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime, int eventMask) {
        GeotabEventRecordFacade facade = new GeotabEventRecordFacade(ctx, null);
        GeotabEventRecord record = null;

        switch (queryMethod.getValue()) {
            case StatusRecordQueryMethodEnum.TIMESTAMP:
                record = facade.FetchByTimestampAndEventType(this.vehicleId, new Date(timestamp), this.getEventTypesFromEventMask(eventMask));
                if (setRefTime && record != null && record.getTimestampUtc() != null)
                    this.referenceTimestamps.setEventReferenceTime(record.getTimestampUtc().getTime());
                break;
            case StatusRecordQueryMethodEnum.RECORDID:
                if (recordId == GeotabConstants.MOST_RECENT_RECORD_ID)
                    record = facade.FetchLatestByVehicle(this.vehicleId);

                break;
            default:
                break;
        }

        if (record != null) {
            this.UpdateEventRecordFromGeotabEventRecord(ctx, eventRecord, record, timestamp);
        }
    }

    private void UpdateEventRecordFromGeotabEventRecord(Context ctx, EventRecord eventRecord, GeotabEventRecord record, long timestamp)
    {
        GeotabHOSDataFacade hosFacade = new GeotabHOSDataFacade(ctx);

        //events may have a millisecond component if there were multiple events created from the same message
        //or for drive on events (the timestamp is back-dated to the time of the move). Strip off the millisecond
        //so we can retrieve the original message.
        Calendar c = Calendar.getInstance();
        c.setTime(record.getTimestampUtc());
        c.set(Calendar.MILLISECOND, 0);
        Date stamp = c.getTime();

        GeotabHOSData hosData = hosFacade.FetchByTimestamp(this.vehicleId, stamp);

        //if there wasn't a message found at that timestamp then look at the key
        if(hosData == null)
            hosData = hosFacade.FetchByVehicleAndKey(this.vehicleId, record.getGeotabHosDataKey());

        record.setHosData(hosData);

        GeotabEventRecordToEventRecordUtility.UpdateEventRecordFromGeotabEventRecord(eventRecord, record);

        Log.d("geotabEvent", String.format("Request for event at %s found event %d at %s", new Date(timestamp).toString(), record.getEventType(), record.getTimestampUtc().toString()));
    }

    @Override
    public void getTripData(Context ctx, TripReport tripReport, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime) {
        GeotabHOSDataFacade hosFacade = new GeotabHOSDataFacade(ctx);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));
        c.set(Calendar.MILLISECOND, 0);
        GeotabHOSData hosData = hosFacade.FetchLatestByVehicle(this.vehicleId);
        if(hosData == null) {
            return;
        }
        GeotabHosDataToTripRecordUtility.updateTripReportFromGeotabHosData(tripReport, hosData);
        tripReport.setRecordId((int) hosData.getPrimaryKey());
    }

    public CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>> getGeotabDataProcessed() {
        return _geotabDataProcessed;
    }

    public void setGeotabDataProcessed(CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>> geotabDataProcessed) {
        this._geotabDataProcessed = geotabDataProcessed;
    }

    @Override
    public String getVehicleId() { return this.vehicleId; }
    @Override
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    private List<Integer> getEventTypesFromEventMask(int mask)
    {
        List<Integer> answer = new ArrayList<Integer>();
        List<Integer> eventTypes = new EventTypeEnum(0).getEnumList();

        for (Integer enumVal : eventTypes) {
            if ((mask & (1 << enumVal)) != 0) { answer.add(enumVal); }
        }

        return answer;
    }
}
