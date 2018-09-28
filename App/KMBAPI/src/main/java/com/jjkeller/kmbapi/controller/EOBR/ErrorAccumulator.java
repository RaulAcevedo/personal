package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.IDataFlag;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ief5781 on 2/20/17.
 */


public class ErrorAccumulator {

    DateTime errorStart = null;
    DateTime errorStop = null;
    DateTime watchWindowStart = null;
    EventTypeEnum eventType = null;
    List<IDataFlag> errorBitList = null;
    ITimeKeeper timeKeeper = null;
    EventRecord previousEvent = null;
    Duration duration = new Duration(0);

    public ErrorAccumulator(DateTime watchWindowStart, EventTypeEnum eventType, IDataFlag errorBit) {
        this(watchWindowStart, eventType, new ArrayList<>(Arrays.asList(errorBit)), TimeKeeper.getInstance());
    }

    public ErrorAccumulator(DateTime watchWindowStart, EventTypeEnum eventType, List<IDataFlag> errorBits) {
        this(watchWindowStart, eventType, errorBits, TimeKeeper.getInstance());
    }

    public ErrorAccumulator(DateTime watchWindowStart, EventTypeEnum eventType, IDataFlag errorBit, ITimeKeeper timeKeeper) {
        this(watchWindowStart, eventType, new ArrayList<>(Arrays.asList(errorBit)), timeKeeper);
    }

    public ErrorAccumulator(DateTime watchWindowStart, EventTypeEnum eventType, List<IDataFlag> errorBits, ITimeKeeper timeKeeper) {
        this.eventType = eventType;
        this.watchWindowStart = watchWindowStart;
        this.timeKeeper = timeKeeper;
        this.errorBitList = errorBits;

        for (IDataFlag errorBit : errorBitList) {
            if(eventType.getValue() != errorBit.getEventType().getValue())
                throw new IllegalArgumentException(String.format("Specified IDataFlag type %s does not correspond to provided EventType %s", errorBit.getClass().getSimpleName(), eventType.toDMOEnum()));
        }
    }

    public void processEvent(EventRecord event) {
        if(event == null || event.getEventType() != eventType.getValue()) {
            return;
        }
        previousEvent = event;
        DateTime eventTime = new DateTime(event.getTimecode());

        if (event.hasDataFlag(errorBitList)) {
            //Error condition started
            if (errorStart == null) {
                errorStart = eventTime;
                errorStop = null;
            }

            //we only want to track error time in this window
            if (errorStart.isBefore(watchWindowStart)) {
                errorStart = watchWindowStart;
                errorStop = null;
            }
        } else {
            //ignore this error if it was cleared prior to the watch window
            if (errorStart != null && !eventTime.isBefore(watchWindowStart)) {
                //the error was cleared, calculate duration
                Duration errorDuration = new Duration(errorStart, eventTime);
                duration = duration.withDurationAdded(errorDuration, 1);
            }

            errorStart = null;
            errorStop = eventTime;
        }
    }

    public EventRecord getPreviousEvent(){
        return previousEvent;
    }

    public DateTime getWatchWindowStart(){
        return watchWindowStart;
    }

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public DateTime getErrorStop() { return errorStop; }

    public boolean getIsInErrorState() { return errorStart != null; }

    public Duration getAccumulation() {
        //if the error condition hasn't ended yet, add the time from the beginning of the error until now
        Duration returnValue = duration;

        if(errorStart != null) {
            Duration ongoingDuration = new Duration(errorStart, timeKeeper.getCurrentDateTime());

            returnValue = duration.withDurationAdded(ongoingDuration, 1);
        }

        return returnValue;
    }

    public void adminDebugSetDuration(Duration duration){
        this.duration = duration;
    }

    public long getErrorAccumulatorReferenceTimeStamp() {
        //always start at the UTC day boundary (i.e. midnight) preceding the 24 hour period start time.
        //In other words, if the daily log start time is 8:00 AM, we need to start at midnight UTC instead
        //since error records are only saved on day changes and when the error state changes.  We cannot
        //just use midnight of the current day - if it's currently 7:30 AM our log is almost over, so we'd
        //have to read from midnight the previous day.
        return watchWindowStart.withZone(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis();
    }
}