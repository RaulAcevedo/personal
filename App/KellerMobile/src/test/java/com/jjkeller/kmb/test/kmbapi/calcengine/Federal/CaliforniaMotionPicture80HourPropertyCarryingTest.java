package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.kmbapi.calcengine.CalcEngineTestHelpers;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by AJB6442 on 9/22/2017.
 */
@RunWith(KMBRoboElectricTestRunner.class)
public class CaliforniaMotionPicture80HourPropertyCarryingTest {
    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final Enums.RuleSetTypeEnum _ruleset = Enums.RuleSetTypeEnum.California_MP_80;

    @Before
    public void setUp() throws Exception { }

    @Test
    public void test_Limits() {
        // 80hr Weekly Duty limit
        // 15hr Daily Duty limit
        // 12hr Daily Drive limit
        LogProperties logProperties = new LogProperties();

        _rulesetEngine = RulesetFactory.ForCaliforniaMotionPicture80HourProperty(false);
        CalcEngineTestHelpers testHelpers = new CalcEngineTestHelpers(_rulesetEngine, _ruleset);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date logStartDate = cal.getTime();

        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        testHelpers.ProcessEvent("11/1/2008 00:00:00,OFF,1.00:00:00,CAMotionPicture,12:00,15:00,80:00:00,,TRUE");

        cal.set(Calendar.DAY_OF_MONTH, 2);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/2/2008 00:00:00,OFF,9:00:00,CAMotionPicture,12:00:00,15:00:00,80:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 9:00:00,ON,0:30:00,CAMotionPicture,12:00:00,14:30:00,79:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 9:30:00,DRV,6:00:00,CAMotionPicture,6:00:00,8:30:00,73:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 15:30:00,ON,1:30:00,CAMotionPicture,6:00:00,7:00:00,72:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 17:00:00,DRV,6:00:00,CAMotionPicture,0:00:00,1:00:00,66:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 23:00:00,ON,0:30:00,CAMotionPicture,0:00:00,0:30:00,65:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 23:30:00,OFF,0:30:00,CAMotionPicture,0:00:00,0:30:00,65:30:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 3);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/3/2008 00:00:00,OFF,9:30:00,CAMotionPicture,12:00:00,15:00:00,65:30:00,,,");
        testHelpers.ProcessEvent("11/3/2008 09:30:00,ON,2:00:00,CAMotionPicture,12:00:00,13:00:00,63:30:00,,,");
        testHelpers.ProcessEvent("11/3/2008 11:30:00,DRV,6:30:00,CAMotionPicture,5:30:00,6:30:00,57:00:00,,,");
        testHelpers.ProcessEvent("11/3/2008 18:00:00,ON,1:00:00,CAMotionPicture,5:30:00,5:30:00,56:00:00,,,");
        testHelpers.ProcessEvent("11/3/2008 19:00:00,OFF,5:00:00,CAMotionPicture,5:30:00,5:30:00,56:00:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 4);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/4/2008 00:00:00,OFF,5:00:00,CAMotionPicture,12:00,15:00,56:00:00,,,");
        testHelpers.ProcessEvent("11/4/2008 05:00:00,ON,1:30:00,CAMotionPicture,12:00,13:30,54:30:00,,,");
        testHelpers.ProcessEvent("11/4/2008 06:30:00,DRV,2:30:00,CAMotionPicture,9:30,11:00,52:00:00,,,");
        testHelpers.ProcessEvent("11/4/2008 09:00:00,ON,1:30:00,CAMotionPicture,9:30,9:30,50:30:00,,,");
        testHelpers.ProcessEvent("11/4/2008 10:30:00,DRV,1:30:00,CAMotionPicture,8:00,8:00,49:00:00,,,");
        testHelpers.ProcessEvent("11/4/2008 12:00:00,OFF,3:00:00,CAMotionPicture,8:00,8:00,49:00:00,,,");
        testHelpers.ProcessEvent("11/4/2008 15:00:00,ON,0:30:00,CAMotionPicture,8:00,7:30,48:30:00,,,");
        testHelpers.ProcessEvent("11/4/2008 15:30:00,DRV,4:00:00,CAMotionPicture,4:00,3:30,44:30:00,,,");
        testHelpers.ProcessEvent("11/4/2008 19:30:00,ON,0:30:00,CAMotionPicture,4:00,3:00,44:00:00,,,");
        testHelpers.ProcessEvent("11/4/2008 20:00:00,OFF,4:00:00,CAMotionPicture,4:00,3:00,44:00:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 5);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/5/2008 00:00:00,OFF,4:00:00,CAMotionPicture,12:00,15:00,44:00:00,,,");
        testHelpers.ProcessEvent("11/5/2008 04:00:00,SLP,3:00:00,CAMotionPicture,12:00,15:00,44:00:00,,,");
        testHelpers.ProcessEvent("11/5/2008 07:00:00,ON,0:30:00,CAMotionPicture,12:00,14:30,43:30:00,,,");
        testHelpers.ProcessEvent("11/5/2008 07:30:00,DRV,3:30:00,CAMotionPicture,8:30,11:00,40:00:00,,,");
        testHelpers.ProcessEvent("11/5/2008 11:00:00,SLP,2:00:00,CAMotionPicture,8:30,11:00,40:00:00,,,");
        testHelpers.ProcessEvent("11/5/2008 13:00:00,DRV,8:00:00,CAMotionPicture,0:30,3:00,32:00,,,");
        testHelpers.ProcessEvent("11/5/2008 21:00:00,ON,0:30:00,CAMotionPicture,0:30,2:30,31:30,,,");
        testHelpers.ProcessEvent("11/5/2008 21:30:00,OFF,2:30:00,CAMotionPicture,0:30,2:30,31:30,,,");

        cal.set(Calendar.DAY_OF_MONTH, 6);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/6/2008 00:00:00,OFF,7:30:00,CAMotionPicture,12:00,15:00,31:30,,,");
        testHelpers.ProcessEvent("11/6/2008 07:30:00,ON,0:30:00,CAMotionPicture,12:00,14:30,31:00,,,");
        testHelpers.ProcessEvent("11/6/2008 08:00:00,DRV,8:00:00,CAMotionPicture,4:00,6:30,23:00,,,");
        testHelpers.ProcessEvent("11/6/2008 16:00:00,ON,1:00:00,CAMotionPicture,4:00,5:30,22:00,,,");
        testHelpers.ProcessEvent("11/6/2008 17:00:00,OFF,7:00:00,CAMotionPicture,4:00,5:30,22:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 7);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/7/2008 00:00:00,OFF,5:00:00,CAMotionPicture,12:00,15:00,22:00,,,");
        testHelpers.ProcessEvent("11/7/2008 05:00:00,ON,3:00:00,CAMotionPicture,12:00,12:00,19:00,,,");
        testHelpers.ProcessEvent("11/7/2008 08:00:00,DRV,2:00:00,CAMotionPicture,10:00,10:00,17:00,,,");
        testHelpers.ProcessEvent("11/7/2008 10:00:00,OFF,3:00:00,CAMotionPicture,10:00,10:00,17:00,,,");
        testHelpers.ProcessEvent("11/7/2008 13:00:00,DRV,7:00:00,CAMotionPicture,3:00,3:00,10:00,,,");
        testHelpers.ProcessEvent("11/7/2008 20:00:00,ON,1:00:00,CAMotionPicture,3:00,2:00,9:00,,,");
        testHelpers.ProcessEvent("11/7/2008 21:00:00,OFF,3:00:00,CAMotionPicture,3:00,2:00,9:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 8);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // Can't reset since no consecutive hours off duty
        testHelpers.ProcessEvent("11/8/2008 00:00:00,OFF,3:00:00,CAMotionPicture,3:00,2:00,9:00,,,");
        testHelpers.ProcessEvent("11/8/2008 03:00:00,ON,5:00:00,CAMotionPicture,3:00,0:00,4:00,,,");
        testHelpers.ProcessEvent("11/8/2008 08:00:00,DRV,2:00:00,CAMotionPicture,1:00,0:00,2:00,,,");
        testHelpers.ProcessEvent("11/8/2008 10:00:00,OFF,3:00:00,CAMotionPicture,1:00,0:00,2:00,,,");
        testHelpers.ProcessEvent("11/8/2008 13:00:00,DRV,4:00:00,CAMotionPicture,0:00,0:00,0:00,,,");
        testHelpers.ProcessEvent("11/8/2008 17:00:00,ON,1:00:00,CAMotionPicture,0:00,0:00,0:00,,,");
        testHelpers.ProcessEvent("11/8/2008 18:00:00,OFF,6:00:00,CAMotionPicture,0:00,0:00,0:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 9);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        // 8 consecutive off duty so reset daily limits 12 and 15
        testHelpers.ProcessEvent("11/9/2008 00:00:00,OFF,2:00:00,CAMotionPicture,12:00,15:00,0:00,,,");
    }

    @Test
    public void test_34HourReset() {
        // 80hr Weekly Duty limit
        // 15hr Daily Duty limit
        // 12hr Daily Drive limit
        LogProperties logProperties = new LogProperties();

        _rulesetEngine = RulesetFactory.ForCaliforniaMotionPicture80HourProperty(true);
        CalcEngineTestHelpers testHelpers = new CalcEngineTestHelpers(_rulesetEngine, _ruleset);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date logStartDate = cal.getTime();

        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        testHelpers.ProcessEvent("11/1/2008 00:00:00,OFF,1.00:00:00,CAMotionPicture,12:00,15:00,80:00:00,,TRUE");

        cal.set(Calendar.DAY_OF_MONTH, 2);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        testHelpers.ProcessEvent("11/2/2008 00:00:00,OFF,9:00:00,CAMotionPicture,12:00,15:00,80:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 9:00:00,ON,0:30:00,CAMotionPicture,12:00,14:30,79:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 9:30:00,DRV,6:00:00,CAMotionPicture,6:00,8:30,73:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 15:30:00,ON,1:30:00,CAMotionPicture,6:00,7:00,72:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 17:00:00,DRV,6:00:00,CAMotionPicture,0:00,1:00,66:00:00,,,");
        testHelpers.ProcessEvent("11/2/2008 23:00:00,ON,0:30:00,CAMotionPicture,0:00,0:30,65:30:00,,,");
        testHelpers.ProcessEvent("11/2/2008 23:30:00,OFF,0:30:00,CAMotionPicture,0:00,0:30,65:30:00,,,");

        cal.set(Calendar.DAY_OF_MONTH, 4);
        logStartDate = cal.getTime();
        logProperties.setLogDate(logStartDate);
        _rulesetEngine.PrepareStartOfLog(logProperties);
        testHelpers.ProcessEvent("11/4/2008 00:00:00,OFF,9:30:00,CAMotionPicture,12:00,15:00,80:00:00,,,");
    }

}
