package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IControllerFactory;
import com.jjkeller.kmbapi.controller.share.AobrdEnforceMinimumLengthStrategy;
import com.jjkeller.kmbapi.controller.share.EldEnforceMinimumLengthStrategy;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ControllerFactory implements IControllerFactory {

    private static IControllerFactory _instance;

    private final Context ctx = GlobalState.getInstance().getApplicationContext();
    EmployeeRuleController employeeRuleController = new EmployeeRuleController(ctx);
    LogCheckerComplianceDatesController logCheckerComplianceDatesController = new LogCheckerComplianceDatesController(ctx);
    LogEntryController logEntryController = new LogEntryController(ctx);
    UnassignedPeriodController unassignedPeriodController = new UnassignedPeriodController(
            ctx,
            GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ?
                    new EldEnforceMinimumLengthStrategy() :
                    new AobrdEnforceMinimumLengthStrategy());
    AdminController adminController = new AdminController(ctx);
    AppUpdateController appUpdateController = new AppUpdateController(ctx);
    DashboardController dashboardController = new DashboardController(ctx);
    DataUsageController dataUsageController = new DataUsageController(ctx);
    EmployeeLogAobrdController employeeLogAobrdController = new EmployeeLogAobrdController(ctx);
    EmployeeLogEldMandateController employeeLogEldMandateController = new EmployeeLogEldMandateController(ctx);
    EmployeeLogRevisionController employeeLogRevisionController = new EmployeeLogRevisionController(ctx);
    EngineRecordController engineRecordController = new EngineRecordController(ctx, GlobalState.getInstance().getAppSettings(ctx));
    EobrConfigController eobrConfigController = new EobrConfigController(ctx);
    EobrDiagnosticCommandController eobrDiagnosticCommandController = new EobrDiagnosticCommandController(ctx);
    ExemptLogValidationController exemptLogValidationController = new ExemptLogValidationController(ctx);
    FailureController failureController = new FailureController(ctx);
    FeatureToggleController featureToggleController = new FeatureToggleController(ctx);
    FileUploadController fileUploadController = new FileUploadController(ctx);
    FuelPurchaseController fuelPurchaseController = new FuelPurchaseController(ctx);
    GeotabController geotabController = new GeotabController(ctx);
    HosAlertController hosAlertController = new HosAlertController(ctx);
    HosAuditController hosAuditController = new HosAuditController(ctx);
    LocationCodeController locationCodeController = new LocationCodeController(ctx);
    LogHyrailController logHyrailController = new LogHyrailController(ctx);
    LoginController loginController = new LoginController(ctx);
    LogNonRegulatedDrivingController logNonRegulatedDrivingController = new LogNonRegulatedDrivingController(ctx);
    LogPersonalConveyanceController logPersonalConveyanceController = new LogPersonalConveyanceController(ctx);
    LogRemarksController logRemarksController = new LogRemarksController(ctx);
    MotionPictureController motionPictureController = new MotionPictureController(ctx);
    RoadsideInspectionController roadsideInspectionController = new RoadsideInspectionController(ctx);
    RouteController routeController = new RouteController(ctx);
    SystemStartupController systemStartupController = new SystemStartupController(ctx);
    TeamDriverController teamDriverController = new TeamDriverController(ctx);
    TripRecordController tripRecordController = new TripRecordController(ctx);
    VehicleInspectionController vehicleInspectionController = new VehicleInspectionController(ctx);
    AutoAssignUnassignedEventsController autoAssignUnassignedEventsController = new AutoAssignUnassignedEventsController(ctx);

    public static IControllerFactory getInstance()
    {
        if(_instance == null) {
            _instance = new ControllerFactory();
            return _instance;
        }
        return _instance;
    }

    protected void setInstance(IControllerFactory factory) {
        _instance = factory;
    }

    public LogEntryController getLogEntryController(){
        return logEntryController;
    }


    public LogEntryController getLogEntryController(Context ctx) {
        return new LogEntryController(ctx);
    }

    public LogCheckerComplianceDatesController getLogCheckerComplianceDateController(){
        return logCheckerComplianceDatesController;
    }


    public UnassignedPeriodController getUnassignedPeriodController(){
        return unassignedPeriodController;
    }


    public EmployeeRuleController getEmployeeRuleController(){
        return employeeRuleController;
    }



    public AdminController getAdminController(){
        return adminController;
    }



    public AppUpdateController getAppUpdateController(){
        return appUpdateController;
    }



    public DashboardController getDashboardController(){
        return dashboardController;
    }



    public DataUsageController getDataUsageController(){
        return dataUsageController;
    }



    public EmployeeLogAobrdController getEmployeeLogAobrdController(){
        return employeeLogAobrdController;
    }



    public EmployeeLogEldMandateController getEmployeeLogEldMandateController(){
        return employeeLogEldMandateController;
    }


    public EmployeeLogRevisionController getEmployeeLogRevisionController(){
        return employeeLogRevisionController;
    }


    public EngineRecordController getEngineRecordController(){
        return engineRecordController;
    }


    public EobrConfigController getEobrConfigController(){
        return eobrConfigController;
    }


    public EobrDiagnosticCommandController getEobrDiagnosticCommandController(){
        return eobrDiagnosticCommandController;
    }


    public ExemptLogValidationController getExemptLogValidationController(){
        return exemptLogValidationController;
    }



    public FailureController getFailureController(){
        return failureController;
    }



    public FeatureToggleController getFeatureToggleController(){
        return featureToggleController;
    }



    public FileUploadController getFileUploadController(){
        return fileUploadController;
    }



    public FuelPurchaseController getFuelPurchaseController(){
        return fuelPurchaseController;
    }



    public GeotabController getGeotabController(){
        return geotabController;
    }



    public HosAlertController getHosAlertController(){
        return hosAlertController;
    }



    public HosAuditController getHosAuditController(){
        return hosAuditController;
    }



    public LocationCodeController getLocationCodeController(){
        return locationCodeController;
    }



    public LogHyrailController getLogHyrailController(){
        return logHyrailController;
    }



    public LoginController getLoginController(){
        return loginController;
    }



    public LogNonRegulatedDrivingController getLogNonRegulatedDrivingController(){
        return logNonRegulatedDrivingController;
    }



    public LogPersonalConveyanceController getLogPersonalConveyanceController(){
        return logPersonalConveyanceController;
    }



    public LogRemarksController getLogRemarksController(){
        return logRemarksController;
    }



    public MotionPictureController getMotionPictureController(){
        return motionPictureController;
    }



    public RoadsideInspectionController getRoadsideInspectionController(){
        return roadsideInspectionController;
    }



    public RouteController getRouteController(){
        return routeController;
    }



    public SystemStartupController getSystemStartupController(){
        return systemStartupController;
    }



    public TeamDriverController getTeamDriverController(){
        return teamDriverController;
    }



    public TripRecordController getTripRecordController(){
        return tripRecordController;
    }



    public VehicleInspectionController getVehicleInspectionController(){
        return vehicleInspectionController;
    }

    public AutoAssignUnassignedEventsController getAutoAssignUnassignedEventsController(){
        return autoAssignUnassignedEventsController;
    }
}
