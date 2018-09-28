package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.os.Bundle;

import android.util.Log;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Locale;

@RunWith(KMBRoboElectricTestRunner.class)

public class PassengerCarrying70HourTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.US70Hour;

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void test_Standard() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        _rulesetEngine = RulesetFactory.ForUS70Passenger();

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("12/01/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/01/2008 00:00:00,OFF,08:00:00,US70Hour,10:00,15:00,70:00,,,");
        this.ProcessEvent("12/01/2008 08:00:00,ON,01:00:00,US70Hour,10:00,14:00,69:00,,,");
        this.ProcessEvent("12/01/2008 09:00:00,DRV,10:00:00,US70Hour,00:00,04:00,59:00,,,");
        this.ProcessEvent("12/01/2008 19:00:00,ON,01:00:00,US70Hour,00:00,03:00,58:00,,,");
        this.ProcessEvent("12/01/2008 20:00:00,OFF,04:00:00,US70Hour,00:00,03:00,58:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/02/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/02/2008 00:00:00,OFF,04:00:00,US70Hour,10:00,15:00,58:00,,,");
        this.ProcessEvent("12/02/2008 04:00:00,ON,01:00:00,US70Hour,10:00,14:00,57:00,,,");
        this.ProcessEvent("12/02/2008 05:00:00,DRV,02:00:00,US70Hour,08:00,12:00,55:00,,,");
        this.ProcessEvent("12/02/2008 07:00:00,OFF,05:00:00,US70Hour,08:00,12:00,55:00,,,");
        this.ProcessEvent("12/02/2008 12:00:00,ON,01:00:00,US70Hour,08:00,11:00,54:00,,,");
        this.ProcessEvent("12/02/2008 13:00:00,DRV,04:00:00,US70Hour,04:00,07:00,50:00,,,");
        this.ProcessEvent("12/02/2008 17:00:00,ON,02:00:00,US70Hour,04:00,05:00,48:00,,,");
        this.ProcessEvent("12/02/2008 19:00:00,OFF,05:00:00,US70Hour,04:00,05:00,48:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/03/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/03/2008 00:00:00,OFF,00:30:00,US70Hour,04:00,05:00,48:00,,,");
        this.ProcessEvent("12/03/2008 00:30:00,ON,00:30:00,US70Hour,04:00,04:30,47:30,,,");
        this.ProcessEvent("12/03/2008 01:00:00,DRV,05:00:00,US70Hour,00:00,00:00,42:30,,,");
        this.ProcessEvent("12/03/2008 06:00:00,ON,01:00:00,US70Hour,00:00,00:00,41:30,,,");
        this.ProcessEvent("12/03/2008 07:00:00,OFF,17:00:00,US70Hour,10:00,15:00,41:30,,,");

        logProperties.setLogDate(new Date(Date.parse("12/04/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/04/2008 00:00:00,OFF,03:00:00,US70Hour,10:00,15:00,41:30,,,");
        this.ProcessEvent("12/04/2008 03:00:00,ON,01:00:00,US70Hour,10:00,14:00,40:30,,,");
        this.ProcessEvent("12/04/2008 04:00:00,DRV,05:00:00,US70Hour,05:00,09:00,35:30,,,");
        this.ProcessEvent("12/04/2008 09:00:00,ON,01:00:00,US70Hour,05:00,08:00,34:30,,,");
        this.ProcessEvent("12/04/2008 10:00:00,SLP,04:00:00,US70Hour,05:00,08:00,34:30,,,");
        this.ProcessEvent("12/04/2008 14:00:00,ON,01:00:00,US70Hour,05:00,07:00,33:30,,,");
        this.ProcessEvent("12/04/2008 15:00:00,DRV,05:00:00,US70Hour,00:00,02:00,28:30,,,");
        this.ProcessEvent("12/04/2008 20:00:00,ON,01:00:00,US70Hour,00:00,01:00,27:30,,,");
        this.ProcessEvent("12/04/2008 21:00:00,SLP,03:00:00,US70Hour,00:00,01:00,27:30,,,");

        logProperties.setLogDate(new Date(Date.parse("12/05/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/05/2008 00:00:00,SLP,02:00:00,US70Hour,05:00,08:00,27:30,,,");
        this.ProcessEvent("12/05/2008 02:00:00,ON,01:00:00,US70Hour,05:00,07:00,26:30,,,");
        this.ProcessEvent("12/05/2008 03:00:00,DRV,05:00:00,US70Hour,00:00,02:00,21:30,,,");
        this.ProcessEvent("12/05/2008 08:00:00,ON,01:00:00,US70Hour,00:00,01:00,20:30,,,");
        this.ProcessEvent("12/05/2008 09:00:00,SLP,03:00:00,US70Hour,05:00,08:00,20:30,,,");
        this.ProcessEvent("12/05/2008 12:00:00,ON,01:00:00,US70Hour,05:00,07:00,19:30,,,");
        this.ProcessEvent("12/05/2008 13:00:00,DRV,05:00:00,US70Hour,00:00,02:00,14:30,,,");
        this.ProcessEvent("12/05/2008 18:00:00,ON,01:00:00,US70Hour,00:00,01:00,13:30,,,");
        this.ProcessEvent("12/05/2008 19:00:00,SLP,04:00:00,US70Hour,00:00,01:00,13:30,,,");
        this.ProcessEvent("12/05/2008 23:00:00,OFF,01:00:00,US70Hour,00:00,01:00,13:30,,,");

        logProperties.setLogDate(new Date(Date.parse("12/06/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/06/2008 00:00:00,ON,01:00:00,US70Hour,00:00,00:00,12:30,,,");
        this.ProcessEvent("12/06/2008 01:00:00,DRV,03:00:00,US70Hour,00:00,00:00,09:30,,,");
        this.ProcessEvent("12/06/2008 04:00:00,ON,01:00:00,US70Hour,00:00,00:00,08:30,,,");
        this.ProcessEvent("12/06/2008 05:00:00,OFF,19:00:00,US70Hour,10:00,15:00,08:30,,,");

        logProperties.setLogDate(new Date(Date.parse("12/07/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/07/2008 00:00:00,OFF,15:00:00,US70Hour,10:00,15:00,08:30,,,");
        this.ProcessEvent("12/07/2008 15:00:00,ON,01:00:00,US70Hour,10:00,14:00,07:30,,,");
        this.ProcessEvent("12/07/2008 16:00:00,DRV,04:00:00,US70Hour,06:00,10:00,03:30,,,");
        this.ProcessEvent("12/07/2008 20:00:00,ON,00:30:00,US70Hour,06:00,09:30,03:00,,,");
        this.ProcessEvent("12/07/2008 20:30:00,SLP,03:30:00,US70Hour,06:00,09:30,03:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/08/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/08/2008 00:00:00,SLP,04:00:00,US70Hour,06:00,09:30,03:00,,,");
        this.ProcessEvent("12/08/2008 04:00:00,OFF,00:30:00,US70Hour,10:00,15:00,03:00,,,");
        this.ProcessEvent("12/08/2008 04:30:00,ON,00:30:00,US70Hour,10:00,14:30,02:30,,,");
        this.ProcessEvent("12/08/2008 05:00:00,DRV,10:00:00,US70Hour,00:00,04:30,00:00,,,");
        this.ProcessEvent("12/08/2008 15:00:00,ON,01:00:00,US70Hour,00:00,03:30,00:00,,,");
        this.ProcessEvent("12/08/2008 16:00:00,OFF,08:00:00,US70Hour,10:00,15:00,00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/10/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/10/2008 00:00:00,ON,07:00:00,US70Hour,10:00,08:00,06:30,,,");
        this.ProcessEvent("12/10/2008 07:00:00,DRV,06:00:00,US70Hour,04:00,02:00,00:30,,,");
        this.ProcessEvent("12/10/2008 13:00:00,OFF,01:00:00,US70Hour,04:00,02:00,00:30,,,");
        this.ProcessEvent("12/10/2008 14:00:00,SLP,03:00:00,US70Hour,04:00,02:00,00:30,,,");
        this.ProcessEvent("12/10/2008 17:00:00,OFF,02:00:00,US70Hour,04:00,02:00,00:30,,,");
        this.ProcessEvent("12/10/2008 19:00:00,DRV,02:00:00,US70Hour,02:00,00:00,00:00,,,");
        this.ProcessEvent("12/10/2008 21:00:00,ON,01:00:00,US70Hour,02:00,00:00,00:00,,,");
        this.ProcessEvent("12/10/2008 22:00:00,SLP,02:00:00,US70Hour,02:00,00:00,00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/11/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/11/2008 00:00:00,ON,02:00:00,US70Hour,02:00,00:00,02:00,,,");
        this.ProcessEvent("12/11/2008 02:00:00,OFF,01:00:00,US70Hour,02:00,00:00,02:00,,,");
        this.ProcessEvent("12/11/2008 03:00:00,SLP,02:00:00,US70Hour,02:00,00:00,02:00,,,");
        this.ProcessEvent("12/11/2008 05:00:00,ON,01:00:00,US70Hour,02:00,00:00,01:00,,,");
        this.ProcessEvent("12/11/2008 06:00:00,DRV,02:00:00,US70Hour,00:00,00:00,00:00,,,");
        this.ProcessEvent("12/11/2008 08:00:00,ON,01:00:00,US70Hour,00:00,00:00,00:00,,,");
        this.ProcessEvent("12/11/2008 09:00:00,SLP,05:00:00,US70Hour,06:00,06:00,00:00,,,");
        this.ProcessEvent("12/11/2008 14:00:00,ON,01:00:00,US70Hour,06:00,05:00,00:00,,,");
        this.ProcessEvent("12/11/2008 15:00:00,DRV,02:00:00,US70Hour,04:00,03:00,00:00,,,");
        this.ProcessEvent("12/11/2008 17:00:00,SLP,02:00:00,US70Hour,04:00,03:00,00:00,,,");
        this.ProcessEvent("12/11/2008 19:00:00,OFF,02:00:00,US70Hour,04:00,03:00,00:00,,,");
        this.ProcessEvent("12/11/2008 21:00:00,ON,01:00:00,US70Hour,04:00,02:00,00:00,,,");
        this.ProcessEvent("12/11/2008 22:00:00,DRV,02:00:00,US70Hour,02:00,00:00,00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("12/12/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/12/2008 00:00:00,SLP,03:00:00,US70Hour,06:00,09:00,06:00,,,");
        this.ProcessEvent("12/12/2008 03:00:00,ON,01:00:00,US70Hour,06:00,08:00,05:00,,,");
        this.ProcessEvent("12/12/2008 04:00:00,DRV,04:00:00,US70Hour,02:00,04:00,01:00,,,");
        this.ProcessEvent("12/12/2008 08:00:00,ON,01:00:00,US70Hour,02:00,03:00,00:00,,,");
        this.ProcessEvent("12/12/2008 09:00:00,SLP,02:00:00,US70Hour,02:00,03:00,00:00,,,");
        this.ProcessEvent("12/12/2008 11:00:00,OFF,01:00:00,US70Hour,02:00,03:00,00:00,,,");
        this.ProcessEvent("12/12/2008 12:00:00,ON,01:00:00,US70Hour,02:00,02:00,00:00,,,");
        this.ProcessEvent("12/12/2008 13:00:00,DRV,02:00:00,US70Hour,00:00,00:00,00:00,,,");
        this.ProcessEvent("12/12/2008 15:00:00,ON,03:00:00,US70Hour,00:00,00:00,00:00,,,");
        this.ProcessEvent("12/12/2008 18:00:00,SLP,05:00:00,US70Hour,04:00,03:00,00:00,,,");
        this.ProcessEvent("12/12/2008 23:00:00,DRV,01:00:00,US70Hour,03:00,02:00,00:00,,,");


    }


    private void AssertResults(HoursOfServiceSummary summary, String expectedDriveAvailable, String expectedDutyAvailable, String expectedWeeklyAvailable) {

        Assert.assertNotNull(summary);

        long valExpectedDriveAvail = this.ReadTimespan(expectedDriveAvailable);
        long valExpectedDutyAvail = this.ReadTimespan(expectedDutyAvailable);
        long valExpectedWeeklyAvail = this.ReadTimespan(expectedWeeklyAvailable);

        Bundle dailyDriveSummary = _rulesetEngine.DailyDriveSummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualDriveUsed = dailyDriveSummary.getLong("used");
        long actualDriveAvailable = dailyDriveSummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualDriveAllowed = dailyDriveSummary.getInt("allowed");

        Bundle dailyDutySummary = _rulesetEngine.DailyDutySummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualDutyUsed = dailyDutySummary.getLong("used");
        long actualDutyAvailable = dailyDutySummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualDutyAllowed = dailyDutySummary.getInt("allowed");

        Bundle weeklytDutySummary = _rulesetEngine.WeeklyDutySummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualWeeklyUsed = weeklytDutySummary.getLong("used");
        long actualWeeklyAvailable = weeklytDutySummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualWeeklyAllowed = weeklytDutySummary.getInt("allowed");

        Date logEventDate = summary.getRecentDutyTimestamp();
        Assert.assertEquals(String.format(Locale.US, " Daily Drive Hours Available %s expected: %s actual: %s", logEventDate, expectedDriveAvailable, this.ConvertToHoursMinutesSeconds(actualDriveAvailable)), valExpectedDriveAvail, actualDriveAvailable);
        Assert.assertEquals(String.format(Locale.US, " Daily Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedDutyAvailable, this.ConvertToHoursMinutesSeconds(actualDutyAvailable)), valExpectedDutyAvail, actualDutyAvailable);
        Assert.assertEquals(String.format(Locale.US, " Weekly Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedWeeklyAvailable, this.ConvertToHoursMinutesSeconds(actualWeeklyAvailable)), valExpectedWeeklyAvail, actualWeeklyAvailable);
    }


    private void ProcessEvent(String eventInfo) {
        String[] tokens = eventInfo.split(",");

        Date eventTimestamp = this.ReadTimestamp(tokens[0]);
        DutyStatusEnum dutyStatus = this.ReadDutyStatus(tokens[1]);
        long eventDuration = this.ReadTimespan(tokens[2]);

        HoursOfServiceSummary summary = _rulesetEngine.EndOfDutyStatusUpdate(eventTimestamp, dutyStatus, eventDuration, _ruleset);
        this.AssertResults(summary, tokens[4], tokens[5], tokens[6]);
    }

    private Date ReadTimestamp(String obj) {
        Date answer = null;
        if (obj != null) {
            try {
                answer = DateUtility.getHomeTerminalDateTimeFormat24Hour().parse(obj);
            } catch (Exception e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }
        return answer;
    }

    private long ReadTimespan(String obj) {
        long answer = -1;
        if (obj != null) {
            try {
                long hrs;
                long min;
                long sec;

                String[] tokens = obj.split(":");
                switch (tokens.length) {
                    case 2:
                        hrs = Integer.parseInt(tokens[0]) * 60 * 60;  // Convert hrs to sec
                        min = Integer.parseInt(tokens[1]) * 60;  // Convert min to sec
                        answer = (hrs + min) * 1000;  // Convert sec to milliseconds
                        break;
                    case 3:
                        if (tokens[0].contains(".")) {
                            String[] days_hours = tokens[0].split("\\.");
                            hrs = ((Integer.parseInt(days_hours[0]) * 24 * 60 * 60) + (Integer.parseInt(days_hours[1]) * 60 * 60));  // Convert days/hours to seconds
                        } else
                            hrs = Integer.parseInt(tokens[0]) * 60 * 60;  // Convert hours to seconds
                        min = Integer.parseInt(tokens[1]) * 60; // Convert min to sec
                        sec = Integer.parseInt(tokens[2]);
                        answer = (hrs + min + sec) * 1000; // Convert sec to milliseconds
                        break;
                }
            } catch (Exception e) {

                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        } else {
            answer = 1;
        }
        return answer;
    }

    private DutyStatusEnum ReadDutyStatus(String obj) {
        DutyStatusEnum answer = DutyStatusEnum.OFF;
        if (obj != null) {
            try {
                answer = DutyStatusEnum.valueOf(obj);
            } catch (Exception e) {

                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }
        return answer;
    }


    private String ConvertToHoursMinutesSeconds(long milliseconds) {
        long x = milliseconds / 1000;
        int seconds = (int) (x % 60);
        x /= 60;
        int minutes = (int) (x % 60);
        x /= 60;
        int hours = (int) (x % 24);
        x /= 24;
        int days = (int) x;

        String answer;
        if (days > 0)
            answer = String.format(Locale.US, "%d.%02d:%02d:%02d", days, hours, minutes, seconds);
        else
            answer = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        return answer;
    }

}
