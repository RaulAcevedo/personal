package com.jjkeller.kmbapi.realtime;

import java.util.concurrent.TimeUnit;


public interface ScheduledProcess extends Runnable {

    long getTimeDelay();

    TimeUnit getTimeUnit();

}
