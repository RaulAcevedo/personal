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
public class PropertyCarrying70HourTest extends KmbRoboTestBase {

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
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllActive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("11/01/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/01/2008 00:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/02/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/02/2008 00:00:00,OFF,10:00:00,US70Hour,11:00,14:00,70:00,,TRUE,");
        this.ProcessEvent("11/02/2008 10:00:00,ON,00:30:00,US70Hour,11:00,13:30,69:30,,TRUE,");
        this.ProcessEvent("11/02/2008 10:30:00,DRV,06:00:00,US70Hour,05:00,07:30,63:30,,TRUE,");
        this.ProcessEvent("11/02/2008 16:30:00,ON,01:30:00,US70Hour,05:00,06:00,62:00,,TRUE,");
        this.ProcessEvent("11/02/2008 18:00:00,DRV,05:00:00,US70Hour,00:00,01:00,57:00,,TRUE,");
        this.ProcessEvent("11/02/2008 23:00:00,ON,00:30:00,US70Hour,00:00,00:30,56:30,,TRUE,");
        this.ProcessEvent("11/02/2008 23:30:00,OFF,00:30:00,US70Hour,00:00,00:00,56:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/03/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/03/2008 00:00:00,OFF,09:30:00,US70Hour,11:00,14:00,56:30,,TRUE,");
        this.ProcessEvent("11/03/2008 09:30:00,ON,02:00:00,US70Hour,11:00,12:00,54:30,,TRUE,");
        this.ProcessEvent("11/03/2008 11:30:00,DRV,06:30:00,US70Hour,04:30,05:30,48:00,,TRUE,");
        this.ProcessEvent("11/03/2008 18:00:00,ON,01:00:00,US70Hour,04:30,04:30,47:00,,TRUE,");
        this.ProcessEvent("11/03/2008 19:00:00,OFF,05:00:00,US70Hour,04:30,00:00,47:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/04/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/04/2008 00:00:00,OFF,05:00:00,US70Hour,11:00,14:00,47:00,,TRUE,");
        this.ProcessEvent("11/04/2008 05:00:00,ON,01:30:00,US70Hour,11:00,12:30,45:30,,TRUE,");
        this.ProcessEvent("11/04/2008 06:30:00,DRV,02:30:00,US70Hour,08:30,10:00,43:00,,TRUE,");
        this.ProcessEvent("11/04/2008 09:00:00,ON,01:30:00,US70Hour,08:30,08:30,41:30,,TRUE,");
        this.ProcessEvent("11/04/2008 10:30:00,DRV,01:30:00,US70Hour,07:00,07:00,40:00,,TRUE,");
        this.ProcessEvent("11/04/2008 12:00:00,OFF,03:00:00,US70Hour,07:00,04:00,40:00,,TRUE,");
        this.ProcessEvent("11/04/2008 15:00:00,ON,00:30:00,US70Hour,07:00,03:30,39:30,,TRUE,");
        this.ProcessEvent("11/04/2008 15:30:00,DRV,04:00:00,US70Hour,03:00,00:00,35:30,,TRUE,");
        this.ProcessEvent("11/04/2008 19:30:00,ON,00:30:00,US70Hour,03:00,00:00,35:00,,TRUE,");
        this.ProcessEvent("11/04/2008 20:00:00,OFF,04:00:00,US70Hour,03:00,00:00,35:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/05/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/05/2008 00:00:00,OFF,04:00:00,US70Hour,03:00,00:00,35:00,,TRUE,");
        this.ProcessEvent("11/05/2008 04:00:00,SLP,03:00:00,US70Hour,11:00,14:00,35:00,,TRUE,");
        this.ProcessEvent("11/05/2008 07:00:00,ON,00:30:00,US70Hour,11:00,13:30,34:30,,TRUE,");
        this.ProcessEvent("11/05/2008 07:30:00,DRV,03:30:00,US70Hour,07:30,10:00,31:00,,TRUE,");
        this.ProcessEvent("11/05/2008 11:00:00,SLP,02:00:00,US70Hour,07:30,08:00,31:00,,TRUE,");
        this.ProcessEvent("11/05/2008 13:00:00,DRV,08:00:00,US70Hour,00:00,00:00,23:00,,TRUE,");
        this.ProcessEvent("11/05/2008 21:00:00,ON,00:30:00,US70Hour,00:00,00:00,22:30,,TRUE,");
        this.ProcessEvent("11/05/2008 21:30:00,OFF,02:30:00,US70Hour,00:00,00:00,22:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/06/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/06/2008 00:00:00,OFF,07:30:00,US70Hour,11:00,14:00,22:30,,TRUE,");
        this.ProcessEvent("11/06/2008 07:30:00,ON,00:30:00,US70Hour,11:00,13:30,22:00,,TRUE,");
        this.ProcessEvent("11/06/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:30,14:00,,TRUE,");
        this.ProcessEvent("11/06/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:30,13:00,,TRUE,");
        this.ProcessEvent("11/06/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,13:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/07/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/07/2008 00:00:00,OFF,05:00:00,US70Hour,11:00,14:00,13:00,,TRUE,");
        this.ProcessEvent("11/07/2008 05:00:00,ON,03:00:00,US70Hour,11:00,11:00,10:00,,TRUE,");
        this.ProcessEvent("11/07/2008 08:00:00,DRV,02:00:00,US70Hour,09:00,09:00,08:00,,TRUE,");
        this.ProcessEvent("11/07/2008 10:00:00,OFF,03:00:00,US70Hour,09:00,06:00,08:00,,TRUE,");
        this.ProcessEvent("11/07/2008 13:00:00,DRV,07:00:00,US70Hour,02:00,00:00,01:00,,TRUE,");
        this.ProcessEvent("11/07/2008 20:00:00,ON,01:00:00,US70Hour,02:00,00:00,00:00,,TRUE,");
        this.ProcessEvent("11/07/2008 21:00:00,OFF,03:00:00,US70Hour,02:00,00:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/09/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/09/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,70:00,,TRUE,");
        this.ProcessEvent("11/09/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,69:00,,TRUE,");
        this.ProcessEvent("11/09/2008 08:00:00,DRV,05:00:00,US70Hour,06:00,08:00,64:00,,TRUE,");
        this.ProcessEvent("11/09/2008 13:00:00,OFF,02:00:00,US70Hour,06:00,06:00,64:00,,TRUE,");
        this.ProcessEvent("11/09/2008 15:00:00,ON,02:00:00,US70Hour,06:00,04:00,62:00,,TRUE,");
        this.ProcessEvent("11/09/2008 17:00:00,DRV,05:30:00,US70Hour,00:30,00:00,56:30,,TRUE,");
        this.ProcessEvent("11/09/2008 22:30:00,ON,00:30:00,US70Hour,00:30,00:00,56:00,,TRUE,");
        this.ProcessEvent("11/09/2008 23:00:00,OFF,01:00:00,US70Hour,00:30,00:00,56:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/11/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/11/2008 00:00:00,OFF,03:00:00,US70Hour,11:00,14:00,56:00,,TRUE,");
        this.ProcessEvent("11/11/2008 03:00:00,ON,01:00:00,US70Hour,11:00,13:00,55:00,,TRUE,");
        this.ProcessEvent("11/11/2008 04:00:00,DRV,04:00:00,US70Hour,07:00,09:00,51:00,,TRUE,");
        this.ProcessEvent("11/11/2008 08:00:00,ON,01:00:00,US70Hour,07:00,08:00,50:00,,TRUE,");
        this.ProcessEvent("11/11/2008 09:00:00,SLP,02:00:00,US70Hour,07:00,06:00,50:00,,TRUE,");
        this.ProcessEvent("11/11/2008 11:00:00,ON,01:00:00,US70Hour,07:00,05:00,49:00,,TRUE,");
        this.ProcessEvent("11/11/2008 12:00:00,DRV,05:00:00,US70Hour,02:00,00:00,44:00,,TRUE,");
        this.ProcessEvent("11/11/2008 17:00:00,ON,01:00:00,US70Hour,02:00,00:00,43:00,,TRUE,");
        this.ProcessEvent("11/11/2008 18:00:00,SLP,06:00:00,US70Hour,02:00,00:00,43:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/12/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/12/2008 00:00:00,SLP,02:00:00,US70Hour,06:00,07:00,43:00,,TRUE,");
        this.ProcessEvent("11/12/2008 02:00:00,ON,01:00:00,US70Hour,06:00,06:00,42:00,,TRUE,");
        this.ProcessEvent("11/12/2008 03:00:00,DRV,06:00:00,US70Hour,00:00,00:00,36:00,,TRUE,");
        this.ProcessEvent("11/12/2008 09:00:00,ON,01:00:00,US70Hour,00:00,00:00,35:00,,TRUE,");
        this.ProcessEvent("11/12/2008 10:00:00,SLP,02:00:00,US70Hour,05:00,04:00,35:00,,TRUE,");
        this.ProcessEvent("11/12/2008 12:00:00,ON,01:00:00,US70Hour,05:00,03:00,34:00,,TRUE,");
        this.ProcessEvent("11/12/2008 13:00:00,DRV,03:00:00,US70Hour,02:00,00:00,31:00,,TRUE,");
        this.ProcessEvent("11/12/2008 16:00:00,ON,01:00:00,US70Hour,02:00,00:00,30:00,,TRUE,");
        this.ProcessEvent("11/12/2008 17:00:00,SLP,07:00:00,US70Hour,02:00,00:00,30:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/13/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/13/2008 00:00:00,SLP,02:00:00,US70Hour,08:00,09:00,30:00,,TRUE,");
        this.ProcessEvent("11/13/2008 02:00:00,ON,01:00:00,US70Hour,08:00,08:00,29:00,,TRUE,");
        this.ProcessEvent("11/13/2008 03:00:00,DRV,02:00:00,US70Hour,06:00,06:00,27:00,,TRUE,");
        this.ProcessEvent("11/13/2008 05:00:00,OFF,01:00:00,US70Hour,06:00,05:00,27:00,,TRUE,");
        this.ProcessEvent("11/13/2008 06:00:00,DRV,04:00:00,US70Hour,02:00,01:00,23:00,,TRUE,");
        this.ProcessEvent("11/13/2008 10:00:00,ON,01:00:00,US70Hour,02:00,00:00,22:00,,TRUE,");
        this.ProcessEvent("11/13/2008 11:00:00,OFF,00:15:00,US70Hour,02:00,00:00,22:00,,TRUE,");
        this.ProcessEvent("11/13/2008 11:15:00,SLP,01:45:00,US70Hour,05:00,03:00,22:00,,TRUE,");
        this.ProcessEvent("11/13/2008 13:00:00,ON,00:30:00,US70Hour,05:00,02:30,21:30,,TRUE,");
        this.ProcessEvent("11/13/2008 13:30:00,DRV,03:30:00,US70Hour,01:30,00:00,18:00,,TRUE,");
        this.ProcessEvent("11/13/2008 17:00:00,ON,01:00:00,US70Hour,01:30,00:00,17:00,,TRUE,");
        this.ProcessEvent("11/13/2008 18:00:00,SLP,06:00:00,US70Hour,01:30,00:00,17:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/14/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/14/2008 00:00:00,SLP,01:00:00,US70Hour,01:30,00:00,17:00,,TRUE,");
        this.ProcessEvent("11/14/2008 01:00:00,OFF,02:00:00,US70Hour,01:30,00:00,17:00,,TRUE,");
        this.ProcessEvent("11/14/2008 03:00:00,ON,01:00:00,US70Hour,01:30,00:00,16:00,,TRUE,");
        this.ProcessEvent("11/14/2008 04:00:00,DRV,05:00:00,US70Hour,00:00,00:00,11:00,,TRUE,");
        this.ProcessEvent("11/14/2008 09:00:00,ON,01:00:00,US70Hour,00:00,00:00,10:00,,TRUE,");
        this.ProcessEvent("11/14/2008 10:00:00,SLP,02:00:00,US70Hour,00:00,00:00,10:00,,TRUE,");
        this.ProcessEvent("11/14/2008 12:00:00,ON,01:00:00,US70Hour,00:00,00:00,09:00,,TRUE,");
        this.ProcessEvent("11/14/2008 13:00:00,DRV,03:00:00,US70Hour,00:00,00:00,06:00,,TRUE,");
        this.ProcessEvent("11/14/2008 16:00:00,ON,01:00:00,US70Hour,00:00,00:00,05:00,,TRUE,");
        this.ProcessEvent("11/14/2008 17:00:00,SLP,07:00:00,US70Hour,00:00,00:00,05:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/15/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/15/2008 00:00:00,SLP,01:00:00,US70Hour,08:00,09:00,05:00,,TRUE,");
        this.ProcessEvent("11/15/2008 01:00:00,ON,00:30:00,US70Hour,08:00,08:30,04:30,,TRUE,");
        this.ProcessEvent("11/15/2008 01:30:00,DRV,04:00:00,US70Hour,04:00,04:30,00:30,,TRUE,");
        this.ProcessEvent("11/15/2008 05:30:00,ON,00:30:00,US70Hour,04:00,04:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 06:00:00,OFF,00:30:00,US70Hour,04:00,03:30,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 06:30:00,SLP,02:30:00,US70Hour,07:00,06:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 09:00:00,DRV,02:00:00,US70Hour,05:00,04:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 11:00:00,OFF,02:00:00,US70Hour,05:00,02:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 13:00:00,ON,00:30:00,US70Hour,05:00,01:30,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 13:30:00,DRV,01:30:00,US70Hour,03:30,00:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 15:00:00,SLP,08:00:00,US70Hour,09:30,12:00,00:00,,TRUE,");
        this.ProcessEvent("11/15/2008 23:00:00,ON,01:00:00,US70Hour,09:30,11:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/18/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/18/2008 00:00:00,OFF,03:00:00,US70Hour,11:00,14:00,70:00,,TRUE,");
        this.ProcessEvent("11/18/2008 03:00:00,ON,01:00:00,US70Hour,11:00,13:00,69:00,,TRUE,");
        this.ProcessEvent("11/18/2008 04:00:00,DRV,03:00:00,US70Hour,08:00,10:00,66:00,,TRUE,");
        this.ProcessEvent("11/18/2008 07:00:00,SLP,08:00:00,US70Hour,08:00,10:00,66:00,,TRUE,");
        this.ProcessEvent("11/18/2008 15:00:00,ON,01:00:00,US70Hour,08:00,09:00,65:00,,TRUE,");
        this.ProcessEvent("11/18/2008 16:00:00,DRV,05:00:00,US70Hour,03:00,04:00,60:00,,TRUE,");
        this.ProcessEvent("11/18/2008 21:00:00,SLP,02:00:00,US70Hour,06:00,06:00,60:00,,TRUE,");
        this.ProcessEvent("11/18/2008 23:00:00,OFF,00:30:00,US70Hour,06:00,05:30,60:00,,TRUE,");
        this.ProcessEvent("11/18/2008 23:30:00,ON,00:30:00,US70Hour,06:00,05:00,59:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/19/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/19/2008 00:00:00,ON,00:30:00,US70Hour,06:00,04:30,59:00,,TRUE,");
        this.ProcessEvent("11/19/2008 00:30:00,DRV,04:30:00,US70Hour,01:30,00:00,54:30,,TRUE,");
        this.ProcessEvent("11/19/2008 05:00:00,ON,01:00:00,US70Hour,01:30,00:00,53:30,,TRUE,");
        this.ProcessEvent("11/19/2008 06:00:00,SLP,08:00:00,US70Hour,06:30,07:30,53:30,,TRUE,");
        this.ProcessEvent("11/19/2008 14:00:00,ON,01:30:00,US70Hour,06:30,06:00,52:00,,TRUE,");
        this.ProcessEvent("11/19/2008 15:30:00,DRV,06:00:00,US70Hour,00:30,00:00,46:00,,TRUE,");
        this.ProcessEvent("11/19/2008 21:30:00,ON,00:30:00,US70Hour,00:30,00:00,45:30,,TRUE,");
        this.ProcessEvent("11/19/2008 22:00:00,OFF,02:00:00,US70Hour,05:00,04:00,45:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/21/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/21/2008 00:00:00,OFF,06:00:00,US70Hour,11:00,14:00,45:30,,TRUE,");
        this.ProcessEvent("11/21/2008 06:00:00,ON,01:00:00,US70Hour,11:00,13:00,44:30,,TRUE,");
        this.ProcessEvent("11/21/2008 07:00:00,DRV,08:00:00,US70Hour,03:00,05:00,36:30,,TRUE,");
        this.ProcessEvent("11/21/2008 15:00:00,ON,01:00:00,US70Hour,03:00,04:00,35:30,,TRUE,");
        this.ProcessEvent("11/21/2008 16:00:00,OFF,08:00:00,US70Hour,03:00,00:00,35:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/22/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/22/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,35:30,,TRUE,");
        this.ProcessEvent("11/22/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,34:30,,TRUE,");
        this.ProcessEvent("11/22/2008 09:00:00,DRV,07:00:00,US70Hour,04:00,06:00,27:30,,TRUE,");
        this.ProcessEvent("11/22/2008 16:00:00,ON,01:00:00,US70Hour,04:00,05:00,26:30,,TRUE,");
        this.ProcessEvent("11/22/2008 17:00:00,OFF,07:00:00,US70Hour,04:00,00:00,26:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/23/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/23/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,26:30,,TRUE,");
        this.ProcessEvent("11/23/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,25:30,,TRUE,");
        this.ProcessEvent("11/23/2008 09:00:00,DRV,07:00:00,US70Hour,04:00,06:00,18:30,,TRUE,");
        this.ProcessEvent("11/23/2008 16:00:00,ON,01:00:00,US70Hour,04:00,05:00,17:30,,TRUE,");
        this.ProcessEvent("11/23/2008 17:00:00,OFF,07:00:00,US70Hour,04:00,00:00,17:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/24/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/24/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,17:30,,TRUE,");
        this.ProcessEvent("11/24/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,16:30,,TRUE,");
        this.ProcessEvent("11/24/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:00,08:30,,TRUE,");
        this.ProcessEvent("11/24/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:00,07:30,,TRUE,");
        this.ProcessEvent("11/24/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,07:30,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/25/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/25/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,07:30,,TRUE,");
        this.ProcessEvent("11/25/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,06:30,,TRUE,");
        this.ProcessEvent("11/25/2008 09:00:00,DRV,06:30:00,US70Hour,04:30,06:30,00:00,,TRUE,");
        this.ProcessEvent("11/25/2008 15:30:00,ON,01:30:00,US70Hour,04:30,05:00,00:00,,TRUE,");
        this.ProcessEvent("11/25/2008 17:00:00,OFF,07:00:00,US70Hour,04:30,00:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/26/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/26/2008 00:00:00,OFF,07:00:00,US70Hour,11:00,14:00,09:00,,TRUE,");
        this.ProcessEvent("11/26/2008 07:00:00,ON,01:00:00,US70Hour,11:00,13:00,08:00,,TRUE,");
        this.ProcessEvent("11/26/2008 08:00:00,DRV,08:00:00,US70Hour,03:00,05:00,00:00,,TRUE,");
        this.ProcessEvent("11/26/2008 16:00:00,ON,01:00:00,US70Hour,03:00,04:00,00:00,,TRUE,");
        this.ProcessEvent("11/26/2008 17:00:00,OFF,07:00:00,US70Hour,03:00,00:00,00:00,,TRUE,");

        logProperties.setLogDate(new Date(Date.parse("11/27/2008")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("11/27/2008 00:00:00,OFF,08:00:00,US70Hour,11:00,14:00,13:00,,TRUE,");
        this.ProcessEvent("11/27/2008 08:00:00,ON,01:00:00,US70Hour,11:00,13:00,12:00,,TRUE,");
        this.ProcessEvent("11/27/2008 09:00:00,DRV,08:00:00,US70Hour,03:00,05:00,04:00,,TRUE,");
        this.ProcessEvent("11/27/2008 17:00:00,ON,01:00:00,US70Hour,03:00,04:00,03:00,,TRUE,");
        this.ProcessEvent("11/27/2008 18:00:00,OFF,06:00:00,US70Hour,03:00,00:00,03:00,,TRUE,");


    }

    @Test
    public void test_Standard_OutsideSuspension_With34HourReset() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, true);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are not active in the suspension period
        // this will act as if all logs are outside of the suspension period and processing the 34 hour reset provisions
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        //	.thenReturn(true);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllInactive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("07/01/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/1/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/02/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/2/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/2/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/2/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/2/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/03/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/3/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/3/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/3/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/3/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/04/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/4/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/4/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/4/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("7/4/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/05/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/5/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("7/5/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("7/5/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("7/5/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/06/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/6/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,14:00:00,,");
        this.ProcessEvent("7/6/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,11:00:00,,");
        this.ProcessEvent("7/6/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/6/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/07/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/7/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/08/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/8/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/8/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/8/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/8/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/09/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/9/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/9/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/9/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/9/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/10/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/10/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/10/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/10/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("7/10/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/11/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/11/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("7/11/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("7/11/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("7/11/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/12/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/12/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,14:00:00,,");
        this.ProcessEvent("7/12/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,11:00:00,,");
        this.ProcessEvent("7/12/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/12/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/13/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/13/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,00:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/14/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/14/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,00:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/15/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/15/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/15/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/15/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/15/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/16/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/16/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/16/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/16/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/16/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/17/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/17/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/17/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/17/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("7/17/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/18/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/18/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("7/18/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("7/18/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("7/18/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/19/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/19/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,14:00:00,,");
        this.ProcessEvent("7/19/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,11:00:00,,");
        this.ProcessEvent("7/19/2013 8:00:00,DRV,9:00:00,US70Hour,2:00,2:00,2:00:00,,");
        this.ProcessEvent("7/19/2013 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,2:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/20/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/20/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,2:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/21/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/21/2013 0:00:00,OFF,7:00:00,US70Hour,11:00,14:00,2:00:00,,");
        this.ProcessEvent("7/21/2013 7:00:00,ON,1:00:00,US70Hour,11:00,13:00,1:00:00,,");
        this.ProcessEvent("7/21/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,2:00,0:00:00,,");
        this.ProcessEvent("7/21/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/22/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/22/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/23/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/23/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/24/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/24/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/24/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/24/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/24/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/25/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/25/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/25/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/25/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/25/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/26/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/26/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/27/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/27/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/28/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/28/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/28/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/28/2013 8:00:00,DRV,09:00:00,US70Hour,2:00,2:00,30:00:00,,");
        this.ProcessEvent("7/28/2013 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,30:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/29/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/29/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,30:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/30/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/30/2013 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,30:00:00,,");
        this.ProcessEvent("7/30/2013 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,27:00:00,,");
        this.ProcessEvent("7/30/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,16:00:00,,");
        this.ProcessEvent("7/30/2013 17:00:00,OFF,7:00:00,US70Hour,0:00,0:00,16:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/31/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/31/2013 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,16:00:00,,");
        this.ProcessEvent("7/31/2013 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,13:00:00,,");
        this.ProcessEvent("7/31/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,2:00:00,,");
        this.ProcessEvent("7/31/2013 17:00:00,OFF,7:00:00,US70Hour,0:00,0:00,2:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/01/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/1/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,16:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/02/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/2/2013 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,30:00:00,,");
        this.ProcessEvent("8/2/2013 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,27:00:00,,");
        this.ProcessEvent("8/2/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,16:00:00,,");
        this.ProcessEvent("8/2/2013 17:00:00,ON,7:00:00,US70Hour,0:00,0:00,9:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/03/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/3/2013 0:00:00,ON,3:00:00,US70Hour,0:00,0:00,6:00:00,,");
        this.ProcessEvent("8/3/2013 3:00:00,OFF,21:00:00,US70Hour,11:00,14:00,6:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/04/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/4/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,6:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/05/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/5/2013 0:00:00,OFF,16:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("8/5/2013 16:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("8/5/2013 17:00:00,DRV,2:00:00,US70Hour,9:00,11:00,67:00:00,,");
        this.ProcessEvent("8/5/2013 19:00:00,OFF,5:00:00,US70Hour,9:00,6:00,67:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/06/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/6/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,67:00:00,,");
        this.ProcessEvent("8/6/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,64:00:00,,");
        this.ProcessEvent("8/6/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,53:00:00,,");
        this.ProcessEvent("8/6/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,53:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/07/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/7/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,53:00:00,,");
        this.ProcessEvent("8/7/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,50:00:00,,");
        this.ProcessEvent("8/7/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,39:00:00,,");
        this.ProcessEvent("8/7/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,39:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/08/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/8/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,39:00:00,,");
        this.ProcessEvent("8/8/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,36:00:00,,");
        this.ProcessEvent("8/8/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,25:00:00,,");
        this.ProcessEvent("8/8/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/09/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/9/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,25:00:00,,");
        this.ProcessEvent("8/9/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,22:00:00,,");
        this.ProcessEvent("8/9/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,11:00:00,,");
        this.ProcessEvent("8/9/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,11:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/10/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/10/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,11:00:00,,");
        this.ProcessEvent("8/10/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,8:00:00,,");
        this.ProcessEvent("8/10/2013 8:00:00,DRV,8:00:00,US70Hour,3:00,3:00,0:00:00,,");
        this.ProcessEvent("8/10/2013 16:00:00,OFF,8:00,US70Hour,3:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/11/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/11/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/12/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/12/2013 0:00:00,OFF,16:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("8/12/2013 16:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("8/12/2013 17:00:00,DRV,2:00:00,US70Hour,9:00,11:00,67:00:00,,");
        this.ProcessEvent("8/12/2013 19:00:00,OFF,5:00:00,US70Hour,9:00,6:00,67:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/13/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/13/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,67:00:00,,");
        this.ProcessEvent("8/13/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,64:00:00,,");
        this.ProcessEvent("8/13/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,53:00:00,,");
        this.ProcessEvent("8/13/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,53:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/14/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/14/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,53:00:00,,");
        this.ProcessEvent("8/14/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,50:00:00,,");
        this.ProcessEvent("8/14/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,39:00:00,,");
        this.ProcessEvent("8/14/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,39:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/15/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/15/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,39:00:00,,");
        this.ProcessEvent("8/15/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,36:00:00,,");
        this.ProcessEvent("8/15/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,25:00:00,,");
        this.ProcessEvent("8/15/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/16/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/16/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,25:00:00,,");
        this.ProcessEvent("8/16/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,22:00:00,,");
        this.ProcessEvent("8/16/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,11:00:00,,");
        this.ProcessEvent("8/16/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,11:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/17/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/17/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,11:00:00,,");
        this.ProcessEvent("8/17/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,8:00:00,,");
        this.ProcessEvent("8/17/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("8/17/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/18/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/18/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/19/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/19/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("8/19/2013 8:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("8/19/2013 9:00:00,DRV,2:00:00,US70Hour,9:00,11:00,67:00:00,,");
        this.ProcessEvent("8/19/2013 11:00:00,OFF,2:00:00,US70Hour,9:00,9:00,67:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/20/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/20/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,67:00:00,,");
        this.ProcessEvent("8/20/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,64:00:00,,");
        this.ProcessEvent("8/20/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,53:00:00,,");
        this.ProcessEvent("8/20/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,53:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/21/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/21/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,53:00:00,,");
        this.ProcessEvent("8/21/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,50:00:00,,");
        this.ProcessEvent("8/21/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,39:00:00,,");
        this.ProcessEvent("8/21/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,39:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/22/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/22/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,39:00:00,,");
        this.ProcessEvent("8/22/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,36:00:00,,");
        this.ProcessEvent("8/22/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,25:00:00,,");
        this.ProcessEvent("8/22/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/23/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/23/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/24/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/24/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/25/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/25/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,25:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/26/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/26/2013 0:00:00,OFF,9:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("8/26/2013 9:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("8/26/2013 12:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("8/26/2013 23:00:00,OFF,1:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/27/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/27/2013 0:00:00,OFF,9:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("8/27/2013 9:00:00,ON,2:00:00,US70Hour,11:00,12:00,54:00:00,,");
        this.ProcessEvent("8/27/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,1:00,43:00:00,,");
        this.ProcessEvent("8/27/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,43:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/28/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/28/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,43:00:00,,");
        this.ProcessEvent("8/28/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,40:00:00,,");
        this.ProcessEvent("8/28/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,29:00:00,,");
        this.ProcessEvent("8/28/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,29:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/29/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/29/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,29:00:00,,");
        this.ProcessEvent("8/29/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,26:00:00,,");
        this.ProcessEvent("8/29/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,15:00:00,,");
        this.ProcessEvent("8/29/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,15:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/30/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/30/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,15:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("08/31/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("8/31/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,15:00:00,,");
        this.ProcessEvent("8/31/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,12:00:00,,");
        this.ProcessEvent("8/31/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,1:00:00,,");
        this.ProcessEvent("8/31/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,1:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("09/01/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("9/1/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,1:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("09/02/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("9/2/2013 0:00:00,OFF,8:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("9/2/2013 8:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("9/2/2013 11:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("9/2/2013 22:00:00,OFF,2:00:00,US70Hour,0:00,0:00,56:00:00,,");

    }

    @Test
    public void test_Standard_NewWeeklyDutyRule_SwitchReset() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, true);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // create mock such that all checkdates are not active in the suspension period
        // this will act as if all logs are outside of the suspension period and processing the 34 hour reset provisions
        //when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
        // 	.thenReturn(true);
        mockComplianceDatesController = new MockLogCheckerComplianceDatesControllerAllInactive();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("07/01/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/1/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/02/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/2/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/2/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/2/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/2/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/03/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/3/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/3/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/3/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/3/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/04/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/4/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/4/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/4/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("7/4/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/05/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/5/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("7/5/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("7/5/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("7/5/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/06/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/6/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,14:00:00,,");
        this.ProcessEvent("7/6/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,11:00:00,,");
        this.ProcessEvent("7/6/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/6/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/07/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/7/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/08/2013")));
        logProperties.setWeeklyResetStartTimestamp(new Date(Date.parse("7/6/2013 19:00:00")));
        logProperties.setIsWeeklyResetUsed(false);
        logProperties.setIsWeeklyResetUsedOverridden(true);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/8/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,0:00:00,,");
        this.ProcessEvent("7/8/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,0:00:00,,");
        this.ProcessEvent("7/8/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/8/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");
        logProperties.setWeeklyResetStartTimestamp(null);
        logProperties.setIsWeeklyResetUsed(false);
        logProperties.setIsWeeklyResetUsedOverridden(false);

        logProperties.setLogDate(new Date(Date.parse("07/09/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/9/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,0:00:00,,");
        this.ProcessEvent("7/9/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,0:00:00,,");
        this.ProcessEvent("7/9/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/9/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/10/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/10/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,0:00:00,,");
        this.ProcessEvent("7/10/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,0:00:00,,");
        this.ProcessEvent("7/10/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/10/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/11/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/11/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,0:00:00,,");
        this.ProcessEvent("7/11/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,0:00:00,,");
        this.ProcessEvent("7/11/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/11/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/12/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/12/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,0:00:00,,");
        this.ProcessEvent("7/12/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,0:00:00,,");
        this.ProcessEvent("7/12/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,0:00:00,,");
        this.ProcessEvent("7/12/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/13/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/13/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,0:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/14/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/14/2013 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/15/2013")));
        logProperties.setWeeklyResetStartTimestamp(new Date(Date.parse("7/13/2013 19:00:00")));
        logProperties.setIsWeeklyResetUsed(true);
        logProperties.setIsWeeklyResetUsedOverridden(false);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/15/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("7/15/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("7/15/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("7/15/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");
        logProperties.setWeeklyResetStartTimestamp(null);
        logProperties.setIsWeeklyResetUsed(false);
        logProperties.setIsWeeklyResetUsedOverridden(false);

        logProperties.setLogDate(new Date(Date.parse("07/16/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/16/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("7/16/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("7/16/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("7/16/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/17/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/17/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("7/17/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("7/17/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("7/17/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("07/18/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("7/18/2013 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("7/18/2013 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("7/18/2013 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("7/18/2013 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

    }

    @Test
    public void test_Transition_IntoResetPeriod() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, true);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // setup our mock to call a fake method to deal with the suspension period checking
        // the fake will use 12/16/2014 as the start
//		when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
//	 		.thenAnswer(new Answer<Boolean>() {
//	 			@Override
//	 			public Boolean answer(InvocationOnMock invocation){
//	 				Date dateToCheck = (Date)invocation.getArguments()[1];
//	 				boolean dateRangeDefinesActivePeriod = (Boolean) invocation.getArguments()[2];
//
//	 		    	Date complianceStartDate = new Date(Date.parse("12/16/2014"));
//	 		    	Date complianceEndDate = new Date(Date.parse("03/16/2015"));
//
//	 		    	boolean isActive = false;    	
//	 		    	if (complianceStartDate.compareTo(dateToCheck) < 0)
//	 		    	{    	
//	 		    		if (complianceEndDate == null)
//	 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
//	 		    		
//	 		    		else if (complianceEndDate.compareTo(dateToCheck) > 0)
//	 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
//	 		    		else
//	 		    			isActive = dateRangeDefinesActivePeriod ? false : true;
//	 		    	}
//	 		    	else
//	 		    	{
//	 		    		isActive = dateRangeDefinesActivePeriod ? false : true;
//	 		    	}
//	 		    	    	    	
//	 				return isActive;    	
//	 			}
//	 		} );
        mockComplianceDatesController = new MockLogCheckerComplianceDatesController();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/06/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/6/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/6/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("12/6/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("12/6/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/07/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/7/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("12/7/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("12/7/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("12/7/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/08/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/8/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,42:00:00,,");
        this.ProcessEvent("12/8/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,39:00:00,,");
        this.ProcessEvent("12/8/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,28:00:00,,");
        this.ProcessEvent("12/8/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,28:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/09/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/9/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,28:00:00,,");
        this.ProcessEvent("12/9/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,25:00:00,,");
        this.ProcessEvent("12/9/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,14:00:00,,");
        this.ProcessEvent("12/9/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,14:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/10/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/10/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,14:00:00,,");
        this.ProcessEvent("12/10/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,11:00:00,,");
        this.ProcessEvent("12/10/2014 8:00:00,DRV,9:00:00,US70Hour,2:00,2:00,2:00:00,,");
        this.ProcessEvent("12/10/2014 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,2:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/11/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/11/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,2:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/12/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/12/2014 0:00:00,OFF,7:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/12/2014 7:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("12/12/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,2:00,58:00:00,,");
        this.ProcessEvent("12/12/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/13/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/13/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/14/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/14/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,58:00:00,,");

        // NO RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/15/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/15/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,58:00:00,,");
        this.ProcessEvent("12/15/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,55:00:00,,");
        this.ProcessEvent("12/15/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,44:00:00,,");
        this.ProcessEvent("12/15/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,44:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/16/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/16/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,44:00:00,,");
        this.ProcessEvent("12/16/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,41:00:00,,");
        this.ProcessEvent("12/16/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,30:00:00,,");
        this.ProcessEvent("12/16/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,30:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/17/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/17/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,30:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/18/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/18/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/19/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/19/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/19/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("12/19/2014 8:00:00,DRV,09:00:00,US70Hour,2:00,2:00,58:00:00,,");
        this.ProcessEvent("12/19/2014 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/20/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/20/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/21/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/21/2014 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/21/2014 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("12/21/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("12/21/2014 17:00:00,OFF,7:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/22/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/22/2014 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("12/22/2014 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("12/22/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,42:00:00,,");
        this.ProcessEvent("12/22/2014 17:00:00,OFF,7:00:00,US70Hour,0:00,0:00,42:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/23/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/23/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,42:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/24/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/24/2014 0:00:00,OFF,3:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/24/2014 3:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("12/24/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("12/24/2014 17:00:00,ON,7:00:00,US70Hour,0:00,0:00,49:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/25/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/25/2014 0:00:00,ON,3:00:00,US70Hour,0:00,0:00,46:00:00,,");
        this.ProcessEvent("12/25/2014 3:00:00,OFF,21:00:00,US70Hour,11:00,14:00,46:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/26/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/26/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/27/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/27/2014 0:00:00,OFF,16:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/27/2014 16:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("12/27/2014 17:00:00,DRV,2:00:00,US70Hour,9:00,11:00,67:00:00,,");
        this.ProcessEvent("12/27/2014 19:00:00,OFF,5:00:00,US70Hour,9:00,6:00,67:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/28/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/28/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,67:00:00,,");
        this.ProcessEvent("12/28/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,64:00:00,,");
        this.ProcessEvent("12/28/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,53:00:00,,");
        this.ProcessEvent("12/28/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,53:00:00,,");

    }

    @Test
    public void test_Transition_IntoSuspensionPeriod_OnExactStartDate() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, true);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // setup our mock to call a fake method to deal with the suspension period checking
        // the fake will use 12/16/2014 as the start
//		when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
// 		.thenAnswer(new Answer<Boolean>() {
// 			@Override
// 			public Boolean answer(InvocationOnMock invocation){
// 				Date dateToCheck = (Date)invocation.getArguments()[1];
// 				boolean dateRangeDefinesActivePeriod = (Boolean) invocation.getArguments()[2];
//
// 		    	Date complianceStartDate = new Date(Date.parse("12/16/2014"));
// 		    	Date complianceEndDate = new Date(Date.parse("03/16/2015"));
//
// 		    	boolean isActive = false;    	
// 		    	if (complianceStartDate.compareTo(dateToCheck) < 0)
// 		    	{    	
// 		    		if (complianceEndDate == null)
// 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
// 		    		
// 		    		else if (complianceEndDate.compareTo(dateToCheck) > 0)
// 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
// 		    		else
// 		    			isActive = dateRangeDefinesActivePeriod ? false : true;
// 		    	}
// 		    	else
// 		    	{
// 		    		isActive = dateRangeDefinesActivePeriod ? false : true;
// 		    	}
// 		    	    	    	
// 				return isActive;    	
// 			}
// 		} );
        mockComplianceDatesController = new MockLogCheckerComplianceDatesController();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/10/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/10/2014 0:00:00,OFF,7:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/10/2014 7:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("12/10/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,2:00,58:00:00,,");
        this.ProcessEvent("12/10/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/11/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/11/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/12/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/12/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/13/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/13/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/13/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("12/13/2014 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("12/13/2014 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/14/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/14/2014 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("12/14/2014 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("12/14/2014 8:00:00,DRV,7:00:00,US70Hour,4:00,4:00,46:00:00,,");
        this.ProcessEvent("12/14/2014 15:00:00,OFF,09:00:00,US70Hour,4:00,0:00,46:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("12/15/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/15/2014 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,46:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("12/16/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("12/16/2014 0:00:00,OFF,1:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("12/16/2014 1:00:00,ON,7:00:00,US70Hour,11:00,7:00,63:00:00,,");
        this.ProcessEvent("12/16/2014 8:00:00,DRV,09:00:00,US70Hour,2:00,0:00,54:00:00,,");
        this.ProcessEvent("12/16/2014 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,54:00:00,,");

    }

    @Test
    public void test_Transition_OutOfSuspensionPeriod_OnExactEndDate() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        //_rulesetEngine = RulesetFactory.ForUS70Property(true, true, false, true);

        RulesetProperties properties = new RulesetProperties();
        properties.setIsShortHaulExceptionAllowed(true);
        properties.setIs34HourResetAllowed(true);
        properties.setIs8HourDrivingRuleEnabled(false);

        // setup our mock to call a fake method to deal with the suspension period checking
        // the fake will use 12/16/2014 as the start
//		when(mockComplianceDatesController.IsLogCheckerComplianceDateActive(anyInt(), any(java.util.Date.class), anyBoolean()))
// 		.thenAnswer(new Answer<Boolean>() {
// 			@Override
// 			public Boolean answer(InvocationOnMock invocation){
// 				Date dateToCheck = (Date)invocation.getArguments()[1];
// 				boolean dateRangeDefinesActivePeriod = (Boolean) invocation.getArguments()[2];
//
// 		    	Date complianceStartDate = new Date(Date.parse("12/16/2014"));
// 		    	Date complianceEndDate = new Date(Date.parse("03/16/2015"));
//
// 		    	boolean isActive = false;    	
// 		    	if (complianceStartDate.compareTo(dateToCheck) < 0)
// 		    	{    	
// 		    		if (complianceEndDate == null)
// 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
// 		    		
// 		    		else if (complianceEndDate.compareTo(dateToCheck) > 0)
// 		    			isActive = dateRangeDefinesActivePeriod ? true : false;
// 		    		else
// 		    			isActive = dateRangeDefinesActivePeriod ? false : true;
// 		    	}
// 		    	else
// 		    	{
// 		    		isActive = dateRangeDefinesActivePeriod ? false : true;
// 		    	}
// 		    	    	    	
// 				return isActive;    	
// 			}
// 		} );
        mockComplianceDatesController = new MockLogCheckerComplianceDatesController();

        _rulesetEngine = new PropertyCarrying70Hour(properties, mockComplianceDatesController);

        LogProperties logProperties = new LogProperties();

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("03/10/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/10/2015 0:00:00,OFF,7:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("03/10/2015 7:00:00,ON,1:00:00,US70Hour,11:00,13:00,69:00:00,,");
        this.ProcessEvent("03/10/2015 8:00:00,DRV,11:00:00,US70Hour,0:00,2:00,58:00:00,,");
        this.ProcessEvent("03/10/2015 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/11/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/11/2015 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,58:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/12/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/12/2015 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,70:00:00,,");

        // RESET HERE
        logProperties.setLogDate(new Date(Date.parse("03/13/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/13/2015 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,70:00:00,,");
        this.ProcessEvent("03/13/2015 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,67:00:00,,");
        this.ProcessEvent("03/13/2015 8:00:00,DRV,11:00:00,US70Hour,0:00,0:00,56:00:00,,");
        this.ProcessEvent("03/13/2015 19:00:00,OFF,5:00:00,US70Hour,0:00,0:00,56:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/14/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/14/2015 0:00:00,OFF,5:00:00,US70Hour,11:00,14:00,56:00:00,,");
        this.ProcessEvent("03/14/2015 5:00:00,ON,3:00:00,US70Hour,11:00,11:00,53:00:00,,");
        this.ProcessEvent("03/14/2015 8:00:00,DRV,7:00:00,US70Hour,4:00,4:00,46:00:00,,");
        this.ProcessEvent("03/14/2015 15:00:00,OFF,09:00:00,US70Hour,4:00,0:00,46:00:00,,");

        logProperties.setLogDate(new Date(Date.parse("03/15/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/15/2015 0:00:00,OFF,1.00:00:00,US70Hour,11:00,14:00,46:00:00,,");

        // NO RESET HERE
        logProperties.setLogDate(new Date(Date.parse("03/16/2014")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/16/2015 0:00:00,OFF,1:00:00,US70Hour,11:00,14:00,46:00:00,,");
        this.ProcessEvent("03/16/2015 1:00:00,ON,7:00:00,US70Hour,11:00,7:00,39:00:00,,");
        this.ProcessEvent("03/16/2015 8:00:00,DRV,09:00:00,US70Hour,2:00,0:00,30:00:00,,");
        this.ProcessEvent("03/16/2015 17:00:00,OFF,7:00:00,US70Hour,2:00,0:00,30:00:00,,");

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
