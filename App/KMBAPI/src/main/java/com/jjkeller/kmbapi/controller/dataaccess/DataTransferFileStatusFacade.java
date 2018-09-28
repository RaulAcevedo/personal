package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.DataTransferFileStatusPersist;
import com.jjkeller.kmbapi.controller.dataaccess.db.DataTransferMechanismStatusPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbapi.proxydata.DataTransferFileStatus;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;

public class DataTransferFileStatusFacade extends FacadeBase {
    public DataTransferFileStatusFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public DataTransferFileStatus Fetch()
    {
        DataTransferFileStatusPersist<DataTransferFileStatus> persist = new DataTransferFileStatusPersist<>(DataTransferFileStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.Fetch();
    }

    public void Save(DataTransferFileStatus DataTransferFileStatusData)
    {
        DataTransferFileStatusPersist<DataTransferFileStatus> persist = new DataTransferFileStatusPersist<>(DataTransferFileStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        persist.Persist(DataTransferFileStatusData);
    }

    public DataTransferFileStatus GetLatestByTransferMethod(RoadsideDataTransferMethodEnum dataTransferMethod)
    {
        DataTransferFileStatusPersist<DataTransferFileStatus> persist = new DataTransferFileStatusPersist<>(DataTransferFileStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchLatestByTransferMethod(dataTransferMethod);
    }
}
