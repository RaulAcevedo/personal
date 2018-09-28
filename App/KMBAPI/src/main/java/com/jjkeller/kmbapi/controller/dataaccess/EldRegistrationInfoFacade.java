package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.EldRegistrationInfoPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EldRegistrationInfo;

import java.util.Date;
import java.util.List;

public class EldRegistrationInfoFacade extends FacadeBase {

    public EldRegistrationInfoFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public List<EldRegistrationInfo> Fetch() {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchList();
    }

    public Date FetchOldestChangeDate() {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchOldestChangeDate();
    }

    public List<EldRegistrationInfo> fetchRegistrationInfoByTypeAndVersion(String type, String version) {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.fetchRegistrationInfoByTypeAndVersion(type, version);
    }

    public void Save(List<EldRegistrationInfo> list) {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());

        for (EldRegistrationInfo regInfo : list)
            persist.Persist(regInfo);
    }

    public List<EldRegistrationInfo> fetchDefaultRegistrationInfoByType(String type) {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.fetchDefaultRegistrationInfoByType(type);
    }

    public void Save(EldRegistrationInfo eldRegistrationInfoData) {
        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());
        persist.Persist(eldRegistrationInfoData);
    }

    public void DeleteAll() {
        List<EldRegistrationInfo> allRecords = Fetch();

        EldRegistrationInfoPersist<EldRegistrationInfo> persist = new EldRegistrationInfoPersist<>(
                EldRegistrationInfo.class, this.getContext(), GlobalState.getInstance().getCurrentUser());

        for (EldRegistrationInfo record : allRecords) {
            persist.Delete(record);
        }
    }
}
