package com.jjkeller.kmbapi.controller.EOBR;

/**
 * Created by ief5781 on 2/21/17.
 */

import com.jjkeller.kmbapi.TestableTimeKeeper;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.IDataFlag;

import org.joda.time.Duration;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RunWith(JUnit4.class)
public class ErrorAccumulatorTest {

    @Test(expected=IllegalArgumentException.class)
    public void WhenPassedMismatchArgumentsShouldThrowException() {
        try {
            ErrorAccumulator sut = new ErrorAccumulator(DateTime.now(), new EventTypeEnum(EventTypeEnum.ERROR), DataFlagEnums.GpsEventFlags.GPS_FAULT);
        } catch(IllegalArgumentException ex) {
            Assert.assertEquals("Specified IDataFlag type GpsEventFlags does not correspond to provided EventType ERROR", ex.getMessage());

            throw ex;
        }
    }

    @Test
    public void ErrorAccumulated_WhenErrorEntirelyInWatchWindow() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        Duration expected = Duration.standardMinutes(15);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:30"), expected);

        for(EventRecord error : errors)
            sut.processEvent(error);

        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:45").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorsAccumulated_IgnoresEventsWithWrongType() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        Duration expected = Duration.standardMinutes(15);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:30"), expected);
        addGpsFault(errors, DateTime.parse("2017-02-20T11:30"), expected);

        for(EventRecord error : errors)
            sut.processEvent(error);

        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:45").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorsAccumulated_IgnoresSubsequentErrorsOnsUntilErrorCleared() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:30"), null);
        addVssFault(errors, DateTime.parse("2017-02-20T11:45"), null);

        errors.add(
            createVssFault(DateTime.parse("2017-02-20T12:00"), false)
        );

        for(EventRecord error : errors)
            sut.processEvent(error);

        Duration expected = Duration.standardMinutes(30);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T12:00").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorsAccumulated_IgnoresSubsequentErrorsOffs() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:30"), Duration.standardMinutes(5));

        //add an extra ERROR OFF
        errors.add(
            createVssFault(DateTime.parse("2017-02-20T11:40"), false)
        );

        for(EventRecord error : errors)
            sut.processEvent(error);

        Duration expected = Duration.standardMinutes(5);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:40").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorsNotAccumulated_IgnoresEventsWithWrongType() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addGpsFault(errors, DateTime.parse("2017-02-20T11:30"), Duration.standardMinutes(15));

        for(EventRecord error : errors)
            sut.processEvent(error);

        Duration expected = Duration.standardMinutes(0);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertNull(sut.getErrorStop());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorAccumulated_WithMultipleSetsOfErrors() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:00"),
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:00"), Duration.standardMinutes(15));
        addVssFault(errors, DateTime.parse("2017-02-20T11:30"), Duration.standardMinutes(15));

        for(EventRecord error : errors)
            sut.processEvent(error);

        //2 15-minute error periods
        Duration expected = Duration.standardMinutes(30);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:45").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorAccumulated_WhenErrorBeginsBeforeWatchWindow() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:30"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:00"), Duration.standardMinutes(45));

        for(EventRecord error : errors)
            sut.processEvent(error);

        //error ends at 11:45, only 15 minutes in watch window
        Duration expected = Duration.standardMinutes(15);

        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:45").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorNotAccumulated_WhenErrorIsOnlyBeforeWatchWindow() {
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T12:00"));

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:45"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();
        addVssFault(errors, DateTime.parse("2017-02-20T11:00"), Duration.standardMinutes(30));

        for(EventRecord error : errors)
            sut.processEvent(error);

        //error ends at 11:30, nothing in watch window
        Duration expected = Duration.standardMinutes(0);

        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertEquals(DateTime.parse("2017-02-20T11:30").getMillis(), sut.getErrorStop().getMillis());
        Assert.assertFalse(sut.getIsInErrorState());
    }

    @Test
    public void ErrorAccumulated_UnendedErrorContinuesAccumulation() {
        DateTime now = DateTime.parse("2017-02-20T12:00");
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(now);

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:30"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();

        DateTime errorStart = DateTime.parse("2017-02-20T11:45");
        addVssFault(errors, errorStart, null);

        for(EventRecord error : errors)
            sut.processEvent(error);

        //the error doesn't end, so the duration is based on "now"
        Duration expected = new Duration(errorStart, now);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());

        //change "now" to make sure it increases
        now = DateTime.parse("2017-02-20T13:00");
        timekeeper.setSettableNow(now);
        expected = new Duration(errorStart, now);
        actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertNull(sut.getErrorStop());
        Assert.assertTrue(sut.getIsInErrorState());
    }

    @Test
    public void ErrorAccumulated_CompleteErrorSetAndUnendedErrorContinuesAccumulation() {
        DateTime now = DateTime.parse("2017-02-20T12:00");
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(now);

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T11:30"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);

        List<EventRecord> errors = new LinkedList<>();

        //the complete error set (on and off) starts at 11:35 for 5 minutes
        addVssFault(errors, DateTime.parse("2017-02-20T11:35"), Duration.standardMinutes(5));

        DateTime errorStart = DateTime.parse("2017-02-20T11:45");
        addVssFault(errors, errorStart, null);

        for (EventRecord error : errors)
            sut.processEvent(error);

        //the error doesn't end, so the duration is based on "now"
        Duration expected = new Duration(errorStart, now).withDurationAdded(Duration.standardMinutes(5), 1);
        Duration actual = sut.getAccumulation();

        Assert.assertEquals(expected.getMillis(), actual.getMillis());
        Assert.assertNull(sut.getErrorStop());
        Assert.assertTrue(sut.getIsInErrorState());
    }

    @Test
    public void ErrorAccumulatorReferenceTimeStamp_Midnight() {
        DateTime now = DateTime.parse("2017-02-20T12:00");
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(now);

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T00:00"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);


        long extectedReferenceTimeStamp = 1487548800000L;
        long actualReferenceTimeStamp = sut.getErrorAccumulatorReferenceTimeStamp();

        Assert.assertEquals(extectedReferenceTimeStamp, actualReferenceTimeStamp);
    }

    @Test
    public void ErrorAccumulatorReferenceTimeStamp_10PM() {
        DateTime now = DateTime.parse("2017-02-20T12:00");
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(now);

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-19T22:00"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);


        long extectedReferenceTimeStamp = 1487548800000L;
        long actualReferenceTimeStamp = sut.getErrorAccumulatorReferenceTimeStamp();

        Assert.assertEquals(extectedReferenceTimeStamp, actualReferenceTimeStamp);
    }

    @Test
    public void ErrorAccumulatorReferenceTimeStamp_8AM() {
        DateTime now = DateTime.parse("2017-02-20T12:00");
        TestableTimeKeeper timekeeper = new TestableTimeKeeper(now);

        ErrorAccumulator sut = new ErrorAccumulator(
                DateTime.parse("2017-02-20T08:00"), //watch window
                new EventTypeEnum(EventTypeEnum.ERROR),
                new ArrayList<IDataFlag>(Arrays.asList(DataFlagEnums.ErrorEventFlags.VSS_FAULT,DataFlagEnums.ErrorEventFlags.ODO_FAULT)),
                timekeeper);


        long extectedReferenceTimeStamp = 1487548800000L;
        long actualReferenceTimeStamp = sut.getErrorAccumulatorReferenceTimeStamp();

        Assert.assertEquals(extectedReferenceTimeStamp, actualReferenceTimeStamp);

    }

    private void addVssFault(List<EventRecord> events, DateTime start, Duration duration) {
        events.add(createVssFault(start, true));

        if(duration != null)
            events.add(createVssFault(start.withDurationAdded(duration, 1), false));
    }

    private void addGpsFault(List<EventRecord> events, DateTime start, Duration duration) {
        events.add(createGpsFault(start, true));

        if(duration != null)
            events.add(createGpsFault(start.withDurationAdded(duration, 1), false));
    }

    private EventRecord createVssFault(DateTime start, boolean errorState) {
        EventRecord event = new EventRecord();
        event.setEventType(EventTypeEnum.ERROR);
        event.setTimecode(start.getMillis());

        if(errorState)
            event.setEventData(1 << DataFlagEnums.ErrorEventFlags.VSS_FAULT.getIndex());

        return event;
    }

    private EventRecord createGpsFault(DateTime start, boolean errorState) {
        EventRecord event = new EventRecord();
        event.setEventType(EventTypeEnum.GPS);
        event.setTimecode(start.getMillis());

        if(errorState)
            event.setEventData(1 << DataFlagEnums.GpsEventFlags.GPS_FAULT.getIndex());

        return event;
    }
}
