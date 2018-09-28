package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.ApplicationStatePersist;
import com.jjkeller.kmbapi.proxydata.ApplicationStateSettings;

public class ApplicationStateFacade extends FacadeBase{

    public ApplicationStateFacade(Context ctx) { super(ctx); }

    public ApplicationStateSettings Fetch() {
        ApplicationStatePersist<ApplicationStateSettings> persist = new ApplicationStatePersist<ApplicationStateSettings>(ApplicationStateSettings.class, this.getContext());
        return persist.Fetch();
    }

    public void Save(ApplicationStateSettings ApplicationStateSettings) {
        ApplicationStatePersist<ApplicationStateSettings> persist = new ApplicationStatePersist<ApplicationStateSettings>(ApplicationStateSettings.class, this.getContext());
        persist.Persist(ApplicationStateSettings);
    }
}
