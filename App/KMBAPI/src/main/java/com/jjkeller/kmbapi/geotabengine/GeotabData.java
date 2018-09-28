package com.jjkeller.kmbapi.geotabengine;

import com.jjkeller.kmbapi.common.Hex;
import com.jjkeller.kmbapi.common.TabDataConversionUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jhm2586 on 8/31/2016.
 */
public class GeotabData {
    public static final int DATE_TIME_BASE_OFFSET_SECONDS = 1009843200;
    protected static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    protected final double LatLonConversionFactor = Math.pow(10, -7);
    protected final float RpmConversionFactor = 1f / 4f;
    protected final float OdometerConversionFactor = 1f / 10f;
    private long datetime;
    private float latitude = 0.0F;
    private float longitude = 0.0F;
    private float speedometer = -1.0F;
    private float tachometer = -1.0F;
    private float odometer = -1.0F;
    private float origOdometer = -1.0F;
    private byte status;

    public GeotabData(){
        this(new byte[42]);
    }

    public GeotabData(byte[] abData){

        GeotabData geotabData = this;

        long dateTime = extractUnsignedInt(abData, 3);    // seconds since 1/1/2002
        dateTime += DATE_TIME_BASE_OFFSET_SECONDS;             // seconds since 1/1/1970
        dateTime *= 1000;                           // milliseconds
        geotabData.setDatetime(dateTime);           // milliseconds since epoch

        int iLatitude = extractInt(abData, 7);
        double lat = (float)iLatitude * LatLonConversionFactor;
        geotabData.setLatitude((float)lat);    // (Units given in 10^-7)

        int iLongitude = extractInt(abData, 11);
        double lon = (float)iLongitude * LatLonConversionFactor;
        geotabData.setLongitude((float)lon);    // (Units given in 10^-7)

        //A byte according to the language spec represents a value between −128 - 127.
        //Since we don't care about negative values when it comes to speed, we need to convert it to unsigned byte, increasing the max value to 255
        float mph = abData[15] & 0xFF;
        mph *= Constants.MILES_PER_KILOMETER;
        geotabData.setSpeedometer(mph);

        int rpm = extractUnsignedShort(abData, 16);
        geotabData.setTachometer(rpm * RpmConversionFactor); // Convert to RPM (Units given in 0.25)

        long tabOdometerValue = extractUnsignedInt(abData, 18);
        float originalOdometer = (float)tabOdometerValue / 10f;
        geotabData.setOrigOdometer(originalOdometer);

        float convertedOdometer = TabDataConversionUtil.convertOdometerReading(tabOdometerValue);
        geotabData.setOdometer(convertedOdometer);

        geotabData.setStatus(abData[22]);
    }

    public static GeotabData FromByteArray(byte[] abData) {
        return new GeotabData(abData);
    }

    public static int extractInt(byte[] abData, int position) {
        byte[] bytes = new byte[4];
        System.arraycopy(abData, position, bytes, 0, bytes.length);
        ByteBuffer abConvert = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return abConvert.getInt();
    }

    public static long extractUnsignedInt(byte[] abData, int position) {
        byte[] bytes = new byte[4];
        System.arraycopy(abData, position, bytes, 0, bytes.length);
        ByteBuffer abConvert = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return abConvert.getInt() & 0xffffffffL;
    }


    public static int extractUnsignedShort(byte[] abData, int position) {
        byte[] bytes = new byte[2];
        System.arraycopy(abData, position, bytes, 0, bytes.length);
        ByteBuffer abConvert = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return abConvert.getShort() & 0xffff;
    }

    /**
     * // (Units given in seconds since Jan 1, 2002)
     */
    public static Date extractDate(byte[] abData, int position) {
        byte[] bytes = new byte[4];
        System.arraycopy(abData, position, bytes, 0, bytes.length);
        ByteBuffer abConvert = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        long secondsSince2002 = abConvert.getInt() & 0xffffffffL;
        long secondsSinceEpoch = secondsSince2002 + DATE_TIME_BASE_OFFSET_SECONDS;
        long millisecondsSinceEpoch = secondsSinceEpoch * 1000;

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.clear();
        c.setTimeInMillis(millisecondsSinceEpoch);
        return c.getTime();
    }

    public static byte[] populateInt(final long data) {
        return ByteBuffer
                .allocate(4)
                .order(ByteOrder.nativeOrder())
                .putInt((int) data)
                .array();
    }

    public static byte[] populateDate(final Date date) {
        long secondsSince2002 = convertMillisecondsSinceEpochToSecondsSinceJanOne2002(date.getTime());
        ByteBuffer abConvert = ByteBuffer
                .allocate(4)
                .order(ByteOrder.nativeOrder())
                .putInt((int) secondsSince2002);

        return abConvert.array();
    }

    public static byte[] populateShort(final int data) {
        ByteBuffer abConvert = ByteBuffer
                .allocate(2)
                .order(ByteOrder.nativeOrder())
                .putShort((short) data);
        return abConvert.array();
    }

    public static byte[] populateByte(final float data) {
        ByteBuffer abConvert = ByteBuffer
                .allocate(1)
                .order(ByteOrder.nativeOrder())
                .put((byte) data);
        return abConvert.array();
    }

    private static long convertSecondsSinceJanOne2002ToMillisecondsSinceEpoch(long secondsSinceJanOne2002) {
        long secondsSinceEpoch = secondsSinceJanOne2002 + DATE_TIME_BASE_OFFSET_SECONDS;
        return secondsSinceEpoch * 1000;
    }

    private static long convertMillisecondsSinceEpochToSecondsSinceJanOne2002(long millisecondsSinceEpoch) {
        long secondsSinceEpoch = millisecondsSinceEpoch / 1000;
        return secondsSinceEpoch - DATE_TIME_BASE_OFFSET_SECONDS;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getSpeedometer() {
        return speedometer;
    }

    public void setSpeedometer(float speedometer) {
        this.speedometer = speedometer;
    }

    public float getTachometer() {
        return tachometer;
    }

    public void setTachometer(float tachometer) {
        this.tachometer = tachometer;
    }

    /**
     * Gets odometer reading from Tab device in miles.
     * @return
     */
    public float getOdometer() {
        return odometer;
    }

    /**
     * Sets odometer reading from Tab device in miles.
     * @return
     */
    public void setOdometer(float odometer) {
        this.odometer = odometer;
    }

    /**
     * Gets original odometer reading from Tab device in KM.
     * @return
     */
    public float getOrigOdometer() {
        return origOdometer;
    }

    /**
     * Sets original odometer reading from Tab device in KM.
     */
    public void setOrigOdometer(float odometer) {
        this.origOdometer = odometer;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public boolean isGpsLatched() {
        return ((getStatus() & (1 << 0)) != 0) && !GlobalState.getInstance().getForceGeotabInvalidGPS();
    }

    public boolean isIgnitionOn() {
        return (getStatus() & (1 << 1)) != 0;
    }

    public boolean hasEngineData() {
        return (getStatus() & (1 << 2)) != 0;
    }

    public boolean isDateTimeValid() {
        return (getStatus() & (1 << 3)) != 0;
    }

    public HOSMessage toHOSMessage() {
        HOSMessage message = new HOSMessage();
        message.setTimestampUtc(new DateTime(getDatetime()));
        message.setGpsLatitude(getLatitude());
        message.setGpsLongitude(getLongitude());
        message.setSpeedometer(getSpeedometer());
        message.setTachometer(getTachometer());
        message.setOdometer(getOdometer());
        message.setOrigOdometer(getOrigOdometer());
        message.setGpsValid(isGpsLatched());
        message.setIgnitionOn(isIgnitionOn());
        message.setEngineActivityDetected(hasEngineData());
        message.setDatetimeValid(isDateTimeValid());

        return message;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "DateTime: %s\nLatitude: %.5f\nLongitude: %.5f\nSpeed: %.2f\nTach: %.0f\nOdometer: %.1f\nGPS Latch: %b\nIGN ON: %b\nEngine Data: %b\nDate Valid: %b",
                format.format(new Date(getDatetime())),
                getLatitude(),
                getLongitude(),
                getSpeedometer(),
                getTachometer(),
                getOdometer(),
                isGpsLatched(),
                isIgnitionOn(),
                hasEngineData(),
                isDateTimeValid());
    }

    @Override
    public boolean equals(Object o) {
        GeotabData data = (GeotabData)o;

        return this.getDatetime() == data.getDatetime() &&
                this.getLatitude() == data.getLatitude() &&
                this.getLongitude() == data.getLongitude() &&
                this.getOdometer() == data.getOdometer() &&
                this.getSpeedometer() == data.getSpeedometer() &&
                this.getTachometer() == data.getTachometer() &&
                this.getStatus() == data.getStatus();
    }

    @Override
    public int hashCode() {
        return (int)(this.getDatetime() / 1000);
    }

    protected String extractString(byte[] abData, int position) {
        return  String.format("%08X", extractUnsignedInt(abData, position));
    }


    public byte[] calcChecksum(byte[] data, int iLength) {
        byte[] checksum = new byte[]{0x00, 0x00};

        for (int i = 0; i < iLength; i++) {
            checksum[0] += data[i];
            checksum[1] += checksum[0];
        }
        return checksum;
    }
}