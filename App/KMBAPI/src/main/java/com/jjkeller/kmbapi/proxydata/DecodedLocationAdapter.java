package com.jjkeller.kmbapi.proxydata;

/**
 * Class that extends DecodedLocation and allows for the translation of DecodedLocation properties to
 * Event properties
 */
public class DecodedLocationAdapter extends DecodedLocation {
    /**
     * Event that we want to translate properties to and from. Will be parent of LocationAdapter
     * object that contains this object
     */
    protected transient EmployeeLogEldEvent _parentAdapter = null;

    /**
     * Constructor that accepts an Event to translate properties to and from
     *
     * @param parentAdapter Event to translate
     */
    public DecodedLocationAdapter(EmployeeLogEldEvent parentAdapter) {
        super();
        this.setInternalParentAdapter(parentAdapter);
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
     * Method that accepts an event to pass into the constructor of a DecodedLocationAdapter object
     * as it's parent, and a DecodedLocation object to transform into an Adapter
     *
     * @param adapter  Event to translate properties to and from
     * @param location DecodedLocation object to pull values from and assign to
     * @return DecodedLocationAdapter representing given DecodedLocation parameter
     */
    public static final DecodedLocationAdapter getAdapterFromBase(EldEventAdapter adapter, DecodedLocation location) {
        DecodedLocationAdapter returnAdapter = new DecodedLocationAdapter(adapter._event);
        if (location != null) {
            returnAdapter.setCity(location.getCity());
            returnAdapter.setCountry(location.getCountry());
            returnAdapter.setCounty(location.getCounty());
            returnAdapter.setPostalCode(location.getPostalCode());
            returnAdapter.setState(location.getState());
            returnAdapter.setStreet(location.getStreet());
            if (location.isPrimaryKeySet())
                returnAdapter.setPrimaryKey(location.getPrimaryKey());
            //Sync the location string after assigning properties
            returnAdapter.syncLocationString();
        }
        return returnAdapter;
    }

            /*
 The following block of methods are overrides of the DecodedLocation base class's getters and setters.
 This block defines how we will use the EmployeeLogEldEvent object to store values for the
 DecodedLocation object. Further, it defines how we will construct object(s) that are present on a
DecodedLocation but not on an EmployeeLogEldEvent, given the properties we have.

Overrides requiring additional explanation will have method-level documentation as well
  */

    @Override
    public void setCity(String city) {
        super.setCity(city);
        syncLocationString();
    }

    @Override
    public void setState(String state) {
        super.setState(state);
        syncLocationString();
    }

    @Override
    public void setPostalCode(String postalCode) {
        super.setPostalCode(postalCode);
        syncLocationString();
    }

    @Override
    public void setStreet(String street) {
        super.setStreet(street);
        syncLocationString();
    }

    @Override
    public void setCounty(String county) {
        super.setCounty(county);
        syncLocationString();
    }

    @Override
    public void setCountry(String country) {
        super.setCountry(country);
        syncLocationString();
    }

    @Override
    public String LocationString() {
        syncLocationString();
        String returnString = super.LocationString();
        if (returnString == null || returnString.equals("")) {
            if (this._parentAdapter != null)
                returnString = this._parentAdapter.getGeolocation();
        }
        return returnString;
    }

    @Override
    public boolean IsEmpty() {
        syncLocationString();
        boolean isEmpty = super.IsEmpty();
        if (isEmpty)
            isEmpty = (this._parentAdapter == null || (this._parentAdapter.getGeolocation() == null || this._parentAdapter.getGeolocation().equals("")));
        return isEmpty;
    }

    /**
     * Method that syncs the current Geocoded location with the Geolocation property on the parent
     * event. Ensures that once we have viable data, we persist it to the correct property/(ies)
     */
    private void syncLocationString() {
        if (this._parentAdapter != null) {
            String currentLocString = super.LocationString();
            //If the internal location isn't blank
            if (!currentLocString.equals("")) {
                this._parentAdapter.setGeolocation(currentLocString);
            }
        }
    }
}
