package com.jjkeller.kmb.share;

import android.content.Intent;
import android.os.Bundle;

import com.jjkeller.kmb.RoadsideInspectionDataTransfer;
import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.interfaces.ILogout.LogoutControllerMethods;
import com.jjkeller.kmb.interfaces.ILogout.LogoutFragActions;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbui.R;

import java.util.Date;

public abstract class LogoutBaseActivity extends BaseActivity implements LogoutFragActions, LogoutControllerMethods {

    protected boolean _dataTransferWorkflow = false;

    protected LoginController getMyController() {
        return (LoginController) this.getController();
    }

    private LogEntryController _logEntryController = null;

    public LogEntryController getMyLogEntryController() {
        if (_logEntryController == null) {
            _logEntryController = new LogEntryController(this);
        }
        return _logEntryController;
    }

    private EmployeeLogEldMandateController _employeeLogEldMandateController = null;

    public EmployeeLogEldMandateController getMyEmployeeLogELDMandateController() {
        if (_employeeLogEldMandateController == null) {
            _employeeLogEldMandateController = new EmployeeLogEldMandateController(this);
        }
        return _employeeLogEldMandateController;
    }

    @Override
    protected void InitController() {
        this.setController(new LoginController(this));
    }

    @Override
    protected void Return(boolean exitApp) {
        if (mRetryDialog == null || !mRetryDialog.isShowing()) {
            this.finish();

            GlobalState.getInstance().setIsUserLoggingOut(false);

            Bundle extras = new Bundle();
            if (this._dataTransferWorkflow) {
                extras.putBoolean(this.getString(R.string.data_transfer_workflow), true);
                this.startActivity(RoadsideInspectionDataTransfer.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
            } else if (isExitingAppOrActivityIsStillVisible(exitApp)) {
                extras.putBoolean(this.getString(R.string.exit), exitApp);
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
            }

            UnlockScreenRotation();
        }
    }

    /**
     * It's possible another screen has popped up, like Unidentified Events.
     * We don't want to dismiss that screen if it did, unless we're exiting the app.
     */
    private boolean isExitingAppOrActivityIsStillVisible(boolean exitApp) {
        return exitApp || IsCurrentActivity(getClass());
    }

    /**
     * Check the validity of the off-duty time chosen.
     * The only problem is when 'Exact Time' is chosen, because the selected time must be in the future.
     * Returns whether or not the time is valid.
     * @return true if the time is valid
     */
    protected abstract boolean Validate();

    protected abstract Date CreateExactTime();

    public Date getCurrentClockHomeTerminalTime() {
        return this.getMyController().getCurrentClockHomeTerminalTime();
    }

    @Override
    public boolean canUseOffDutyWellSite() {
        return GlobalState.getInstance().getCurrentUser() != null && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isAnyOilFieldRuleset() && GlobalState.getInstance().getCurrentEmployeeLog() != null && GlobalState.getInstance().getCurrentEmployeeLog().getIsOperatesSpecificVehiclesForOilfield();
    }

    protected boolean isEldMandateEnabled() {
        return GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
    }

	public boolean ShouldShowManualLocation(){
		ELDCommon eldCommon = new ELDCommon(getIntent(),this.getResources(),getMyLogEntryController(),getMyEmployeeLogELDMandateController());
		return !this.getIsExemptFromELDUse() && eldCommon.ShouldShowManualLocation();
	}
}
