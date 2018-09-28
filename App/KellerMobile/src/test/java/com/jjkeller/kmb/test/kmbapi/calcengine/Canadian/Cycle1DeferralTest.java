package com.jjkeller.kmb.test.kmbapi.calcengine.Canadian;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
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
public class Cycle1DeferralTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.Canadian_Cycle1;

    @Test
    public void test_Standard() {
        _rulesetEngine = RulesetFactory.ForCanadianCycle1(false);
        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("03/11/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/11/2009 00:00:00,ON,12:00:00,Canadian_Cycle1,13:00,2:00,58:00:00,DayOne,");
        this.ProcessEvent("03/11/2009 12:00:00,DRV,2:00:00,Canadian_Cycle1,11:00,0:00,56:00:00,,");
        this.ProcessEvent("03/11/2009 14:00:00,ON,2:00:00,Canadian_Cycle1,11:00,0:00,54:00:00,,");
        this.ProcessEvent("03/11/2009 16:00:00,OFF,8:00:00,Canadian_Cycle1,13:00,14:00,54:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/12/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/12/2009 00:00:00,DRV,13:00:00,Canadian_Cycle1,0:00,1:00,41:00:00,DayTwo,");
        this.ProcessEvent("03/12/2009 13:00:00,ON,1:00:00,Canadian_Cycle1,0:00,0:00,40:00:00,,");
        this.ProcessEvent("03/12/2009 14:00:00,OFF,10:00:00,Canadian_Cycle1,11:00,14:00,40:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/13/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/13/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,40:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/14/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/14/2009 00:00:00,DRV,8:00:00,Canadian_Cycle1,5:00,6:00,32:00:00,DayOne,");
        this.ProcessEvent("03/14/2009 08:00:00,OFF,8:00:00,Canadian_Cycle1,13:00,14:00,32:00:00,,");
        this.ProcessEvent("03/14/2009 16:00:00,DRV,8:00:00,Canadian_Cycle1,5:00,6:00,24:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/15/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/15/2009 00:00:00,DRV,5:00:00,Canadian_Cycle1,0:00,1:00,19:00,DayTwo,");
        this.ProcessEvent("03/15/2009 05:00:00,OFF,12:00:00,Canadian_Cycle1,5:00,14:00,19:00,,");
        this.ProcessEvent("03/15/2009 17:00:00,DRV,7:00:00,Canadian_Cycle1,0:00,7:00,12:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/16/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/16/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,12:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/17/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/17/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/18/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/18/2009 00:00:00,OFF,1:00:00,Canadian_Cycle1,13:00,14:00,70:00:00,DayOne,");
        this.ProcessEvent("03/18/2009 01:00:00,ON,1:00:00,Canadian_Cycle1,13:00,13:00,69:00:00,,");
        this.ProcessEvent("03/18/2009 02:00:00,DRV,5:00:00,Canadian_Cycle1,8:00,8:00,64:00:00,,");
        this.ProcessEvent("03/18/2009 07:00:00,ON,1:00:00,Canadian_Cycle1,8:00,7:00,63:00:00,,");
        this.ProcessEvent("03/18/2009 08:00:00,SLP,8:00:00,Canadian_Cycle1,13:00,14:00,63:00:00,,");
        this.ProcessEvent("03/18/2009 16:00:00,ON,1:00:00,Canadian_Cycle1,13:00,13:00,62:00:00,,");
        this.ProcessEvent("03/18/2009 17:00:00,DRV,7:00:00,Canadian_Cycle1,6:00,6:00,55:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/19/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/19/2009 00:00:00,OFF,4:00:00,Canadian_Cycle1,6:00,4:00,55:00:00,DayTwo,");
        this.ProcessEvent("03/19/2009 04:00:00,ON,2:00:00,Canadian_Cycle1,6:00,2:00,53:00:00,,");
        this.ProcessEvent("03/19/2009 06:00:00,DRV,2:00:00,Canadian_Cycle1,4:00,0:00,51:00:00,,");
        this.ProcessEvent("03/19/2009 08:00:00,ON,1:00:00,Canadian_Cycle1,4:00,0:00,50:00:00,,");
        this.ProcessEvent("03/19/2009 09:00:00,OFF,9:00:00,Canadian_Cycle1,12:00,14:00,50:00:00,,");
        this.ProcessEvent("03/19/2009 18:00:00,ON,3:00:00,Canadian_Cycle1,12:00,11:00,47:00:00,,");
        this.ProcessEvent("03/19/2009 21:00:00,DRV,3:00:00,Canadian_Cycle1,9:00,8:00,44:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/20/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/20/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,44:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/21/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/21/2009 00:00:00,OFF,4:00:00,Canadian_Cycle1,13:00,14:00,44:00:00,DayOne,");
        this.ProcessEvent("03/21/2009 04:00:00,ON,2:00:00,Canadian_Cycle1,13:00,12:00,42:00:00,,");
        this.ProcessEvent("03/21/2009 06:00:00,DRV,2:00:00,Canadian_Cycle1,11:00,10:00,40:00:00,,");
        this.ProcessEvent("03/21/2009 08:00:00,ON,1:00:00,Canadian_Cycle1,11:00,9:00,39:00:00,,");
        this.ProcessEvent("03/21/2009 09:00:00,OFF,9:00:00,Canadian_Cycle1,13:00,14:00,39:00:00,,");
        this.ProcessEvent("03/21/2009 18:00:00,ON,3:00:00,Canadian_Cycle1,13:00,11:00,36:00:00,,");
        this.ProcessEvent("03/21/2009 21:00:00,DRV,3:00:00,Canadian_Cycle1,10:00,8:00,33:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/22/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/22/2009 00:00:00,DRV,1:00:00,Canadian_Cycle1,9:00,7:00,32:00:00,DayTwo, ");
        this.ProcessEvent("03/22/2009 01:00:00,OFF,4:00:00,Canadian_Cycle1,9:00,5:00,32:00:00,,");
        this.ProcessEvent("03/22/2009 05:00:00,ON,1:00:00,Canadian_Cycle1,9:00,4:00,31:00:00,,");
        this.ProcessEvent("03/22/2009 06:00:00,DRV,3:00:00,Canadian_Cycle1,6:00,1:00,28:00:00,,");
        this.ProcessEvent("03/22/2009 09:00:00,ON,1:00:00,Canadian_Cycle1,6:00,0:00,27:00:00,,");
        this.ProcessEvent("03/22/2009 10:00:00,OFF,9:00:00,Canadian_Cycle1,13:00,14:00,27:00:00,,");
        this.ProcessEvent("03/22/2009 19:00:00,ON,1:00:00,Canadian_Cycle1,13:00,13:00,26:00:00,,");
        this.ProcessEvent("03/22/2009 20:00:00,DRV,4:00:00,Canadian_Cycle1,9:00,9:00,22:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/23/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/23/2009 00:00:00,DRV,6:00:00,Canadian_Cycle1,3:00,3:00,16:00,DayOne,");
        this.ProcessEvent("03/23/2009 06:00:00,ON,1:00:00,Canadian_Cycle1,3:00,2:00,15:00,,");
        this.ProcessEvent("03/23/2009 07:00:00,OFF,8:00:00,Canadian_Cycle1,13:00,14:00,15:00,,");
        this.ProcessEvent("03/23/2009 15:00:00,ON,3:00:00,Canadian_Cycle1,13:00,11:00,12:00,,");
        this.ProcessEvent("03/23/2009 18:00:00,DRV,6:00:00,Canadian_Cycle1,7:00,5:00,6:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/24/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/24/2009 00:00:00,DRV,3:00:00,Canadian_Cycle1,4:00,2:00,3:00,DayTwo,");
        this.ProcessEvent("03/24/2009 03:00:00,ON,2:00:00,Canadian_Cycle1,4:00,0:00,1:00,,");
        this.ProcessEvent("03/24/2009 05:00:00,DRV,2:00:00,Canadian_Cycle1,2:00,0:00,0:00,,");
        this.ProcessEvent("03/24/2009 07:00:00,ON,2:00:00,Canadian_Cycle1,2:00,0:00,0:00,,");
        this.ProcessEvent("03/24/2009 09:00:00,OFF,10:00:00,Canadian_Cycle1,9:00,14:00,0:00,,");
        this.ProcessEvent("03/24/2009 19:00:00,ON,1:00:00,Canadian_Cycle1,9:00,13:00,0:00,,");
        this.ProcessEvent("03/24/2009 20:00:00,DRV,4:00:00,Canadian_Cycle1,5:00,9:00,0:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/25/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/25/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,7:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/26/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/26/2009 00:00:00,OFF,24:00:00,Canadian_Cycle1,13:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/27/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/27/2009 00:00:00,DRV,13:00:00,Canadian_Cycle1,0:00,1:00,57:00:00,DayOne,");
        this.ProcessEvent("03/27/2009 13:00:00,OFF,1:00:00,Canadian_Cycle1,0:00,1:00,57:00:00,,");
        this.ProcessEvent("03/27/2009 14:00:00,ON,3:00:00,Canadian_Cycle1,0:00,0:00,54:00:00,,");
        this.ProcessEvent("03/27/2009 17:00:00,OFF,7:00:00,Canadian_Cycle1,0:00,0:00,54:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/28/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/28/2009 00:00:00,OFF,1:00:00,Canadian_Cycle1,13:00,14:00,54:00:00,DayTwo,");
        this.ProcessEvent("03/28/2009 01:00:00,DRV,6:00:00,Canadian_Cycle1,7:00,8:00,48:00:00,,");
        this.ProcessEvent("03/28/2009 07:00:00,OFF,2:00:00,Canadian_Cycle1,7:00,8:00,48:00:00,,");
        this.ProcessEvent("03/28/2009 09:00:00,DRV,4:00:00,Canadian_Cycle1,3:00,4:00,44:00:00,,");
        this.ProcessEvent("03/28/2009 13:00:00,OFF,11:00:00,Canadian_Cycle1,3:00,14:00,44:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/29/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayOne);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/29/2009 00:00:00,OFF,3:00:00,Canadian_Cycle1,13:00,14:00,44:00:00,DayOne,");
        this.ProcessEvent("03/29/2009 03:00:00,DRV,14:00:00,Canadian_Cycle1,0:00,0:00,30:00:00,,");
        this.ProcessEvent("03/29/2009 17:00:00,OFF,7:00:00,Canadian_Cycle1,0:00,0:00,30:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/30/2009")));
        logProperties.setCanadaDeferralType(CanadaDeferralTypeEnum.DayTwo);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/30/2009 00:00:00,OFF,1:00:00,Canadian_Cycle1,12:00,14:00,30:00:00,DayTwo,");
        this.ProcessEvent("03/30/2009 01:00:00,DRV,12:00:00,Canadian_Cycle1,0:00,2:00,18:00,,");
        this.ProcessEvent("03/30/2009 13:00:00,ON,1:00:00,Canadian_Cycle1,0:00,1:00,17:00,,");
        this.ProcessEvent("03/30/2009 14:00:00,OFF,10:00:00,Canadian_Cycle1,0:00,14:00,17:00,,");
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
