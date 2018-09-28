package com.jjkeller.kmbapi.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.EOBR.ManipulableEobrReader;
import com.jjkeller.kmbapi.controller.EOBR.datamanipulators.DistHoursRuntimeClearer;
import com.jjkeller.kmbapi.controller.EOBR.datamanipulators.HeavyBusFaker;
import com.jjkeller.kmbapi.controller.EOBR.datamanipulators.OdometerClearer;
import com.jjkeller.kmbapi.controller.EOBR.datamanipulators.StatusBufferRuntimeClearer;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.EobrException;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdminController extends ControllerBase {
    private static final String ADMIN_SHARED_PREFERENCES = "com.jjkeller.kmbapi.controller.AdminController";

    private EobrReader _eobrReader;

    public AdminController(Context ctx) {
        this(ctx, EobrReader.getInstance());
    }

    public AdminController(Context ctx, EobrReader eobrReader) {
        super(ctx);
        _eobrReader = eobrReader;
    }

    public boolean getIsEOBRDeviceAvailable() {
        return EobrReader.getIsEobrDeviceAvailable();
    }

    public boolean getIsEOBRDeviceOnlineOrReadingHistory() {
        return EobrReader.getIsEobrDeviceOnlineOrReadingHistory();
    }

    public StatusRecord GetCurrentEOBRData(boolean updateRefTimestamp) {
        StatusRecord record = new StatusRecord();
        if (_eobrReader != null) {
            try {
                _eobrReader.Technician_GetCurrentData(record, updateRefTimestamp);
            } catch (Exception e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }
        return record;
    }

    public float GetEobrDashboardOdometerValue(float odometerValue) {
        float dashOdom = 0F;
        if (_eobrReader != null) {
            dashOdom = _eobrReader.ConvertToDashboardOdometer(this.getContext(), odometerValue);
        }
        return dashOdom;
    }

    /// <summary>
    /// Answer the count of all unsubmitted local logs
    /// </summary>
    /// <returns></returns>
    public int GetLocalLogCount() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        int logCount = facade.FetchLogCountByStatus(1); //LogSourceStatusEnum.LocalUnsubmitted);
        return logCount;
    }

    /// <summary>
    /// Answer the count of all DMO server logs
    /// </summary>
    /// <returns></returns>
    public int GetServerLogCount() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        int logCount = facade.FetchLogCountByStatus(3); //LogSourceStatusEnum.DMOServerLog);
        return logCount;
    }

    public String GetEobrIdentifier() {
        String eobrId = null;

        if (_eobrReader != null) {
            try {
                eobrId = _eobrReader.Technician_GetUniqueIdentifier(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return eobrId;
    }

    public String GetEobrSerialNumber() {
        String eobrSerialNumber = null;
        if (_eobrReader != null) {
            try {
                eobrSerialNumber = _eobrReader.Technician_GetSerialNumber(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return eobrSerialNumber;
    }

    public String GetEobrCompanyPassKey() {
        String eobrCompanyPasskey = null;
        if (_eobrReader != null) {
            try {
                eobrCompanyPasskey = _eobrReader.Technician_GetCompanyPassKey(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return eobrCompanyPasskey;
    }

    public int GetEobrDataInterval() {
        int eobrDataInterval = 0;
        if (_eobrReader != null) {
            try {
                eobrDataInterval = _eobrReader.Technician_GetDataCollectionRate(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return eobrDataInterval;
    }

    public int GetEngineTimeoutDuration() {
        int eobrEngineTimeout = 0;
        if (_eobrReader != null) {
            try {
                eobrEngineTimeout = _eobrReader.Technician_GetEngineOffCommsTimeoutDuration(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return eobrEngineTimeout;
    }

    public DatabusTypeEnum GetBusType() {
        DatabusTypeEnum eobrActiveBusType = new DatabusTypeEnum(DatabusTypeEnum.NULL);
        if (_eobrReader != null) {
            try {
                eobrActiveBusType = _eobrReader.Technician_GetBusType(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }
        return eobrActiveBusType;
    }

    public Bundle GetEobrVersionInfo() {
        Bundle versionInfo = null;
        if (_eobrReader != null) {
            try {
                versionInfo = _eobrReader.Technician_GetEOBRRevisions();
                if (versionInfo.getInt(this.getContext().getString(R.string.rc)) != 0) {
                    versionInfo = null;
                }
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }
        return versionInfo;
    }

    public Date GetEobrDeviceTime() {
        Date eobrDeviceTime = null;
        if (_eobrReader != null) {
            try {
                eobrDeviceTime = _eobrReader.Technician_ReadClockUniversalTime(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }
        return eobrDeviceTime;
    }


    public Bundle GetEobrReferenceTimestampsAll() {

        Bundle bundle = null;

        if (_eobrReader != null) {
            try {
                bundle = _eobrReader.Technician_ReadReferenceTimestamp();
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        return bundle;
    }


    public Date GetEobrReferenceTimestamp() {
        Date eobrReferenceTimestamp = null;
        if (_eobrReader != null) {
            try {
                eobrReferenceTimestamp = _eobrReader.Technician_ReadReferenceTimestamp(this.getContext());
            } catch (Exception ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }
        return eobrReferenceTimestamp;
    }

    public float GetEobrOdometerOffset() {
        Bundle bundle = null;
        float offset = 0.0F;
        if (_eobrReader != null) {
            bundle = _eobrReader.Technician_ReadOdometerCalibrationValues();
            if (bundle != null && bundle.containsKey(this.getContext().getString(R.string.offsetparam))) {
                offset = bundle.getFloat(this.getContext().getString(R.string.offsetparam));
            }
        }
        return offset;
    }

    public double GetEobrOdometerMultiplier() {
        Bundle bundle = null;
        float multiplier = 0.0F;
        if (_eobrReader != null) {
            bundle = _eobrReader.Technician_ReadOdometerCalibrationValues();
            if (bundle != null && bundle.containsKey(this.getContext().getString(R.string.multiplierparam))) {
                multiplier = bundle.getFloat(this.getContext().getString(R.string.multiplierparam));
            }
        }
        return multiplier;
    }

    public Bundle GetEobrThresholdValues() {
        Bundle thresholds = null;
        if (_eobrReader != null) {
            try {
                thresholds = _eobrReader.Technician_GetThresholds(this.getContext(), 0);
                if (thresholds.getInt(this.getContext().getString(R.string.rc)) != 0) {
                    thresholds = null;
                }
            } catch (Exception ex) {
                Log.e("GetEobrThresholdValues", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }
        return thresholds;
    }

    /**
     * Answer if the specified daily password matches the system generated daily password
     *
     * @param dailyPassword
     * @return true if the password matches, false otherwise
     */
    public boolean IsDailyPasswordValid(String dailyPassword) {
        boolean isValid = false;

        if (dailyPassword != null && dailyPassword.length() > 0) {
            String officialDailyPassword = this.CreateDailyPassword();
            if (officialDailyPassword != null && officialDailyPassword.length() > 0) {
                isValid = dailyPassword.compareTo(officialDailyPassword) == 0;
            }
        }

        return isValid;
    }

    /**
     * Deletes data from Geotab Tables
     * Currently used for Geotab users on Clear EOBR Admin section
     */
    public void PurgeGeotabTables() {

        GeotabController geotabController = new GeotabController(this.getContext());
        geotabController.PurgeGeotabTables();
    }

    /**
     * Clears engine state
     */
    public void ClearEngineState() {

        GeotabController geotabController = new GeotabController(this.getContext());
        geotabController.ClearEngineState();
    }

    /**
     * Reset the historical data stored in the EOBR
     * There must be an EOBR somewhat connected, but it doesn't have to be ONLINE
     */
    public boolean ResetEobrHistoryData() {

        boolean isSuccessful = false;

        if (_eobrReader.getEobrIdentifier() != null &&
                _eobrReader.getEobrIdentifier().length() > 0 &&
                _eobrReader.getCurrentConnectionState() != EobrReader.ConnectionState.SHUTDOWN) {

            try {
                _eobrReader.IgnoreResume(true);
                _eobrReader.SuspendReading();

                // 2014.07.16 sjn - Since history has been successfully reset, allow the history reading process to abort
                //                  There was a problem reported where every time a user would sign in, it would read history,
                //                  but never complete.  Because history reading is a long-running event, it should abort
                //                  whenever history is reset.
                GlobalState.getInstance().setAbortReadingHistory(true);

                // sleep for 15 seconds - allow max time for any current calls to eobr to timeout
                Thread.sleep(15000);

                if (_eobrReader.IsDevicePhysicallyConnected(this.getContext(), true)) {
                    int rc = _eobrReader.ResetHistoryData();
                    isSuccessful = rc == 0;
                }
            } catch (Throwable excp) {
                isSuccessful = false;
            } finally {
                _eobrReader.IgnoreResume(false);
                _eobrReader.ResumeReading();
            }
        }

        return isSuccessful;
    }

    private String CreateDailyPassword() {

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String dateToday = df.format(TimeKeeper.getInstance().now());
        String dailyPassword = this.GenerateDailyPassword(dateToday);
        return dailyPassword;
    }

    /**
     * Generate the Daily Password according to the DMO algorithm found here: http://localhost/Web/admin/GenerateDailyPassword.aspx
     *
     * @param sDate
     * @return
     */
    private String GenerateDailyPassword(String sDate) {
        String password = null;
        try {
            int array_max = 8;
            int[] idt = new int[array_max];

            // set up individual integers with mmddyyyy values
            // first make sure date is mm-dd-yyyy format
            idt[0] = Integer.parseInt(sDate.substring(0, 1));
            idt[1] = Integer.parseInt(sDate.substring(1, 2));
            idt[2] = Integer.parseInt(sDate.substring(3, 4));
            idt[3] = Integer.parseInt(sDate.substring(4, 5));
            int iday = idt[3];
            idt[4] = Integer.parseInt(sDate.substring(6, 7));
            idt[5] = Integer.parseInt(sDate.substring(7, 8));
            idt[6] = Integer.parseInt(sDate.substring(8, 9));
            idt[7] = Integer.parseInt(sDate.substring(9, 10));

            //  if todays date ends in a zero, arbitrarily pick 5
            if (iday == 0) {
                iday = 5;
            }

            //add day digits to both month digits
            iday = iday + idt[2] + idt[1] + idt[0];

            //get only the last digit of this summed, positive number
            int iLen = String.valueOf(iday).length();
            String lastDigit = String.valueOf(iday).substring(iLen - 1, iLen);
            iday = Integer.parseInt(lastDigit);

            //for each digit in the array, ultimately derive an
            //A-Z letter, or 0-9 digit
            for (int isub = 0; isub <= array_max - 1; isub++) {
                //get the  difference between number and 9
                idt[isub] = (9 - idt[isub]);
                //if this number ends in a zero, arbitrarily pick 5
                if (idt[isub] == 0) {
                    idt[isub] = 9;
                }
                //multiply the current array digit by current value of "iday" (iday
                //is progessively incremented by 1 for each occurrence in the array)
                idt[isub] = idt[isub] * (iday) + idt[isub];
                //the result will be a number always <= 90 (max could be 9 * 9 + 9)
                //Equate the derived number to an ANSI character set code table value (65 - 90  is A-Z,
                //48-57 is 0-9)
                int iNum = idt[isub];
                if (iNum < 27) {
                    idt[isub] = idt[isub] + 64;
                } else if (iNum < 48) {
                    idt[isub] = idt[isub] - 26 + 64;
                } else if (iNum < 58) //48 - 57, leave it alone, 0 - 9
                {
                } else if (iNum < 65) {
                    idt[isub] = idt[isub] + 10;
                } else //ok - calculated won't be greater than 90 (Z)
                {
                }

                //add 1 to multiplier
                iday = iday + 1;
                if (iday > 9) {
                    iday = 1;
                }

            }    //next array digit

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j <= array_max - 1; j++) {
                char ch = (char) idt[j];
                sb.append(ch);
            }
            password = sb.toString();

            Log.v("Daily Password", "Daily password is: " + password);
        } catch (Exception ex) {

            Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
        }

        return password;
    }

    /// <summary>
    /// Wipes Eobr data   
    /// </summary>
    /// <returns></returns>
    public boolean ResetEobr(Context ctx, ProgressDialog pd) {
        boolean isSuccessful = false;
        try {
            new ResetEobrTask(ctx, pd).execute();
            isSuccessful = true;
        } catch (Exception excp) {
            // unknown error, should probably throw this error back
            this.HandleException(excp);
        }

        return isSuccessful;
    }

    public class ResetEobrTask extends AsyncTask<Void, String, Void> {
        ProgressDialog pd;
        Exception ex;
        String clearingEntireDeviceMessage;
        String clearingDeviceFailedMessage;
        int clearDevice;
        int unableToClearMessage;
        int successfullyClearDeviceMessage;

        public ResetEobrTask(Context ctx, ProgressDialog pd) {
            this.pd = pd;
            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                clearingEntireDeviceMessage = ctx.getString(R.string.msgclearingentireeld);
                unableToClearMessage = R.string.msgunabletocleareld;
                clearDevice = R.string.msgcleareld;
                successfullyClearDeviceMessage = R.string.msgsuccessfullyclearedeld;
                clearingDeviceFailedMessage = ctx.getString(R.string.clearing_eld_failed);
            } else {
                clearingEntireDeviceMessage = ctx.getString(R.string.msgclearingentireeobr);
                unableToClearMessage = R.string.msgunabletocleareobr;
                clearDevice = R.string.msgcleareobr;
                successfullyClearDeviceMessage = R.string.msgsuccessfullyclearedeobr;
                clearingDeviceFailedMessage = ctx.getString(R.string.clearing_eobr_failed);


            }
        }

        protected void onPreExecute() {
            pd.setMessage(clearingEntireDeviceMessage);
            pd.show();
        }

        protected Void doInBackground(Void... params) {

            try {
                if (_eobrReader == null) {
                    return null;
                }

                if (_eobrReader.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
                    String password = String.format("EOBR%s", _eobrReader.getEobrSerialNumber());

                    try {
                        _eobrReader.Technician_ClearAllEobrData(password);
                    } catch (EobrException eobrEx) {
                        throw new KmbApplicationException(String.format(clearingDeviceFailedMessage, eobrEx.getReturnCode()));
                    }
                }
            } catch (Exception excp) {
                this.ex = excp;
            }

            return null;
        }

        protected void onProgressUpdate(String... updateVals) {
            if (!updateVals[0].equals("")) {
                pd.setMessage(updateVals[0]);
                pd.setProgress(0);
            }
            if (!updateVals[1].equals("")) {
                pd.setProgress(Integer.parseInt(updateVals[1]));
            }
        }

        protected void onPostExecute(Void unused) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }

                if (ex != null) {
                    HandleException(ex);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(unableToClearMessage);
                    builder.setMessage(ex.getMessage());
                    builder.setPositiveButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    builder1.setTitle(clearDevice);
                    builder1.setMessage(successfullyClearDeviceMessage);
                    builder1.setPositiveButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder1.show();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                pd = null;
            }
        }

    }

    /**
     * Reset the historical data stored in the EOBR
     * There must be an EOBR somewhat connected, but it doesn't have to be ONLINE
     */
    public boolean PerformPowerCycleResetEobr() {

        boolean isSuccessful = false;

        String macAddress = _eobrReader.EobrMacAddress();
        String serialNumber = _eobrReader.getEobrSerialNumber();
        if (macAddress != null && serialNumber != null && macAddress.length() > 0 && serialNumber.length() > 0 && _eobrReader.getCurrentConnectionState() != EobrReader.ConnectionState.SHUTDOWN) {

            try {
                // since the ELD will be disconnecting and rebooting, temporarily stop the service
                _eobrReader.IgnoreResume(true);
                _eobrReader.SuspendReading();

                // because we're about ready to power-cycle the ELD, abort the history reading process
                // it doesn't matter if we're actually reading history or not because the ELD will be reset regardless
                GlobalState.getInstance().setAbortReadingHistory(true);

                // sleep for 15 seconds - allow max time for any current calls to eobr to complete (or timeout)
                Thread.sleep(15000);

                _eobrReader.PerformPowerCycleReset(this.getContext());

                // when the power-cycle completes, update the reset date in the database
                EobrConfigController eobrConfigCtrlr = new EobrConfigController(this.getContext());
                eobrConfigCtrlr.UpdateEobrPowerCycleResetDate(serialNumber);

                // wait for the ELD to complete the reboot before attempting to discover it
                Thread.sleep(15000);
                isSuccessful = _eobrReader.WaitForEobrDiscovery(this.getContext(), macAddress, 2);
            } catch (Throwable excp) {
                isSuccessful = false;
            } finally {
                _eobrReader.ReadHistoryOnTimerPop();
                _eobrReader.IgnoreResume(false);
                _eobrReader.ResumeReading();
            }
        }

        return isSuccessful;
    }

    /**
     * Adds a malfunction as an active debug malfunction
     *
     * @param malfunction The malfunction to add
     */
    public void AddDebugMalfunction(Malfunction malfunction) throws Throwable {
        setDebugMalfunction(malfunction, true);

        switch (malfunction) {
            case POSITIONING_COMPLIANCE:
                setPositionComplianceMalfunction(true);
                break;
            default:
                ControllerFactory.getInstance().getEmployeeLogEldMandateController().createMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), malfunction);
                break;
        }


    }

    /**
     * Removes a malfunction from the active debug malfunctions
     *
     * @param malfunction The malfunction to remove
     */
    public void RemoveDebugMalfunction(Malfunction malfunction) throws Throwable {
        setDebugMalfunction(malfunction, false);

        switch (malfunction) {
            case POSITIONING_COMPLIANCE:
                setPositionComplianceMalfunction(false);
                break;
            default:
                ControllerFactory.getInstance().getEmployeeLogEldMandateController().clearMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), malfunction);
                break;
        }
    }

    private void setPositionComplianceMalfunction(boolean shouldMalfunction) throws Throwable {
        long millisecondDuration;
        ErrorAccumulator errorAccumulator = GlobalState.getInstance().getGpsErrors();

        if (shouldMalfunction) {
            millisecondDuration = TimeUnit.HOURS.toMillis(1);
            ControllerFactory.getInstance().getEmployeeLogEldMandateController().createMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), Malfunction.POSITIONING_COMPLIANCE);
        } else {
            millisecondDuration = 0;
            ControllerFactory.getInstance().getEmployeeLogEldMandateController().clearMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), Malfunction.POSITIONING_COMPLIANCE);
        }

        if (errorAccumulator != null) {
            errorAccumulator.adminDebugSetDuration(new Duration(millisecondDuration));
        }

    }

    /**
     * Gets all active debug malfunctions
     *
     * @return A {@link List} of all active debug malfunctions
     */
    public List<Malfunction> GetDebugMalfunctions() {
        List<Malfunction> result = new ArrayList<>();
        SharedPreferences preferences = getAdminControllerSharedPreferences();
        for (Malfunction malfunction : Malfunction.values()) {
            if (preferences.getBoolean(getDebugMalfunctionPreferencesKey(malfunction), false)) {
                result.add(malfunction);
            }
        }
        return result;
    }

    /**
     * Adds a data diagnostic as an active debug data diagnostic
     *
     * @param dataDiagnostic The data diagnostic to add
     */
    public void AddDebugDataDiagnostic(DataDiagnosticEnum dataDiagnostic) throws Throwable {
        setDebugDataDiagnostic(dataDiagnostic, true);
        ControllerFactory.getInstance().getEmployeeLogEldMandateController().CreateDataDiagnosticEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), dataDiagnostic, 0);
    }

    /**
     * Removes a data diagnostic from the active debug data diagnostics
     *
     * @param dataDiagnostic The data diagnostic to remove
     */
    public void RemoveDebugDataDiagnostic(DataDiagnosticEnum dataDiagnostic) throws Throwable {
        setDebugDataDiagnostic(dataDiagnostic, false);
        ControllerFactory.getInstance().getEmployeeLogEldMandateController().CreateDataDiagnosticClearedEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), dataDiagnostic, 0);
    }

    /**
     * Gets all active debug data diagnostics
     *
     * @return A {@link List} of all active data diagnostics
     */
    public List<DataDiagnosticEnum> GetDebugDataDiagnostics() {
        List<DataDiagnosticEnum> result = new ArrayList<>();
        SharedPreferences preferences = getAdminControllerSharedPreferences();
        for (DataDiagnosticEnum dataDiagnostic : DataDiagnosticEnum.values()) {
            if (preferences.getBoolean(getDebugDataDiagnosticPreferencesKey(dataDiagnostic), false)) {
                result.add(dataDiagnostic);
            }
        }
        return result;
    }

    private SharedPreferences getAdminControllerSharedPreferences() {
        return getContext().getSharedPreferences(ADMIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void setDebugMalfunction(Malfunction malfunction, boolean isEnabled) {
        if (malfunction != null) {
            SharedPreferences preferences = getAdminControllerSharedPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getDebugMalfunctionPreferencesKey(malfunction), isEnabled);
            editor.apply();
            EventBus.getDefault().post(malfunction);
        }

    }

    private void setDebugDataDiagnostic(DataDiagnosticEnum dataDiagnostic, boolean isEnabled) {
        if (dataDiagnostic != null) {
            SharedPreferences preferences = getAdminControllerSharedPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getDebugDataDiagnosticPreferencesKey(dataDiagnostic), isEnabled);
            editor.apply();
            EventBus.getDefault().post(dataDiagnostic);
        }
    }

    private String getDebugMalfunctionPreferencesKey(Malfunction malfunction) {
        return "debug_malfunctions_" + malfunction.getMalfunctionCode();
    }

    private String getDebugDataDiagnosticPreferencesKey(DataDiagnosticEnum dataDiagnostic) {
        return "debug_data_diagnostics_" + dataDiagnostic.getValue();
    }

    public void AddMissingDataDiagnostic(Enums.EmployeeLogEldEventType eventType) {
        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentDriversLog();

        ManipulableEobrReader eobrReader = new ManipulableEobrReader();
        HeavyBusFaker heavyBusFaker = new HeavyBusFaker();
        OdometerClearer odometerClearer = new OdometerClearer();
        DistHoursRuntimeClearer distHoursRuntimeClearer = new DistHoursRuntimeClearer(getContext());
        StatusBufferRuntimeClearer statusBufferRuntimeClearer = new StatusBufferRuntimeClearer();

        switch (eventType) {
            case DutyStatusChange:
                currentLog = CreateNewDutyStatusChangeEvent(mandateController, currentLog, true);

                break;
            case IntermediateLog:
                TripReport tripReport = new TripReport();
                tripReport.setFixUncert((short) 3);
                tripReport.setDriverId(1);
                tripReport.setLatitude(0);
                tripReport.setLongitude(0);

                Date tempDrivingTimestamp = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
                GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);

                try {
                    mandateController.CreateIntermediateEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.IntermediateLog, tripReport);

                } catch (Throwable throwable) {
                    Log.e("UnhandledCatch", throwable.getMessage() + ": " + Log.getStackTraceString(throwable));
                }

                //Explicitly save log
                currentLog = GlobalState.getInstance().getCurrentDriversLog();
                mandateController.SaveLocalEmployeeLog(currentLog);

                //Create event after intermediate event to avoid RODS being stuck in driving
                currentLog = CreateNewDutyStatusChangeEvent(mandateController, currentLog, false);

                //reset modified variables
                GlobalState.getInstance().setPotentialDrivingStopTimestamp(tempDrivingTimestamp);
                break;
            case ChangeInDriversIndication:
                Location location = GlobalState.getInstance().getLastLocation();

                try {
                    mandateController.CreateChangeInDriversIndicationEvent(currentLog, DateUtility.getCurrentDateTimeUTC(), location, null, EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, null);
                } catch (Throwable throwable) {
                    Log.e("UnhandledCatch", throwable.getMessage() + ": " + Log.getStackTraceString(throwable));
                }

                //Explicitly save log
                mandateController.SaveLocalEmployeeLog(currentLog);
                break;
            case LoginLogout:
                try {
                    //login/logout events need valid odometer readings and engine hour readings if connected to a heavy bus
                    mandateController = new EmployeeLogEldMandateController(getContext(), eobrReader);

                    odometerClearer.register(eobrReader);

                    mandateController.CreateLoginLogoutEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.LoginEvent);

                    odometerClearer.unregister(eobrReader);
                    heavyBusFaker.register(eobrReader);
                    distHoursRuntimeClearer.register(eobrReader);
                    statusBufferRuntimeClearer.register(eobrReader);

                    mandateController.CreateLoginLogoutEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.LogoutEvent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case EnginePowerUpPowerDown:
                try {
                    //powerup/powerdown events need valid odometer readings, engine hours if on a heavy bus, and valid
                    //gps readings if the distance traveled since last GPS reading is less than a certain threshold
                    mandateController = new EmployeeLogEldMandateController(getContext(), eobrReader);

                    EventRecord eventRecord = getLatestEventRecord(eobrReader);
                    StatusRecord statusRecord = getLatestStatusRecord(eobrReader);
                    statusRecord.setOdometerReading(-1);
                    statusRecord.setActiveBusType(DatabusTypeEnum.J1939);
                    eventRecord.setStatusRecordData(statusRecord);
                    eventRecord.setTimecode(TimeKeeper.getInstance().getCurrentDateTime().getMillis());

                    mandateController.CreateEnginePowerUpOrShutDownEvent(null, eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent);
                    Thread.sleep(1000);

                    distHoursRuntimeClearer.register(eobrReader);
                    statusBufferRuntimeClearer.register(eobrReader);
                    statusRecord.setOdometerReading(100);
                    eventRecord.setTimecode(TimeKeeper.getInstance().getCurrentDateTime().getMillis());

                    mandateController.CreateEnginePowerUpOrShutDownEvent(null, eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EngineShutDownEvent);
                    Thread.sleep(1000);

                    eobrReader.removeManipulators();
                    statusRecord.setGpsUncertDistance(3);
                    statusRecord.setGpsLatitude(0);
                    statusRecord.setGpsLongitude(0);
                    eventRecord.setTimecode(TimeKeeper.getInstance().getCurrentDateTime().getMillis());

                    mandateController.CreateEnginePowerUpOrShutDownEvent(null, eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent);

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case Malfunction_DataDiagnosticDetection:
                try {
                    //malfunction events need odometer readings, and engine hour readings if on a heavy bus
                    mandateController = new EmployeeLogEldMandateController(getContext(), eobrReader);

                    odometerClearer.register(eobrReader);
                    Date malfunctionTime = new Date(TimeKeeper.getInstance().getCurrentDateTime().getMillis() - 10000);

                    mandateController.createMalfunctionForLoggedInUsers(malfunctionTime, Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE);
                    Thread.sleep(5000);

                    odometerClearer.unregister(eobrReader);
                    heavyBusFaker.register(eobrReader);
                    distHoursRuntimeClearer.register(eobrReader);
                    statusBufferRuntimeClearer.register(eobrReader);
                    malfunctionTime = new Date(TimeKeeper.getInstance().getCurrentDateTime().getMillis() - 5000);

                    mandateController.createMalfunctionForLoggedInUsers(malfunctionTime, Malfunction.TIMING_COMPLIANCE);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        LogEntryController logController = ControllerFactory.getInstance().getLogEntryController();
        logController.setCurrentEmployeeLog(currentLog);
    }

    private EventRecord getLatestEventRecord(ManipulableEobrReader eobrReader) {
        EventRecord eventRecord = new EventRecord();
        eventRecord.setRecordId(-1);
        eobrReader.Technician_GetEventData(eventRecord, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), new EventTypeEnum(EventTypeEnum.ANYTYPE), -1, false);
        return eventRecord;
    }

    private StatusRecord getLatestStatusRecord(ManipulableEobrReader eobrReader) {
        StatusRecord statusRecord = new StatusRecord();
        statusRecord.setRecordId(-1);
        eobrReader.Technician_GetCurrentData(statusRecord, false);

        return statusRecord;
    }

    private EmployeeLog CreateNewDutyStatusChangeEvent(EmployeeLogEldMandateController mandateController, EmployeeLog currentLog, boolean isMissingDataEvent) {
        Date eventEndTime = DateUtility.getCurrentDateTimeUTC();
        EmployeeLogEldEvent editedEvent = new EmployeeLogEldEvent();
        editedEvent.setLogKey((int) currentLog.getPrimaryKey());
        editedEvent.setEventCode(EmployeeLogEldEventCode.DutyStatus_Sleeper);
        editedEvent.setEventDateTime(LocalDateTime.fromDateFields(eventEndTime).plusMillis(-30).toDate());

        String comment = isMissingDataEvent ? null : "commment";
        editedEvent.setEventComment(comment);

        try {
            mandateController.saveEldEvent(editedEvent, eventEndTime);
        } catch (Throwable throwable) {
            Log.e("UnhandledCatch", throwable.getMessage() + ": " + Log.getStackTraceString(throwable));
        }

        return mandateController.GetLocalEmployeeLog(GlobalState.getInstance().getCurrentUser(), currentLog.getLogDate());
    }
}

