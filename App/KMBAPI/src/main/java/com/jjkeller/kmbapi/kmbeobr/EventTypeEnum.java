package com.jjkeller.kmbapi.kmbeobr;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.enums.EnumBase;

import java.util.List;
import java.util.Arrays;

public class EventTypeEnum extends EnumBase {

	public static final int ANYTYPE = 0;
	public static final int VEHICLESTOPPED = 1;
	public static final int IGNITIONOFF = 2;
	public static final int IGNITIONON = 3;
	public static final int LOSSOFTABPOWER = 4;
	public static final int TABRESET = 5;
	public static final int EXITSLEEPMODE = 6;
	public static final int ENTERSLEEPMODE = 7;
	public static final int TABHARDWAREFAULT = 8;
	public static final int DTCSTATUSCHANGE = 9;
	public static final int MILOFF = 10;
	public static final int MILON = 11;
	public static final int RPMOVERTHRESHOLD = 12;
	public static final int RPMUNDERTHRESHOLD = 13;
	public static final int SPEEDOVERTHRESHOLD = 14;
	public static final int SPEEDUNDERTHRESHOLD = 15;
	public static final int HARDBRAKE = 16;
	public static final int DRIVESTART = 17;
	public static final int DRIVEEND = 18;
	public static final int PTOON = 19;
	public static final int PTOOFF = 20;
	public static final int DRIVER = 21;
	public static final int MOVE = 22;
	public static final int ERROR = 23;
	public static final int GPS = 24;
	public static final int HOURLYTRIPRECORD = 90;
	public static final int ROUTEPOSITION = 91;
	public static final int HARDBRAKESURROUNDING_NOTUSED = 92; // this can be renamed/reused.
	public static final int MAPPOSITION = 93;
	
	private static final String DmoEnum_ANYTYPE = "ANYTYPE";
    private static final String DmoEnum_VEHICLESTOPPED = "VEHICLESTOPPED";
    private static final String DmoEnum_IGNITIONOFF = "IGNITIONOFF";
    private static final String DmoEnum_IGNITIONON = "IGNITIONON";
    private static final String DmoEnum_LOSSOFTABPOWER = "LOSSOFTABPOWER";
    private static final String DmoEnum_TABRESET = "TABRESET";
    private static final String DmoEnum_EXITSLEEPMODE = "EXITSLEEPMODE";
    private static final String DmoEnum_ENTERSLEEPMODE = "ENTERSLEEPMODE";
    private static final String DmoEnum_TABHARDWAREFAULT = "TABHARDWAREFAULT";
    private static final String DmoEnum_DTCSTATUSCHANGE = "DTCSTATUSCHANGE";
    private static final String DmoEnum_MILOFF = "MILOFF";
    private static final String DmoEnum_MILON = "MILON";
    private static final String DmoEnum_RPMOVERTHRESHOLD = "RPMOVERTHRESHOLD";
    private static final String DmoEnum_RPMUNDERTHRESHOLD = "RPMUNDERTHRESHOLD";
    private static final String DmoEnum_SPEEDOVERTHRESHOLD = "SPEEDOVERTHRESHOLD";
    private static final String DmoEnum_SPEEDUNDERTHRESHOLD = "SPEEDUNDERTHRESHOLD";
    private static final String DmoEnum_HARDBRAKE = "HARDBRAKE";
    private static final String DmoEnum_DRIVESTART = "DRIVESTART";
    private static final String DmoEnum_DRIVEEND = "DRIVEEND";
    private static final String DmoEnum_PTOON = "PTOON";
    private static final String DmoEnum_PTOOFF = "PTOOFF";
    private static final String DmoEnum_DRIVER = "DRIVER";
	private static final String DmoEnum_MOVE = "MOVE";
	private static final String DmoEnum_ERROR = "ERROR";
	private static final String DmoEnum_GPS = "GPS";
    private static final String DmoEnum_HOURLYTRIPRECORD = "HOURLYTRIPRECORD";
    private static final String DmoEnum_ROUTEPOSITION = "ROUTEPOSITION";
    private static final String DmoEnum_MAPPOSITION =  "MAPPOSITION";

    private static List<Integer> eventTypeEnumList = Arrays.asList(VEHICLESTOPPED, IGNITIONOFF, IGNITIONON, LOSSOFTABPOWER, TABRESET, EXITSLEEPMODE, ENTERSLEEPMODE,
				TABHARDWAREFAULT, DTCSTATUSCHANGE, MILOFF, MILON, RPMOVERTHRESHOLD, RPMUNDERTHRESHOLD, SPEEDOVERTHRESHOLD, SPEEDUNDERTHRESHOLD,
				HARDBRAKE, DRIVESTART, DRIVEEND, PTOON, PTOOFF, DRIVER, MOVE, ERROR, GPS, HOURLYTRIPRECORD, ROUTEPOSITION, MAPPOSITION);

    public static final int ARRAYID = R.array.eventtype_array;

	public EventTypeEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
	    	case ANYTYPE:
	    	case VEHICLESTOPPED:
	    	case IGNITIONOFF:
	    	case IGNITIONON:
	    	case LOSSOFTABPOWER:
	    	case TABRESET:
	    	case EXITSLEEPMODE:
	    	case ENTERSLEEPMODE:
	    	case TABHARDWAREFAULT:
	    	case DTCSTATUSCHANGE:
	    	case MILOFF:
	    	case MILON:
	    	case RPMOVERTHRESHOLD:
	    	case RPMUNDERTHRESHOLD:
	    	case SPEEDOVERTHRESHOLD:
	    	case SPEEDUNDERTHRESHOLD:
	    	case HARDBRAKE:
	    	case DRIVESTART:
	    	case DRIVEEND:
	    	case PTOON:
	    	case PTOOFF:
	    	case DRIVER:
	    	case MOVE:
			case ERROR:
			case GPS:
	    	case HOURLYTRIPRECORD:
	    	case ROUTEPOSITION:
	    	case MAPPOSITION:
	    		this.value = value;
	    		break;
    		default:
    			super.setValue(value);
    	}
    }

    @Override
	public String toDMOEnum() {
    	if(value == ANYTYPE)
    		return DmoEnum_ANYTYPE;
    	else if(value == VEHICLESTOPPED)
    		return DmoEnum_VEHICLESTOPPED;
    	else if(value == IGNITIONOFF)
    		return DmoEnum_IGNITIONOFF;
    	else if(value == IGNITIONON)
    		return DmoEnum_IGNITIONON;
    	else if(value == LOSSOFTABPOWER)
    		return DmoEnum_LOSSOFTABPOWER;
    	else if(value == TABRESET)
    		return DmoEnum_TABRESET;
    	else if(value == EXITSLEEPMODE)
    		return DmoEnum_EXITSLEEPMODE;
    	else if(value == ENTERSLEEPMODE)
    		return DmoEnum_ENTERSLEEPMODE;
    	else if(value == TABHARDWAREFAULT)
    		return DmoEnum_TABHARDWAREFAULT;
    	else if(value == DTCSTATUSCHANGE)
    		return DmoEnum_DTCSTATUSCHANGE;
    	else if(value == MILOFF)
    		return DmoEnum_MILOFF;
    	else if(value == MILON)
    		return DmoEnum_MILON;
    	else if(value == RPMOVERTHRESHOLD)
    		return DmoEnum_RPMOVERTHRESHOLD;
    	else if(value == RPMUNDERTHRESHOLD)
    		return DmoEnum_RPMUNDERTHRESHOLD;
    	else if(value == SPEEDOVERTHRESHOLD)
    		return DmoEnum_SPEEDOVERTHRESHOLD;
    	else if(value == SPEEDUNDERTHRESHOLD)
    		return DmoEnum_SPEEDUNDERTHRESHOLD;
    	else if(value == HARDBRAKE)
    		return DmoEnum_HARDBRAKE;
    	else if(value == DRIVESTART)
    		return DmoEnum_DRIVESTART;
    	else if(value == DRIVEEND)
    		return DmoEnum_DRIVEEND;
    	else if(value == PTOON)
    		return DmoEnum_PTOON;
    	else if(value == PTOOFF)
    		return DmoEnum_PTOOFF;
    	else if (value == DRIVER)
    		return DmoEnum_DRIVER;
    	else if (value == MOVE)
    		return DmoEnum_MOVE;
		else if (value == ERROR)
			return DmoEnum_ERROR;
		else if (value == GPS)
			return DmoEnum_GPS;
    	else if(value == HOURLYTRIPRECORD)
    		return 	DmoEnum_HOURLYTRIPRECORD;
    	else if(value == ROUTEPOSITION)
    		return DmoEnum_ROUTEPOSITION;
    	else if (value == MAPPOSITION)
    		return DmoEnum_MAPPOSITION;
    	else
    		return DmoEnum_ANYTYPE;
	}

	@Override
	protected int getArrayId() {
		return 0;
		//return ARRAYID;
	}

	public EventTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(0);//(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new EventTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static EventTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_ANYTYPE) == 0)
    		return new EventTypeEnum(ANYTYPE);
    	else if(name.compareTo(DmoEnum_VEHICLESTOPPED) == 0)
    		return new EventTypeEnum(VEHICLESTOPPED);
    	else if(name.compareTo(DmoEnum_IGNITIONOFF) == 0)
    		return new EventTypeEnum(IGNITIONOFF);
    	else if(name.compareTo(DmoEnum_IGNITIONON) == 0)
    		return new EventTypeEnum(IGNITIONON);
    	else if(name.compareTo(DmoEnum_LOSSOFTABPOWER) == 0)
    		return new EventTypeEnum(LOSSOFTABPOWER);
    	else if(name.compareTo(DmoEnum_TABRESET) == 0)
    		return new EventTypeEnum(TABRESET);
    	else if(name.compareTo(DmoEnum_EXITSLEEPMODE) == 0)
    		return new EventTypeEnum(EXITSLEEPMODE);
    	else if(name.compareTo(DmoEnum_ENTERSLEEPMODE) == 0)
    		return new EventTypeEnum(ENTERSLEEPMODE);
    	else if(name.compareTo(DmoEnum_TABHARDWAREFAULT) == 0)
    		return new EventTypeEnum(TABHARDWAREFAULT);
    	else if(name.compareTo(DmoEnum_DTCSTATUSCHANGE) == 0)
    		return new EventTypeEnum(DTCSTATUSCHANGE);
    	else if(name.compareTo(DmoEnum_MILOFF) == 0)
    		return new EventTypeEnum(MILOFF);
    	else if(name.compareTo(DmoEnum_MILON) == 0)
    		return new EventTypeEnum(MILON);
    	else if(name.compareTo(DmoEnum_RPMOVERTHRESHOLD) == 0)
    		return new EventTypeEnum(RPMOVERTHRESHOLD);
    	else if(name.compareTo(DmoEnum_RPMUNDERTHRESHOLD) == 0)
    		return new EventTypeEnum(RPMUNDERTHRESHOLD);
    	else if(name.compareTo(DmoEnum_SPEEDOVERTHRESHOLD) == 0)
    		return new EventTypeEnum(SPEEDOVERTHRESHOLD);
    	else if(name.compareTo(DmoEnum_SPEEDUNDERTHRESHOLD) == 0)
    		return new EventTypeEnum(SPEEDUNDERTHRESHOLD);
    	else if(name.compareTo(DmoEnum_HARDBRAKE) == 0)
    		return new EventTypeEnum(HARDBRAKE);
    	else if(name.compareTo(DmoEnum_DRIVESTART) == 0)
    		return new EventTypeEnum(DRIVESTART);
    	else if(name.compareTo(DmoEnum_DRIVEEND) == 0)
    		return new EventTypeEnum(DRIVEEND);
    	else if(name.compareTo(DmoEnum_PTOON) == 0)
    		return new EventTypeEnum(PTOON);
    	else if(name.compareTo(DmoEnum_PTOOFF) == 0)
    		return new EventTypeEnum(PTOOFF);
    	else if(name.compareTo(DmoEnum_DRIVER) == 0)
    		return new EventTypeEnum(DRIVER);
    	else if(name.compareTo(DmoEnum_MOVE) == 0)
    		return new EventTypeEnum(MOVE);
		else if(name.compareTo(DmoEnum_ERROR) == 0)
			return new EventTypeEnum(ERROR);
		else if(name.compareTo(DmoEnum_GPS) == 0)
			return new EventTypeEnum(GPS);
    	else if(name.compareTo(DmoEnum_HOURLYTRIPRECORD) == 0)
    		return new EventTypeEnum(HOURLYTRIPRECORD);
    	else if(name.compareTo(DmoEnum_ROUTEPOSITION) == 0)
    		return new EventTypeEnum(ROUTEPOSITION);
    	else if (name.compareTo(DmoEnum_MAPPOSITION) == 0)
    		return new EventTypeEnum(MAPPOSITION);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
	
	public String toStringDisplay(Context ctx)
	{
		if(value == ANYTYPE)
    		return ctx.getResources().getString(R.string.eventtype_anytype);
    	else if(value == VEHICLESTOPPED)
    		return ctx.getResources().getString(R.string.eventtype_vehiclestopped);
    	else if(value == IGNITIONOFF)
    		return ctx.getResources().getString(R.string.eventtype_ignitionoff);
    	else if(value == IGNITIONON)
    		return ctx.getResources().getString(R.string.eventtype_ignitionon);
    	else if(value == LOSSOFTABPOWER)
    		return ctx.getResources().getString(R.string.eventtype_lossoftabpower);
    	else if(value == TABRESET)
    		return ctx.getResources().getString(R.string.eventtype_tabreset);
    	else if(value == EXITSLEEPMODE)
    		return ctx.getResources().getString(R.string.eventtype_exitsleepmode);
    	else if(value == ENTERSLEEPMODE)
    		return ctx.getResources().getString(R.string.eventtype_entersleepmode);
    	else if(value == TABHARDWAREFAULT)
    		return ctx.getResources().getString(R.string.eventtype_tabhardwarefault);
    	else if(value == DTCSTATUSCHANGE)
    		return ctx.getResources().getString(R.string.eventtype_dtcstatuschange);
    	else if(value == MILOFF)
    		return ctx.getResources().getString(R.string.eventtype_miloff);
    	else if(value == MILON)
    		return ctx.getResources().getString(R.string.eventtype_milon);
    	else if(value == RPMOVERTHRESHOLD)
    		return ctx.getResources().getString(R.string.eventtype_rpmoverthreshold);
    	else if(value == RPMUNDERTHRESHOLD)
    		return ctx.getResources().getString(R.string.eventtype_rpmunderthreshold);
    	else if(value == SPEEDOVERTHRESHOLD)
    		return ctx.getResources().getString(R.string.eventtype_speedoverthreshold);
    	else if(value == SPEEDUNDERTHRESHOLD)
    		return ctx.getResources().getString(R.string.eventtype_speedunderthreshold);
    	else if(value == HARDBRAKE)
    		return ctx.getResources().getString(R.string.eventtype_hardbrake);
    	else if(value == DRIVESTART)
    		return ctx.getResources().getString(R.string.eventtype_drivestart);
    	else if(value == DRIVEEND)
    		return ctx.getResources().getString(R.string.eventtype_driveend);
    	else if(value == PTOON)
    		return ctx.getResources().getString(R.string.eventtype_ptoon);
    	else if(value == PTOOFF)
    		return ctx.getResources().getString(R.string.eventtype_ptooff);
    	else if (value == DRIVER)
    		return ctx.getResources().getString(R.string.eventtype_driver);
    	else if (value == MOVE)
    		return ctx.getResources().getString(R.string.eventtype_move);
		else if (value == ERROR)
			return ctx.getResources().getString(R.string.eventtype_error);
		else if (value == GPS)
			return ctx.getResources().getString(R.string.eventtype_gps);
    	else if(value == HOURLYTRIPRECORD)
    		return ctx.getResources().getString(R.string.eventtype_hourlytriprecord);
    	else if(value == ROUTEPOSITION)
    		return ctx.getResources().getString(R.string.eventtype_routeposition);
    	else if (value == MAPPOSITION)
    		return ctx.getResources().getString(R.string.eventtype_mapposition);
    	else
    		return ctx.getResources().getString(R.string.eventtype_anytype);
	}

	public List getEnumList(){ return eventTypeEnumList; }
}
