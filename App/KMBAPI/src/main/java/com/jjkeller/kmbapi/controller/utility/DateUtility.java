package com.jjkeller.kmbapi.controller.utility;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtility {
    private static final String HomeTerminalDateFormat = "MM/dd/yyyy";
    private static final String HomeTerminalShortDateFormat = "MM/dd/yy";
    private static final String HomeTerminalDateTimeFormat = "MM/dd/yyyy HH:mm:ss";
    private static final String HomeTerminalDateTimeFormat12Hour = "MM/dd/yyyy hh:mm aa";
    private static final String HomeTerminalDateTimeFormat12HourWithSeconds = "MM/dd/yyyy hh:mm:ss aa";
    private static final String HomeTerminalDateTimeFormat24Hour = "MM/dd/yyyy HH:mm:ss";
    private static final String getHomeTerminalTime12HourFormat = "hh:mm aa";
    private static final String getHomeTerminalTime12HourFormatWithSeconds = "hh:mm:ss aa";
    private static final String HomeTerminalTime24HourFormat = "HH:mm:ss";
    private static final String HomeTerminalReferenceTimestampFormat = "MM/dd/yy hh:mm:ss a";
    private static final String HomeTerminalDMOSoapDateTimeFormat = "M/d/yy hh:mm:ss a";
    private static final String HomeTerminalDMOSoapDateTimestampUTCFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String HomeTerminalDMOWebApiDateTimeFormatUtcOffset = "MM/dd/yyyy hh:mm:ss a Z";
    private static final String HomeTerminalSqlDateFormat = "yyyy-MM-dd";
    private static final String HomeTerminalSqlDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String DateFormat = "MM/dd/yyyy";
    private static final String DateNoSeparatorsFormat = "MMddyyyy";
    private static final String Time24HourFormat = "HH:mm:ss";
    private static final String DMOSoapDateTimeFormat = "M/d/yy hh:mm:ss a";
    private static final String DMOSoapDateTimestampFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DMOSoapDateTimestampUTCFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String HomeTerminalFullMonthDateFormat = "MMMM dd, yyyy";
    private static final String sqlDateTimeFormatMS = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String UnassignedDrivingPeriodDateFormat = "MM-dd-yyyyhh:mm:ss";

	private static long CLOCK_TIMESTAMP_OFFSET_SECONDS = 0;
	private static boolean HAS_CLOCK_BEEN_SET = false;
    private static TimeZone lastSetTimeZone = TimeZone.getDefault();

    private static SimpleDateFormat getFormatWithTimeZone(String format, TimeZone tz){
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        sdf.setTimeZone(tz);
        return sdf;
    }

    // SimpleDateFormat is not thread-safe, so we can't share instances
	public static SimpleDateFormat getHomeTerminalDateFormat() { return getFormatWithTimeZone(HomeTerminalDateFormat, lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalShortDateFormat() { return getFormatWithTimeZone(HomeTerminalShortDateFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalDateTimeFormat() { return getFormatWithTimeZone(HomeTerminalDateTimeFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalDateTimeFormat12Hour() { return getFormatWithTimeZone(HomeTerminalDateTimeFormat12Hour,  lastSetTimeZone); }
    public static SimpleDateFormat getHomeTerminalDateTimeFormat12HourWithSeconds() { return getFormatWithTimeZone(HomeTerminalDateTimeFormat12HourWithSeconds,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalDateTimeFormat24Hour() { return getFormatWithTimeZone(HomeTerminalDateTimeFormat24Hour,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalTime12HourFormat() { return getFormatWithTimeZone(getHomeTerminalTime12HourFormat, lastSetTimeZone); }
    public static SimpleDateFormat getHomeTerminalTime12HourFormatWithSeconds() { return getFormatWithTimeZone(getHomeTerminalTime12HourFormatWithSeconds, lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalTime24HourFormat() { return getFormatWithTimeZone(HomeTerminalTime24HourFormat, lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalReferenceTimestampFormat() { return getFormatWithTimeZone(HomeTerminalReferenceTimestampFormat, lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalDMOSoapDateTimeFormat() { return getFormatWithTimeZone(HomeTerminalDMOSoapDateTimeFormat, lastSetTimeZone); }
    public static SimpleDateFormat getHomeTerminalDMOSoapDateTimeFormatUTC() { return getFormatWithTimeZone(HomeTerminalDMOSoapDateTimeFormat, TimeZone.getTimeZone("Etc/UTC")); }
	public static SimpleDateFormat getHomeTerminalDMOSoapDateTimestampUTCFormat() { return getFormatWithTimeZone(HomeTerminalDMOSoapDateTimestampUTCFormat, lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalDMOWebApiDateTimeFormatUtcOffset() { return getFormatWithTimeZone(HomeTerminalDMOWebApiDateTimeFormatUtcOffset, lastSetTimeZone);}
	public static SimpleDateFormat getHomeTerminalSqlDateFormat() { return getFormatWithTimeZone(HomeTerminalSqlDateFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalSqlDateTimeFormat() { return getFormatWithTimeZone(HomeTerminalSqlDateTimeFormat,  lastSetTimeZone); }
    public static SimpleDateFormat getHomeTerminalSqlDateTimeFormat(TimeZone timeZone) { return getFormatWithTimeZone(HomeTerminalSqlDateTimeFormat,  timeZone); }
	public static SimpleDateFormat getDateFormat() { return getFormatWithTimeZone(DateFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getDateNoSeparatorsFormat() { return getFormatWithTimeZone(DateNoSeparatorsFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getTime24HourFormat() { return getFormatWithTimeZone(Time24HourFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getDMOSoapDateTimeFormat() { return getFormatWithTimeZone(DMOSoapDateTimeFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getDMOSoapDateTimestampFormat() { return getFormatWithTimeZone(DMOSoapDateTimestampFormat, lastSetTimeZone); }
	public static SimpleDateFormat getDMOSoapDateTimestampUTCFormat() { return getFormatWithTimeZone(DMOSoapDateTimestampUTCFormat,  lastSetTimeZone); }
	public static SimpleDateFormat getHomeTerminalFullMonthDateFormat() { return getFormatWithTimeZone(HomeTerminalFullMonthDateFormat,  lastSetTimeZone); }
    public static SimpleDateFormat getSqlDateTimeFormatMS() { return getFormatWithTimeZone(sqlDateTimeFormatMS, lastSetTimeZone); }
    public static SimpleDateFormat getUnassignedDrivingPeriodDateFormat() {return new SimpleDateFormat(UnassignedDrivingPeriodDateFormat); }

    /**
     * Creates a string with HH:MM for isMandateEnabled false.  HH:MM:SS for true.
     * @param datetime Date to convert to string.
     * @param isMandateEnabled Displays seconds if enabled.
     * @return Converted string from Date.  Empty string if datetime is null.
     */
    public static String createHomeTerminalTimeString(Date datetime, boolean isMandateEnabled){
        if (datetime == null){
            return "";
        }

        if (isMandateEnabled){
            return getHomeTerminalTime12HourFormatWithSeconds().format(datetime);
        }
        return getHomeTerminalTime12HourFormat().format(datetime);
    }

    public static String createTimeDurationString(Long millisecondDuration, boolean isMandateEnabled){
        return createTimeDurationString(millisecondDuration, isMandateEnabled, false);
    }

    public static String createTimeDurationString(Long millisecondDuration, boolean isMandateEnabled, boolean roundUp){
        Formatter durationFormatter = new Formatter(new StringBuilder(20), Locale.US);

        int seconds = (int) (millisecondDuration / 1000 % 60);
        int minutes = (int) (millisecondDuration / (1000 * 60) % 60);
        int hours = (int) (millisecondDuration / (1000 * 60 * 60));

        if (roundUp){
            if (isMandateEnabled && millisecondDuration % 1000 != 0) {
                seconds += 1;
            } else if (!isMandateEnabled && seconds != 0){
                minutes += 1;
                seconds = 0;
            }
        }

        if (seconds >= 60){
            minutes += 1;
            seconds %= 60;
        }
        if (minutes >= 60){
            hours += 1;
            minutes %= 60;
        }


        if (isMandateEnabled){
            return durationFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }
        return durationFormatter.format("%d:%02d", hours, minutes).toString();
    }

    /**
     * Date as 2000-01-01
     * Format: yyyy-MM-dd
     *
     * @param user user for home terminal time
     * @return SimpleDateFormat w/ set to the User's HomeTerminal TimeZone
     */
	public static SimpleDateFormat getHomeTerminalSqlDateFormat(User user)
	{
        SimpleDateFormat homeTerminalSqlDateFormat = getHomeTerminalSqlDateFormat();
        homeTerminalSqlDateFormat.setTimeZone(user.getHomeTerminalTimeZone().toTimeZone());
		return homeTerminalSqlDateFormat;
	}

    /**
     * Date and Time as 2000-01-01 13:00:00
     * Format: yyyy-MM-dd HH:mm:ss
     *
     * @param user user for home terminal time
     * @return SimpleDateFormat w/ set to the User's HomeTerminal TimeZone
     */
	public static SimpleDateFormat getHomeTerminalSqlDateTimeFormat(User user)
	{
        SimpleDateFormat homeTerminalSqlDateTimeFormat = getHomeTerminalSqlDateTimeFormat();
        homeTerminalSqlDateTimeFormat.setTimeZone(user.getHomeTerminalTimeZone().toTimeZone());
		return homeTerminalSqlDateTimeFormat;
	}
	
	public static final int MILLISECONDS_PER_MINUTE = 60000;
	public static final long MILLISECONDS_PER_HOUR = 3600000;
	public static final long MILLISECONDS_PER_DAY = 86400000;
	public static final int SECONDS_PER_DAY = 86400;

	public static Date getCurrentDateTimeUTC(){
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            return getCurrentDateTimeUTC_forEldMandate();

        return getCurrentDateTimeUTC_forAOBRD();
	}

    private static Date getCurrentDateTimeUTC_forAOBRD() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        if(CLOCK_TIMESTAMP_OFFSET_SECONDS != 0)
        {
            cal.add(Calendar.SECOND, (int)CLOCK_TIMESTAMP_OFFSET_SECONDS);
        }

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private static Date getCurrentDateTimeUTC_forEldMandate() {
        return TimeKeeper.getInstance().getCurrentDateTime().toDate();
    }

    public static DateTime getCurrentDateTime(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(TimeKeeper.getInstance().getCurrentDateTime().toDate()); // Not yet using Sync from TimeKeeper

        if(CLOCK_TIMESTAMP_OFFSET_SECONDS != 0)
        {
            cal.add(Calendar.SECOND, (int) CLOCK_TIMESTAMP_OFFSET_SECONDS);
        }
        return new DateTime(cal.getTime());
    }

	public static Date getSixMonthDateTimeUTC(){
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            return getSixMonthDateTimeUTC_forEldMandate();

        return getSixMonthDateTimeUTC_forAOBRD();
	}

    private static Date getSixMonthDateTimeUTC_forAOBRD() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(TimeKeeper.getInstance().now());

        if(CLOCK_TIMESTAMP_OFFSET_SECONDS != 0)
        {
            cal.add(Calendar.SECOND, (int)CLOCK_TIMESTAMP_OFFSET_SECONDS);
        }

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH) - 6));

        return cal.getTime();
    }

    private static Date getSixMonthDateTimeUTC_forEldMandate() {
        DateTime dt = TimeKeeper.getInstance().getCurrentDateTime();

        Calendar cal = Calendar.getInstance();
        cal.setTime(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH) - 6));

        return cal.getTime();
    }

    public static Date getCurrentDateTimeWithSecondsUTC(){

        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            return getCurrentDateTimeWithSecondsUTC_forEldMandate();

        return getCurrentDateTimeWithSecondsUTC_forAOBRD();

	}

    private static Date getCurrentDateTimeWithSecondsUTC_forAOBRD() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(TimeKeeper.getInstance().now());

        if(CLOCK_TIMESTAMP_OFFSET_SECONDS != 0)
        {
            cal.add(Calendar.SECOND, (int)CLOCK_TIMESTAMP_OFFSET_SECONDS);
        }

        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private static Date getCurrentDateTimeWithSecondsUTC_forEldMandate() {
        return getCurrentDateTimeUTC();
    }

	public static Date getDateTimeWithoutSecondsFromDate(Date date) {
		Date returnDate = null;

		try {
			// Truncate seconds
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			returnDate = cal.getTime();
		}
		catch (Exception e) {
			returnDate = null;
		}

		return returnDate;
	}


	public static Date getDateTimeFromString(String date)
	{
        return getDateTimeFromString(date, getHomeTerminalDateTimeFormat(), lastSetTimeZone);
    }

    public static Date getDateTimeFromString(String date, SimpleDateFormat dateFormat, TimeZone timeZone)
	{
		Date retVal = null;

		try
		{
            dateFormat.setTimeZone(timeZone);
            retVal = dateFormat.parse(date);
		}
		catch (ParseException e)
		{

        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}

		return retVal;
	}


	public static Date getDateFromString(String date)
	{
		Date retVal = null;

		try
		{
            SimpleDateFormat sdf = getHomeTerminalDateFormat();
            sdf.setTimeZone(lastSetTimeZone);
            retVal = sdf.parse(date);
		}
		catch (ParseException e)
		{

			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}

		return retVal;
	}

    /**
     * Answer the current clock time, in the user's home terminal time zone.
     * Use the universal clock in the EOBR, or if not available,
     * then get universal time from the system clock.
     * Convert the current universal time into the home terminal timezone.
     *
     * Deprecated because this does the exact same thing
     *
     * @param user User that the time is for
     * @deprecated use getCurrentDateTime() instead.
     */
    @Deprecated
	public static Date CurrentHomeTerminalTime(User user) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            return CurrentHomeTerminalTime_forEldMandate(user);

        return CurrentHomeTerminalTime_forAORBD(user);
	}

    public static Date getCurrentHomeTerminalTime(User user) {
        DateTime date = new DateTime(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        DateTime newDate = date.toDateTime(DateTimeZone.forTimeZone(user.getHomeTerminalTimeZone().toTimeZone()));
        LogCat.getInstance().v("DateUtil.CurrentHomeTerminalTime", newDate.toDate().toString());

        try {
            return newDate.toDate();
        } catch (Exception ex) {

            ErrorLogHelper.RecordException(ex);
            return null;
        }
    }

    private static Date CurrentHomeTerminalTime_forAORBD(User user) {
        Date returnDate;
        Calendar c = null;
        // At times Calendar.getInstance can cause a NullPointer exception, it
        // seems to be a timing issue,
        // so if there is an exception wait for 2 secs and then get
        // Calendar.getInstance
        try {
            c = Calendar.getInstance(user.getHomeTerminalTimeZone().toTimeZone());
        } catch (Exception ex) {
            try {
                Thread.sleep(2000);
                c = Calendar.getInstance(user.getHomeTerminalTimeZone().toTimeZone());
            } catch (InterruptedException e) {
                ErrorLogHelper.RecordException(e);
            }
        }

        c.setTime(getCurrentDateTimeUTC());
        returnDate = c.getTime();

        return returnDate;
    }

    private static Date CurrentHomeTerminalTime_forEldMandate(User user) {
        DateTime date = new DateTime(getCurrentDateTimeUTC());
        DateTime newDate = date.toDateTime(DateTimeZone.forTimeZone(user.getHomeTerminalTimeZone().toTimeZone()));
        LogCat.getInstance().v("DateUtil.CurrentHomeTerminalTime", newDate.toDate().toString());

        try {
            return newDate.toDate();
        } catch (Exception ex) {

                ErrorLogHelper.RecordException(ex);
            return null;
        }
    }

    public static Date AddDays(Date startDate, int daysToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }

    public static Date AddHours(Date startDate, int hoursToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        return cal.getTime();
    }

    public static Date AddMinutes(Date startDate, int minutesToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        cal.add(Calendar.MINUTE, minutesToAdd);
        return cal.getTime();
    }

    public static Date AddSeconds(Date startDate, int secondsToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        cal.add(Calendar.SECOND, secondsToAdd);
        return cal.getTime();
    }

    public static Date AddTime(Date startDate, Date timeToAdd)
    {
    	Calendar addTime = Calendar.getInstance();
    	addTime.setTimeZone(lastSetTimeZone);
    	addTime.setTime(timeToAdd);
    	
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        cal.add(Calendar.HOUR_OF_DAY, addTime.get(Calendar.HOUR_OF_DAY));
        cal.add(Calendar.MINUTE, addTime.get(Calendar.MINUTE));
        cal.add(Calendar.SECOND, addTime.get(Calendar.SECOND));
        return cal.getTime();    	
    }
    
    public static Date AddMilliseconds(Date startDate, int millisecondsToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MILLISECOND, millisecondsToAdd);
        return cal.getTime();
    }

    public static Date addMilliseconds(Date startDate, long millisecondsToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millisecondsToAdd + startDate.getTime());
        return cal.getTime();
    }

    /**
     * Convert the timestamp to the previous boundary for use as a Log Event time.
     * Log Events are defined on a 1 minute boundary.  The conversion involves
     * removing the seconds from the timestamp.   For example if the time to convert
     * is 8:52:12 AM, the new converted time should be 8:52:00 AM.
     * @return Date without seconds and milliseconds. This should be the time of the last event.
     */
    public static Date ConvertToPreviousLogEventTime(Date timestampToConvert)
    {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            return ConvertToPreviousLogEventTime_forEldMandate(timestampToConvert);

        return ConvertToPreviousLogEventTime_forAOBRD(timestampToConvert);
    }

    private static Date ConvertToPreviousLogEventTime_forAOBRD(Date timestampToConvert) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestampToConvert);

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private static Date ConvertToPreviousLogEventTime_forEldMandate(Date timestampToConvert) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestampToConvert);

        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Date GetDateFromDateTime(Date timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

	public static double ConvertMillisecondsToHours(long ms)
	{
		return ConvertMillisecondsToMinutes(ms) / 60;

	}
	
	public static double ConvertMillisecondsToMinutes(long ms)
	{
		return (double)ms / (double)(60 * 1000);
	}

    public static double ConvertMillisecondsToSeconds(long ms)
    {
        return (double)ms / (double)(1000);
    }

    /**
     * used to set system time to server time
     * @param timestampUtc
     */
    public static void SetSystemTime(Date timestampUtc) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            TimeKeeper.getInstance().synchronizeWithServerTime(timestampUtc);
        }
        updateClock(timestampUtc);
    }


    private static void updateClock(Date dateTime){
        // set the local clock
        Date currentClockUtc = TimeKeeper.getInstance().getCurrentDateTime().toDate();  //Get the current Android time

        long timestampTicks = dateTime.getTime();
        long currentClockUtcTicks = currentClockUtc.getTime();
        long currentOffset = Math.round((timestampTicks - currentClockUtcTicks) / 1000);
        long previousOffset = CLOCK_TIMESTAMP_OFFSET_SECONDS;
        CLOCK_TIMESTAMP_OFFSET_SECONDS = currentOffset;

        HAS_CLOCK_BEEN_SET = true;

        long offsetDifference = currentOffset - previousOffset;

        String msg = String.format("DateUtility.SetSystemTime to: {%s} current: {%s} offset: {%s} diff: {%s}", dateTime, currentClockUtc, CLOCK_TIMESTAMP_OFFSET_SECONDS, offsetDifference);
        LogCat.getInstance().v("DateUtility", msg);
        ErrorLogHelper.RecordMessage(msg);
    }

    public static void setHomeTerminalTimeDateFormatTimeZone(TimeZone tz)
    {
        lastSetTimeZone = tz;

		TimeZone.setDefault(tz); // Hack: Android framework "Strongly" recommends not doing this.

		LogCat.getInstance().d("DateUtility", String.format("SetHomeTerminalTimeZone %s", tz.getDisplayName()));
    }

    /**
     * Answer the number of days between the two dates
     */
	public static int DaysBetween(Date date1, Date date2) {
		
		Calendar gc1 = Calendar.getInstance();	// this will be the chronologically earlier of the two dates
		Calendar gc2 = Calendar.getInstance();
		
		if(date1.getTime() < date2.getTime())
		{
			// date1 is chronologically earlier than date2
			gc1.setTime(date1);
			gc2.setTime(date2);
		}
		else
		{
			// date2 is chronologically earlier than date1
			gc1.setTime(date2);
			gc2.setTime(date1);
		}
				
		if(gc1.get(Calendar.YEAR) < 1900 || gc2.get(Calendar.YEAR) < 1900) 
			// If one of the dates comes in with a whacked out year, than abort and don't return any days between
			return 0;
		
		// at this point gc1 is chronologically earlier than gc2

		// determine the number of days within the current year
		int days1 = gc1.get(Calendar.DAY_OF_YEAR) - 1;
		int days2 = gc2.get(Calendar.DAY_OF_YEAR) - 1;
		int daysBetween = days2 - days1;

		if(gc1.get(Calendar.YEAR) != gc2.get(Calendar.YEAR))
		{
			// each of the two dates fall in a different year

			// determine what the highest year is
			int maxYear = Math.max(gc1.get(Calendar.YEAR), gc2.get(Calendar.YEAR));

			// add the number of days for each year between date1 and date2
			Calendar gctmp = (Calendar) gc1.clone();
			for (int f = gctmp.get(Calendar.YEAR); f < maxYear; f++) {
				// this algorithm accounts for leap years correctly
				daysBetween += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);
				gctmp.add(Calendar.YEAR, 1);
			}	
		}	
		
		return daysBetween;
	} 
	
	/**
	 * Answer if the System Clock has been set
	 */
	public static boolean HasClockBeenSet() {
		return HAS_CLOCK_BEEN_SET;
	}
	
	/**
	 * Answer if the System Clock has been set
	 */
	public static long ClockTimestampOffset() {
		return CLOCK_TIMESTAMP_OFFSET_SECONDS;
	}
	
	/**
	 * Answer if the System Setting for "Automatic" Date and Time settings - Use network-provided time is set
	 */
	public static boolean IsAutoDateTimePreferenceSet(ContentResolver ctr) {
//	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//	        // For JB+
//	        return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) > 0;
//	    }
	    // For older Android versions
	    return Settings.System.getInt(ctr, Settings.System.AUTO_TIME, 0) > 0;
	}

    public static long getMillisecondsFromHHmm(String timespanString) throws ParseException
    {
        return getMillisecondsFromHHmmss(timespanString + ":00");
    }

    public static long getMillisecondsFromHHmmss(String timespanString) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date time = dateFormat.parse(timespanString);

        return time.getTime();
    }

    public static String getHHmmssFromMilliseconds(long milliseconds)
    {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / DateUtility.MILLISECONDS_PER_MINUTE) % 60;
        long hours = (milliseconds / DateUtility.MILLISECONDS_PER_HOUR);

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
	 * <p>Checks if two dates are on the same day ignoring time.</p>
	 * @param date1  the first date, not altered, not null
	 * @param date2  the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException if either date is <code>null</code>
	 */
	@SuppressWarnings("WeakerAccess")
    public static boolean IsSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return IsSameDay(cal1, cal2);
	}

	/**
	 * <p>Checks if two calendars represent the same day ignoring time.</p>
	 * @param cal1  the first calendar, not altered, not null
	 * @param cal2  the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException if either calendar is <code>null</code>
	 */
	@SuppressWarnings("WeakerAccess")
    public static boolean IsSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
				cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

    /**
     * Compares two dates and check if they are the same without taking care of seconds
     * @param firstDate First date to be compare
     * @param secondDate Second date to be compare
     * @return true if the year, month, day, hour and minute are equal.
     */
    public static boolean IsSameDayWithoutSeconds(Date firstDate, Date secondDate) {
        Calendar first = Calendar.getInstance();
        first.setTime(firstDate);

        Calendar second = Calendar.getInstance();
        second.setTime(secondDate);

        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
                && first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH)
                && first.get(Calendar.HOUR_OF_DAY) == second.get(Calendar.HOUR_OF_DAY)
                && first.get(Calendar.MINUTE) == second.get(Calendar.MINUTE);
    }


    /**
     * <p>Checks if a date is today.</p>
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean IsToday(Date date, User currentDriver) {
        return IsSameDay(date, DateUtility.CurrentHomeTerminalTime(currentDriver));
    }

    public static DateTime getEndOfLogDate(DateTime input) {
        return new DateTime(input.getYear(), input.getMonthOfYear(), input.getDayOfMonth(), 23, 59, 59, input.getZone());
    }

    @SuppressWarnings("unused")
    public static DateTime getDateTimeFromStringAsDateTime(String date)
    {
        try
        {
            SimpleDateFormat sdf = getHomeTerminalDateTimeFormat();
            sdf.setTimeZone(TimeZone.getDefault());
            return new DateTime(sdf.parse(date)).toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        }
        catch (ParseException e)
        {
            LogCat.getInstance().e("UnhandledCatch", e.getMessage() + ": " + LogCat.getInstance().getStackTraceString(e));
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static LocalDate getDateFromStringAsLocalDate(String date)
    {
        Date retVal = null;

        try
        {
            SimpleDateFormat sdf = getHomeTerminalDateFormat();
            sdf.setTimeZone(TimeZone.getDefault());
            retVal = sdf.parse(date);
        }
        catch (ParseException e)
        {

            LogCat.getInstance().e("UnhandledCatch", e.getMessage() + ": " + LogCat.getInstance().getStackTraceString(e));
        }

        return new LocalDate(retVal);
    }


    /**
     * Tests for joda DateTime that is null or set at the epoch
     * JodaDateTime at epoch has most likely not been populated
     * @param jodaDateTime
     * @return
     */
    public static boolean isNullOrEpoch(DateTime jodaDateTime)
    {
        return jodaDateTime == null || jodaDateTime.getMillis() == 0L;
    }

    /**
     * Convert date to another timezone, but not really. This is actually a hack
     * because the Java Date object is thought of as UTC. By "converting" the timezone here
     * we're actually changing the time, not the zone. This is to be used very carefully.
     */
    public static Date convertToTimezone(Date sourceDate, TimeZone targetTimeZone) {
        try {
            // convert timezone to Joda-Time object
            DateTimeZone targetDateTimeZone = DateTimeZone.forTimeZone(targetTimeZone);

            // convert sourcedate to Joda-Time object
            DateTime targetDateTime = new DateTime(sourceDate).toDateTime(targetDateTimeZone);

            // convert to Joda-Time LocalDateTime object
            LocalDateTime targetLocalDateTime = targetDateTime.toLocalDateTime();

            // convert to java Date object
            return targetLocalDateTime.toDate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static long GetTotalSeconds(Date startDate)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        return cal.getTimeInMillis()/1000;
    }
}

