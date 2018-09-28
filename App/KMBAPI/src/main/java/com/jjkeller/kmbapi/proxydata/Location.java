package com.jjkeller.kmbapi.proxydata;



public class Location extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String name = "";
	private GpsLocation gpsInfo = new GpsLocation();
	private float odometerReading = -1;
	private float endOdometerReading = -1;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public Location(){}
    
    public Location(String name)
    {
    	this.name = name;
    }
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GpsLocation getGpsInfo() {
		if(gpsInfo == null)
			gpsInfo = new GpsLocation();
		return gpsInfo;
	}

	public void setGpsInfo(GpsLocation gpsInfo) {
		this.gpsInfo = gpsInfo;
	}

	public float getOdometerReading() {
		return odometerReading;
	}

	public void setOdometerReading(float odometerReading) {
		this.odometerReading = odometerReading;
	}

	public float getEndOdometerReading() {
		return endOdometerReading;
	}

	public void setEndOdometerReading(float endOdometerReading) {
		this.endOdometerReading = endOdometerReading;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

	/// <summary>
    /// Answer the string to use for display of Location info.
    /// If the user-entered location name exists, then use it.
    /// Otherwise, use the GPS to display the lat/long coordinates.
    /// </summary>
    /// <returns></returns>
    public String ToLocationString()
    {
        String answer = null;

        if (this.getGpsInfo() != null && !this.getGpsInfo().IsEmpty() && !this.getGpsInfo().getDecodedInfo().IsEmpty())
        {
            // first, there is a GPS point available,and it's been decoded
            answer = this.getGpsInfo().ToLocationString();
        }
        else if (this.getName() != null && !this.getName().equals(""))
        {
            // second, there GPS point has not been decoded, but a user-enter location is available
            answer = this.getName();
        }

        // TCH - 7/15/2010 - No longer display the gps coordinates as the location,
        // if we cannot reverse geocode the coordinates, the user will be prompted
        // to enter a location
        //else if (this.GpsInfo != null && !this.GpsInfo.IsEmpty() && this.GpsInfo.DecodedInfo.IsEmpty())
        //{
        //    // lastly, there is no user-entered location, but there is
        //    // a non-decoded GPS point, use the coords of the point
        //    answer = this.GpsInfo.ToLocationString();
        //}

        return answer;
    }
	
    /// <summary>
    /// Answer if the location is empty. 
    /// This is empty when both the name and the GPS info are not available.
    /// </summary>
    /// <returns></returns>
    public boolean IsEmpty()
    {
        // only empty when the name is empty
    	boolean isEmpty = true;

        if (name != null && !name.equals(""))
        {
            // the name is defined, so the location is not empty
            isEmpty = false;
        }

        // TCH - 7/15/2010 - Location is deemed empty if no manual entry and no 
        // gps decoded value
        if (isEmpty && gpsInfo != null && !gpsInfo.IsEmpty() && gpsInfo.getDecodedInfo() != null && !gpsInfo.getDecodedInfo().IsEmpty())
        {
            // the gps info is valid, so the location is not empty
            isEmpty = false;
        }

        return isEmpty;
    }
}
