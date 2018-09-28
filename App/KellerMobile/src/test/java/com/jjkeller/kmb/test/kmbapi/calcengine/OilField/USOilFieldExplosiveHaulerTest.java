package com.jjkeller.kmb.test.kmbapi.calcengine.OilField;

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
public class USOilFieldExplosiveHaulerTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.USOilField;


    @Test
    public void test_ExplosiveHaulers() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        _rulesetEngine = RulesetFactory.ForUSOilField(true, true, true);

        LogProperties logProperties = new LogProperties();
        logProperties.setIsHaulingExplosives(true);

        logProperties.setLogDate(new Date(Date.parse("11/1/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/1/2013 0:00:00,OFF,1.00:00:00,US70Hour,8:00,11:00,8:00,14:00,70:00:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/2/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/2/2013 0:00:00,OFF,10:00:00,US70Hour,8:00,11:00,8:00,14:00,70:00:00,,TRUE");
        this.ProcessEvent("11/2/2013 10:00:00,ON,0:30:00,US70Hour,7:30,11:00,7:30,13:30,69:30:00,,TRUE");
        this.ProcessEvent("11/2/2013 10:30:00,DRV,6:00:00,US70Hour,1:30,5:00,1:30,7:30,63:30:00,,TRUE");
        this.ProcessEvent("11/2/2013 16:30:00,ON,1:30:00,US70Hour,8:00,5:00,5:00,6:00,62:00:00,,TRUE");
        this.ProcessEvent("11/2/2013 18:00:00,DRV,5:00:00,US70Hour,3:00,0:00,0:00,1:00,57:00:00,,TRUE");
        this.ProcessEvent("11/2/2013 23:00:00,ON,0:30:00,US70Hour,8:00,0:00,0:00,0:30,56:30:00,,TRUE");
        this.ProcessEvent("11/2/2013 23:30:00,OFF,0:30:00,US70Hour,8:00,0:00,0:00,0:00,56:30:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/3/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/3/2013 0:00:00,OFF,9:30:00,US70Hour,8:00,11:00,8:00,14:00,56:30:00,,TRUE");
        this.ProcessEvent("11/3/2013 9:30:00,DRV,4:30:00,US70Hour,3:30,6:30,6:00,9:30,52:00:00,,TRUE");
        this.ProcessEvent("11/3/2013 14:00:00,ON,0:15:00,US70Hour,3:15,6:30,3:15,9:15,51:45:00,,TRUE");
        this.ProcessEvent("11/3/2013 14:15:00,OFF,0:15:00,US70Hour,8:00,6:30,8:00,9:00,51:45:00,,TRUE");
        this.ProcessEvent("11/3/2013 14:30:00,DRV,4:30:00,US70Hour,3:30,2:00,4:30,4:30,47:15:00,,TRUE");
        this.ProcessEvent("11/3/2013 19:00:00,OFF,5:00:00,US70Hour,8:00,2:00,4:30,0:00,47:15:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/4/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/4/2013 0:00:00,OFF,5:00:00,US70Hour,8:00,11:00,8:00,14:00,47:15:00,,TRUE");
        this.ProcessEvent("11/4/2013 5:00:00,ON,1:30:00,US70Hour,6:30,11:00,6:30,12:30,45:45:00,,TRUE");
        this.ProcessEvent("11/4/2013 6:30:00,DRV,2:30:00,US70Hour,4:00,8:30,4:00,10:00,43:15:00,,TRUE");
        this.ProcessEvent("11/4/2013 9:00:00,ON,1:30:00,US70Hour,8:00,8:30,8:00,8:30,41:45:00,,TRUE");
        this.ProcessEvent("11/4/2013 10:30:00,DRV,1:30:00,US70Hour,6:30,7:00,6:30,7:00,40:15:00,,TRUE");
        this.ProcessEvent("11/4/2013 12:00:00,OFF,3:00:00,US70Hour,8:00,7:00,7:00,4:00,40:15:00,,TRUE");
        this.ProcessEvent("11/4/2013 15:00:00,ON,0:30:00,US70Hour,8:00,7:00,7:00,3:30,39:45:00,,TRUE");
        this.ProcessEvent("11/4/2013 15:30:00,DRV,4:00:00,US70Hour,4:00,3:00,3:00,0:00,35:45:00,,TRUE");
        this.ProcessEvent("11/4/2013 19:30:00,ON,0:30:00,US70Hour,8:00,3:00,3:00,0:00,35:15:00,,TRUE");
        this.ProcessEvent("11/4/2013 20:00:00,OFF,4:00:00,US70Hour,8:00,3:00,3:00,0:00,35:15:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/5/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/5/2013 0:00:00,OFF,4:00:00,US70Hour,8:00,3:00,3:00,0:00,35:15:00,,TRUE");
        this.ProcessEvent("11/5/2013 4:00:00,SLP,3:00:00,US70Hour,8:00,11:00,8:00,14:00,35:15:00,,TRUE");
        this.ProcessEvent("11/5/2013 7:00:00,ON,0:30:00,US70Hour,7:30,11:00,7:30,13:30,34:45:00,,TRUE");
        this.ProcessEvent("11/5/2013 7:30:00,DRV,3:30:00,US70Hour,4:00,7:30,4:00,10:00,31:15:00,,TRUE");
        this.ProcessEvent("11/5/2013 11:00:00,SLP,2:00:00,US70Hour,8:00,7:30,7:30,8:00,31:15:00,,TRUE");
        this.ProcessEvent("11/5/2013 13:00:00,DRV,8:00:00,US70Hour,0:00,0:00,0:00,0:00,23:15,,TRUE");
        this.ProcessEvent("11/5/2013 21:00:00,ON,0:20:00,US70Hour,0:00,0:00,0:00,0:00,22:55,,TRUE");
        this.ProcessEvent("11/5/2013 21:20:00,SLP,0:10:00,US70Hour,8:00,0:00,0:00,0:00,22:55,,TRUE");
        this.ProcessEvent("11/5/2013 21:30:00,OFF,2:30:00,US70Hour,8:00,0:00,0:00,0:00,22:55,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/6/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/6/2013 0:00:00,OFF,7:30:00,US70Hour,8:00,11:00,8:00,14:00,22:55,,TRUE");
        this.ProcessEvent("11/6/2013 7:30:00,ON,0:15:00,US70Hour,7:45,11:00,7:45,13:45,22:40,,TRUE");
        this.ProcessEvent("11/6/2013 7:45:00,DRV,8:00:00,US70Hour,0:00,3:00,0:00,5:45,14:40,,TRUE");
        this.ProcessEvent("11/6/2013 15:45:00,ON,0:15:00,US70Hour,0:00,3:00,0:00,5:30,14:25,,TRUE");
        this.ProcessEvent("11/6/2013 16:00:00,SLP,1:00:00,US70Hour,8:00,3:00,3:00,4:30,14:25,,TRUE");
        this.ProcessEvent("11/6/2013 17:00:00,OFF,7:00:00,US70Hour,8:00,3:00,3:00,0:00,14:25,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/7/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/7/2013 0:00:00,OFF,5:00:00,US70Hour,8:00,11:00,8:00,14:00,14:25,,TRUE");
        this.ProcessEvent("11/7/2013 5:00:00,ON,0:15:00,US70Hour,7:45,11:00,7:45,13:45,14:10,,TRUE");
        this.ProcessEvent("11/7/2013 5:15:00,SLP,2:45:00,US70Hour,8:00,11:00,8:00,11:00,14:10,,TRUE");
        this.ProcessEvent("11/7/2013 8:00:00,DRV,2:00:00,US70Hour,6:00,9:00,6:00,9:00,12:10,,TRUE");
        this.ProcessEvent("11/7/2013 10:00:00,ON,0:20:00,US70Hour,5:40,9:00,5:40,8:40,11:50,,TRUE");
        this.ProcessEvent("11/7/2013 10:20:00,DRV,2:40:00,US70Hour,3:00,6:20,3:00,6:00,9:10,,TRUE");
        this.ProcessEvent("11/7/2013 13:00:00,ON,1:00:00,US70Hour,8:00,6:20,6:20,5:00,8:10,,TRUE");
        this.ProcessEvent("11/7/2013 14:00:00,OFF,10:00:00,US70Hour,8:00,11:00,8:00,14:00,8:10,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/8/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/8/2013 0:00:00,OFF,1.00:00:00,US70Hour,8:00,11:00,8:00,14:00,70:00:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/9/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/9/2013 0:00:00,OFF,1.00:00:00,US70Hour,8:00,11:00,8:00,14:00,70:00:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/10/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/10/2013 0:00:00,OFF,18:00:00,US70Hour,8:00,11:00,8:00,14:00,70:00:00,,TRUE");
        this.ProcessEvent("11/10/2013 18:00:00,ON,1:00:00,US70Hour,7:00,11:00,7:00,13:00,69:00:00,,TRUE");
        this.ProcessEvent("11/10/2013 19:00:00,DRV,5:00:00,US70Hour,2:00,6:00,2:00,8:00,64:00:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/11/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/11/2013 0:00:00,ON,1:00:00,US70Hour,8:00,6:00,6:00,7:00,63:00:00,,TRUE");
        this.ProcessEvent("11/11/2013 1:00:00,DRV,4:00:00,US70Hour,4:00,2:00,2:00,3:00,59:00:00,,TRUE");
        this.ProcessEvent("11/11/2013 5:00:00,ON,1:00:00,US70Hour,8:00,2:00,2:00,2:00,58:00:00,,TRUE");
        this.ProcessEvent("11/11/2013 6:00:00,OFF,10:00:00,US70Hour,8:00,11:00,8:00,14:00,58:00:00,,TRUE");
        this.ProcessEvent("11/11/2013 16:00:00,ON,0:30:00,US70Hour,7:30,11:00,7:30,13:30,57:30:00,,TRUE");
        this.ProcessEvent("11/11/2013 16:30:00,DRV,1:00:00,US70Hour,6:30,10:00,6:30,12:30,56:30:00,,TRUE");
        this.ProcessEvent("11/11/2013 17:30:00,SLP,2:00:00,US70Hour,8:00,10:00,8:00,10:30,56:30:00,,TRUE");
        this.ProcessEvent("11/11/2013 19:30:00,DRV,0:30:00,US70Hour,7:30,9:30,7:30,10:00,56:00:00,,TRUE");
        this.ProcessEvent("11/11/2013 20:30:00,SLP,3:30:00,US70Hour,8:00,9:30,8:00,6:30,56:00:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/12/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/12/2013 0:00:00,SLP,4:30:00,US70Hour,8:00,10:30,8:00,13:30,56:00:00,,TRUE");
        this.ProcessEvent("11/12/2013 4:30:00,ON,1:00:00,US70Hour,7:00,10:30,7:00,12:30,55:00:00,,TRUE");
        this.ProcessEvent("11/12/2013 5:30:00,DRV,8:00:00,US70Hour,0:00,2:30,0:00,4:30,47:00:00,,TRUE");
        this.ProcessEvent("11/12/2013 13:30:00,ON,0:30:00,US70Hour,8:00,2:30,2:30,4:00,46:30:00,,TRUE");
        this.ProcessEvent("11/12/2013 14:00:00,OFF,11:00:00,US70Hour,8:00,11:00,8:00,14:00,46:30:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/13/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/13/2013 0:00:00,ON,1:00:00,US70Hour,7:00,11:00,7:00,13:00,45:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 1:00:00,SLP,3:00:00,US70Hour,8:00,11:00,8:00,10:00,45:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 4:00:00,DRV,1:00:00,US70Hour,7:00,10:00,7:00,9:00,44:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 5:00:00,SLP,8:00:00,US70Hour,8:00,10:00,8:00,13:00,44:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 13:00:00,ON,1:00:00,US70Hour,7:00,10:00,7:00,12:00,43:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 14:00:00,DRV,8:00:00,US70Hour,0:00,2:00,0:00,4:00,35:30:00,,TRUE");
        this.ProcessEvent("11/13/2013 22:00:00,SLP,2:00:00,US70Hour,8:00,3:00,2:00,5:00,35:30:00,,TRUE");

        logProperties.setLogDate(new Date(Date.parse("11/14/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/14/2013 0:00:00,ON,0:30:00,US70Hour,7:30,3:00,7:30,4:30,35:00:00,,TRUE");
        this.ProcessEvent("11/14/2013 0:30:00,SLP,8:00:00,US70Hour,8:00,11:00,8:00,13:30,35:00:00,,TRUE");
        this.ProcessEvent("11/14/2013 8:00:00,ON,0:30:00,US70Hour,7:30,11:00,7:30,13:00,34:30:00,,TRUE");
        this.ProcessEvent("11/14/2013 9:00:00,DRV,1:00:00,US70Hour,6:30,10:00,6:30,12:00,33:30:00,,TRUE");
        this.ProcessEvent("11/14/2013 10:00:00,SLP,2:00:00,US70Hour,8:00,10:00,8:00,12:30,33:30:00,,TRUE");
        this.ProcessEvent("11/14/2013 12:00:00,ON,2:00:00,US70Hour,6:00,10:00,6:00,10:30,31:30:00,,TRUE");
        this.ProcessEvent("11/14/2013 14:00:00,DRV,6:30:00,US70Hour,0:00,3:30,0:00,4:00,25:00:00,,TRUE");
        this.ProcessEvent("11/14/2013 20:30:00,OFF,3:30:00,US70Hour,8:00,3:30,3:30,0:30,25:00:00,,TRUE");


    }


    private void AssertResults(HoursOfServiceSummary summary, String expectedDriveRestBreakAvailable, String expectedDriveAvailable, String expectedDutyAvailable, String expectedWeeklyAvailable) {

        Assert.assertNotNull(summary);

        long valExpectedDriveAvail = this.ReadTimespan(expectedDriveAvailable);
        long valExpectedDriveRestBreakAvail = this.ReadTimespan(expectedDriveRestBreakAvailable);
        long valExpectedDutyAvail = this.ReadTimespan(expectedDutyAvailable);
        long valExpectedWeeklyAvail = this.ReadTimespan(expectedWeeklyAvailable);

        Bundle dailyDriveSummary = _rulesetEngine.DailyDriveSummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualDriveUsed = dailyDriveSummary.getLong("used");
        long actualDriveAvailable = dailyDriveSummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualDriveAllowed = dailyDriveSummary.getInt("allowed");

        @SuppressWarnings("UnusedAssignment") long actualDriveResetBreakUsed = -1;
        long actualDriveRestBreakAvailable = -1;
        @SuppressWarnings("UnusedAssignment") int actualDriveRestBreakAllowed = -1;
        Bundle dailyDriveRestBreakSummary = _rulesetEngine.DailyDriveRestBreakSummary(summary);
        if (dailyDriveRestBreakSummary != null) {
            if (dailyDriveRestBreakSummary.containsKey("used"))
                //noinspection UnusedAssignment
                actualDriveResetBreakUsed = dailyDriveRestBreakSummary.getLong("used");
            if (dailyDriveRestBreakSummary.containsKey("avail"))
                actualDriveRestBreakAvailable = dailyDriveRestBreakSummary.getLong("avail");
            if (dailyDriveRestBreakSummary.containsKey("allowed"))
                //noinspection UnusedAssignment
                actualDriveRestBreakAllowed = dailyDriveRestBreakSummary.getInt("allowed");
        }

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
        if (actualDriveRestBreakAvailable >= 0)
            Assert.assertEquals(String.format(Locale.US, " Daily Drive Rest Break Hours Available %s expected: %s actual: %s", logEventDate, expectedDriveRestBreakAvailable, this.ConvertToHoursMinutesSeconds(actualDriveRestBreakAvailable)), valExpectedDriveRestBreakAvail, actualDriveRestBreakAvailable);
        Assert.assertEquals(String.format(Locale.US, " Daily Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedDutyAvailable, this.ConvertToHoursMinutesSeconds(actualDutyAvailable)), valExpectedDutyAvail, actualDutyAvailable);
        Assert.assertEquals(String.format(Locale.US, " Weekly Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedWeeklyAvailable, this.ConvertToHoursMinutesSeconds(actualWeeklyAvailable)), valExpectedWeeklyAvail, actualWeeklyAvailable);
    }


    private void ProcessEvent(String eventInfo) {
        String[] tokens = eventInfo.split(",");

        Date eventTimestamp = this.ReadTimestamp(tokens[0]);
        DutyStatusEnum dutyStatus = this.ReadDutyStatus(tokens[1]);
        long eventDuration = this.ReadTimespan(tokens[2]);

        HoursOfServiceSummary summary = _rulesetEngine.EndOfDutyStatusUpdate(eventTimestamp, dutyStatus, eventDuration, _ruleset);
        this.AssertResults(summary, tokens[4], tokens[5], tokens[7], tokens[8]);
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
            answer = String.format(Locale.US, " %d.%02d:%02d:%02d", days, hours, minutes, seconds);
        else
            answer = String.format(Locale.US, " %02d:%02d:%02d", hours, minutes, seconds);
        return answer;
    }
}
