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

public class DummyAppUpdateCheck implements IAppUpdateCheck {
    @Override
    public void addHandler(IAppUpdateCheckHandler handler) { }

    @Override
    public boolean isAppUpdateAvailable(boolean checkForAutoUpdate) throws KmbApplicationException { return false; }
}
