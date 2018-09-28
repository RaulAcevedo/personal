package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import junit.framework.Assert;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public abstract class ExemptTestBase extends KmbRoboTestBase {
    IHosRulesetCalcEngine _rulesetEngine = null;
    RuleSetTypeEnum _ruleset = RuleSetTypeEnum.Null;

    void processEventAndAssertResults(String eventDateTimeString, String eventDurationString, DutyStatusEnum dutyStatus,
                                      String expectedDriveAvailableString, String expectedDutyAvailableString, String expectedWeeklyAvailableString) throws ParseException {

        Date eventTimestamp = DateUtility.getHomeTerminalDateTimeFormat().parse(eventDateTimeString);
        long eventDuration = DateUtility.getMillisecondsFromHHmm(eventDurationString);

        HoursOfServiceSummary summary = _rulesetEngine.EndOfDutyStatusUpdate(eventTimestamp, dutyStatus, eventDuration, _ruleset);
        this.assertResults(summary, DateUtility.getMillisecondsFromHHmm(expectedDriveAvailableString), DateUtility.getMillisecondsFromHHmm(expectedDutyAvailableString), DateUtility.getMillisecondsFromHHmm(expectedWeeklyAvailableString));
    }

    private void assertResults(HoursOfServiceSummary summary, long expectedDriveAvailable, long expectedDutyAvailable, long expectedWeeklyAvailable) {

        Assert.assertNotNull(summary);

        Bundle dailyDriveSummary = _rulesetEngine.DailyDriveSummary(summary);
        //long actualDriveUsed = dailyDriveSummary.getLong("used");
        long actualDriveAvailable = dailyDriveSummary.getLong("avail");
        //int actualDriveAllowed = dailyDriveSummary.getInt("allowed");

        Bundle dailyDutySummary = _rulesetEngine.DailyDutySummary(summary);
        //long actualDutyUsed = dailyDutySummary.getLong("used");
        long actualDutyAvailable = dailyDutySummary.getLong("avail");
        //int actualDutyAllowed = dailyDutySummary.getInt("allowed");

        Bundle weeklytDutySummary = _rulesetEngine.WeeklyDutySummary(summary);
        //long actualWeeklyUsed = weeklytDutySummary.getLong("used");
        long actualWeeklyAvailable = weeklytDutySummary.getLong("avail");
        //int actualWeeklyAllowed = weeklytDutySummary.getInt("allowed");

        Date logEventDate = summary.getRecentDutyTimestamp();
        Assert.assertEquals(String.format(Locale.US, " daily drive available (%s): %s (expected)<>%s (actual)", logEventDate,
                DateUtility.getHHmmssFromMilliseconds(expectedDriveAvailable), DateUtility.getHHmmssFromMilliseconds(actualDriveAvailable)),
                expectedDriveAvailable, actualDriveAvailable);
        Assert.assertEquals(String.format(Locale.US, " daily duty available (%s): %s (expected)<>%s (actual)", logEventDate,
                DateUtility.getHHmmssFromMilliseconds(expectedDutyAvailable), DateUtility.getHHmmssFromMilliseconds(actualDutyAvailable)),
                expectedDutyAvailable, actualDutyAvailable);
        Assert.assertEquals(String.format(Locale.US, " weekly duty available (%s): %s (expected)<>%s (actual)", logEventDate,
                DateUtility.getHHmmssFromMilliseconds(expectedWeeklyAvailable), DateUtility.getHHmmssFromMilliseconds(actualWeeklyAvailable)),
                expectedWeeklyAvailable, actualWeeklyAvailable);
    }
}
