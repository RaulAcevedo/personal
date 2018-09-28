package com.jjkeller.kmbapi.proxydata;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;

import org.joda.time.Duration;

/**
 * Duty Summary
 *
 * Data object to hold how many hours used, available, regulation, etc for a particular duty.
 * This is to replace passing a bundle around.
 *
 * Created by Charles Stebbins on 4/11/2017.
 */

public class DutySummary {

    private int totalHours;
    private long usedMilliseconds;
    private long availableMilliseconds;
    private String regulationCode;
    private boolean shortHaulAvailable;

    public DutySummary(){
        this.regulationCode = "";
    }

    public int getAllowedHours() {
        return totalHours;
    }

    public long getUsedMilliseconds() {
        return usedMilliseconds;
    }

    public long getAvailableMilliseconds() {
        return availableMilliseconds;
    }


    public String getRegulationCode() {
        return regulationCode;
    }

    public boolean isShortHaulAvailable() {
        return shortHaulAvailable;
    }

    /**
     * Create from the legacy bundle.
     *
     * @return DutySummary created from a bundle.
     */
    public static DutySummary createFromBundle(Bundle dutySummaryBundle, Context context){

        DutySummary returnDutySummary = new DutySummary();

        if (dutySummaryBundle != null) {
            returnDutySummary.totalHours = dutySummaryBundle.getInt(context.getString(R.string.summary_allowed), 0);
            returnDutySummary.usedMilliseconds = dutySummaryBundle.getLong(context.getString(R.string.summary_used), 0);
            returnDutySummary.availableMilliseconds = dutySummaryBundle.getLong(context.getString(R.string.summary_avail), 0);
            returnDutySummary.regulationCode = dutySummaryBundle.getString(context.getString(R.string.summary_regsection), "");
            returnDutySummary.shortHaulAvailable = dutySummaryBundle.getBoolean(context.getString(R.string.summary_shorthaulavail), false);
        }

        return returnDutySummary;
    }
}
