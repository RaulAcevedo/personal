package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import junit.framework.Assert;

import java.util.Date;
import java.util.Locale;

class ProcessEventHelper extends KmbRoboTestBase {

    public IHosRulesetCalcEngine rulesetEngine = null;
    private Enums.RuleSetTypeEnum ruleset = Enums.RuleSetTypeEnum.Wisconsin_7Day;

    public ProcessEventHelper(Enums.RuleSetTypeEnum ruleSet, IHosRulesetCalcEngine calcEngine) {
        ruleset = ruleSet;
        rulesetEngine = calcEngine;
    }

    private void AssertResults(HoursOfServiceSummary summary, String expectedDriveAvailable, String expectedDutyAvailable, String expectedWeeklyAvailable) {

        Assert.assertNotNull(summary);

        long valExpectedDriveAvail = this.ReadTimespan(expectedDriveAvailable);
        long valExpectedDutyAvail = this.ReadTimespan(expectedDutyAvailable);
        long valExpectedWeeklyAvail = this.ReadTimespan(expectedWeeklyAvailable);

        Bundle dailyDriveSummary = rulesetEngine.DailyDriveSummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualDriveUsed = dailyDriveSummary.getLong("used");
        long actualDriveAvailable = dailyDriveSummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualDriveAllowed = dailyDriveSummary.getInt("allowed");

        Bundle dailyDutySummary = rulesetEngine.DailyDutySummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualDutyUsed = dailyDutySummary.getLong("used");
        long actualDutyAvailable = dailyDutySummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualDutyAllowed = dailyDutySummary.getInt("allowed");

        Bundle weeklytDutySummary = rulesetEngine.WeeklyDutySummary(summary);
        @SuppressWarnings("UnusedAssignment") long actualWeeklyUsed = weeklytDutySummary.getLong("used");
        long actualWeeklyAvailable = weeklytDutySummary.getLong("avail");
        @SuppressWarnings("UnusedAssignment") int actualWeeklyAllowed = weeklytDutySummary.getInt("allowed");

        Date logEventDate = summary.getRecentDutyTimestamp();
        Assert.assertEquals(String.format(Locale.US, " Daily Drive Hours Available %s expected: %s actual: %s", logEventDate, expectedDriveAvailable, this.ConvertToHoursMinutesSeconds(actualDriveAvailable)), valExpectedDriveAvail, actualDriveAvailable);
        Assert.assertEquals(String.format(Locale.US, " Daily Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedDutyAvailable, this.ConvertToHoursMinutesSeconds(actualDutyAvailable)), valExpectedDutyAvail, actualDutyAvailable);
        Assert.assertEquals(String.format(Locale.US, " Weekly Duty Hours Available %s expected: %s actual: %s", logEventDate, expectedWeeklyAvailable, this.ConvertToHoursMinutesSeconds(actualWeeklyAvailable)), valExpectedWeeklyAvail, actualWeeklyAvailable);
    }

    public void ProcessEvent(String eventInfo) {
        String[] tokens = eventInfo.split(",");

        Date eventTimestamp = this.ReadTimestamp(tokens[0]);
        Enums.DutyStatusEnum dutyStatus = this.ReadDutyStatus(tokens[1]);
        long eventDuration = this.ReadTimespan(tokens[2]);

        HoursOfServiceSummary summary = rulesetEngine.EndOfDutyStatusUpdate(eventTimestamp, dutyStatus, eventDuration, ruleset);
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

                String[] tokens = obj.trim().split(":");
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

    private Enums.DutyStatusEnum ReadDutyStatus(String obj) {
        Enums.DutyStatusEnum answer = Enums.DutyStatusEnum.OFF;
        if (obj != null) {
            try {
                answer = Enums.DutyStatusEnum.valueOf(obj);
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
