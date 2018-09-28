package com.jjkeller.kmb.test.kmbapi.calcengine.Canadian;

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

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Locale;

@RunWith(KMBRoboElectricTestRunner.class)
public class Cycle2TeamDriverTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.Canadian_Cycle2;

    @Test
    public void test_Standard() {
        _rulesetEngine = RulesetFactory.ForCanadianCycle2(true);
        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("08/26/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/26/2009 00:00:00,OFF,24:00:00,Canadian_Cycle2,13:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("08/27/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/27/2009 00:00:00,OFF,24:00:00,Canadian_Cycle2,13:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("08/28/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/28/2009 00:00:00,OFF,08:00:00,Canadian_Cycle2,13:00,14:00,70:00,,,");
        this.ProcessEvent("08/28/2009 08:00:00,ON,00:30:00,Canadian_Cycle2,13:00,13:30,69:30,,,");
        this.ProcessEvent("08/28/2009 08:30:00,DRV,01:00:00,Canadian_Cycle2,12:00,12:30,68:30,,,");
        this.ProcessEvent("08/28/2009 09:30:00,SLP,07:00:00,Canadian_Cycle2,12:00,07:30,68:30,,,");
        this.ProcessEvent("08/28/2009 16:30:00,ON,00:30:00,Canadian_Cycle2,12:00,07:00,68:00,,,");
        this.ProcessEvent("08/28/2009 17:00:00,DRV,01:00:00,Canadian_Cycle2,11:00,06:00,67:00,,,");
        this.ProcessEvent("08/28/2009 18:00:00,SLP,03:00:00,Canadian_Cycle2,11:00,03:00,67:00,,,");
        this.ProcessEvent("08/28/2009 21:00:00,ON,00:30:00,Canadian_Cycle2,11:00,02:30,66:30,,,");
        this.ProcessEvent("08/28/2009 21:30:00,DRV,02:30:00,Canadian_Cycle2,08:30,00:00,64:00,,,");

        logProperties.setLogDate(new Date(Date.parse("08/29/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/29/2009 00:00:00,DRV,00:30:00,Canadian_Cycle2,08:00,00:00,63:30,,,");
        this.ProcessEvent("08/29/2009 00:30:00,SLP,04:00:00,Canadian_Cycle2,09:00,08:00,63:30,,,");
        this.ProcessEvent("08/29/2009 04:30:00,ON,01:30:00,Canadian_Cycle2,09:00,06:30,62:00,,,");
        this.ProcessEvent("08/29/2009 06:00:00,DRV,01:00:00,Canadian_Cycle2,08:00,05:30,61:00,,,");
        this.ProcessEvent("08/29/2009 07:00:00,SLP,07:00:00,Canadian_Cycle2,11:30,11:00,61:00,,,");
        this.ProcessEvent("08/29/2009 14:00:00,ON,00:30:00,Canadian_Cycle2,11:30,10:30,60:30,,,");
        this.ProcessEvent("08/29/2009 14:30:00,DRV,03:30:00,Canadian_Cycle2,08:00,07:00,57:00,,,");
        this.ProcessEvent("08/29/2009 18:00:00,ON,01:00:00,Canadian_Cycle2,08:00,06:00,56:00,,,");
        this.ProcessEvent("08/29/2009 19:00:00,OFF,05:00:00,Canadian_Cycle2,08:00,03:30,56:00,,,");

        logProperties.setLogDate(new Date(Date.parse("08/30/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/30/2009 00:00:00,ON,00:30:00,Canadian_Cycle2,08:30,03:00,55:30,,,");
        this.ProcessEvent("08/30/2009 00:30:00,OFF,02:00:00,Canadian_Cycle2,08:30,01:00,55:30,,,");
        this.ProcessEvent("08/30/2009 02:30:00,SLP,02:00:00,Canadian_Cycle2,08:30,00:00,55:30,,,");
        this.ProcessEvent("08/30/2009 04:30:00,ON,01:30:00,Canadian_Cycle2,08:30,00:00,54:00,,,");
        this.ProcessEvent("08/30/2009 06:00:00,DRV,01:00:00,Canadian_Cycle2,07:30,00:00,53:00,,,");
        this.ProcessEvent("08/30/2009 07:00:00,SLP,07:00:00,Canadian_Cycle2,08:30,00:00,53:00,,,");
        this.ProcessEvent("08/30/2009 14:00:00,ON,00:30:00,Canadian_Cycle2,08:30,00:00,52:30,,,");
        this.ProcessEvent("08/30/2009 14:30:00,DRV,03:30:00,Canadian_Cycle2,05:00,00:00,49:00,,,");
        this.ProcessEvent("08/30/2009 18:00:00,ON,01:00:00,Canadian_Cycle2,05:00,00:00,48:00,,,");
        this.ProcessEvent("08/30/2009 19:00:00,OFF,05:00:00,Canadian_Cycle2,05:00,00:00,48:00,,,");

        logProperties.setLogDate(new Date(Date.parse("08/31/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("08/31/2009 00:00:00,DRV,00:30:00,Canadian_Cycle2,04:30,00:00,47:30,,,");
        this.ProcessEvent("08/31/2009 00:30:00,SLP,04:00:00,Canadian_Cycle2,09:00,05:30,47:30,,,");
        this.ProcessEvent("08/31/2009 04:30:00,ON,01:30:00,Canadian_Cycle2,09:00,04:00,46:00,,,");
        this.ProcessEvent("08/31/2009 06:00:00,OFF,01:00:00,Canadian_Cycle2,09:00,03:00,46:00,,,");
        this.ProcessEvent("08/31/2009 07:00:00,ON,01:30:00,Canadian_Cycle2,09:00,01:30,44:30,,,");
        this.ProcessEvent("08/31/2009 08:30:00,SLP,01:30:00,Canadian_Cycle2,09:00,00:00,44:30,,,");
        this.ProcessEvent("08/31/2009 10:00:00,ON,03:00:00,Canadian_Cycle2,09:00,00:00,41:30,,,");
        this.ProcessEvent("08/31/2009 13:00:00,DRV,01:30:00,Canadian_Cycle2,07:30,00:00,40:00,,,");
        this.ProcessEvent("08/31/2009 14:30:00,SLP,07:00:00,Canadian_Cycle2,11:00,06:00,40:00,,,");
        this.ProcessEvent("08/31/2009 21:30:00,ON,01:00:00,Canadian_Cycle2,11:00,05:00,39:00,,,");
        this.ProcessEvent("08/31/2009 22:30:00,OFF,01:30:00,Canadian_Cycle2,11:00,03:30,39:00,,,");

        logProperties.setLogDate(new Date(Date.parse("09/01/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("09/01/2009 00:00:00,OFF,24:00:00,Canadian_Cycle2,13:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("09/02/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("09/02/2009 00:00:00,OFF,24:30:00,Canadian_Cycle2,13:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("09/03/2009")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("09/03/2009 00:00:00,DRV,00:30:00,Canadian_Cycle2,12:30,13:30,69:30,,,");
        this.ProcessEvent("09/03/2009 00:30:00,SLP,04:00:00,Canadian_Cycle2,12:30,11:30,69:30,,,");
        this.ProcessEvent("09/03/2009 04:30:00,ON,01:30:00,Canadian_Cycle2,12:30,10:00,68:00,,,");
        this.ProcessEvent("09/03/2009 06:00:00,DRV,01:00:00,Canadian_Cycle2,11:30,09:00,67:00,,,");
        this.ProcessEvent("09/03/2009 07:00:00,ON,01:30:00,Canadian_Cycle2,11:30,07:30,65:30,,,");
        this.ProcessEvent("09/03/2009 08:30:00,SLP,02:00:00,Canadian_Cycle2,11:30,05:30,65:30,,,");
        this.ProcessEvent("09/03/2009 10:30:00,ON,03:00:00,Canadian_Cycle2,11:30,02:30,62:30,,,");
        this.ProcessEvent("09/03/2009 13:30:00,DRV,01:30:00,Canadian_Cycle2,10:00,01:00,61:00,,,");
        this.ProcessEvent("09/03/2009 15:00:00,SLP,07:00:00,Canadian_Cycle2,10:00,05:00,61:00,,,");
        this.ProcessEvent("09/03/2009 22:00:00,ON,01:00:00,Canadian_Cycle2,10:00,04:00,60:00,,,");
        this.ProcessEvent("09/03/2009 23:00:00,OFF,01:00:00,Canadian_Cycle2,10:00,03:30,60:00,,,");
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
