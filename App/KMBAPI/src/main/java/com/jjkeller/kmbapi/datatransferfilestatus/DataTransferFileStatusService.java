package com.jjkeller.kmbapi.datatransferfilestatus;

import com.jjkeller.kmbapi.controller.RoadsideInspectionController;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class DataTransferFileStatusService extends Service {
    private final RoadsideInspectionController roadsideInspectionController = new RoadsideInspectionController(this);
    private static final int TIMER_LENGTH_MILLIS = 120 * 1000;
    private Timer timer  = null;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startImmediately = false;
        if (intent != null && intent.getExtras() != null){
            startImmediately = intent.getBooleanExtra("startImmediately", false);
        }

        if (startImmediately) {
            timer.scheduleAtFixedRate(new DataTransferFileStatusTimerTask(), 0, TIMER_LENGTH_MILLIS);
        } else {
            timer.scheduleAtFixedRate(new DataTransferFileStatusTimerTask(), TIMER_LENGTH_MILLIS, TIMER_LENGTH_MILLIS);
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (timer != null) {
            timer.cancel();
        } else {
            timer = new Timer();
        }
    }

    class DataTransferFileStatusTimerTask extends TimerTask {
        @Override
        public void run() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isFinished = roadsideInspectionController.PollEmailDataTransferStatus();
                    if (isFinished){
                        timer.cancel();
                        stopSelf();
                    }
                }
            });

            t.start();
        }
    }
}
