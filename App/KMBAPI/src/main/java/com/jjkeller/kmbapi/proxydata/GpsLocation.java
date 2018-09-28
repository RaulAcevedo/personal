package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.kmbeobr.GpsFix;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class GpsLocation extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private Date timestampUtc = null;
	private float latitudeDeg = -1;
	private float longitudeDeg = -1;
	private DecodedLocation decodedInfo = new DecodedLocation();
	
    private static final char NORTH = 'N';
    private static final char SOUTH = 'S';
    private static final char EAST = 'E';
    private static final char WEST = 'W';

    private char lonDir;
    private char latDir;

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public GpsLocation(){}
    
    /// <summary>
    /// Construct a GPS location given the lat/long in decimal degree format.
    /// Note: west and south directions are negative.
    /// </summary>
    /// <param name="latitudeDecimalDegrees"></param>
    /// <param name="longitudeDecimalDegrees"></param>
   public GpsLocation(float latitudeDecimalDegrees, float longitudeDecimalDegrees)
    {
    	this(null, latitudeDecimalDegrees, longitudeDecimalDegrees);
    }

   /// <summary>
   /// Construct a GPS location given the lat/long in decimal degree format.
   /// Note: west and south directions are negative.
   /// </summary>
   /// <param name="timestampUtc"></param>
   /// <param name="latitudeDecimalDegrees"></param>
   /// <param name="longitudeDecimalDegrees"></param>
   public GpsLocation(Date timestampUtc, float latitudeDecimalDegrees, float longitudeDecimalDegrees)
   {
       this.timestampUtc = timestampUtc;
       this.latitudeDeg = latitudeDecimalDegrees;
       this.longitudeDeg = longitudeDecimalDegrees;
   }

   /// <summary>
   /// Construct the GPS location, as long at the timestamp is not null.
   /// Expected direction values are 'N', 'S', 'E' and 'W'.
   /// The lat/long degrees must be positive decimal values.
   /// If the lat/long degrees or direction is not expected, then an exception is thrown.
   /// Note:  This constuctor is intended to be create a GPS location from the
   /// info passed in from the EOBR status record
   /// </summary>
   /// <exception cref="System.ArgumentException">thrown when one of the arguments is not valid</exception>
   /// <param name="timestamp">UTC of the GPS satellite</param>
   /// <param name="latitudeDegrees">latitude degrees</param>
   /// <param name="latitudeDirection">latitude direction.  Valid values are 'N', or 'S'</param>
   /// <param name="longitudeDegrees">longitude degrees</param>
   /// <param name="longitudeDirection">longitude direction.  Valid values are 'E', or 'W'</param>
   public GpsLocation(Date timestamp, float latitudeDegrees, char latitudeDirection, float longitudeDegrees, char longitudeDirection)
   {
       if (timestamp != null)
       {
           latDir = Character.toUpperCase(latitudeDirection);
           lonDir = Character.toUpperCase(longitudeDirection);

           latitudeDegrees = Math.abs(latitudeDegrees);
           longitudeDegrees =Math.abs(longitudeDegrees);
           // verify that each value makes sense
           if ((latDir == NORTH || latDir == SOUTH) && (lonDir == EAST || lonDir == WEST) && latitudeDegrees != 0.0F && longitudeDegrees != 0.0F)
           {
               this.timestampUtc = timestamp;
               this.latitudeDeg = ToDecimalDegrees(latitudeDegrees, latDir);
               this.longitudeDeg = ToDecimalDegrees(longitudeDegrees, lonDir);
           }
           else
           {
               // at least of the parms is the wrong format
               throw new IllegalArgumentException(String.format("Unexpected GPS value found - timestamp: '%s' lat: '%f' latdir: '%s' long: '%f' longdir: '%s'", timestamp.toString(), latitudeDegrees, latitudeDirection, longitudeDegrees, longitudeDirection));
           }
       }
   }

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public Date getTimestampUtc() {
		return timestampUtc;
	}

	public void setTimestampUtc(Date timestampUtc) {
		this.timestampUtc = timestampUtc;
	}

	public float getLatitudeDegrees() {
		return latitudeDeg;
	}

	public void setLatitudeDegrees(float latitudeDegrees) {
		this.latitudeDeg = latitudeDegrees;
	}

	public float getLongitudeDegrees() {
		return longitudeDeg;
	}

	public void setLongitudeDegrees(float longitudeDegrees) {
		this.longitudeDeg = longitudeDegrees;
	}

	public DecodedLocation getDecodedInfo() {
		if(decodedInfo == null)
			decodedInfo = new DecodedLocation();
		return decodedInfo;
	}

	public void setDecodedInfo(DecodedLocation decodedInfo) {
		this.decodedInfo = decodedInfo;
	}

    public Character getLonDirection()
    {
        return this.lonDir;
    }
    public Character getLatDirection()
    {
        return this.latDir;
    }


	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

	/// <summary>
    /// Answer the string representation of the GPS coords to use for
    /// location entry in KellerMobile.
    /// The resulting format will be dd°mm'ss"D
    ///         where dd - degrees, mm - minutes, ss - seconds, D - direction
    /// </summary>
    /// <returns></returns>
    public String ToLocationString()
    {
        StringBuilder answer = new StringBuilder();
        if (!this.IsEmpty())
        {
            if (!this.getDecodedInfo().IsEmpty())
            {
                // this GPS location has already been decoded, use it
                answer.append(this.getDecodedInfo().LocationString());
            }
            else
            {
                // note: west and south directions will be negative degrees
                this.ToDMS(answer, this.getLatitudeDegrees(), this.getLatitudeDegrees() > 0 ? NORTH : SOUTH);
                answer.append(" ");
                this.ToDMS(answer, this.getLongitudeDegrees(), this.getLongitudeDegrees() < 0 ? WEST : EAST);
            }
        }
        return answer.toString();
    }

    /// <summary>
    /// Answer the DMS string for the decimalDegree value.   
    /// The resulting format will be dd°mm'ss"D
    ///         where dd - degrees, mm - minutes, ss - seconds, D - direction
    /// </summary>
    private void ToDMS(StringBuilder sb, float decimalDegrees, char direction)
    {
        float decimalDegreeValue = Math.abs(decimalDegrees);
        int degrees = (int)decimalDegreeValue;
        sb.append(degrees);
        sb.append('°');

        float remainder = decimalDegreeValue - degrees;
        float totalSeconds = remainder * 3600.0F;
        int minutes = (int)(totalSeconds / 60.0F);
        float seconds = totalSeconds - (minutes * 60.0F);

        if (seconds > 0.0F)
        {
            // both seconds and minutes
            sb.append(minutes);
            sb.append('\'');
            NumberFormat numberFormat = new DecimalFormat("#,###.00");
            sb.append(numberFormat.format(seconds));
            sb.append('"');
        }
        else if (minutes > 0)
        {
            // minutes only
            sb.append(minutes);
            sb.append('\'');
        }

        sb.append(direction);
    }
    
    /// <summary>
    /// Answer the decimal degree value of the direction.
    /// Note: West and South degree values are negative
    /// </summary>
    /// <param name="val"></param>
    /// <param name="direction"></param>
    /// <returns></returns>
    private static float ToDecimalDegrees(float val, char direction)
    {
        float answer = val;
        char dir = Character.toUpperCase(direction);
        if (val > 0)
        {
            if (dir == WEST || dir == SOUTH)
            {
                // west and south direction are negative
                answer = answer * -1;
            }
        }
        return answer;
    }

    /// <summary>
    /// Answer if the GPS info is empty. 
    /// </summary>
    /// <returns></returns>
    public boolean IsEmpty()
    {
        // only empty when the timestamp has not been set yet
        return timestampUtc == null;
    }
    
    public static GpsLocation FromGpsFix(GpsFix gpsFix)
    {
    	return new GpsLocation(gpsFix.getTimecodeAsDate(), gpsFix.getLatitude(), gpsFix.getLongitude());
    }

}
