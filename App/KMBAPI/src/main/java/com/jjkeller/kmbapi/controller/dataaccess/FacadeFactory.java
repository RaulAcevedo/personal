package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IFacadeFactory;
import com.jjkeller.kmbapi.controller.share.User;

public class FacadeFactory implements IFacadeFactory {
	
	private static IFacadeFactory _instance; 
	
	public static IFacadeFactory GetInstance() {
		if(_instance == null)
			return (_instance = new FacadeFactory()); 
		
		return _instance; 
	}
	
	// Override in test double to set factory mock
	protected void setInstance(IFacadeFactory factory) {
		_instance = factory; 
	}
	
	public CompanyFacade getCompanyFacade(Context ctx) {
		return new CompanyFacade(ctx); 
	}	
	
	public DataUsageFacade getDataUsageFacade(Context ctx) {
		return new DataUsageFacade(ctx); 
	}
	
	public IEmployeeLogFacade getEmployeeLogFacade(Context ctx) {
		return new EmployeeLogFacade(ctx); 
	}
	
	public IEmployeeLogFacade getEmployeeLogFacade(Context ctx, User user) {
		return new EmployeeLogFacade(ctx, user); 
	}
	
	public EmployeeRuleFacade getEmployeeRuleFacade(Context ctx, User user) {
		return new EmployeeRuleFacade(ctx, user); 
	}
	
	public EngineRecordFacade getEngineRecordFacade(Context ctx, User user) {
		return new EngineRecordFacade(ctx, user); 
	}
	
	public EobrConfigurationFacade getEobrConfigurationFacade(Context ctx, User user) {
		return new EobrConfigurationFacade(ctx, user); 
	}

    @Override
    public EobrConfigurationFacade getEobrConfigurationFacade(Context ctx) {
        return new EobrConfigurationFacade(ctx);
    }
	
	public EventDataRecordFacade getEventDataRecordFacade(Context ctx, User user) {
		return new EventDataRecordFacade(ctx, user); 
	}
	
	public FuelPurchaseFacade getFuelPurchaseFacade(Context ctx, User user) {
		return new FuelPurchaseFacade(ctx, user); 
	}

	public KMBEncompassUserFacade getKMBEncompassUserFacade(Context ctx) {
		return new KMBEncompassUserFacade(ctx);
	}

	public LocationCodeFacade getLocationCodeFacade(Context ctx) {
		return new LocationCodeFacade(ctx); 
	}
	
	public LogCheckerComplianceDatesFacade getLogCheckerComplianceDatesFacade(Context ctx) {
		return new LogCheckerComplianceDatesFacade(ctx); 
	}
	
	public LogRemarkItemFacade getLogRemarkItemFacade(Context ctx) {
		return new LogRemarkItemFacade(ctx); 
	}
	
	public RoutePositionFacade getRoutePositionFacade(Context ctx) {
		return new RoutePositionFacade(ctx); 
	}
	
	public TripRecordFacade getTripRecordFacade(Context ctx, User user) {
		return new TripRecordFacade(ctx, user); 
	}
	
	public UnassignedDrivingPeriodFacade getUnassignedDrivingPeriodFacade(Context ctx, User user) {
		return new UnassignedDrivingPeriodFacade(ctx, user); 
	}
	
	public UnassignedEobrFailurePeriodFacade getUnassignedEobrFailurePeriodFacade(Context ctx, User user) {
		return new UnassignedEobrFailurePeriodFacade(ctx, user); 
	}
	
	public UserFacade getUserFacade(Context ctx, User user) {
		return new UserFacade(ctx, user); 
	}
	
	public VehicleInspectionFacade getVehicleInspectionFacade(Context ctx) {
		return new VehicleInspectionFacade(ctx); 
	}
}
