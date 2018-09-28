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
public class TexasOilFieldTest extends KmbRoboTestBase {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.TexasOilField;


    @Test
    public void test_Standard() {

        //HoursOfServiceSummary summary;

        _rulesetEngine = RulesetFactory.ForTexasOilField(true);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("10/1/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/1/2013 0:00:00,OFF,1.00:00:00,TexasOilField,12:00,15:00,70:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/2/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/2/2013 0:00:00,OFF,6:00:00,TexasOilField,12:00,15:00,70:00:00,1,,,");
        this.ProcessEvent("10/2/2013 6:00:00,ON,0:30:00,TexasOilField,12:00,14:30,69:30:00,1,,,");
        this.ProcessEvent("10/2/2013 6:30:00,DRV,4:00:00,TexasOilField,8:00,10:30,65:30:00,1,,,");
        this.ProcessEvent("10/2/2013 10:30:00,OFFWLLST,1:30:00,TexasOilField,8:00,10:30,65:30:00,1,,,");
        this.ProcessEvent("10/2/2013 12:00:00,ON,0:30:00,TexasOilField,8:00,10:00,65:00:00,1,,,");
        this.ProcessEvent("10/2/2013 12:30:00,OFF,2:30:00,TexasOilField,8:00,10:00,65:00:00,1,,,");
        this.ProcessEvent("10/2/2013 15:00:00,DRV,5:00:00,TexasOilField,3:00,5:00,60:00:00,1,,,");
        this.ProcessEvent("10/2/2013 20:00:00,ON,1:00:00,TexasOilField,3:00,4:00,59:00:00,1,,,");
        this.ProcessEvent("10/2/2013 21:00:00,OFF,3:00:00,TexasOilField,3:00,4:00,59:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/3/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/3/2013 0:00:00,OFF,5:00:00,TexasOilField,12:00,15:00,59:00:00,1,,,");
        this.ProcessEvent("10/3/2013 5:00:00,ON,0:30:00,TexasOilField,12:00,14:30,58:30:00,1,,,");
        this.ProcessEvent("10/3/2013 5:30:00,DRV,3:00:00,TexasOilField,9:00,11:30,55:30:00,1,,,");
        this.ProcessEvent("10/3/2013 8:30:00,ON,0:30:00,TexasOilField,9:00,11:00,55:00:00,1,,,");
        this.ProcessEvent("10/3/2013 9:00:00,OFFWLLST,3:00:00,TexasOilField,9:00,11:00,55:00:00,1,,,");
        this.ProcessEvent("10/3/2013 12:00:00,ON,0:30:00,TexasOilField,9:00,10:30,54:30:00,1,,,");
        this.ProcessEvent("10/3/2013 12:30:00,DRV,8:00:00,TexasOilField,1:00,2:30,46:30:00,1,,,");
        this.ProcessEvent("10/3/2013 20:30:00,ON,0:30:00,TexasOilField,1:00,2:00,46:00:00,1,,,");
        this.ProcessEvent("10/3/2013 21:00:00,SLP,3:00:00,TexasOilField,1:00,2:00,46:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/4/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/4/2013 0:00:00,SLP,2:00:00,TexasOilField,4:00,6:00,46:00:00,1,,,");
        this.ProcessEvent("10/4/2013 2:00:00,ON,1:00:00,TexasOilField,4:00,5:00,45:00:00,1,,,");
        this.ProcessEvent("10/4/2013 3:00:00,DRV,4:00:00,TexasOilField,0:00,1:00,41:00:00,1,,,");
        this.ProcessEvent("10/4/2013 7:00:00,ON,0:30:00,TexasOilField,0:00,0:30,40:30:00,1,,,");
        this.ProcessEvent("10/4/2013 7:30:00,OFFWLLST,3:00:00,TexasOilField,8:00,9:30,40:30:00,1,,,");
        this.ProcessEvent("10/4/2013 10:30:00,ON,1:00:00,TexasOilField,8:00,8:30,39:30:00,1,,,");
        this.ProcessEvent("10/4/2013 11:30:00,DRV,4:00:00,TexasOilField,4:00,4:30,35:30:00,1,,,");
        this.ProcessEvent("10/4/2013 15:30:00,ON,0:30:00,TexasOilField,4:00,4:00,35:00:00,1,,,");
        this.ProcessEvent("10/4/2013 16:00:00,SLP,3:00:00,TexasOilField,4:00,4:00,35:00:00,1,,,");
        this.ProcessEvent("10/4/2013 19:00:00,OFF,2:00:00,TexasOilField,4:00,4:00,35:00:00,1,,,");
        this.ProcessEvent("10/4/2013 21:00:00,ON,0:30:00,TexasOilField,4:00,3:30,34:30:00,1,,,");
        this.ProcessEvent("10/4/2013 21:30:00,DRV,2:30:00,TexasOilField,1:30,1:00,32:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/5/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/5/2013 0:00:00,DRV,1:00:00,TexasOilField,0:30,0:00,31:00:00,1,,,");
        this.ProcessEvent("10/5/2013 1:00:00,ON,0:30:00,TexasOilField,0:30,0:00,30:30:00,1,,,");
        this.ProcessEvent("10/5/2013 1:30:00,SLP,5:00:00,TexasOilField,8:30,10:30,30:30:00,1,,,");
        this.ProcessEvent("10/5/2013 6:30:00,ON,3:00:00,TexasOilField,8:30,7:30,27:30:00,1,,,");
        this.ProcessEvent("10/5/2013 9:30:00,DRV,5:00:00,TexasOilField,3:30,2:30,22:30:00,1,,,");
        this.ProcessEvent("10/5/2013 14:30:00,OFFWLLST,3:00:00,TexasOilField,7:00,7:00,22:30:00,1,,,");
        this.ProcessEvent("10/5/2013 17:30:00,ON,1:00:00,TexasOilField,7:00,6:00,21:30:00,1,,,");
        this.ProcessEvent("10/5/2013 18:30:00,DRV,3:00:00,TexasOilField,4:00,3:00,18:30:00,1,,,");
        this.ProcessEvent("10/5/2013 21:30:00,ON,0:30:00,TexasOilField,4:00,2:30,18:00:00,1,,,");
        this.ProcessEvent("10/5/2013 22:00:00,OFF,2:00:00,TexasOilField,4:00,2:30,18:00:00,1,,,");


        logProperties.setLogDate(new Date(Date.parse("10/6/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/6/2013 0:00:00,OFF,12:00:00,TexasOilField,12:00,15:00,18:00:00,0,,,");
        this.ProcessEvent("10/6/2013 12:00:00,ON,1:00:00,TexasOilField,12:00,14:00,17:00:00,0,,,");
        this.ProcessEvent("10/6/2013 13:00:00,DRV,1:00:00,TexasOilField,11:00,13:00,16:00:00,0,,,");
        this.ProcessEvent("10/6/2013 14:00:00,ON,0:30:00,TexasOilField,11:00,12:30,15:30:00,0,,,");
        this.ProcessEvent("10/6/2013 14:30:00,OFF,9:30:00,TexasOilField,12:00,15:00,15:30:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/7/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/7/2013 0:00:00,OFF,12:00:00,TexasOilField,12:00,15:00,15:30:00,0,,,");
        this.ProcessEvent("10/7/2013 12:00:00,ON,1:00:00,TexasOilField,12:00,14:00,14:30:00,0,,,");
        this.ProcessEvent("10/7/2013 13:00:00,DRV,1:00:00,TexasOilField,11:00,13:00,13:30:00,0,,,");
        this.ProcessEvent("10/7/2013 14:00:00,ON,0:30:00,TexasOilField,11:00,12:30,13:00:00,0,,,");
        this.ProcessEvent("10/7/2013 14:30:00,OFF,9:30:00,TexasOilField,12:00,15:00,13:00:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/8/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/8/2013 0:00:00,OFF,12:00:00,TexasOilField,12:00,15:00,13:00:00,0,,,");
        this.ProcessEvent("10/8/2013 12:00:00,ON,1:00:00,TexasOilField,12:00,14:00,12:00:00,0,,,");
        this.ProcessEvent("10/8/2013 13:00:00,DRV,1:00:00,TexasOilField,11:00,13:00,11:00:00,0,,,");
        this.ProcessEvent("10/8/2013 14:00:00,ON,0:30:00,TexasOilField,11:00,12:30,10:30:00,0,,,");
        this.ProcessEvent("10/8/2013 14:30:00,OFF,9:30:00,TexasOilField,12:00,15:00,10:30:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/9/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/9/2013 0:00:00,OFF,12:00:00,TexasOilField,12:00,15:00,21:30:00,0,,,");
        this.ProcessEvent("10/9/2013 12:00:00,ON,1:00:00,TexasOilField,12:00,14:00,20:30:00,0,,,");
        this.ProcessEvent("10/9/2013 13:00:00,DRV,1:00:00,TexasOilField,11:00,13:00,19:30:00,0,,,");
        this.ProcessEvent("10/9/2013 14:00:00,ON,0:30:00,TexasOilField,11:00,12:30,19:00:00,0,,,");
        this.ProcessEvent("10/9/2013 14:30:00,OFF,9:30:00,TexasOilField,12:00,15:00,19:00:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/10/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/10/2013 0:00:00,OFF,7:00:00,TexasOilField,12:00,15:00,32:00:00,1,,,");
        this.ProcessEvent("10/10/2013 7:00:00,ON,1:00:00,TexasOilField,12:00,14:00,31:00:00,1,,,");
        this.ProcessEvent("10/10/2013 8:00:00,DRV,4:00:00,TexasOilField,8:00,10:00,27:00:00,1,,,");
        this.ProcessEvent("10/10/2013 12:00:00,ON,0:30:00,TexasOilField,8:00,9:30,26:30:00,1,,,");
        this.ProcessEvent("10/10/2013 12:30:00,OFFWLLST,3:00:00,TexasOilField,8:00,9:30,26:30:00,1,,,");
        this.ProcessEvent("10/10/2013 15:30:00,ON,0:30:00,TexasOilField,8:00,9:00,26:00:00,1,,,");
        this.ProcessEvent("10/10/2013 16:00:00,DRV,3:00:00,TexasOilField,5:00,6:00,23:00:00,1,,,");
        this.ProcessEvent("10/10/2013 19:00:00,OFF,5:00:00,TexasOilField,5:00,6:00,23:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/11/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/11/2013 0:00:00,OFF,5:00:00,TexasOilField,12:00,15:00,37:00:00,1,,,");
        this.ProcessEvent("10/11/2013 5:00:00,ON,1:00:00,TexasOilField,12:00,14:00,36:00:00,1,,,");
        this.ProcessEvent("10/11/2013 6:00:00,DRV,5:00:00,TexasOilField,7:00,9:00,31:00:00,1,,,");
        this.ProcessEvent("10/11/2013 11:00:00,ON,0:15:00,TexasOilField,7:00,8:45,30:45:00,1,,,");
        this.ProcessEvent("10/11/2013 11:15:00,OFFWLLST,1:30:00,TexasOilField,7:00,8:45,30:45:00,1,,,");
        this.ProcessEvent("10/11/2013 12:45:00,ON,0:30:00,TexasOilField,7:00,8:15,30:15:00,1,,,");
        this.ProcessEvent("10/11/2013 13:15:00,OFF,0:15:00,TexasOilField,7:00,8:15,30:15:00,1,,,");
        this.ProcessEvent("10/11/2013 13:30:00,DRV,3:30:00,TexasOilField,3:30,4:45,26:45:00,1,,,");
        this.ProcessEvent("10/11/2013 17:00:00,ON,1:00:00,TexasOilField,3:30,3:45,25:45:00,1,,,");
        this.ProcessEvent("10/11/2013 18:00:00,OFF,6:00:00,TexasOilField,3:30,3:45,25:45:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/12/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/12/2013 0:00:00,OFF,1.00:00:00,TexasOilField,12:00,15:00,70:00:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/13/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/13/2013 0:00:00,OFF,3:00:00,TexasOilField,12:00,15:00,70:00:00,1,,,");
        this.ProcessEvent("10/13/2013 3:00:00,ON,3:00:00,TexasOilField,12:00,12:00,67:00:00,1,,,");
        this.ProcessEvent("10/13/2013 6:00:00,DRV,7:00:00,TexasOilField,5:00,5:00,60:00:00,1,,,");
        this.ProcessEvent("10/13/2013 13:00:00,OFFWLLST,1:00:00,TexasOilField,5:00,5:00,60:00:00,1,,,");
        this.ProcessEvent("10/13/2013 14:00:00,DRV,3:30:00,TexasOilField,1:30,1:30,56:30:00,1,,,");
        this.ProcessEvent("10/13/2013 17:30:00,ON,1:00:00,TexasOilField,1:30,0:30,55:30:00,1,,,");
        this.ProcessEvent("10/13/2013 18:30:00,DRV,2:00:00,TexasOilField,0:00,0:00,53:30:00,1,,,");
        this.ProcessEvent("10/13/2013 20:30:00,OFF,3:30:00,TexasOilField,0:00,0:00,53:30:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/14/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/14/2013 0:00:00,SLP,4:30:00,TexasOilField,12:00,15:00,53:30:00,1,,,");
        this.ProcessEvent("10/14/2013 4:30:00,ON,0:30:00,TexasOilField,12:00,14:30,53:00:00,1,,,");
        this.ProcessEvent("10/14/2013 5:00:00,DRV,6:00:00,TexasOilField,6:00,8:30,47:00:00,1,,,");
        this.ProcessEvent("10/14/2013 11:00:00,ON,2:30:00,TexasOilField,6:00,6:00,44:30:00,1,,,");
        this.ProcessEvent("10/14/2013 13:30:00,OFFWLLST,4:00:00,TexasOilField,6:00,6:00,44:30:00,1,,,");
        this.ProcessEvent("10/14/2013 17:30:00,SLP,4:00:00,TexasOilField,12:00,15:00,44:30:00,1,,,");
        this.ProcessEvent("10/14/2013 21:30:00,ON,0:30:00,TexasOilField,12:00,14:30,44:00:00,1,,,");
        this.ProcessEvent("10/14/2013 22:00:00,DRV,2:00:00,TexasOilField,10:00,12:30,42:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/15/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/15/2013 0:00:00,DRV,5:00:00,TexasOilField,5:00,7:30,37:00:00,1,,,");
        this.ProcessEvent("10/15/2013 5:00:00,ON,0:30:00,TexasOilField,5:00,7:00,36:30:00,1,,,");
        this.ProcessEvent("10/15/2013 5:30:00,OFF,4:00:00,TexasOilField,5:00,7:00,36:30:00,1,,,");
        this.ProcessEvent("10/15/2013 9:30:00,OFFWLLST,3:00:00,TexasOilField,5:00,7:00,36:30:00,1,,,");
        this.ProcessEvent("10/15/2013 12:30:00,SLP,2:00:00,TexasOilField,12:00,15:00,36:30:00,1,,,");
        this.ProcessEvent("10/15/2013 14:30:00,DRV,4:00:00,TexasOilField,8:00,11:00,32:30:00,1,,,");
        this.ProcessEvent("10/15/2013 18:30:00,ON,0:30:00,TexasOilField,8:00,10:30,32:00:00,1,,,");
        this.ProcessEvent("10/15/2013 19:00:00,OFF,5:00:00,TexasOilField,8:00,10:30,32:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/16/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/16/2013 0:00:00,OFF,1.00:00:00,TexasOilField,12:00,15:00,70:00:00,0,,,");

        logProperties.setLogDate(new Date(Date.parse("10/17/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/17/2013 0:00:00,OFF,3:00:00,TexasOilField,12:00,15:00,70:00:00,1,,,");
        this.ProcessEvent("10/17/2013 3:00:00,ON,1:30:00,TexasOilField,12:00,13:30,68:30:00,1,,,");
        this.ProcessEvent("10/17/2013 4:30:00,DRV,0:30:00,TexasOilField,11:30,13:00,68:00:00,1,,,");
        this.ProcessEvent("10/17/2013 5:00:00,OFFWLLST,3:15:00,TexasOilField,11:30,13:00,68:00:00,1,,,");
        this.ProcessEvent("10/17/2013 8:15:00,ON,2:30:00,TexasOilField,11:30,10:30,65:30:00,1,,,");
        this.ProcessEvent("10/17/2013 10:45:00,DRV,5:00:00,TexasOilField,6:30,5:30,60:30:00,1,,,");
        this.ProcessEvent("10/17/2013 15:45:00,ON,0:30:00,TexasOilField,6:30,5:00,60:00:00,1,,,");
        this.ProcessEvent("10/17/2013 16:15:00,OFFWLLST,4:45:00,TexasOilField,7:00,7:00,60:00:00,1,,,");
        this.ProcessEvent("10/17/2013 21:00:00,ON,1:00:00,TexasOilField,7:00,6:00,59:00:00,1,,,");
        this.ProcessEvent("10/17/2013 22:00:00,DRV,2:00:00,TexasOilField,5:00,4:00,57:00:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/18/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/18/2013 0:00:00,DRV,1:00:00,TexasOilField,4:00,3:00,56:00:00,1,,,");
        this.ProcessEvent("10/18/2013 1:00:00,ON,0:30:00,TexasOilField,4:00,2:30,55:30:00,1,,,");
        this.ProcessEvent("10/18/2013 1:30:00,OFF,22:30:00,TexasOilField,12:00,15:00,55:30:00,1,,,");

        logProperties.setLogDate(new Date(Date.parse("10/19/2013")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("10/19/2013 0:00:00,OFF,2:00:00,TexasOilField,12:00,15:00,70:00:00,1,,,");
        this.ProcessEvent("10/19/2013 2:00:00,ON,0:30:00,TexasOilField,12:00,14:30,69:30:00,1,,,");
        this.ProcessEvent("10/19/2013 2:30:00,DRV,2:00:00,TexasOilField,10:00,12:30,67:30:00,1,,,");
        this.ProcessEvent("10/19/2013 4:30:00,ON,0:30:00,TexasOilField,10:00,12:00,67:00:00,1,,,");
        this.ProcessEvent("10/19/2013 5:00:00,SLP,1:45:00,TexasOilField,10:00,12:00,67:00:00,1,,,");
        this.ProcessEvent("10/19/2013 6:45:00,ON,0:30:00,TexasOilField,10:00,11:30,66:30:00,1,,,");
        this.ProcessEvent("10/19/2013 7:15:00,DRV,4:00:00,TexasOilField,6:00,7:30,62:30:00,1,,,");
        this.ProcessEvent("10/19/2013 11:15:00,ON,0:15:00,TexasOilField,6:00,7:15,62:15:00,1,,,");
        this.ProcessEvent("10/19/2013 11:30:00,SLP,6:15:00,TexasOilField,6:00,7:15,62:15:00,1,,,");
        this.ProcessEvent("10/19/2013 17:45:00,DRV,6:15:00,TexasOilField,0:00,1:00,56:00:00,1,,,");
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

