package com.jjkeller.kmbapi.proxydata;

public class DecodedLocation extends ProxyBase {
	
	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String city = "";
    private String state = "";
    private String postalCode = "";
    private String street = "";
    private String county = "";
    private String country = "";
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

    public static DecodedLocation FromLocation(String locationString)
    {
        DecodedLocation loc = new DecodedLocation();
        if (locationString != null && locationString.compareTo("") != 0)
        {
            if (locationString.indexOf(',') > 0)
            {
                String[] tokens = locationString.split(",");
                loc.setCity(tokens[0].trim());
                loc.setState(tokens[1].trim());
            }
            else
            {
                loc.setPostalCode(locationString);
            }
        }
        return loc;
    }
    
	public String LocationString()
	{
        if (this.city != null && this.city.compareTo("") != 0 && this.state != null && this.state.compareTo("") != 0)
            return this.city + ", " + this.state;
        else if (this.postalCode != null && this.postalCode.compareTo("") != 0)
            return this.postalCode;
        else
            return "";
	}

    public boolean IsEmpty()
    {
    	return ((city == null || city.length() == 0) && (state == null || state.length() == 0))
    			&& (postalCode == null || postalCode.length() == 0);
    }
}
