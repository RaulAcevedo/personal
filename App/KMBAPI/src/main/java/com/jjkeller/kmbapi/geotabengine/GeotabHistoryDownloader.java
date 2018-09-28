package com.jjkeller.kmbapi.geotabengine;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rjm6250 on 2/28/17.
 */

public class GeotabHistoryDownloader {

    private Context _context;
    private int _delayTime;
    private boolean _repeatDownload;
    private String _eobrSerialNumber;

    public GeotabHistoryDownloader(Context context, AppSettings appSettings, String eobrSerialNumber){
        this._context = context;
        this._delayTime = appSettings.getDownloadHistoryGeoTabMS();
        this._repeatDownload = appSettings.getGeotabHistoryRepeatDownload();
        this._eobrSerialNumber = eobrSerialNumber;
    }

    public void download() {
        downloadHistoryInNewThread();
    }

    public void startTimer(){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new GeotabHistoryRunnable(_repeatDownload, scheduledThreadPoolExecutor), _delayTime, _delayTime, TimeUnit.MILLISECONDS);
    }

    private class GeotabHistoryRunnable implements Runnable {
        private ScheduledThreadPoolExecutor _scheduledThreadPoolExecutor;
        private boolean _repeatDownload;


        public GeotabHistoryRunnable(boolean repeatDownload, ScheduledThreadPoolExecutor scheduledThreadPoolExecutor){
            _repeatDownload = repeatDownload;
            _scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        }

        @Override
        public void run() {
            try {
                downloadHistory(false);
            } catch (KmbApplicationException e) {
                Log.e("GeoTab", e.getMessage(), e);
            }

            if(_repeatDownload == false){
                _scheduledThreadPoolExecutor.shutdown();
            }
        }
    }

    private void downloadHistoryInNewThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    downloadHistory(false);
                } catch (KmbApplicationException e) {
                    Log.e("GeoTab", e.getMessage(), e);
                }
            }
        });

        thread.start();
    }

    public void downloadHistory(boolean isReviewed) throws KmbApplicationException {
        if(_eobrSerialNumber != null) {
            GeotabController geotabController = new GeotabController(_context);

            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
                geotabController.DownloadUnidentifiedELDEventsForCurrentDevice((_eobrSerialNumber), isReviewed);
            else
                geotabController.DownloadUnassignedDrivingPeriodsForCurrentLog(_eobrSerialNumber);
        }
    }
}