package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

/**
 * Class that extends GpsLocation and allows for the translation of GpsLocation properties to
 * Event properties
 */
public class GpsLocationAdapter extends GpsLocation {

    /**
     * Event that we want to translate properties to and from. Will be parent of LocationAdapter
     * object that contains this object
     */
    protected transient EmployeeLogEldEvent _parentAdapter = null;
    /**
     * DecodedLocationAdapter object representing the decoded location of this gps location, and
     * allows further property translation between event and decoded location
     */
    protected DecodedLocationAdapter _decodedLocationAdapter = new DecodedLocationAdapter(null);


    /**
     * Constructor accepting an event that will be used to translate properties to and from
     *
     * @param parentAdapter Event to translate to and from
     */
    protected GpsLocationAdapter(EmployeeLogEldEvent parentAdapter) {
        super();
        this.setInternalParentAdapter(parentAdapter);
        if (this._decodedLocationAdapter == null) {
            this._decodedLocationAdapter = new DecodedLocationAdapter(this._parentAdapter);
        } else {
            this._decodedLocationAdapter.setInternalParentAdapter(this._parentAdapter);
        }
    }

    /**
     * Constructor accepting an event, latitude and longitude info, and a timestamp representing
     * time fix was obtained
     *
     * @param parentAdapter Event to translate to and from
     * @param timestamp     Timestamp when gps location was obtained
     * @param lat           Latitude value as float
     * @param lon           Longitude value as float
     */
    protected GpsLocationAdapter(EmployeeLogEldEvent parentAdapter, Date timestamp, float lat, float lon) {
        super(timestamp, lat, lon);
        this.setInternalParentAdapter(parentAdapter);
        this.setParentPropertiesFromConstructor(timestamp, lat, lon);
        if (this._decodedLocationAdapter == null) {
            this._decodedLocationAdapter = new DecodedLocationAdapter(this._parentAdapter);
        } else {
            this._decodedLocationAdapter.setInternalParentAdapter(this._parentAdapter);
        }
    }

    /**
     * Constructor accepting an event, latitude and longitude info, and a timestamp representing
     * time fix was obtained. Additionally accepts direction for both lat and long
     *
     * @param parentAdapter Event to translate to and from
     * @param timestamp     Timestamp when gps location was obtained
     * @param lat           Latitude value as float
     * @param latDirection  Direction of latitude value
     * @param lon           Longitude value as float
     * @param lonDirection  Direction of longitude value
     */
    protected GpsLocationAdapter(EmployeeLogEldEvent parentAdapter, Date timestamp, float lat, char latDirection, float lon, char lonDirection) {
        super(timestamp, lat, latDirection, lon, lonDirection);
        this.setInternalParentAdapter(parentAdapter);
        this.setParentPropertiesFromConstructor(timestamp, lat, lon);
        this._decodedLocationAdapter.setInternalParentAdapter(this._parentAdapter);
    }

    /**
     * Method that sets the internal reference to an event for property translation
     *
     * @param parentAdapter Event to translate properties to and from
     */
    protected void setInternalParentAdapter(EmployeeLogEldEvent parentAdapter) {
        this._parentAdapter = parentAdapter;
    }

    /**
     * Method to be called from constructor to set parent adapter base properties
     *
     * @param timestamp GpsTimestamp
     * @param lat       Latitude
     * @param lon       Longitude
     */
    protected void setParentPropertiesFromConstructor(Date timestamp, float lat, float lon) {
        if (this._parentAdapter != null) {
            this._parentAdapter.setGpsTimestamp(timestamp);
            this._parentAdapter.setLatitude((double) lat);
            this._parentAdapter.setLongitude((double) lon);
        }
    }

    /**
     * Method that accepts an event to pass into the constructor of a GpsLocationAdapter object as
     * it's parent, and a GpsLocation object to transform into an Adapter
     *
     * @param adapter Event to translate properties to and from
     * @param baseObj GpsLocation object to pull values from
     * @return GpsLocationAdapter representing passed in GpsLocation
     */
    public static final GpsLocationAdapter getAdapterFromBase(EldEventAdapter adapter, GpsLocation baseObj) {
        GpsLocationAdapter returnAdapter;
        if (baseObj == null) {
            return null;
        }
        //Create Adapter from as much data as we can
        if (!baseObj.IsEmpty()) {
            boolean hasValidDirectionalInfo = baseObj.getLatDirection() != Character.MIN_VALUE && baseObj.getLonDirection() != Character.MIN_VALUE;

            if (hasValidDirectionalInfo) {
                returnAdapter = new GpsLocationAdapter(adapter._event, baseObj.getTimestampUtc(), baseObj.getLatitudeDegrees(), baseObj.getLatDirection(), baseObj.getLongitudeDegrees(), baseObj.getLonDirection());
            } else {
                returnAdapter = new GpsLocationAdapter(adapter._event, baseObj.getTimestampUtc(), baseObj.getLatitudeDegrees(), baseObj.getLongitudeDegrees());
            }
        } else if (adapter != null) {
            returnAdapter = new GpsLocationAdapter(adapter._event);
        } else {
            returnAdapter = new GpsLocationAdapter(null);
        }

        if (baseObj.isPrimaryKeySet())
            returnAdapter.setPrimaryKey(baseObj.getPrimaryKey());

        returnAdapter.setDecodedInfo(baseObj.getDecodedInfo());

        return returnAdapter;
    }

        /*
 The following block of methods are overrides of the GpsLocation base class's getters and setters.
 This block defines how we will use the EmployeeLogEldEvent object to store values for the
 GpsLocation object. Further, it defines how we will construct object(s) that are present on a
GpsLocation but not on an EmployeeLogEldEvent, given the properties we have.

Overrides requiring additional explanation will have method-level documentation as well
  */

    @Override
    public long getPrimaryKey() {
        return super.getPrimaryKey();
    }

    @Override
    public void setPrimaryKey(long primaryKey) {
        super.setPrimaryKey(primaryKey);
    }

    @Override
    public boolean isPrimaryKeySet() {
        return super.isPrimaryKeySet();
    }

    @Override
    public float getLatitudeDegrees() {
        if (this._parentAdapter != null && this._parentAdapter.getLatitude() != null) {
            return this._parentAdapter.getLatitude().floatValue();
        } else
            return super.getLatitudeDegrees();
    }

    /**
     * Setter for the timestamp a GpsLocation was obtained. If the timestampUtc object is not null,
     * we clear status codes as a timestamp denotes a successful gps location fix
     *
     * @param timestampUtc
     */
    @Override
    public void setTimestampUtc(Date timestampUtc) {
        super.setTimestampUtc(timestampUtc);
        if (this._parentAdapter != null)
            this._parentAdapter.setGpsTimestamp(timestampUtc);
    }

    @Override
    public Date getTimestampUtc() {
        Date returnDate = super.getTimestampUtc();
        if (this._parentAdapter != null)
            returnDate = this._parentAdapter.getGpsTimestamp();

        return returnDate;
    }

    @Override
    public void setLatitudeDegrees(float latitudeDegrees) {
        super.setLatitudeDegrees(latitudeDegrees);
        //Use a cast, will lose precision
        if (this._parentAdapter != null)
            this._parentAdapter.setLatitude((double) latitudeDegrees);
    }

    @Override
    public float getLongitudeDegrees() {
        if (this._parentAdapter != null && this._parentAdapter.getLongitude() != null) {
            return this._parentAdapter.getLongitude().floatValue();
        } else
            return super.getLongitudeDegrees();
    }

    @Override
    public void setLongitudeDegrees(float longitudeDegrees) {
        super.setLongitudeDegrees(longitudeDegrees);
        //Use a cast, will lose precision
        if (this._parentAdapter != null)
            this._parentAdapter.setLongitude((double) longitudeDegrees);
    }

    /**
     * Getter that returns the internal DecodedLocationAdapter object from this object. If that
     * field is null, it is set to a new DecodedLocationAdapter using the parent event of this object
     * as an argument to allow property translation
     *
     * @return DecodedLocation containing values of geocoded location info
     */
    @Override
    public DecodedLocation getDecodedInfo() {
        if (this._decodedLocationAdapter == null || this._decodedLocationAdapter._parentAdapter == null) {
            this._decodedLocationAdapter = new DecodedLocationAdapter(this._parentAdapter);
        }
        return this._decodedLocationAdapter;
    }

    /**
     * Setter that accepts a DecodedLocation object and translates that object to an Adapter.
     * Assigns new adapter to internal decoded location adapter field
     *
     * @param decodedInfo DecodedLocation object to translate and assign to internal adapter field
     */
    @Override
    public void setDecodedInfo(DecodedLocation decodedInfo) {
        super.setDecodedInfo(decodedInfo);
        this._decodedLocationAdapter = DecodedLocationAdapter.getAdapterFromBase(this._parentAdapter, decodedInfo);
    }
}
