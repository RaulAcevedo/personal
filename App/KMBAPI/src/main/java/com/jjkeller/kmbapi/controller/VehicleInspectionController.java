package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.dataaccess.VehicleInspectionFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionList;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class VehicleInspectionController extends ControllerBase {

    private static final String OFFSETPARAM = "OffsetParam";

    public VehicleInspectionController(Context ctx) {
        super(ctx);

    }

    /// <summary>
    /// Download the recent inspections for the selected EOBR
    /// </summary>
    /// <returns></returns>
    public VehicleInspection DownloadRecentInspectionFor(EobrConfiguration eobrDevice) {
        VehicleInspection vehicleInspection = null;
        if (eobrDevice != null) {
            vehicleInspection = this.DownloadRecentVehicleInspection(eobrDevice);
        }
        return vehicleInspection;
    }

    /// <summary>
    /// Download the most recent inspection for the specific EOBR
    /// Answer if there was an inspection downloaded.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns></returns>
    @SuppressWarnings("unused")
    public VehicleInspection DownloadRecentVehicleInspection(EobrConfiguration eobrDevice) {
        VehicleInspection vehicleInspection = null;

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
            vehicleInspection = rwsh.DownloadRecentVehicleInspection(eobrDevice.getSerialNumber(), changeTimestampUTC);

            this.SaveVehicleInspection(vehicleInspection);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (JsonParseException jpe) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (jpe == null)
                jpe = new JsonParseException(JsonParseException.class.getName());
            this.HandleException(jpe, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (IOException ioe) {
            this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
        }

        return vehicleInspection;
    }

    /// <summary>
    /// Download the recent pre inspections for the selected EOBR
    /// </summary>
    /// <returns></returns>
    public VehicleInspection DownloadRecentPreInspectionFor(EobrConfiguration eobrDevice) {
        VehicleInspection vehicleInspection = null;
        if (eobrDevice != null) {
            vehicleInspection = this.DownloadRecentVehiclePreInspection(eobrDevice);
        }

        return vehicleInspection;
    }

    /// <summary>
    /// Download the most recent pre inspection for the specific EOBR
    /// Answer if there was an inspection downloaded.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns></returns>
    @SuppressWarnings("unused")
    public VehicleInspection DownloadRecentVehiclePreInspection(EobrConfiguration eobrDevice) {
        VehicleInspection vehicleInspection = null;

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
            vehicleInspection = rwsh.DownloadRecentVehiclePreInspection(eobrDevice.getSerialNumber(), changeTimestampUTC);

            this.SaveVehicleInspection(vehicleInspection);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (JsonParseException jpe) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (jpe == null)
                jpe = new JsonParseException(JsonParseException.class.getName());
            this.HandleException(jpe, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (IOException ioe) {
            this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
        }

        return vehicleInspection;
    }

    /// <summary>
    /// Check for an open DVIR
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns></returns>
    @SuppressWarnings("unused")
    public boolean CheckForOpenDVIR(String eobrSerialNumber) {
        boolean openDVIR = false;

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
            openDVIR = rwsh.CheckForOpenDVIR(eobrSerialNumber, changeTimestampUTC);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (JsonParseException jpe) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (jpe == null)
                jpe = new JsonParseException(JsonParseException.class.getName());
            this.HandleException(jpe, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (IOException ioe) {
            this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
        }

        return openDVIR;
    }

    /// <summary>
    /// Download the most recent inspection for the specific trailer
    /// Answer if there was an inspection downloaded.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns></returns>
    public VehicleInspection DownloadRecentTrailerInspection(String unitCodeForTrailer) {
        VehicleInspection vehicleInspection = null;

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
            vehicleInspection = rwsh.DownloadRecentTrailerInspection(unitCodeForTrailer, changeTimestampUTC);

            this.SaveVehicleInspection(vehicleInspection);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (IOException ioe) {
            this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
        }

        return vehicleInspection;
    }

    /// <summary>
    /// Download the most recent pre inspection for the specific trailer
    /// Answer if there was an inspection downloaded.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns></returns>
    public VehicleInspection DownloadRecentTrailerPreInspection(String unitCodeForTrailer) {
        VehicleInspection vehicleInspection = null;

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
            vehicleInspection = rwsh.DownloadRecentTrailerPreInspection(unitCodeForTrailer, changeTimestampUTC);

            this.SaveVehicleInspection(vehicleInspection);
        } catch (JsonSyntaxException jse) {
            this.HandleException(jse, this.getContext().getString(R.string.downloadconfigfromdmo));
        } catch (IOException ioe) {
            this.HandleException(ioe, this.getContext().getString(R.string.downloadconfigfromdmo));
        }

        return vehicleInspection;
    }

    /// <summary>
    /// Download the recent inspections for the selected EOBR
    /// </summary>
    /// <returns></returns>
    public void ReviewRecentTractorInspectionFor(EobrConfiguration eobrDevice) {
        this.setCurrentVehicleInspection(null);
        if (eobrDevice != null) {
            VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
            VehicleInspection vehicleInspection = facade.FetchRecentTractorInspectionForCurrentUser(eobrDevice);
            this.setCurrentVehicleInspection(vehicleInspection);
        }
    }

    /// <summary>
    /// Download the recent pre inspections for the selected EOBR
    /// </summary>
    /// <returns></returns>
    public void ReviewRecentTractorPreInspectionFor(EobrConfiguration eobrDevice) {
        this.setCurrentVehiclePreInspection(null);
        if (eobrDevice != null) {
            VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
            VehicleInspection vehicleInspection = facade.FetchRecentTractorPreInspectionForCurrentUser(eobrDevice);
            this.setCurrentVehiclePreInspection(vehicleInspection);
        }
    }

    /// <summary>
    /// Download the recent trailer inspections for the selected unit code
    /// </summary>
    /// <returns></returns>
    public void ReviewRecentTrailerInspectionFor(String trailerNumber) {
        this.setCurrentVehicleInspection(null);
        if (trailerNumber != null) {
            VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
            VehicleInspection vehicleInspection = facade.FetchRecentTrailerInspectionForCurrentUser(trailerNumber);
            this.setCurrentVehicleInspection(vehicleInspection);
        }
    }

    /// <summary>
    /// Download the recent trailer pre inspections for the selected unit code
    /// </summary>
    /// <returns></returns>
    public void ReviewRecentTrailerPreInspectionFor(String trailerNumber) {
        this.setCurrentVehiclePreInspection(null);
        if (trailerNumber != null) {
            VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
            VehicleInspection vehicleInspection = facade.FetchRecentTrailerPreInspectionForCurrentUser(trailerNumber);
            this.setCurrentVehiclePreInspection(vehicleInspection);
        }
    }

    // Save the vehicle inspection to the local database
    private boolean SaveVehicleInspection(VehicleInspection vehicleInspection) {
        boolean isSuccessful = false;

        if (vehicleInspection != null) {
            VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
            facade.Save(vehicleInspection);
        }
        isSuccessful = true;

        return isSuccessful;
    }

    /// <summary>
    /// Answer a list of all tractor numbers available for inspection
    /// </summary>
    /// <returns></returns>
    public List<EobrConfiguration> AllEobrDevices() {
        EobrConfigurationFacade eobrFacade = new EobrConfigurationFacade(this.getContext());
        List<EobrConfiguration> eobrList = eobrFacade.FetchAll();

        if (eobrList != null && EobrReader.getInstance().getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
            // if we're online to an EOBR right now and there is a list of eobr devices, 
            // verify that this EOBR is in the device list
            String serial = EobrReader.getInstance().getEobrSerialNumber();
            String tractorNbr = EobrReader.getInstance().getEobrIdentifier();
            boolean isFound = false;
            for (EobrConfiguration eobr : eobrList) {
                if (eobr.getSerialNumber().contains(serial)) {
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                // the currently online eobr is not in the list, so add this one to the list
                EobrConfiguration eobrConfiguration = new EobrConfiguration();
                eobrConfiguration.setSerialNumber(serial);
                eobrConfiguration.setTractorNumber(tractorNbr);
                eobrList.add(eobrConfiguration);
            }
        }
        return eobrList;
    }

    /// <summary>
    /// Answer a the tractor number of the currently connected EOBR device
    /// </summary>
    /// <returns></returns>
    public String CurrentConnectedTractorNumber() {
        return EobrReader.getInstance().getEobrIdentifier();
    }

    public VehicleInspection getCurrentVehicleInspection() {
        return GlobalState.getInstance().getCurrentVehicleInspection();
    }

    public void setCurrentVehicleInspection(VehicleInspection vehicleInspection) {
        GlobalState.getInstance().setCurrentVehicleInspection(vehicleInspection);
    }

    public VehicleInspection getCurrentVehiclePreInspection() {
        return GlobalState.getInstance().getCurrentVehiclePreInspection();
    }

    public void setCurrentVehiclePreInspection(VehicleInspection vehiclePreInspection) {
        GlobalState.getInstance().setCurrentVehiclePreInspection(vehiclePreInspection);
    }

    /// <summary>
    /// Create a new PreTrip tractor inspection
    /// </summary>
    public void StartInspection(InspectionTypeEnum inspectionType, boolean isPoweredUnit) {
        if (isPoweredUnit)
            this.setCurrentVehicleInspection(this.CreateTractorInspection(inspectionType));
        else
            this.setCurrentVehicleInspection(this.CreateTrailerInspection(inspectionType));
    }

    /// <summary>
    /// Assign the eobr device to the current inspection
    /// </summary>
    /// <param name="tactorNumber"></param>
    /// <param name="serialNumber"></param>
    public void AssignEobrDevice(String tractorNumber, String serialNumber) {
        if (tractorNumber.length() <= 0 || serialNumber.length() <= 0) return;

        this.getCurrentVehicleInspection().setTractorNumber(tractorNumber);
        this.getCurrentVehicleInspection().setSerialNumber(serialNumber);
    }

    /// <summary>
    /// Assign the trailer number to the current inspection
    /// </summary>
    /// <param name="eobrDevice"></param>
    public void AssignTrailerNumber(String trailerNumber) {
        this.getCurrentVehicleInspection().setTrailerNumber(trailerNumber);
    }

    /// <summary>
    /// Assign the trailer number to the current inspection
    /// </summary>
    /// <param name="eobrDevice"></param>
    public void AssignInspectionDate(Date inspectionDate) {
        this.getCurrentVehicleInspection().setInspectionTimeStamp(inspectionDate);
    }

    /// <summary>
    /// Assign the odometer reading to the current inspection
    /// </summary>
    public void AssignEobrOdometer() {

        if (this.getCurrentVehicleInspection().getIsPoweredUnit()) {
            if (DateUtility.GetDateFromDateTime(this.getCurrentVehicleInspection().getInspectionTimeStamp()).compareTo(DateUtility.GetDateFromDateTime(DateUtility.CurrentHomeTerminalTime(this.getCurrentUser()))) == 0) {
                StatusRecord statusRec = new StatusRecord();

                if (EobrReader.getIsEobrDeviceAvailable()) {
                    if (EobrReader.getInstance().Technician_GetCurrentData(statusRec, false) == 0) {
                        Bundle bundle = EobrReader.getInstance().Technician_ReadOdometerCalibrationValues();
                        float offset = bundle.getFloat(OFFSETPARAM);
                        float odometer = statusRec.getOdometerReading() + offset;

                        this.getCurrentVehicleInspection().setInspectionOdometer(odometer);
                    }
                }

            }
        }
    }

    /// <summary>
    /// Save the current inspection report to the local database and submit to DMO
    /// </summary>
    /// <returns></returns>
    public boolean Submit(VehicleInspection inspection) {
        boolean isSuccessful = true;

        isSuccessful = this.Save(inspection, this.getContext());
        if (isSuccessful)
            isSuccessful = this.SubmitInspectionToDMO(inspection);

        return isSuccessful;
    }


    /// <summary>
    /// Certify that the corrections have been made and the inspection has been reviewed
    /// </summary>
    public void CertifyAsCorrected(VehicleInspection inspection) {
        if (inspection == null) return;

        if (!inspection.getAreDefectsCorrected() && !inspection.getAreCorrectionsNotNeeded()) {
            // the driver is allowed to certify the repairs, if not done already
            inspection.setCertifiedByDate(DateUtility.getCurrentDateTimeUTC());
            inspection.setCertifiedByName(this.getCurrentUser().getCredentials().getEmployeeFullName());
        }

        // fill in the review information
        inspection.setReviewedByDate(DateUtility.getCurrentDateTimeUTC());
        inspection.setReviewedByName(this.getCurrentUser().getCredentials().getEmployeeFullName());
        inspection.setReviewedByEmployeeId(this.getCurrentUser().getCredentials().getEmployeeId());
    }


    /// <summary>
    /// Save the current inspection to the local database.
    /// Answer if successful
    /// </summary>
    /// <returns></returns>
    public boolean Save(VehicleInspection inspection, Context ctx) {
        boolean isSuccessful = false;

        VehicleInspectionFacade facade = new VehicleInspectionFacade(ctx);

        facade.Save(inspection);
        isSuccessful = true;

        return isSuccessful;
    }

    /// <summary>
    /// Add a defect to the current inspection being edited
    /// </summary>
    /// <param name="defect"></param>
    public void AddDefectToInspection(InspectionDefectType defect) {
        this.getCurrentVehicleInspection().AddDefect(defect);
    }

    /// <summary>
    /// Remove all defects from the inspection
    /// </summary>
    public void RemoveAllDefects() {
        this.getCurrentVehicleInspection().RemoveAllDefects();
        this.getCurrentVehicleInspection().setIsConditionSatisfactory(true);
    }

    /// <summary>
    /// Remove the defect from the inspection currently being edited
    /// </summary>
    /// <param name="defect"></param>
    public void RemoveDefectFromInspection(InspectionDefectType defect) {
        this.getCurrentVehicleInspection().RemoveDefect(defect);
    }

    public boolean DoesInspectionContainDefect(InspectionDefectType defect) {
        return this.getCurrentVehicleInspection().ContainsDefect(defect);
    }

    public int[] GetSerializableDefectList() {
        return this.getCurrentVehicleInspection().GetSerializableDefectList();
    }

    public void PutSerializableDefectList(int[] defectList) {
        this.getCurrentVehicleInspection().PutSerializableDefectList(defectList);
    }

    /// <summary>
    /// Create a new vehicle inspection for a powered unit (truck or tractor)
    /// </summary>
    /// <returns></returns>
    private VehicleInspection CreateTractorInspection(InspectionTypeEnum inspectionType) {
        VehicleInspection ins = new VehicleInspection();

        ins.setInspectionTypeEnum(inspectionType);
        ins.setInspectionTimeStamp(this.getCurrentClockHomeTerminalTime());
        ins.setIsPoweredUnit(true);
        ins.setSerialNumber(EobrReader.getInstance().getEobrSerialNumber());
        ins.setTractorNumber(EobrReader.getInstance().getEobrIdentifier());
        ins.setIsConditionSatisfactory(true);

        return ins;
    }

    /// <summary>
    /// Create a new vehicle inspection for a non-powered unit (trailer)
    /// </summary>
    /// <returns></returns>
    private VehicleInspection CreateTrailerInspection(InspectionTypeEnum inspectionType) {
        VehicleInspection ins = new VehicleInspection();

        ins.setInspectionTypeEnum(inspectionType);
        ins.setInspectionTimeStamp(this.getCurrentClockHomeTerminalTime());
        ins.setIsPoweredUnit(false);
        ins.setSerialNumber(null);
        ins.setTractorNumber(null);
        ins.setIsConditionSatisfactory(true);

        return ins;
    }

    /// <summary>
    /// Submit all inspections that need to be sent up to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    public boolean SubmitAllInspectionsToDMO() {
        boolean isSuccesful = false;

        if (this.getIsNetworkAvailable()) {
            try {
                // first fetch all unsubmitted records
                VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());
                List<VehicleInspection> unSubmittedItems = facade.FetchAllUnsubmitted();
                // are there any to send?
                if (unSubmittedItems != null && unSubmittedItems.size() > 0) {
                    // second, attempt to send each inspection to DMO
                    for (VehicleInspection inspection : unSubmittedItems) {
                        RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                        VehicleInspectionList listToSend = new VehicleInspectionList();
                        listToSend.Add(inspection);
                        // third, submit the inspections
                        rwsh.SubmitVehicleInspections(listToSend);
                        // fourth, mark the inspection as submitted
                        facade.MarkAsSubmitted(listToSend);
                    }
                }
                isSuccesful = true;
            } catch (JsonSyntaxException jse) {
                this.HandleException(jse);
            } catch (IOException ioe) {
                this.HandleException(ioe);
            }
        }

        return isSuccesful;
    }

    /// <summary>
    /// Submit the specified inspection to DMO.
    /// Answer if this was completed successfully.
    /// </summary>
    /// <returns></returns>
    private boolean SubmitInspectionToDMO(VehicleInspection inspection) {
        boolean isSuccesful = false;

        if (this.getIsNetworkAvailable()) {
            try {
                VehicleInspectionFacade facade = new VehicleInspectionFacade(this.getContext());

                // attempt to send to DMO
                VehicleInspectionList listToSend = new VehicleInspectionList();
                listToSend.Add(inspection);

                // there is a list to send to DMO
                RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                rwsh.SubmitVehicleInspections(listToSend);

                // if successful, then mark is as submitted, and save
                facade.MarkAsSubmitted(listToSend);

                isSuccesful = true;
            } catch (JsonSyntaxException jse) {
                this.HandleException(jse);
            } catch (IOException ioe) {
                this.HandleException(ioe);
            }
        }

        return isSuccesful;
    }
}
