package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.proxydata.EngineRecord;
import com.jjkeller.kmbapi.proxydata.EngineRecordList;
import com.jjkeller.kmbapi.proxydata.EventDataRecord;
import com.jjkeller.kmbapi.proxydata.EventDataRecordList;
import com.jjkeller.kmbapi.proxydata.RoutePosition;
import com.jjkeller.kmbapi.proxydata.RoutePositionList;
import com.jjkeller.kmbapi.proxydata.TripRecord;
import com.jjkeller.kmbapi.proxydata.TripRecordList;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by aaz3239 on 4/12/17.
 */

public class IotDataSerialization {


    private Gson _gson;


    public IotDataSerialization() {

        _gson   = new GsonBuilder()
                .setDateFormat(
                        DateUtility.getHomeTerminalDMOSoapDateTimeFormat()
                                .toPattern())
                .registerTypeAdapter(EventDataRecordList.class,
                        new EventDataRecordListSerializer())
                .registerTypeAdapter(EventDataRecord[].class,
                        new EventDataRecordArraySerializer())
                .registerTypeAdapter(EventDataRecord.class,
                        new EventDataRecordSerializer())
                .registerTypeAdapter(EngineRecordList.class,
                        new EngineRecordListSerializer())
                .registerTypeAdapter(EngineRecord[].class,
                        new EngineRecordArraySerializer())
                .registerTypeAdapter(EngineRecord.class,
                        new EngineRecordSerializer())
                .registerTypeAdapter(TripRecordList.class,
                        new TripRecordListSerializer())
                .registerTypeAdapter(TripRecord[].class,
                        new TripRecordArraySerializer())
                .registerTypeAdapter(TripRecord.class,
                        new TripRecordSerializer())
                .registerTypeAdapter(RoutePositionList.class,
                        new RoutePositionListSerializer())
                .registerTypeAdapter(RoutePosition[].class,
                        new RoutePositionArraySerializer())
                .registerTypeAdapter(RoutePosition.class,
                        new RoutePositionSerializer())
                .create();
    }

    public Gson getGson(){
        return _gson;
    }

    private class EventDataRecordListSerializer implements
            JsonSerializer<EventDataRecordList> {
        public JsonElement serialize(EventDataRecordList src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("DriverEmployeeId", src.getDriverEmployeeId());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());

            EventDataRecord[] eventDataRecords = src.getEventRecords();

            if (eventDataRecords != null) {
                JsonElement jsonArray = _gson.toJsonTree(eventDataRecords);
                o.add("EventDataRecords", jsonArray);
            }

            return o;
        }
    }

    private class EventDataRecordArraySerializer implements
            JsonSerializer<EventDataRecord[]> {
        public JsonElement serialize(EventDataRecord[] src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (EventDataRecord er : src) {
                array.add(_gson.toJsonTree(er, EventDataRecord.class));
            }

            return array;
        }
    }

    private class EventDataRecordSerializer implements
            JsonSerializer<EventDataRecord> {
        public JsonElement serialize(EventDataRecord src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("DriverEmployeeId", src.getDriverEmployeeId());
            o.addProperty("EventType", src.getEventType());
            o.addProperty("EventData", src.getEventData());
            if (src.getEobrTimestamp() == null)
                o.add("EobrTimestamp", null);
            else
                o.addProperty("EobrTimestamp",
                        dateTimeFormat.format(src.getEobrTimestamp()));

            o.addProperty("Odometer", Float.toString(src.getOdometer()));
            if (Float.isNaN(src.getGpsLatitude()))
                o.addProperty("GpsLatitude",
                        GlobalState.MSDecimalSerializationMinValue);
            else
                o.addProperty("GpsLatitude",
                        Float.toString(src.getGpsLatitude()));

            if (Float.isNaN(src.getGpsLongitude()))
                o.addProperty("GpsLongitude",
                        GlobalState.MSDecimalSerializationMinValue);
            else
                o.addProperty("GpsLongitude",
                        Float.toString(src.getGpsLongitude()));

            o.addProperty("Speedometer", Float.toString(src.getSpeedometer()));
            o.addProperty("Tachometer", Float.toString(src.getTachometer()));

            return o;
        }
    }

    private class EngineRecordListSerializer implements
            JsonSerializer<EngineRecordList> {
        public JsonElement serialize(EngineRecordList src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            o.addProperty("EobrTractorNumber", src.getEobrTractorNumber());
            o.addProperty("DriverEmployeeId", src.getDriverEmployeeId());

            EngineRecord[] engineRecords = src.getEngineRecords();

            if (engineRecords != null) {
                JsonElement jsonArray = _gson.toJsonTree(engineRecords);
                o.add("EngineRecords", jsonArray);
            }

            return o;
        }
    }

    private class EngineRecordArraySerializer implements
            JsonSerializer<EngineRecord[]> {
        public JsonElement serialize(EngineRecord[] src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (EngineRecord er : src) {
                array.add(_gson.toJsonTree(er, EngineRecord.class));
            }

            return array;
        }
    }

    private class EngineRecordSerializer implements
            JsonSerializer<EngineRecord> {
        public JsonElement serialize(EngineRecord src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("EobrOverallStatus", src.getEobrOverallStatus());
            o.addProperty("RecordType", src.getRecordType().getValue());
            if (src.getEobrTimestamp() == null)
                o.add("EobrTimestamp", null);
            else
                o.addProperty("EobrTimestamp",
                        dateTimeFormat.format(src.getEobrTimestamp()));
            o.addProperty("Speedometer", Float.toString(src.getSpeedometer()));
            o.addProperty("Odometer", Float.toString(src.getOdometer()));
            o.addProperty("Tachometer", Float.toString(src.getTachometer()));
            if (src.getGpsTimestamp() == null)
                o.add("GpsTimestamp", null);
            else
                o.addProperty("GpsTimestamp",
                        dateTimeFormat.format(src.getGpsTimestamp()));
            o.addProperty("GpsLatitude", Float.toString(src.getGpsLatitude()));
            o.addProperty("GpsLongitude", Float.toString(src.getGpsLongitude()));
            o.addProperty("FuelEconomyAverage",
                    Float.toString(src.getFuelEconomyAverage()));
            o.addProperty("FuelEconomyInstant",
                    Float.toString(src.getFuelEconomyInstant()));
            o.addProperty("FuelUseTotal", Float.toString(src.getFuelUseTotal()));
            o.addProperty("BrakePressure",
                    Float.toString(src.getBrakePressure()));
            o.addProperty("CruiseControlSet",
                    Boolean.toString(src.getCruiseControlSet()));
            o.addProperty("TransmissionAttained", src.getTransmissionAttained());
            o.addProperty("TransmissionSelected", src.getTransmissionSelected());

            return o;
        }
    }

    private class TripRecordListSerializer implements
            JsonSerializer<TripRecordList> {
        public JsonElement serialize(TripRecordList src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            o.addProperty("EobrTractorNumber", src.getEobrTractorNumber());
            o.addProperty("DriverEmployeeId", src.getDriverEmployeeId());

            TripRecord[] tripRecords = src.getTripRecords();

            if (tripRecords != null) {
                JsonElement jsonArray = _gson.toJsonTree(tripRecords);
                o.add("TripRecords", jsonArray);
            }

            return o;
        }
    }

    private class TripRecordArraySerializer implements
            JsonSerializer<TripRecord[]> {
        public JsonElement serialize(TripRecord[] src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (TripRecord er : src) {
                array.add(_gson.toJsonTree(er, TripRecord.class));
            }

            return array;
        }
    }

    private class TripRecordSerializer implements JsonSerializer<TripRecord> {
        public JsonElement serialize(TripRecord src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("TripNumber", src.getTripNumber());
            o.addProperty("IgnitionState", src.getIgnitionState());
            o.addProperty("TripSecs", src.getTripSecs());
            o.addProperty("TripDist", src.getTripDist());
            o.addProperty("IdleSecs", Float.toString(src.getIdleSecs()));
            o.addProperty("Odometer", Float.toString(src.getOdometer()));
            o.addProperty("MaxSpeed", Float.toString(src.getMaxSpeed()));

            if (Float.isNaN(src.getGpsLatitude()))
                o.addProperty("GpsLatitude",
                        GlobalState.MSDecimalSerializationMinValue);
            else
                o.addProperty("GpsLatitude",
                        Float.toString(src.getGpsLatitude()));
            if (Float.isNaN(src.getGpsLongitude()))
                o.addProperty("GpsLongitude",
                        GlobalState.MSDecimalSerializationMinValue);
            else
                o.addProperty("GpsLongitude",
                        Float.toString(src.getGpsLongitude()));

            o.addProperty("TripFuel", Float.toString(src.getTripFuel()));
            o.addProperty("AllowedSpeed", Float.toString(src.getAllowedSpeed()));
            o.addProperty("AllowedTach", Float.toString(src.getAllowedTach()));
            o.addProperty("MaxTach", src.getMaxEngRPM());
            o.addProperty("AvgTach", src.getAvgEngRPM());

            if (src.getTimestamp() == null)
                o.add("EobrTimestampUtc", null);
            else
                o.addProperty("EobrTimestampUtc",
                        dateTimeFormat.format(src.getTimestamp()));

            return o;
        }
    }


    private class RoutePositionListSerializer implements
            JsonSerializer<RoutePositionList> {
        public JsonElement serialize (RoutePositionList src, Type typeOfT,
                                      JsonSerializationContext context){

            JsonObject o = new JsonObject();

            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());

            RoutePosition[] routePositions = src.getRoutePositions();

            if (routePositions != null) {
                JsonElement jsonArray = _gson.toJsonTree(routePositions);
                o.add("RoutePositions", jsonArray);
            }

            return o;
        }
    }

    private class RoutePositionArraySerializer implements
            JsonSerializer<RoutePosition[]> {
        public JsonElement serialize(RoutePosition[] src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (RoutePosition rp : src) {
                array.add(_gson.toJsonTree(rp, RoutePosition.class));
            }

            return array;
        }
    }

    private class RoutePositionSerializer implements
            JsonSerializer<RoutePosition> {
        public JsonElement serialize(RoutePosition src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("EobrId", src.getEobrId());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());

            if (src.getGpsTimestamp() == null)
                o.addProperty("GpsTimestamp", "null");
            else
                o.addProperty("GpsTimestamp",
                        dateTimeFormat.format(src.getGpsTimestamp()));

            o.addProperty("GpsLatitude", Float.toString(src.getGpsLatitude()));
            o.addProperty("GpsLongitude", Float.toString(src.getGpsLongitude()));
            o.addProperty("Odometer", Float.toString(src.getOdometer()));
            o.addProperty("IsUnladen", Boolean.toString(src.getIsUnladen()));

            return o;
        }
    }

}
