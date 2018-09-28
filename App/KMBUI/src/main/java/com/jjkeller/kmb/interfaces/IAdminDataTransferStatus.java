package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.DataTransferMechanismStatusController;

/**
 * Created by t000622 on 3/28/2017.
 */

public interface IAdminDataTransferStatus {
    interface AdminDataTransferStatusFragControllerMethods{
        DataTransferMechanismStatusController getMyController();
    }

    interface AdminDataTransferStatusFragActions{
        void handleAdminAddDataTransferFailureClick();
        void handleAdminAddDataTransferSuccessClick();
        void handleAdminClearDataTransferRecordsClick();
    }
}
