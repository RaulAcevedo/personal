package com.jjkeller.kmb.test.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmb.test.kmbapi.controller.dataaccess.MockEmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.dataaccess.ApplicationStateFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("SameParameterValue")
@RunWith(KMBRoboElectricTestRunner.class)
public class EmployeeLogEldMandateControllerTest extends KmbRoboTestBase {

    @Before
    public void setUp() throws Exception {
       super.setUp();

        // set up a mock context for this test
        User user = new User();
        user.setCredentials(new LoginCredentials());
        user.getCredentials().setPrimaryKey(0);
        user.setHomeTerminalTimeZone(new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME));

        Context ctx = GlobalState.getInstance().getApplicationContext();
        new LoginController(ctx).setCurrentUser(user);
    }


    /**
     * reconcileRuleSetChange Tests
     */
    @Test
    public void test_changeRulesetOfCompatibleEldEvents_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        List<EmployeeLogEldEvent> actualResults = invokePrivateMethod_changeRulesetOfCompatibleEldEvents(new EmployeeLogEldMandateController(GlobalState.getInstance().getApplicationContext()), inputEvents, new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));

        Assert.assertEquals(0, actualResults.size());
    }

    @Test
    public void test_changeRulesetOfCompatibleEldEvents_allSameClassification() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        inputEvents.add(createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 1, 1, 2, null, new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(2L, "2016-07-30T05:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(3L, "2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(4L, "2016-07-30T11:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(5L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));

        MockEmployeeLogEldEventFacade mockEmployeeLogEldEventFacade = new MockEmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser());
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(1, createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 1, 1, 2, null, new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(2, createEmployeeLogEldEvent(2L, "2016-07-30T05:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(3, createEmployeeLogEldEvent(3L, "2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(4, createEmployeeLogEldEvent(4L, "2016-07-30T11:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(5, createEmployeeLogEldEvent(5L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));

        // Change US60 => US70
        List<EmployeeLogEldEvent> actualResults = invokePrivateMethod_changeRulesetOfCompatibleEldEvents(new EmployeeLogEldMandateController(GlobalState.getInstance().getApplicationContext(), mockEmployeeLogEldEventFacade, new EventSequenceIdGenerator(new ApplicationStateFacade(GlobalState.getInstance().getApplicationContext()))), inputEvents, new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));

        Assert.assertEquals(10, actualResults.size());

        // 1 original US60 - InactiveChanged
        Assert.assertEquals(1, (int) actualResults.get(0).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(0).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(0).getRuleSet().getValue());

        // 2 original US60 - InactiveChanged
        Assert.assertEquals(2, (int) actualResults.get(1).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(1).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(1).getRuleSet().getValue());

        // 3 original US60 - InactiveChanged
        Assert.assertEquals(3, (int) actualResults.get(2).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(2).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(2).getRuleSet().getValue());

        // 4 original US60 - InactiveChanged
        Assert.assertEquals(4, (int) actualResults.get(3).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(3).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(3).getRuleSet().getValue());

        // 5 original US60 - InactiveChanged
        Assert.assertEquals(5, (int) actualResults.get(4).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(4).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(4).getRuleSet().getValue());


        // 1 clone - US70, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(5).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(5).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US70HOUR, actualResults.get(5).getRuleSet().getValue());

        // 2 clone - US70, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(6).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(6).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US70HOUR, actualResults.get(6).getRuleSet().getValue());

        // 1 clone - US70, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(7).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(7).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US70HOUR, actualResults.get(7).getRuleSet().getValue());

        // 1 clone - US70, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(8).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(8).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US70HOUR, actualResults.get(8).getRuleSet().getValue());

        // 1 clone - US70, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(9).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(9).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US70HOUR, actualResults.get(9).getRuleSet().getValue());
    }

    public void test_changeRulesetOfCompatibleEldEvents_mixedClassifications() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        inputEvents.add(createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(2L, "2016-07-30T05:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(3L, "2016-07-30T06:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(4L, "2016-07-30T11:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(5L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(6L, "2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(7L, "2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(8L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(9L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(10L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(11L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(12L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        inputEvents.add(createEmployeeLogEldEvent(13L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(14L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(15L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));

        MockEmployeeLogEldEventFacade mockEmployeeLogEldEventFacade = new MockEmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser());
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(1, createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(2, createEmployeeLogEldEvent(2L, "2016-07-30T05:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 3, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(3, createEmployeeLogEldEvent(3L, "2016-07-30T06:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(4, createEmployeeLogEldEvent(4L, "2016-07-30T11:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(5, createEmployeeLogEldEvent(5L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(6, createEmployeeLogEldEvent(6L, "2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(7, createEmployeeLogEldEvent(7L, "2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(8, createEmployeeLogEldEvent(8L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(9, createEmployeeLogEldEvent(9L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(10, createEmployeeLogEldEvent(10L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(11, createEmployeeLogEldEvent(11L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(12, createEmployeeLogEldEvent(12L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(13, createEmployeeLogEldEvent(13L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(14, createEmployeeLogEldEvent(14L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        mockEmployeeLogEldEventFacade._fetchByKeyValues.put(15, createEmployeeLogEldEvent(15L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, 4, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));

        // Change any Canadian's => CANADIAN_CYCLE1
        List<EmployeeLogEldEvent> actualResults = invokePrivateMethod_changeRulesetOfCompatibleEldEvents(new EmployeeLogEldMandateController(GlobalState.getInstance().getApplicationContext(), mockEmployeeLogEldEventFacade, new EventSequenceIdGenerator(new ApplicationStateFacade(GlobalState.getInstance().getApplicationContext()))), inputEvents, new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1));

        // no changes - all are already CANADIAN_CYCLE1
        Assert.assertEquals(15, actualResults.size());


        // Change CANADIAN_CYCLE1 => CANADIAN_CYCLE2
        actualResults = invokePrivateMethod_changeRulesetOfCompatibleEldEvents(new EmployeeLogEldMandateController(GlobalState.getInstance().getApplicationContext(), mockEmployeeLogEldEventFacade, new EventSequenceIdGenerator(new ApplicationStateFacade(GlobalState.getInstance().getApplicationContext()))), inputEvents, new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE2));

        // 6 changes
        Assert.assertEquals(21, actualResults.size());

        // 1 original US60 - Active
        Assert.assertEquals(1, (int) actualResults.get(0).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(0).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(0).getRuleSet().getValue());

        // 2 original US60 - Active
        Assert.assertEquals(2, (int) actualResults.get(1).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(1).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(1).getRuleSet().getValue());

        // 3 original US60 - Active
        Assert.assertEquals(3, (int) actualResults.get(2).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(2).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(2).getRuleSet().getValue());

        // 4 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(4, (int) actualResults.get(3).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(3).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(3).getRuleSet().getValue());

        // 5 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(5, (int) actualResults.get(4).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(4).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(4).getRuleSet().getValue());

        // 6 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(6, (int) actualResults.get(5).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(5).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(5).getRuleSet().getValue());

        // 7 original US60 - Active
        Assert.assertEquals(7, (int) actualResults.get(6).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(6).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(6).getRuleSet().getValue());

        // 8 original US60 - Active
        Assert.assertEquals(8, (int) actualResults.get(7).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(7).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(7).getRuleSet().getValue());

        // 9 original US60 - Active
        Assert.assertEquals(9, (int) actualResults.get(8).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(8).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(8).getRuleSet().getValue());

        // 10 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(10, (int) actualResults.get(9).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(9).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(9).getRuleSet().getValue());

        // 11 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(11, (int) actualResults.get(10).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(10).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(10).getRuleSet().getValue());

        // 12 original CANADIAN_CYCLE1 - InactiveChanged
        Assert.assertEquals(12, (int) actualResults.get(11).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), (int) actualResults.get(11).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE1, actualResults.get(11).getRuleSet().getValue());

        // 13 original US60 - Active
        Assert.assertEquals(13, (int) actualResults.get(12).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(12).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(12).getRuleSet().getValue());

        // 14 original US60 - Active
        Assert.assertEquals(14, (int) actualResults.get(13).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(13).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(13).getRuleSet().getValue());

        // 15 original US60 - Active
        Assert.assertEquals(15, (int) actualResults.get(14).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), (int) actualResults.get(14).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.US60HOUR, actualResults.get(14).getRuleSet().getValue());


        // 4 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(15).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(15).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(15).getRuleSet().getValue());

        // 5 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(16).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(16).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(16).getRuleSet().getValue());

        // 6 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(17).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(17).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(17).getRuleSet().getValue());

        // 10 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(18).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(18).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(18).getRuleSet().getValue());

        // 11 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(19).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(19).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(19).getRuleSet().getValue());

        // 12 clone - CANADIAN_CYCLE2, InactiveChangeRequested
        Assert.assertEquals(-1, (int) actualResults.get(20).getPrimaryKey());
        Assert.assertEquals(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue(), (int) actualResults.get(20).getEventRecordStatus());
        Assert.assertEquals(RuleSetTypeEnum.CANADIAN_CYCLE2, actualResults.get(20).getRuleSet().getValue());
    }

    /**
     * HELPER METHODS
     */

    private EmployeeLogEldEvent createEmployeeLogEldEvent(long primaryKey, String eventDateTime, Enums.EmployeeLogEldEventType eventType, int eventCode, Integer eventRecordStatus, Integer eventRecordOrigin, String tractorNumber, RuleSetTypeEnum ruleSetTypeEnum) {

        //	EventType
        //	1 = Duty Status Change
        //	3 = Change in driver’s indication of PC or YM

        //	EventCode for Duty Status Changes
        //	1 = Off Duty
        //	2 = Sleeper
        //	3 = Driving
        //	4 = On Duty

        // EventRecordStatus
        // 1 = Active
        // 2 = Inactive – Changed
        // 3 = Inactive – Change Requested
        // 4 = Inactive – Change Rejected

        Date startDateTime = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        try {
            startDateTime = format.parse(eventDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(startDateTime);

        eldEvent.setPrimaryKey(primaryKey);
        eldEvent.setEventType(eventType);
        eldEvent.setEventCode(eventCode);
        eldEvent.setEventRecordStatus(eventRecordStatus);
        eldEvent.setEventRecordOrigin(eventRecordOrigin);
        eldEvent.setTractorNumber(tractorNumber);
        eldEvent.setRuleSet(ruleSetTypeEnum);

        return eldEvent;
    }

    private <TOut> TOut invokePrivateMethod_changeRulesetOfCompatibleEldEvents(EmployeeLogEldMandateController obj, Object... params) {
        Method method;
        TOut requiredObj = null;
        try {
            method = obj.getClass().getDeclaredMethod("changeRulesetOfCompatibleEldEvents", List.class, RuleSetTypeEnum.class);
            method.setAccessible(true);
            //noinspection unchecked
            requiredObj = (TOut)method.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return requiredObj;
    }

    private void AssertEventCodeCreatedAndDiagnosticFlagSet(EmployeeLogEldEvent checkedEvent, EmployeeLog log) {
        DataDiagnosticEnum missingRequiredDataElements = DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS;

        org.junit.Assert.assertEquals(1, log.getEldEventList().getEldEventList().length);
        EmployeeLogEldEvent dataDiagnosticEvent = log.getEldEventList().getEldEventList()[0];
        org.junit.Assert.assertEquals(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, dataDiagnosticEvent.getEventType());
        org.junit.Assert.assertEquals(missingRequiredDataElements.toDMOEnum(), dataDiagnosticEvent.getDiagnosticCode());
        org.junit.Assert.assertEquals(EmployeeLogEldEventCode.EldDataDiagnosticLogged, dataDiagnosticEvent.getEventCode());
    }
}
