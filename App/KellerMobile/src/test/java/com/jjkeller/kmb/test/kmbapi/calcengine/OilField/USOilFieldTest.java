package com.jjkeller.kmb.test.kmbapi.calcengine.OilField;

import android.os.Bundle;

import android.util.Log;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
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
public class USOilFieldTest {

    private IHosRulesetCalcEngine _rulesetEngine = null;
    private final RuleSetTypeEnum _ruleset = RuleSetTypeEnum.USOilField;

    @Test
    public void test_Standard() {

        @SuppressWarnings("UnusedAssignment") HoursOfServiceSummary summary;

        _rulesetEngine = RulesetFactory.ForUSOilField(true, true, false);

        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("03/10/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/10/2012 00:00:00,OFF,1.00:00:00,USOilField,11:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("03/11/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("3/11/2012 00:00:00,OFF,2:00:00,USOilField,11:00,14:00,70:00,,,");
        this.ProcessEvent("3/11/2012 02:00:00,ON,00:30:00,USOilField,11:00,13:30,69:30,,,");
        this.ProcessEvent("3/11/2012 02:30:00,DRV,01:00:00,USOilField,10:00,12:30,68:30,,,");
        this.ProcessEvent("3/11/2012 03:30:00,OFFWLLST,04:30:00,USOilField,10:00,12:30,68:30,,,");
        this.ProcessEvent("3/11/2012 08:00:00,ON,01:30:00,USOilField,10:00,11:00,67:00,,,");
        this.ProcessEvent("3/11/2012 09:30:00,DRV,02:30:00,USOilField,07:30,08:30,64:30,,,");
        this.ProcessEvent("3/11/2012 12:00:00,ON,01:00:00,USOilField,07:30,07:30,63:30,,,");
        this.ProcessEvent("3/11/2012 13:00:00,OFFWLLST,05:00:00,USOilField,07:30,07:30,63:30,,,");
        this.ProcessEvent("3/11/2012 18:00:00,ON,01:00:00,USOilField,07:30,06:30,62:30,,,");
        this.ProcessEvent("3/11/2012 19:00:00,DRV,05:00:00,USOilField,02:30,01:30,57:30,,,");

        logProperties.setLogDate(new Date(Date.parse("03/12/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/12/2012 00:00:00,DRV,01:30:00,USOilField,01:00,00:00,56:00,,,");
        this.ProcessEvent("03/12/2012 01:30:00,ON,00:30:00,USOilField,01:00,00:00,55:30,,,");
        this.ProcessEvent("03/12/2012 02:00:00,SLP,05:30:00,USOilField,04:30,06:00,55:30,,,");
        this.ProcessEvent("03/12/2012 07:30:00,ON,00:30:00,USOilField,04:30,05:30,55:00,,,");
        this.ProcessEvent("03/12/2012 08:00:00,DRV,04:30:00,USOilField,00:00,01:00,50:30,,,");
        this.ProcessEvent("03/12/2012 12:30:00,ON,00:30:00,USOilField,00:00,00:30,50:00,,,");
        this.ProcessEvent("03/12/2012 13:00:00,OFFWLLST,04:30:00,USOilField,06:30,08:30,50:00,,,");
        this.ProcessEvent("03/12/2012 17:30:00,ON,00:30:00,USOilField,06:30,08:00,49:30,,,");
        this.ProcessEvent("03/12/2012 18:00:00,DRV,04:30:00,USOilField,02:00,03:30,45:00,,,");
        this.ProcessEvent("03/12/2012 22:30:00,ON,00:30:00,USOilField,02:00,03:00,44:30,,,");
        this.ProcessEvent("03/12/2012 23:00:00,SLP,01:00:00,USOilField,02:00,02:00,44:30,,,");

        logProperties.setLogDate(new Date(Date.parse("03/13/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/13/2012 00:00:00,SLP,05:00:00,USOilField,06:30,08:30,44:30,,,");
        this.ProcessEvent("03/13/2012 05:00:00,DRV,05:00:00,USOilField,01:30,03:30,39:30,,,");
        this.ProcessEvent("03/13/2012 10:00:00,ON,00:30:00,USOilField,01:30,03:00,39:00,,,");
        this.ProcessEvent("03/13/2012 10:30:00,OFFWLLST,04:00:00,USOilField,06:00,08:30,39:00,,,");
        this.ProcessEvent("03/13/2012 14:30:00,ON,00:30:00,USOilField,06:00,08:00,38:30,,,");
        this.ProcessEvent("03/13/2012 15:00:00,DRV,05:30:00,USOilField,00:30,02:30,33:00,,,");
        this.ProcessEvent("03/13/2012 20:30:00,ON,01:00:00,USOilField,00:30,01:30,32:00,,,");
        this.ProcessEvent("03/13/2012 21:30:00,SLP,02:30:00,USOilField,00:30,00:00,32:00,,,");

        logProperties.setLogDate(new Date(Date.parse("03/14/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/14/2012 00:00:00,SLP,04:00:00,USOilField,05:30,07:00,32:00,,,");
        this.ProcessEvent("03/14/2012 04:00:00,ON,00:30:00,USOilField,05:30,06:30,31:30,,,");
        this.ProcessEvent("03/14/2012 04:30:00,OFFWLLST,01:30:00,USOilField,05:30,06:30,31:30,,,");
        this.ProcessEvent("03/14/2012 06:00:00,DRV,05:00:00,USOilField,00:30,01:30,26:30,,,");
        this.ProcessEvent("03/14/2012 11:00:00,ON,00:30:00,USOilField,00:30,01:00,26:00,,,");
        this.ProcessEvent("03/14/2012 11:30:00,OFFWLLST,05:00:00,USOilField,06:00,08:00,26:00,,,");
        this.ProcessEvent("03/14/2012 16:30:00,ON,01:00:00,USOilField,06:00,07:00,25:00,,,");
        this.ProcessEvent("03/14/2012 17:30:00,DRV,02:30:00,USOilField,03:30,04:30,22:30,,,");
        this.ProcessEvent("03/14/2012 20:00:00,ON,00:30:00,USOilField,03:30,04:00,22:00,,,");
        this.ProcessEvent("03/14/2012 20:30:00,OFF,02:00:00,USOilField,03:30,02:00,22:00,,,");
        this.ProcessEvent("03/14/2012 22:30:00 ,ON,01:00:00,USOilField,03:30,01:00,21:00,,,");
        this.ProcessEvent("03/14/2012 23:30:00,DRV,00:30:00,USOilField,03:00,00:30,20:30,,,");

        logProperties.setLogDate(new Date(Date.parse("03/15/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/15/2012 00:00:00,DRV,00:15:00,USOilField,02:45,00:15,20:15,,,");
        this.ProcessEvent("03/15/2012 00:15:00,ON,00:30:00,USOilField,02:45,00:00,19:45,,,");
        this.ProcessEvent("03/15/2012 00:45:00,OFF,00:45:00,USOilField,02:45,00:00,19:45,,,");
        this.ProcessEvent("03/15/2012 01:30:00,SLP,05:00:00,USOilField,07:45,05:00,19:45,,,");
        this.ProcessEvent("03/15/2012 06:30:00,ON,00:30:00,USOilField,07:45,04:30,19:15,,,");
        this.ProcessEvent("03/15/2012 07:00:00,DRV,05:30:00,USOilField,02:15,00:00,13:45,,,");
        this.ProcessEvent("03/15/2012 12:30:00,ON,00:30:00,USOilField,02:15,00:00,13:15,,,");
        this.ProcessEvent("03/15/2012 13:00:00,OFFWLLST,05:00:00,USOilField,05:30,07:30,13:15,,,");
        this.ProcessEvent("03/15/2012 18:00:00,ON,01:00:00,USOilField,05:30,06:30,12:15,,,");
        this.ProcessEvent("03/15/2012 19:00:00,OFF,01:00:00,USOilField,05:30,05:30,12:15,,,");
        this.ProcessEvent("03/15/2012 20:00:00,ON,00:30:00,USOilField,05:30,05:00,11:45,,,");
        this.ProcessEvent("03/15/2012 20:30:00,DRV,01:30:00,USOilField,04:00,03:30,10:15,,,");
        this.ProcessEvent("03/15/2012 22:00:00,ON,00:30:00,USOilField,04:00,03:00,09:45,,,");
        this.ProcessEvent("03/15/2012 22:30:00,OFF,01:30:00,USOilField,04:00,01:30,09:45,,,");

        logProperties.setLogDate(new Date(Date.parse("03/16/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/16/2012 00:00:00,OFF,1.00:00:00,USOilField,11:00,14:00,70:00,,,");

        logProperties.setLogDate(new Date(Date.parse("03/17/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/17/2012 00:00:00,OFF,02:00:00,USOilField,11:00,14:00,70:00,,,");
        this.ProcessEvent("03/17/2012 02:00:00,ON,00:30:00,USOilField,11:00,13:30,69:30,,,");
        this.ProcessEvent("03/17/2012 02:30:00,DRV,01:00:00,USOilField,10:00,12:30,68:30,,,");
        this.ProcessEvent("03/17/2012 03:30:00,OFFWLLST,03:30:00,USOilField,10:00,12:30,68:30,,,");
        this.ProcessEvent("03/17/2012 07:00:00,ON,01:00:00,USOilField,10:00,11:30,67:30,,,");
        this.ProcessEvent("03/17/2012 08:00:00,OFF,04:00:00,USOilField,10:00,07:30,67:30,,,");
        this.ProcessEvent("03/17/2012 12:00:00,ON,00:30:00,USOilField,10:00,07:00,67:00,,,");
        this.ProcessEvent("03/17/2012 12:30:00,DRV,01:00:00,USOilField,09:00,06:00,66:00,,,");
        this.ProcessEvent("03/17/2012 13:30:00,ON,00:45:00,USOilField,09:00,05:15,65:15,,,");
        this.ProcessEvent("03/17/2012 14:15:00,OFFWLLST,03:45:00,USOilField,09:00,05:15,65:15,,,");
        this.ProcessEvent("03/17/2012 18:00:00,ON,00:45:00,USOilField,09:00,04:30,64:30,,,");
        this.ProcessEvent("03/17/2012 18:45:00,DRV,00:30:00,USOilField,08:30,04:00,64:00,,,");
        this.ProcessEvent("03/17/2012 19:15:00,ON,00:45:00,USOilField,08:30,03:15,63:15,,,");
        this.ProcessEvent("03/17/2012 20:00:00,OFF,04:00:00,USOilField,08:30,00:00,63:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/18/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/18/2012 00:00:00,OFF,06:00:00,USOilField,11:00,14:00,63:15,,,");
        this.ProcessEvent("03/18/2012 06:00:00,ON,00:30:00,USOilField,11:00,13:30,62:45,,,");
        this.ProcessEvent("03/18/2012 06:30:00,DRV,00:30:00,USOilField,10:30,13:00,62:15,,,");
        this.ProcessEvent("03/18/2012 07:00:00,ON,00:30:00,USOilField,10:30,12:30,61:45,,,");
        this.ProcessEvent("03/18/2012 07:30:00,OFFWLLST,01:00:00,USOilField,10:30,12:30,61:45,,,");
        this.ProcessEvent("03/18/2012 08:30:00,ON,00:30:00,USOilField,10:30,12:00,61:15,,,");
        this.ProcessEvent("03/18/2012 09:00:00,OFFWLLST,03:30:00,USOilField,10:30,12:00,61:15,,,");
        this.ProcessEvent("03/18/2012 12:30:00,ON,00:30:00,USOilField,10:30,11:30,60:45,,,");
        this.ProcessEvent("03/18/2012 13:00:00,DRV,01:15:00,USOilField,09:15,10:15,59:30,,,");
        this.ProcessEvent("03/18/2012 14:15:00,ON,00:45:00,USOilField,09:15,09:30,58:45,,,");
        this.ProcessEvent("03/18/2012 15:00:00,OFF,01:30:00,USOilField,09:15,08:00,58:45,,,");
        this.ProcessEvent("03/18/2012 16:30:00,ON,01:00:00,USOilField,09:15,07:00,57:45,,,");
        this.ProcessEvent("03/18/2012 17:30:00,DRV,01:00:00,USOilField,08:15,06:00,56:45,,,");
        this.ProcessEvent("03/18/2012 18:30:00,ON,00:30:00,USOilField,08:15,05:30,56:15,,,");
        this.ProcessEvent("03/18/2012 19:00:00,OFF,05:00:00,USOilField,08:15,00:30,56:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/19/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/19/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,56:15,,,");
        this.ProcessEvent("03/19/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,55:45,,,");
        this.ProcessEvent("03/19/2012 05:30:00,DRV,01:00:00,USOilField,10:00,12:30,54:45,,,");
        this.ProcessEvent("03/19/2012 06:30:00,ON,00:30:00,USOilField,10:00,12:00,54:15,,,");
        this.ProcessEvent("03/19/2012 07:00:00,OFFWLLST,03:30:00,USOilField,10:00,12:00,54:15,,,");
        this.ProcessEvent("03/19/2012 10:30:00,ON,00:30:00,USOilField,10:00,11:30,53:45,,,");
        this.ProcessEvent("03/19/2012 11:00:00,OFF,02:00:00,USOilField,10:00,09:30,53:45,,,");
        this.ProcessEvent("03/19/2012 13:00:00,ON,00:30:00,USOilField,10:00,09:00,53:15,,,");
        this.ProcessEvent("03/19/2012 13:30:00,DRV,00:30:00,USOilField,09:30,08:30,52:45,,,");
        this.ProcessEvent("03/19/2012 14:00:00,ON,01:00:00,USOilField,09:30,07:30,51:45,,,");
        this.ProcessEvent("03/19/2012 15:00:00,OFFWLLST,02:00:00,USOilField,09:30,07:30,51:45,,,");
        this.ProcessEvent("03/19/2012 17:00:00,ON,00:30:00,USOilField,09:30,07:00,51:15,,,");
        this.ProcessEvent("03/19/2012 17:30:00,DRV,01:00:00,USOilField,08:30,06:00,50:15,,,");
        this.ProcessEvent("03/19/2012 18:30:00,ON,00:30:00,USOilField,08:30,05:30,49:45,,,");
        this.ProcessEvent("03/19/2012 19:00:00,OFF,05:00:00,USOilField,08:30,00:30,49:45,,,");

        logProperties.setLogDate(new Date(Date.parse("03/20/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/20/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,49:45,,,");
        this.ProcessEvent("03/20/2012 05:00:00,ON,01:00:00,USOilField,11:00,13:00,48:45,,,");
        this.ProcessEvent("03/20/2012 06:00:00,DRV,00:30:00,USOilField,10:30,12:30,48:15,,,");
        this.ProcessEvent("03/20/2012 06:30:00,ON,00:30:00,USOilField,10:30,12:00,47:45,,,");
        this.ProcessEvent("03/20/2012 07:00:00,OFFWLLST,03:30:00,USOilField,10:30,12:00,47:45,,,");
        this.ProcessEvent("03/20/2012 10:30:00,ON,01:00:00,USOilField,10:30,11:00,46:45,,,");
        this.ProcessEvent("03/20/2012 11:30:00,OFFWLLST,02:00:00,USOilField,10:30,11:00,46:45,,,");
        this.ProcessEvent("03/20/2012 13:30:00,ON,00:30:00,USOilField,10:30,10:30,46:15,,,");
        this.ProcessEvent("03/20/2012 14:00:00,DRV,01:30:00,USOilField,09:00,09:00,44:45,,,");
        this.ProcessEvent("03/20/2012 15:30:00,ON,01:00:00,USOilField,09:00,08:00,43:45,,,");
        this.ProcessEvent("03/20/2012 16:30:00,OFFWLLST,01:00:00,USOilField,09:00,08:00,43:45,,,");
        this.ProcessEvent("03/20/2012 17:30:00,ON,00:30:00,USOilField,09:00,07:30,43:15,,,");
        this.ProcessEvent("03/20/2012 18:00:00,DRV,00:30:00,USOilField,08:30,07:00,42:45,,,");
        this.ProcessEvent("03/20/2012 18:30:00,ON,00:30:00,USOilField,08:30,06:30,42:15,,,");
        this.ProcessEvent("03/20/2012 19:00:00,OFF,05:00:00,USOilField,08:30,01:30,42:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/21/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/21/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,42:15,,,");
        this.ProcessEvent("03/21/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,41:45,,,");
        this.ProcessEvent("03/21/2012 05:30:00,DRV,01:00:00,USOilField,10:00,12:30,40:45,,,");
        this.ProcessEvent("03/21/2012 06:30:00,ON,00:30:00,USOilField,10:00,12:00,40:15,,,");
        this.ProcessEvent("03/21/2012 07:00:00,OFFWLLST,03:00:00,USOilField,10:00,12:00,40:15,,,");
        this.ProcessEvent("03/21/2012 10:00:00,ON,00:30:00,USOilField,10:00,11:30,39:45,,,");
        this.ProcessEvent("03/21/2012 10:30:00,SLP,01:30:00,USOilField,10:00,10:00,39:45,,,");
        this.ProcessEvent("03/21/2012 12:00:00,ON,00:30:00,USOilField,10:00,09:30,39:15,,,");
        this.ProcessEvent("03/21/2012 12:30:00,DRV,01:00:00,USOilField,09:00,08:30,38:15,,,");
        this.ProcessEvent("03/21/2012 13:30:00,ON,00:30:00,USOilField,09:00,08:00,37:45,,,");
        this.ProcessEvent("03/21/2012 14:00:00,OFFWLLST,03:00:00,USOilField,09:00,08:00,37:45,,,");
        this.ProcessEvent("03/21/2012 17:00:00,ON,00:30:00,USOilField,09:00,07:30,37:15,,,");
        this.ProcessEvent("03/21/2012 17:30:00,DRV,01:00:00,USOilField,08:00,06:30,36:15,,,");
        this.ProcessEvent("03/21/2012 18:30:00,ON,00:30:00,USOilField,08:00,06:00,35:45,,,");
        this.ProcessEvent("03/21/2012 19:00:00,OFF,05:00:00,USOilField,08:00,01:00,35:45,,,");

        logProperties.setLogDate(new Date(Date.parse("03/22/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/22/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,35:45,,,");
        this.ProcessEvent("03/22/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,35:15,,,");
        this.ProcessEvent("03/22/2012 05:30:00,DRV,01:00:00,USOilField,10:00,12:30,34:15,,,");
        this.ProcessEvent("03/22/2012 06:30:00,OFFWLLST,03:00:00,USOilField,10:00,12:30,34:15,,,");
        this.ProcessEvent("03/22/2012 09:30:00,ON,00:30:00,USOilField,10:00,12:00,33:45,,,");
        this.ProcessEvent("03/22/2012 10:00:00,DRV,00:30:00,USOilField,09:30,11:30,33:15,,,");
        this.ProcessEvent("03/22/2012 10:30:00,ON,00:30:00,USOilField,09:30,11:00,32:45,,,");
        this.ProcessEvent("03/22/2012 11:00:00,SLP,02:30:00,USOilField,09:30,08:30,32:45,,,");
        this.ProcessEvent("03/22/2012 13:30:00,ON,00:30:00,USOilField,09:30,08:00,32:15,,,");
        this.ProcessEvent("03/22/2012 14:00:00,OFFWLLST,03:00:00,USOilField,09:30,08:00,32:15,,,");
        this.ProcessEvent("03/22/2012 17:00:00,ON,00:30:00,USOilField,09:30,07:30,31:45,,,");
        this.ProcessEvent("03/22/2012 17:30:00,DRV,01:00:00,USOilField,08:30,06:30,30:45,,,");
        this.ProcessEvent("03/22/2012 18:30:00,ON,00:30:00,USOilField,08:30,06:00,30:15,,,");
        this.ProcessEvent("03/22/2012 19:00:00,OFF,05:00:00,USOilField,08:30,01:00,30:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/23/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/23/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,30:15,,,");
        this.ProcessEvent("03/23/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,29:45,,,");
        this.ProcessEvent("03/23/2012 05:30:00,DRV,01:00:00,USOilField,10:00,12:30,28:45,,,");
        this.ProcessEvent("03/23/2012 06:30:00,ON,00:30:00,USOilField,10:00,12:00,28:15,,,");
        this.ProcessEvent("03/23/2012 07:00:00,OFFWLLST,05:30:00,USOilField,10:00,12:00,28:15,,,");
        this.ProcessEvent("03/23/2012 12:30:00,ON,01:30:00,USOilField,10:00,10:30,26:45,,,");
        this.ProcessEvent("03/23/2012 14:00:00,DRV,00:30:00,USOilField,09:30,10:00,26:15,,,");
        this.ProcessEvent("03/23/2012 14:30:00,ON,00:30:00,USOilField,09:30,09:30,25:45,,,");
        this.ProcessEvent("03/23/2012 15:00:00,OFF,01:30:00,USOilField,09:30,08:00,25:45,,,");
        this.ProcessEvent("03/23/2012 16:30:00,ON,00:30:00,USOilField,09:30,07:30,25:15,,,");
        this.ProcessEvent("03/23/2012 17:00:00,DRV,01:30:00,USOilField,08:00,06:00,23:45,,,");
        this.ProcessEvent("03/23/2012 18:30:00,ON,00:30:00,USOilField,08:00,05:30,23:15,,,");
        this.ProcessEvent("03/23/2012 19:00:00,OFFWLLST,03:30:00,USOilField,08:00,05:30,23:15,,,");
        this.ProcessEvent("03/23/2012 22:30:00,SLP,01:30:00,USOilField,08:00,04:00,23:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/24/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/24/2012 00:00:00,SLP,04:30:00,USOilField,09:00,07:30,23:15,,,");
        this.ProcessEvent("03/24/2012 04:30:00,OFF,00:30:00,USOilField,11:00,14:00,23:15,,,");
        this.ProcessEvent("03/24/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,22:45,,,");
        this.ProcessEvent("03/24/2012 05:30:00,DRV,01:00:00,USOilField,10:00,12:30,21:45,,,");
        this.ProcessEvent("03/24/2012 06:30:00,ON,00:30:00,USOilField,10:00,12:00,21:15,,,");
        this.ProcessEvent("03/24/2012 07:00:00,OFFWLLST,02:30:00,USOilField,10:00,12:00,21:15,,,");
        this.ProcessEvent("03/24/2012 09:30:00,ON,00:30:00,USOilField,10:00,11:30,20:45,,,");
        this.ProcessEvent("03/24/2012 10:00:00,DRV,01:00:00,USOilField,09:00,10:30,19:45,,,");
        this.ProcessEvent("03/24/2012 11:00:00,ON,01:00:00,USOilField,09:00,09:30,18:45,,,");
        this.ProcessEvent("03/24/2012 12:00:00,OFF,02:00:00,USOilField,09:00,07:30,18:45,,,");
        this.ProcessEvent("03/24/2012 14:00:00,ON,00:30:00,USOilField,09:00,07:00,18:15,,,");
        this.ProcessEvent("03/24/2012 14:30:00,DRV,01:00:00,USOilField,08:00,06:00,17:15,,,");
        this.ProcessEvent("03/24/2012 15:30:00,ON,00:30:00,USOilField,08:00,05:30,16:45,,,");
        this.ProcessEvent("03/24/2012 16:00:00,OFFWLLST,01:30:00,USOilField,08:00,05:30,16:45,,,");
        this.ProcessEvent("03/24/2012 17:30:00,ON,00:30:00,USOilField,08:00,05:00,16:15,,,");
        this.ProcessEvent("03/24/2012 18:00:00,DRV,00:30:00,USOilField,07:30,04:30,15:45,,,");
        this.ProcessEvent("03/24/2012 18:30:00,ON,00:30:00,USOilField,07:30,04:00,15:15,,,");
        this.ProcessEvent("03/24/2012 19:00:00,OFF,05:00:00,USOilField,07:30,00:00,15:15,,,");

        logProperties.setLogDate(new Date(Date.parse("03/25/2012")));
        _rulesetEngine.PrepareStartOfLog(logProperties);
        this.ProcessEvent("03/25/2012 00:00:00,OFF,05:00:00,USOilField,11:00,14:00,22:00,,,");
        this.ProcessEvent("03/25/2012 05:00:00,ON,00:30:00,USOilField,11:00,13:30,21:30,,,");
        this.ProcessEvent("03/25/2012 05:30:00,DRV,02:00:00,USOilField,09:00,11:30,19:30,,,");
        this.ProcessEvent("03/25/2012 07:30:00,ON,01:00:00,USOilField,09:00,10:30,18:30,,,");
        this.ProcessEvent("03/25/2012 08:30:00,OFFWLLST,03:00:00,USOilField,09:00,10:30,18:30,,,");
        this.ProcessEvent("03/25/2012 11:30:00,ON,01:00:00,USOilField,09:00,09:30,17:30,,,");
        this.ProcessEvent("03/25/2012 12:30:00,OFFWLLST,02:00:00,USOilField,09:00,09:30,17:30,,,");
        this.ProcessEvent("03/25/2012 14:30:00,ON,01:00:00,USOilField,09:00,08:30,16:30,,,");
        this.ProcessEvent("03/25/2012 15:30:00,OFF,01:30:00,USOilField,09:00,07:00,16:30,,,");
        this.ProcessEvent("03/25/2012 17:00:00,ON,00:30:00,USOilField,09:00,06:30,16:00,,,");
        this.ProcessEvent("03/25/2012 17:30:00,DRV,01:00:00,USOilField,08:00,05:30,15:00,,,");
        this.ProcessEvent("03/25/2012 18:30:00,ON,00:30:00,USOilField,08:00,05:00,14:30,,,");
        this.ProcessEvent("03/25/2012 19:00:00,OFF,05:00:00,USOilField,08:00,00:00,14:30,,,");


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
