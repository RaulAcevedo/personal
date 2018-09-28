package com.jjkeller.kmbapi.compare;

import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.malfunction.BaseMalfunctionTestConfig;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class GeneralEventComparatorTest extends BaseMalfunctionTestConfig {


    private EmployeeLogEldEvent buildEvent(String dateTime, int eventType, int eventCode, int recordOrigin, int recordStatus) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent();
        event.setEventDateTime(getDate(dateTime));
        event.setEventCode(eventCode);
        event.setEventType(Enums.EmployeeLogEldEventType.setFromInt(eventType));
        event.setEventRecordStatus(recordStatus);
        event.setEventRecordOrigin(recordOrigin);
        return event;
    }


    public Date getDateDbFormat(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.000");
        DateTime dt = formatter.parseDateTime(dateTimeString);
        return dt.toDate();
    }

    public Date getDate(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
        DateTime dt = formatter.parseDateTime(dateTimeString);
        return dt.toDate();
    }

    @Test
    public void testDifferentDates() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();
        list.add(buildEvent("01/15/2018 04:32:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:34", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 2, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));

        Collections.sort(list, new GenericEventComparer());

        assertEquals(list.get(0).getEventDateTime().getTime(), getDate("01/15/2018 03:30:34").getTime());
        assertEquals(list.get(1).getEventDateTime().getTime(), getDate("01/15/2018 03:30:35").getTime());
        assertEquals(list.get(2).getEventDateTime().getTime(), getDate("01/15/2018 04:32:35").getTime());

    }


    @Test
    public void testDifferentOriginTieBreaker() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 2, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 3, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 4, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 3, 1));

        Collections.sort(list, new GenericEventComparer());
        //used in an equals clause but doesn't actually sort.

        assertEquals(list.get(0).getEventRecordOrigin().intValue(), 1);
        assertEquals(list.get(1).getEventRecordOrigin().intValue(), 2);
        assertEquals(list.get(2).getEventRecordOrigin().intValue(), 3);
        assertEquals(list.get(3).getEventRecordOrigin().intValue(), 3);
        assertEquals(list.get(4).getEventRecordOrigin().intValue(), 4);

    }


    @Test
    public void testDifferentEventCodeTieBreaker() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_Sleeper, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_NULL, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1));

        Collections.sort(list, new GenericEventComparer());

        assertEquals(list.get(0).getEventCode(), EmployeeLogEldEventCode.DutyStatus_NULL);
        assertEquals(list.get(1).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals(list.get(2).getEventCode(), EmployeeLogEldEventCode.DutyStatus_Sleeper);
        assertEquals(list.get(3).getEventCode(), EmployeeLogEldEventCode.DutyStatus_Driving);
        assertEquals(list.get(4).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        assertEquals(list.get(5).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite);


        list = new ArrayList<>();

        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_Sleeper, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_NULL, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1));


    }


    @Test
    public void testDifferentEventTypeTieBreakers() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue(), EmployeeLogEldEventCode.EldDataDiagnosticLogged, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue(), EmployeeLogEldEventCode.EldDataDiagnosticCleared, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.LoginLogout.getValue(), EmployeeLogEldEventCode.Login, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.LoginLogout.getValue(), EmployeeLogEldEventCode.Logout, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Certification.getValue(), EmployeeLogEldEventCode.Certification_MaxValue, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Certification.getValue(), EmployeeLogEldEventCode.Certification_MaxValue, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.IntermediateLog.getValue(), EmployeeLogEldEventCode.IntermediateLog_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.IntermediateLog.getValue(), EmployeeLogEldEventCode.IntermediateLog_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));

        Collections.sort(list, new GenericEventComparer());

        assertEquals(list.get(0).getEventType().getValue(), Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue());
        assertEquals(list.get(1).getEventType().getValue(), Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue());
        assertEquals(list.get(2).getEventType().getValue(), Enums.EmployeeLogEldEventType.LoginLogout.getValue());
        assertEquals(list.get(3).getEventType().getValue(), Enums.EmployeeLogEldEventType.LoginLogout.getValue());
        assertEquals(list.get(4).getEventType().getValue(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue());
        assertEquals(list.get(5).getEventType().getValue(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue());
        assertEquals(list.get(6).getEventType().getValue(), Enums.EmployeeLogEldEventType.DutyStatusChange.getValue());
        assertEquals(list.get(7).getEventType().getValue(), Enums.EmployeeLogEldEventType.DutyStatusChange.getValue());
        assertEquals(list.get(8).getEventType().getValue(), Enums.EmployeeLogEldEventType.IntermediateLog.getValue());
        assertEquals(list.get(9).getEventType().getValue(), Enums.EmployeeLogEldEventType.IntermediateLog.getValue());
        assertEquals(list.get(10).getEventType().getValue(), Enums.EmployeeLogEldEventType.Certification.getValue());
        assertEquals(list.get(11).getEventType().getValue(), Enums.EmployeeLogEldEventType.Certification.getValue());
        assertEquals(list.get(12).getEventType().getValue(), Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue());
        assertEquals(list.get(13).getEventType().getValue(), Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue());

        list = new ArrayList<>();

        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue(), EmployeeLogEldEventCode.EldDataDiagnosticLogged, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue(), EmployeeLogEldEventCode.EldDataDiagnosticCleared, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.LoginLogout.getValue(), EmployeeLogEldEventCode.Login, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.LoginLogout.getValue(), EmployeeLogEldEventCode.Logout, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Certification.getValue(), EmployeeLogEldEventCode.Certification_MaxValue, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.Certification.getValue(), EmployeeLogEldEventCode.Certification_MaxValue, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.IntermediateLog.getValue(), EmployeeLogEldEventCode.IntermediateLog_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.IntermediateLog.getValue(), EmployeeLogEldEventCode.IntermediateLog_ConventionalLocationPrecision, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1));

    }

    @Test
    public void testDifferentStatusCodeTieBreaker() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 3));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 2));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 3, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 4, 2));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 3, 1));

        Collections.sort(list, new GenericEventComparer());

        assertEquals(list.get(0).getEventRecordStatus().intValue(), 1);
        assertEquals(list.get(1).getEventRecordStatus().intValue(), 2);

        assertEquals(list.get(0).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals(list.get(1).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);

        assertEquals(list.get(2).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        assertEquals(list.get(3).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        assertEquals(list.get(4).getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        //NOTE original at least moved status 1 to the front.
        assertEquals(list.get(2).getEventRecordStatus().intValue(), 1);


        list = new ArrayList<>();

        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 3));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 2));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OnDuty, 3, 1));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 4, 2));
        list.add(buildEvent("01/15/2018 03:30:35", Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEventCode.DutyStatus_OffDuty, 3, 1));
    }


    @Test
    public void testEveryComboOfEventstAtTheSameTime() {
        List<EmployeeLogEldEvent> list = new ArrayList<>();

        for (Enums.EmployeeLogEldEventType eventTypeEnum : Enums.EmployeeLogEldEventType.values()) {
            int minCodeValue = 0;
            int maxCodeValue = 0;
            switch (eventTypeEnum) {
                case DutyStatusChange:
                    minCodeValue = 0;
                    maxCodeValue = 5;
                    break;
                case IntermediateLog:
                case LoginLogout:
                    minCodeValue = 1;
                    maxCodeValue = 2;
                    break;
                case ChangeInDriversIndication:
                    minCodeValue = 0;
                    maxCodeValue = 2;
                    break;
                case Certification:
                    minCodeValue = 1;
                    maxCodeValue = 9;
                    break;
                case EnginePowerUpPowerDown:
                    minCodeValue = 1;
                    maxCodeValue = 4;
                    break;
                case Malfunction_DataDiagnosticDetection:
                    minCodeValue = 0;
                    maxCodeValue = 4;
                    break;
                default:
                    break;
            }

            for (int eventCode = minCodeValue; eventCode <= maxCodeValue; eventCode++) {
                for (int recordOrigin = 1; recordOrigin <= 4; recordOrigin++) {
                    for (Enums.EmployeeLogEldEventRecordStatus recordStatus : Enums.EmployeeLogEldEventRecordStatus.values()) {
                        list.add(buildEvent("12/11/2018 21:30:50", eventTypeEnum.getValue(), eventCode, recordOrigin, recordStatus.getValue()));
                    }
                }
            }

        }
        //original comparator would fail if items were shuffled first.
        Collections.shuffle(list);
        Collections.sort(list, new GenericEventComparer());

    }


}
