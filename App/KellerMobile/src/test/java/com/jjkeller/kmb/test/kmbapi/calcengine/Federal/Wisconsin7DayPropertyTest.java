package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;


import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(KMBRoboElectricTestRunner.class)
public class Wisconsin7DayPropertyTest extends KmbRoboTestBase {

    @Before
    public void setUp(){

    }

    @Test
    public void test_Standard() {
        setupAppSettingsMocks();
        ProcessEventHelper event = new ProcessEventHelper(RuleSetTypeEnum.Wisconsin_7Day, RulesetFactory.ForWisconsin7DayProperty());

        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(new Date(Date.parse("11/02/2008")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        logProperties.setLogDate(new Date(Date.parse("01/02/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/02/2016 00:00:00,OFF,10:00:00,Wisconsin_7Day,12:00,16:00,70:00,,,");
        event.ProcessEvent("01/02/2016 10:00:00,ON,00:30:00,Wisconsin_7Day,12:00,15:30,69:30,,,");
        event.ProcessEvent("01/02/2016 10:30:00,DRV,06:00:00,Wisconsin_7Day,06:00,09:30,63:30,,,");
        event.ProcessEvent("01/02/2016 16:30:00,ON,01:30:00,Wisconsin_7Day,06:00,08:00,62:00,,,");
        event.ProcessEvent("01/02/2016 18:00:00,DRV,05:00:00,Wisconsin_7Day,01:00,03:00,57:00,,,");
        event.ProcessEvent("01/02/2016 23:00:00,ON,00:30:00,Wisconsin_7Day,01:00,02:30,56:30,,,");
        event.ProcessEvent("01/02/2016 23:30:00,OFF,00:30:00,Wisconsin_7Day,01:00,02:00,56:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/03/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/03/2016 00:00:00,OFF,09:30:00,Wisconsin_7Day,12:00,16:00,56:30,,,");
        event.ProcessEvent("01/03/2016 09:30:00,ON,02:00:00,Wisconsin_7Day,12:00,14:00,54:30,,,");
        event.ProcessEvent("01/03/2016 11:30:00,DRV,06:30:00,Wisconsin_7Day,05:30,07:30,48:00,,,");
        event.ProcessEvent("01/03/2016 18:00:00,ON,01:00:00,Wisconsin_7Day,05:30,06:30,47:00,,,");
        event.ProcessEvent("01/03/2016 19:00:00,OFF,05:00:00,Wisconsin_7Day,05:30,01:30,47:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/04/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/04/2016 00:00:00,OFF,05:00:00,Wisconsin_7Day,12:00,16:00,47:00,,,");
        event.ProcessEvent("01/04/2016 05:00:00,ON,01:30:00,Wisconsin_7Day,12:00,14:30,45:30,,,");
        event.ProcessEvent("01/04/2016 06:30:00,DRV,02:30:00,Wisconsin_7Day,09:30,12:00,43:00,,,");
        event.ProcessEvent("01/04/2016 09:00:00,ON,01:30:00,Wisconsin_7Day,09:30,10:30,41:30,,,");
        event.ProcessEvent("01/04/2016 10:30:00,DRV,01:30:00,Wisconsin_7Day,08:00,09:00,40:00,,,");
        event.ProcessEvent("01/04/2016 12:00:00,OFF,03:00:00,Wisconsin_7Day,08:00,06:00,40:00,,,");
        event.ProcessEvent("01/04/2016 15:00:00,ON,00:30:00,Wisconsin_7Day,08:00,05:30,39:30,,,");
        event.ProcessEvent("01/04/2016 15:30:00,DRV,04:00:00,Wisconsin_7Day,04:00,01:30,35:30,,,");
        event.ProcessEvent("01/04/2016 19:30:00,ON,00:30:00,Wisconsin_7Day,04:00,01:00,35:00,,,");
        event.ProcessEvent("01/04/2016 20:00:00,OFF,04:00:00,Wisconsin_7Day,04:00,00:00,35:00,,,");


        logProperties.setLogDate(new Date(Date.parse("01/05/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/05/2016 00:00:00,OFF,04:00:00,Wisconsin_7Day,04:00,00:00,35:00,,,");
        event.ProcessEvent("01/05/2016 04:00:00,SLP,03:00:00,Wisconsin_7Day,12:00,16:00,35:00,,,");
        event.ProcessEvent("01/05/2016 07:00:00,ON,00:30:00,Wisconsin_7Day,12:00,15:30,34:30,,,");
        event.ProcessEvent("01/05/2016 07:30:00,DRV,03:30:00,Wisconsin_7Day,08:30,12:00,31:00,,,");
        event.ProcessEvent("01/05/2016 11:00:00,SLP,02:00:00,Wisconsin_7Day,08:30,10:00,31:00,,,");
        event.ProcessEvent("01/05/2016 13:00:00,DRV,08:00:00,Wisconsin_7Day,00:30,02:00,23:00,,,");
        event.ProcessEvent("01/05/2016 21:00:00,ON,00:30:00,Wisconsin_7Day,00:30,01:30,22:30,,,");
        event.ProcessEvent("01/05/2016 21:30:00,OFF,02:30:00,Wisconsin_7Day,00:30,00:00,22:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/06/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/06/2016 00:00:00,OFF,07:30:00,Wisconsin_7Day,12:00,16:00,22:30,,,");
        event.ProcessEvent("01/06/2016 07:30:00,ON,00:30:00,Wisconsin_7Day,12:00,15:30,22:00,,,");
        event.ProcessEvent("01/06/2016 08:00:00,DRV,08:00:00,Wisconsin_7Day,04:00,07:30,14:00,,,");
        event.ProcessEvent("01/06/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,04:00,06:30,13:00,,,");
        event.ProcessEvent("01/06/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,04:00,00:00,13:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/07/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/07/2016 00:00:00,OFF,05:00:00,Wisconsin_7Day,12:00,16:00,13:00,,,");
        event.ProcessEvent("01/07/2016 05:00:00,ON,03:00:00,Wisconsin_7Day,12:00,13:00,10:00,,,");
        event.ProcessEvent("01/07/2016 08:00:00,DRV,02:00:00,Wisconsin_7Day,10:00,11:00,08:00,,,");
        event.ProcessEvent("01/07/2016 10:00:00,OFF,03:00:00,Wisconsin_7Day,10:00,08:00,08:00,,,");
        event.ProcessEvent("01/07/2016 13:00:00,DRV,07:00:00,Wisconsin_7Day,03:00,01:00,01:00,,,");
        event.ProcessEvent("01/07/2016 20:00:00,ON,01:00:00,Wisconsin_7Day,03:00,00:00,00:00,,,");
        event.ProcessEvent("01/07/2016 21:00:00,OFF,03:00:00,Wisconsin_7Day,03:00,00:00,00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/09/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/09/2016 00:00:00,OFF,07:00:00,Wisconsin_7Day,12:00,16:00,13:30,,,");
        event.ProcessEvent("01/09/2016 07:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,12:30,,,");
        event.ProcessEvent("01/09/2016 08:00:00,DRV,05:00:00,Wisconsin_7Day,07:00,10:00,07:30,,,");
        event.ProcessEvent("01/09/2016 13:00:00,OFF,02:00:00,Wisconsin_7Day,07:00,08:00,07:30,,,");
        event.ProcessEvent("01/09/2016 15:00:00,ON,02:00:00,Wisconsin_7Day,07:00,06:00,05:30,,,");
        event.ProcessEvent("01/09/2016 17:00:00,DRV,05:30:00,Wisconsin_7Day,01:30,00:30,00:00,,,");
        event.ProcessEvent("01/09/2016 22:30:00,ON,00:30:00,Wisconsin_7Day,01:30,00:00,00:00,,,");
        event.ProcessEvent("01/09/2016 23:00:00,OFF,01:00:00,Wisconsin_7Day,01:30,00:00,00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/11/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/11/2016 00:00:00,OFF,03:00:00,Wisconsin_7Day,12:00,16:00,21:00,,,");
        event.ProcessEvent("01/11/2016 03:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,20:00,,,");
        event.ProcessEvent("01/11/2016 04:00:00,DRV,04:00:00,Wisconsin_7Day,08:00,11:00,16:00,,,");
        event.ProcessEvent("01/11/2016 08:00:00,ON,01:00:00,Wisconsin_7Day,08:00,10:00,15:00,,,");
        event.ProcessEvent("01/11/2016 09:00:00,SLP,02:00:00,Wisconsin_7Day,08:00,08:00,15:00,,,");
        event.ProcessEvent("01/11/2016 11:00:00,ON,01:00:00,Wisconsin_7Day,08:00,07:00,14:00,,,");
        event.ProcessEvent("01/11/2016 12:00:00,DRV,05:00:00,Wisconsin_7Day,03:00,02:00,09:00,,,");
        event.ProcessEvent("01/11/2016 17:00:00,ON,01:00:00,Wisconsin_7Day,03:00,01:00,08:00,,,");
        event.ProcessEvent("01/11/2016 18:00:00,SLP,06:00:00,Wisconsin_7Day,03:00,00:00,08:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/12/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/12/2016 00:00:00,SLP,02:00:00,Wisconsin_7Day,07:00,09:00,20:30,,,");
        event.ProcessEvent("01/12/2016 02:00:00,ON,01:00:00,Wisconsin_7Day,07:00,08:00,19:30,,,");
        event.ProcessEvent("01/12/2016 03:00:00,DRV,06:00:00,Wisconsin_7Day,01:00,02:00,13:30,,,");
        event.ProcessEvent("01/12/2016 09:00:00,ON,01:00:00,Wisconsin_7Day,01:00,01:00,12:30,,,");
        event.ProcessEvent("01/12/2016 10:00:00,SLP,02:00:00,Wisconsin_7Day,06:00,06:00,12:30,,,");
        event.ProcessEvent("01/12/2016 12:00:00,ON,01:00:00,Wisconsin_7Day,06:00,05:00,11:30,,,");
        event.ProcessEvent("01/12/2016 13:00:00,DRV,03:00:00,Wisconsin_7Day,03:00,02:00,08:30,,,");
        event.ProcessEvent("01/12/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,03:00,01:00,07:30,,,");
        event.ProcessEvent("01/12/2016 17:00:00,SLP,07:00:00,Wisconsin_7Day,03:00,00:00,07:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/13/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/13/2016 00:00:00,SLP,02:00:00,Wisconsin_7Day,09:00,11:00,17:00,,,");
        event.ProcessEvent("01/13/2016 02:00:00,ON,01:00:00,Wisconsin_7Day,09:00,10:00,16:00,,,");
        event.ProcessEvent("01/13/2016 03:00:00,DRV,02:00:00,Wisconsin_7Day,07:00,08:00,14:00,,,");
        event.ProcessEvent("01/13/2016 05:00:00,OFF,01:00:00,Wisconsin_7Day,07:00,07:00,14:00,,,");
        event.ProcessEvent("01/13/2016 06:00:00,DRV,04:00:00,Wisconsin_7Day,03:00,03:00,10:00,,,");
        event.ProcessEvent("01/13/2016 10:00:00,ON,01:00:00,Wisconsin_7Day,03:00,02:00,09:00,,,");
        event.ProcessEvent("01/13/2016 11:00:00,OFF,00:15:00,Wisconsin_7Day,03:00,01:45,09:00,,,");
        event.ProcessEvent("01/13/2016 11:15:00,SLP,01:45:00,Wisconsin_7Day,06:00,05:00,09:00,,,");
        event.ProcessEvent("01/13/2016 13:00:00,ON,00:30:00,Wisconsin_7Day,06:00,04:30,08:30,,,");
        event.ProcessEvent("01/13/2016 13:30:00,DRV,03:30:00,Wisconsin_7Day,02:30,01:00,05:00,,,");
        event.ProcessEvent("01/13/2016 17:00:00,ON,01:00:00,Wisconsin_7Day,02:30,00:00,04:00,,,");
        event.ProcessEvent("01/13/2016 18:00:00,SLP,06:00:00,Wisconsin_7Day,02:30,00:00,04:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/14/2016")));

        event.ProcessEvent("01/14/2016 00:00:00,SLP,01:00:00,Wisconsin_7Day,02:30,00:00,17:00,,,");
        event.ProcessEvent("01/14/2016 01:00:00,OFF,02:00:00,Wisconsin_7Day,02:30,00:00,17:00,,,");
        event.ProcessEvent("01/14/2016 03:00:00,ON,01:00:00,Wisconsin_7Day,02:30,00:00,16:00,,,");
        event.ProcessEvent("01/14/2016 04:00:00,DRV,05:00:00,Wisconsin_7Day,00:00,00:00,11:00,,,");
        event.ProcessEvent("01/14/2016 09:00:00,ON,01:00:00,Wisconsin_7Day,00:00,00:00,10:00,,,");
        event.ProcessEvent("01/14/2016 10:00:00,SLP,01:30:00,Wisconsin_7Day,00:00,00:00,10:00,,,");
        event.ProcessEvent("01/14/2016 11:30:00,OFF,00:30:00,Wisconsin_7Day,00:00,00:00,10:00,,,");
        event.ProcessEvent("01/14/2016 12:00:00,ON,01:00:00,Wisconsin_7Day,00:00,00:00,09:00,,,");
        event.ProcessEvent("01/14/2016 13:00:00,DRV,03:00:00,Wisconsin_7Day,00:00,00:00,06:00,,,");
        event.ProcessEvent("01/14/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,00:00,00:00,05:00,,,");
        event.ProcessEvent("01/14/2016 17:00:00,SLP,07:00:00,Wisconsin_7Day,00:00,00:00,05:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/15/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/15/2016 00:00:00,SLP,01:00:00,Wisconsin_7Day,09:00,11:00,05:00,,,");
        event.ProcessEvent("01/15/2016 01:00:00,ON,00:30:00,Wisconsin_7Day,09:00,10:30,04:30,,,");
        event.ProcessEvent("01/15/2016 01:30:00,DRV,04:00:00,Wisconsin_7Day,05:00,06:30,00:30,,,");
        event.ProcessEvent("01/15/2016 05:30:00,ON,00:30:00,Wisconsin_7Day,05:00,06:00,00:00,,,");
        event.ProcessEvent("01/15/2016 06:00:00,OFF,00:30:00,Wisconsin_7Day,05:00,05:30,00:00,,,");
        event.ProcessEvent("01/15/2016 06:30:00,SLP,02:30:00,Wisconsin_7Day,08:00,08:00,00:00,,,");
        event.ProcessEvent("01/15/2016 09:00:00,DRV,02:00:00,Wisconsin_7Day,06:00,06:00,00:00,,,");
        event.ProcessEvent("01/15/2016 11:00:00,OFF,02:00:00,Wisconsin_7Day,06:00,04:00,00:00,,,");
        event.ProcessEvent("01/15/2016 13:00:00,ON,00:30:00,Wisconsin_7Day,06:00,03:30,00:00,,,");
        event.ProcessEvent("01/15/2016 13:30:00,DRV,01:30:00,Wisconsin_7Day,04:30,02:00,00:00,,,");
        event.ProcessEvent("01/15/2016 15:00:00,SLP,08:00:00,Wisconsin_7Day,10:30,14:00,00:00,,,");
        event.ProcessEvent("01/15/2016 23:00:00,ON,01:00:00,Wisconsin_7Day,10:30,13:00,00:00,,,");


        logProperties.setLogDate(new Date(Date.parse("01/18/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/18/2016 00:00:00,OFF,03:00:00,Wisconsin_7Day,12:00,16:00,22:00,,,");
        event.ProcessEvent("01/18/2016 03:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,21:00,,,");
        event.ProcessEvent("01/18/2016 04:00:00,DRV,03:00:00,Wisconsin_7Day,09:00,12:00,18:00,,,");
        event.ProcessEvent("01/18/2016 07:00:00,SLP,08:00:00,Wisconsin_7Day,09:00,12:00,18:00,,,");
        event.ProcessEvent("01/18/2016 15:00:00,ON,01:00:00,Wisconsin_7Day,09:00,11:00,17:00,,,");
        event.ProcessEvent("01/18/2016 16:00:00,DRV,05:00:00,Wisconsin_7Day,04:00,06:00,12:00,,,");
        event.ProcessEvent("01/18/2016 21:00:00,SLP,02:00:00,Wisconsin_7Day,07:00,08:00,12:00,,,");
        event.ProcessEvent("01/18/2016 23:00:00,OFF,00:30:00,Wisconsin_7Day,07:00,07:30,12:00,,,");
        event.ProcessEvent("01/18/2016 23:30:00,ON,00:30:00,Wisconsin_7Day,07:00,07:00,11:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/19/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/19/2016 00:00:00,ON,06:00:00,Wisconsin_7Day,07:00,01:00,18:30,,,");
        event.ProcessEvent("01/19/2016 06:00:00,SLP,08:00:00,Wisconsin_7Day,12:00,09:30,18:30,,,");
        event.ProcessEvent("01/19/2016 14:00:00,ON,01:30:00,Wisconsin_7Day,12:00,08:00,17:00,,,");
        event.ProcessEvent("01/19/2016 15:30:00,DRV,06:00:00,Wisconsin_7Day,06:00,02:00,11:00,,,");
        event.ProcessEvent("01/19/2016 21:30:00,ON,00:30:00,Wisconsin_7Day,06:00,01:30,10:30,,,");
        event.ProcessEvent("01/19/2016 22:00:00,OFF,02:00:00,Wisconsin_7Day,06:00,06:00,10:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/21/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/21/2016 00:00:00,OFF,06:00:00,Wisconsin_7Day,12:00,16:00,35:30,,,");
        event.ProcessEvent("01/21/2016 06:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,34:30,,,");
        event.ProcessEvent("01/21/2016 07:00:00,DRV,08:00:00,Wisconsin_7Day,04:00,07:00,26:30,,,");
        event.ProcessEvent("01/21/2016 15:00:00,ON,01:00:00,Wisconsin_7Day,04:00,06:00,25:30,,,");
        event.ProcessEvent("01/21/2016 16:00:00,OFF,08:00:00,Wisconsin_7Day,04:00,00:00,25:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/22/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/22/2016 00:00:00,OFF,08:00:00,Wisconsin_7Day,12:00,16:00,35:30,,,");
        event.ProcessEvent("01/22/2016 08:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,34:30,,,");
        event.ProcessEvent("01/22/2016 09:00:00,DRV,07:00:00,Wisconsin_7Day,05:00,08:00,27:30,,,");
        event.ProcessEvent("01/22/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,05:00,07:00,26:30,,,");
        event.ProcessEvent("01/22/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,05:00,00:00,26:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/23/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/23/2016 00:00:00,OFF,08:00:00,Wisconsin_7Day,12:00,16:00,26:30,,,");
        event.ProcessEvent("01/23/2016 08:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,25:30,,,");
        event.ProcessEvent("01/23/2016 09:00:00,DRV,07:00:00,Wisconsin_7Day,05:00,08:00,18:30,,,");
        event.ProcessEvent("01/23/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,05:00,07:00,17:30,,,");
        event.ProcessEvent("01/23/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,05:00,00:00,17:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/24/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/24/2016 00:00:00,OFF,07:00:00,Wisconsin_7Day,12:00,16:00,17:30,,,");
        event.ProcessEvent("01/24/2016 07:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,16:30,,,");
        event.ProcessEvent("01/24/2016 08:00:00,DRV,08:00:00,Wisconsin_7Day,04:00,07:00,08:30,,,");
        event.ProcessEvent("01/24/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,04:00,06:00,07:30,,,");
        event.ProcessEvent("01/24/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,04:00,00:00,07:30,,,");

        logProperties.setLogDate(new Date(Date.parse("01/25/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/25/2016 00:00:00,OFF,08:00:00,Wisconsin_7Day,12:00,16:00,18:00,,,");
        event.ProcessEvent("01/25/2016 08:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,17:00,,,");
        event.ProcessEvent("01/25/2016 09:00:00,DRV,06:30:00,Wisconsin_7Day,05:30,08:30,10:30,,,");
        event.ProcessEvent("01/25/2016 15:30:00,ON,01:30:00,Wisconsin_7Day,05:30,07:00,09:00,,,");
        event.ProcessEvent("01/25/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,05:30,00:00,09:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/26/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/26/2016 00:00:00,OFF,08:00:00,Wisconsin_7Day,12:00,16:00,23:00,,,");
        event.ProcessEvent("01/26/2016 08:00:00,DRV,08:00:00,Wisconsin_7Day,04:00,08:00,15:00,,,");
        event.ProcessEvent("01/26/2016 16:00:00,ON,01:00:00,Wisconsin_7Day,04:00,07:00,14:00,,,");
        event.ProcessEvent("01/26/2016 17:00:00,OFF,07:00:00,Wisconsin_7Day,04:00,00:00,14:00,,,");

        logProperties.setLogDate(new Date(Date.parse("01/27/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("01/27/2016 00:00:00,OFF,08:00:00,Wisconsin_7Day,12:00,16:00,14:00,,,");
        event.ProcessEvent("01/27/2016 08:00:00,ON,01:00:00,Wisconsin_7Day,12:00,15:00,13:00,,,");
        event.ProcessEvent("01/27/2016 09:00:00,DRV,08:00:00,Wisconsin_7Day,04:00,07:00,05:00,,,");
        event.ProcessEvent("01/27/2016 17:00:00,ON,01:00:00,Wisconsin_7Day,04:00,06:00,04:00,,,");
        event.ProcessEvent("01/27/2016 18:00:00,OFF,06:00:00,Wisconsin_7Day,04:00,00:00,04:00,,,");
    }

}
