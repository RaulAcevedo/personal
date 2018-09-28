package com.jjkeller.kmbapi.controller.share;

import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Created by jld5296 on 11/1/16.
 */
public class DateOnlyEqualityComparator {
    private Date date1;
    private Date date2;

    public DateOnlyEqualityComparator(Date date1, Date date2) {
        this.date1 = date1;
        this.date2 = date2;
    }

    public boolean areEqual() {
        LocalDate empLogLogDate = new LocalDate(date2);
        LocalDate localEndingLogDate = new LocalDate(date1);
        return empLogLogDate.equals(localEndingLogDate);
    }

    public static boolean areEqual(Date date1, Date date2) {
        LocalDate empLogLogDate = new LocalDate(date2);
        LocalDate localEndingLogDate = new LocalDate(date1);
        return empLogLogDate.equals(localEndingLogDate);
    }
}
