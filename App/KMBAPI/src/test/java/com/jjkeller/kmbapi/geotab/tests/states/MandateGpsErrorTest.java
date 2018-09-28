package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MandateGpsErrorTest extends HosProcessorStateMachineTestBase {
    public MandateGpsErrorTest() throws Exception {
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsValid_ReturnNothing() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:02"));
        message.setOdometer(100.0f);
        message.setOrigOdometer(100.0f);
        message.setGpsValid(true);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords.size() == 0);
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsInvalid_ReturnNothing() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:00"));
        message.setOdometer(100.0f);
        message.setOrigOdometer(100.0f);
        message.setGpsValid(false);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords.size() == 0);
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsInvalid_ReturnGPSError() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message1 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:00"));
        message1.setOdometer(100.0f);
        message1.setOrigOdometer(100.0f);
        message1.setGpsValid(false);
        MessageResult result1 =  state.processMessage(message1);
        ArrayList<EventRecord> eventRecords1 = result1.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords1.size() == 0);

        IHOSMessage message2 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:30"));
        message2.setOdometer(115.0f);
        message2.setOrigOdometer(115.0f);
        message2.setGpsValid(false);
        MessageResult result2 =  state.processMessage(message2);
        ArrayList<EventRecord> eventRecords2 = result2.getEventRecords();
        Assert.assertTrue("Should be 1 event", eventRecords2.size() == 1);
        Assert.assertTrue("Should be GPS Error event", eventRecords2.get(0).getEventType() == EventTypeEnum.GPS);
        Assert.assertTrue("Should have GPS Event Data set", eventRecords2.get(0).getEventData() == 1);
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsInvalid_NotOver5MilesUncertainty_ReturnNoGPSError() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 12:58")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message1 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:00"));
        message1.setOdometer(100.0f);
        message1.setOrigOdometer(100.0f);
        message1.setGpsValid(true);
        MessageResult result1 =  state.processMessage(message1);
        ArrayList<EventRecord> eventRecords1 = result1.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords1.size() == 0);

        IHOSMessage message2 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:30"));
        message2.setOdometer(106.0f);
        message2.setOrigOdometer(106.0f);
        message2.setGpsValid(false);
        MessageResult result2 =  state.processMessage(message2);
        ArrayList<EventRecord> eventRecords2 = result2.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords2.size() == 0);
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsInvalid_NoGPSErrorDuringContinuation() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 12:56")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message1 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:00"));
        message1.setOdometer(100.0f);
        message1.setOrigOdometer(100.0f);
        message1.setGpsValid(true);
        MessageResult result1 =  state.processMessage(message1);
        ArrayList<EventRecord> eventRecords1 = result1.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords1.size() == 0);

        IHOSMessage message2 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:30"));
        message2.setOdometer(109.0f);
        message2.setOrigOdometer(109.0f);
        message2.setGpsValid(false);
        MessageResult result2 =  state.processMessage(message2);
        ArrayList<EventRecord> eventRecords2 = result2.getEventRecords();
        Assert.assertTrue("Should be 1 event", eventRecords2.size() == 1);
        Assert.assertTrue("Should be GPS Error event", eventRecords2.get(0).getEventType() == EventTypeEnum.GPS);
        Assert.assertTrue("Should have GPS Event Data set", eventRecords2.get(0).getEventData() == 1);

        IHOSMessage message3 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:45"));
        message3.setOdometer(111.0f);
        message3.setOrigOdometer(111.0f);
        message3.setGpsValid(false);
        MessageResult result3 =  state.processMessage(message3);
        ArrayList<EventRecord> eventRecords3 = result3.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords3.size() == 0);
    }

    @Test
    public void testProcessMessage_whenDrivingAndGpsInvalid_CreateAndClearGPSError() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 12:56")));
        State state = new MandateMovingDriving(sharedState);
        IHOSMessage message1 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:00"));
        message1.setOdometer(100.0f);
        message1.setOrigOdometer(100.0f);
        message1.setGpsValid(true);
        MessageResult result1 =  state.processMessage(message1);
        ArrayList<EventRecord> eventRecords1 = result1.getEventRecords();
        Assert.assertTrue("Should be no events", eventRecords1.size() == 0);

        IHOSMessage message2 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:30"));
        message2.setOdometer(109.0f);
        message2.setOrigOdometer(109.0f);
        message2.setGpsValid(false);
        MessageResult result2 =  state.processMessage(message2);
        ArrayList<EventRecord> eventRecords2 = result2.getEventRecords();
        Assert.assertTrue("Should be 1 event", eventRecords2.size() == 1);
        Assert.assertTrue("Should be GPS Error event", eventRecords2.get(0).getEventType() == EventTypeEnum.GPS);
        Assert.assertTrue("Should have GPS Event Data set", eventRecords2.get(0).getEventData() == 1);

        IHOSMessage message3 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:45"));
        message3.setOdometer(111.0f);
        message3.setOrigOdometer(111.0f);
        message3.setGpsValid(false);
        MessageResult result3 =  state.processMessage(message3);
        ArrayList<EventRecord> eventRecords3 = result3.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords3.size() == 0);

        IHOSMessage message4 =  HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 14:00"));
        message4.setOdometer(115.0f);
        message4.setOrigOdometer(115.0f);
        message4.setGpsValid(true);
        MessageResult result4 =  state.processMessage(message4);
        ArrayList<EventRecord> eventRecords4 = result4.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords4.size() == 1);
        Assert.assertTrue("Should be GPS Error event", eventRecords4.get(0).getEventType() == EventTypeEnum.GPS);
        Assert.assertTrue("Should not have GPS Event Data set", eventRecords4.get(0).getEventData() == 0);
    }
}
