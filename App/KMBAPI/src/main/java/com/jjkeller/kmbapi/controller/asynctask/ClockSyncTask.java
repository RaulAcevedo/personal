package com.jjkeller.kmbapi.controller.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jjkeller.kmbapi.controller.EOBR.EobrReader;

/**
 * Created by T000684 on 6/9/2017.
 */
public class ClockSyncTask extends AsyncTask<String, Void, Void> {


    private EobrReader eobrReader;
    private Context ctx;

    public ClockSyncTask(EobrReader reader, Context c){
        eobrReader = reader;
        ctx = c;
    }

    protected Void doInBackground(String... urls) {
        try {
            if(eobrReader.getCurrentConnectionState() != EobrReader.ConnectionState.OFFLINE) {
                eobrReader.PerformClockSynchronization(ctx);
            }
        } catch (Exception e) {
            Log.e("TimeSync", "Error in async time sync ", e);
        }
        return null;
    }

    protected void onPostExecute(Void v) {

    }
}