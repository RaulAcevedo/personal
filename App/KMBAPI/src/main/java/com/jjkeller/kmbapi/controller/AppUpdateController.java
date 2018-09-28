package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdate;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheck;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheckHandler;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateHandler;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.proxydata.ApplicationUpdateInfo;

import java.io.IOException;

public class AppUpdateController extends ControllerBase
implements IAppUpdateCheckHandler, IAppUpdateHandler {

    /// <summary>
    /// return value from webservice call for updates
    /// </summary>
    private ApplicationUpdateInfo _info = null;

	@Override
	public void setApplicationUpdateInfo(ApplicationUpdateInfo applicationUpdateInfo) {
		_info = applicationUpdateInfo;
	}

	@Override
	public ApplicationUpdateInfo getApplicationUpdateInfo() {
		return _info;
	}

	private IAppUpdateCheck _appUpdateCheck = AppUpdateFactory.getInstance().getAppUpdateCheckComponent();
	public IAppUpdateCheck getAppUpdateCheck() {
		return _appUpdateCheck;
	}

	private IAppUpdate _appUpdate = AppUpdateFactory.getInstance().getAppUpdateComponent();
	public IAppUpdate getAppUpdate() {
		return _appUpdate;
	}

	public AppUpdateController(Context ctx) {
		super(ctx);

		_appUpdateCheck.addHandler(this);
		_appUpdate.addHandler(this);
	}

	//TODO implement DBUpdate
	/*
        /// <summary>
        /// Checks if there's a DB update to perform
        /// </summary>
        /// <returns>bool</returns>
        public bool IsDBUpdateRequired()
        {
            bool updateRequired = false;

            DBMigrationFacade facade = new DBMigrationFacade(this.CurrentUser);
            updateRequired = facade.IsDatabaseUpgradeRequired();
            return updateRequired;
        }

        /// <summary>
        /// Performs data migration to a new version of the DB
        /// </summary>
        /// <returns>bool</returns>
        public bool PerformDBUpdate()
        {
            bool isSuccessful = false;

            DBMigrationFacade facade = new DBMigrationFacade(this.CurrentUser);
            isSuccessful = facade.PerformDatabaseMigration();

            return isSuccessful;
        }
	*/
	
	/**
	 * Returns the new application version, assuming that an update is available.
	 * If no update is available, then null is returned.
	 * @return The new application version, or null if no update is available
	 */
    public String getNewUpdateVersion()
    {
    	String newVersion = null;

    	if (_info != null && _info.getIsAvailable())
    	{
    		newVersion = _info.getNewVersion();
    	}

    	return newVersion; 
    }

    /**
     * Returns true if the new application version contains an EOBR firmware update.
     * If no update is available, then false is returned.
     * @return true if the new application version contains an EOBR firmware update, and false otherwise
     */
	public boolean isFirmwareUpdateIncluded() {
		boolean isFirmware = false;
		
		if(_info != null && _info.getIsAvailable())
		{
			isFirmware = _info.getIncludesFirmwareUpdate();
		}
		
		return isFirmware;
	}
	
	/**
	 * Returns true if compliance tablets must use wifi for this update
	 * @return true if wifi is required for compliance tablets, and false otherwise
	 */
	public boolean isWifiRequired()
	{
		boolean isWifiRequired = false;
		if ( this.getPackageName().equals(this.getContext().getString(R.string.alkpackage)) && _info != null && _info.getIsAvailable()) {
			isWifiRequired = _info.isWifiRequired();
		}
		return isWifiRequired;
	}

	@Override
	public void cleanUp() throws KmbApplicationException
	{
		try {
			this.getAppUpdate().shutDown();
		} catch (IOException e) {
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.isappupdateavailable), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
	}

	@Override
	public String getPackageName() {
		return GlobalState.getInstance().getPackageName();
	}


}
