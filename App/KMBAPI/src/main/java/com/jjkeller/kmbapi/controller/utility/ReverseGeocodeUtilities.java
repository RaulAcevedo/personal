package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.GpsLocation;

import java.util.Collections;
import java.util.Date;

public class ReverseGeocodeUtilities {

	private static float RADIUS = 6371;  // earth's radius in KM

	public static double GetDistanceBetweenPoints(double curLat, double curLon, double foundLat, double foundLon)
	{
		double dLat = Math.toRadians(curLat - foundLat);
		double dLon = Math.toRadians(curLon - foundLon);
			
		double lat1 = Math.toRadians(foundLat);
		double lat2 = Math.toRadians(curLat);
			
		double a = Math.sin(dLat /2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			
		double distance = RADIUS * c;

		return distance;
	}
	
	public static String GetBearing(double curLat, double curLon, double foundLat, double foundLon)
	{
		double dLon = Math.toRadians(curLon - foundLon);
		double lat1 = Math.toRadians(foundLat);
		double lat2 = Math.toRadians(curLat);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		
		return ConvertBearingToDirection(bearing);
	}

	public static boolean eventIsMissingGeolocation(EmployeeLogEldEvent event) {
		return event != null && event.getGeolocation() == null && event.getLatitude() != null && event.getLongitude() != null;
	}

	public static String getGeolocationFromEventData(Date timestampUtc, Double latitudeDegrees, Double longitudeDegrees) {
		GpsLocation gpsLocation = new GpsLocation(timestampUtc, latitudeDegrees.floatValue(), longitudeDegrees.floatValue());
		IAPIController empLogController = MandateObjectFactory
				.getInstance(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getFeatureService())
				.getCurrentEventController();

		empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
		return gpsLocation.ToLocationString();
	}

	private static String ConvertBearingToDirection(double bearing)
	{
		String bearingStr = "";
		if (bearing >= -15 && bearing <= 15)
			bearingStr = "N";
		else if (bearing > 15 && bearing < 75)
			bearingStr = "NE";
		else if (bearing >= 75 && bearing <= 105)
			bearingStr = "E";
		else if (bearing > 105 && bearing < 165)
			bearingStr = "SE";
		else if (bearing >= 165 || bearing <= -165)
			bearingStr = "S";
		else if (bearing > -165 && bearing < -105)
			bearingStr = "SW";
		else if (bearing >= -105 && bearing <= -75)
			bearingStr = "W";
		else if (bearing > -75 && bearing < -15)
			bearingStr = "NW";

		return bearingStr;
	}
}
