package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;
import com.jjkeller.kmbapi.controller.dataaccess.db.DataTransferMechanismStatusPersist;


import java.util.List;

/**
 * Created by KLarsen on 2/10/2017.
 */

public class DataTransferMechanismStatusFacade extends FacadeBase {
    public DataTransferMechanismStatusFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public DataTransferMechanismStatus Fetch()
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.Fetch();
    }

    public void Save(DataTransferMechanismStatus DataTransferMechanismStatusData)
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        persist.Persist(DataTransferMechanismStatusData);
    }

   public DataTransferMechanismStatus GetByTransferId(String transferId)
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchByTransferId(transferId);
    }

    public DataTransferMechanismStatus GetLastFailedTransfer(String transferId)
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchLastFailedTransfer(transferId);
    }

    public List<DataTransferMechanismStatus> GetLastFourTransfers()
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchLastFourTransfers();
    }

    public DataTransferMechanismStatus FetchCurrentTransfer()
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchCurrentTransfer();
    }

    public void DeleteAllTransfers()
    {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        persist.DeleteAllTransfers();
    }
     /*public DataTransferMechanismStatus GetAuthorityByAuthorityId(String motionPictureAuthorityId) {
        DataTransferMechanismStatusPersist<DataTransferMechanismStatus> persist = new DataTransferMechanismStatusPersist<>(DataTransferMechanismStatus.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchAuthorityByAuthorityId(motionPictureAuthorityId);
    }
    */
}
