package com.jjkeller.kmb.test.kmbapi.controller.ELDMandateController.Utility;

import android.content.Context;
import android.os.AsyncTask;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.jjkeller.kmbapi.controller.dataaccess.ApplicationStateFacade;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.proxydata.ApplicationStateSettings;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

@SuppressWarnings("unused")
@RunWith(AndroidJUnit4.class)
@SmallTest
public class EventSequenceIdGeneratorTest {

    private Context ctx;

    @Before
    public void setUp(){
        ctx = InstrumentationRegistry.getTargetContext();
    }

    private Context getContext(){
        return ctx;
    }

    @Test
    public void testGetNextSequenceNumber_WhenZero_ReturnOne() {
        EventSequenceIdGenerator sequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(getContext()));
        initializePersistedSequenceNumber(0);

        int nextSequenceNumber = sequenceIdGenerator.GetNextSequenceNumber();

        Assert.assertEquals(1, nextSequenceNumber);
    }

    @Test
    public void testGetNextSequenceNumber_WhenCalled_PersistsSequenceNumber() {
        EventSequenceIdGenerator sequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(getContext()));
        ApplicationStateFacade facade = new ApplicationStateFacade(getContext());
        initializePersistedSequenceNumber(0);

        sequenceIdGenerator.GetNextSequenceNumber();

        int persistedValue = facade.Fetch().getEventSequenceId();
        Assert.assertEquals(1, persistedValue);
    }

    @Test
    public void testGetNextSequenceNumber_When65535_ReturnZero() {
        EventSequenceIdGenerator sequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(getContext()));
        initializePersistedSequenceNumber(65535);

        int nextSequenceNumber = sequenceIdGenerator.GetNextSequenceNumber();

        Assert.assertEquals(0, nextSequenceNumber);
    }

    @Test
    public void testGetNextSequenceNumber_DoesNotReturnDuplicates() {
        int iterations = 10;
        final CountDownLatch latch = new CountDownLatch(iterations);
        final ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 500, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        final EventSequenceIdGenerator sequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(getContext()));
        initializePersistedSequenceNumber(0);

        for(int i = 0; i < iterations; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    results.add(sequenceIdGenerator.GetNextSequenceNumber());
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final HashSet<Integer> hashSet = new HashSet<>();
        for(Integer result : results) {
            if(hashSet.contains(result)) {
                fail("Received duplicate sequence number");
            } else {
                hashSet.add(result);
            }
        }
    }

    private void initializePersistedSequenceNumber(int sequenceNumber) {
        ApplicationStateFacade facade = new ApplicationStateFacade(getContext());
        ApplicationStateSettings settings = facade.Fetch();
        if (settings == null) {
            settings = new ApplicationStateSettings();
        }
        settings.setEventSequenceId(sequenceNumber);
        facade.Save(settings);
    }
}
