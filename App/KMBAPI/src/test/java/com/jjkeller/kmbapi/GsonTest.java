package com.jjkeller.kmbapi;

import com.google.gson.Gson;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by T000682 on 4/26/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class GsonTest {

    @Before
    public void setup(){
        User user = new User();

        LoginCredentials credentials = new LoginCredentials();
        credentials.setEmployeeId("My super coll employee");

        user.setCredentials(credentials);

        GlobalState.getInstance().setCurrentUser(user);
    }

    @Test
    public void testGson(){
        EmployeeLogEldEvent toJsonEvent = new EmployeeLogEldEvent();
        EmployeeLogEldEvent toJsonEvent2 = new EmployeeLogEldEvent();

        toJsonEvent.setRuleSet(new RuleSetTypeEnum(RuleSetTypeEnum.ALASKA_8DAY));
        toJsonEvent.setEventDateTime(new DateTime(2017, 4, 4, 11, 12, 15).toDate());

        toJsonEvent2.setRuleSet(new RuleSetTypeEnum(RuleSetTypeEnum.ALASKA_8DAY));
        toJsonEvent2.setEventDateTime(new DateTime(2017, 4, 4, 13, 13, 12).toDate());

        Gson gson = JsonUtil.getGson();

        String json = gson.toJson(toJsonEvent);
        EmployeeLogEldEvent fromJsonEvent = gson.fromJson(json, EmployeeLogEldEvent.class);

        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.ALASKA_8DAY), fromJsonEvent.getRulesetType());
        Assert.assertEquals(new DateTime(2017, 4, 4, 11, 12, 15).toDate(), fromJsonEvent.getEventDateTime());


        List<EmployeeLogEldEvent> eventList = new LinkedList<>();
        eventList.add(toJsonEvent);
        eventList.add(toJsonEvent2);


        String jsonArray = gson.toJson(eventList);
        List<EmployeeLogEldEvent> fromJsonArray = gson.fromJson(jsonArray, JsonUtil.TYPE_LIST_OF_EMPLOYEE_LOG_ELD_EVENT);

        Assert.assertEquals(2, fromJsonArray.size());
        Assert.assertEquals(new DateTime(2017, 4, 4, 11, 12, 15).toDate(), fromJsonArray.get(0).getEventDateTime());
        Assert.assertEquals(new DateTime(2017, 4, 4, 13, 13, 12).toDate(), fromJsonArray.get(1).getEventDateTime());
    }
}
