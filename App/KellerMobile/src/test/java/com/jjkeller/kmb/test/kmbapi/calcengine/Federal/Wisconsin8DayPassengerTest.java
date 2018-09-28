package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(KMBRoboElectricTestRunner.class)
public class Wisconsin8DayPassengerTest {

    @Test
    public void test_Standard() {

        ProcessEventHelper event = new ProcessEventHelper(RuleSetTypeEnum.Wisconsin_8Day, RulesetFactory.ForWisconsin8DayPassenger());
        LogProperties logProperties = new LogProperties();

        logProperties.setLogDate(new Date(Date.parse("04/02/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/02/2016 00:00:00,OFF,10:00:00,Wisconsin_8Day,12:00,16:00,80:00:00,,,");
        event.ProcessEvent("04/02/2016 10:00:00,ON,00:30:00,Wisconsin_8Day,12:00,15:30,79:30:00,,,");
        event.ProcessEvent("04/02/2016 10:30:00,DRV,06:00:00,Wisconsin_8Day,06:00,09:30,73:30:00,,,");
        event.ProcessEvent("04/02/2016 16:30:00,ON,01:30:00,Wisconsin_8Day,06:00,08:00,72:00:00,,,");
        event.ProcessEvent("04/02/2016 18:00:00,DRV,05:00:00,Wisconsin_8Day,01:00,03:00,67:00:00,,,");
        event.ProcessEvent("04/02/2016 23:00:00,ON,00:30:00,Wisconsin_8Day,01:00,02:30,66:30:00,,,");
        event.ProcessEvent("04/02/2016 23:30:00,OFF,00:30:00,Wisconsin_8Day,01:00,02:00,66:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/03/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/03/2016 00:00:00,OFF,09:30:00,Wisconsin_8Day,12:00,16:00,66:30:00,,,");
        event.ProcessEvent("04/03/2016 09:30:00,ON,02:00:00,Wisconsin_8Day,12:00,14:00,64:30:00,,,");
        event.ProcessEvent("04/03/2016 11:30:00,DRV,06:30:00,Wisconsin_8Day,05:30,07:30,58:00:00,,,");
        event.ProcessEvent("04/03/2016 18:00:00,ON,01:00:00,Wisconsin_8Day,05:30,06:30,57:00:00,,,");
        event.ProcessEvent("04/03/2016 19:00:00,OFF,05:00:00,Wisconsin_8Day,05:30,01:30,57:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/04/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/04/2016 00:00:00,OFF,05:00:00,Wisconsin_8Day,12:00,16:00,57:00:00,,,");
        event.ProcessEvent("04/04/2016 05:00:00,ON,01:30:00,Wisconsin_8Day,12:00,14:30,55:30:00,,,");
        event.ProcessEvent("04/04/2016 06:30:00,DRV,02:30:00,Wisconsin_8Day,09:30,12:00,53:00:00,,,");
        event.ProcessEvent("04/04/2016 09:00:00,ON,01:30:00,Wisconsin_8Day,09:30,10:30,51:30:00,,,");
        event.ProcessEvent("04/04/2016 10:30:00,DRV,01:30:00,Wisconsin_8Day,08:00,09:00,50:00:00,,,");
        event.ProcessEvent("04/04/2016 12:00:00,OFF,03:00:00,Wisconsin_8Day,08:00,06:00,50:00:00,,,");
        event.ProcessEvent("04/04/2016 15:00:00,ON,00:30:00,Wisconsin_8Day,08:00,05:30,49:30:00,,,");
        event.ProcessEvent("04/04/2016 15:30:00,DRV,04:00:00,Wisconsin_8Day,04:00,01:30,45:30:00,,,");
        event.ProcessEvent("04/04/2016 19:30:00,ON,00:30:00,Wisconsin_8Day,04:00,01:00,45:00:00,,,");
        event.ProcessEvent("04/04/2016 20:00:00,OFF,04:00:00,Wisconsin_8Day,04:00,00:00,45:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/05/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/05/2016 00:00:00,OFF,04:00:00,Wisconsin_8Day,04:00,00:00,45:00:00,,,");
        event.ProcessEvent("04/05/2016 04:00:00,SLP,03:00:00,Wisconsin_8Day,12:00,16:00,45:00:00,,,");
        event.ProcessEvent("04/05/2016 07:00:00,ON,00:30:00,Wisconsin_8Day,12:00,15:30,44:30:00,,,");
        event.ProcessEvent("04/05/2016 07:30:00,DRV,03:30:00,Wisconsin_8Day,08:30,12:00,41:00:00,,,");
        event.ProcessEvent("04/05/2016 11:00:00,SLP,02:00:00,Wisconsin_8Day,08:30,10:00,41:00:00,,,");
        event.ProcessEvent("04/05/2016 13:00:00,DRV,08:00:00,Wisconsin_8Day,00:30,02:00,33:00:00,,,");
        event.ProcessEvent("04/05/2016 21:00:00,ON,00:30:00,Wisconsin_8Day,00:30,01:30,32:30:00,,,");
        event.ProcessEvent("04/05/2016 21:30:00,OFF,02:30:00,Wisconsin_8Day,00:30,00:00,32:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/06/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/06/2016 00:00:00,OFF,07:30:00,Wisconsin_8Day,12:00,16:00,32:30:00,,,");
        event.ProcessEvent("04/06/2016 07:30:00,ON,00:30:00,Wisconsin_8Day,12:00,15:30,32:00:00,,,");
        event.ProcessEvent("04/06/2016 08:00:00,DRV,08:00:00,Wisconsin_8Day,04:00,07:30,24:00:00,,,");
        event.ProcessEvent("04/06/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,04:00,06:30,23:00:00,,,");
        event.ProcessEvent("04/06/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,04:00,00:00,23:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/07/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/07/2016 00:00:00,OFF,05:00:00,Wisconsin_8Day,12:00,16:00,23:00:00,,,");
        event.ProcessEvent("04/07/2016 05:00:00,ON,03:00:00,Wisconsin_8Day,12:00,13:00,20:00:00,,,");
        event.ProcessEvent("04/07/2016 08:00:00,DRV,02:00:00,Wisconsin_8Day,10:00,11:00,18:00:00,,,");
        event.ProcessEvent("04/07/2016 10:00:00,OFF,03:00:00,Wisconsin_8Day,10:00,08:00,18:00:00,,,");
        event.ProcessEvent("04/07/2016 13:00:00,DRV,07:00:00,Wisconsin_8Day,03:00,01:00,11:00:00,,,");
        event.ProcessEvent("04/07/2016 20:00:00,ON,01:00:00,Wisconsin_8Day,03:00,00:00,10:00:00,,,");
        event.ProcessEvent("04/07/2016 21:00:00,OFF,03:00:00,Wisconsin_8Day,03:00,00:00,10:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/09/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/09/2016 00:00:00,OFF,07:00:00,Wisconsin_8Day,12:00,16:00,10:00:00,,,");
        event.ProcessEvent("04/09/2016 07:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,09:00:00,,,");
        event.ProcessEvent("04/09/2016 08:00:00,DRV,05:00:00,Wisconsin_8Day,07:00,10:00,04:00:00,,,");
        event.ProcessEvent("04/09/2016 13:00:00,OFF,02:00:00,Wisconsin_8Day,07:00,08:00,04:00:00,,,");
        event.ProcessEvent("04/09/2016 15:00:00,ON,02:00:00,Wisconsin_8Day,07:00,06:00,02:00:00,,,");
        event.ProcessEvent("04/09/2016 17:00:00,DRV,05:30:00,Wisconsin_8Day,01:30,00:30,00:00:00,,,");
        event.ProcessEvent("04/09/2016 22:30:00,ON,00:30:00,Wisconsin_8Day,01:30,00:00,00:00:00,,,");
        event.ProcessEvent("04/09/2016 23:00:00,OFF,01:00:00,Wisconsin_8Day,01:30,00:00,00:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/11/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/11/2016 00:00:00,OFF,03:00:00,Wisconsin_8Day,12:00,16:00,19:00:00,,,");
        event.ProcessEvent("04/11/2016 03:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,18:00:00,,,");
        event.ProcessEvent("04/11/2016 04:00:00,DRV,04:00:00,Wisconsin_8Day,08:00,11:00,14:00:00,,,");
        event.ProcessEvent("04/11/2016 08:00:00,ON,01:00:00,Wisconsin_8Day,08:00,10:00,13:00:00,,,");
        event.ProcessEvent("04/11/2016 09:00:00,SLP,02:00:00,Wisconsin_8Day,08:00,08:00,13:00:00,,,");
        event.ProcessEvent("04/11/2016 11:00:00,ON,01:00:00,Wisconsin_8Day,08:00,07:00,12:00:00,,,");
        event.ProcessEvent("04/11/2016 12:00:00,DRV,05:00:00,Wisconsin_8Day,03:00,02:00,07:00:00,,,");
        event.ProcessEvent("04/11/2016 17:00:00,ON,01:00:00,Wisconsin_8Day,03:00,01:00,06:00:00,,,");
        event.ProcessEvent("04/11/2016 18:00:00,SLP,06:00:00,Wisconsin_8Day,03:00,00:00,06:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/12/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/12/2016 00:00:00,SLP,02:00:00,Wisconsin_8Day,07:00,09:00,18:00:00,,,");
        event.ProcessEvent("04/12/2016 02:00:00,ON,01:00:00,Wisconsin_8Day,07:00,08:00,17:00:00,,,");
        event.ProcessEvent("04/12/2016 03:00:00,DRV,06:00:00,Wisconsin_8Day,01:00,02:00,11:00:00,,,");
        event.ProcessEvent("04/12/2016 09:00:00,ON,01:00:00,Wisconsin_8Day,01:00,01:00,10:00:00,,,");
        event.ProcessEvent("04/12/2016 10:00:00,SLP,02:00:00,Wisconsin_8Day,06:00,06:00,10:00:00,,,");
        event.ProcessEvent("04/12/2016 12:00:00,ON,01:00:00,Wisconsin_8Day,06:00,05:00,09:00:00,,,");
        event.ProcessEvent("04/12/2016 13:00:00,DRV,03:00:00,Wisconsin_8Day,03:00,02:00,06:00:00,,,");
        event.ProcessEvent("04/12/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,03:00,01:00,05:00:00,,,");
        event.ProcessEvent("04/12/2016 17:00:00,SLP,07:00:00,Wisconsin_8Day,03:00,00:00,05:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/13/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/13/2016 00:00:00,SLP,02:00:00,Wisconsin_8Day,09:00,11:00,17:30:00,,,");
        event.ProcessEvent("04/13/2016 02:00:00,ON,01:00:00,Wisconsin_8Day,09:00,10:00,16:30:00,,,");
        event.ProcessEvent("04/13/2016 03:00:00,DRV,02:00:00,Wisconsin_8Day,07:00,08:00,14:30:00,,,");
        event.ProcessEvent("04/13/2016 05:00:00,OFF,01:00:00,Wisconsin_8Day,07:00,07:00,14:30:00,,,");
        event.ProcessEvent("04/13/2016 06:00:00,DRV,04:00:00,Wisconsin_8Day,03:00,03:00,10:30:00,,,");
        event.ProcessEvent("04/13/2016 10:00:00,ON,01:00:00,Wisconsin_8Day,03:00,02:00,09:30:00,,,");
        event.ProcessEvent("04/13/2016 11:00:00,OFF,00:15:00,Wisconsin_8Day,03:00,01:45,09:30:00,,,");
        event.ProcessEvent("04/13/2016 11:15:00,SLP,01:45:00,Wisconsin_8Day,06:00,05:00,09:30:00,,,");
        event.ProcessEvent("04/13/2016 13:00:00,ON,00:30:00,Wisconsin_8Day,06:00,04:30,09:00:00,,,");
        event.ProcessEvent("04/13/2016 13:30:00,DRV,03:30:00,Wisconsin_8Day,02:30,01:00,05:30:00,,,");
        event.ProcessEvent("04/13/2016 17:00:00,ON,01:00:00,Wisconsin_8Day,02:30,00:00,04:30:00,,,");
        event.ProcessEvent("04/13/2016 18:00:00,SLP,06:00:00,Wisconsin_8Day,02:30,00:00,04:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/14/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/14/2016 00:00:00,SLP,01:00:00,Wisconsin_8Day,02:30,00:00,14:00:00,,,");
        event.ProcessEvent("04/14/2016 01:00:00,OFF,02:00:00,Wisconsin_8Day,02:30,00:00,14:00:00,,,");
        event.ProcessEvent("04/14/2016 03:00:00,ON,01:00:00,Wisconsin_8Day,02:30,00:00,13:00:00,,,");
        event.ProcessEvent("04/14/2016 04:00:00,DRV,05:00:00,Wisconsin_8Day,00:00,00:00,08:00:00,,,");
        event.ProcessEvent("04/14/2016 09:00:00,ON,01:00:00,Wisconsin_8Day,00:00,00:00,07:00:00,,,");
        event.ProcessEvent("04/14/2016 10:00:00,SLP,01:30:00,Wisconsin_8Day,00:00,00:00,07:00:00,,,");
        event.ProcessEvent("04/14/2016 11:30:00,OFF,00:30:00,Wisconsin_8Day,00:00,00:00,07:00:00,,,");
        event.ProcessEvent("04/14/2016 12:00:00,ON,01:00:00,Wisconsin_8Day,00:00,00:00,06:00:00,,,");
        event.ProcessEvent("04/14/2016 13:00:00,DRV,03:00:00,Wisconsin_8Day,00:00,00:00,03:00:00,,,");
        event.ProcessEvent("04/14/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,00:00,00:00,02:00:00,,,");
        event.ProcessEvent("04/14/2016 17:00:00,SLP,07:00:00,Wisconsin_8Day,00:00,00:00,02:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/15/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/15/2016 00:00:00,SLP,01:00:00,Wisconsin_8Day,09:00,11:00,15:00:00,,,");
        event.ProcessEvent("04/15/2016 01:00:00,ON,00:30:00,Wisconsin_8Day,09:00,10:30,14:30:00,,,");
        event.ProcessEvent("04/15/2016 01:30:00,DRV,04:00:00,Wisconsin_8Day,05:00,06:30,10:30:00,,,");
        event.ProcessEvent("04/15/2016 05:30:00,ON,00:30:00,Wisconsin_8Day,05:00,06:00,10:00:00,,,");
        event.ProcessEvent("04/15/2016 06:00:00,OFF,00:30:00,Wisconsin_8Day,05:00,05:30,10:00:00,,,");
        event.ProcessEvent("04/15/2016 06:30:00,SLP,02:30:00,Wisconsin_8Day,08:00,08:00,10:00:00,,,");
        event.ProcessEvent("04/15/2016 09:00:00,DRV,02:00:00,Wisconsin_8Day,06:00,06:00,08:00:00,,,");
        event.ProcessEvent("04/15/2016 11:00:00,OFF,02:00:00,Wisconsin_8Day,06:00,04:00,08:00:00,,,");
        event.ProcessEvent("04/15/2016 13:00:00,ON,00:30:00,Wisconsin_8Day,06:00,03:30,07:30:00,,,");
        event.ProcessEvent("04/15/2016 13:30:00,DRV,01:30:00,Wisconsin_8Day,04:30,02:00,06:00:00,,,");
        event.ProcessEvent("04/15/2016 15:00:00,SLP,08:00:00,Wisconsin_8Day,10:30,14:00,06:00:00,,,");
        event.ProcessEvent("04/15/2016 23:00:00,ON,01:00:00,Wisconsin_8Day,10:30,13:00,05:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/18/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/18/2016 00:00:00,OFF,03:00:00,Wisconsin_8Day,12:00,16:00,19:00:00,,,");
        event.ProcessEvent("04/18/2016 03:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,18:00:00,,,");
        event.ProcessEvent("04/18/2016 04:00:00,DRV,03:00:00,Wisconsin_8Day,09:00,12:00,15:00:00,,,");
        event.ProcessEvent("04/18/2016 07:00:00,SLP,08:00:00,Wisconsin_8Day,09:00,12:00,15:00:00,,,");
        event.ProcessEvent("04/18/2016 15:00:00,ON,01:00:00,Wisconsin_8Day,09:00,11:00,14:00:00,,,");
        event.ProcessEvent("04/18/2016 16:00:00,DRV,05:00:00,Wisconsin_8Day,04:00,06:00,09:00:00,,,");
        event.ProcessEvent("04/18/2016 21:00:00,SLP,02:00:00,Wisconsin_8Day,07:00,08:00,09:00:00,,,");
        event.ProcessEvent("04/18/2016 23:00:00,OFF,00:30:00,Wisconsin_8Day,07:00,07:30,09:00:00,,,");
        event.ProcessEvent("04/18/2016 23:30:00,ON,00:30:00,Wisconsin_8Day,07:00,07:00,08:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/19/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/19/2016 00:00:00,ON,06:00:00,Wisconsin_8Day,07:00,01:00,15:30:00,,,");
        event.ProcessEvent("04/19/2016 06:00:00,SLP,08:00:00,Wisconsin_8Day,12:00,09:30,15:30:00,,,");
        event.ProcessEvent("04/19/2016 14:00:00,ON,01:30:00,Wisconsin_8Day,12:00,08:00,14:00:00,,,");
        event.ProcessEvent("04/19/2016 15:30:00,DRV,06:00:00,Wisconsin_8Day,06:00,02:00,08:00:00,,,");
        event.ProcessEvent("04/19/2016 21:30:00,ON,00:30:00,Wisconsin_8Day,06:00,01:30,07:30:00,,,");
        event.ProcessEvent("04/19/2016 22:00:00,OFF,02:00:00,Wisconsin_8Day,06:00,06:00,07:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/21/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/21/2016 00:00:00,OFF,06:00:00,Wisconsin_8Day,12:00,16:00,33:30:00,,,");
        event.ProcessEvent("04/21/2016 06:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,32:30:00,,,");
        event.ProcessEvent("04/21/2016 07:00:00,DRV,08:00:00,Wisconsin_8Day,04:00,07:00,24:30:00,,,");
        event.ProcessEvent("04/21/2016 15:00:00,ON,01:00:00,Wisconsin_8Day,04:00,06:00,23:30:00,,,");
        event.ProcessEvent("04/21/2016 16:00:00,OFF,08:00:00,Wisconsin_8Day,04:00,00:00,23:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/22/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/22/2016 00:00:00,OFF,08:00:00,Wisconsin_8Day,12:00,16:00,35:30:00,,,");
        event.ProcessEvent("04/22/2016 08:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,34:30:00,,,");
        event.ProcessEvent("04/22/2016 09:00:00,DRV,07:00:00,Wisconsin_8Day,05:00,08:00,27:30:00,,,");
        event.ProcessEvent("04/22/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,05:00,07:00,26:30:00,,,");
        event.ProcessEvent("04/22/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,05:00,00:00,26:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/23/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/23/2016 00:00:00,OFF,08:00:00,Wisconsin_8Day,12:00,16:00,36:30:00,,,");
        event.ProcessEvent("04/23/2016 08:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,35:30:00,,,");
        event.ProcessEvent("04/23/2016 09:00:00,DRV,07:00:00,Wisconsin_8Day,05:00,08:00,28:30:00,,,");
        event.ProcessEvent("04/23/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,05:00,07:00,27:30:00,,,");
        event.ProcessEvent("04/23/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,05:00,00:00,27:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/24/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/24/2016 00:00:00,OFF,07:00:00,Wisconsin_8Day,12:00,16:00,27:30:00,,,");
        event.ProcessEvent("04/24/2016 07:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,26:30:00,,,");
        event.ProcessEvent("04/24/2016 08:00:00,DRV,08:00:00,Wisconsin_8Day,04:00,07:00,18:30:00,,,");
        event.ProcessEvent("04/24/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,04:00,06:00,17:30:00,,,");
        event.ProcessEvent("04/24/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,04:00,00:00,17:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/25/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/25/2016 00:00:00,OFF,08:00:00,Wisconsin_8Day,12:00,16:00,17:30:00,,,");
        event.ProcessEvent("04/25/2016 08:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,16:30:00,,,");
        event.ProcessEvent("04/25/2016 09:00:00,DRV,06:30:00,Wisconsin_8Day,05:30,08:30,10:00:00,,,");
        event.ProcessEvent("04/25/2016 15:30:00,ON,01:30:00,Wisconsin_8Day,05:30,07:00,08:30:00,,,");
        event.ProcessEvent("04/25/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,05:30,00:00,08:30:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/26/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/26/2016 00:00:00,OFF,08:00:00,Wisconsin_8Day,12:00,16:00,19:00:00,,,");
        event.ProcessEvent("04/26/2016 08:00:00,DRV,08:00:00,Wisconsin_8Day,04:00,08:00,11:00:00,,,");
        event.ProcessEvent("04/26/2016 16:00:00,ON,01:00:00,Wisconsin_8Day,04:00,07:00,10:00:00,,,");
        event.ProcessEvent("04/26/2016 17:00:00,OFF,07:00:00,Wisconsin_8Day,04:00,00:00,10:00:00,,,");

        logProperties.setLogDate(new Date(Date.parse("04/27/2016")));
        event.rulesetEngine.PrepareStartOfLog(logProperties);
        event.ProcessEvent("04/27/2016 00:00:00,OFF,08:00:00,Wisconsin_8Day,12:00,16:00,24:00:00,,,");
        event.ProcessEvent("04/27/2016 08:00:00,ON,01:00:00,Wisconsin_8Day,12:00,15:00,23:00:00,,,");
        event.ProcessEvent("04/27/2016 09:00:00,DRV,08:00:00,Wisconsin_8Day,04:00,07:00,15:00:00,,,");
        event.ProcessEvent("04/27/2016 17:00:00,ON,01:00:00,Wisconsin_8Day,04:00,06:00,14:00:00,,,");
        event.ProcessEvent("04/27/2016 18:00:00,OFF,06:00:00,Wisconsin_8Day,04:00,00:00,14:00:00,,,");

    }
}
