package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.os.Bundle;

import android.util.Log;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.Federal.USConstruction7DayProperty;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Locale;

@RunWith(KMBRoboElectricTestRunner.class)
public class USConstruction7DayPropertyTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.USConstruction_8Day;

    @Test
    public void test_Standard_InSuspension() {

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
        ILogCheckerComplianceDatesController mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new USConstruction7DayProperty(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("11/01/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/01/2008 00:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/02/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/02/2008 00:00:00,OFF,10:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");
        this.ProcessEvent("11/02/2008 10:00:00,ON,00:30:00,US70Hour,11:00,13:30,59:30,,TRUE,");
        this.ProcessEvent("11/02/2008 10:30:00,DRV,06:00:00,US70Hour,05:00,07:30,53:30,,TRUE,");
        this.ProcessEvent("11/02/2008 16:30:00,ON,01:30:00,US70Hour,05:00,06:00,52:00,,TRUE,");
        this.ProcessEvent("11/02/2008 18:00:00,DRV,05:00:00,US70Hour,00:00,01:00,47:00,,TRUE,");
        this.ProcessEvent("11/02/2008 23:00:00,ON,00:30:00,US70Hour,00:00,00:30,46:30,,TRUE,");
        this.ProcessEvent("11/02/2008 23:30:00,OFF,00:30:00,US70Hour,00:00,00:00,46:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/03/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/03/2008 00:00:00,OFF,09:30:00,US70Hour,11:00,14:00,46:30,,TRUE,");
        this.ProcessEvent("11/03/2008 09:30:00,ON,02:00:00,US70Hour,11:00,12:00,44:30,,TRUE,");
        this.ProcessEvent("11/03/2008 11:30:00,DRV,06:30:00,US70Hour,04:30,05:30,38:00,,TRUE,");
        this.ProcessEvent("11/03/2008 18:00:00,ON,01:00:00,US70Hour,04:30,04:30,37:00,,TRUE,");
        this.ProcessEvent("11/03/2008 19:00:00,OFF,05:00:00,US70Hour,04:30,00:00,37:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/04/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/04/2008 00:00:00,OFF,05:00:00,US70Hour,11:00,14:00,37:00,,TRUE,");
        this.ProcessEvent("11/04/2008 05:00:00,ON,01:30:00,US70Hour,11:00,12:30,35:30,,TRUE,");
        this.ProcessEvent("11/04/2008 06:30:00,DRV,02:30:00,US70Hour,08:30,10:00,33:00,,TRUE,");
        this.ProcessEvent("11/04/2008 09:00:00,ON,01:30:00,US70Hour,08:30,08:30,31:30,,TRUE,");
        this.ProcessEvent("11/04/2008 10:30:00,DRV,01:30:00,US70Hour,07:00,07:00,30:00,,TRUE,");
        this.ProcessEvent("11/04/2008 12:00:00,OFF,03:00:00,US70Hour,07:00,04:00,30:00,,TRUE,");
        this.ProcessEvent("11/04/2008 15:00:00,ON,00:30:00,US70Hour,07:00,03:30,29:30,,TRUE,");
        this.ProcessEvent("11/04/2008 15:30:00,DRV,04:00:00,US70Hour,03:00,00:00,25:30,,TRUE,");
        this.ProcessEvent("11/04/2008 19:30:00,ON,00:30:00,US70Hour,03:00,00:00,25:00,,TRUE,");
        this.ProcessEvent("11/04/2008 20:00:00,OFF,04:00:00,US70Hour,03:00,00:00,25:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/05/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/05/2008 00:00:00,OFF,04:00:00,US70Hour,03:00,00:00,25:00,,TRUE,");
        this.ProcessEvent("11/05/2008 04:00:00,SLP,03:00:00,US70Hour,11:00,14:00,25:00,,TRUE,");
        this.ProcessEvent("11/05/2008 07:00:00,ON,00:30:00,US70Hour,11:00,13:30,24:30,,TRUE,");
        this.ProcessEvent("11/05/2008 07:30:00,DRV,03:30:00,US70Hour,07:30,10:00,21:00,,TRUE,");
        this.ProcessEvent("11/05/2008 11:00:00,SLP,02:00:00,US70Hour,07:30,08:00,21:00,,TRUE,");
        this.ProcessEvent("11/05/2008 13:00:00,DRV,08:00:00,US70Hour,00:00,00:00,13:00,,TRUE,");
        this.ProcessEvent("11/05/2008 21:00:00,ON,00:30:00,US70Hour,00:00,00:00,12:30,,TRUE,");
        this.ProcessEvent("11/05/2008 21:30:00,OFF,02:30:00,US70Hour,00:00,00:00,12:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/06/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/06/2008 00:00:00,OFF,07:30:00,US70Hour,11:00,14:00,12:30,,TRUE,");
        this.ProcessEvent("11/06/2008 07:30:00,ON,00:30:00,US70Hour,11:00,13:30,12:00,,TRUE,");
        this.ProcessEvent("11/06/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:30,04:00,,TRUE,");
        this.ProcessEvent("11/06/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:30,03:00,,TRUE,");
        this.ProcessEvent("11/06/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,03:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/07/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/07/2008 00:00:00,OFF,05:00:00,US70Hour,11:00,14:00,03:00,,TRUE,");
        this.ProcessEvent("11/07/2008 05:00:00,ON,03:00:00,US70Hour,11:00,11:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 08:00:00,DRV,02:00:00,US70Hour,09:00,09:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 10:00:00,OFF,03:00:00,US70Hour,09:00,06:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 13:00:00,DRV,07:00:00,US70Hour,02:00,00:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 20:00:00,ON,01:00:00,US70Hour,02:00,00:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 21:00:00,OFF,03:00:00,US70Hour,02:00,00:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/09/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/09/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");
        this.ProcessEvent("11/09/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,59:00,,TRUE,");
        this.ProcessEvent("11/09/2008 08:00:00,DRV,05:00:00,US70Hour,06:00,08:00,54:00,,TRUE,");
        this.ProcessEvent("11/09/2008 13:00:00,OFF,02:00:00,US70Hour,06:00,06:00,54:00,,TRUE,");
        this.ProcessEvent("11/09/2008 15:00:00,ON,02:00:00,US70Hour,06:00,04:00,52:00,,TRUE,");
        this.ProcessEvent("11/09/2008 17:00:00,DRV,05:30:00,US70Hour,00:30,00:00,46:30,,TRUE,");
        this.ProcessEvent("11/09/2008 22:30:00,ON,00:30:00,US70Hour,00:30,00:00,46:00,,TRUE,");
        this.ProcessEvent("11/09/2008 23:00:00,OFF,01:00:00,US70Hour,00:30,00:00,46:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/11/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/11/2008 00:00:00,OFF,03:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");
        this.ProcessEvent("11/11/2008 03:00:00,ON,01:00:00,US70Hour,11:00,13:00,59:00,,TRUE,");
        this.ProcessEvent("11/11/2008 04:00:00,DRV,04:00:00,US70Hour,07:00,09:00,55:00,,TRUE,");
        this.ProcessEvent("11/11/2008 08:00:00,ON,01:00:00,US70Hour,07:00,08:00,54:00,,TRUE,");
        this.ProcessEvent("11/11/2008 09:00:00,SLP,02:00:00,US70Hour,07:00,06:00,54:00,,TRUE,");
        this.ProcessEvent("11/11/2008 11:00:00,ON,01:00:00,US70Hour,07:00,05:00,53:00,,TRUE,");
        this.ProcessEvent("11/11/2008 12:00:00,DRV,05:00:00,US70Hour,02:00,00:00,48:00,,TRUE,");
        this.ProcessEvent("11/11/2008 17:00:00,ON,01:00:00,US70Hour,02:00,00:00,47:00,,TRUE,");
        this.ProcessEvent("11/11/2008 18:00:00,SLP,06:00:00,US70Hour,02:00,00:00,47:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/12/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/12/2008 00:00:00,SLP,02:00:00,US70Hour,06:00,07:00,47:00,,TRUE,");
        this.ProcessEvent("11/12/2008 02:00:00,ON,01:00:00,US70Hour,06:00,06:00,46:00,,TRUE,");
        this.ProcessEvent("11/12/2008 03:00:00,DRV,06:00:00,US70Hour,00:00,00:00,40:00,,TRUE,");
        this.ProcessEvent("11/12/2008 09:00:00,ON,01:00:00,US70Hour,00:00,00:00,39:00,,TRUE,");
        this.ProcessEvent("11/12/2008 10:00:00,SLP,02:00:00,US70Hour,05:00,04:00,39:00,,TRUE,");
        this.ProcessEvent("11/12/2008 12:00:00,ON,01:00:00,US70Hour,05:00,03:00,38:00,,TRUE,");
        this.ProcessEvent("11/12/2008 13:00:00,DRV,03:00:00,US70Hour,02:00,00:00,35:00,,TRUE,");
        this.ProcessEvent("11/12/2008 16:00:00,ON,01:00:00,US70Hour,02:00,00:00,34:00,,TRUE,");
        this.ProcessEvent("11/12/2008 17:00:00,SLP,07:00:00,US70Hour,02:00,00:00,34:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/13/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/13/2008 00:00:00,SLP,02:00:00,US70Hour,08:00,09:00,34:00,,TRUE,");
        this.ProcessEvent("11/13/2008 02:00:00,ON,01:00:00,US70Hour,08:00,08:00,33:00,,TRUE,");
        this.ProcessEvent("11/13/2008 03:00:00,DRV,02:00:00,US70Hour,06:00,06:00,31:00,,TRUE,");
        this.ProcessEvent("11/13/2008 05:00:00,OFF,01:00:00,US70Hour,06:00,05:00,31:00,,TRUE,");
        this.ProcessEvent("11/13/2008 06:00:00,DRV,04:00:00,US70Hour,02:00,01:00,27:00,,TRUE,");
        this.ProcessEvent("11/13/2008 10:00:00,ON,01:00:00,US70Hour,02:00,00:00,26:00,,TRUE,");
        this.ProcessEvent("11/13/2008 11:00:00,OFF,00:15:00,US70Hour,02:00,00:00,26:00,,TRUE,");
        this.ProcessEvent("11/13/2008 11:15:00,SLP,01:45:00,US70Hour,05:00,03:00,26:00,,TRUE,");
        this.ProcessEvent("11/13/2008 13:00:00,ON,00:30:00,US70Hour,05:00,02:30,25:30,,TRUE,");
        this.ProcessEvent("11/13/2008 13:30:00,DRV,03:30:00,US70Hour,01:30,00:00,22:00,,TRUE,");
        this.ProcessEvent("11/13/2008 17:00:00,ON,01:00:00,US70Hour,01:30,00:00,21:00,,TRUE,");
        this.ProcessEvent("11/13/2008 18:00:00,SLP,06:00:00,US70Hour,01:30,00:00,21:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/14/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/14/2008 00:00:00,SLP,01:00:00,US70Hour,01:30,00:00,21:00,,TRUE,");
        this.ProcessEvent("11/14/2008 01:00:00,OFF,02:00:00,US70Hour,01:30,00:00,21:00,,TRUE,");
        this.ProcessEvent("11/14/2008 03:00:00,ON,01:00:00,US70Hour,01:30,00:00,20:00,,TRUE,");
        this.ProcessEvent("11/14/2008 04:00:00,DRV,05:00:00,US70Hour,00:00,00:00,15:00,,TRUE,");
        this.ProcessEvent("11/14/2008 09:00:00,ON,01:00:00,US70Hour,00:00,00:00,14:00,,TRUE,");
        this.ProcessEvent("11/14/2008 10:00:00,SLP,02:00:00,US70Hour,00:00,00:00,14:00,,TRUE,");
        this.ProcessEvent("11/14/2008 12:00:00,ON,01:00:00,US70Hour,00:00,00:00,13:00,,TRUE,");
        this.ProcessEvent("11/14/2008 13:00:00,DRV,03:00:00,US70Hour,00:00,00:00,10:00,,TRUE,");
        this.ProcessEvent("11/14/2008 16:00:00,ON,01:00:00,US70Hour,00:00,00:00,09:00,,TRUE,");
        this.ProcessEvent("11/14/2008 17:00:00,SLP,07:00:00,US70Hour,00:00,00:00,09:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/15/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/15/2008 00:00:00,SLP,01:00:00,US70Hour,08:00,09:00,09:00,,TRUE,");
        this.ProcessEvent("11/15/2008 01:00:00,ON,00:30:00,US70Hour,08:00,08:30,08:30,,TRUE,");
        this.ProcessEvent("11/15/2008 01:30:00,DRV,04:00:00,US70Hour,04:00,04:30,04:30,,TRUE,");
        this.ProcessEvent("11/15/2008 05:30:00,ON,00:30:00,US70Hour,04:00,04:00,04:00,,TRUE,");
        this.ProcessEvent("11/15/2008 06:00:00,OFF,00:30:00,US70Hour,04:00,03:30,04:00,,TRUE,");
        this.ProcessEvent("11/15/2008 06:30:00,SLP,02:30:00,US70Hour,07:00,06:00,04:00,,TRUE,");
        this.ProcessEvent("11/15/2008 09:00:00,DRV,02:00:00,US70Hour,05:00,04:00,02:00,,TRUE,");
        this.ProcessEvent("11/15/2008 11:00:00,OFF,02:00:00,US70Hour,05:00,02:00,02:00,,TRUE,");
        this.ProcessEvent("11/15/2008 13:00:00,ON,00:30:00,US70Hour,05:00,01:30,01:30,,TRUE,");
        this.ProcessEvent("11/15/2008 13:30:00,DRV,01:30:00,US70Hour,03:30,00:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 15:00:00,SLP,08:00:00,US70Hour,09:30,12:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 23:00:00,ON,01:00:00,US70Hour,09:30,11:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/18/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/18/2008 00:00:00,OFF,03:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");
        this.ProcessEvent("11/18/2008 03:00:00,ON,01:00:00,US70Hour,11:00,13:00,59:00,,TRUE,");
        this.ProcessEvent("11/18/2008 04:00:00,DRV,03:00:00,US70Hour,08:00,10:00,56:00,,TRUE,");
        this.ProcessEvent("11/18/2008 07:00:00,SLP,08:00:00,US70Hour,08:00,10:00,56:00,,TRUE,");
        this.ProcessEvent("11/18/2008 15:00:00,ON,01:00:00,US70Hour,08:00,09:00,55:00,,TRUE,");
        this.ProcessEvent("11/18/2008 16:00:00,DRV,05:00:00,US70Hour,03:00,04:00,50:00,,TRUE,");
        this.ProcessEvent("11/18/2008 21:00:00,SLP,02:00:00,US70Hour,06:00,06:00,50:00,,TRUE,");
        this.ProcessEvent("11/18/2008 23:00:00,OFF,00:30:00,US70Hour,06:00,05:30,50:00,,TRUE,");
        this.ProcessEvent("11/18/2008 23:30:00,ON,00:30:00,US70Hour,06:00,05:00,49:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/19/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/19/2008 00:00:00,ON,00:30:00,US70Hour,06:00,04:30,49:00,,TRUE,");
        this.ProcessEvent("11/19/2008 00:30:00,DRV,04:30:00,US70Hour,01:30,00:00,44:30,,TRUE,");
        this.ProcessEvent("11/19/2008 05:00:00,ON,01:00:00,US70Hour,01:30,00:00,43:30,,TRUE,");
        this.ProcessEvent("11/19/2008 06:00:00,SLP,08:00:00,US70Hour,06:30,07:30,43:30,,TRUE,");
        this.ProcessEvent("11/19/2008 14:00:00,ON,01:30:00,US70Hour,06:30,06:00,42:00,,TRUE,");
        this.ProcessEvent("11/19/2008 15:30:00,DRV,06:00:00,US70Hour,00:30,00:00,36:00,,TRUE,");
        this.ProcessEvent("11/19/2008 21:30:00,ON,00:30:00,US70Hour,00:30,00:00,35:30,,TRUE,");
        this.ProcessEvent("11/19/2008 22:00:00,OFF,02:00:00,US70Hour,05:00,04:00,35:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/21/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/21/2008 00:00:00,OFF,06:00:00,US70Hour,11:00,14:00,60:00,,TRUE,");
        this.ProcessEvent("11/21/2008 06:00:00,ON,01:00:00,US70Hour,11:00,13:00,59:00,,TRUE,");
        this.ProcessEvent("11/21/2008 07:00:00,DRV,08:00:00,US70Hour,03:00,05:00,51:00,,TRUE,");
        this.ProcessEvent("11/21/2008 15:00:00,ON,01:00:00,US70Hour,03:00,04:00,50:00,,TRUE,");
        this.ProcessEvent("11/21/2008 16:00:00,OFF,08:00:00,US70Hour,03:00,00:00,50:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/22/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/22/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,50:00,,TRUE,");
        this.ProcessEvent("11/22/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,49:00,,TRUE,");
        this.ProcessEvent("11/22/2008 09:00:00,DRV,07:00:00,US70Hour,04:00,06:00,42:00,,TRUE,");
        this.ProcessEvent("11/22/2008 16:00:00,ON,01:00:00,US70Hour,04:00,05:00,41:00,,TRUE,");
        this.ProcessEvent("11/22/2008 17:00:00,OFF,07:00:00,US70Hour,04:00,00:00,41:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/23/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/23/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,41:00,,TRUE,");
        this.ProcessEvent("11/23/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,40:00,,TRUE,");
        this.ProcessEvent("11/23/2008 09:00:00,DRV,07:00:00,US70Hour,04:00,06:00,33:00,,TRUE,");
        this.ProcessEvent("11/23/2008 16:00:00,ON,01:00:00,US70Hour,04:00,05:00,32:00,,TRUE,");
        this.ProcessEvent("11/23/2008 17:00:00,OFF,07:00:00,US70Hour,04:00,00:00,32:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/24/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/24/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,32:00,,TRUE,");
        this.ProcessEvent("11/24/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,31:00,,TRUE,");
        this.ProcessEvent("11/24/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:00,23:00,,TRUE,");
        this.ProcessEvent("11/24/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:00,22:00,,TRUE,");
        this.ProcessEvent("11/24/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,22:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/25/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/25/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,22:00,,TRUE,");
        this.ProcessEvent("11/25/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,21:00,,TRUE,");
        this.ProcessEvent("11/25/2008 09:00:00,DRV,06:30:00,US70Hour,04:30,06:30,14:30,,TRUE,");
        this.ProcessEvent("11/25/2008 15:30:00,ON,01:30:00,US70Hour,04:30,05:00,13:00,,TRUE,");
        this.ProcessEvent("11/25/2008 17:00:00,OFF,07:00:00,US70Hour,04:30,00:00,13:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/26/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/26/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,13:00,,TRUE,");
        this.ProcessEvent("11/26/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,12:00,,TRUE,");
        this.ProcessEvent("11/26/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:00,04:00,,TRUE,");
        this.ProcessEvent("11/26/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:00,03:00,,TRUE,");
        this.ProcessEvent("11/26/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,03:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/27/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/27/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,03:00,,TRUE,");
        this.ProcessEvent("11/27/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,02:00,,TRUE,");
        this.ProcessEvent("11/27/2008 09:00:00,DRV,08:00:00,US70Hour,03:00,05:00,00:00,,TRUE,");
        this.ProcessEvent("11/27/2008 17:00:00,ON,01:00:00,US70Hour,03:00,04:00,00:00,,TRUE,");
        this.ProcessEvent("11/27/2008 18:00:00,OFF,06:00:00,US70Hour,03:00,00:00,00:00,,TRUE,");
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
            answer = String.format(Locale.US, " %d.%02d:%02d:%02d", days, hours, minutes, seconds);
        else
            answer = String.format(Locale.US, " %02d:%02d:%02d", hours, minutes, seconds);
        return answer;
    }

}
