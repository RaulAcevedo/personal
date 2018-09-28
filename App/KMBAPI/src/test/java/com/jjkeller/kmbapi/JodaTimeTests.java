package com.jjkeller.kmbapi;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jld5296 on 2/1/17.
 */
@RunWith(JUnit4.class)
public class JodaTimeTests {
    @Test
    public void test_startOfLogDate(){
        Date date = new Date(10000000L);
        TimeZone tz = TimeZone.getTimeZone("America/Pacific");

        Date oldResult = oldMethod(date, tz);
        Date newResult = newMethod(date, tz);
//TODO : Fix This
        //Assert.assertEquals(oldResult, newResult);
    }

    private Date oldMethod(Date date, TimeZone tz){
        // log date needs the time component removed
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date logDate = cal.getTime();

        return logDate;
    }

    private Date newMethod(Date date, TimeZone tz){
        return new DateTime(date, DateTimeZone.forTimeZone(tz)).toLocalDate().toDate();
    }
}
