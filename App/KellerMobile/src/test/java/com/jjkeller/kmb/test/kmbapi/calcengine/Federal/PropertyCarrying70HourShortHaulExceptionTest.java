package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.os.Bundle;

import android.util.Log;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.Federal.PropertyCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Locale;

@RunWith(KMBRoboElectricTestRunner.class)
public class PropertyCarrying70HourShortHaulExceptionTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.US70Hour;

    //@Mock private LogCheckerComplianceDatesController mockComplianceDatesController;
    private ILogCheckerComplianceDatesController mockComplianceDatesController;

    @Before
    public void setUp() throws Exception {

        //MockitoAnnotations.initMocks(this);
        //mockComplianceDatesController = mock(LogCheckerComplianceDatesController.class);
    }


    @Test
    public void test_ShortHaulException_FirstDay() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        // 	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsTodaysLog(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,70:00,,TRUE,FALSE");
        this.ProcessEventAndAssert("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,69:00,,TRUE,");
        this.ProcessEventAndAssert("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,65:00,,TRUE,");
        this.ProcessEventAndAssert("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,64:00,,TRUE,");
        this.ProcessEventAndAssert("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,64:00,,TRUE,");
    }

    @Test
    public void test_ShortHaulException_FirstException() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/02/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/02/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,64:00,,TRUE,FALSE");
        this.ProcessEvent("01/02/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,63:00,,TRUE,");
        this.ProcessEvent("01/02/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,59:00,,TRUE,");
        this.ProcessEvent("01/02/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,58:00,,TRUE,");
        this.ProcessEvent("01/02/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,58:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/03/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,58:00,,TRUE,FALSE");
        this.ProcessEvent("01/03/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,57:00,,TRUE,");
        this.ProcessEvent("01/03/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,53:00,,TRUE,");
        this.ProcessEvent("01/03/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,52:00,,TRUE,");
        this.ProcessEvent("01/03/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,52:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/04/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,52:00,,TRUE,FALSE");
        this.ProcessEvent("01/04/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,51:00,,TRUE,");
        this.ProcessEvent("01/04/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,47:00,,TRUE,");
        this.ProcessEvent("01/04/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,46:00,,TRUE,");
        this.ProcessEvent("01/04/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/05/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/05/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,46:00,,TRUE,FALSE");
        this.ProcessEvent("01/05/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,45:00,,TRUE,");
        this.ProcessEvent("01/05/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,41:00,,TRUE,");
        this.ProcessEvent("01/05/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,40:00,,TRUE,");
        this.ProcessEvent("01/05/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,40:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2009")));
        logProperties.setIsTodaysLog(true);
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/06/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,40:00,,TRUE,TRUE");
        this.ProcessEventAndAssert("01/06/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,39:00,,TRUE,");
        this.ProcessEventAndAssert("01/06/2009 05:00:00,DRV,04:00:00,US70Hour,07:00,11:00,35:00,,TRUE,");
        this.ProcessEventAndAssert("01/06/2009 09:00:00,ON,03:00:00,US70Hour,07:00,08:00,32:00,,TRUE,");
        this.ProcessEventAndAssert("01/06/2009 12:00:00,DRV,07:00:00,US70Hour,00:00,01:00,25:00,,TRUE,");
        this.ProcessEventAndAssert("01/06/2009 19:00:00,ON,00:30:00,US70Hour,00:00,00:30,24:30,,TRUE,");
        this.ProcessEventAndAssert("01/06/2009 19:30:00,OFF,04:30:00,US70Hour,00:00,00:00,24:30,,TRUE,");
    }

    @Test
    public void test_ShortHaulException_DayAfterException() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/02/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/02/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,64:00,,TRUE,FALSE");
        this.ProcessEvent("01/02/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,63:00,,TRUE,");
        this.ProcessEvent("01/02/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,59:00,,TRUE,");
        this.ProcessEvent("01/02/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,58:00,,TRUE,");
        this.ProcessEvent("01/02/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,58:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/03/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,58:00,,TRUE,FALSE");
        this.ProcessEvent("01/03/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,57:00,,TRUE,");
        this.ProcessEvent("01/03/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,53:00,,TRUE,");
        this.ProcessEvent("01/03/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,52:00,,TRUE,");
        this.ProcessEvent("01/03/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,52:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/04/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,52:00,,TRUE,FALSE");
        this.ProcessEvent("01/04/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,51:00,,TRUE,");
        this.ProcessEvent("01/04/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,47:00,,TRUE,");
        this.ProcessEvent("01/04/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,46:00,,TRUE,");
        this.ProcessEvent("01/04/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/05/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/05/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,46:00,,TRUE,FALSE");
        this.ProcessEvent("01/05/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,45:00,,TRUE,");
        this.ProcessEvent("01/05/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,41:00,,TRUE,");
        this.ProcessEvent("01/05/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,40:00,,TRUE,");
        this.ProcessEvent("01/05/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,40:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/06/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,40:00,,TRUE,TRUE");
        this.ProcessEvent("01/06/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,39:00,,TRUE,");
        this.ProcessEvent("01/06/2009 05:00:00,DRV,04:00:00,US70Hour,07:00,11:00,35:00,,TRUE,");
        this.ProcessEvent("01/06/2009 09:00:00,ON,03:00:00,US70Hour,07:00,08:00,32:00,,TRUE,");
        this.ProcessEvent("01/06/2009 12:00:00,DRV,07:00:00,US70Hour,00:00,01:00,25:00,,TRUE,");
        this.ProcessEvent("01/06/2009 19:00:00,ON,00:30:00,US70Hour,00:00,00:30,24:30,,TRUE,");
        this.ProcessEvent("01/06/2009 19:30:00,OFF,04:30:00,US70Hour,00:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/07/2009")));
        logProperties.setIsTodaysLog(true);
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/07/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEventAndAssert("01/07/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEventAndAssert("01/07/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEventAndAssert("01/07/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEventAndAssert("01/07/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");
    }

    @Test
    public void test_ShortHaulException_ExceptionAfterWeeklyReset() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/02/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/02/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,64:00,,TRUE,FALSE");
        this.ProcessEvent("01/02/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,63:00,,TRUE,");
        this.ProcessEvent("01/02/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,59:00,,TRUE,");
        this.ProcessEvent("01/02/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,58:00,,TRUE,");
        this.ProcessEvent("01/02/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,58:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/03/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,58:00,,TRUE,FALSE");
        this.ProcessEvent("01/03/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,57:00,,TRUE,");
        this.ProcessEvent("01/03/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,53:00,,TRUE,");
        this.ProcessEvent("01/03/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,52:00,,TRUE,");
        this.ProcessEvent("01/03/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,52:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/04/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,52:00,,TRUE,FALSE");
        this.ProcessEvent("01/04/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,51:00,,TRUE,");
        this.ProcessEvent("01/04/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,47:00,,TRUE,");
        this.ProcessEvent("01/04/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,46:00,,TRUE,");
        this.ProcessEvent("01/04/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/05/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/05/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,46:00,,TRUE,FALSE");
        this.ProcessEvent("01/05/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,45:00,,TRUE,");
        this.ProcessEvent("01/05/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,41:00,,TRUE,");
        this.ProcessEvent("01/05/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,40:00,,TRUE,");
        this.ProcessEvent("01/05/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,40:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/06/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,40:00,,TRUE,TRUE");
        this.ProcessEvent("01/06/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,39:00,,TRUE,");
        this.ProcessEvent("01/06/2009 05:00:00,DRV,04:00:00,US70Hour,07:00,11:00,35:00,,TRUE,");
        this.ProcessEvent("01/06/2009 09:00:00,ON,03:00:00,US70Hour,07:00,08:00,32:00,,TRUE,");
        this.ProcessEvent("01/06/2009 12:00:00,DRV,07:00:00,US70Hour,00:00,01:00,25:00,,TRUE,");
        this.ProcessEvent("01/06/2009 19:00:00,ON,00:30:00,US70Hour,00:00,00:30,24:30,,TRUE,");
        this.ProcessEvent("01/06/2009 19:30:00,OFF,04:30:00,US70Hour,00:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/07/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/07/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEvent("01/07/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEvent("01/07/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEvent("01/07/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEvent("01/07/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/09/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        logProperties.setIsTodaysLog(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/09/2009 00:00:00,OFF,05:00:00,US70Hour,11:00,16:00,70:00,,TRUE,TRUE");
        this.ProcessEventAndAssert("01/09/2009 05:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEventAndAssert("01/09/2009 06:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEventAndAssert("01/09/2009 10:00:00,ON,03:00:00,US70Hour,07:00,08:00,62:00,,TRUE,");
        this.ProcessEventAndAssert("01/09/2009 13:00:00,DRV,07:00:00,US70Hour,00:00,01:00,55:00,,TRUE,");
        this.ProcessEventAndAssert("01/09/2009 20:00:00,ON,00:30:00,US70Hour,00:00,00:30,54:30,,TRUE,");
        this.ProcessEventAndAssert("01/09/2009 20:30:00,OFF,03:30:00,US70Hour,00:00,00:00,54:30,,TRUE,");
    }

    @Test
    public void test_ShortHaulException_WeeklyResetOver8Days() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/02/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/02/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,64:00,,TRUE,FALSE");
        this.ProcessEvent("01/02/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,63:00,,TRUE,");
        this.ProcessEvent("01/02/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,59:00,,TRUE,");
        this.ProcessEvent("01/02/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,58:00,,TRUE,");
        this.ProcessEvent("01/02/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,58:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/03/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,58:00,,TRUE,FALSE");
        this.ProcessEvent("01/03/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,57:00,,TRUE,");
        this.ProcessEvent("01/03/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,53:00,,TRUE,");
        this.ProcessEvent("01/03/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,52:00,,TRUE,");
        this.ProcessEvent("01/03/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,52:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/04/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,52:00,,TRUE,FALSE");
        this.ProcessEvent("01/04/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,51:00,,TRUE,");
        this.ProcessEvent("01/04/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,47:00,,TRUE,");
        this.ProcessEvent("01/04/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,46:00,,TRUE,");
        this.ProcessEvent("01/04/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/05/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/05/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,46:00,,TRUE,FALSE");
        this.ProcessEvent("01/05/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,45:00,,TRUE,");
        this.ProcessEvent("01/05/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,41:00,,TRUE,");
        this.ProcessEvent("01/05/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,40:00,,TRUE,");
        this.ProcessEvent("01/05/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,40:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/06/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,40:00,,TRUE,TRUE");
        this.ProcessEvent("01/06/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,39:00,,TRUE,");
        this.ProcessEvent("01/06/2009 05:00:00,DRV,04:00:00,US70Hour,07:00,11:00,35:00,,TRUE,");
        this.ProcessEvent("01/06/2009 09:00:00,ON,03:00:00,US70Hour,07:00,08:00,32:00,,TRUE,");
        this.ProcessEvent("01/06/2009 12:00:00,DRV,07:00:00,US70Hour,00:00,01:00,25:00,,TRUE,");
        this.ProcessEvent("01/06/2009 19:00:00,ON,00:30:00,US70Hour,00:00,00:30,24:30,,TRUE,");
        this.ProcessEvent("01/06/2009 19:30:00,OFF,04:30:00,US70Hour,00:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/07/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/07/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEvent("01/07/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEvent("01/07/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEvent("01/07/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEvent("01/07/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/09/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/09/2009 00:00:00,OFF,05:00:00,US70Hour,11:00,16:00,70:00,,TRUE,TRUE");
        this.ProcessEvent("01/09/2009 05:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/09/2009 06:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/09/2009 10:00:00,ON,03:00:00,US70Hour,07:00,08:00,62:00,,TRUE,");
        this.ProcessEvent("01/09/2009 13:00:00,DRV,07:00:00,US70Hour,00:00,01:00,55:00,,TRUE,");
        this.ProcessEvent("01/09/2009 20:00:00,ON,00:30:00,US70Hour,00:00,00:30,54:30,,TRUE,");
        this.ProcessEvent("01/09/2009 20:30:00,OFF,03:30:00,US70Hour,00:00,00:00,54:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/10/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/10/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,54:30,,TRUE,FALSE");
        this.ProcessEvent("01/10/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,53:30,,TRUE,");
        this.ProcessEvent("01/10/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,49:30,,TRUE,");
        this.ProcessEvent("01/10/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,48:30,,TRUE,");
        this.ProcessEvent("01/10/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,48:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/11/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/11/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,48:30,,TRUE,FALSE");
        this.ProcessEvent("01/11/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,47:30,,TRUE,");
        this.ProcessEvent("01/11/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,43:30,,TRUE,");
        this.ProcessEvent("01/11/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,42:30,,TRUE,");
        this.ProcessEvent("01/11/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,42:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/12/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/12/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,42:30,,TRUE,FALSE");
        this.ProcessEvent("01/12/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,41:30,,TRUE,");
        this.ProcessEvent("01/12/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,37:30,,TRUE,");
        this.ProcessEvent("01/12/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,36:30,,TRUE,");
        this.ProcessEvent("01/12/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,36:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/13/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/13/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,36:30,,TRUE,FALSE");
        this.ProcessEvent("01/13/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,35:30,,TRUE,");
        this.ProcessEvent("01/13/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,31:30,,TRUE,");
        this.ProcessEvent("01/13/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,30:30,,TRUE,");
        this.ProcessEvent("01/13/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,30:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/14/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/14/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,30:30,,TRUE,FALSE");
        this.ProcessEvent("01/14/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,29:30,,TRUE,");
        this.ProcessEvent("01/14/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,25:30,,TRUE,");
        this.ProcessEvent("01/14/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,24:30,,TRUE,");
        this.ProcessEvent("01/14/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/15/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/15/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEvent("01/15/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEvent("01/15/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEvent("01/15/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEvent("01/15/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/16/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        logProperties.setIsTodaysLog(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/16/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,18:30,,TRUE,TRUE");
        this.ProcessEventAndAssert("01/16/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,17:30,,TRUE,");
        this.ProcessEventAndAssert("01/16/2009 05:00:00,DRV,05:00:00,US70Hour,06:00,10:00,12:30,,TRUE,");
        this.ProcessEventAndAssert("01/16/2009 10:00:00,ON,04:00:00,US70Hour,06:00,06:00,08:30,,TRUE,");
        this.ProcessEventAndAssert("01/16/2009 14:00:00,DRV,05:00:00,US70Hour,01:00,01:00,03:30,,TRUE,");
        this.ProcessEventAndAssert("01/16/2009 19:00:00,ON,01:00:00,US70Hour,01:00,00:00,02:30,,TRUE,");
        this.ProcessEventAndAssert("01/16/2009 20:00:00,OFF,04:00:00,US70Hour,01:00,00:00,02:30,,TRUE,");

    }

    @Test
    public void test_ShortHaulException_CompleteMonth() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, false);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are active in the suspension period
        // This will be what will happen when all of the logs processed fall within the suspension period
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(false);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();
        logProperties.setHasReturnedToWorkLocation(true);

        logProperties.setLogDate(new Date(Date.parse("01/01/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/01/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/01/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/01/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/01/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/01/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/02/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/02/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,64:00,,TRUE,FALSE");
        this.ProcessEvent("01/02/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,63:00,,TRUE,");
        this.ProcessEvent("01/02/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,59:00,,TRUE,");
        this.ProcessEvent("01/02/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,58:00,,TRUE,");
        this.ProcessEvent("01/02/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,58:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/03/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,58:00,,TRUE,FALSE");
        this.ProcessEvent("01/03/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,57:00,,TRUE,");
        this.ProcessEvent("01/03/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,53:00,,TRUE,");
        this.ProcessEvent("01/03/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,52:00,,TRUE,");
        this.ProcessEvent("01/03/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,52:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/04/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,52:00,,TRUE,FALSE");
        this.ProcessEvent("01/04/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,51:00,,TRUE,");
        this.ProcessEvent("01/04/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,47:00,,TRUE,");
        this.ProcessEvent("01/04/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,46:00,,TRUE,");
        this.ProcessEvent("01/04/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/05/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/05/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,46:00,,TRUE,FALSE");
        this.ProcessEvent("01/05/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,45:00,,TRUE,");
        this.ProcessEvent("01/05/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,41:00,,TRUE,");
        this.ProcessEvent("01/05/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,40:00,,TRUE,");
        this.ProcessEvent("01/05/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,40:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/06/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,40:00,,TRUE,TRUE");
        this.ProcessEvent("01/06/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,39:00,,TRUE,");
        this.ProcessEvent("01/06/2009 05:00:00,DRV,04:00:00,US70Hour,07:00,11:00,35:00,,TRUE,");
        this.ProcessEvent("01/06/2009 09:00:00,ON,03:00:00,US70Hour,07:00,08:00,32:00,,TRUE,");
        this.ProcessEvent("01/06/2009 12:00:00,DRV,07:00:00,US70Hour,00:00,01:00,25:00,,TRUE,");
        this.ProcessEvent("01/06/2009 19:00:00,ON,00:30:00,US70Hour,00:00,00:30,24:30,,TRUE,");
        this.ProcessEvent("01/06/2009 19:30:00,OFF,04:30:00,US70Hour,00:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/07/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/07/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEvent("01/07/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEvent("01/07/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEvent("01/07/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEvent("01/07/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/09/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/09/2009 00:00:00,OFF,05:00:00,US70Hour,11:00,16:00,70:00,,TRUE,TRUE");
        this.ProcessEvent("01/09/2009 05:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/09/2009 06:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/09/2009 10:00:00,ON,03:00:00,US70Hour,07:00,08:00,62:00,,TRUE,");
        this.ProcessEvent("01/09/2009 13:00:00,DRV,07:00:00,US70Hour,00:00,01:00,55:00,,TRUE,");
        this.ProcessEvent("01/09/2009 20:00:00,ON,00:30:00,US70Hour,00:00,00:30,54:30,,TRUE,");
        this.ProcessEvent("01/09/2009 20:30:00,OFF,03:30:00,US70Hour,00:00,00:00,54:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/10/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/10/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,54:30,,TRUE,FALSE");
        this.ProcessEvent("01/10/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,53:30,,TRUE,");
        this.ProcessEvent("01/10/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,49:30,,TRUE,");
        this.ProcessEvent("01/10/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,48:30,,TRUE,");
        this.ProcessEvent("01/10/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,48:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/11/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/11/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,48:30,,TRUE,FALSE");
        this.ProcessEvent("01/11/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,47:30,,TRUE,");
        this.ProcessEvent("01/11/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,43:30,,TRUE,");
        this.ProcessEvent("01/11/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,42:30,,TRUE,");
        this.ProcessEvent("01/11/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,42:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/12/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/12/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,42:30,,TRUE,FALSE");
        this.ProcessEvent("01/12/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,41:30,,TRUE,");
        this.ProcessEvent("01/12/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,37:30,,TRUE,");
        this.ProcessEvent("01/12/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,36:30,,TRUE,");
        this.ProcessEvent("01/12/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,36:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/13/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/13/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,36:30,,TRUE,FALSE");
        this.ProcessEvent("01/13/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,35:30,,TRUE,");
        this.ProcessEvent("01/13/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,31:30,,TRUE,");
        this.ProcessEvent("01/13/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,30:30,,TRUE,");
        this.ProcessEvent("01/13/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,30:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/14/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/14/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,30:30,,TRUE,FALSE");
        this.ProcessEvent("01/14/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,29:30,,TRUE,");
        this.ProcessEvent("01/14/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,25:30,,TRUE,");
        this.ProcessEvent("01/14/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,24:30,,TRUE,");
        this.ProcessEvent("01/14/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,24:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/15/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/15/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,24:30,,TRUE,FALSE");
        this.ProcessEvent("01/15/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,23:30,,TRUE,");
        this.ProcessEvent("01/15/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,19:30,,TRUE,");
        this.ProcessEvent("01/15/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,18:30,,TRUE,");
        this.ProcessEvent("01/15/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,18:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/16/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/16/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,18:30,,TRUE,TRUE");
        this.ProcessEvent("01/16/2009 04:00:00,ON,01:00:00,US70Hour,11:00,15:00,17:30,,TRUE,");
        this.ProcessEvent("01/16/2009 05:00:00,DRV,05:00:00,US70Hour,06:00,10:00,12:30,,TRUE,");
        this.ProcessEvent("01/16/2009 10:00:00,ON,04:00:00,US70Hour,06:00,06:00,08:30,,TRUE,");
        this.ProcessEvent("01/16/2009 14:00:00,DRV,05:00:00,US70Hour,01:00,01:00,03:30,,TRUE,");
        this.ProcessEvent("01/16/2009 19:00:00,ON,01:00:00,US70Hour,01:00,00:00,02:30,,TRUE,");
        this.ProcessEvent("01/16/2009 20:00:00,OFF,04:00:00,US70Hour,01:00,00:00,02:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/17/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/17/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,18:00,,TRUE,FALSE");
        this.ProcessEvent("01/17/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,17:00,,TRUE,");
        this.ProcessEvent("01/17/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,13:00,,TRUE,");
        this.ProcessEvent("01/17/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,12:00,,TRUE,");
        this.ProcessEvent("01/17/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,12:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/18/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/18/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,18:00,,TRUE,FALSE");
        this.ProcessEvent("01/18/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,17:00,,TRUE,");
        this.ProcessEvent("01/18/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,13:00,,TRUE,");
        this.ProcessEvent("01/18/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,12:00,,TRUE,");
        this.ProcessEvent("01/18/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,12:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/19/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/19/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,14:00,18:00,,TRUE,FALSE");
        this.ProcessEvent("01/19/2009 04:00:00,ON,01:00:00,US70Hour,11:00,13:00,17:00,,TRUE,");
        this.ProcessEvent("01/19/2009 05:00:00,DRV,03:00:00,US70Hour,08:00,10:00,14:00,,TRUE,");
        this.ProcessEvent("01/19/2009 08:00:00,OFF,06:00:00,US70Hour,08:00,04:00,14:00,,TRUE,");
        this.ProcessEvent("01/19/2009 14:00:00,DRV,05:00:00,US70Hour,03:00,00:00,09:00,,TRUE,");
        this.ProcessEvent("01/19/2009 19:00:00,ON,01:00:00,US70Hour,03:00,00:00,08:00,,TRUE,");
        this.ProcessEvent("01/19/2009 20:00:00,OFF,04:00:00,US70Hour,03:00,00:00,08:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/20/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/20/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,14:00,,TRUE,FALSE");
        this.ProcessEvent("01/20/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,13:00,,TRUE,");
        this.ProcessEvent("01/20/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,09:00,,TRUE,");
        this.ProcessEvent("01/20/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,08:00,,TRUE,");
        this.ProcessEvent("01/20/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,08:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/21/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/21/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,14:00,,TRUE,FALSE");
        this.ProcessEvent("01/21/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,13:00,,TRUE,");
        this.ProcessEvent("01/21/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,09:00,,TRUE,");
        this.ProcessEvent("01/21/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,08:00,,TRUE,");
        this.ProcessEvent("01/21/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,08:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/22/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/22/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,14:00,14:00,,TRUE,FALSE");
        this.ProcessEvent("01/22/2009 09:00:00,ON,01:00:00,US70Hour,11:00,13:00,13:00,,TRUE,");
        this.ProcessEvent("01/22/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,09:00,09:00,,TRUE,");
        this.ProcessEvent("01/22/2009 14:00:00,ON,01:00:00,US70Hour,07:00,08:00,08:00,,TRUE,");
        this.ProcessEvent("01/22/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,00:00,08:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/23/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/23/2009 00:00:00,OFF,02:00:00,US70Hour,11:00,16:00,14:00,,TRUE,FALSE");
        this.ProcessEvent("01/23/2009 02:00:00,ON,03:00:00,US70Hour,11:00,13:00,11:00,,TRUE,");
        this.ProcessEvent("01/23/2009 05:00:00,DRV,05:00:00,US70Hour,06:00,08:00,06:00,,TRUE,");
        this.ProcessEvent("01/23/2009 10:00:00,OFF,03:00:00,US70Hour,06:00,05:00,06:00,,TRUE,");
        this.ProcessEvent("01/23/2009 13:00:00,DRV,05:30:00,US70Hour,00:30,00:00,00:30,,TRUE,");
        this.ProcessEvent("01/23/2009 18:30:00,ON,00:30:00,US70Hour,00:30,00:00,00:00,,TRUE,");
        this.ProcessEvent("01/23/2009 19:00:00,OFF,05:00:00,US70Hour,00:30,00:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/24/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/24/2009 00:00:00,OFF,05:00:00,US70Hour,11:00,16:00,16:00,,TRUE,TRUE");
        this.ProcessEvent("01/24/2009 05:00:00,ON,03:00:00,US70Hour,11:00,13:00,13:00,,TRUE,");
        this.ProcessEvent("01/24/2009 08:00:00,DRV,03:00:00,US70Hour,08:00,10:00,10:00,,TRUE,");
        this.ProcessEvent("01/24/2009 11:00:00,OFF,03:00:00,US70Hour,08:00,07:00,10:00,,TRUE,");
        this.ProcessEvent("01/24/2009 14:00:00,DRV,06:00:00,US70Hour,02:00,01:00,04:00,,TRUE,");
        this.ProcessEvent("01/24/2009 20:00:00,ON,01:00:00,US70Hour,02:00,00:00,03:00,,TRUE,");
        this.ProcessEvent("01/24/2009 21:00:00,OFF,03:00:00,US70Hour,02:00,00:00,03:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/26/2009")));
        logProperties.setIsShortHaulExceptionUsed(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("01/26/2009 00:00:00,OFF,09:00:00,US70Hour,11:00,16:00,70:00,,TRUE,FALSE");
        this.ProcessEvent("01/26/2009 09:00:00,ON,01:00:00,US70Hour,11:00,15:00,69:00,,TRUE,");
        this.ProcessEvent("01/26/2009 10:00:00,DRV,04:00:00,US70Hour,07:00,11:00,65:00,,TRUE,");
        this.ProcessEvent("01/26/2009 14:00:00,ON,01:00:00,US70Hour,07:00,10:00,64:00,,TRUE,");
        this.ProcessEvent("01/26/2009 15:00:00,OFF,09:00:00,US70Hour,07:00,01:00,64:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("01/27/2009")));
        logProperties.setIsShortHaulExceptionUsed(true);
        logProperties.setIsTodaysLog(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEventAndAssert("01/27/2009 00:00:00,OFF,04:00:00,US70Hour,11:00,16:00,64:00,,TRUE,TRUE");
        this.ProcessEventAndAssert("01/27/2009 04:00:00,ON,02:00:00,US70Hour,11:00,14:00,62:00,,TRUE,");
        this.ProcessEventAndAssert("01/27/2009 06:00:00,DRV,06:00:00,US70Hour,05:00,08:00,56:00,,TRUE,");
        this.ProcessEventAndAssert("01/27/2009 12:00:00,ON,03:00:00,US70Hour,05:00,05:00,53:00,,TRUE,");
        this.ProcessEventAndAssert("01/27/2009 15:00:00,DRV,04:00:00,US70Hour,01:00,01:00,49:00,,TRUE,");
        this.ProcessEventAndAssert("01/27/2009 19:00:00,ON,01:00:00,US70Hour,01:00,00:00,48:00,,TRUE,");
        this.ProcessEventAndAssert("01/27/2009 20:00:00,OFF,04:00:00,US70Hour,01:00,00:00,48:00,,TRUE,");
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

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary = _rulesetEngine.EndOfDutyStatusUpdate(eventTimestamp, dutyStatus, eventDuration, _ruleset);
    }

    private void ProcessEventAndAssert(String eventInfo) {
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
            answer = String.format(Locale.US, " %d.%02d:%02d:%02d", days, hours, minutes, seconds);
        else
            answer = String.format(Locale.US, " %02d:%02d:%02d", hours, minutes, seconds);
        return answer;
    }
}
