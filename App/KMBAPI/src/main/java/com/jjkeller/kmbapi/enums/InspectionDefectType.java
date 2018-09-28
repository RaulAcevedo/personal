package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class InspectionDefectType extends EnumBase {

	public static final int NULL = 0;
	public static final int AIRCOMPRESSOR = 1;
	public static final int AIRLINES = 2;
	public static final int BATTERY = 3;
	public static final int BODY = 4;
	public static final int BRAKEACCESSORIES = 5;
	public static final int BRAKECONNECTIONS = 6;
	public static final int BRAKES = 7;
	public static final int BRAKESPARKING = 8;
	public static final int BRAKESSERVICE = 9;
	public static final int CLUTCH = 10;
	public static final int COUPLINGDEVICES = 11;
	public static final int COUPLINGKINGPIN = 12;
	public static final int DEFROSTERHEATER = 13;
	public static final int DOORS = 14;
	public static final int DRIVELINE = 15;
	public static final int ENGINE = 16;
	public static final int EXHAUST = 17;
	public static final int FIFTHWHEEL = 18;
	public static final int FRAMEANDASSEMBLY = 19;
	public static final int FRONTAXLE = 20;
	public static final int FUELTANK = 21;
	public static final int HITCH = 22;
	public static final int HORN = 23;
	public static final int LANDINGGEAR = 24;
	public static final int LIGHTSALL = 25;
	public static final int LIGHTSHEAD = 26;
	public static final int LIGHTSTAIL = 27;
	public static final int LIGHTSSTOP = 28;
	public static final int LIGHTSDASH = 29;
	public static final int LIGHTSTURNINDICATORS = 30;
	public static final int MIRRORS = 31;
	public static final int MUFFLER = 32;
	public static final int OILPRESSURE = 33;
	public static final int OTHER = 34;
	public static final int RADIATOR = 35;
	public static final int REAREND = 36;
	public static final int REFLECTORS = 37;
	public static final int ROOF = 38;
	public static final int SAFETYFIREEXTINGUISHER = 39;
	public static final int SAFETYREFLECTIVETRIANGLES = 40;
	public static final int SAFETYFLAGS = 41;
	public static final int SAFETYFLARES  = 42;
	public static final int SAFETYSPAREBULBFUSES = 43;
	public static final int SAFETYSPARESEALBEAM = 44;
	public static final int SUSPENSIONSYSTEM = 45;
	public static final int STARTER = 46;
	public static final int STEERING = 47;
	public static final int TACHOGRAPH = 48;
	public static final int TARPAULIN = 49;
	public static final int TIRES = 50;
	public static final int TIRECHAINS = 51;
	public static final int TRANSMISSION = 52;
	public static final int WHEELSANDRIMS = 53;
	public static final int WINDOWS = 54;
	public static final int WINDSHIELDWIPERS = 55;
	
    private static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_AirCompressor = "AirCompressor";
	public static final String DmoEnum_AirLines = "AirLines";
	public static final String DmoEnum_Battery = "Battery";
    public static final String DmoEnum_BODY = "Body";
	public static final String DmoEnum_BRAKEACCESSORIES = "BrakeAccessories";
	public static final String DmoEnum_BRAKECONNECTIONS = "BrakeConnections";
	public static final String DmoEnum_BRAKES = "Brakes";
	public static final String DmoEnum_BRAKESPARKING = "BrakesParking";
	public static final String DmoEnum_BRAKESSERVICE = "BrakesService";
	public static final String DmoEnum_Clutch = "Clutch";
	public static final String DmoEnum_CouplingDevices = "CouplingDevices";
	public static final String DmoEnum_CouplingKingPin = "CouplingKingPin";
	public static final String DmoEnum_DefrosterHeater = "DefrosterHeater";
	public static final String DmoEnum_Doors = "Doors";
	public static final String DmoEnum_DriveLine = "DriveLine";
	public static final String DmoEnum_Engine = "Engine";
	public static final String DmoEnum_Exhaust = "Exhaust";
	public static final String DmoEnum_FifthWheel = "FifthWheel";
	public static final String DmoEnum_FrameAndAssembly = "FrameAndAssembly";
	public static final String DmoEnum_FrontAxle = "FrontAxle";
	public static final String DmoEnum_FuelTank = "FuelTank";
	public static final String DmoEnum_Hitch = "Hitch";
	public static final String DmoEnum_Horn = "Horn";
	public static final String DmoEnum_LandingGear = "LandingGear";
	public static final String DmoEnum_LightsAll = "LightsAll";
	public static final String DmoEnum_LightsHead = "LightsHead";
	public static final String DmoEnum_LightsTail = "LightsTail";
	public static final String DmoEnum_LightsStop = "LightsStop";
	public static final String DmoEnum_LightsDash = "LightsDash";
	public static final String DmoEnum_LightsTurnIndicators = "LightsTurnIndicators";
	public static final String DmoEnum_Mirrors = "Mirrors";
	public static final String DmoEnum_Muffler = "Muffler";
	public static final String DmoEnum_OilPressure = "OilPressure";
	public static final String DmoEnum_Other = "Other";
	public static final String DmoEnum_Radiator = "Radiator";
	public static final String DmoEnum_RearEnd = "RearEnd";
	public static final String DmoEnum_Reflectors = "Reflectors";
	public static final String DmoEnum_Roof = "Roof";
	public static final String DmoEnum_SafetyFireExtinguisher = "SafetyFireExtinguisher";
	public static final String DmoEnum_SafetyReflectiveTriangles = "SafetyReflectiveTriangles";
	public static final String DmoEnum_SafetyFlags = "SafetyFlags";
	public static final String DmoEnum_SafetyFlaresFusees = "SafetyFlaresFusees";
	public static final String DmoEnum_SafetySpareBulbFuses = "SafetySpareBulbFuses";
	public static final String DmoEnum_SafetySpareSealBeam = "SafetySpareSealBeam";
	public static final String DmoEnum_SuspensionSystem = "SuspensionSystem";
	public static final String DmoEnum_Starter = "Starter";
	public static final String DmoEnum_Steering = "Steering";
	public static final String DmoEnum_Tachograph = "Tachograph";
	public static final String DmoEnum_Tarpaulin = "Tarpaulin";
	public static final String DmoEnum_Tires = "Tires";
	public static final String DmoEnum_TireChains = "TireChains";
	public static final String DmoEnum_Transmission = "Transmission";
	public static final String DmoEnum_WheelsAndRims = "WheelsAndRims";
	public static final String DmoEnum_Windows = "Windows";
	public static final String DmoEnum_WindshieldWipers = "WindshieldWipers";
	
	public static final int ARRAYID = R.array.inspectiondefect_array;

	public InspectionDefectType(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
    		case NULL:
	    	case AIRCOMPRESSOR:
	    	case AIRLINES:
	    	case BATTERY:
	    	case BODY:
	    	case BRAKEACCESSORIES:
	    	case BRAKECONNECTIONS:
	    	case BRAKES:
	    	case BRAKESPARKING:
	    	case BRAKESSERVICE:
	    	case CLUTCH:
	    	case COUPLINGDEVICES:
	    	case COUPLINGKINGPIN:
	    	case DEFROSTERHEATER:
	    	case DOORS:
	    	case DRIVELINE:
	    	case ENGINE:
	    	case EXHAUST:
	    	case FIFTHWHEEL:
	    	case FRAMEANDASSEMBLY:
	    	case FRONTAXLE:
	    	case FUELTANK:
	    	case HITCH:
	    	case HORN:
	    	case LANDINGGEAR:
	    	case LIGHTSALL:
	    	case LIGHTSHEAD:
	    	case LIGHTSTAIL:
	    	case LIGHTSSTOP:
	    	case LIGHTSDASH:
	    	case LIGHTSTURNINDICATORS:
	    	case MIRRORS:
	    	case MUFFLER:
	    	case OILPRESSURE:
	    	case OTHER:
	    	case RADIATOR:
	    	case REAREND:
	    	case REFLECTORS:
	    	case ROOF:
	    	case SAFETYFIREEXTINGUISHER:
	    	case SAFETYREFLECTIVETRIANGLES:
	    	case SAFETYFLAGS:
	    	case SAFETYFLARES:
	    	case SAFETYSPAREBULBFUSES:
	    	case SAFETYSPARESEALBEAM:
	    	case SUSPENSIONSYSTEM:
	    	case STARTER:
	    	case STEERING:
	    	case TACHOGRAPH:
	    	case TARPAULIN:
	    	case TIRES:
	    	case TIRECHAINS:
	    	case TRANSMISSION:
	    	case WHEELSANDRIMS:
	    	case WINDOWS:
	    	case WINDSHIELDWIPERS:
	    		this.value = value;
	    		break;
    		default:
    			super.setValue(value);
    	}
    }

	@Override
	public String toDMOEnum()
	{
    	if(value == NULL)
    		return DmoEnum_Null;
    	else if(value == AIRCOMPRESSOR)
    		return DmoEnum_AirCompressor;
    	else if(value == AIRLINES)
    		return DmoEnum_AirLines;
    	else if(value == BATTERY)
    		return DmoEnum_Battery;
    	else if(value == BODY)
    		return DmoEnum_BODY;
    	else if(value == BRAKEACCESSORIES)
    		return DmoEnum_BRAKEACCESSORIES;
    	else if(value == BRAKECONNECTIONS)
    		return DmoEnum_BRAKECONNECTIONS;
    	else if(value == BRAKES)
    		return DmoEnum_BRAKES;
    	else if(value == BRAKESPARKING)
    		return DmoEnum_BRAKESPARKING;
    	else if(value == BRAKESSERVICE)
    		return DmoEnum_BRAKESSERVICE;
    	else if(value == CLUTCH)
    		return DmoEnum_Clutch;
    	else if(value == COUPLINGDEVICES)
    		return DmoEnum_CouplingDevices;
    	else if(value == COUPLINGKINGPIN)
    		return DmoEnum_CouplingKingPin;
    	else if(value == DEFROSTERHEATER)
    		return DmoEnum_DefrosterHeater;
    	else if(value == DOORS)
    		return DmoEnum_Doors;
    	else if(value == DRIVELINE)
    		return DmoEnum_DriveLine;
    	else if(value == ENGINE)
    		return DmoEnum_Engine;
    	else if(value == EXHAUST)
    		return DmoEnum_Exhaust;
    	else if(value == FIFTHWHEEL)
    		return DmoEnum_FifthWheel;
    	else if(value == FRAMEANDASSEMBLY)
    		return DmoEnum_FrameAndAssembly;
    	else if(value == FRONTAXLE)
    		return DmoEnum_FrontAxle;
    	else if(value == FUELTANK)
    		return DmoEnum_FuelTank;
    	else if(value == HITCH)
    		return DmoEnum_Hitch;
    	else if(value == HORN)
    		return DmoEnum_Horn;
    	else if(value == LANDINGGEAR)
    		return DmoEnum_LandingGear;
    	else if(value == LIGHTSALL)
    		return DmoEnum_LightsAll;
    	else if(value == LIGHTSHEAD)
    		return DmoEnum_LightsHead;
    	else if(value == LIGHTSTAIL)
    		return DmoEnum_LightsTail;
    	else if(value == LIGHTSSTOP)
    		return DmoEnum_LightsStop;
    	else if(value == LIGHTSDASH)
    		return DmoEnum_LightsDash;
    	else if(value == LIGHTSTURNINDICATORS)
    		return DmoEnum_LightsTurnIndicators;
    	else if(value == MIRRORS)
    		return DmoEnum_Mirrors;
    	else if(value == MUFFLER)
    		return DmoEnum_Muffler;
    	else if(value == OILPRESSURE)
    		return DmoEnum_OilPressure;
    	else if(value == OTHER)
    		return DmoEnum_Other;
    	else if(value == RADIATOR)
    		return DmoEnum_Radiator;
    	else if(value == REAREND)
    		return DmoEnum_RearEnd;
    	else if(value == REFLECTORS)
    		return DmoEnum_Reflectors;
    	else if(value == ROOF)
    		return DmoEnum_Roof;
    	else if(value == SAFETYFIREEXTINGUISHER)
    		return DmoEnum_SafetyFireExtinguisher;
    	else if(value == SAFETYREFLECTIVETRIANGLES)
    		return DmoEnum_SafetyReflectiveTriangles;
    	else if(value == SAFETYFLAGS)
    		return DmoEnum_SafetyFlags;
    	else if(value == SAFETYFLARES)
    		return DmoEnum_SafetyFlaresFusees;
    	else if(value == SAFETYSPAREBULBFUSES)
    		return DmoEnum_SafetySpareBulbFuses;
    	else if(value == SAFETYSPARESEALBEAM)
    		return DmoEnum_SafetySpareSealBeam;
    	else if(value == SUSPENSIONSYSTEM)
    		return DmoEnum_SuspensionSystem;
    	else if(value == STARTER)
    		return DmoEnum_Starter;
    	else if(value == STEERING)
    		return DmoEnum_Steering;
    	else if(value == TACHOGRAPH)
    		return DmoEnum_Tachograph;
    	else if(value == TARPAULIN)
    		return DmoEnum_Tarpaulin;
    	else if(value == TIRES)
    		return DmoEnum_Tires;
    	else if(value == TIRECHAINS)
    		return DmoEnum_TireChains;
    	else if(value == TRANSMISSION)
    		return DmoEnum_Transmission;
    	else if(value == WHEELSANDRIMS)
    		return DmoEnum_WheelsAndRims;
    	else if(value == WINDOWS)
    		return DmoEnum_Windows;
    	else if(value == WINDSHIELDWIPERS)
    		return DmoEnum_WindshieldWipers;
    	else		
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public InspectionDefectType valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new InspectionDefectType(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static InspectionDefectType valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new InspectionDefectType(NULL);
    	else if(name.compareTo(DmoEnum_AirCompressor) == 0)
    		return new InspectionDefectType(AIRCOMPRESSOR);
    	else if(name.compareTo(DmoEnum_AirLines) == 0)
    		return new InspectionDefectType(AIRLINES);
    	else if(name.compareTo(DmoEnum_Battery) == 0)
    		return new InspectionDefectType(BATTERY);
    	else if(name.compareTo(DmoEnum_BODY) == 0)
    		return new InspectionDefectType(BODY);
    	else if(name.compareTo(DmoEnum_BRAKEACCESSORIES) == 0)
    		return new InspectionDefectType(BRAKEACCESSORIES);
    	else if(name.compareTo(DmoEnum_BRAKECONNECTIONS) == 0)
    		return new InspectionDefectType(BRAKECONNECTIONS);
    	else if(name.compareTo(DmoEnum_BRAKES) == 0)
    		return new InspectionDefectType(BRAKES);
    	else if(name.compareTo(DmoEnum_BRAKESPARKING) == 0)
    		return new InspectionDefectType(BRAKESPARKING);
    	else if(name.compareTo(DmoEnum_BRAKESSERVICE) == 0)
    		return new InspectionDefectType(BRAKESSERVICE);
    	else if(name.compareTo(DmoEnum_Clutch) == 0)
    		return new InspectionDefectType(CLUTCH);
    	else if(name.compareTo(DmoEnum_CouplingDevices) == 0)
    		return new InspectionDefectType(COUPLINGDEVICES);
    	else if(name.compareTo(DmoEnum_CouplingKingPin) == 0)
    		return new InspectionDefectType(COUPLINGKINGPIN);
    	else if(name.compareTo(DmoEnum_DefrosterHeater) == 0)
    		return new InspectionDefectType(DEFROSTERHEATER);
    	else if(name.compareTo(DmoEnum_Doors) == 0)
    		return new InspectionDefectType(DOORS);
    	else if(name.compareTo(DmoEnum_DriveLine) == 0)
    		return new InspectionDefectType(DRIVELINE);
    	else if(name.compareTo(DmoEnum_Engine) == 0)
    		return new InspectionDefectType(ENGINE);
    	else if(name.compareTo(DmoEnum_Exhaust) == 0)
    		return new InspectionDefectType(EXHAUST);
    	else if(name.compareTo(DmoEnum_FifthWheel) == 0)
    		return new InspectionDefectType(FIFTHWHEEL);
    	else if(name.compareTo(DmoEnum_FrameAndAssembly) == 0)
    		return new InspectionDefectType(FRAMEANDASSEMBLY);
    	else if(name.compareTo(DmoEnum_FrontAxle) == 0)
    		return new InspectionDefectType(FRONTAXLE);
    	else if(name.compareTo(DmoEnum_FuelTank) == 0)
    		return new InspectionDefectType(FUELTANK);
    	else if(name.compareTo(DmoEnum_Hitch) == 0)
    		return new InspectionDefectType(HITCH);
    	else if(name.compareTo(DmoEnum_Horn) == 0)
    		return new InspectionDefectType(HORN);
    	else if(name.compareTo(DmoEnum_LandingGear) == 0)
    		return new InspectionDefectType(LANDINGGEAR);
    	else if(name.compareTo(DmoEnum_LightsAll) == 0)
    		return new InspectionDefectType(LIGHTSALL);
    	else if(name.compareTo(DmoEnum_LightsHead) == 0)
    		return new InspectionDefectType(LIGHTSHEAD);
    	else if(name.compareTo(DmoEnum_LightsTail) == 0)
    		return new InspectionDefectType(LIGHTSTAIL);
    	else if(name.compareTo(DmoEnum_LightsStop) == 0)
    		return new InspectionDefectType(LIGHTSSTOP);
    	else if(name.compareTo(DmoEnum_LightsDash) == 0)
    		return new InspectionDefectType(LIGHTSDASH);
    	else if(name.compareTo(DmoEnum_LightsTurnIndicators) == 0)
    		return new InspectionDefectType(LIGHTSTURNINDICATORS);
    	else if(name.compareTo(DmoEnum_Mirrors) == 0)
    		return new InspectionDefectType(MIRRORS);
    	else if(name.compareTo(DmoEnum_Muffler) == 0)
    		return new InspectionDefectType(MUFFLER);
    	else if(name.compareTo(DmoEnum_OilPressure) == 0)
    		return new InspectionDefectType(OILPRESSURE);
    	else if(name.compareTo(DmoEnum_Other) == 0)
    		return new InspectionDefectType(OTHER);
    	else if(name.compareTo(DmoEnum_Radiator) == 0)
    		return new InspectionDefectType(RADIATOR);
    	else if(name.compareTo(DmoEnum_RearEnd) == 0)
    		return new InspectionDefectType(REAREND);
    	else if(name.compareTo(DmoEnum_Reflectors) == 0)
    		return new InspectionDefectType(REFLECTORS);
    	else if(name.compareTo(DmoEnum_Roof) == 0)
    		return new InspectionDefectType(ROOF);
    	else if(name.compareTo(DmoEnum_SafetyFireExtinguisher) == 0)
    		return new InspectionDefectType(SAFETYFIREEXTINGUISHER);
    	else if(name.compareTo(DmoEnum_SafetyReflectiveTriangles) == 0)
    		return new InspectionDefectType(SAFETYREFLECTIVETRIANGLES);
    	else if(name.compareTo(DmoEnum_SafetyFlags) == 0)
    		return new InspectionDefectType(SAFETYFLAGS);
    	else if(name.compareTo(DmoEnum_SafetyFlaresFusees) == 0)
    		return new InspectionDefectType(SAFETYFLARES);
    	else if(name.compareTo(DmoEnum_SafetySpareBulbFuses) == 0)
    		return new InspectionDefectType(SAFETYSPAREBULBFUSES);
    	else if(name.compareTo(DmoEnum_SafetySpareSealBeam) == 0)
    		return new InspectionDefectType(SAFETYSPARESEALBEAM);
    	else if(name.compareTo(DmoEnum_SuspensionSystem) == 0)
    		return new InspectionDefectType(SUSPENSIONSYSTEM);
    	else if(name.compareTo(DmoEnum_Starter) == 0)
    		return new InspectionDefectType(STARTER);
    	else if(name.compareTo(DmoEnum_Steering) == 0)
    		return new InspectionDefectType(STEERING);
    	else if(name.compareTo(DmoEnum_Tachograph) == 0)
    		return new InspectionDefectType(TACHOGRAPH);
    	else if(name.compareTo(DmoEnum_Tarpaulin) == 0)
    		return new InspectionDefectType(TARPAULIN);
    	else if(name.compareTo(DmoEnum_Tires) == 0)
    		return new InspectionDefectType(TIRES);
    	else if(name.compareTo(DmoEnum_TireChains) == 0)
    		return new InspectionDefectType(TIRECHAINS);
    	else if(name.compareTo(DmoEnum_Transmission) == 0)
    		return new InspectionDefectType(TRANSMISSION);
    	else if(name.compareTo(DmoEnum_WheelsAndRims) == 0)
    		return new InspectionDefectType(WHEELSANDRIMS);
    	else if(name.compareTo(DmoEnum_Windows) == 0)
    		return new InspectionDefectType(WINDOWS);
    	else if(name.compareTo(DmoEnum_WindshieldWipers) == 0)
    		return new InspectionDefectType(WINDSHIELDWIPERS);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }

}
