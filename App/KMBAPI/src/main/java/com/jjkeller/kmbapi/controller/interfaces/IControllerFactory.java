package com.jjkeller.kmbapi.controller.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbapi.controller.AppUpdateController;
import com.jjkeller.kmbapi.controller.AutoAssignUnassignedEventsController;
import com.jjkeller.kmbapi.controller.DashboardController;
import com.jjkeller.kmbapi.controller.DataUsageController;
import com.jjkeller.kmbapi.controller.EmployeeLogAobrdController;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.EmployeeLogRevisionController;
import com.jjkeller.kmbapi.controller.EmployeeRuleController;
import com.jjkeller.kmbapi.controller.EngineRecordController;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.EobrDiagnosticCommandController;
import com.jjkeller.kmbapi.controller.ExemptLogValidationController;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.FeatureToggleController;
import com.jjkeller.kmbapi.controller.FileUploadController;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.HosAlertController;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.LocationCodeController;
import com.jjkeller.kmbapi.controller.LogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LogHyrailController;
import com.jjkeller.kmbapi.controller.LogNonRegulatedDrivingController;
import com.jjkeller.kmbapi.controller.LogPersonalConveyanceController;
import com.jjkeller.kmbapi.controller.LogRemarksController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.RoadsideInspectionController;
import com.jjkeller.kmbapi.controller.RouteController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.TripRecordController;
import com.jjkeller.kmbapi.controller.UnassignedPeriodController;
import com.jjkeller.kmbapi.controller.VehicleInspectionController;

public interface IControllerFactory {
	LogEntryController getLogEntryController();
    LogEntryController getLogEntryController(Context ctx);
	LogCheckerComplianceDatesController getLogCheckerComplianceDateController();
    UnassignedPeriodController getUnassignedPeriodController();

    EmployeeRuleController getEmployeeRuleController();

    AdminController getAdminController();

    AppUpdateController getAppUpdateController();

    DashboardController getDashboardController();

    DataUsageController getDataUsageController();

    EmployeeLogAobrdController getEmployeeLogAobrdController();

    EmployeeLogEldMandateController getEmployeeLogEldMandateController();

    EmployeeLogRevisionController getEmployeeLogRevisionController();

    EngineRecordController getEngineRecordController();

    EobrConfigController getEobrConfigController();

    EobrDiagnosticCommandController getEobrDiagnosticCommandController();

    ExemptLogValidationController getExemptLogValidationController();

    FailureController getFailureController();

    FeatureToggleController getFeatureToggleController();

    FileUploadController getFileUploadController();

    FuelPurchaseController getFuelPurchaseController();

    GeotabController getGeotabController();

    HosAlertController getHosAlertController();

    HosAuditController getHosAuditController();

    LocationCodeController getLocationCodeController();

    LogHyrailController getLogHyrailController();

    LoginController getLoginController();

    LogNonRegulatedDrivingController getLogNonRegulatedDrivingController();

    LogPersonalConveyanceController getLogPersonalConveyanceController();

    LogRemarksController getLogRemarksController();

    MotionPictureController getMotionPictureController();

    RoadsideInspectionController getRoadsideInspectionController();

    RouteController getRouteController();

    SystemStartupController getSystemStartupController();

    TeamDriverController getTeamDriverController();

    TripRecordController getTripRecordController();

    VehicleInspectionController getVehicleInspectionController();

    AutoAssignUnassignedEventsController getAutoAssignUnassignedEventsController();
}