package com.jjkeller.kmbapi.common;

import android.app.Application;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;
import org.junit.After;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 10/4/16.
 */
public abstract class TestBase {

    private final ITimeKeeper timeKeeper;

    public TestBase() {
        LogCat logStub = mock(LogCat.class);
        LogCat.setInstance(logStub);
        doNothing().when(logStub).v(anyString(), anyString());
        doNothing().when(logStub).i(anyString(), anyString());
        doNothing().when(logStub).d(anyString(), anyString());
        doNothing().when(logStub).e(anyString(), anyString());
        doNothing().when(logStub).w(anyString(), anyString());
        doNothing().when(logStub).wtf(anyString(), anyString());

        timeKeeper = mock(ITimeKeeper.class);
        TimeKeeper.setTimeKeeper(timeKeeper);
        when(timeKeeper.now()).thenReturn(new Date());
        when(timeKeeper.getCurrentDateTime()).thenReturn(new DateTime());
    }

    protected void EnableEldMandate(boolean isEnabled){
        IFeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(isEnabled);
        GlobalState.getInstance().setfeatureToggleService(ftService);
    }

    protected Date Now() {
        return TimeKeeper.getInstance().now();
    }

    protected void Now(int year, int month, int day, int hour, int minute, int second, TimeZone tz) {
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(year, month, day, hour, minute, second);
        when(timeKeeper.now()).thenReturn(calendar.getTime());
        when(timeKeeper.getCurrentDateTime()).thenReturn(new DateTime(calendar.getTime()));
    }

    protected void Now(long millSinceEpoc){
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(millSinceEpoc);
        when(timeKeeper.now()).thenReturn(cal.getTime());
        when(timeKeeper.getCurrentDateTime()).thenReturn(new DateTime(cal.getTime()));
    }


    public void closeDatabase(Application app) throws NoSuchFieldException, IllegalAccessException {
        //needed to clean out old data with roboeletric
        //feels hacky... TODO find better way.
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, app);
        persist.open();
        persist.closeDatabase();
    }
}
