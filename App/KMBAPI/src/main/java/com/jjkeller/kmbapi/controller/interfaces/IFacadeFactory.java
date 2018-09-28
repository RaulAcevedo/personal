package com.jjkeller.kmbapi.controller.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.CompanyFacade;
import com.jjkeller.kmbapi.controller.dataaccess.DataUsageFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeRuleFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EngineRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EventDataRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.FuelPurchaseFacade;
import com.jjkeller.kmbapi.controller.dataaccess.KMBEncompassUserFacade;
import com.jjkeller.kmbapi.controller.dataaccess.LocationCodeFacade;
import com.jjkeller.kmbapi.controller.dataaccess.LogCheckerComplianceDatesFacade;
import com.jjkeller.kmbapi.controller.dataaccess.LogRemarkItemFacade;
import com.jjkeller.kmbapi.controller.dataaccess.RoutePositionFacade;
import com.jjkeller.kmbapi.controller.dataaccess.TripRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedDrivingPeriodFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedEobrFailurePeriodFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UserFacade;
import com.jjkeller.kmbapi.controller.dataaccess.VehicleInspectionFacade;
import com.jjkeller.kmbapi.controller.share.User;

public interface IFacadeFactory {

	CompanyFacade getCompanyFacade(Context ctx);

	DataUsageFacade getDataUsageFacade(Context ctx);

	IEmployeeLogFacade getEmployeeLogFacade(Context ctx);

	EmployeeRuleFacade getEmployeeRuleFacade(Context ctx, User user);

	EngineRecordFacade getEngineRecordFacade(Context ctx, User user);

	EobrConfigurationFacade getEobrConfigurationFacade(Context ctx, User user);

    EobrConfigurationFacade getEobrConfigurationFacade(Context ctx);

    EventDataRecordFacade getEventDataRecordFacade(Context ctx, User user);

	FuelPurchaseFacade getFuelPurchaseFacade(Context ctx, User user);

	KMBEncompassUserFacade getKMBEncompassUserFacade(Context ctx);

	LocationCodeFacade getLocationCodeFacade(Context ctx);

	LogCheckerComplianceDatesFacade getLogCheckerComplianceDatesFacade(
			Context ctx);

	LogRemarkItemFacade getLogRemarkItemFacade(Context ctx);

	RoutePositionFacade getRoutePositionFacade(Context ctx);

	TripRecordFacade getTripRecordFacade(Context ctx, User user);

	UnassignedDrivingPeriodFacade getUnassignedDrivingPeriodFacade(Context ctx,
			User user);

	UnassignedEobrFailurePeriodFacade getUnassignedEobrFailurePeriodFacade(
			Context ctx, User user);

	UserFacade getUserFacade(Context ctx, User user);

	VehicleInspectionFacade getVehicleInspectionFacade(Context ctx);

	IEmployeeLogFacade getEmployeeLogFacade(Context ctx, User user);

}