package com.jjkeller.kmbapi.controller;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheck;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheckHandler;

import java.io.IOException;

public class EncompassAppUpdateCheck implements IAppUpdateCheck {
    private IAppUpdateCheckHandler _handler = null;

    @Override
    public void addHandler(IAppUpdateCheckHandler handler) { _handler = handler; }

    @Override
    public boolean isAppUpdateAvailable(boolean checkForAutoUpdate) throws KmbApplicationException {
        boolean updateAvailable = false;
        if (NetworkUtilities.VerifyWebServiceConnection(_handler.getContext())) {
            // first, if the system has already run, then clean up first
            _handler.cleanUp();

            // fetch the currently executing version of the app.
            String currentVersion = GlobalState.getInstance().getPackageVersionName();

            try {
                String appName = "KellerMobileAndroid";
                if(_handler.getPackageName().equals(_handler.getContext().getString(R.string.alkpackage))) {
                    appName = "KellerMobileALK";
                }

                RESTWebServiceHelper serviceHelper = new RESTWebServiceHelper(_handler.getContext());
                _handler.setApplicationUpdateInfo(serviceHelper.CheckForUpdates(appName, GlobalState.getInstance().getHardwarePlatform(), GlobalState.getInstance().getCPUArchitecture(), currentVersion, checkForAutoUpdate));
                updateAvailable = _handler.getApplicationUpdateInfo() != null && _handler.getApplicationUpdateInfo().getIsAvailable();
            }
            catch (JsonSyntaxException e) {
                _handler.HandleExceptionAndThrow(e, _handler.getContext().getString(R.string.downloadcompanyconfigsettings), _handler.getContext().getString(R.string.exception_webservicecommerror), _handler.getContext().getString(R.string.exception_serviceunavailable));
            }
            catch (JsonParseException jpe) {
                // when connected to a network, but unable to get to webservice "e" is null
                if(jpe == null)
                    jpe = new JsonParseException(JsonParseException.class.getName());
                _handler.HandleExceptionAndThrow(jpe, _handler.getContext().getString(R.string.downloadcompanyconfigsettings), _handler.getContext().getString(R.string.exception_webservicecommerror), _handler.getContext().getString(R.string.exception_serviceunavailable));
            }
            catch (IOException e) {
                ErrorLogHelper.RecordException(_handler.getContext(), e);
                e.printStackTrace();
            }
        }

        return updateAvailable;
    }
}
