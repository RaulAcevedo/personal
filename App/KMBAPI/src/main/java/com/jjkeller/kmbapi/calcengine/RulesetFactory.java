package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Canadian.Cycle1;
import com.jjkeller.kmbapi.calcengine.Canadian.Cycle2;
import com.jjkeller.kmbapi.calcengine.Federal.Alaska7DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Alaska7DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.Alaska8DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Alaska8DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.CaliforniaIntrastatePassenger;
import com.jjkeller.kmbapi.calcengine.Federal.CaliforniaIntrastateProperty;
import com.jjkeller.kmbapi.calcengine.Federal.CaliforniaMotionPicture80HourCarrying;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt100MilePassengerCarrying60Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt100MilePassengerCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt100MilePropertyCarrying60Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt100MilePropertyCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt150MilePropertyCarrying60Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt150MilePropertyCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Florida7DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Florida7DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.Florida8DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Florida8DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.PassengerCarrying60Hour;
import com.jjkeller.kmbapi.calcengine.Federal.PassengerCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.Federal.PropertyCarrying60Hour;
import com.jjkeller.kmbapi.calcengine.Federal.PropertyCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.Federal.Texas;
import com.jjkeller.kmbapi.calcengine.Federal.USConstruction7DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.USConstruction8DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.USMotionPicture7DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.USMotionPicture7DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.USMotionPicture8DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.USMotionPicture8DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.Wisconsin7DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Wisconsin7DayProperty;
import com.jjkeller.kmbapi.calcengine.Federal.Wisconsin8DayPassenger;
import com.jjkeller.kmbapi.calcengine.Federal.Wisconsin8DayProperty;
import com.jjkeller.kmbapi.calcengine.OilField.TexasOilField;
import com.jjkeller.kmbapi.calcengine.OilField.USOilField;

public class RulesetFactory {

    public static IHosRulesetCalcEngine ForUS60Passenger()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new PassengerCarrying60Hour(properties);
    }
    
    public static IHosRulesetCalcEngine ForExempt100MilePropertyCarrying60Hour(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
        return new Exempt100MilePropertyCarrying60Hour(properties);
    }
    
    public static IHosRulesetCalcEngine ForExempt100MilePropertyCarrying70Hour(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
        return new Exempt100MilePropertyCarrying70Hour(properties);
    }

    public static IHosRulesetCalcEngine ForExempt100MilePassengerCarrying60Hour()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new Exempt100MilePassengerCarrying60Hour(properties);
    }
    
    public static IHosRulesetCalcEngine ForExempt100MilePassengerCarrying70Hour()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new Exempt100MilePassengerCarrying70Hour(properties);
    }
    
    public static IHosRulesetCalcEngine ForExempt150MilePropertyCarrying60Hour(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
        return new Exempt150MilePropertyCarrying60Hour(properties);
    }
    
    public static IHosRulesetCalcEngine ForExempt150MilePropertyCarrying70Hour(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
        return new Exempt150MilePropertyCarrying70Hour(properties);
    }

    public static IHosRulesetCalcEngine ForUS70Passenger()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new PassengerCarrying70Hour(properties);
    }

    public static IHosRulesetCalcEngine ForUS60Property(boolean isShortHaulExceptionAllowed, boolean is34HourResetAllowed, boolean is8HourDrivingRuleEnabled) 
    {
    	RulesetProperties properties = new RulesetProperties(); 
    	properties.setIsShortHaulExceptionAllowed(isShortHaulExceptionAllowed);
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
    	properties.setIs8HourDrivingRuleEnabled(is8HourDrivingRuleEnabled);
    	
        return new PropertyCarrying60Hour(properties);
    }

    public static IHosRulesetCalcEngine ForUS70Property(boolean isShortHaulExceptionAllowed, boolean is34HourResetAllowed, boolean is8HourDrivingRuleEnabled)
    {
    	RulesetProperties properties = new RulesetProperties();    	
    	properties.setIsShortHaulExceptionAllowed(isShortHaulExceptionAllowed);
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
    	properties.setIs8HourDrivingRuleEnabled(is8HourDrivingRuleEnabled);
    	
    	return new PropertyCarrying70Hour(properties);
    }

    public static IHosRulesetCalcEngine ForCanadianCycle1(boolean isTeamDriverPresent)
    {
    	RulesetProperties properties = new RulesetProperties();    	
    	properties.setIsTeamDriverPresent(isTeamDriverPresent);
    	
        return new Cycle1(properties);
    }

    public static IHosRulesetCalcEngine ForCanadianCycle2(boolean isTeamDriverPresent)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIsTeamDriverPresent(isTeamDriverPresent);

       	return new Cycle2(properties);
    }

    public static IHosRulesetCalcEngine ForFlorida7DayProperty(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);

       	return new Florida7DayProperty(properties);
    }

    public static IHosRulesetCalcEngine ForFlorida8DayProperty(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);
       	
        return new Florida8DayProperty(properties);
    }

    public static IHosRulesetCalcEngine ForFlorida7DayPassenger()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new Florida7DayPassenger(properties);
    }

    public static IHosRulesetCalcEngine ForFlorida8DayPassenger()
    {
    	RulesetProperties properties = new RulesetProperties();    	
        return new Florida8DayPassenger(properties);
    }

    public static IHosRulesetCalcEngine ForTexas(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties(); 
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);
       	
        return new Texas(properties);
    }
    
    public static IHosRulesetCalcEngine ForUSOilField(boolean isShortHaulExceptionAllowed, boolean is34HourResetAllowed, boolean is8HourDrivingRuleEnabled)
    {
    	RulesetProperties properties = new RulesetProperties(); 
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
    	properties.setIs8HourDrivingRuleEnabled(is8HourDrivingRuleEnabled);
    	properties.setIsShortHaulExceptionAllowed(isShortHaulExceptionAllowed);
    	
    	return new USOilField(properties);
    }

    public static IHosRulesetCalcEngine ForTexasOilField(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties(); 
    	properties.setIs34HourResetAllowed(is34HourResetAllowed);
    	
    	return new TexasOilField(properties);
    }
    
	public static IHosRulesetCalcEngine ForUSMotionPicture7DayPassenger()
	{
		return new USMotionPicture7DayPassenger(new RulesetProperties());
	}
    
	public static IHosRulesetCalcEngine ForUSMotionPicture8DayPassenger()
	{
		return new USMotionPicture8DayPassenger(new RulesetProperties());
	}
    
	public static IHosRulesetCalcEngine ForUSMotionPicture7DayProperty(boolean is34HourResetAllowed)
	{
		RulesetProperties properties = new RulesetProperties();
		properties.setIs34HourResetAllowed(is34HourResetAllowed);
		return new USMotionPicture7DayProperty(properties);
	}

	public static IHosRulesetCalcEngine ForUSMotionPicture8DayProperty(boolean is34HourResetAllowed)
	{
		RulesetProperties properties = new RulesetProperties();
		properties.setIs34HourResetAllowed(is34HourResetAllowed);
		return new USMotionPicture8DayProperty(properties);
	}
	
	public static IHosRulesetCalcEngine ForAlaska7DayPassenger()
    {
    	return new Alaska7DayPassenger(new RulesetProperties());
    }

    public static IHosRulesetCalcEngine ForAlaska8DayPassenger()
    {
    	return new Alaska8DayPassenger(new RulesetProperties());
    }
	
	public static IHosRulesetCalcEngine ForAlaska7DayProperty(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);

       	return new Alaska7DayProperty(properties);
    }

    public static IHosRulesetCalcEngine ForAlaska8DayProperty(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);
       	
        return new Alaska8DayProperty(properties);
    }
    
    public static IHosRulesetCalcEngine ForCaliforniaIntrastatePassenger()
    {
    	return new CaliforniaIntrastatePassenger(new RulesetProperties());
    }
    
    public static IHosRulesetCalcEngine ForCaliforniaIntrastateProperty(boolean is34HourResetAllowed)
    {
    	RulesetProperties properties = new RulesetProperties();    	
       	properties.setIs34HourResetAllowed(is34HourResetAllowed);
       	
    	return new CaliforniaIntrastateProperty(properties);
    }

    public static IHosRulesetCalcEngine ForWisconsin7DayPassenger()
    {
        return new Wisconsin7DayPassenger(new RulesetProperties());
    }

    public static IHosRulesetCalcEngine ForWisconsin8DayPassenger()
    {
        return new Wisconsin8DayPassenger(new RulesetProperties());
    }

    public static IHosRulesetCalcEngine ForWisconsin7DayProperty()
    {
        return new Wisconsin7DayProperty(new RulesetProperties());
    }

    public static IHosRulesetCalcEngine ForWisconsin8DayProperty()
    {
        return new Wisconsin8DayProperty(new RulesetProperties());
    }

    public static IHosRulesetCalcEngine ForUSConstruction7DayProperty(boolean is34HourResetAllowed, boolean is8HourDrivingRuleEnabled, boolean isShortHaulExceptionAllowed)
    {
        RulesetProperties properties = new RulesetProperties();
        properties.setIs34HourResetAllowed(is34HourResetAllowed);
        properties.setIs8HourDrivingRuleEnabled(is8HourDrivingRuleEnabled);
        properties.setIsShortHaulExceptionAllowed(isShortHaulExceptionAllowed);

        return new USConstruction7DayProperty(properties);
    }

    public static IHosRulesetCalcEngine ForUSConstruction8DayProperty(boolean is34HourResetAllowed, boolean is8HourDrivingRuleEnabled, boolean isShortHaulExceptionAllowed)
    {
        RulesetProperties properties = new RulesetProperties();
        properties.setIs34HourResetAllowed(is34HourResetAllowed);
        properties.setIs8HourDrivingRuleEnabled(is8HourDrivingRuleEnabled);
        properties.setIsShortHaulExceptionAllowed(isShortHaulExceptionAllowed);

        return new USConstruction8DayProperty(properties);
    }

    public static IHosRulesetCalcEngine ForCaliforniaMotionPicture80HourProperty(boolean is34HourResetAllowed)
    {
        RulesetProperties properties = new RulesetProperties();
        properties.setIs34HourResetAllowed(is34HourResetAllowed);
        return new CaliforniaMotionPicture80HourCarrying(properties);
    }
}