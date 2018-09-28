package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.configuration.GlobalState;

import java.util.Date;

/**
 * Class that extends LocationTranslationBase and allows for the transformation of properties on an
 * EmployeeLogEldEvent to and from a Location object
 */
public class LocationAdapter extends Location {

    /**
     * Protected T parentAdapter is the EventTranslationBase containing properties to assign to and
     * from a TConcreteType object
     */
    protected transient EmployeeLogEldEvent _parentAdapter = null;
    /**
     * Internal GpsLocationAdapter to allow further property translation
     */
    private GpsLocationAdapter _gpsInfo = new GpsLocationAdapter(null);

    /**
     * Constructor that sets the parent event that will have properties translated
     *
     * @param parentEvent Event containing this Adapter
     */
    public LocationAdapter(EmployeeLogEldEvent parentEvent) {
        this._parentAdapter = parentEvent;
        if (this._gpsInfo == null) {
            this._gpsInfo = this.getGpsInfoFromIEvent();
            if (this._gpsInfo == null)
                this._gpsInfo = new GpsLocationAdapter(this._parentAdapter);
        }
        else
            this._gpsInfo.setInternalParentAdapter(this._parentAdapter);
    }

    /*
 The following block of methods are overrides of the Location base class's getters and setters.
 This block defines how we will use the EmployeeLogEldEvent object to store values for the
 Location object. Further, it defines how we will construct object(s) that are present on a
Location but not on an EmployeeLogEldEvent, given the properties we have.

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
    public String getName() {
        if (this._parentAdapter != null)
            return this._parentAdapter.getDriversLocationDescription();
        else
            return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        if (this._parentAdapter != null)
            this._parentAdapter.setDriversLocationDescription(name);
    }

    /**
     * Getter that returns this object's internal GpsLocationAdapter object. If that object is null,
     * it creates a new one from the properties currently present on the object.
     *
     * @return internal GpsLocationAdapter object
     */
    @Override
    public GpsLocation getGpsInfo() {
        if (this._gpsInfo == null || this._gpsInfo._parentAdapter == null) {
            this._gpsInfo = this.getGpsInfoFromIEvent();
        }
        return this._gpsInfo;
    }

    /**
     * Setter that accepts a GpsLocation to be assigned to the internal GpsLocation object. If the
     * object is an Adapter, it sets it straight away. If not, it translates the incoming base
     * object into an adapter object and then assigns it. It then takes that object and assigns
     * the corresponding properties on the parent event to that of the GpsLocationAdapter
     *
     * @param gpsInfo GpsLocation containing values to set
     */
    @Override
    public void setGpsInfo(GpsLocation gpsInfo) {
        super.setGpsInfo(gpsInfo);
        this._gpsInfo = GpsLocationAdapter.getAdapterFromBase(this._parentAdapter, gpsInfo);
    }

    @Override
    public float getOdometerReading() {
        if (this._parentAdapter != null && this._parentAdapter.getOdometer() != null) {
            return this._parentAdapter.getOdometer();
        } else
            return super.getOdometerReading();
    }

    @Override
    public void setOdometerReading(float odometerReading) {
        super.setOdometerReading(odometerReading);
        if (this._parentAdapter != null)
            this._parentAdapter.setOdometer(odometerReading);
    }

    @Override
    public float getEndOdometerReading() {
        if (this._parentAdapter != null && this._parentAdapter.getEndOdometer() != null) {
            return this._parentAdapter.getEndOdometer();
        } else
            return super.getEndOdometerReading();
    }

    @Override
    public void setEndOdometerReading(float endOdometerReading) {
        super.setEndOdometerReading(endOdometerReading);
        if (this._parentAdapter != null)
            this._parentAdapter.setEndOdometer(endOdometerReading);
    }

    @Override
    public String ToLocationString() {
        String returnString = super.ToLocationString();
        if (returnString == null || returnString.equals("")) {
            if (this._parentAdapter != null) {
                returnString = this._parentAdapter.getGeolocation();
                if (returnString == null || returnString.equals("")) {
                    returnString = this._parentAdapter.getDriversLocationDescription();
                }
            }
        }
        return returnString;
    }

    @Override
    public boolean IsEmpty() {
        return super.IsEmpty() &&
                (this._parentAdapter == null ||
                        ((this._parentAdapter.getGeolocation() == null || this._parentAdapter.getGeolocation().equals("")) &&
                                (this._parentAdapter.getDriversLocationDescription() == null ||
                this._parentAdapter.getDriversLocationDescription().equals(""))));
    }

    /**
     * Method to create a LocationAdapter object from a Location base object and set base properties
     * on its parent adapter object
     *
     * @param adapterInstance EventAdapter containing location object being assigned to
     * @param baseObj         Location object to pull properties from
     * @return fully populated LocationAdapter
     */
    public static final LocationAdapter getAdapterFromBase(EldEventAdapter adapterInstance, Location baseObj) {
        LocationAdapter returnAdapter;

        //Got nothing, just new one up
        if (baseObj == null) {
            adapterInstance._event.setDriversLocationDescription("");
            adapterInstance._event.setLatitude(null);
            adapterInstance._event.setLongitude(null);
            adapterInstance._event.setGeolocation(null);
            adapterInstance._event.setGpsTimestamp(null);
            return new LocationAdapter(adapterInstance._event);
        }
        else
        {
            returnAdapter = new LocationAdapter(adapterInstance._event);
        }
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            returnAdapter.setName(baseObj.getName());
        }
        returnAdapter.setPrimaryKey(baseObj.getPrimaryKey());
        returnAdapter.setEndOdometerReading(baseObj.getEndOdometerReading());
        returnAdapter.setOdometerReading(baseObj.getOdometerReading());
        //Translate child objects
        GpsLocationAdapter gpsLocationAdapter = GpsLocationAdapter.getAdapterFromBase(adapterInstance, baseObj.getGpsInfo());
        returnAdapter.setGpsInfo(gpsLocationAdapter);
        return returnAdapter;
    }

    /**
     * Method that attempts to create a GpsLocationAdapter from properties present on the Event
     *
     * @return GpsLocationAdapter object representing current values on Event object
     */
    private GpsLocationAdapter getGpsInfoFromIEvent() {
        if (this._parentAdapter != null) {
            //Get lat and lon from parent event
            Double lat = this._parentAdapter.getLatitude() != null ? this._parentAdapter.getLatitude() : -1f, lon = this._parentAdapter.getLongitude() != null ? this._parentAdapter.getLongitude() : -1f;

            Date gpsTimestamp = this._parentAdapter.getGpsTimestamp();
            GpsLocationAdapter gpsLoc = new GpsLocationAdapter(this._parentAdapter, gpsTimestamp, lat.floatValue(), lon.floatValue());
            return gpsLoc;
        }
        return new GpsLocationAdapter(null);
    }

}
