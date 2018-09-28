package com.jjkeller.kmbapi.common;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ief5781 on 1/24/17.
 */

public class WatchdogTimer {
    private long interval;
    private TimeUnit unit;
    private Runnable onTimeout;

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture schedule;

    public WatchdogTimer(long interval, TimeUnit unit, Runnable onTimeout) {
        this.interval = interval;
        this.unit = unit;
        this.onTimeout = onTimeout;
    }

    public void start() {
        if(!isExecutionCancellable()) {
            schedule = executor.schedule(onTimeout, interval, unit);
        }
    }

    public void stop() {
        if(isExecutionCancellable()) {
            schedule.cancel(false);
        }
    }

    public void reset() {
        stop();
        start();
    }

    private boolean isExecutionCancellable() {
        return schedule != null && !schedule.isCancelled() && !schedule.isDone();
    }
}
